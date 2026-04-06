package io.github.szpontium.api.hebe.models

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class AccountLinks(
    @SerialName("Root") val root: String,
    @SerialName("Group") val group: String,
    @SerialName("Symbol") val symbol: String,
    @SerialName("Alias") val alias: String? = null,
    @SerialName("QuestionnaireRoot") val questionnaireRoot: String,
    @SerialName("ExResourcesUrl") val exResourcesUrl: String
)

@Serializable
data class SchoolUnit(
    @SerialName("Id") val id: Int,
    @SerialName("Symbol") val symbol: String,
    @SerialName("Short") val short: String,
    @SerialName("RestURL") val restUrl: String,
    @SerialName("Name") val name: String,
    @SerialName("Address") val address: String? = null,
    @SerialName("Patron") val patron: String? = null,
    @SerialName("DisplayName") val displayName: String,
    @SerialName("SchoolTopic") val schoolTopic: String
)

@Serializable
data class ConstituentUnit(
    @SerialName("Id") val id: Int,
    @SerialName("Short") val short: String,
    @SerialName("Name") val name: String,
    @SerialName("Address") val address: String? = null,
    @SerialName("Patron") val patron: String? = null,
    @SerialName("SchoolTopic") val schoolTopic: String
)

@Serializable
data class Pupil(
    @SerialName("Id") val id: Int,
    @SerialName("LoginId") val loginId: Int,
    @SerialName("FirstName") val firstName: String,
    @SerialName("SecondName") val secondName: String,
    @SerialName("Surname") val surname: String,
    @SerialName("Sex") val sex: Boolean
)

@Serializable
data class Period(
    @SerialName("Capabilities") val capabilities: List<String>,
    @SerialName("Id") val id: Int,
    @SerialName("Level") val level: Int,
    @SerialName("Number") val number: Int,
    @SerialName("StartAt") val start: LocalDate,
    @SerialName("EndAt") val end: LocalDate,
    @SerialName("Current") val current: Boolean,
    @SerialName("Last") val last: Boolean
)

@Serializable
data class Journal(
    @SerialName("Id") val id: Int,
    @SerialName("StartAt") val start: LocalDate,
    @SerialName("EndAt") val end: LocalDate,
    @SerialName("PupilNumber") val pupilNumber: Int
)

@Serializable
data class Constraints(
    @SerialName("AbsenceDaysBefore") val absenceDaysBefore: Int,
    @SerialName("AbsenceHoursBefore") val absenceHoursBefore: LocalTime,
    @SerialName("PresenceBlocade") val presenceBlocade: JsonElement? = null
)

@Serializable
data class AccountMessageBox(
    @SerialName("Id") val id: Int,
    @SerialName("GlobalKey") val globalKey: String,
    @SerialName("Name") val name: String
)

@Serializable
data class Account(
    @SerialName("TopLevelPartition") val topLevelPartition: String,
    @SerialName("Partition") val partition: String,
    @SerialName("Links") val links: AccountLinks,
    @SerialName("ClassDisplay") val classDisplay: String? = null,
    @SerialName("InfoDisplay") val infoDisplay: String? = null,
    @SerialName("Login") val login: JsonElement? = null,
    @SerialName("Unit") val unit: SchoolUnit,
    @SerialName("ConstituentUnit") val constituentUnit: ConstituentUnit,
    @SerialName("Capabilities") val capabilities: List<String>,
    @SerialName("EducatorsList") val educatorsList: List<Address>,
    @SerialName("Pupil") val pupil: Pupil,
    @SerialName("CaretakerId") val caretakerId: Int? = null,
    @SerialName("Periods") val periods: List<Period>,
    @SerialName("Journal") val journal: Journal? = null,
    @SerialName("Constraints") val constraints: Constraints,
    @SerialName("State") val state: Int,
    @SerialName("MessageBox") val messageBox: AccountMessageBox? = null,
    @SerialName("ProfileId") val profileId: String? = null
)
