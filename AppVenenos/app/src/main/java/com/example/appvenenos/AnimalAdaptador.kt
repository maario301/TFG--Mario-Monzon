package com.example.appvenenos

import android.os.Bundle // IMPORTANTE
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button // IMPORTANTE
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appvenenos.paginas.PaginaMapa
import com.google.android.material.bottomnavigation.BottomNavigationView // IMPORTANTE
import androidx.fragment.app.Fragment // IMPORTANTE

class AnimalAdaptador(private val listaAnimales: List<Animal>) :
    RecyclerView.Adapter<AnimalAdaptador.AnimalViewHolder>() {

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

        // 1. Rellenar textos
        holder.txtNombre.text = animal.nombre_comun
        holder.txtCientifico.text = animal.nombre_cientifico
        holder.txtToxicidad.text = "Toxicidad: ${animal.toxicidad}"
        holder.txtDescripcion.text = animal.descripcion ?: "Sin descripción disponible"
        holder.txtSintomas.text = "Síntomas: ${animal.sintomas ?: "No especificados"}"
        holder.txtTratamiento.text = "Tratamiento: ${animal.tratamiento ?: "Consulte a un médico"}"

        // --- EL TERCER CÓDIGO VA AQUÍ ---
        holder.btnLocalizar.setOnClickListener {
            // Convertimos el contexto a MainActivity para usar sus funciones públicas
            val activity = context as? MainActivity

            // Creamos el "paquete" con los datos del animal
            val bundle = Bundle().apply {
                putString("nombre", animal.nombre_comun)
                putString("cientifico", animal.nombre_cientifico)
            }

            // Creamos el Fragmento del mapa y le metemos los datos
            val fragmentMapa = PaginaMapa().apply {
                arguments = bundle
            }

            activity?.let {
                // Cambiamos al fragmento del mapa (manteniendo el menú)
                it.cambiarPagina(fragmentMapa)

                // Marcamos visualmente el icono del mapa en la barra inferior
                val navBar = it.findViewById<BottomNavigationView>(R.id.barra_navegacion)
                navBar.selectedItemId = R.id.nav_mapa
            }
        }

        // 3. Lógica de imagen local
        val nombreFotoLocal = animal.nombre_cientifico.lowercase()
        val imageResId = context.resources.getIdentifier(nombreFotoLocal, "drawable", context.packageName)

        Glide.with(context)
            .load(if (imageResId != 0) imageResId else android.R.drawable.ic_menu_gallery)
            .centerCrop()
            .into(holder.imgAnimal)
    }

    override fun getItemCount() = listaAnimales.size
}