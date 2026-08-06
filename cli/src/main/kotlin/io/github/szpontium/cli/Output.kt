package io.github.szpontium.cli

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*

private val prettyJson = Json { prettyPrint = true; explicitNulls = false }
private val compactJson = Json { prettyPrint = false; explicitNulls = false }

fun emit(element: JsonElement, args: CliArgs) {
    val format = args.value("format") ?: if (args.flag("json")) "json" else if (System.console() == null) "json" else "table"
    when (format) {
        "json" -> println((if (args.flag("compact")) compactJson else prettyJson).encodeToString(element))
        "table", "plain" -> printHuman(element)
        else -> throw CliError("Unknown format '$format' (use json, table, or plain)", Exit.USAGE)
    }
}

fun ok(message: String, args: CliArgs, extra: JsonObject = buildJsonObject { }) = emit(
    buildJsonObject {
        put("ok", true)
        put("message", message)
        extra.forEach { (key, value) -> put(key, value) }
    }, args
)

private fun printHuman(element: JsonElement) {
    when (element) {
        is JsonArray -> printRows(element)
        is JsonObject -> {
            if (element.size == 1 && element.values.firstOrNull() is JsonArray) printRows(element.values.first() as JsonArray)
            else element.forEach { (key, value) -> println("${key}: ${short(value)}") }
        }
        else -> println(short(element))
    }
}

private fun printRows(rows: JsonArray) {
    if (rows.isEmpty()) {
        println("No results.")
        return
    }
    if (rows.any { it !is JsonObject }) {
        rows.forEach { println(short(it)) }
        return
    }
    val objects = rows.map { it.jsonObject }
    val columns = objects.flatMap { it.keys }.distinct().take(10)
    val widths = columns.associateWith { column ->
        maxOf(column.length, objects.maxOf { short(it[column] ?: JsonNull).length }).coerceAtMost(42)
    }
    println(columns.joinToString("  ") { it.padEnd(widths.getValue(it)) })
    println(columns.joinToString("  ") { "-".repeat(widths.getValue(it)) })
    objects.forEach { row ->
        println(columns.joinToString("  ") { column -> truncate(short(row[column] ?: JsonNull), widths.getValue(column)).padEnd(widths.getValue(column)) })
    }
}

private fun short(value: JsonElement): String = when (value) {
    JsonNull -> ""
    is JsonPrimitive -> value.content
    is JsonArray -> value.joinToString(", ") { short(it) }
    is JsonObject -> value.entries.joinToString(", ") { "${it.key}=${short(it.value)}" }
}

private fun truncate(value: String, width: Int): String = if (value.length <= width) value else value.take((width - 1).coerceAtLeast(0)) + "…"
