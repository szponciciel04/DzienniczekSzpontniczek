package io.github.szpontium.cli

import io.github.szpontium.api.hebe.SzpontApi
import io.github.szpontium.api.hebe.models.Account
import io.github.szpontium.api.prometheus.PrometheusMessagesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.datetime.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import java.time.DayOfWeek
import java.time.temporal.TemporalAdjusters

private val wireJson = Json { encodeDefaults = true; explicitNulls = false }
private inline fun <reified T> encoded(value: T): JsonElement = wireJson.encodeToJsonElement(value)

fun resolveAccount(profile: Profile, selector: String?): Account {
    if (profile.accounts.isEmpty()) throw CliError("Profile has no student accounts", Exit.CONFIG)
    if (selector == null) return profile.accounts.getOrNull(profile.selectedAccount) ?: profile.accounts.first()
    selector.toIntOrNull()?.let { number ->
        profile.accounts.firstOrNull { it.pupil.id == number }?.let { return it }
        profile.accounts.getOrNull(number)?.let { return it }
    }
    val normalized = selector.lowercase()
    return profile.accounts.firstOrNull {
        "${it.pupil.firstName} ${it.pupil.surname}".lowercase().contains(normalized)
    } ?: throw CliError("Account '$selector' not found", Exit.USAGE)
}

private fun resolvePeriod(account: Account, selector: String?) = when {
    selector == null -> account.periods.firstOrNull { it.current } ?: account.periods.lastOrNull()
    else -> selector.toIntOrNull()?.let { number ->
        account.periods.firstOrNull { it.id == number }
            ?: account.periods.firstOrNull { it.number == number }
            ?: account.periods.getOrNull(number)
    }
} ?: throw CliError("Period '${selector ?: "current"}' not found", Exit.USAGE)

private fun dates(args: CliArgs): Pair<LocalDate, LocalDate> {
    val today = java.time.LocalDate.now()
    val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val from = args.value("from")?.let(::parseDate) ?: LocalDate.parse(monday.toString())
    val to = args.value("to")?.let(::parseDate) ?: LocalDate.parse(monday.plusDays(6).toString())
    if (to < from) throw CliError("--to cannot be before --from", Exit.USAGE)
    return from to to
}

private fun parseDate(value: String): LocalDate = try {
    LocalDate.parse(value)
} catch (_: Exception) {
    throw CliError("Invalid date '$value'; expected YYYY-MM-DD", Exit.USAGE)
}

private fun box(account: Account, args: CliArgs): String = args.value("box")
    ?: account.messageBox?.globalKey
    ?: throw CliError("This account has no message box; pass --box", Exit.CONFIG)

