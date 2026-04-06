package io.github.szpontium.api.hebe

import io.github.szpontium.api.hebe.credentials.ICredential
import io.ktor.client.HttpClient
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private const val HEBE_APP_NAME = "DzienniczekPlus 2.0"
private const val HEBE_APP_VERSION = "26.01.01 (G)"
private const val HEBE_APP_VERSION_CODE = "890"

private val TOKEN_PREFIXES = mapOf(
    "3S1" to "https://lekcjaplus.vulcan.net.pl",
    "TA1" to "https://uonetplus-komunikacja.umt.tarnow.pl",
    "OP1" to "https://uonetplus-komunikacja.eszkola.opolskie.pl",
    "RZ1" to "https://uonetplus-komunikacja.resman.pl",
    "GD1" to "https://uonetplus-komunikacja.edu.gdansk.pl",
    "KA1" to "https://uonetplus-komunikacja.mcuw.katowice.eu",
    "KA2" to "https://uonetplus-komunikacja-test.mcuw.katowice.eu",
    "LU1" to "https://uonetplus-komunikacja.edu.lublin.eu",
    "LU2" to "https://test-uonetplus-komunikacja.edu.lublin.eu",
    "P03" to "https://efeb-komunikacja-pro-efebmobile.pro.vulcan.pl",
    "P01" to "http://efeb-komunikacja.pro-hudson.win.vulcan.pl",
    "P02" to "http://efeb-komunikacja.pro-hudsonrc.win.vulcan.pl",
    "P90" to "http://efeb-komunikacja-pro-mwujakowska.neo.win.vulcan.pl",
    "KO1" to "https://uonetplus-komunikacja.eduportal.koszalin.pl"
)

/**
 * VULCAN Hebe API client. Registers via a security token + PIN pair.
 *
 * Usage:
 * 1. Create a credential: `RsaCredential.createNew("Android", "My Device")`
 * 2. Create the API: `SzpontHebeApi(credential, httpClient)`
 * 3. Register: `api.registerByToken(token, pin, tenant)`
 * 4. Call any endpoint from [SzpontApi]
 */
class SzpontHebeApi(
    credential: ICredential,
    httpClient: HttpClient
) : SzpontApi(
    credential,
    SzpontHttpClient(credential, HEBE_APP_NAME, HEBE_APP_VERSION, HEBE_APP_VERSION_CODE, httpClient)
) {

    /**
     * Registers the device using a security token and PIN.
     *
     * @param securityToken The 3-character token prefix + remaining characters (e.g. "3S1ABCDE...")
     * @param pin The registration PIN shown in the VULCAN web portal
     * @param tenant The school tenant symbol
     * @return The REST URL assigned after successful registration
     */
    suspend fun registerByToken(securityToken: String, pin: String, tenant: String): String {
        val token = securityToken.uppercase()
        val baseUrl = TOKEN_PREFIXES[token.take(3)]
            ?: throw WrongTokenException("Unknown token prefix: ${token.take(3)}")
        val restUrl = "$baseUrl/$tenant/api"

        szpontHttpClient.request(
            method = "POST",
            endpoint = "mobile/register/token",
            restUrl = restUrl,
            payload = buildJsonObject {
                put("OS", credential.deviceOs)
                put("Certificate", credential.certificate)
                put("CertificateType", credential.type)
                put("DeviceModel", credential.deviceModel)
                put("SelfIdentifier", credential.deviceId)
                put("CertificateThumbprint", credential.fingerprint)
                put("SecurityToken", token)
                put("PIN", pin)
            }
        )

        credential.restUrl = restUrl
        return restUrl
    }
}
