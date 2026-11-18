package com.example.huellaapp

import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.example.huellaapp.model.RecuperarRequest
import com.example.huellaapp.model.ConfirmarRecuperacionRequest
import com.example.huellaapp.network.RetrofitClient
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import org.json.JSONObject
import retrofit2.Response

class RecuperarAccesoActivity : ResponsiveActivity() {

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
                        Toast.makeText(this@RecuperarAccesoActivity, response.body()?.mensaje, Toast.LENGTH_LONG).show()
                        mostrarConfirmacion(email)
                    } else {
                        mostrarError(response)
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Toast.makeText(this@RecuperarAccesoActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                } finally {
                    btnEnviarEnlace.isEnabled = true
                    btnEnviarEnlace.text = "Enviar código"
                }
            }
        }

        findViewById<Button>(R.id.btnYaTengoCodigo).setOnClickListener {
            val email = etEmailRecuperar.text.toString().trim()
            if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) mostrarConfirmacion(email)
            else etEmailRecuperar.error = "Ingresa un correo válido"
        }

        tvVolverLogin.setOnClickListener {
            finish()
        }
    }

    private fun mostrarConfirmacion(email: String) {
        val codigo = EditText(this).apply {
            hint = "Código del correo"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
            isSingleLine = true
        }
        val password = EditText(this).apply {
            hint = "Nueva contraseña"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            isSingleLine = true
        }
        val campos = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val margen = (24 * resources.displayMetrics.density).toInt()
            setPadding(margen, 0, margen, 0)
            addView(codigo)
            addView(password)
        }
        val dialogo = AlertDialog.Builder(this)
            .setTitle("Crear una nueva contraseña")
            .setMessage(email)
            .setView(campos)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Guardar", null)
            .show()
        val guardar = dialogo.getButton(AlertDialog.BUTTON_POSITIVE)
        guardar.setOnClickListener {
            val valorCodigo = codigo.text.toString().trim()
            val nuevaClave = password.text.toString()
            if (!valorCodigo.matches(Regex("[a-fA-F0-9]{16}"))) {
                codigo.error = "Copia los 16 caracteres del correo"
                return@setOnClickListener
            }
            if (nuevaClave.trim().length < 8 || nuevaClave.toByteArray().size > 72) {
                password.error = "Usa al menos 8 caracteres y una contraseña más corta si supera 72 bytes"
                return@setOnClickListener
            }
            guardar.isEnabled = false
            lifecycleScope.launch {
                try {
                    val request = ConfirmarRecuperacionRequest(email, valorCodigo, nuevaClave)
                    val respuesta = RetrofitClient.apiService.confirmarRecuperacion(request)
                    if (respuesta.isSuccessful) {
                        Toast.makeText(this@RecuperarAccesoActivity, "Contraseña actualizada. Ya puedes iniciar sesión.", Toast.LENGTH_LONG).show()
                        dialogo.dismiss()
                        finish()
                    } else mostrarError(respuesta)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Toast.makeText(this@RecuperarAccesoActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                } finally {
                    guardar.isEnabled = true
                }
            }
        }
    }

    private fun mostrarError(respuesta: Response<*>) {
        val mensaje = runCatching {
            respuesta.errorBody()?.string()?.let { JSONObject(it).optString("mensaje") }
        }.getOrNull()?.takeIf { it.isNotBlank() }
        Toast.makeText(this, mensaje ?: "No se pudo completar la recuperación.", Toast.LENGTH_LONG).show()
    }
}
