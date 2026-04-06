package io.github.szpontium.api.hebe

import io.github.szpontium.api.hebe.credentials.ICredential
import io.github.szpontium.api.hebe.models.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

private val EPOCH_START_DATETIME = LocalDateTime(1970, 1, 1, 1, 0, 0)
private const val INT_MIN = Int.MIN_VALUE
private const val DEFAULT_PAGE_SIZE = 500

open class SzpontApi(
    protected val credential: ICredential,
    protected val szpontHttpClient: SzpontHttpClient
) {

    suspend fun getAccounts(pupilId: Int? = null): List<Account> {
        val envelope = szpontHttpClient.request(
            method = "GET",
            restUrl = credential.restUrl ?: error("restUrl not set – register first"),
            endpoint = "mobile/register/hebe",
            query = mapOf("mode" to 2),
            pupilId = pupilId
        )
        return szpontJson.decodeFromJsonElement(envelope!!)
    }

    suspend fun makeHeartbeat(restUrl: String, pupilId: Int? = null) {
        szpontHttpClient.request(
            method = "GET",
            restUrl = restUrl,
            endpoint = "mobile/heartbeat",
            pupilId = pupilId
        )
    }

    suspend fun getAddressbook(restUrl: String, box: String, pupilId: Int? = null): List<Address> {
        val envelope = szpontHttpClient.request(
            method = "GET",
            restUrl = restUrl,
            endpoint = "mobile/addressbook",
            query = mapOf("box" to box),
            pupilId = pupilId
        )
        return szpontJson.decodeFromJsonElement(envelope!!)
    }

    suspend fun getAnnouncements(
        restUrl: String,
        unitId: Int,
        pupilId: Int,
        view: Int = 6,
        lastId: Int = INT_MIN,
        pageSize: Int = DEFAULT_PAGE_SIZE
    ): List<Announcement> {
        val envelope = szpontHttpClient.request(
            method = "GET",
            restUrl = restUrl,
            endpoint = "mobile/announcement/byPupil",
            query = mapOf(
                "unitId" to unitId,
                "pupilId" to pupilId,
                "view" to view,
                "lastId" to lastId,
                "pageSize" to pageSize
            ),
            pupilId = pupilId
        )
        return szpontJson.decodeFromJsonElement(envelope!!)
    }

    suspend fun getCompletedLessons(
        restUrl: String,
        pupilId: Int,
        dateFrom: LocalDate,
        dateTo: LocalDate,
        lastSyncDate: LocalDateTime = EPOCH_START_DATETIME,
        lastId: Int = INT_MIN,
        pageSize: Int = DEFAULT_PAGE_SIZE
    ): List<Lesson> {
        val envelope = szpontHttpClient.request(
            method = "GET",
            restUrl = restUrl,
            endpoint = "mobile/lesson/byPupil",
            query = mapOf(
                "pupilId" to pupilId,
                "dateFrom" to dateFrom,
                "dateTo" to dateTo,
                "lastSyncDate" to lastSyncDate,
                "lastId" to lastId,
                "pageSize" to pageSize
            ),
            pupilId = pupilId
        )
        return szpontJson.decodeFromJsonElement(envelope!!)
    }

    suspend fun getDuty(
        restUrl: String,
        pupilId: Int,
        lastSyncDate: LocalDateTime = EPOCH_START_DATETIME,
        lastId: Int = INT_MIN,
        pageSize: Int = DEFAULT_PAGE_SIZE
    ): List<Duty> {
        val envelope = szpontHttpClient.request(
            method = "GET",
            restUrl = restUrl,
            endpoint = "mobile/school/duty/byPupil",
            query = mapOf(
                "pupilId" to pupilId,
                "lastSyncDate" to lastSyncDate,
                "lastId" to lastId,
                "pageSize" to pageSize
            ),
            pupilId = pupilId
        )
        return szpontJson.decodeFromJsonElement(envelope!!)
    }

    suspend fun getExams(
        restUrl: String,
        pupilId: Int,
        dateFrom: LocalDate,
        dateTo: LocalDate,
        lastSyncDate: LocalDateTime = EPOCH_START_DATETIME,
        lastId: Int = INT_MIN,
        pageSize: Int = DEFAULT_PAGE_SIZE
    ): List<Exam> {
        val envelope = szpontHttpClient.request(
            method = "GET",
            restUrl = restUrl,
            endpoint = "mobile/exam/byPupil",
            query = mapOf(
                "pupilId" to pupilId,
                "dateFrom" to dateFrom,
                "dateTo" to dateTo,
                "lastSyncDate" to lastSyncDate,
                "lastId" to lastId,
                "pageSize" to pageSize
            ),
            pupilId = pupilId
        )
        return szpontJson.decodeFromJsonElement(envelope!!)
    }

    suspend fun getGrades(
        restUrl: String,
        unitId: Int,
        pupilId: Int,
        periodId: Int,
        lastSyncDate: LocalDateTime = EPOCH_START_DATETIME,
        lastId: Int = INT_MIN,
        pageSize: Int = DEFAULT_PAGE_SIZE
    ): List<Grade> {
        val envelope = szpontHttpClient.request(
            method = "GET",
            restUrl = restUrl,
            endpoint = "mobile/grade/byPupil",
            query = mapOf(
                "unitId" to unitId,
                "pupilId" to pupilId,
                "periodId" to periodId,
                "lastSyncDate" to lastSyncDate,
                "lastId" to lastId,
                "pageSize" to pageSize
            ),
            pupilId = pupilId
        )
        return szpontJson.decodeFromJsonElement(envelope!!)
    }

    suspend fun getGradesAverages(
        restUrl: String,
        unitId: Int,
        pupilId: Int,
        periodId: Int,
        lastId: Int = INT_MIN,
        pageSize: Int = DEFAULT_PAGE_SIZE
    ): List<GradeAverage> {
        val envelope = szpontHttpClient.request(
            method = "GET",
            restUrl = restUrl,
            endpoint = "mobile/grade/average/byPupil",
            query = mapOf(
                "unitId" to unitId,
                "pupilId" to pupilId,
                "periodId" to periodId,
                "lastId" to lastId,
                "pageSize" to pageSize,
                "scope" to "auto"
            ),
            pupilId = pupilId
        )
        return szpontJson.decodeFromJsonElement(envelope!!)
    }

    suspend fun getGradesSummary(
        restUrl: String,
        unitId: Int,
        pupilId: Int,
        periodId: Int,
        lastId: Int = INT_MIN,
        pageSize: Int = DEFAULT_PAGE_SIZE
    ): List<GradeSummary> {
        val envelope = szpontHttpClient.request(
            method = "GET",
            restUrl = restUrl,
            endpoint = "mobile/grade/summary/byPupil",
            query = mapOf(
                "unitId" to unitId,
                "pupilId" to pupilId,
                "periodId" to periodId,
                "lastId" to lastId,
                "pageSize" to pageSize
            ),
            pupilId = pupilId
        )
        return szpontJson.decodeFromJsonElement(envelope!!)
    }

    suspend fun getHomework(
        restUrl: String,
        pupilId: Int,
        dateFrom: LocalDate,
        dateTo: LocalDate,
        lastSyncDate: LocalDateTime = EPOCH_START_DATETIME,
        lastId: Int = INT_MIN,
        pageSize: Int = DEFAULT_PAGE_SIZE
    ): List<Homework> {
        val envelope = szpontHttpClient.request(
            method = "GET",
            restUrl = restUrl,
            endpoint = "mobile/homework/byPupil",
            query = mapOf(
                "pupilId" to pupilId,
                "dateFrom" to dateFrom,
                "dateTo" to dateTo,
                "lastSyncDate" to lastSyncDate,
                "lastId" to lastId,
                "pageSize" to pageSize
            ),
            pupilId = pupilId
        )
        return szpontJson.decodeFromJsonElement(envelope!!)
    }

    suspend fun getKindergartenHours(
        restUrl: String,
        pupilId: Int,
        constituentUnitId: Int
    ): KindergartenHours {
        val envelope = szpontHttpClient.request(
            method = "GET",
            restUrl = restUrl,
            endpoint = "mobile/school/hours",
            query = mapOf("pupilId" to pupilId, "constituentId" to constituentUnitId),
            pupilId = pupilId
        )
        return szpontJson.decodeFromJsonElement(envelope!!)
    }

    suspend fun getKindergartenTeachers(
        restUrl: String,
        pupilId: Int,
        lastSyncDate: LocalDateTime = EPOCH_START_DATETIME,
        lastId: Int = INT_MIN,
        pageSize: Int = DEFAULT_PAGE_SIZE
    ): List<Teacher> {
        val envelope = szpontHttpClient.request(
            method = "GET",
            restUrl = restUrl,
            endpoint = "mobile/teacher/kindergarten/byPupil",
            query = mapOf(
                "pupilId" to pupilId,
                "lastId" to lastId,
                "pageSize" to pageSize,
                "lastSyncDate" to lastSyncDate
            ),
            pupilId = pupilId
        )
        return szpontJson.decodeFromJsonElement(envelope!!)
    }

    suspend fun getLuckyNumber(
        restUrl: String,
        pupilId: Int,
        constituentUnitId: Int,
        day: LocalDate = LocalDate.fromEpochDays(0)
    ): LuckyNumber {
        val envelope = szpontHttpClient.request(
            method = "GET",
            restUrl = restUrl,
            endpoint = "mobile/school/lucky",
            query = mapOf(
                "pupilId" to pupilId,
                "constituentId" to constituentUnitId,
                "day" to day
            ),
            pupilId = pupilId
        )
        return szpontJson.decodeFromJsonElement(envelope!!)
    }

    suspend fun getMealMenu(
        restUrl: String,
        pupilId: Int,
        full: Boolean,
        dateFrom: LocalDate,
        dateTo: LocalDate,
        lastId: Int = INT_MIN,
        pageSize: Int = DEFAULT_PAGE_SIZE,
        lastSyncDate: LocalDateTime = EPOCH_START_DATETIME
    ): List<MealMenu> {
        val envelope = szpontHttpClient.request(
            method = "GET",
            restUrl = restUrl,
            endpoint = "mobile/eatery",
            query = mapOf(
                "pupilId" to pupilId,
                "full" to full,
                "dateFrom" to dateFrom,
                "dateTo" to dateTo,
                "lastId" to lastId,
                "pageSize" to pageSize,
                "lastSyncDate" to lastSyncDate
            ),
            pupilId = pupilId
        )
        return szpontJson.decodeFromJsonElement(envelope!!)
    }

    suspend fun getMeetings(
        restUrl: String,
        pupilId: Int,
        dateFrom: LocalDate,
        lastSyncDate: LocalDateTime = EPOCH_START_DATETIME,
        lastId: Int = INT_MIN,
        pageSize: Int = DEFAULT_PAGE_SIZE
    ): List<Meeting> {
        val envelope = szpontHttpClient.request(
            method = "GET",
            restUrl = restUrl,
            endpoint = "mobile/meetings/byPupil",
            query = mapOf(
                "pupilId" to pupilId,
                "from" to dateFrom,
                "lastId" to lastId,
                "pageSize" to pageSize,
                "lastSyncDate" to lastSyncDate
            ),
            pupilId = pupilId
        )
        return szpontJson.decodeFromJsonElement(envelope!!)
    }

    suspend fun getNotes(
        restUrl: String,
        pupilId: Int,
        lastId: Int = INT_MIN,
        pageSize: Int = DEFAULT_PAGE_SIZE,
        lastSyncDate: LocalDateTime = EPOCH_START_DATETIME
    ): List<Note> {
        val envelope = szpontHttpClient.request(
            method = "GET",
            restUrl = restUrl,
            endpoint = "mobile/note/byPupil",
            query = mapOf(
                "pupilId" to pupilId,
                "lastId" to lastId,
                "pageSize" to pageSize,
                "lastSyncDate" to lastSyncDate
            ),
            pupilId = pupilId
        )
        return szpontJson.decodeFromJsonElement(envelope!!)
    }

    suspend fun getPlannedLessons(
        restUrl: String,
        pupilId: Int,
        dateFrom: LocalDate,
        dateTo: LocalDate,
        lastSyncDate: LocalDateTime = EPOCH_START_DATETIME,
        lastId: Int = INT_MIN,
        pageSize: Int = DEFAULT_PAGE_SIZE
    ): List<Lesson> {
        val envelope = szpontHttpClient.request(
            method = "GET",
            restUrl = restUrl,
            endpoint = "mobile/lesson/planned/byPupil",
            query = mapOf(
                "pupilId" to pupilId,
                "dateFrom" to dateFrom,
                "dateTo" to dateTo,
                "lastSyncDate" to lastSyncDate,
                "lastId" to lastId,
                "pageSize" to pageSize
            ),
            pupilId = pupilId
        )
        return szpontJson.decodeFromJsonElement(envelope!!)
    }

    suspend fun getPresenceExtra(
        restUrl: String,
        pupilId: Int,
        dateFrom: LocalDate,
        dateTo: LocalDate,
        lastSyncDate: LocalDateTime = EPOCH_START_DATETIME,
        lastId: Int = INT_MIN,
        pageSize: Int = DEFAULT_PAGE_SIZE
    ): List<PresenceExtra> {
        val envelope = szpontHttpClient.request(
            method = "GET",
            restUrl = restUrl,
            endpoint = "mobile/presence/extra/byPupil",
            query = mapOf(
                "pupilId" to pupilId,
                "dateFrom" to dateFrom,
                "dateTo" to dateTo,
                "lastSyncDate" to lastSyncDate,
                "lastId" to lastId,
                "pageSize" to pageSize
            ),
            pupilId = pupilId
        )
        return szpontJson.decodeFromJsonElement(envelope!!)
    }

    suspend fun getPresenceExtraInfo(
        restUrl: String,
        pupilId: Int,
        weakRefId: Int,
        type: Int
    ): PresenceExtraInfo {
        val envelope = szpontHttpClient.request(
            method = "GET",
            restUrl = restUrl,
            endpoint = "mobile/presence/extra/info",
            query = mapOf(
                "pupilId" to pupilId,
                "weakRefId" to weakRefId,
                "type" to type
            ),
            pupilId = pupilId
        )
        return szpontJson.decodeFromJsonElement(envelope!!)
    }

    suspend fun getPresenceMonthStats(
        restUrl: String,
        pupilId: Int,
        periodId: Int
    ): List<PresenceMonthStats> {
        val envelope = szpontHttpClient.request(
            method = "GET",
            restUrl = restUrl,
            endpoint = "mobile/presence/stats/perMonth",
            query = mapOf("pupilId" to pupilId, "periodId" to periodId),
            pupilId = pupilId
        )
        return szpontJson.decodeFromJsonElement(envelope!!)
    }

    suspend fun getPresenceSubjectStats(
        restUrl: String,
        pupilId: Int,
        periodId: Int
    ): List<PresenceSubjectStats> {
        val envelope = szpontHttpClient.request(
            method = "GET",
            restUrl = restUrl,
            endpoint = "mobile/presence/stats/perSubject",
            query = mapOf("pupilId" to pupilId, "periodId" to periodId),
            pupilId = pupilId
        )
        return szpontJson.decodeFromJsonElement(envelope!!)
    }

    suspend fun getReceivedMessages(
        restUrl: String,
        box: String,
        pupilId: Int,
        lastId: Int = INT_MIN,
        pageSize: Int = DEFAULT_PAGE_SIZE,
        lastSyncDate: LocalDateTime = EPOCH_START_DATETIME
    ): List<Message> {
        val envelope = szpontHttpClient.request(
            method = "GET",
            restUrl = restUrl,
            endpoint = "mobile/messages/received/byBox",
            query = mapOf(
                "box" to box,
                "pupilId" to pupilId,
                "lastId" to lastId,
                "pageSize" to pageSize,
                "lastSyncDate" to lastSyncDate
            ),
            pupilId = pupilId
        )
        return szpontJson.decodeFromJsonElement(envelope!!)
    }

    suspend fun getSchedule(
        restUrl: String,
        pupilId: Int,
        dateFrom: LocalDate,
        dateTo: LocalDate,
        lastId: Int = INT_MIN,
        pageSize: Int = DEFAULT_PAGE_SIZE,
        lastSyncDate: LocalDateTime = EPOCH_START_DATETIME
    ): List<Schedule> {
        val envelope = szpontHttpClient.request(
            method = "GET",
            restUrl = restUrl,
            endpoint = "mobile/schedule/withchanges/byPupil",
            query = mapOf(
                "pupilId" to pupilId,
                "dateFrom" to dateFrom,
                "dateTo" to dateTo,
                "lastId" to lastId,
                "pageSize" to pageSize,
                "lastSyncDate" to lastSyncDate
            ),
            pupilId = pupilId
        )
        return szpontJson.decodeFromJsonElement(envelope!!)
    }

    suspend fun getScheduleExtra(
        restUrl: String,
        pupilId: Int,
        dateFrom: LocalDate,
        dateTo: LocalDate,
        lastSyncDate: LocalDateTime = EPOCH_START_DATETIME,
        lastId: Int = INT_MIN,
        pageSize: Int = DEFAULT_PAGE_SIZE
    ): List<ScheduleExtra> {
        val envelope = szpontHttpClient.request(
            method = "GET",
            restUrl = restUrl,
            endpoint = "mobile/schedule/extra/withchanges/byPupil",
            query = mapOf(
                "pupilId" to pupilId,
                "dateFrom" to dateFrom,
                "dateTo" to dateTo,
                "lastSyncDate" to lastSyncDate,
                "lastId" to lastId,
                "pageSize" to pageSize
            ),
            pupilId = pupilId
        )
        return szpontJson.decodeFromJsonElement(envelope!!)
    }

    suspend fun getSchoolInfo(
        restUrl: String,
        pupilId: Int,
        lastSyncDate: LocalDateTime = EPOCH_START_DATETIME
    ): List<SchoolInfo> {
        val envelope = szpontHttpClient.request(
            method = "GET",
            restUrl = restUrl,
            endpoint = "mobile/school/info",
            query = mapOf("pupilId" to pupilId, "lastSyncDate" to lastSyncDate),
            pupilId = pupilId
        )
        return szpontJson.decodeFromJsonElement(envelope!!)
    }

    suspend fun getTeachers(
        restUrl: String,
        periodId: Int,
        pupilId: Int,
        lastSyncDate: LocalDateTime = EPOCH_START_DATETIME,
        lastId: Int = INT_MIN,
        pageSize: Int = DEFAULT_PAGE_SIZE
    ): List<Teacher> {
        val envelope = szpontHttpClient.request(
            method = "GET",
            restUrl = restUrl,
            endpoint = "mobile/teacher/byPeriod",
            query = mapOf(
                "periodId" to periodId,
                "pupilId" to pupilId,
                "lastId" to lastId,
                "pageSize" to pageSize,
                "lastSyncDate" to lastSyncDate
            ),
            pupilId = pupilId
        )
        return szpontJson.decodeFromJsonElement(envelope!!)
    }

    suspend fun getTimeslots(pupilId: Int? = null): List<Timeslot> {
        val envelope = szpontHttpClient.request(
            method = "GET",
            restUrl = credential.restUrl ?: error("restUrl not set – register first"),
            endpoint = "mobile/dictionary/timeslot",
            query = mapOf("pupilId" to pupilId),
            pupilId = pupilId
        )
        return szpontJson.decodeFromJsonElement(envelope!!)
    }

    suspend fun getTrips(
        restUrl: String,
        pupilId: Int,
        dateFrom: LocalDate,
        dateTo: LocalDate
    ): List<Trip> {
        val envelope = szpontHttpClient.request(
            method = "GET",
            restUrl = restUrl,
            endpoint = "mobile/trips/byPupil",
            query = mapOf(
                "pupilId" to pupilId,
                "dateFrom" to dateFrom,
                "dateTo" to dateTo
            ),
            pupilId = pupilId
        )
        return szpontJson.decodeFromJsonElement(envelope!!)
    }

    suspend fun getUserEvents(restUrl: String, pupilId: Int): List<UserEvent> {
        val envelope = szpontHttpClient.request(
            method = "GET",
            restUrl = restUrl,
            endpoint = "mobile/userEvents/byPupil",
            query = mapOf("pupilId" to pupilId),
            pupilId = pupilId
        )
        return szpontJson.decodeFromJsonElement(envelope!!)
    }

    suspend fun getVacations(
        restUrl: String,
        pupilId: Int,
        dateFrom: LocalDate,
        dateTo: LocalDate,
        lastSyncDate: LocalDateTime = EPOCH_START_DATETIME,
        lastId: Int = INT_MIN,
        pageSize: Int = DEFAULT_PAGE_SIZE
    ): List<Vacation> {
        val envelope = szpontHttpClient.request(
            method = "GET",
            restUrl = restUrl,
            endpoint = "mobile/school/vacation",
            query = mapOf(
                "pupilId" to pupilId,
                "dateFrom" to dateFrom,
                "dateTo" to dateTo,
                "lastId" to lastId,
                "pageSize" to pageSize,
                "lastSyncDate" to lastSyncDate
            ),
            pupilId = pupilId
        )
        return szpontJson.decodeFromJsonElement(envelope!!)
    }

    suspend fun changeMessageImportance(
        restUrl: String,
        boxKey: String,
        messageKey: String,
        importance: Boolean,
        pupilId: Int? = null
    ) {
        szpontHttpClient.request(
            method = "POST",
            endpoint = "mobile/messages/importance",
            restUrl = restUrl,
            pupilId = pupilId,
            payload = buildJsonObject {
                put("BoxKey", boxKey)
                put("MessageKey", messageKey)
                put("Importance", importance)
            }
        )
    }

    suspend fun changeMessageStatus(
        restUrl: String,
        boxKey: String,
        messageKey: String,
        status: Int,
        pupilId: Int? = null
    ) {
        szpontHttpClient.request(
            method = "POST",
            endpoint = "mobile/messages/status",
            restUrl = restUrl,
            pupilId = pupilId,
            payload = buildJsonObject {
                put("BoxKey", boxKey)
                put("MessageKey", messageKey)
                put("Status", status)
            }
        )
    }

    suspend fun setPushLocale(locale: String, pupilId: Int? = null) {
        szpontHttpClient.request(
            method = "POST",
            endpoint = "mobile/push/locale",
            restUrl = credential.restUrl ?: error("restUrl not set – register first"),
            pupilId = pupilId,
            payload = JsonPrimitive(locale),
            verifyResponse = false
        )
    }

    suspend fun setAllPushSetting(turnOn: Boolean, pupilId: Int? = null) {
        szpontHttpClient.request(
            method = "POST",
            endpoint = "mobile/push/all",
            restUrl = credential.restUrl ?: error("restUrl not set – register first"),
            pupilId = pupilId,
            payload = JsonPrimitive(if (turnOn) "on" else "off")
        )
    }

    suspend fun setPushSetting(option: String, active: Boolean, pupilId: Int? = null): PushSetting {
        val envelope = szpontHttpClient.request(
            method = "POST",
            endpoint = "mobile/push",
            restUrl = credential.restUrl ?: error("restUrl not set – register first"),
            pupilId = pupilId,
            payload = buildJsonObject {
                put("Option", option)
                put("Active", active)
            }
        )
        return szpontJson.decodeFromJsonElement(envelope!!)
    }

    suspend fun configurePush(
        options: Map<String, Boolean>,
        locale: String,
        pupilId: Int? = null
    ): List<PushSetting> {
        val envelope = szpontHttpClient.request(
            method = "POST",
            endpoint = "mobile/push/configure",
            restUrl = credential.restUrl ?: error("restUrl not set – register first"),
            pupilId = pupilId,
            payload = buildJsonObject {
                putJsonArray("Options") {
                    options.forEach { (name, active) ->
                        add(buildJsonObject {
                            put("Option", name)
                            put("Active", active)
                        })
                    }
                }
                put("Locale", locale)
            }
        )
        return szpontJson.decodeFromJsonElement(envelope!!)
    }

    suspend fun deleteCredential(pupilId: Int? = null) {
        szpontHttpClient.request(
            method = "DELETE",
            endpoint = "mobile/register",
            restUrl = credential.restUrl ?: error("restUrl not set – register first"),
            pupilId = pupilId
        )
    }
}
