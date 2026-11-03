package com.example.huellaapp

import android.content.Context
import android.os.Bundle
import android.util.Patterns
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.huellaapp.utils.LocationData
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegistroActivity : AppCompatActivity() {

    private lateinit var etNombreRegistro: EditText
    private lateinit var etEmailRegistro: EditText
    private lateinit var etCelularRegistro: EditText
    private lateinit var etPasswordRegistro: EditText
    private lateinit var autoCompleteDepartamento: AutoCompleteTextView
    private lateinit var autoCompleteCiudad: AutoCompleteTextView
    private lateinit var btnRegistrarme: Button
    private lateinit var tvGoToLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        // Enlace de vistas
        etNombreRegistro = findViewById(R.id.etNombreRegistro)
        etEmailRegistro = findViewById(R.id.etEmailRegistro)
        etCelularRegistro = findViewById(R.id.etCelularRegistro)
        etPasswordRegistro = findViewById(R.id.etPasswordRegistro)
        autoCompleteDepartamento = findViewById(R.id.autoCompleteDepartamento)
        autoCompleteCiudad = findViewById(R.id.autoCompleteCiudad)
        btnRegistrarme = findViewById(R.id.btnRegistrarme)
        tvGoToLogin = findViewById(R.id.tvGoToLogin)

        // Configurar AutoCompleteTextView de Departamentos
        val departamentos = LocationData.departamentos
        val deptAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, departamentos)
        autoCompleteDepartamento.setAdapter(deptAdapter)

        // Lógica para poblar Ciudades dependiendo del Departamento seleccionado
        autoCompleteDepartamento.setOnItemClickListener { _, _, position, _ ->
            val deptSeleccionado = deptAdapter.getItem(position) ?: ""
            val ciudades = LocationData.departamentosYCiudades[deptSeleccionado] ?: emptyList()
            
            val ciudadAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, ciudades)
            autoCompleteCiudad.setAdapter(ciudadAdapter)
            autoCompleteCiudad.text.clear() // Limpiar la ciudad si el usuario cambia el departamento
        }

        // Eventos
        btnRegistrarme.setOnClickListener {
            validarRegistro()
        }

        tvGoToLogin.setOnClickListener {
            finish() // Cierra esta actividad y regresa al Login
        }
    }

    private fun validarRegistro() {
        val nombre = etNombreRegistro.text.toString().trim()
        val email = etEmailRegistro.text.toString().trim()
        val celular = etCelularRegistro.text.toString().trim()
        val password = etPasswordRegistro.text.toString().trim()

        if (nombre.isEmpty() || email.isEmpty() || celular.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmailRegistro.error = "Correo electrónico inválido"
            return
        }

        if (password.length < 8) {
            etPasswordRegistro.error = "La contraseña debe tener mínimo 8 caracteres"
            return
        }

        // GUARDAR DATOS EN SHAREDPREFERENCES
        val prefs = getSharedPreferences("HuellAppPrefs", MODE_PRIVATE)
        prefs.edit().apply {
            putString("nombre", nombre)
            putString("email", email)
            putString("celular", celular)
            putString("departamento", autoCompleteDepartamento.text.toString().trim())
            putString("ciudad", autoCompleteCiudad.text.toString().trim())
        }.apply()

        // Simulación de éxito (posteriormente irá aquí Retrofit)
        Toast.makeText(this, "Cuenta creada exitosamente. Por favor, inicia sesión.", Toast.LENGTH_LONG).show()
        finish() // Regresar al Login
    }
}
