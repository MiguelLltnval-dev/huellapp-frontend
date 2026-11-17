package com.example.huellaapp

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.huellaapp.model.Mascota
import com.example.huellaapp.network.RetrofitClient
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.Calendar

class CrearMascotaActivity : AppCompatActivity() {

    companion object {
        fun crearIntent(context: android.content.Context, borrador: com.example.huellaapp.model.MascotaBorrador?) =
            Intent(context, CrearMascotaActivity::class.java).apply {
                putExtra("nombre", borrador?.nombre)
                putExtra("especie", borrador?.especie)
                putExtra("raza", borrador?.raza)
                putExtra("color", borrador?.color)
                putExtra("ciudad", borrador?.ciudad)
            }
    }

    private lateinit var ivBack: ImageView
    private lateinit var btnCapturarFoto: LinearLayout
    private lateinit var ivFotoPreview: ImageView
    private lateinit var tvCambiarFoto: TextView
    
    private lateinit var etNombre: EditText
    private lateinit var etRaza: EditText
    private lateinit var etColor: EditText
    private lateinit var autoCompleteCiudad: AutoCompleteTextView
    private lateinit var etFecha: EditText
    private lateinit var etEspecieOtro: EditText
    private lateinit var btnRegistrarMascota: Button

    private lateinit var btnEspeciePerro: TextView
    private lateinit var btnEspecieGato: TextView
    private lateinit var btnEspecieOtro: TextView

    private var especieSeleccionada: String = ""
    private var fotoBase64: String? = null // Aquí guardaremos la foto convertida a Base64 para el backend

    // Selector de Galería usando ActivityResultContracts (La forma moderna y recomendada)
    private val selectorGaleria = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            try {
                // 1. Mostrar la foto en la UI
                ivFotoPreview.setImageURI(uri)
                ivFotoPreview.visibility = View.VISIBLE
                tvCambiarFoto.visibility = View.VISIBLE
                btnCapturarFoto.visibility = View.GONE

                // 2. Convertir la imagen a Base64 para enviarla en el JSON a Node.js
                val inputStream = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                val outputStream = ByteArrayOutputStream()
                // Comprimimos la imagen (calidad 50%) para que el JSON no sea tan pesado
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
        setContentView(R.layout.activity_crear_mascota)

        // Enlaces de Vistas
        ivBack = findViewById(R.id.ivBack)
        btnCapturarFoto = findViewById(R.id.btnCapturarFoto)
        ivFotoPreview = findViewById(R.id.ivFotoPreview)
        tvCambiarFoto = findViewById(R.id.tvCambiarFoto)
        etNombre = findViewById(R.id.etNombre)
        etRaza = findViewById(R.id.etRaza)
        etColor = findViewById(R.id.etColor)
        autoCompleteCiudad = findViewById(R.id.autoCompleteCiudad)
        etFecha = findViewById(R.id.etFecha)
        etEspecieOtro = findViewById(R.id.etEspecieOtro)
        btnRegistrarMascota = findViewById(R.id.btnRegistrarMascota)

        btnEspeciePerro = findViewById(R.id.btnEspeciePerro)
        btnEspecieGato = findViewById(R.id.btnEspecieGato)
        btnEspecieOtro = findViewById(R.id.btnEspecieOtro)

        // 1. Botón regresar
        ivBack.setOnClickListener { finish() }

        // 2. Abrir Galería al tocar el botón punteado o el texto de cambiar foto
        btnCapturarFoto.setOnClickListener {
            selectorGaleria.launch("image/*")
        }
        tvCambiarFoto.setOnClickListener {
            selectorGaleria.launch("image/*")
        }

        // 3. Toggles de Especie
        btnEspeciePerro.setOnClickListener { seleccionarEspecie("Perro") }
        btnEspecieGato.setOnClickListener { seleccionarEspecie("Gato") }
        btnEspecieOtro.setOnClickListener { seleccionarEspecie("Otro") }

        // 4. Configurar AutoComplete Ciudad
        val ciudades = resources.getStringArray(R.array.ciudades_colombia)
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, ciudades)
        autoCompleteCiudad.setAdapter(adapter)

        if (savedInstanceState == null) {
            etNombre.setText(intent.getStringExtra("nombre"))
            etRaza.setText(intent.getStringExtra("raza"))
            etColor.setText(intent.getStringExtra("color"))
            autoCompleteCiudad.setText(intent.getStringExtra("ciudad"), false)
        }
        val especieInicial = savedInstanceState?.getString("especieSeleccionada")
            ?: intent.getStringExtra("especie")
        when (especieInicial?.lowercase()) {
            "perro" -> seleccionarEspecie("Perro")
            "gato" -> seleccionarEspecie("Gato")
            "otro" -> seleccionarEspecie("Otro")
            null, "" -> Unit
            else -> {
                seleccionarEspecie("Otro")
                etEspecieOtro.setText(especieInicial)
            }
        }