suspend fun runHebeCommand(command: String, subcommand: String?, args: CliArgs, ctx: HebeContext): JsonElement {
    val api = ctx.api
    val account = ctx.account
    val period = resolvePeriod(account, args.value("period"))
    val (from, to) = dates(args)
    val pageSize = args.int("page-size", 500)
    return when (command) {
        "dashboard", "summary" -> dashboard(api, account, period.id)
        "accounts" -> encoded(ctx.profile.accounts)
        "periods" -> encoded(account.periods)
        "heartbeat" -> { api.makeHeartbeat(account.unit.restUrl, account.pupil.id); success("Heartbeat sent") }
        "addressbook" -> encoded(api.getAddressbook(account.unit.restUrl, box(account, args), account.pupil.id))
        "announcements" -> encoded(api.getAnnouncements(account.unit.restUrl, account.unit.id, account.pupil.id, pageSize = pageSize))
        "completed-lessons" -> encoded(api.getCompletedLessons(account.unit.restUrl, account.pupil.id, from, to, pageSize = pageSize))
        "duties" -> encoded(api.getDuty(account.unit.restUrl, account.pupil.id, pageSize = pageSize))
        "exams" -> encoded(api.getExams(account.unit.restUrl, account.pupil.id, from, to, pageSize = pageSize))
        "grades" -> when (subcommand) {
            "averages" -> encoded(api.getGradesAverages(account.unit.restUrl, account.unit.id, account.pupil.id, period.id, pageSize = pageSize))
            "summary" -> encoded(api.getGradesSummary(account.unit.restUrl, account.unit.id, account.pupil.id, period.id, pageSize = pageSize))
            null, "list" -> encoded(api.getGrades(account.unit.restUrl, account.unit.id, account.pupil.id, period.id, pageSize = pageSize))
            else -> unknown("grades $subcommand")
        }
        "grade-averages" -> encoded(api.getGradesAverages(account.unit.restUrl, account.unit.id, account.pupil.id, period.id, pageSize = pageSize))
        "grade-summary" -> encoded(api.getGradesSummary(account.unit.restUrl, account.unit.id, account.pupil.id, period.id, pageSize = pageSize))
        "homework" -> encoded(api.getHomework(account.unit.restUrl, account.pupil.id, from, to, pageSize = pageSize))
        "kindergarten-hours" -> encoded(api.getKindergartenHours(account.unit.restUrl, account.pupil.id, account.constituentUnit.id))
        "kindergarten-teachers" -> encoded(api.getKindergartenTeachers(account.unit.restUrl, account.pupil.id, pageSize = pageSize))
        "lucky-number", "lucky" -> encoded(api.getLuckyNumber(account.unit.restUrl, account.pupil.id, account.constituentUnit.id, args.value("day")?.let(::parseDate) ?: LocalDate.parse(java.time.LocalDate.now().toString())))
        "meal-menu", "menu" -> encoded(api.getMealMenu(account.unit.restUrl, account.pupil.id, !args.flag("brief"), from, to, pageSize = pageSize))
        "meetings" -> encoded(api.getMeetings(account.unit.restUrl, account.pupil.id, from, pageSize = pageSize))
        "notes" -> encoded(api.getNotes(account.unit.restUrl, account.pupil.id, pageSize = pageSize))
        "planned-lessons" -> encoded(api.getPlannedLessons(account.unit.restUrl, account.pupil.id, from, to, pageSize = pageSize))
        "presence", "attendance" -> when (subcommand) {
            "months" -> encoded(api.getPresenceMonthStats(account.unit.restUrl, account.pupil.id, period.id))
            "subjects" -> encoded(api.getPresenceSubjectStats(account.unit.restUrl, account.pupil.id, period.id))
            "info" -> encoded(api.getPresenceExtraInfo(
                account.unit.restUrl, account.pupil.id,
                args.required("weak-ref-id").toIntOrNull() ?: throw CliError("--weak-ref-id must be an integer", Exit.USAGE),
                args.required("type").toIntOrNull() ?: throw CliError("--type must be an integer", Exit.USAGE)
            ))
            null, "list" -> encoded(api.getPresenceExtra(account.unit.restUrl, account.pupil.id, from, to, pageSize = pageSize))
            else -> unknown("presence $subcommand")
        }
        "presence-months" -> encoded(api.getPresenceMonthStats(account.unit.restUrl, account.pupil.id, period.id))
        "presence-subjects" -> encoded(api.getPresenceSubjectStats(account.unit.restUrl, account.pupil.id, period.id))
        "messages" -> messages(subcommand ?: "received", args, ctx)
        "message" -> messageDetails(args.required("id"), args, ctx)
        "schedule", "timetable" -> encoded(api.getSchedule(account.unit.restUrl, account.pupil.id, from, to, pageSize = pageSize))
        "schedule-extra" -> encoded(api.getScheduleExtra(account.unit.restUrl, account.pupil.id, from, to, pageSize = pageSize))
        "school-info" -> encoded(api.getSchoolInfo(account.unit.restUrl, account.pupil.id))
        "teachers" -> encoded(api.getTeachers(account.unit.restUrl, period.id, account.pupil.id, pageSize = pageSize))
        "timeslots" -> encoded(api.getTimeslots(account.pupil.id))
        "trips" -> encoded(api.getTrips(account.unit.restUrl, account.pupil.id, from, to))
        "events" -> encoded(api.getUserEvents(account.unit.restUrl, account.pupil.id))
        "vacations" -> encoded(api.getVacations(account.unit.restUrl, account.pupil.id, from, to, pageSize = pageSize))
        "push" -> push(subcommand, args, api, account)
        "credential" -> credential(subcommand, args, api, account)
        else -> unknown(command)
    }
}

private suspend fun dashboard(api: SzpontApi, account: Account, periodId: Int): JsonElement = coroutineScope {
    val today = java.time.LocalDate.now()
    val weekAgo = LocalDate.parse(today.minusDays(7).toString())
    val nextWeek = LocalDate.parse(today.plusDays(7).toString())
    val now = LocalDate.parse(today.toString())
    val grades = async { api.getGrades(account.unit.restUrl, account.unit.id, account.pupil.id, periodId) }
    val averages = async { api.getGradesAverages(account.unit.restUrl, account.unit.id, account.pupil.id, periodId) }
    val exams = async { api.getExams(account.unit.restUrl, account.pupil.id, now, nextWeek) }
    val homework = async { api.getHomework(account.unit.restUrl, account.pupil.id, now, nextWeek) }
    val lucky = async { api.getLuckyNumber(account.unit.restUrl, account.pupil.id, account.constituentUnit.id, now) }
    buildJsonObject {
        put("student", "${account.pupil.firstName} ${account.pupil.surname}")
        put("recentGrades", encoded(grades.await().filter { it.createdAt.date >= weekAgo }))
        put("averages", encoded(averages.await()))
        put("upcomingExams", encoded(exams.await()))
        put("upcomingHomework", encoded(homework.await()))
        put("luckyNumber", encoded(lucky.await()))
    }
}

