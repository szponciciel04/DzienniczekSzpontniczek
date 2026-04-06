@file:OptIn(ExperimentalTime::class)

package io.github.szpontium.api.hebe.signer


import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.DelicateCryptographyApi
import dev.whyoleg.cryptography.algorithms.RSA
import dev.whyoleg.cryptography.algorithms.SHA1
import dev.whyoleg.cryptography.algorithms.SHA256
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.DateTimeComponents.Companion.Format
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.alternativeParsing
import kotlinx.datetime.format.char
import kotlinx.datetime.format.optional
import kotlin.time.Clock
import kotlin.time.Instant
import net.thauvin.erik.urlencoder.UrlEncoderUtil
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.time.Duration.Companion.days
import kotlin.time.ExperimentalTime

object HebeX509Signer {
    private fun ByteArray.toHexString() = joinToString("") { byte ->
        val hex = byte.toInt() and 0xFF
        hex.toString(16).padStart(2, '0')
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun generateKeyPair(): Triple<String, String, String> {
        val crypto = CryptographyProvider.Default.get(RSA.PKCS1)
        val keyPair = crypto.keyPairGenerator().generateKeyBlocking()
        val publicKey = Base64.encode(keyPair.publicKey.encodeToByteArrayBlocking(RSA.PublicKey.Format.DER))
        val privateKey = Base64.encode(keyPair.privateKey.encodeToByteArrayBlocking(RSA.PrivateKey.Format.DER))
        val publicHash = CryptographyProvider.Default.get(SHA256).hasher().hashBlocking(publicKey.encodeToByteArray()).toHexString()
        return Triple(publicKey, privateKey, publicHash)
    }

    @OptIn(ExperimentalEncodingApi::class, DelicateCryptographyApi::class)
    fun generateCertificate(publicPem: String, privatePem: String): Pair<String, String> {
        val crypto = CryptographyProvider.Default.get(RSA.PKCS1)
        val privateKey = crypto.privateKeyDecoder(SHA256).decodeFromByteArrayBlocking(
            RSA.PrivateKey.Format.DER, Base64.decode(privatePem)
        )


        val publicKey = crypto.publicKeyDecoder(SHA256).decodeFromByteArrayBlocking(
            RSA.PublicKey.Format.DER, Base64.decode(publicPem)
        )

        val notBefore = Clock.System.now()
        val notAfter = notBefore + (20 * 365).days // Approximate for 20 years

        val cert = X509Generator(X509Generator.Algorithm.RSA_SHA256)
            .generate(subject = mapOf("CN" to "APP_CERTIFICATE CA Certificate"),
                notBefore = notBefore,
                notAfter = notAfter,
                serialNumber = 1,
                publicKey = publicKey,
                privateKey = privateKey
            )

        val provider = CryptographyProvider.Default

        val certificatePem = Base64.encode(cert)
        val certificateHash = provider.get(SHA1).hasher().hashBlocking(cert).toHexString()
        return Pair(certificatePem, certificateHash)
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun getDigest(body: String?): String? {
        if (body == null) return null
        return Base64.encode(CryptographyProvider.Default.get(SHA256).hasher().hashBlocking(body.encodeToByteArray()))
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun getSignatureValue(values: String, privatePem: String): String {
        val crypto = CryptographyProvider.Default.get(RSA.PKCS1)
        val privateKey = crypto.privateKeyDecoder(SHA256).decodeFromByteArrayBlocking(
            RSA.PrivateKey.Format.DER, Base64.decode(privatePem)
        )
        return Base64.encode(privateKey.signatureGenerator().generateSignatureBlocking(values.encodeToByteArray()))
    }

    private fun getEncodedPath(path: String): String {
        val url = ("(api/mobile/.+)".toRegex().find(path))
            ?: throw IllegalArgumentException("The URL does not seem correct (does not match `(api/mobile/.+)` regex)")

        return UrlEncoderUtil.encode(url.groupValues[0], "UTF-8").lowercase()
    }

    private fun getHeaders(digest: String?, canonicalUrl: String, timestamp: Instant): MutableMap<String, String> {
        val headers = mutableMapOf<String, String>()
        headers["vCanonicalUrl"] = canonicalUrl
        if (digest != null) headers["Digest"] = digest
        headers["vDate"] = timestamp.format(RFC_1123_WITH_ZERO)
        return headers
    }

    fun getSignatureHeaders(
        keyId: String,
        privatePem: String,
        body: String?,
        requestPath: String,
        timestamp: Instant
    ): Map<String, String> {
        val canonicalUrl = getEncodedPath(requestPath)
        val digest = getDigest(body)
        val headers = getHeaders(digest, canonicalUrl, timestamp)
        val headerNames = headers.keys.joinToString(" ")
        val headerValues = headers.values.joinToString("")
        val signatureValue = getSignatureValue(headerValues, privatePem)

        if (body != null) headers["Digest"] = "SHA-256=${digest}"
        headers["Signature"] = """keyId="$keyId",headers="$headerNames",algorithm="sha256withrsa",signature=Base64(SHA256withRSA($signatureValue))"""

        return headers
    }
}

val RFC_1123_WITH_ZERO: DateTimeFormat<DateTimeComponents> = Format {
    alternativeParsing({
        // the day of week may be missing
    }) {
        dayOfWeek(DayOfWeekNames.ENGLISH_ABBREVIATED)
        chars(", ")
    }
    day(Padding.ZERO)
    char(' ')
    monthName(MonthNames.ENGLISH_ABBREVIATED)
    char(' ')
    year()
    char(' ')
    hour()
    char(':')
    minute()
    optional {
        char(':')
        second()
    }
    chars(" ")
    alternativeParsing({
        chars("UT")
    }, {
        chars("Z")
    }) {
        optional("GMT") {
            offset(UtcOffset.Formats.FOUR_DIGITS)
        }
    }
}