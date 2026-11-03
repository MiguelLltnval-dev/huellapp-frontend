package com.example.huellaapp.model

import com.google.gson.annotations.SerializedName

data class Mascota(
    @SerializedName("_id") val id: String? = null,
    val nombre: String,
    val especie: String,
    val color: String,
    val ciudad: String,
    val estado: String = "PERDIDO", // Nuevo campo del backend
    val fotoUrl: String? = null,    // Nuevo campo del backend
    // Agregamos usuarioId quemado (mock) para cumplir con el backend
    val usuarioId: String = "64c9f1e1e4b0a2a1f0a1b2c3" 
)
