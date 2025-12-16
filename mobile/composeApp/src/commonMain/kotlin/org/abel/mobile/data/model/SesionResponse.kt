package org.abel.mobile.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SesionResponse(
    val id: Long? = null,
    val eventoId: Long? = null,
    val estado: String? = null,
    val asientosSeleccionados: List<Asiento>? = null,
    val mensaje: String? = null
)