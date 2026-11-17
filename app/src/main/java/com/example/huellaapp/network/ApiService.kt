package com.example.huellaapp.network

import com.example.huellaapp.model.AuthResponse
import com.example.huellaapp.model.EstadoRequest
import com.example.huellaapp.model.LoginRequest
import com.example.huellaapp.model.Mascota
import com.example.huellaapp.model.RegistroRequest
import com.example.huellaapp.model.PerfilRequest
import com.example.huellaapp.model.RecuperarRequest
import com.example.huellaapp.model.ConfirmarRecuperacionRequest
import com.example.huellaapp.model.ChatRequest
import com.example.huellaapp.model.ChatResponse
import com.example.huellaapp.model.Pista
import com.example.huellaapp.model.PistaRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    // --- ENDPOINTS MASCOTAS ---

    @GET("mascotas")
    suspend fun obtenerMascotas(): Response<List<Mascota>>

    @GET("mascotas/{id}")
    suspend fun obtenerMascota(@Path("id") id: String): Response<Mascota>

    @POST("mascotas")
    suspend fun registrarMascota(@Body mascota: Mascota): Response<Mascota>

    @PATCH("mascotas/{id}/estado")
    suspend fun actualizarEstadoMascota(
        @Path("id") id: String,
        @Body request: EstadoRequest
    ): Response<Mascota>

    @GET("mascotas/{id}/pistas")
    suspend fun obtenerPistas(@Path("id") id: String): Response<List<Pista>>

    @POST("mascotas/{id}/pistas")
    suspend fun registrarPista(
        @Path("id") id: String,
        @Body request: PistaRequest
    ): Response<Pista>


    // --- ENDPOINTS USUARIOS (AUTH) ---

    @POST("usuarios/registro")
    suspend fun registrarUsuario(@Body request: RegistroRequest): Response<AuthResponse>

    @POST("usuarios/login")
    suspend fun iniciarSesion(@Body request: LoginRequest): Response<AuthResponse>

    @PATCH("usuarios/{id}/perfil")
    suspend fun actualizarPerfilUsuario(
        @Path("id") id: String,
        @Body request: PerfilRequest
    ): Response<AuthResponse>

    @POST("usuarios/recuperar")
    suspend fun recuperarPassword(@Body request: RecuperarRequest): Response<AuthResponse>

    @POST("usuarios/recuperar/confirmar")
    suspend fun confirmarRecuperacion(@Body request: ConfirmarRecuperacionRequest): Response<AuthResponse>

    // --- ENDPOINTS ASISTENTE IA ---

    @POST("ia/chat")
    suspend fun chatearConIA(@Body request: ChatRequest): Response<ChatResponse>
}
