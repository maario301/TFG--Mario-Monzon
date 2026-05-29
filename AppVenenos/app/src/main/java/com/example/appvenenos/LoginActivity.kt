package com.example.appvenenos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    // ID del cliente WEB de OAuth (el mismo que GOOGLE_CLIENT_ID del backend).
    // Lo creas en Google Cloud -> Credenciales -> ID de cliente OAuth -> Aplicación web.
    private val webClientId = "1008038388461-8aroje9apah72m44ciqk61lq14m6f7l0.apps.googleusercontent.com"

    private lateinit var sessionManager: SessionManager
    private lateinit var googleSignInClient: GoogleSignInClient

    // Recoge el resultado del selector de cuentas de Google
    private val googleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken != null) {
                    loginConGoogle(idToken)
                } else {
                    Toast.makeText(this, "No se obtuvo el token de Google", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                Toast.makeText(this, "Error con Google (código ${e.statusCode})", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etUsuario = findViewById<EditText>(R.id.etUsuario)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegistro = findViewById<android.widget.TextView>(R.id.tvRegistro)
        val btnGoogle = findViewById<SignInButton>(R.id.btnGoogle)
        sessionManager = SessionManager(this)

        // Configuración de Google Sign-In: pedimos el id_token para enviarlo al backend
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        tvRegistro.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }

        btnGoogle.setOnClickListener {
            // Cerramos cualquier sesión previa para que siempre muestre el selector de cuentas
            googleSignInClient.signOut().addOnCompleteListener {
                googleLauncher.launch(googleSignInClient.signInIntent)
            }
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
                                procesarLoginCorrecto(token, user)
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

    // Envía el id_token de Google al backend y, si todo va bien, entra como un login normal
    private fun loginConGoogle(idToken: String) {
        val datos = mapOf("id_token" to idToken)
        ConexionApi.instancia.loginGoogle(datos).enqueue(object : Callback<TokenResponse> {
            override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                if (response.isSuccessful) {
                    val token = response.body()?.access
                    if (token != null) {
                        procesarLoginCorrecto(token, "Usuario")
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "No se pudo iniciar sesión con Google", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Error de conexión con AWS", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Lógica común a ambos logins: guarda el token, consulta si es admin y entra a la app
    private fun procesarLoginCorrecto(token: String, usuarioFallback: String) {
        sessionManager.saveAuthToken(token)

        ConexionApi.instancia.getMe("Bearer $token").enqueue(object : Callback<MeResponse> {
            override fun onResponse(call: Call<MeResponse>, response: Response<MeResponse>) {
                val isAdmin = response.body()?.is_staff ?: false
                getSharedPreferences("AppVenenos", MODE_PRIVATE)
                    .edit()
                    .putBoolean("is_admin", isAdmin)
                    .putString("usuario_nombre", response.body()?.username ?: usuarioFallback)
                    .apply()
                irAMain()
            }
            override fun onFailure(call: Call<MeResponse>, t: Throwable) {
                irAMain()
            }
        })
    }

    private fun irAMain() {
        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
        finish()
    }
}
