package com.example.huellaapp.network

import com.example.huellaapp.model.Mascota
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @GET("mascotas")
    suspend fun obtenerMascotas(): Response<List<Mascota>>

    @POST("mascotas")
    suspend fun registrarMascota(@Body mascota: Mascota): Response<Mascota>
}
