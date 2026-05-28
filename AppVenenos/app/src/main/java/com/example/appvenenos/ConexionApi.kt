package com.example.appvenenos

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ConexionApi {
    private const val BASE_URL = "http://54.197.104.161:8000/"

    val instancia: ComandosApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ComandosApi::class.java)
    }
}