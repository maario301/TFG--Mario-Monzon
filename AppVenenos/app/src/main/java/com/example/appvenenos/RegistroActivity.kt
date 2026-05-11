package com.example.appvenenos

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegistroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        // 1. Referencias a los elementos del XML
        val etUser = findViewById<EditText>(R.id.etRegistroUsuario)
        val etPass = findViewById<EditText>(R.id.etRegistroPassword)
        val etConfirm = findViewById<EditText>(R.id.etConfirmarPassword)
        val btnRegistrar = findViewById<Button>(R.id.btnFinalizarRegistro)

        // 2. Lógica del botón
        btnRegistrar.setOnClickListener {
            val user = etUser.text.toString().trim()
            val pass = etPass.text.toString().trim()
            val confirm = etConfirm.text.toString().trim()

            if (user.isNotEmpty() && pass.isNotEmpty()) {
                if (pass == confirm) {
                    ejecutarRegistro(user, pass)
                } else {
                    Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun ejecutarRegistro(user: String, pass: String) {
        // Django necesita estos TRES campos para validar el registro
        val datos = mapOf(
            "username" to user,
            "email" to "$user@test.com", // Añadimos esto como campo obligatorio
            "password" to pass
        )

        ConexionApi.instancia.registrar(datos).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@RegistroActivity, "¡Usuario $user creado!", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    // Si entra aquí, mira la consola de AWS: verás un 400 o un 404
                    Toast.makeText(this@RegistroActivity, "Error en el servidor: Revisa los datos", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@RegistroActivity, "Fallo de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}