private suspend fun messages(folder: String, args: CliArgs, ctx: HebeContext): JsonElement {
    val account = ctx.account
    if (ctx.profile.provider == "eduvulcan" && !args.flag("hebe")) {
        val login = args.value("username") ?: System.getenv("DZIENNICZEK_USERNAME") ?: ctx.profile.prometheusLogin
        val password = args.value("password") ?: System.getenv("DZIENNICZEK_PASSWORD") ?: ctx.profile.prometheusPassword
        val tenant = ctx.profile.prometheusTenant ?: account.unit.symbol
        if (login != null && password != null) {
            val api = PrometheusMessagesApi(tenant, login, password)
            api.initialize()
            return when (folder) {
                "received", "inbox" -> encoded(api.getReceivedMessages(box(account, args), args.int("page-size", 50), args.int("last-id", 0)))
                "sent" -> encoded(api.getSentMessages(box(account, args), args.int("page-size", 50), args.int("last-id", 0)))
                "deleted", "trash" -> encoded(api.getDeletedMessages(box(account, args), args.int("page-size", 50), args.int("last-id", 0)))
                else -> unknown("messages $folder")
            }
        }
    }
    return when (folder) {
        "received", "inbox" -> encoded(ctx.api.getReceivedMessages(account.unit.restUrl, box(account, args), account.pupil.id, pageSize = args.int("page-size", 500)))
        "sent" -> encoded(ctx.api.getSentMessages(account.unit.restUrl, box(account, args), account.pupil.id, pageSize = args.int("page-size", 500)))
        "deleted", "trash" -> encoded(ctx.api.getDeletedMessages(account.unit.restUrl, box(account, args), account.pupil.id, pageSize = args.int("page-size", 500)))
        "importance" -> {
            ctx.api.changeMessageImportance(account.unit.restUrl, box(account, args), args.required("id"), args.required("important").toBooleanStrictOrNull() ?: throw CliError("--important must be true or false", Exit.USAGE), account.pupil.id)
            success("Message importance changed")
        }
        "status" -> {
            ctx.api.changeMessageStatus(account.unit.restUrl, box(account, args), args.required("id"), args.required("status").toIntOrNull() ?: throw CliError("--status must be an integer", Exit.USAGE), account.pupil.id)
            success("Message status changed")
        }
        else -> unknown("messages $folder")
    }
}

private suspend fun messageDetails(id: String, args: CliArgs, ctx: HebeContext): JsonElement {
    if (ctx.profile.provider == "eduvulcan" && !args.flag("hebe")) {
        val login = args.value("username") ?: System.getenv("DZIENNICZEK_USERNAME") ?: ctx.profile.prometheusLogin
        val password = args.value("password") ?: System.getenv("DZIENNICZEK_PASSWORD") ?: ctx.profile.prometheusPassword
        if (login != null && password != null) {
            val api = PrometheusMessagesApi(ctx.profile.prometheusTenant ?: ctx.account.unit.symbol, login, password)
            api.initialize()
            return encoded(api.getMessageDetails(id))
        }
    }
    val account = ctx.account
    val all = ctx.api.getReceivedMessages(account.unit.restUrl, box(account, args), account.pupil.id) +
        ctx.api.getSentMessages(account.unit.restUrl, box(account, args), account.pupil.id) +
        ctx.api.getDeletedMessages(account.unit.restUrl, box(account, args), account.pupil.id)
    return encoded(all.firstOrNull { it.id == id } ?: throw CliError("Message '$id' not found", Exit.API))
}

private suspend fun push(subcommand: String?, args: CliArgs, api: SzpontApi, account: Account): JsonElement = when (subcommand) {
    "locale" -> { api.setPushLocale(args.required("locale"), account.pupil.id); success("Push locale changed") }
    "all" -> { api.setAllPushSetting(args.required("enabled").toBooleanStrictOrNull() ?: throw CliError("--enabled must be true or false", Exit.USAGE), account.pupil.id); success("Push settings changed") }
    "set" -> encoded(api.setPushSetting(args.required("option"), args.required("enabled").toBooleanStrictOrNull() ?: throw CliError("--enabled must be true or false", Exit.USAGE), account.pupil.id))
    "configure" -> {
        val options = args.values("option").associate { raw ->
            val parts = raw.split('=', limit = 2)
            if (parts.size != 2) throw CliError("--option must use NAME=true|false", Exit.USAGE)
            parts[0] to (parts[1].toBooleanStrictOrNull() ?: throw CliError("Invalid push option '$raw'", Exit.USAGE))
        }
        encoded(api.configurePush(options, args.value("locale") ?: "pl-PL", account.pupil.id))
    }
    else -> unknown("push ${subcommand ?: ""}".trim())
}

private suspend fun credential(subcommand: String?, args: CliArgs, api: SzpontApi, account: Account): JsonElement = when (subcommand) {
    "delete" -> {
        if (!args.flag("yes")) throw CliError("credential delete requires --yes", Exit.USAGE)
        api.deleteCredential(account.pupil.id)
        success("Remote credential deleted")
    }
    else -> unknown("credential ${subcommand ?: ""}".trim())
}

private fun success(message: String) = buildJsonObject { put("ok", true); put("message", message) }
private fun unknown(command: String): Nothing = throw CliError("Unknown command '$command'; run 'dzienniczek help'", Exit.USAGE)
