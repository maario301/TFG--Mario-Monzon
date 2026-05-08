package com.example.appvenenos.páginas // Esto le dice a Android que el archivo está en esa subcarpeta

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.appvenenos.R // Importante para que encuentre los XML

class PaginaInicio : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.pagina_inicio, container, false)
    }
}