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

        // --- BLOQUE 2: CONSEJOS ---
        val txtConsejo: TextView = root.findViewById(R.id.txtConsejoCuerpo)
        val listaConsejos = listOf(
            "Mantén la calma y aléjate lentamente si avistas una especie peligrosa.",
            "Usa botas altas y pantalones largos en zonas de vegetación densa.",
            "No metas las manos en grietas sin revisar primero."
        )
        txtConsejo.text = listaConsejos.random()

        // --- BLOQUE 3: TARJETAS INTERACTIVAS (FLIP CARDS) ---
        val container = root.findViewById<FrameLayout>(R.id.containerSerpiente)
        val front = root.findViewById<CardView>(R.id.cardFrontSerpiente)
        val back = root.findViewById<CardView>(R.id.cardBackSerpiente)

        setupFlipCard(container, front, back)

        return root
    }

    private fun setupFlipCard(container: View, front: View, back: View) {
        var isFrontVisible = true

        // Cargamos los animadores que creaste en res/animator
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