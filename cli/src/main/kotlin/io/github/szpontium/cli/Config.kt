package io.github.szpontium.cli

import io.github.szpontium.api.hebe.SzpontApi
import io.github.szpontium.api.hebe.SzpontHebeApi
import io.github.szpontium.api.hebe.SzpontHebeCeApi
import io.github.szpontium.api.hebe.credentials.RsaCredential
import io.github.szpontium.api.hebe.models.Account
import io.github.szpontium.api.librus.SzpontLibrusApi
import io.github.szpontium.api.librus.models.LibrusSynergiaAccount
import io.ktor.client.HttpClient
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.PosixFilePermission

@Serializable
data class CredentialData(
    val type: String,
    val restUrl: String? = null,
    val certificate: String,
    val privateKey: String,
    val fingerprint: String,
    val notificationToken: String? = null,
    val deviceId: String,
    val deviceOs: String,
    val deviceModel: String,
) {
    fun restore() = RsaCredential(
        type, restUrl, certificate, privateKey, fingerprint,
        notificationToken, deviceId, deviceOs, deviceModel
    )

    companion object {
        fun from(value: RsaCredential) = CredentialData(
            value.type, value.restUrl, value.certificate, value.privateKey,
            value.fingerprint, value.notificationToken, value.deviceId,
            value.deviceOs, value.deviceModel
        )
    }
}

@Serializable
data class Profile(
    val provider: String,
    val credential: CredentialData? = null,
    val accounts: List<Account> = emptyList(),
    val selectedAccount: Int = 0,
    val prometheusLogin: String? = null,
    val prometheusPassword: String? = null,
    val prometheusTenant: String? = null,
    val librusPortalToken: String? = null,
    val librusApiToken: String? = null,
    val librusAccountLogin: String? = null,
    val librusStudentName: String? = null,
    val librusAccounts: List<LibrusSynergiaAccount> = emptyList(),
)

@Serializable
data class ConfigData(
    val version: Int = 1,
    val currentProfile: String? = null,
    val profiles: Map<String, Profile> = emptyMap(),
)

class ConfigStore private constructor(val path: Path) {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    fun load(): ConfigData {
        if (!Files.exists(path)) return ConfigData()
        return try {
            json.decodeFromString(Files.readString(path))
        } catch (e: Exception) {
            throw CliError("Cannot read config ${path}: ${e.message}", Exit.CONFIG)
        }
    }

    fun save(data: ConfigData) {
        Files.createDirectories(path.parent)
        val temporary = path.resolveSibling("${path.fileName}.tmp")
        Files.writeString(temporary, json.encodeToString(data))
        restrict(temporary)
        try {
            Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        } catch (_: Exception) {
            Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING)
        }
        restrict(path)
    }

    private fun restrict(file: Path) {
        try {
            Files.setPosixFilePermissions(file, setOf(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE))
        } catch (_: UnsupportedOperationException) {
            // WSL, Linux and macOS use POSIX permissions; non-POSIX filesystems are best effort.
        }
    }

    companion object {
        fun create(override: String? = null): ConfigStore {
            val selected = override ?: System.getenv("DZIENNICZEK_CONFIG")
            if (!selected.isNullOrBlank()) return ConfigStore(Path.of(selected).toAbsolutePath())
            val base = System.getenv("XDG_CONFIG_HOME")?.takeIf { it.isNotBlank() }
                ?: Path.of(System.getProperty("user.home"), ".config").toString()
            return ConfigStore(Path.of(base, "dzienniczek", "config.json"))
        }
    }
}

data class HebeContext(
    val name: String,
    val profile: Profile,
    val api: SzpontApi,
    val account: Account,
)

fun Profile.hebeApi(client: HttpClient): SzpontApi {
    val restored = credential?.restore() ?: throw CliError("Profile has no VULCAN credential", Exit.CONFIG)
    return when (provider) {
        "eduvulcan" -> SzpontHebeCeApi(restored, client)
        "vulcan" -> SzpontHebeApi(restored, client)
        else -> throw CliError("Command requires a VULCAN or eduVULCAN profile", Exit.USAGE)
    }
}

fun Profile.librusApi(client: HttpClient): SzpontLibrusApi {
    if (provider != "librus") throw CliError("Command requires a Librus profile", Exit.USAGE)
    return SzpontLibrusApi(client, librusPortalToken, librusApiToken)
}
