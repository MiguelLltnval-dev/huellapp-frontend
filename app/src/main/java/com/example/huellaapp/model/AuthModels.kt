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
    val ciudad: String?,
    val fotoPerfil: String? // Campo nuevo para la imagen
)

data class PerfilRequest(
    val nombre: String,
    val celular: String,
    val departamento: String,
    val ciudad: String,
    val fotoPerfil: String
)

data class EstadoRequest(
    val estado: String
)

data class RecuperarRequest(
    val email: String
)

data class ConfirmarRecuperacionRequest(
    val email: String,
    val codigo: String,
    val password: String
)

data class ChatRequest(
    val mensaje: String,
    val contexto: String? = null,
    val modoPista: Boolean = false
)

data class ChatResponse(
    val respuesta: String,
    val esBorrador: Boolean = false,
    val mascotaGenerada: MascotaBorrador? = null,
    val esPista: Boolean = false,
    val pistaGenerada: PistaBorrador? = null,
    val coincidencias: List<MascotaCoincidencia>? = null,
    val puedeReportar: Boolean = false,
    val avistamiento: Boolean = false
)

data class MascotaCoincidencia(
    @SerializedName("_id") val id: String,
    val nombre: String,
    val especie: String,
    val color: String?,
    val raza: String?,
    val ciudad: String?
)

data class MascotaBorrador(
    val especie: String?,
    val raza: String?,
    val color: String?,
    val ciudad: String?,
    val nombre: String?
)

data class PistaBorrador(
    val descripcion: String?,
    val ubicacion: String?
)

data class PistaRequest(
    val descripcion: String,
    val ubicacion: String
)

data class Pista(
    @SerializedName("_id") val id: String? = null,
    val descripcion: String,
    val ubicacion: String,
    val fecha: String? = null,
    val usuarioId: UsuarioData? = null
)
