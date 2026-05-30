package com.example.appvenenos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appvenenos.paginas.PaginaMapa
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.SimpleDateFormat
import java.util.*
import androidx.fragment.app.Fragment

class AnimalAdaptador(
    private val listaAnimales: List<Animal>,
    private val origen: String? = null
) : RecyclerView.Adapter<AnimalAdaptador.AnimalViewHolder>() {

    class AnimalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgAnimal: ImageView = view.findViewById(R.id.imgAnimal)
        val btnLocalizar: Button = view.findViewById(R.id.btnLocalizar)
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

        // 1. Rellenar los campos de texto con los datos del servidor
        holder.txtNombre.text = animal.nombre_comun
        holder.txtCientifico.text = animal.nombre_cientifico
        holder.txtToxicidad.text = "Toxicidad: ${animal.toxicidad}"
        holder.txtDescripcion.text = animal.descripcion ?: "Sin descripción disponible"
        holder.txtSintomas.text = "Síntomas: ${animal.sintomas ?: "No especificados"}"
        holder.txtTratamiento.text = "Tratamiento: ${animal.tratamiento ?: "Consulte a un médico"}"

        // 2. Lógica del botón Localizar (Enviar datos al Mapa)
        holder.btnLocalizar.setOnClickListener {
            val activity = context as? MainActivity

            // Formateamos el nombre científico para que coincida con el archivo drawable
            // Ejemplo: "Vipera Latastei" -> "vipera_latastei"
            val nombreFormateado = animal.nombre_cientifico
                .lowercase()
                .trim()
                .replace(" ", "_")

            val fechaActual = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

            val bundle = Bundle().apply {
                putString("nombre", animal.nombre_comun)
                putString("cientifico", nombreFormateado)
                putString("fecha", fechaActual)
                // "camara" -> GPS real | si no, colocación manual en el mapa
                putString("origen", origen ?: "galeria")
            }

            // Creamos la instancia de PaginaMapa con los argumentos
            val fragmentMapa = PaginaMapa().apply {
                arguments = bundle
            }

            activity?.let { main ->
                // Cambiar al fragmento del mapa (con los datos del avistamiento)
                main.cambiarPagina(fragmentMapa)

                // Marcar visualmente el icono "Mapa" SIN recrear el fragmento.
                // (usar selectedItemId dispararía el listener y crearía un PaginaMapa()
                //  vacío encima, perdiendo los argumentos -> no aparecía el marcador)
                val navBar = main.findViewById<BottomNavigationView>(R.id.barra_navegacion)
                navBar.menu.findItem(R.id.nav_mapa).isChecked = true
            }
        }

        // 3. Cargar la imagen del animal en la lista (usando Glide)
        // Buscamos en drawable el nombre científico en minúsculas (sin guiones bajos para la lista si así los tienes)
        val nombreFotoLocal = animal.nombre_cientifico.lowercase().trim().replace(" ", "_")
        val imageResId = context.resources.getIdentifier(nombreFotoLocal, "drawable", context.packageName)

        Glide.with(context)
            .load(if (imageResId != 0) imageResId else android.R.drawable.ic_menu_gallery)
            .centerCrop()
            .into(holder.imgAnimal)
    }

    override fun getItemCount() = listaAnimales.size
}