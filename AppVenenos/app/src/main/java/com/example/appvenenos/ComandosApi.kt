package com.example.appvenenos

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ComandosApi {
    // Para el login
    @POST("api/token/")
    fun login(@Body credentials: Map<String, String>): Call<TokenResponse>

    // Para los animales (ahora con Header de autorización)
    @GET("api/animales/")
    fun getAnimales(@Header("Authorization") token: String): Call<List<Animal>>
}