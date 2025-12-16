package org.abel.mobile.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Asiento(
    val fila: Int,
    val columna: Int,
    val estado: String? = null,
    val persona: String? = null
)