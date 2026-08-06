package io.github.szpontium.cli

import io.github.szpontium.api.librus.LibrusMessageFolder
import io.github.szpontium.api.librus.SzpontLibrusApi
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.*

private val librusJson = Json { encodeDefaults = true; explicitNulls = false }
private inline fun <reified T> librusEncoded(value: T): JsonElement = librusJson.encodeToJsonElement(value)

suspend fun runLibrusCommand(command: String, subcommand: String?, args: CliArgs, api: SzpontLibrusApi): JsonElement {
    val week = args.value("week") ?: args.value("from") ?: java.time.LocalDate.now()
        .with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)).toString()
    return when (command) {
        "dashboard", "summary" -> kotlinx.serialization.json.buildJsonObject {
            put("me", librusEncoded(api.getMe()))
            put("luckyNumber", api.getLuckyNumber())
            put("grades", librusEncoded(api.getGrades()))
            put("homework", librusEncoded(api.getHomework()))
            put("events", librusEncoded(api.getEvents()))
        }
        "accounts" -> librusEncoded(api.getSynergiaAccounts())
        "me" -> librusEncoded(api.getMe())
        "lucky", "lucky-number" -> librusEncoded(api.getLuckyNumber())
        "grades" -> when (subcommand) {
            "categories" -> librusEncoded(api.getGradeCategories())
            null, "list" -> librusEncoded(api.getGrades())
            else -> librusUnknown("grades $subcommand")
        }
        "grade-categories" -> librusEncoded(api.getGradeCategories())
        "homework" -> librusEncoded(api.getHomework())
        "events" -> when (subcommand) {
            "categories" -> librusEncoded(api.getEventCategories())
            null, "list" -> librusEncoded(api.getEvents())
            else -> librusUnknown("events $subcommand")
        }
        "event-categories" -> librusEncoded(api.getEventCategories())
        "schedule", "timetable" -> librusEncoded(api.getTimetable(try { LocalDate.parse(week) } catch (_: Exception) { throw CliError("--week must be YYYY-MM-DD", Exit.USAGE) }))
        "messages" -> librusMessages(api, subcommand ?: "list")
        "message" -> {
            val id = args.required("id")
            if (args.flag("api")) librusEncoded(api.getMessageContent(id.toIntOrNull() ?: throw CliError("API message --id must be an integer", Exit.USAGE)))
            else librusEncoded(api.getSynergiaMessageContent(id))
        }
        "notices", "notes" -> when (subcommand) {
            "categories" -> librusEncoded(api.getNoticeCategories())
            null, "list" -> librusEncoded(api.getNotices())
            else -> librusUnknown("notices $subcommand")
        }
        "notice-categories" -> librusEncoded(api.getNoticeCategories())
        "subjects" -> librusEncoded(api.getSubjects())
        "users", "teachers" -> librusEncoded(api.getUsers())
        "classrooms" -> librusEncoded(api.getClassrooms())
        "auto-login-token" -> librusEncoded(api.getAutoLoginToken())
        else -> librusUnknown(command)
    }
}

private suspend fun librusMessages(api: SzpontLibrusApi, subcommand: String): JsonElement = when (subcommand) {
    "list", "api" -> librusEncoded(api.getMessages())
    "received", "inbox" -> librusEncoded(api.getSynergiaMessages(api.getAutoLoginToken(), LibrusMessageFolder.RECEIVED))
    "sent" -> librusEncoded(api.getSynergiaMessages(api.getAutoLoginToken(), LibrusMessageFolder.SENT))
    "deleted", "trash" -> librusEncoded(api.getSynergiaMessages(api.getAutoLoginToken(), LibrusMessageFolder.DELETED))
    else -> librusUnknown("messages $subcommand")
}

private fun librusUnknown(command: String): Nothing = throw CliError("Unknown Librus command '$command'", Exit.USAGE)
