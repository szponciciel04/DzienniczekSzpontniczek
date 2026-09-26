package io.github.szpontium.api.prometheus

import com.fleeksoft.ksoup.Ksoup
import io.github.szpontium.api.hebe.HebeHttpIdentity
import io.github.szpontium.api.hebe.appOS
import io.github.szpontium.api.hebe.appUserAgent
import io.github.szpontium.api.hebe.appVersionCode
import io.github.szpontium.api.hebe.hebeCeIdentity
import io.github.szpontium.api.prometheus.models.ApiApResponse
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.http.parameters
import io.ktor.serialization.kotlinx.json.json as ktorJson
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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.thauvin.erik.urlencoder.UrlEncoderUtil
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.time.Clock
import kotlin.time.Instant

private val RFC_1123_FORMAT: DateTimeFormat<DateTimeComponents> = Format {
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

@OptIn(ExperimentalEncodingApi::class)
fun decodeJWT(jwt: String): JwtPayload {
    val chunks = jwt.split(".")
    val b64 = Base64.UrlSafe.withPadding(Base64.PaddingOption.PRESENT_OPTIONAL)
    val decoded = b64.decode(chunks[1]).decodeToString()
    return Json { ignoreUnknownKeys = true }.decodeFromString<JwtPayload>(decoded)
}

data class PrometheusLoginResult(
    /** Wszystkie tokeny JWT */
    val tokens: List<String>,
    /** Mapa tenant (symbol) -> lista tokenów JWT */
    val tokensByTenant: Map<String, List<String>>,
    /** Mapa tenant (symbol) -> pierwszy tenant JWT token z Tokens[] (kompatybilność) */
    val tenantTokens: Map<String, String>,
    /** Główny accessToken z pola AccessToken w /api/ap */
    val mainAccessToken: String,
    /** Ciastka sesyjne */
    val cookies: List<io.ktor.http.Cookie>,
    /** Pełna odpowiedź z /api/ap */
    val apiApResponse: ApiApResponse
)

class PrometheusLoginHelper(
    var autoAcceptConsent: Boolean = true,
    var identity: HebeHttpIdentity = hebeCeIdentity,
    var prometheusBaseUrl: String = "https://eduvulcan.pl"
) {

    private val cookieStorage = AcceptAllCookiesStorage()
    private val json = Json { ignoreUnknownKeys = true }

    private fun createClient(followRedirects: Boolean): HttpClient = HttpClient {
        this.followRedirects = followRedirects

        install(HttpTimeout) {
            requestTimeoutMillis = 60000
            connectTimeoutMillis = 30000
            socketTimeoutMillis = 30000
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
        deviceModel: String = "Android"
    ): PrometheusLoginResult {
        require(login.isNotBlank()) { "Login must not be blank" }
        require(password.isNotBlank()) { "Password must not be blank" }

        // 1. Sprawdzenie czy captcha jest wymagana
        val showCaptcha = queryUserInfo(login)

        // 2. Pobranie strony logowania, tokenu CSRF i parametrów captchy
        val loginPageHtml = httpClient.get("$prometheusBaseUrl/logowanie").bodyAsText()
        if (loginPageHtml.contains("Twój adres e-mail nie został jeszcze potwierdzony.")) {
            val msg = Ksoup.parse(loginPageHtml).select("div.message-snackbar-content").text().ifEmpty {
                "Twój adres e-mail nie został jeszcze potwierdzony."
            }
            throw PrometheusEmailNotConfirmedException(msg)
        }

        val csrfToken = extractCsrfToken(loginPageHtml)

        val captchaResponse = if (showCaptcha) {
            val (challenge, difficulty, rounds) = extractCaptchaParams(loginPageHtml)
            POWCaptchaResolver.computeCaptchaResponse(challenge, difficulty, rounds)
        } else ""

        // 3. Właściwe logowanie — bez redirectów, aby zweryfikować poprawność po nagłówku Location
        val formBody = buildString {
            append("UserName=").append(UrlEncoderUtil.encode(login))
            append("&Password=").append(UrlEncoderUtil.encode(password))
            append("&captcha-response=").append(UrlEncoderUtil.encode(captchaResponse))
            append("&__RequestVerificationToken=").append(UrlEncoderUtil.encode(csrfToken))
        }

        val loginResponse = noRedirectClient.post("$prometheusBaseUrl/logowanie") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(formBody)
        }

        val loginBody = loginResponse.bodyAsText()
        if (loginBody.contains("Twój adres e-mail nie został jeszcze potwierdzony.")) {
            val msg = Ksoup.parse(loginBody).select("div.message-snackbar-content").text().ifEmpty {
                "Twój adres e-mail nie został jeszcze potwierdzony."
            }
            throw PrometheusEmailNotConfirmedException(msg)
        }
        if (loginBody.contains("robot", ignoreCase = true) || loginBody.contains("robak", ignoreCase = true)) {
            throw IllegalStateException("Captcha validation failed — ochrona przed robotami")
        }
        if (loginResponse.headers[HttpHeaders.Location] == null) {
            throw IllegalStateException("Invalid credentials")
        }

        // 4. Pobranie danych z /api/ap z obsługą zgód i nagłówkami mobilnymi
        val apiAp = processApiApWithConsent(deviceModel = deviceModel)

        if (!apiAp.success) {
            throw PrometheusJwtException("API /api/ap zwróciło błąd: ${apiAp.errorMessage}")
        }
        if (apiAp.tokens.isEmpty()) {
            throw PrometheusJwtException("Brak uczniów (pusta lista tokenów)")
        }

        val tokensByTenant = apiAp.tokens.groupBy { jwt -> decodeJWT(jwt).tenant }
        val tenantTokens = tokensByTenant.mapValues { (_, tokens) -> tokens.first() }

        return PrometheusLoginResult(
            tokens = apiAp.tokens,
            tokensByTenant = tokensByTenant,
            tenantTokens = tenantTokens,
            mainAccessToken = apiAp.accessToken,
            cookies = cookieStorage.get(io.ktor.http.Url(prometheusBaseUrl)),
            apiApResponse = apiAp
        )
    }

    suspend fun getApiAp(accessToken: String? = null, deviceModel: String = "Android"): String {
        return httpClient.get("$prometheusBaseUrl/api/ap") {
            contentType(ContentType.Application.Json)
            headers {
                append("user-agent", identity.appUserAgent(deviceModel))
                append("vapi", "1")
                append("vcanonicalurl", "api%2fap")
                append("vos", identity.appOS(deviceModel))
                append("vversioncode", identity.appVersionCode(deviceModel))
                append("vdate", Clock.System.now().format(RFC_1123_FORMAT))
                if (accessToken != null) {
                    bearerAuth(accessToken)
                }
            }
        }.bodyAsText()
    }

    suspend fun processApiApWithConsent(accessToken: String? = null, deviceModel: String = "Android"): ApiApResponse {
        val initialHtml = getApiAp(accessToken, deviceModel)
        val apiAp = decodeApiApHtml(initialHtml)

        if (apiAp.isConsentAccepted) {
            return apiAp
        }
        if (!autoAcceptConsent) {
            throw PrometheusConsentException(apiAp.errorMessage.orEmpty())
        }

        acceptConsent(accessToken = apiAp.accessToken.ifEmpty { accessToken })
        val refreshedHtml = getApiAp(apiAp.accessToken.ifEmpty { accessToken }, deviceModel)
        val refreshed = decodeApiApHtml(refreshedHtml)

        if (!refreshed.isConsentAccepted) {
            throw PrometheusConsentException(refreshed.errorMessage ?: "Nie udało się zaakceptować regulaminu")
        }
        return refreshed
    }

    suspend fun acceptConsent(accessToken: String? = null) {
        httpClient.submitForm(
            url = "$prometheusBaseUrl/konto/zgody",
            formParameters = parameters {
                append("HasUnacceptedOnlyOneRegulation", "true")
                append("Consent[0].Key", "6")
                append("Consent[0].Value", "true")
            }
        ) {
            if (accessToken != null) {
                bearerAuth(accessToken)
            }
        }
    }

    // ── pomocnicze ──────────────────────────────────────────────────────────────

    private suspend fun queryUserInfo(username: String): Boolean {
        return try {
            val body = httpClient.post("$prometheusBaseUrl/Account/QueryUserInfo") {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody("UserName=${UrlEncoderUtil.encode(username)}")
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

    private fun decodeApiApHtml(html: String): ApiApResponse {
        val doc = Ksoup.parse(html)
        val rawValue = doc.selectFirst("#ap")?.attr("value")
            ?: throw IllegalStateException("Element #ap nie znaleziony w /api/ap")
        return json.decodeFromString<ApiApResponse>(rawValue)
    }
}