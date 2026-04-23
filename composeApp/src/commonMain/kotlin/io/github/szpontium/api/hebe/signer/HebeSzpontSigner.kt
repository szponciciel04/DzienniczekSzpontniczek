package io.github.szpontium.api.hebe.signer

import dev.whyoleg.cryptography.BinarySize.Companion.bits
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.DelicateCryptographyApi
import dev.whyoleg.cryptography.algorithms.MD5
import dev.whyoleg.cryptography.algorithms.RSA
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
import kotlin.time.Instant
import net.thauvin.erik.urlencoder.UrlEncoderUtil
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

object HebeSzpontSigner {

    /**
     * Represents the generated RSA key pair along with its MD5 fingerprint.
     */
    data class KeyPairInfo(
        val publicKey: String,
        val privateKey: String,
        val fingerprint: String
    )

    private val crypto = CryptographyProvider.Default
    private val rsaAlgorithm = crypto.get(RSA.PKCS1)

    /**
     * Generates a new 2048-bit RSA key pair.
     * Returns a [KeyPairInfo] which supports destructuring.
     */
    @OptIn(ExperimentalEncodingApi::class, DelicateCryptographyApi::class)
    fun generateKeyPair(): KeyPairInfo {
        val keyPair = rsaAlgorithm.keyPairGenerator(2048.bits).generateKeyBlocking()

        val publicDer = keyPair.publicKey.encodeToByteArrayBlocking(RSA.PublicKey.Format.DER)
        val privateDer = keyPair.privateKey.encodeToByteArrayBlocking(RSA.PrivateKey.Format.DER)

        val publicBase64 = Base64.encode(publicDer)
        val privateBase64 = Base64.encode(privateDer)

        val fingerprint = crypto.get(MD5)
            .hasher()
            .hashBlocking(wrapInPem("PUBLIC KEY", publicBase64).encodeToByteArray())
            .toHexString()

        return KeyPairInfo(publicBase64, privateBase64, fingerprint)
    }

    /**
     * Computes the SHA-256 digest of the request body.
     */
    @OptIn(ExperimentalEncodingApi::class)
    fun getDigest(body: String?): String? = body?.let {
        Base64.encode(crypto.get(SHA256).hasher().hashBlocking(it.encodeToByteArray()))
    }

    /**
     * Generates headers required for authenticating requests to Vulcan Hebe API.
     */
    fun getSignatureHeaders(
        keyId: String,
        privatePem: String,
        body: String?,
        requestPath: String,
        timestamp: Instant
    ): Map<String, String> {
        val canonicalUrl = formatCanonicalUrl(requestPath)
        val digestValue = getDigest(body)
        val formattedDate = timestamp.format(SignatureDateFormat)

        val signedHeaders = buildMap {
            put("vCanonicalUrl", canonicalUrl)
            digestValue?.let { put("Digest", it) }
            put("vDate", formattedDate)
        }

        val signatureValue = signContent(signedHeaders.values.joinToString(""), privatePem)

        return buildMap {
            put("vCanonicalUrl", canonicalUrl)
            put("vDate", formattedDate)
            digestValue?.let { put("Digest", "SHA-256=$it") }
            put("Signature", assembleSignature(keyId, signedHeaders.keys.joinToString(" "), signatureValue))
        }
    }

    private fun formatCanonicalUrl(path: String): String {
        val match = "(api/mobile/.+)".toRegex().find(path)
            ?: throw IllegalArgumentException("URL must match `(api/mobile/.+)` pattern: $path")
        return UrlEncoderUtil.encode(match.groupValues[0], "UTF-8").lowercase()
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun signContent(content: String, privatePem: String): String {
        val privateKey = rsaAlgorithm.privateKeyDecoder(SHA256)
            .decodeFromByteArrayBlocking(RSA.PrivateKey.Format.DER, Base64.decode(privatePem))

        val signature = privateKey.signatureGenerator()
            .generateSignatureBlocking(content.encodeToByteArray())

        return Base64.encode(signature)
    }

    private fun wrapInPem(label: String, content: String): String = buildString {
        appendLine("-----BEGIN $label-----")
        appendLine(content.chunked(64).joinToString("\n"))
        appendLine("-----END $label-----")
    }

    private fun assembleSignature(keyId: String, headers: String, signature: String): String =
        """keyId="$keyId",headers="$headers",algorithm="sha256",signature=Base64(sha256withrsa($signature))"""

    private fun ByteArray.toHexString() = joinToString("") {
        it.toUByte().toString(16).padStart(2, '0')
    }

    private val SignatureDateFormat: DateTimeFormat<DateTimeComponents> = Format {
        alternativeParsing({}) {
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
        alternativeParsing({ chars("UT") }, { chars("Z") }) {
            optional("GMT") {
                offset(UtcOffset.Formats.FOUR_DIGITS)
            }
        }
    }
}