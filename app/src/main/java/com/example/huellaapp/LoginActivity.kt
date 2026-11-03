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

class LoginActivity : AppCompatActivity() {

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
            validarLogin()
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

    private fun validarLogin() {
        val email = etEmailLogin.text.toString().trim()
        val password = etPasswordLogin.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmailLogin.error = "Correo electrónico inválido"
            return
        }

        // MOCK DE DATOS EN SHAREDPREFERENCES
        // Para simular un inicio de sesión completo y llenar el perfil
        val prefs = getSharedPreferences("HuellAppPrefs", MODE_PRIVATE)
        if (!prefs.contains("nombre")) {
            prefs.edit().apply {
                putString("nombre", "Usuario Demo")
                putString("email", email)
                putString("celular", "+57 300 000 0000")
                putString("ciudad", "Bogotá, Colombia")
            }.apply()
        }

        Toast.makeText(this, "¡Bienvenido a HuellApp!", Toast.LENGTH_SHORT).show()
        
        // Ir a la pantalla principal
        val intent = Intent(this, MainActivity::class.java)
        // Limpiamos el historial para que no pueda volver atrás al login
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
