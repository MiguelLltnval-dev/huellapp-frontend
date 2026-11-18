package com.example.huellaapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.huellaapp.model.LoginRequest
import com.example.huellaapp.network.RetrofitClient
import kotlinx.coroutines.launch

class LoginActivity : ResponsiveActivity() {

    private lateinit var etEmailLogin: EditText
    private lateinit var etPasswordLogin: EditText
    private lateinit var tvOlvidastePass: TextView
    private lateinit var btnLogin: Button
    private lateinit var tvGoToRegister: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Enlace de vistas con findViewById
        etEmailLogin = findViewById(R.id.etEmailLogin)
        etPasswordLogin = findViewById(R.id.etPasswordLogin)
        tvOlvidastePass = findViewById(R.id.tvOlvidastePass)
        btnLogin = findViewById(R.id.btnLogin)
        tvGoToRegister = findViewById(R.id.tvGoToRegister)

        // Botón Iniciar Sesión
        btnLogin.setOnClickListener {
            validarYLoguear()
        }

        // Navegar a Recuperar Contraseña
        tvOlvidastePass.setOnClickListener {
            val intent = Intent(this, RecuperarAccesoActivity::class.java)
            startActivity(intent)
        }

        // Navegar a Registro
        tvGoToRegister.setOnClickListener {
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
        }
    }

    private fun validarYLoguear() {
        val email = etEmailLogin.text.toString().trim()
        val password = etPasswordLogin.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmailLogin.error = "Correo electrónico inválido"
            return
        }

        // Bloquear botón durante la petición
        btnLogin.isEnabled = false
        btnLogin.text = "Iniciando sesión..."

        val request = LoginRequest(email, password)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.iniciarSesion(request)

                if (response.isSuccessful) {
                    val authResponse = response.body()
                    val usuario = authResponse?.usuario

                    // GUARDAR TOKEN Y DATOS DEL USUARIO EN SHAREDPREFERENCES
                    val prefs = getSharedPreferences("HuellAppPrefs", MODE_PRIVATE)
                    prefs.edit().apply {
                        putString("token", authResponse?.token)
                        putString("userId", usuario?._id)
                        putString("nombre", usuario?.nombre)
                        putString("email", usuario?.email)
                        putString("celular", usuario?.celular)
                        putString("departamento", usuario?.departamento)
                        putString("ciudad", usuario?.ciudad)
                        putString("fotoPerfil", usuario?.fotoPerfil ?: "")
                    }.apply()

                    RetrofitClient.authToken = authResponse?.token

                    Toast.makeText(this@LoginActivity, "¡Bienvenido a HuellApp!", Toast.LENGTH_SHORT).show()

                    // Ir a la pantalla principal
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    // Limpiamos el historial para que no pueda volver atrás al login
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)

                } else {
                    Toast.makeText(this@LoginActivity, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                    restaurarBoton()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Error de red: revisa tu conexión", Toast.LENGTH_SHORT).show()
                restaurarBoton()
            }
        }
    }

    private fun restaurarBoton() {
        btnLogin.isEnabled = true
        btnLogin.text = "Iniciar Sesión →"
    }
}
