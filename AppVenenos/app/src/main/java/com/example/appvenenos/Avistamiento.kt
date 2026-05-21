package com.example.appvenenos

data class Avistamiento(
    val id: Int,
    val nombre_comun: String,
    val nombre_cientifico: String,
    val usuario: String,
    val fecha: String,
    val latitud: Double,
    val longitud: Double
)