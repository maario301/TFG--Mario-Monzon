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

    class AnimalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgAnimal: ImageView = view.findViewById(R.id.imgAnimal)
        val txtNombre: TextView = view.findViewById(R.id.txtNombreComun)
        val txtCientifico: TextView = view.findViewById(R.id.txtNombreCientifico)
        val txtToxicidad: TextView = view.findViewById(R.id.txtToxicidad)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnimalViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_animal, parent, false)
        return AnimalViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: AnimalViewHolder, position: Int) {
        val animal = listaAnimales[position]
        holder.txtNombre.text = animal.nombre_comun
        holder.txtCientifico.text = animal.nombre_cientifico
        holder.txtToxicidad.text = "Toxicidad: ${animal.toxicidad}"

        Glide.with(holder.itemView.context)
            .load(animal.imagen_url)
            .into(holder.imgAnimal)
    }

    override fun getItemCount() = listaAnimales.size
}