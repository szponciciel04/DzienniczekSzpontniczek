package io.github.szpontium.cli

class CliArgs(tokens: List<String>) {
    val words = mutableListOf<String>()
    private val values = linkedMapOf<String, MutableList<String>>()
    private val switches = linkedSetOf<String>()

    init {
        val booleanOptions = setOf("json", "compact", "debug", "yes", "all", "brief", "hebe", "api", "no-store-password", "help")
        var index = 0
        var options = true
        while (index < tokens.size) {
            val token = tokens[index]
            if (token == "--") {
                options = false
                index++
                continue
            }
            if (options && token.startsWith("--")) {
                val body = token.removePrefix("--")
                if ('=' in body) {
                    val (key, value) = body.split('=', limit = 2)
                    values.getOrPut(key) { mutableListOf() }.add(value)
                } else if (body !in booleanOptions && index + 1 < tokens.size && !tokens[index + 1].startsWith("--")) {
                    values.getOrPut(body) { mutableListOf() }.add(tokens[++index])
                } else {
                    switches.add(body)
                }
            } else {
                words.add(token)
            }
            index++
        }
    }

    fun value(name: String): String? = values[name]?.lastOrNull()
    fun values(name: String): List<String> = values[name].orEmpty()
    fun flag(name: String): Boolean = name in switches || value(name)?.lowercase() in setOf("true", "1", "yes", "on")
    fun required(name: String, env: String? = null, secret: Boolean = false): String {
        value(name)?.takeIf { it.isNotBlank() }?.let { return it }
        env?.let { System.getenv(it)?.takeIf(String::isNotBlank)?.let { found -> return found } }
        val console = System.console()
        if (console != null) {
            val prompt = "${name.replace('-', ' ')}: "
            val entered = if (secret) console.readPassword(prompt)?.concatToString() else console.readLine(prompt)
            if (!entered.isNullOrBlank()) return entered
        }
        val hint = if (env == null) "--$name" else "--$name or $env"
        throw CliError("Missing $hint", Exit.USAGE)
    }

    fun int(name: String, default: Int): Int = value(name)?.toIntOrNull()
        ?: if (value(name) == null) default else throw CliError("--$name must be an integer", Exit.USAGE)
}

object Exit {
    const val OK = 0
    const val USAGE = 2
    const val AUTH = 3
    const val NETWORK = 4
    const val API = 5
    const val CONFIG = 6
    const val INTERNAL = 10
}

class CliError(message: String, val code: Int) : RuntimeException(message)
