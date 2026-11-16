package com.example.huellaapp

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.huellaapp.model.RecuperarRequest
import com.example.huellaapp.network.RetrofitClient
import kotlinx.coroutines.launch

class RecuperarAccesoActivity : AppCompatActivity() {

    private lateinit var etEmailRecuperar: EditText
    private lateinit var btnEnviarEnlace: Button
    private lateinit var tvVolverLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recuperar_acceso)

        etEmailRecuperar = findViewById(R.id.etEmailRecuperar)
        btnEnviarEnlace = findViewById(R.id.btnEnviarEnlace)
        tvVolverLogin = findViewById(R.id.tvVolverLogin)

        btnEnviarEnlace.setOnClickListener {
            val email = etEmailRecuperar.text.toString().trim()

            if (email.isEmpty()) {
                etEmailRecuperar.error = "Ingresa tu correo registrado"
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmailRecuperar.error = "Formato de correo inválido"
                return@setOnClickListener
            }

            btnEnviarEnlace.isEnabled = false
            btnEnviarEnlace.text = "Enviando..."

            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.apiService.recuperarPassword(RecuperarRequest(email))
                    if (response.isSuccessful) {
                        Toast.makeText(this@RecuperarAccesoActivity, "Enlace de recuperación enviado a $email", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        Toast.makeText(this@RecuperarAccesoActivity, "Error: El correo no existe.", Toast.LENGTH_SHORT).show()
                        btnEnviarEnlace.isEnabled = true
                        btnEnviarEnlace.text = "Enviar enlace de recuperación"
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@RecuperarAccesoActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                    btnEnviarEnlace.isEnabled = true
                    btnEnviarEnlace.text = "Enviar enlace de recuperación"
                }
            }
        }

        tvVolverLogin.setOnClickListener {
            finish() 
        }
    }
}
