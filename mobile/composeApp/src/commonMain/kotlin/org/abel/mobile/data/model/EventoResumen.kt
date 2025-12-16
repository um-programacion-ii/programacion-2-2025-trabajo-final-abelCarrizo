package org.abel.mobile.data.model

import kotlinx.serialization.Serializable

@Serializable
data class EventoResumen(
    val id: Long,
    val titulo: String,
    val resumen: String? = null,
    val fecha: String? = null,
    val precioEntrada: Double? = null,
    val tipoEvento: String? = null
)