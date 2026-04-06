package io.github.szpontium.api.hebe

import io.github.szpontium.api.hebe.credentials.ICredential
import io.ktor.client.HttpClient
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

private const val HEBECE_APP_NAME = "DzienniczekPlus 3.0"
private const val HEBECE_APP_VERSION = "26.03.02 (G)"
private const val HEBECE_APP_VERSION_CODE = "936"
private const val HEBECE_API_BASE_URL = "https://lekcjaplus.vulcan.net.pl"

/**
 * VULCAN Hebe CE API client. Registers via a list of JWT tokens.
 *
 * Usage:
 * 1. Create a credential: `RsaCredential.createNew("Android", "My Device")`
 * 2. Create the API: `SzpontHebeCeApi(credential, httpClient)`
 * 3. Register: `api.registerByJwt(tokens, tenant)`
 * 4. Call any endpoint from [SzpontApi]
 */
class SzpontHebeCeApi(
    credential: ICredential,
    httpClient: HttpClient
) : SzpontApi(
    credential,
    SzpontHttpClient(credential, HEBECE_APP_NAME, HEBECE_APP_VERSION, HEBECE_APP_VERSION_CODE, httpClient)
) {

    /**
     * Registers the device using a list of JWT tokens obtained from the VULCAN web portal.
     *
     * @param tokens List of JWT tokens
     * @param tenant The school tenant symbol
     * @return The REST URL assigned after successful registration
     */
    suspend fun registerByJwt(tokens: List<String>, tenant: String): String {
        val restUrl = "$HEBECE_API_BASE_URL/$tenant/api"

        szpontHttpClient.request(
            method = "POST",
            endpoint = "mobile/register/jwt",
            restUrl = restUrl,
            payload = buildJsonObject {
                put("OS", credential.deviceOs)
                put("Certificate", credential.certificate)
                put("CertificateType", credential.type)
                put("DeviceModel", credential.deviceModel)
                put("SelfIdentifier", credential.deviceId)
                put("CertificateThumbprint", credential.fingerprint)
                putJsonArray("Tokens") { tokens.forEach { add(it) } }
            }
        )

        credential.restUrl = restUrl
        return restUrl
    }
}