        // 5. Configurar DatePicker para la Fecha
        etFecha.setOnClickListener {
            mostrarSelectorFecha()
        }

        // 6. Botón registrar
        btnRegistrarMascota.setOnClickListener {
            validarYRegistrarMascota()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("especieSeleccionada", especieSeleccionada)
    }

    private fun seleccionarEspecie(especie: String) {
        especieSeleccionada = especie

        // Resetear fondos
        btnEspeciePerro.setBackgroundResource(R.drawable.bg_toggle_unselected)
        btnEspeciePerro.setTextColor(getColor(R.color.huellapp_text_dark))

        btnEspecieGato.setBackgroundResource(R.drawable.bg_toggle_unselected)
        btnEspecieGato.setTextColor(getColor(R.color.huellapp_text_dark))

        btnEspecieOtro.setBackgroundResource(R.drawable.bg_toggle_unselected)
        btnEspecieOtro.setTextColor(getColor(R.color.huellapp_text_dark))

        // Activar el seleccionado
        when (especie) {
            "Perro" -> {
                btnEspeciePerro.setBackgroundResource(R.drawable.bg_toggle_selected)
                btnEspeciePerro.setTextColor(getColor(R.color.white))
                etEspecieOtro.visibility = View.GONE
            }
            "Gato" -> {
                btnEspecieGato.setBackgroundResource(R.drawable.bg_toggle_selected)
                btnEspecieGato.setTextColor(getColor(R.color.white))
                etEspecieOtro.visibility = View.GONE
            }
            "Otro" -> {
                btnEspecieOtro.setBackgroundResource(R.drawable.bg_toggle_selected)
                btnEspecieOtro.setTextColor(getColor(R.color.white))
                etEspecieOtro.visibility = View.VISIBLE
            }
        }
    }

    private fun mostrarSelectorFecha() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, yearSelected, monthSelected, daySelected ->
                // Mes inicia en 0, sumamos 1
                val fechaFormateada = "$daySelected/${monthSelected + 1}/$yearSelected"
                etFecha.setText(fechaFormateada)
            },
            year,
            month,
            day
        )
        // Opcional: Impedir fechas futuras
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun validarYRegistrarMascota() {
        val nombreInput = etNombre.text.toString().trim()
        val raza = etRaza.text.toString().trim()
        val color = etColor.text.toString().trim()
        val ciudad = autoCompleteCiudad.text.toString().trim()
        val fecha = etFecha.text.toString().trim()

        // El nombre es opcional en la UI (lo rellenamos si está vacío)
        val nombreFinal = if (nombreInput.isEmpty()) "Desconocido" else nombreInput

        // Lógica de Especie
        if (especieSeleccionada.isEmpty()) {
            Toast.makeText(this, "Selecciona una especie", Toast.LENGTH_SHORT).show()
            return
        }

        // Validación de Foto
        if (fotoBase64 == null) {
            Toast.makeText(this, "La foto principal es obligatoria", Toast.LENGTH_SHORT).show()
            return
        }

        val especieFinal = if (especieSeleccionada == "Otro") {
            val textoOtro = etEspecieOtro.text.toString().trim()
            if (textoOtro.isEmpty()) {
                etEspecieOtro.error = "Especifica el animal"
                return
            }
            textoOtro
        } else {
            especieSeleccionada
        }

        if (raza.isEmpty() || color.isEmpty() || ciudad.isEmpty() || fecha.isEmpty()) {
            Toast.makeText(this, "Por favor, completa los campos faltantes", Toast.LENGTH_SHORT).show()
            return
        }

        btnRegistrarMascota.isEnabled = false
        btnRegistrarMascota.text = "Guardando reporte..."

        // Empaquetamos la información para enviarla al backend 
        // ya que el backend solo espera "nombre, especie, color, ciudad".
        val colorYDetalles = "$color. Raza: $raza"
        val ciudadYFecha = "$ciudad. Perdido el: $fecha"

        val prefs = getSharedPreferences("HuellAppPrefs", MODE_PRIVATE)
        val myUserId = prefs.getString("userId", null)

        val nuevaMascota = Mascota(
            id = null,
            nombre = nombreFinal,
            especie = especieFinal,
            color = colorYDetalles,
            ciudad = ciudadYFecha,
            estado = "PERDIDO",
            fotoUrl = fotoBase64, // Enviamos la imagen codificada a Node.js
            usuarioId = myUserId
        )

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.registrarMascota(nuevaMascota)
                if (response.isSuccessful) {
                    Toast.makeText(this@CrearMascotaActivity, "Mascota reportada exitosamente", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this@CrearMascotaActivity, "Error servidor: ${response.code()}", Toast.LENGTH_SHORT).show()
                    restaurarBoton()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CrearMascotaActivity, "Error de red: ${e.message}", Toast.LENGTH_LONG).show()
                restaurarBoton()
            }
        }
    }

    private fun restaurarBoton() {
        btnRegistrarMascota.isEnabled = true
        btnRegistrarMascota.text = "Guardar Reporte ✓"
    }
}
