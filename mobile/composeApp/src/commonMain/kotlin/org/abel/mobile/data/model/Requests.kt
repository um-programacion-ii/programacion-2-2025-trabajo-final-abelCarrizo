package org.abel.mobile.data.model

import kotlinx.serialization.Serializable

@Serializable
data class IniciarSesionRequest(
    val eventoId: Long
)

@Serializable
data class SeleccionarAsientosRequest(
    val eventoId: Long,
    val asientos: List<Asiento>
)

@Serializable
data class AsignarPersonasRequest(
    val asientos: List<Asiento>
)