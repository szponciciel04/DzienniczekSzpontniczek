package io.github.szpontium.api.prometheus.models

import kotlinx.serialization.Serializable

@Serializable
data class PrometheusMessage(
    val apiGlobalKey: String,
    val data: String,
    val id: Int,
    val przeczytana: Boolean = false,
    val hasZalaczniki: Boolean = false,
    val temat: String = "",
    val korespondenci: String? = null,
)

@Serializable
data class PrometheusMessageDetails(
    val apiGlobalKey: String,
    val data: String = "",
    val id: Int = 0,
    val odczytana: Boolean = false,
    val temat: String = "",
    val odbiorcy: List<String> = emptyList(),
    val nadawca: String = "",
    val tresc: String = "",
    val zalaczniki: List<PrometheusAttachment> = emptyList()
)

@Serializable
data class PrometheusAttachment(
    val url: String? = null,
    val idZalacznik: Int? = null,
    val nazwaPliku: String? = null,
    val idOneDrive: String? = null
)

@Serializable
data class PrometheusSendMessage(
    val globalKey: String,
    val watekGlobalKey: String,
    val nadawcaSkrzynkaGlobalKey: String,
    val adresaciSkrzynkiGlobalKeys: List<String>,
    val tytul: String,
    val tresc: String,
    val zalaczniki: List<String> = emptyList(),
    val powitalna: Boolean = false,
    val odpowiedziana: String? = null,
    val przekazana: String? = null
)
