@file:UseSerializers(VulcanDateTimeSerializer::class)

package io.github.szpontium.api.hebe.models

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class MessageAddressExtras(
    @SerialName("DisplayedClass") val displayedClass: String
)

@Serializable
data class MessageAddress(
    @SerialName("GlobalKey") val globalKey: String,
    @SerialName("Name") val name: String,
    @SerialName("HasRead") val hasRead: Boolean? = null,
    @SerialName("Extras") val extras: MessageAddressExtras? = null
)

@Serializable
data class Message(
    @SerialName("Id") val id: String,
    @SerialName("GlobalKey") val globalKey: String,
    @SerialName("ThreadKey") val threadKey: String,
    @SerialName("Subject") val subject: String,
    @SerialName("Content") val content: String,
    @SerialName("SentAt") val sentAt: LocalDateTime,
    @SerialName("ReadAt") val readAt: LocalDateTime? = null,
    @SerialName("Status") val status: Int,
    @SerialName("Sender") val sender: MessageAddress,
    @SerialName("Receiver") val receiver: List<MessageAddress>,
    @SerialName("Attachments") val attachments: List<Attachment>,
    @SerialName("Withdrawn") val withdrawn: Boolean
)
