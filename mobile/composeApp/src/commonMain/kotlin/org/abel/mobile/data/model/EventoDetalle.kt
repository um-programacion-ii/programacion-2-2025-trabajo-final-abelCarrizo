package org.abel.mobile.data.model

import kotlinx.serialization.Serializable

@Serializable
data class EventoDetalle(
    val id: Long,
    val titulo: String,
    val resumen: String? = null,
    val descripcion: String? = null,
    val fecha: String? = null,
    val direccion: String? = null,
    val imagen: String? = null,
    val filaAsientos: Int? = null,
    val columnaAsientos: Int? = null,
    val precioEntrada: Double? = null,
    val tipoEvento: String? = null,
    val integrantes: List<Integrante>? = null
)