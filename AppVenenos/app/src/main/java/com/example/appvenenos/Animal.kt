package com.example.appvenenos

import com.google.gson.annotations.SerializedName

data class Animal(
    val id: Int,
    @SerializedName("nombre_comun") val nombre_comun: String,
    @SerializedName("nombre_cientifico") val nombre_cientifico: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("toxicidad") val toxicidad: String,
    @SerializedName("sintomas") val sintomas: String,
    @SerializedName("tratamiento") val tratamiento: String,
    @SerializedName("imagen_url") val imagen_url: String?
)