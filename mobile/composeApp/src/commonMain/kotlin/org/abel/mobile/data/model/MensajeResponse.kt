package org.abel.mobile.data.model

import kotlinx.serialization.Serializable

@Serializable
data class MensajeResponse(
    val exito: Boolean,
    val mensaje: String
)