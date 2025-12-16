package org.abel.mobile.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Integrante(
    val nombre: String? = null,
    val apellido: String? = null,
    val identificacion: String? = null
)