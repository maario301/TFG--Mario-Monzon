package com.example.appvenenos.paginas

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.appvenenos.Animal
import com.example.appvenenos.Avistamiento
import com.example.appvenenos.ConexionApi
import com.example.appvenenos.R
import com.example.appvenenos.SessionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PaginaInicio : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.pagina_inicio, container, false)

        // --- BLOQUE 1: BOTÓN DEL PÁNICO ---
        val cardPanico: CardView = root.findViewById(R.id.cardPanico)
        cardPanico.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("🆘 GUÍA DE EMERGENCIA")
                .setMessage("1. NO intentes atrapar al animal.\n" +
                        "2. Inmoviliza la extremidad afectada.\n" +
                        "3. NO realices cortes ni succiones el veneno.\n" +
                        "4. Llama al 112 o acude al hospital más cercano.")
                .setPositiveButton("Entendido", null)
                .show()
        }

        // --- BLOQUE: ESTADÍSTICAS EN VIVO (datos reales del backend) ---
        val txtStatTotal = root.findViewById<TextView>(R.id.txtStatTotal)
        val txtStatEspecies = root.findViewById<TextView>(R.id.txtStatEspecies)
        val txtStatUltimo = root.findViewById<TextView>(R.id.txtStatUltimo)

        val token = SessionManager(requireContext()).fetchAuthToken()
        if (token != null) {
            ConexionApi.instancia.getAvistamientos("Bearer $token")
                .enqueue(object : Callback<List<Avistamiento>> {
                    override fun onResponse(call: Call<List<Avistamiento>>, response: Response<List<Avistamiento>>) {
                        if (!isAdded) return
                        if (response.isSuccessful) {
                            val lista = response.body() ?: emptyList()
                            txtStatTotal.text = lista.size.toString()
                            txtStatEspecies.text = lista.map { it.nombre_cientifico }.distinct().size.toString()

                            val ultimo = lista.maxByOrNull { it.id }
                            txtStatUltimo.text = if (ultimo != null) {
                                "🆕 Último: ${ultimo.nombre_comun} (${ultimo.fecha})"
                            } else {
                                "Aún no hay avistamientos registrados"
                            }
                        } else {
                            txtStatUltimo.text = "No se pudieron cargar las estadísticas"
                        }
                    }
                    override fun onFailure(call: Call<List<Avistamiento>>, t: Throwable) {
                        if (!isAdded) return
                        txtStatUltimo.text = "Sin conexión con el servidor"
                    }
                })
        }

        // --- BLOQUE: ANIMAL DEL DÍA (rota cada día y muestra todo lo de la BD) ---
        val imgAnimalDia = root.findViewById<ImageView>(R.id.imgAnimalDia)
        val txtNombreDia = root.findViewById<TextView>(R.id.txtNombreDia)
        val txtCientificoDia = root.findViewById<TextView>(R.id.txtCientificoDia)
        val txtToxicidadDia = root.findViewById<TextView>(R.id.txtToxicidadDia)
        val txtDescripcionDia = root.findViewById<TextView>(R.id.txtDescripcionDia)
        val txtHabitatDia = root.findViewById<TextView>(R.id.txtHabitatDia)
        val txtSintomasDia = root.findViewById<TextView>(R.id.txtSintomasDia)
        val txtTratamientoDia = root.findViewById<TextView>(R.id.txtTratamientoDia)
        val txtCuriosidadDia = root.findViewById<TextView>(R.id.txtCuriosidadDia)

        if (token != null) {
            ConexionApi.instancia.getAnimales("Bearer $token")
                .enqueue(object : Callback<List<Animal>> {
                    override fun onResponse(call: Call<List<Animal>>, response: Response<List<Animal>>) {
                        if (!isAdded) return
                        val lista = response.body() ?: emptyList()
                        if (!response.isSuccessful || lista.isEmpty()) return

                        // Elegimos un animal según el día del año: rota solo y a todos les toca
                        val diaDelAno = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
                        val animal = lista[diaDelAno % lista.size]

                        txtNombreDia.text = animal.nombre_comun
                        txtCientificoDia.text = animal.nombre_cientifico.replace("_", " ")
                        txtToxicidadDia.text = "Toxicidad: ${animal.toxicidad}"
                        txtDescripcionDia.text = "📖 ${animal.descripcion ?: "Sin descripción"}"
                        txtHabitatDia.text = "📍 Dónde encontrarlo: ${animal.habitat ?: "No disponible"}"
                        txtSintomasDia.text = "🩺 Síntomas: ${animal.sintomas ?: "No especificados"}"
                        txtTratamientoDia.text = "💊 Tratamiento: ${animal.tratamiento ?: "Consulte a un médico"}"
                        txtCuriosidadDia.text = "💡 ¿Sabías que...? ${animal.curiosidad ?: "—"}"

                        // Imagen del animal desde drawables (por nombre científico)
                        val nombreFoto = animal.nombre_cientifico.lowercase().trim().replace(" ", "_")
                        val resId = resources.getIdentifier(nombreFoto, "drawable", requireContext().packageName)
                        Glide.with(this@PaginaInicio)
                            .load(if (resId != 0) resId else R.drawable.serpiente)
                            .centerCrop()
                            .into(imgAnimalDia)
                    }
                    override fun onFailure(call: Call<List<Animal>>, t: Throwable) {}
                })
        }

        // --- BLOQUE 2: CONSEJOS DE PREVENCIÓN EXTENSOS ---
        val txtConsejo: TextView = root.findViewById(R.id.txtConsejoCuerpo)
        val listaConsejos = listOf(
            "• EQUIPAMIENTO TÉCNICO: En tus salidas, utiliza calzado de caña alta (botas) y pantalones largos de tejido resistente. El 70% de las mordeduras de ofidios ocurren por pisadas accidentales en zonas de vegetación.",
            "• PROTOCOLO DE INSPECCIÓN: Jamás introduzcas las manos en grietas rocosas, troncos huecos o bajo piedras grandes sin inspeccionar previamente con un bastón. Muchos arácnidos utilizan estos lugares como refugio.",
            "• DISTANCIA CRÍTICA: Si avistas una serpiente, mantén una distancia mínima de 3 metros. No intentes capturarla; el animal solo atacará si se siente acorralado. Retrocede lentamente sin movimientos bruscos.",
            "• HIGIENE EN ACAMPADA: Mantén el perímetro de tu tienda libre de restos orgánicos. Los residuos atraen roedores, los cuales son la principal fuente de alimento de los depredadores venenosos.",
            "• RECONOCIMIENTO VISUAL: Aprende a identificar las especies locales. Las víboras suelen tener pupilas verticales y cabeza triangular, mientras que las culebras tienen pupilas redondas."
        )
        txtConsejo.text = listaConsejos.random()

        // --- BLOQUE 3: TARJETAS INTERACTIVAS (FLIP CARDS) ---
        val scale = resources.displayMetrics.density
        val cameraDist = 8000 * scale

        // 1. Tarjeta de SERPIENTES
        val containerSerpiente = root.findViewById<FrameLayout>(R.id.containerSerpiente)
        val frontSerpiente = root.findViewById<CardView>(R.id.cardFrontSerpiente)
        val backSerpiente = root.findViewById<View>(R.id.layoutBackSerpiente)

        // Personalizar cara trasera Serpiente
        backSerpiente.findViewById<TextView>(R.id.txtTituloBack).text = "AUXILIO: SERPIENTES"
        backSerpiente.findViewById<TextView>(R.id.txtPasosBack).text = "1. Mantener calma e inmovilizar.\n2. NO succionar ni cortar.\n3. Quitar joyas/reloj.\n4. Traslado urgente al hospital."

        containerSerpiente.cameraDistance = cameraDist
        setupFlipCard(containerSerpiente, frontSerpiente, backSerpiente)

        // 2. Tarjeta de ARAÑAS
        val containerArana = root.findViewById<FrameLayout>(R.id.containerArana)
        val frontArana = root.findViewById<CardView>(R.id.cardFrontArana)
        val backArana = root.findViewById<View>(R.id.layoutBackArana)

        // Personalizar cara trasera Araña
        backArana.findViewById<TextView>(R.id.txtTituloBack).text = "AUXILIO: ARÁCNIDOS"
        backArana.findViewById<TextView>(R.id.txtPasosBack).text = "1. Lavar con agua y jabón.\n2. Aplicar frío local (hielo).\n3. Mantener zona elevada.\n4. Acudir a médico si hay mareo."

        containerArana.cameraDistance = cameraDist
        setupFlipCard(containerArana, frontArana, backArana)

        // 3. Tarjeta de AVISPAS/ABEJAS
        val containerAvispa = root.findViewById<FrameLayout>(R.id.containerAvispa)
        val frontAvispa = root.findViewById<CardView>(R.id.cardFrontAvispa)
        val backAvispa = root.findViewById<View>(R.id.layoutBackAvispa)

        // Personalizar cara trasera Avispa
        backAvispa.findViewById<TextView>(R.id.txtTituloBack).text = "AUXILIO: INSECTOS"
        backAvispa.findViewById<TextView>(R.id.txtPasosBack).text = "1. Retirar aguijón (raspando).\n2. Lavar con agua y jabón.\n3. Aplicar amoníaco o frío.\n4. Si hay ahogo, llamar 112."

        containerAvispa.cameraDistance = cameraDist
        setupFlipCard(containerAvispa, frontAvispa, backAvispa)

        return root
    }

    private fun setupFlipCard(container: View, front: View, back: View) {
        var isFrontVisible = true

        val flipOut = AnimatorInflater.loadAnimator(context, R.animator.card_flip_out) as AnimatorSet
        val flipIn = AnimatorInflater.loadAnimator(context, R.animator.card_flip_in) as AnimatorSet

        container.setOnClickListener {
            if (isFrontVisible) {
                flipOut.setTarget(front)
                flipIn.setTarget(back)
                flipOut.start()
                flipIn.start()
                isFrontVisible = false
            } else {
                flipOut.setTarget(back)
                flipIn.setTarget(front)
                flipOut.start()
                flipIn.start()
                isFrontVisible = true
            }
        }
    }
}