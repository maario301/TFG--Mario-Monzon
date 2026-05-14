package com.example.appvenenos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class AnimalAdaptador(private val listaAnimales: List<Animal>) :
    RecyclerView.Adapter<AnimalAdaptador.AnimalViewHolder>() {

    // 1. ViewHolder ampliado con todos los campos del XML
    class AnimalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgAnimal: ImageView = view.findViewById(R.id.imgAnimal)
        val txtNombre: TextView = view.findViewById(R.id.txtNombreComun)
        val txtCientifico: TextView = view.findViewById(R.id.txtNombreCientifico)
        val txtToxicidad: TextView = view.findViewById(R.id.txtToxicidad)

        // Nuevos campos para la ficha completa
        val txtDescripcion: TextView = view.findViewById(R.id.txtDescripcion)
        val txtSintomas: TextView = view.findViewById(R.id.txtSintomas)
        val txtTratamiento: TextView = view.findViewById(R.id.txtTratamiento)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimalViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_animal, parent, false)
        return AnimalViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: AnimalViewHolder, position: Int) {
        val animal = listaAnimales[position]

        // Rellenar textos básicos
        holder.txtNombre.text = animal.nombre_comun
        holder.txtCientifico.text = animal.nombre_cientifico
        holder.txtToxicidad.text = "Toxicidad: ${animal.toxicidad}"

        // 2. Rellenar los nuevos atributos (usando ?: para evitar nulos)
        holder.txtDescripcion.text = animal.descripcion ?: "Sin descripción disponible"
        holder.txtSintomas.text = "Síntomas: ${animal.sintomas ?: "No especificados"}"
        holder.txtTratamiento.text = "Tratamiento: ${animal.tratamiento ?: "Consulte a un médico"}"

        // 3. Construcción de URL y carga de imagen (IP de tu AWS)
        val baseUrl = "http://98.90.201.0:8000"
        val fullImageUrl = animal.imagen_url?.let { url ->
            if (url.startsWith("http")) url else baseUrl + url
        } ?: ""

        Glide.with(holder.itemView.context)
            .load(fullImageUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.stat_notify_error)
            .centerCrop() // Para que la imagen quede bien encuadrada
            .into(holder.imgAnimal)
    }

    override fun getItemCount() = listaAnimales.size
}