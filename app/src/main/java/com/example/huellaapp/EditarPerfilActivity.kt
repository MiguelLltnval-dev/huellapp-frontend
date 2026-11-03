package com.example.huellaapp

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.huellaapp.utils.LocationData

class EditarPerfilActivity : AppCompatActivity() {

    private lateinit var etEditNombre: EditText
    private lateinit var etEditCelular: EditText
    private lateinit var autoCompleteEditDepartamento: AutoCompleteTextView
    private lateinit var autoCompleteEditCiudad: AutoCompleteTextView
    private lateinit var btnGuardarPerfil: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_perfil)

        val ivBackEditar: ImageView = findViewById(R.id.ivBackEditar)
        etEditNombre = findViewById(R.id.etEditNombre)
        etEditCelular = findViewById(R.id.etEditCelular)
        autoCompleteEditDepartamento = findViewById(R.id.autoCompleteEditDepartamento)
        autoCompleteEditCiudad = findViewById(R.id.autoCompleteEditCiudad)
        btnGuardarPerfil = findViewById(R.id.btnGuardarPerfil)

        ivBackEditar.setOnClickListener { finish() }

        // Configurar AutoCompleteTextView de Departamentos
        val departamentos = LocationData.departamentos
        val deptAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, departamentos)
        autoCompleteEditDepartamento.setAdapter(deptAdapter)

        // Poblar Ciudades según Departamento
        autoCompleteEditDepartamento.setOnItemClickListener { _, _, position, _ ->
            val deptSeleccionado = deptAdapter.getItem(position) ?: ""
            val ciudades = LocationData.departamentosYCiudades[deptSeleccionado] ?: emptyList()
            
            val ciudadAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, ciudades)
            autoCompleteEditCiudad.setAdapter(ciudadAdapter)
            autoCompleteEditCiudad.text.clear()
        }

        cargarDatosActuales()

        btnGuardarPerfil.setOnClickListener { guardarCambios() }
    }

    private fun cargarDatosActuales() {
        val prefs = getSharedPreferences("HuellAppPrefs", MODE_PRIVATE)
        etEditNombre.setText(prefs.getString("nombre", ""))
        etEditCelular.setText(prefs.getString("celular", ""))
        autoCompleteEditDepartamento.setText(prefs.getString("departamento", ""), false)
        autoCompleteEditCiudad.setText(prefs.getString("ciudad", ""), false)
        
        // Cargar las ciudades del departamento guardado para que la lista funcione
        val deptGuardado = prefs.getString("departamento", "")
        if (!deptGuardado.isNullOrEmpty()) {
            val ciudades = LocationData.departamentosYCiudades[deptGuardado] ?: emptyList()
            val ciudadAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, ciudades)
            autoCompleteEditCiudad.setAdapter(ciudadAdapter)
        }
    }

    private fun guardarCambios() {
        val nuevoNombre = etEditNombre.text.toString().trim()
        val nuevoCelular = etEditCelular.text.toString().trim()
        val nuevoDepto = autoCompleteEditDepartamento.text.toString().trim()
        val nuevaCiudad = autoCompleteEditCiudad.text.toString().trim()

        if (nuevoNombre.isEmpty() || nuevoCelular.isEmpty() || nuevoDepto.isEmpty() || nuevaCiudad.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = getSharedPreferences("HuellAppPrefs", MODE_PRIVATE)
        prefs.edit().apply {
            putString("nombre", nuevoNombre)
            putString("celular", nuevoCelular)
            putString("departamento", nuevoDepto)
            putString("ciudad", nuevaCiudad)
        }.apply()

        Toast.makeText(this, "Perfil actualizado exitosamente", Toast.LENGTH_SHORT).show()
        finish()
    }
}
