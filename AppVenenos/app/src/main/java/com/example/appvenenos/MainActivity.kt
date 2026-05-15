package com.example.appvenenos

import android.content.Context // Importante para SharedPreferences
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.appvenenos.paginas.*
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var navBar: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)

        navBar = findViewById(R.id.barra_navegacion)

        if (savedInstanceState == null) {
            checkIntent(intent)
        }

        navBar.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> cambiarPagina(PaginaInicio())
                R.id.nav_camara -> {
                    // Lanzamos la actividad de la cámara
                    val intent = Intent(this, PaginaCamara::class.java)
                    startActivity(intent)
                }
                R.id.nav_camara -> cambiarPagina(PaginaCamara()) // Ahora que es Fragment
                R.id.nav_mapa -> {
                    cambiarPagina(PaginaMapa()) // Ahora sí funcionará y mantendrá el menú
                }
            }
            true
        }
    }

    // --- ESTE ES EL MÉTODO QUE FALTABA ---
    override fun onResume() {
        super.onResume()
        // Cuando vuelves de la cámara, esta función se ejecuta automáticamente
        val prefs = getSharedPreferences("AppVenenos", Context.MODE_PRIVATE)
        val animalDetectado = prefs.getString("ultimo_animal", null)

        if (animalDetectado != null) {
            // Si hay un animal guardado, forzamos el cambio a la pestaña historial
            navBar.selectedItemId = R.id.nav_historial
            cambiarPagina(PaginaHistorial())
        }
    }

    private fun checkIntent(intent: Intent?) {
        val irAHistorial = intent?.getBooleanExtra("ir_a_historial", false) ?: false

        if (irAHistorial) {
            navBar.selectedItemId = R.id.nav_historial
            cambiarPagina(PaginaHistorial())
        } else {
            cambiarPagina(PaginaInicio())
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        checkIntent(intent)
    }

    fun cambiarPagina(pagina: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.contenedor_paginas, pagina)
            .commit()
    }
}