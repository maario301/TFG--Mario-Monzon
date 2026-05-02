package com.example.appvenenos

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    // Creamos el archivo de "preferencias"
    private val prefs: SharedPreferences = context.getSharedPreferences("PREFS_TFG", Context.MODE_PRIVATE)

    companion object {
        private const val USER_TOKEN = "user_token"
    }

    // Función para guardar el token cuando hagas el login
    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, token)
        editor.apply()
    }

    // Función para recuperar el token cuando necesites pedir animales
    fun fetchAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    // Función para cerrar sesión (borrar el token)
    fun clearData() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}