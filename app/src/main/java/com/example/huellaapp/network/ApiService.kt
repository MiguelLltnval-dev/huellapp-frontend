package com.example.huellaapp.network

import com.example.huellaapp.model.AuthResponse
import com.example.huellaapp.model.EstadoRequest
import com.example.huellaapp.model.LoginRequest
import com.example.huellaapp.model.Mascota
import com.example.huellaapp.model.RegistroRequest
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

    @POST("mascotas")
    suspend fun registrarMascota(@Body mascota: Mascota): Response<Mascota>

    @PATCH("mascotas/{id}/estado")
    suspend fun actualizarEstadoMascota(
        @Path("id") id: String,
        @Body request: EstadoRequest
    ): Response<Mascota>


    // --- ENDPOINTS USUARIOS (AUTH) ---

    @POST("usuarios/registro")
    suspend fun registrarUsuario(@Body request: RegistroRequest): Response<AuthResponse>

    @POST("usuarios/login")
    suspend fun iniciarSesion(@Body request: LoginRequest): Response<AuthResponse>
}
