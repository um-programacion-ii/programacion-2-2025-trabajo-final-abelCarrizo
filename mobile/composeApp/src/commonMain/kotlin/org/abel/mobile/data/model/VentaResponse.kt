package org.abel.mobile.data.model

import kotlinx.serialization.Serializable

@Serializable
data class VentaResponse(
    val id: Long? = null,
    val eventoId: Long? = null,
    val fechaVenta: String? = null,
    val precioVenta: Double? = null,
    val resultado: Boolean? = null,
    val descripcion: String? = null,
    val asientos: List<Asiento>? = null
)