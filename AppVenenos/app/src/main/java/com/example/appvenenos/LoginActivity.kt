package com.example.appvenenos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUsuario = findViewById<EditText>(R.id.etUsuario)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val sessionManager = SessionManager(this)

        btnLogin.setOnClickListener {
            val user = etUsuario.text.toString()
            val pass = etPassword.text.toString()

            if (user.isNotEmpty() && pass.isNotEmpty()) {
                val datos = mapOf("username" to user, "password" to pass)

                ConexionApi.instancia.login(datos).enqueue(object : Callback<TokenResponse> {
                    override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                        if (response.isSuccessful) {
                            val token = response.body()?.access
                            if (token != null) {
                                // GUARDAMOS LA LLAVE
                                sessionManager.saveAuthToken(token)

                                // VAMOS A LA LISTA DE ANIMALES
                                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                finish() // Cerramos el login para que no pueda volver atrás
                            }
                        } else {
                            Toast.makeText(this@LoginActivity, "Usuario o clave incorrectos", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                        Toast.makeText(this@LoginActivity, "Error de conexión con AWS", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }
}