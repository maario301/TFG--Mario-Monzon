package com.example.appvenenos

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class VenoMapApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Forzamos siempre el modo claro (como se ve en Android Studio),
        // sin seguir el modo oscuro del sistema.
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}
