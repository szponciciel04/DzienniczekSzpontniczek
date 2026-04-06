
@file:OptIn(ExperimentalTime::class)

package io.github.szpontium.api.hebe.signer

import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

@Suppress("unused")
class ASN1Structure {
    private val utc = TimeZone.UTC

    private val data = mutableListOf<Byte>()

    private fun appendTag(tag: Int, target: MutableList<Byte> = data) {
        target.add(tag.toByte())
    }

    private fun appendLength(length: Int, target: MutableList<Byte> = data) {
        if (length < 128) {
            target.add(length.toByte())
            return
        }
        val bytes = length.toULong().toVarByteArray()
        target.add((bytes.size or 0b10000000).toByte())
        target.addAll(bytes.toList())
    }

    private fun appendTLV(tag: Int, value: List<Byte>, target: MutableList<Byte> = data): ASN1Structure {
        appendTag(tag, target)
        appendLength(value.size, target)
        target.addAll(value)
        return this
    }

    private fun appendTLV(tag: Int, value: ByteArray, target: MutableList<Byte> = data): ASN1Structure {
        appendTag(tag, target)
        appendLength(value.size, target)
        target.addAll(value.toList())
        return this
    }

    fun appendInteger(number: Int): ASN1Structure {
        val bytes = number.toVarByteArray()
        return appendTLV(0x02, bytes)
    }

    fun appendLong(number: Long): ASN1Structure {
        val bytes = number.toVarByteArray()
        return appendTLV(0x02, bytes)
    }

    fun appendString(string: String, utf8: Boolean = false): ASN1Structure {
        val bytes = string.encodeToByteArray()
        return appendTLV(if (utf8) 0x0c else 0x13, bytes)
    }

    fun appendSequence(structure: ASN1Structure): ASN1Structure {
        return appendTLV(0x30, structure.data)
    }

    fun appendSet(structure: ASN1Structure): ASN1Structure {
        return appendTLV(0x31, structure.data)
    }

    fun appendBitString(bytes: ByteArray): ASN1Structure {
        appendTag(0x03)
        appendLength(bytes.size + 1)
        data.add(0x00)
        data.addAll(bytes.toList())
        return this
    }

    fun appendOctetString(bytes: ByteArray): ASN1Structure {
        return appendTLV(0x04, bytes)
    }

    fun appendBoolean(boolean: Boolean): ASN1Structure {
        return appendTLV(0x01, listOf(if (boolean) 0xff.toByte() else 0x00))
    }

    fun appendNull(): ASN1Structure {
        return appendTLV(0x05, emptyList())
    }

    fun appendUTCTime(time: Instant): ASN1Structure {
        val utcTime = time.toLocalDateTime(utc)
        val year = if (utcTime.year >= 2000) utcTime.year - 2000 else utcTime.year - 1900
        val list = listOf(
            year,
            utcTime.month,
            utcTime.day,
            utcTime.hour,
            utcTime.minute,
            utcTime.second
        )
        val bytes = list
            .joinToString("") { it.toString().padStart(2, '0') }
            .plus("Z")
            .encodeToByteArray()
        return appendTLV(0x17, bytes)
    }

    fun appendObjectId(oid: String): ASN1Structure {
        val idList = oid.split(".").map { it.toInt() }
        val bytes = mutableListOf<Byte>()
        idList.subList(2, idList.size).asReversed().forEach {
            var first = true
            var number = it
            while (number > 0) {
                var byte = number and 0b01111111
                number = number shr 7
                if (!first && it > 128)
                    byte = byte or 0b10000000
                first = false
                bytes.add(byte.toByte())
            }
        }
        bytes.add((idList[0] * 40 + idList[1]).toByte())
        return appendTLV(0x06, bytes.asReversed())
    }

    fun appendExplicit(position: Int, structure: ASN1Structure): ASN1Structure {
        return appendTLV(0xa0 or position, structure.data)
    }

    fun appendRaw(bytes: ByteArray): ASN1Structure {
        data.addAll(bytes.toList())
        return this
    }

    fun getBytes(): List<Byte> {
        val result = mutableListOf<Byte>()
        appendTLV(0x30, data, result)
        return result
    }

    private fun Int.toVarByteArray(): ByteArray {
        return this.toULong().toVarByteArray()
    }

    private fun Long.toVarByteArray(): ByteArray {
        val bytes = mutableListOf<Byte>()
        var value = this
        while (value != 0L) {
            bytes.add((value and 0xFF).toByte())
            value = value shr 8
        }
        return bytes.reversed().toByteArray()
    }

    private fun ULong.toVarByteArray(): ByteArray {
        val bytes = mutableListOf<Byte>()
        var value = this
        while (value != 0UL) {
            bytes.add((value and 0xFFU).toByte())
            value = value shr 8
        }
        return bytes.reversed().toByteArray()
    }
}
