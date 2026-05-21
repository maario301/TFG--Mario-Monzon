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
        val tvRegistro = findViewById<android.widget.TextView>(R.id.tvRegistro)
        val sessionManager = SessionManager(this)

        tvRegistro.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }

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
                                sessionManager.saveAuthToken(token)

                                // Consultar si es admin
                                ConexionApi.instancia.getMe("Bearer $token").enqueue(object : Callback<MeResponse> {
                                    override fun onResponse(call: Call<MeResponse>, response: Response<MeResponse>) {
                                        val isAdmin = response.body()?.is_staff ?: false
                                        getSharedPreferences("AppVenenos", MODE_PRIVATE)
                                            .edit()
                                            .putBoolean("is_admin", isAdmin)
                                            .putString("usuario_nombre", response.body()?.username ?: user)
                                            .apply()
                                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                        finish()
                                    }
                                    override fun onFailure(call: Call<MeResponse>, t: Throwable) {
                                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                        finish()
                                    }
                                })
                            }
                        } else {
                            Toast.makeText(this@LoginActivity, "Usuario o clave incorrectos", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                        Toast.makeText(this@LoginActivity, "Error de conexión con AWS", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}