package com.example.appvenenos

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ComandosApi {
    @POST("api/token/")
    fun login(@Body credentials: Map<String, String>): Call<TokenResponse>

    // --- AÑADE ESTA FUNCIÓN AQUÍ ---
    @POST("api/register/") // O la ruta que use tu backend (ej: "api/usuarios/")
    fun registrar(@Body datos: Map<String, String>): Call<Void>

    @GET("api/animales/")
    fun getAnimales(@Header("Authorization") token: String): Call<List<Animal>>
}