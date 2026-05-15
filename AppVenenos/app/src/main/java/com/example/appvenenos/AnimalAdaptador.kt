package com.example.appvenenos

import android.content.Intent // IMPORTANTE: Para el cambio de pantalla
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button // IMPORTANTE: Para el botón
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appvenenos.paginas.PaginaMapa // IMPORTANTE: Ruta a tu pantalla de mapa

class AnimalAdaptador(private val listaAnimales: List<Animal>) :
    RecyclerView.Adapter<AnimalAdaptador.AnimalViewHolder>() {

    class AnimalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgAnimal: ImageView = view.findViewById(R.id.imgAnimal)
        val btnLocalizar: Button = view.findViewById(R.id.btnLocalizar) // Ya no dará error
        val txtNombre: TextView = view.findViewById(R.id.txtNombreComun)
        val txtCientifico: TextView = view.findViewById(R.id.txtNombreCientifico)
        val txtToxicidad: TextView = view.findViewById(R.id.txtToxicidad)
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
        val context = holder.itemView.context

        // 1. Rellenar textos
        holder.txtNombre.text = animal.nombre_comun
        holder.txtCientifico.text = animal.nombre_cientifico
        holder.txtToxicidad.text = "Toxicidad: ${animal.toxicidad}"
        holder.txtDescripcion.text = animal.descripcion ?: "Sin descripción disponible"
        holder.txtSintomas.text = "Síntomas: ${animal.sintomas ?: "No especificados"}"
        holder.txtTratamiento.text = "Tratamiento: ${animal.tratamiento ?: "Consulte a un médico"}"

        // 2. Configurar botón de localización
        holder.btnLocalizar.setOnClickListener {
            val intent = Intent(context, PaginaMapa::class.java).apply {
                putExtra("nombre", animal.nombre_comun)
                putExtra("cientifico", animal.nombre_cientifico)
            }
            context.startActivity(intent)
        }

        // 3. LÓGICA DE IMAGEN LOCAL (RECURSOS DRAWABLE)
        val nombreFotoLocal = animal.nombre_cientifico.lowercase()
        val imageResId = context.resources.getIdentifier(
            nombreFotoLocal,
            "drawable",
            context.packageName
        )

        Glide.with(context)
            .load(if (imageResId != 0) imageResId else android.R.drawable.ic_menu_gallery)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.stat_notify_error)
            .centerCrop()
            .into(holder.imgAnimal)
    }

    override fun getItemCount() = listaAnimales.size
}