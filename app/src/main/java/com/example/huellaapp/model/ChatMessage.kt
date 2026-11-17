package com.example.huellaapp.model

data class ChatMessage(
    val texto: String,
    val esUsuario: Boolean,
    val resultado: MascotaCoincidencia? = null,
    val abrirReporte: Boolean = false,
    val borrador: MascotaBorrador? = null,
    val textoAvistamiento: String? = null
)
