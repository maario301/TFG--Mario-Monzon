package com.example.appvenenos.paginas

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.appvenenos.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

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