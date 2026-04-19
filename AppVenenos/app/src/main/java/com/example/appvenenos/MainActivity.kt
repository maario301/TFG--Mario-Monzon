package com.example.appvenenos

import android.os.Bundle
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

        // Llamamos a la API usando nuestro objeto ConexionApi y sus Comandos
        ConexionApi.instancia.obtenerAnimales().enqueue(object : Callback<List<Animal>> {
            override fun onResponse(call: Call<List<Animal>>, response: Response<List<Animal>>) {
                if (response.isSuccessful) {
                    val animales = response.body()
                    if (animales != null) {
                        // Log para ver en la consola si realmente llegan datos
                        println("DEBUG_TFG: Han llegado ${animales.size} animales")

                        val adaptador = AnimalAdaptador(animales)
                        rvAnimales.adapter = adaptador

                        // Esta línea es mágica: obliga a la lista a dibujarse
                        adaptador.notifyDataSetChanged()

                        Toast.makeText(this@MainActivity, "Cargados: ${animales.size}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<List<Animal>>, t: Throwable) {
                // Error de conexión (IP mal, Django apagado, etc.)
                Toast.makeText(this@MainActivity, "Fallo de conexión: ${t.message}", Toast.LENGTH_LONG).show()
                android.util.Log.e("RETROFIT_ERROR", t.message ?: "Error desconocido")
            }
        })

       /* ConexionApi.instancia.obtenerAnimales().enqueue(object : Callback<List<Animal>> {
            override fun onResponse(call: Call<List<Animal>>, response: Response<List<Animal>>) {
                if (response.isSuccessful) {
                    val animales = response.body() ?: emptyList()
                    rvAnimales.adapter = AnimalAdaptador(animales)
                }
            }

            override fun onFailure(call: Call<List<Animal>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })*/
    }
}