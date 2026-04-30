package com.example.appvenenos


    import com.google.gson.annotations.SerializedName

data class Animal(
    val id: Int,

    @SerializedName("nombre_servidor") val nombre_comun: String,
    @SerializedName("nombre_cientifico") val nombre_cientifico: String,
    @SerializedName("descripcion") val descripcion: String, // Añadido
    @SerializedName("toxicidad") val toxicidad: String,
    @SerializedName("sintomas") val sintomas: String,      // Añadido
    @SerializedName("tratamiento") val tratamiento: String, // Añadido
    @SerializedName("imagen_url") val imagen_url: String
)

    /*
    val id: Int,
    val nombre_comun: String,
    val nombre_cientifico: String,
    val descripcion: String,
    val toxicidad: String,
    val sintomas: String,
    val tratamiento: String,
    val imagen_url: String
    */
