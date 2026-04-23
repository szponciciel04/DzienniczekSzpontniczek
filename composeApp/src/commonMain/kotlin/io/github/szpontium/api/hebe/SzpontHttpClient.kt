@file:OptIn(ExperimentalTime::class)

package io.github.szpontium.api.hebe

import io.github.szpontium.api.hebe.credentials.ICredential
import io.github.szpontium.api.hebe.models.EnvelopeResponse
import io.github.szpontium.api.hebe.signer.HebeSzpontSigner
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val USER_AGENT = "Dart/3.10 (dart:io)"
private const val API_VERSION = 1

class SzpontHttpClient(
    private val credential: ICredential,
    private val appName: String,
    private val appVersion: String,
    private val appVersionCode: String,
    private val httpClient: HttpClient
) {

    @OptIn(ExperimentalUuidApi::class)
    private fun buildBody(envelope: JsonElement): String {
        val now = Clock.System.now()
        val localNow = now.toLocalDateTime(TimeZone.currentSystemDefault())
        val formatted = localNow.toVulcanString()
        val body = buildJsonObject {
            put("AppName", appName)
            put("AppVersion", appVersion)
            put("NotificationToken", credential.notificationToken ?: "")
            put("API", API_VERSION)
            put("RequestId", Uuid.random().toString())
            put("Timestamp", now.epochSeconds)
            put("TimestampFormatted", formatted)
            put("Envelope", envelope)
        }
        return szpontJson.encodeToString(body)
    }

    private fun serializeQuery(query: Map<String, Any?>): Map<String, String> {
        return query.mapNotNull { (key, value) ->
            if (value == null) null
            else key to when (value) {
                is String -> value
                is LocalDate -> value.toString()
                is LocalDateTime -> value.toVulcanString()
                else -> value.toString()
            }
        }.toMap()
    }

    suspend fun request(
        method: String,
        endpoint: String,
        restUrl: String,
        pupilId: Int? = null,
        query: Map<String, Any?>? = null,
        payload: JsonElement? = null,
        verifyResponse: Boolean = true
    ): JsonElement? {
        val url = "$restUrl/$endpoint"
        val body = if (payload != null) buildBody(payload) else null
        val now = Clock.System.now()

        val signatureHeaders = HebeSzpontSigner.getSignatureHeaders(
            keyId = credential.fingerprint,
            privatePem = credential.privateKey,
            body = body,
            requestPath = url,
            timestamp = now
        )

        val response = try {
            httpClient.request(url) {
                this.method = HttpMethod.parse(method)
                header("vOS", credential.deviceOs)
                header("vVersionCode", appVersionCode)
                header("vAPI", API_VERSION.toString())
                header("User-Agent", USER_AGENT)
                if (pupilId != null) header("vHint", pupilId.toString())
                if (body != null) {
                    header("Content-Type", "application/json; charset=utf-8")
                } else {
                    header("Content-Type", "application/json")
                }
                signatureHeaders.forEach { (k, v) -> header(k, v) }
                query?.let { serializeQuery(it).forEach { (k, v) -> parameter(k, v) } }
                if (body != null) setBody(body)
            }
        } catch (e: SzpontApiException) {
            throw e
        } catch (e: Exception) {
            throw FailedRequestException(e.message ?: "Request failed")
        }

        if (response.status.value != 200) {
            throw HttpUnsuccessfulStatusException("${response.status.value}: ${response.bodyAsText()}")
        }

        val responseText = response.bodyAsText()

        if ("!DOCTYPE" in responseText) {
            throw ResponseInvalidContentTypeException()
        }

        if (!verifyResponse) return null

        val responseEnvelope = szpontJson.decodeFromString<EnvelopeResponse>(responseText)
        checkEnvelopeStatus(responseEnvelope.status.code, responseEnvelope.status.message)
        return responseEnvelope.envelope
    }

    private fun checkEnvelopeStatus(code: Int, message: String) {
        when (code) {
            0 -> return
            -1 -> throw InternalServerErrorException(message)
            100 -> throw InvalidSignatureException(message)
            101 -> throw InvalidBodyModelException(message)
            102 -> throw MissingHeaderException(message)
            103 -> throw InvalidHeaderException(message)
            104 -> throw MissingUnitSymbolException(message)
            154 -> throw CertificateNotFoundException(message)
            200 -> throw EntityNotFoundException(message)
            201 -> throw UsedTokenException(message)
            202 -> throw WrongTokenException(message)
            203 -> throw WrongPinException(message)
            204 -> throw ExpiredTokenException(message)
            206 -> throw InvalidParameterValueException(message)
            214 -> throw ConstraintViolationException(message)
            else -> throw SzpontApiException("$code: $message")
        }
    }
}

private fun LocalDateTime.toVulcanString(): String {
    val m = month.number.toString().padStart(2, '0')
    val d = day.toString().padStart(2, '0')
    val h = hour.toString().padStart(2, '0')
    val min = minute.toString().padStart(2, '0')
    val s = second.toString().padStart(2, '0')
    return "$year-$m-$d $h:$min:$s"
}
