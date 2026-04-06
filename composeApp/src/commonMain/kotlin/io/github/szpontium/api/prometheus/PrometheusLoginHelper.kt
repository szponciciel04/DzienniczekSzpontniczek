package io.github.szpontium.api.prometheus

import com.fleeksoft.ksoup.Ksoup
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json as ktorJson
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.thauvin.erik.urlencoder.UrlEncoderUtil
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
fun decodeJWT(jwt: String): JwtPayload {
    val chunks = jwt.split(".")
    val b64 = Base64.UrlSafe.withPadding(Base64.PaddingOption.PRESENT_OPTIONAL)
    val decoded = b64.decode(chunks[1]).decodeToString()
    return Json.decodeFromString<JwtPayload>(decoded)
}

data class PrometheusLoginResult(
    /** Mapa tenant (symbol) → tenant JWT token z Tokens[] */
    val tenantTokens: Map<String, String>,
    /** Główny accessToken z pola AccessToken w /api/ap */
    val mainAccessToken: String,
)

class PrometheusLoginHelper {

    private val cookieStorage = AcceptAllCookiesStorage()
    private val json = Json { ignoreUnknownKeys = true }

    private fun createClient(followRedirects: Boolean): HttpClient = HttpClient {
        this.followRedirects = followRedirects

        install(HttpTimeout) {
            requestTimeoutMillis = 20000
            connectTimeoutMillis = 20000
            socketTimeoutMillis = 20000
        }
        install(HttpCookies) {
            storage = cookieStorage
        }
        install(ContentNegotiation) {
            ktorJson(json)
        }
        install(UserAgent) {
            agent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36"
        }
        defaultRequest {
            url {
                protocol = URLProtocol.HTTPS
            }
        }
    }

    private val httpClient = createClient(followRedirects = true)
    private val noRedirectClient = createClient(followRedirects = false)

    suspend fun login(
        login: String,
        password: String,
        @Suppress("UNUSED_PARAMETER") deviceModel: String,
    ): PrometheusLoginResult {
        require(login.isNotBlank()) { "Login must not be blank" }
        require(password.isNotBlank()) { "Password must not be blank" }

        // 1. Sprawdzenie czy captcha jest wymagana
        val showCaptcha = queryUserInfo(login)

        // 2. Pobranie strony logowania, tokenu CSRF i (jeśli trzeba) parametrów captchy
        val loginPageHtml = httpClient.get("https://eduvulcan.pl/logowanie").bodyAsText()
        val csrfToken = extractCsrfToken(loginPageHtml)

        val captchaResponse = if (showCaptcha) {
            val (challenge, difficulty, rounds) = extractCaptchaParams(loginPageHtml)
            POWCaptchaResolver.computeCaptchaResponse(challenge, difficulty, rounds)
        } else ""

        // 3. Właściwe logowanie — bez redirectów, żeby odczytać nagłówek Location
        val formBody = buildString {
            append("Alias=").append(UrlEncoderUtil.encode(login))
            append("&Password=").append(UrlEncoderUtil.encode(password))
            append("&captcha-response=").append(UrlEncoderUtil.encode(captchaResponse))
            append("&__RequestVerificationToken=").append(UrlEncoderUtil.encode(csrfToken))
        }

        val loginResponse = noRedirectClient.post("https://eduvulcan.pl/logowanie") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(formBody)
        }

        val loginBody = loginResponse.bodyAsText()
        if (loginBody.contains("robot", ignoreCase = true) || loginBody.contains("robak", ignoreCase = true)) {
            throw IllegalStateException("Captcha validation failed — ochrona przed robotami")
        }
        if (loginResponse.headers[HttpHeaders.Location] == null) {
            throw IllegalStateException("Invalid credentials")
        }

        // 4. Pobranie danych z /api/ap — cookies są wysyłane automatycznie przez HttpCookies
        val apiApHtml = httpClient.get("https://eduvulcan.pl/api/ap").bodyAsText()

        val apJson = extractApiApJson(apiApHtml)
        val apData = parseApiApJson(apJson)

        check(apData.success) { "API /api/ap zwróciło błąd: ${apData.errorMessage}" }
        check(apData.tokens.isNotEmpty()) { "Brak uczniów (pusta lista tokenów)" }

        val tenantTokens = apData.tokens.associateBy { jwt -> decodeJwtTenant(jwt) }

        return PrometheusLoginResult(
            tenantTokens = tenantTokens,
            mainAccessToken = apData.accessToken,
        )
    }

    // ── pomocnicze ──────────────────────────────────────────────────────────────

    private suspend fun queryUserInfo(username: String): Boolean {
        return try {
            val body = httpClient.post("https://eduvulcan.pl/Account/QueryUserInfo") {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody("alias=${UrlEncoderUtil.encode(username)}")
            }.bodyAsText()
            json.parseToJsonElement(body)
                .jsonObject["data"]?.jsonObject
                ?.get("ShowCaptcha")?.jsonPrimitive?.content == "true"
        } catch (_: Exception) {
            false
        }
    }

    private fun extractCsrfToken(html: String): String {
        val doc = Ksoup.parse(html)
        return doc.selectFirst("input[name=__RequestVerificationToken]")
            ?.attr("value")
            ?: throw IllegalStateException("CSRF token nie znaleziony na stronie logowania")
    }

    private fun extractCaptchaParams(html: String): Triple<String, Long, Int> {
        val doc = Ksoup.parse(html)
        val wrapper = doc.selectFirst("div.captcha-wrapper")
            ?: throw IllegalStateException("Brak elementu captcha-wrapper")
        val challenge = wrapper.attr("data-challenge")
        val difficulty = wrapper.attr("data-difficulty").toLong()
        val rounds = wrapper.attr("data-rounds").toInt()
        return Triple(challenge, difficulty, rounds)
    }

    private fun extractApiApJson(html: String): String {
        val doc = Ksoup.parse(html)
        return doc.selectFirst("#ap")
            ?.attr("value")
            ?: throw IllegalStateException("Element #ap nie znaleziony w /api/ap")
    }

    private data class ApiApData(
        val success: Boolean,
        val tokens: List<String>,
        val accessToken: String,
        val errorMessage: String?,
    )

    private fun parseApiApJson(jsonString: String): ApiApData {
        val obj = json.parseToJsonElement(jsonString).jsonObject
        return ApiApData(
            success = obj["Success"]?.jsonPrimitive?.content == "true",
            tokens = obj["Tokens"]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList(),
            accessToken = obj["AccessToken"]?.jsonPrimitive?.content ?: "",
            errorMessage = obj["ErrorMessage"]?.jsonPrimitive?.contentOrNull,
        )
    }

    private fun decodeJwtTenant(jwt: String): String = decodeJWT(jwt).tenant
}