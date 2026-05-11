package com.example.appvenenos

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ComandosApi {
    @POST("api/token/")
    fun login(@Body credentials: Map<String, String>): Call<TokenResponse>

    @POST("api/register/") // <--- ASEGÚRATE DE QUE ESTÉ ASÍ, CON BARRA AL FINAL
    fun registrar(@Body datos: Map<String, String>): Call<Void>

    @GET("api/animales/")
    fun getAnimales(@Header("Authorization") token: String): Call<List<Animal>>
}