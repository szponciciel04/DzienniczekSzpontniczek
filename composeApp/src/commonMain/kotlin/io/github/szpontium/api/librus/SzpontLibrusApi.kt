package io.github.szpontium.api.librus

import io.github.szpontium.api.librus.models.LibrusAccountsResponse
import io.github.szpontium.api.librus.models.LibrusMeResponse
import io.github.szpontium.api.librus.models.LibrusSynergiaAccount
import io.github.szpontium.api.librus.models.LibrusTokenResponse
import io.github.szpontium.api.librus.models.api.*
import com.fleeksoft.ksoup.Ksoup
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class SzpontLibrusApi(
    private val httpClient: HttpClient,
    var portalAccessToken: String? = null,
    var apiAccessToken: String? = null
) {
    private val json = Json { 
        ignoreUnknownKeys = true 
        coerceInputValues = true
    }

    suspend fun getMe(): LibrusMeResponse {
        val responseText = httpClient.get("${LibrusConstants.API_URL}/Me") {
            header("Authorization", "Bearer $apiAccessToken")
        }.bodyAsText()
        return json.decodeFromString(responseText)
    }

    /**
     * Gets accounts associated with the portal account.
     * Uses Portal API (portal.librus.pl/api)
     */
    suspend fun getSynergiaAccounts(): List<LibrusSynergiaAccount> {
        val responseText = httpClient.get("https://portal.librus.pl/api/v3/SynergiaAccounts") {
            header("Authorization", "Bearer $portalAccessToken")
            header("X-Requested-With", LibrusConstants.HEADER)
        }.bodyAsText()
        return json.decodeFromString<LibrusAccountsResponse>(responseText).accounts
    }

    /**
     * Exchanges portal token for API token for a specific synergia account.
     */
    suspend fun getFreshApiToken(accountLogin: String): String {
        val responseText = httpClient.get("https://portal.librus.pl/api/v3/SynergiaAccounts/fresh/$accountLogin") {
            header("Authorization", "Bearer $portalAccessToken")
            header("X-Requested-With", LibrusConstants.HEADER)
        }.bodyAsText()
        val obj = json.parseToJsonElement(responseText).jsonObject
        return obj["accessToken"]?.jsonPrimitive?.content ?: error("Failed to get fresh API token")
    }

    suspend fun getLuckyNumber(): Int {
        val responseText = httpClient.get("${LibrusConstants.API_URL}/LuckyNumbers") {
            header("Authorization", "Bearer $apiAccessToken")
        }.bodyAsText()
        val obj = json.parseToJsonElement(responseText).jsonObject
        return obj["LuckyNumber"]?.jsonObject?.get("LuckyNumber")?.jsonPrimitive?.int ?: 0
    }

    suspend fun getAutoLoginToken(): String {
        val responseText = httpClient.post("${LibrusConstants.API_URL}/AutoLoginToken") {
            header("Authorization", "Bearer $apiAccessToken")
        }.bodyAsText()
        val obj = json.parseToJsonElement(responseText).jsonObject
        return obj["Token"]?.jsonPrimitive?.content ?: error("Failed to get auto login token")
    }

    suspend fun getSynergiaMessages(token: String, tab: LibrusMessageFolder): List<LibrusWebMessage> {
        val folder = when (tab) {
            LibrusMessageFolder.RECEIVED -> "5"
            LibrusMessageFolder.SENT -> "6"
            LibrusMessageFolder.DELETED -> "7"
        }
        val loginUrl = "https://synergia.librus.pl/loguj/token/$token/przenies/uczen/widok/wiadomosci/$folder"
        
        // This request will set cookies and follow redirects
        val loginResponse = httpClient.get(loginUrl)
        val html = loginResponse.bodyAsText()
        
        // If the login redirect doesn't lead us directly to the list, try fetching it explicitly
        val finalHtml = if (!html.contains("decorated stretch")) {
             httpClient.get("https://synergia.librus.pl/wiadomosci/$folder").bodyAsText()
        } else html

        val doc = Ksoup.parse(finalHtml)
        val messages = mutableListOf<LibrusWebMessage>()
        
        doc.select(".decorated.stretch tbody > tr").forEach { tr ->
            val cells = tr.select("td")
            if (cells.size < 5) return@forEach
            
            val link = cells[3].select("a").first() ?: return@forEach
            val url = link.attr("href")
            // URL might be /wiadomosci/1/5/12345/f0 or similar
            val id = "/([0-9]+)/".toRegex().find(url)?.groupValues?.get(1) ?: url.substringAfterLast("/")
            val subject = link.text().trim()
            val sender = cells[2].text().substringBefore("(").trim()
            val dateStr = cells[4].text().trim()
            val isRead = !tr.hasClass("unread") && cells[2].attr("style").isBlank()
            val hasAttachment = cells[1].select("img").isNotEmpty()
            
            val date = try {
                val parts = dateStr.split(" ")
                val d = LocalDate.parse(parts[0])
                val t = LocalTime.parse(parts[1])
                LocalDateTime(d.year, d.month, d.day, t.hour, t.minute)
            } catch (e: Exception) {
                null
            }
            
            messages.add(
                LibrusWebMessage(
                    id = id,
                    title = subject,
                    senderOrRecipient = sender,
                    date = date,
                    isUnread = !isRead,
                    hasAttachments = hasAttachment
                )
            )
        }
        
        return messages
    }

    suspend fun getSynergiaMessageContent(id: String): String {
        // Try received messages first, then sent if it fails or returns empty
        val receivedUrl = "https://synergia.librus.pl/wiadomosci/1/5/$id/f0"
        val sentUrl = "https://synergia.librus.pl/wiadomosci/1/6/$id/f0"
        
        var response = httpClient.get(receivedUrl).bodyAsText()
        var doc = Ksoup.parse(response)
        var content = doc.select(".container-message-content").html().trim()
        
        if (content.isBlank()) {
            response = httpClient.get(sentUrl).bodyAsText()
            doc = Ksoup.parse(response)
            content = doc.select(".container-message-content").html().trim()
        }

        // Strip HTML tags for simple view, or keep if we want rich text
        return content.replace("<br>", "\n").replace("<[^>]*>".toRegex(), "").trim()
    }

    suspend fun getGrades(): List<LibrusGrade> {
        val responseText = httpClient.get("${LibrusConstants.API_URL}/Grades") {
            header("Authorization", "Bearer $apiAccessToken")
        }.bodyAsText()
        return json.decodeFromString<LibrusGradesResponse>(responseText).grades
    }

    suspend fun getGradeCategories(): List<LibrusGradeCategory> {
        val responseText = httpClient.get("${LibrusConstants.API_URL}/Grades/Categories") {
            header("Authorization", "Bearer $apiAccessToken")
        }.bodyAsText()
        return json.decodeFromString<LibrusGradeCategoriesResponse>(responseText).categories
    }

    suspend fun getHomework(): List<LibrusHomeWorkAssignment> {
        val responseText = httpClient.get("${LibrusConstants.API_URL}/HomeWorkAssignments") {
            header("Authorization", "Bearer $apiAccessToken")
        }.bodyAsText()
        return json.decodeFromString<LibrusHomeWorkAssignmentsResponse>(responseText).assignments ?: emptyList()
    }

    suspend fun getEvents(): List<LibrusEvent> {
        val responseText = httpClient.get("${LibrusConstants.API_URL}/HomeWorks") {
            header("Authorization", "Bearer $apiAccessToken")
        }.bodyAsText()
        return json.decodeFromString<LibrusHomeWorksResponse>(responseText).homeWorks ?: emptyList()
    }

    suspend fun getEventCategories(): List<LibrusIdNameReference> {
        val responseText = httpClient.get("${LibrusConstants.API_URL}/HomeWorks/Categories") {
            header("Authorization", "Bearer $apiAccessToken")
        }.bodyAsText()
        return json.decodeFromString<LibrusHomeWorksCategoriesResponse>(responseText).categories
    }

    suspend fun getTimetable(weekStart: LocalDate): Map<String, List<List<LibrusLesson>>> {
        val responseText = httpClient.get("${LibrusConstants.API_URL}/Timetables?weekStart=$weekStart") {
            header("Authorization", "Bearer $apiAccessToken")
        }.bodyAsText()
        return json.decodeFromString<LibrusTimetableResponse>(responseText).timetable
    }

    suspend fun getMessages(): List<LibrusMessage> {
        val responseText = httpClient.get("${LibrusConstants.API_URL}/Messages") {
            header("Authorization", "Bearer $apiAccessToken")
        }.bodyAsText()
        return json.decodeFromString<LibrusMessagesResponse>(responseText).messages ?: emptyList()
    }

    suspend fun getMessageContent(id: Int): String {
        val responseText = httpClient.get("${LibrusConstants.API_URL}/Messages/$id") {
            header("Authorization", "Bearer $apiAccessToken")
        }.bodyAsText()
        val obj = json.parseToJsonElement(responseText).jsonObject
        return obj["Message"]?.jsonObject?.get("Content")?.jsonPrimitive?.content ?: ""
    }

    suspend fun getNotices(): List<LibrusNotice> {
        val responseText = httpClient.get("${LibrusConstants.API_URL}/Notes") {
            header("Authorization", "Bearer $apiAccessToken")
        }.bodyAsText()
        return json.decodeFromString<LibrusNoticesResponse>(responseText).notices
    }

    suspend fun getNoticeCategories(): List<LibrusNoticeCategory> {
        val responseText = httpClient.get("${LibrusConstants.API_URL}/Notes/Categories") {
            header("Authorization", "Bearer $apiAccessToken")
        }.bodyAsText()
        return json.decodeFromString<LibrusNoticeCategoriesResponse>(responseText).categories
    }

    suspend fun getSubjects(): List<LibrusSubject> {
        val responseText = httpClient.get("${LibrusConstants.API_URL}/Subjects") {
            header("Authorization", "Bearer $apiAccessToken")
        }.bodyAsText()
        return json.decodeFromString<LibrusSubjectsResponse>(responseText).subjects
    }

    suspend fun getUsers(): List<LibrusUser> {
        val responseText = httpClient.get("${LibrusConstants.API_URL}/Users") {
            header("Authorization", "Bearer $apiAccessToken")
        }.bodyAsText()
        return json.decodeFromString<LibrusUsersResponse>(responseText).users
    }

    suspend fun getClassrooms(): List<LibrusClassroom> {
        val responseText = httpClient.get("${LibrusConstants.API_URL}/Classrooms") {
            header("Authorization", "Bearer $apiAccessToken")
        }.bodyAsText()
        return json.decodeFromString<LibrusClassroomsResponse>(responseText).classrooms
    }
}

@Serializable
enum class LibrusMessageFolder { RECEIVED, SENT, DELETED }

@Serializable
data class LibrusWebMessage(
    val id: String,
    val title: String,
    val senderOrRecipient: String,
    val date: LocalDateTime? = null,
    val isUnread: Boolean = false,
    val hasAttachments: Boolean = false
)
