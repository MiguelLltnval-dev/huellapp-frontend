package com.example.huellaapp.model

import com.google.gson.annotations.SerializedName

data class RegistroRequest(
    val nombre: String,
    val email: String,
    val password: String,
    @SerializedName("telefono")
    val celular: String,
    val departamento: String,
    val ciudad: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val mensaje: String?,
    val token: String?,
    val usuario: UsuarioData?
)

data class UsuarioData(
    val _id: String?,
    val nombre: String?,
    val email: String?,
    val celular: String?,
    val departamento: String?,
    val ciudad: String?
)

data class EstadoRequest(
    val estado: String
)
