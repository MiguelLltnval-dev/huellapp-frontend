package com.example.huellaapp.model

import com.google.gson.annotations.SerializedName

data class Mascota(
    @SerializedName("_id") val id: String? = null,
    val nombre: String,
    val especie: String,
    val color: String,
    val ciudad: String,
    val estado: String = "PERDIDO",
    val fotoUrl: String? = null,
    val usuarioId: String? = null,
    val celularDueno: String? = null // El nuevo campo que envía el backend
)
