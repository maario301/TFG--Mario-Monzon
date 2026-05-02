package com.example.appvenenos

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rvAnimales = findViewById<RecyclerView>(R.id.rvAnimales)
        rvAnimales.layoutManager = LinearLayoutManager(this)

        // 1. Recuperamos el token que guardamos antes con el SessionManager
        val sessionManager = SessionManager(this)
        val tokenGuardado = sessionManager.fetchAuthToken()

        if (tokenGuardado != null) {
            // 2. Si tenemos token, pedimos los animales enviando "Bearer llave"
            // Importante: usamos .getAnimales("Bearer $tokenGuardado")
            ConexionApi.instancia.getAnimales("Bearer $tokenGuardado").enqueue(object : Callback<List<Animal>> {
                override fun onResponse(call: Call<List<Animal>>, response: Response<List<Animal>>) {
                    if (response.isSuccessful) {
                        val animales = response.body()
                        if (animales != null) {
                            rvAnimales.adapter = AnimalAdaptador(animales)
                        }
                    } else if (response.code() == 401) {
                        Toast.makeText(this@MainActivity, "Sesión caducada. Haz login", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<Animal>>, t: Throwable) {
                    Log.e("API_ERROR", t.message ?: "Error")
                }
            })
        } else {
            // 3. Si NO hay token, aquí es donde deberías abrir la pantalla de Login
            Toast.makeText(this, "No hay sesión activa", Toast.LENGTH_SHORT).show()
        }
    }
}