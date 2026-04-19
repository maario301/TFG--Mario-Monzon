package com.example.appvenenos

import retrofit2.Call
import retrofit2.http.GET

interface ComandosApi {
    @GET("animales/")
    fun obtenerAnimales(): Call<List<Animal>>
}