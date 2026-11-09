package com.example.huellaapp

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.huellaapp.model.EstadoRequest
import com.example.huellaapp.network.RetrofitClient
import com.example.huellaapp.utils.ImageUtils
import kotlinx.coroutines.launch

class DetalleMascotaActivity : AppCompatActivity() {

    private lateinit var tvDetalleEstado: TextView
    private lateinit var btnMarcarEnCasa: TextView
    private var mascotaId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_mascota)

        // Botones de navegación
        val btnBackDetalle: ImageView = findViewById(R.id.btnBackDetalle)
        val btnShareDetalle: ImageView = findViewById(R.id.btnShareDetalle)
        btnMarcarEnCasa = findViewById(R.id.btnMarcarEnCasa) // Usamos TextView/Button
        val btnLlamarDueno: TextView = findViewById(R.id.btnLlamarDueno)

        // Referencias a las vistas de la Mascota
        val ivDetalleFoto: ImageView = findViewById(R.id.ivDetalleFoto)
        val tvDetalleNombre: TextView = findViewById(R.id.tvDetalleNombre)
        tvDetalleEstado = findViewById(R.id.tvDetalleEstado)
        val tvDetalleUbicacion: TextView = findViewById(R.id.tvDetalleUbicacion)
        
        val tvGridEspecie: TextView = findViewById(R.id.tvGridEspecie)
        val tvGridRaza: TextView = findViewById(R.id.tvGridRaza)
        val tvGridColor: TextView = findViewById(R.id.tvGridColor)
        val tvGridCiudad: TextView = findViewById(R.id.tvGridCiudad)

        val tvRasgos: TextView = findViewById(R.id.tvRasgos)

        // 1. RECUPERAR DATOS DEL INTENT
        mascotaId = intent.getStringExtra("id")
        val nombre = intent.getStringExtra("nombre") ?: "Desconocido"
        val especieStr = intent.getStringExtra("especie") ?: ""
        val colorYDetalles = intent.getStringExtra("color") ?: ""
        val ubicacionStr = intent.getStringExtra("ciudad") ?: ""
        val estado = intent.getStringExtra("estado") ?: "PERDIDO"
        val fotoBase64 = intent.getStringExtra("fotoUrl")

        // 2. POBLAR LOS DATOS BÁSICOS
        tvDetalleNombre.text = nombre
        
        // Cargar Foto Real
        if (!fotoBase64.isNullOrEmpty()) {
            ImageUtils.cargarBase64EnImageView(fotoBase64, ivDetalleFoto)
        }
        
        actualizarUIEstado(estado)

        // 3. DESEMPAQUETAR LOS DATOS
        tvGridEspecie.text = especieStr

        // Parsear "Color. Raza: X"
        if (colorYDetalles.contains("Raza:")) {
            val partes = colorYDetalles.split("Raza:")
            tvGridColor.text = partes[0].trim().removeSuffix(".")
            tvGridRaza.text = partes[1].trim()
        } else {
            tvGridColor.text = colorYDetalles
            tvGridRaza.text = "No especificada"
        }

        // Parsear "Ciudad. Perdido el: X" o "Barrio, Ciudad - Contacto: X"
        tvDetalleUbicacion.text = "Visto por última vez: $ubicacionStr"
        tvGridCiudad.text = ubicacionStr.split(",")[0].split(".")[0].trim()
        tvRasgos.text = colorYDetalles // Mostrar todo el bloque descriptivo como rasgos

        // 4. LISTENERS
        btnBackDetalle.setOnClickListener { finish() }

        btnShareDetalle.setOnClickListener {
            Toast.makeText(this, "Generando cartel de búsqueda...", Toast.LENGTH_SHORT).show()
        }

        btnLlamarDueno.setOnClickListener {
            Toast.makeText(this, "Abriendo marcador de llamadas...", Toast.LENGTH_SHORT).show()
        }

        btnMarcarEnCasa.setOnClickListener {
            marcarMascotaComoEnCasa()
        }
    }

    private fun actualizarUIEstado(estado: String) {
        if (estado == "EN_CASA") {
            tvDetalleEstado.text = "✓ EN CASA"
            tvDetalleEstado.setTextColor(Color.parseColor("#4CAF50"))
            tvDetalleEstado.setBackgroundResource(R.drawable.bg_badge_en_casa)
            btnMarcarEnCasa.visibility = View.GONE
        } else {
            tvDetalleEstado.text = "• PERDIDO"
            tvDetalleEstado.setTextColor(Color.parseColor("#F44336"))
            tvDetalleEstado.setBackgroundResource(R.drawable.bg_badge_perdido)
            btnMarcarEnCasa.visibility = View.VISIBLE
        }
    }

    private fun marcarMascotaComoEnCasa() {
        if (mascotaId.isNullOrEmpty()) {
            Toast.makeText(this, "Error: ID de mascota no encontrado", Toast.LENGTH_SHORT).show()
            return
        }

        btnMarcarEnCasa.isEnabled = false
        btnMarcarEnCasa.text = "Actualizando..."

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.actualizarEstadoMascota(
                    mascotaId!!,
                    EstadoRequest("EN_CASA")
                )

                if (response.isSuccessful) {
                    Toast.makeText(this@DetalleMascotaActivity, "¡Mascota marcada como EN CASA!", Toast.LENGTH_SHORT).show()
                    actualizarUIEstado("EN_CASA")
                } else {
                    Toast.makeText(this@DetalleMascotaActivity, "Error al actualizar: ${response.code()}", Toast.LENGTH_SHORT).show()
                    btnMarcarEnCasa.isEnabled = true
                    btnMarcarEnCasa.text = "Marcar como EN CASA"
                }
            } catch (e: Exception) {
                Toast.makeText(this@DetalleMascotaActivity, "Error de red", Toast.LENGTH_SHORT).show()
                btnMarcarEnCasa.isEnabled = true
                btnMarcarEnCasa.text = "Marcar como EN CASA"
            }
        }
    }
}
