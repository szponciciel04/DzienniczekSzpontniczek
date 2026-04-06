package io.github.szpontium.api.hebe.models

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull

/**
 * Serializer for VULCAN API datetime strings.
 * Handles both "YYYY-MM-DD HH:MM:SS" (space separator) and ISO "YYYY-MM-DDTHH:MM:SS" (T separator).
 */
object VulcanDateTimeSerializer : KSerializer<LocalDateTime> {
    override val descriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): LocalDateTime {
        val raw = decoder.decodeString()
        // Normalize: replace space separator with T, strip timezone suffix
        val normalized = raw.replace(' ', 'T')
            .substringBefore('+')
            .let { if (it.endsWith('Z')) it.dropLast(1) else it }
        return LocalDateTime.parse(normalized)
    }
}

/**
 * Serializer for VULCAN API date-only strings in "YYYY-MM-DD" format.
 */
object VulcanDateSerializer : KSerializer<LocalDate> {
    override val descriptor = PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): LocalDate {
        return LocalDate.parse(decoder.decodeString().substringBefore('T').substringBefore(' '))
    }
}

/**
 * Serializer for numeric values that can come as either an integer (15) or decimal (15.0).
 */
object VulcanNullableIntSerializer : KSerializer<Int?> {
    override val descriptor = PrimitiveSerialDescriptor("NullableInt", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: Int?) {
        if (value == null) {
            encoder.encodeNull()
            return
        }
        encoder.encodeInt(value)
    }

    override fun deserialize(decoder: Decoder): Int? {
        val jsonDecoder = decoder as? JsonDecoder ?: return decoder.decodeInt()
        val element = jsonDecoder.decodeJsonElement()

        if (element is JsonNull) return null

        val primitive = element as? JsonPrimitive ?: return null
        primitive.intOrNull?.let { return it }

        val numeric = primitive.doubleOrNull
            ?: primitive.contentOrNull?.replace(',', '.')?.toDoubleOrNull()

        return numeric?.toInt()
    }
}
