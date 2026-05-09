package com.example.appvenenos

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.appvenenos.páginas.* import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Este XML es el que tiene el FrameLayout y la barra de navegación
        setContentView(R.layout.main_layout)

        val navBar = findViewById<BottomNavigationView>(R.id.barra_navegacion)     // Cargar la pantalla de Inicio al abrir la app
        if (savedInstanceState == null) {
            cambiarPagina(PaginaInicio())
        }

        navBar.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> cambiarPagina(PaginaInicio())
                R.id.nav_camara -> cambiarPagina(PaginaCamara())
                R.id.nav_historial -> cambiarPagina(PaginaHistorial())
                R.id.nav_mapa -> cambiarPagina(PaginaMapa())
            }
            true
        }
    }

    private fun cambiarPagina(pagina: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.contenedor_paginas, pagina)
            .commit()
    }
}