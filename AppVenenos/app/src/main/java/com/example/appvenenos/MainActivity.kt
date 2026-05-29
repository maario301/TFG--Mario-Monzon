package com.example.appvenenos

import android.Manifest
import android.content.Context // Importante para SharedPreferences
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.appvenenos.paginas.*
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var navBar: BottomNavigationView

    // Solicitud del permiso de ubicación (se pide nada más entrar en la app)
    private val permisoUbicacion =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { /* el resultado se gestiona luego en el mapa */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)

        navBar = findViewById(R.id.barra_navegacion)

        pedirPermisoUbicacion()

        if (savedInstanceState == null) {
            checkIntent(intent)
        }

        navBar.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> cambiarPagina(PaginaInicio())
                R.id.nav_camara -> cambiarPagina(PaginaCamara()) // ¡Ya no dará error!
                R.id.nav_historial -> cambiarPagina(PaginaHistorial())
                R.id.nav_mapa -> cambiarPagina(PaginaMapa())
            }
            true
        }
    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences("AppVenenos", Context.MODE_PRIVATE)
        val animalDetectado = prefs.getString("ultimo_animal", null)

        if (animalDetectado != null) {
            navBar.selectedItemId = R.id.nav_historial
            cambiarPagina(PaginaHistorial())

            // IMPORTANTE: Limpiamos el valor para que no te fuerce a ir al
            // historial cada vez que minimices y abras la app.
            prefs.edit().remove("ultimo_animal").apply()
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

    private fun pedirPermisoUbicacion() {
        val concedido = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!concedido) {
            permisoUbicacion.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    fun cambiarPagina(pagina: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.contenedor_paginas, pagina)
            .commit()
    }
}