package com.example.huellaapp

import android.os.Bundle
import android.net.Uri
import android.provider.MediaStore
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.huellaapp.model.PerfilRequest
import com.example.huellaapp.network.RetrofitClient
import com.example.huellaapp.utils.ImageUtils
import com.example.huellaapp.utils.LocationData
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class EditarPerfilActivity : AppCompatActivity() {

    private lateinit var etEditNombre: EditText
    private lateinit var etEditCelular: EditText
    private lateinit var autoCompleteEditDepartamento: AutoCompleteTextView
    private lateinit var autoCompleteEditCiudad: AutoCompleteTextView
    private lateinit var btnGuardarPerfil: Button
    
    private lateinit var ivEditAvatar: ImageView
    private lateinit var btnSeleccionarAvatar: LinearLayout
    private var fotoBase64: String? = null

    // Selector de Galería
    private val selectorGaleria = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            try {
                ivEditAvatar.setImageURI(uri)
                val inputStream = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
                val imageBytes = outputStream.toByteArray()
                fotoBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT)
            } catch (e: Exception) {
                Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_perfil)

        val ivBackEditar: ImageView = findViewById(R.id.ivBackEditar)
        etEditNombre = findViewById(R.id.etEditNombre)
        etEditCelular = findViewById(R.id.etEditCelular)
        autoCompleteEditDepartamento = findViewById(R.id.autoCompleteEditDepartamento)
        autoCompleteEditCiudad = findViewById(R.id.autoCompleteEditCiudad)
        btnGuardarPerfil = findViewById(R.id.btnGuardarPerfil)
        
        ivEditAvatar = findViewById(R.id.ivEditAvatar)
        btnSeleccionarAvatar = findViewById(R.id.btnSeleccionarAvatar)

        ivBackEditar.setOnClickListener { finish() }
        
        btnSeleccionarAvatar.setOnClickListener {
            selectorGaleria.launch("image/*")
        }

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
        
        // Cargar foto actual si existe
        val fotoGuardada = prefs.getString("fotoPerfil", "")
        if (!fotoGuardada.isNullOrEmpty()) {
            fotoBase64 = fotoGuardada
            ImageUtils.cargarBase64EnImageView(fotoGuardada, ivEditAvatar)
        }
        
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

        if (fotoBase64.isNullOrEmpty()) {
            Toast.makeText(this, "La foto de perfil es obligatoria.", Toast.LENGTH_SHORT).show()
            return
        }

        if (nuevoNombre.isEmpty() || nuevoCelular.isEmpty() || nuevoDepto.isEmpty() || nuevaCiudad.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
            return
        }

        btnGuardarPerfil.isEnabled = false
        btnGuardarPerfil.text = "Guardando..."

        val request = PerfilRequest(nuevoNombre, nuevoCelular, nuevoDepto, nuevaCiudad, fotoBase64!!)

        val prefs = getSharedPreferences("HuellAppPrefs", MODE_PRIVATE)
        val userId = prefs.getString("userId", "") ?: ""

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.actualizarPerfilUsuario(userId, request)
                if (response.isSuccessful) {
                    prefs.edit().apply {
                        putString("nombre", nuevoNombre)
                        putString("celular", nuevoCelular)
                        putString("departamento", nuevoDepto)
                        putString("ciudad", nuevaCiudad)
                        putString("fotoPerfil", fotoBase64)
                    }.apply()

                    Toast.makeText(this@EditarPerfilActivity, "Perfil actualizado exitosamente", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@EditarPerfilActivity, "Error al guardar perfil", Toast.LENGTH_SHORT).show()
                    btnGuardarPerfil.isEnabled = true
                    btnGuardarPerfil.text = "Guardar Cambios ✓"
                }
            } catch (e: Exception) {
                Toast.makeText(this@EditarPerfilActivity, "Error de conexión", Toast.LENGTH_SHORT).show()
                btnGuardarPerfil.isEnabled = true
                btnGuardarPerfil.text = "Guardar Cambios ✓"
            }
        }
    }
}
