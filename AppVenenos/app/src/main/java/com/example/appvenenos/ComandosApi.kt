package com.example.appvenenos

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ComandosApi {
    @POST("api/token/") // Asegúrate de que termina en /
    fun login(@Body credentials: Map<String, String>): Call<TokenResponse>

    @GET("api/animales/") // Asegúrate de que termina en /
    fun getAnimales(@Header("Authorization") token: String): Call<List<Animal>>
}