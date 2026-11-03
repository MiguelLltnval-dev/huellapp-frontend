package com.example.huellaapp

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DetalleMascotaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_mascota)

        // Botones de navegación
        val btnBackDetalle: ImageView = findViewById(R.id.btnBackDetalle)
        val btnShareDetalle: ImageView = findViewById(R.id.btnShareDetalle)
        val btnMarcarEnCasa: TextView = findViewById(R.id.btnMarcarEnCasa) // Usamos TextView/Button
        val btnLlamarDueno: TextView = findViewById(R.id.btnLlamarDueno)

        // Referencias a las vistas de la Mascota
        val tvDetalleNombre: TextView = findViewById(R.id.tvDetalleNombre)
        val tvDetalleEstado: TextView = findViewById(R.id.tvDetalleEstado)
        val tvDetalleUbicacion: TextView = findViewById(R.id.tvDetalleUbicacion)
        
        val tvGridEspecie: TextView = findViewById(R.id.tvGridEspecie)
        val tvGridRaza: TextView = findViewById(R.id.tvGridRaza)
        val tvGridColor: TextView = findViewById(R.id.tvGridColor)
        val tvGridCiudad: TextView = findViewById(R.id.tvGridCiudad)

        val tvRasgos: TextView = findViewById(R.id.tvRasgos)

        // 1. RECUPERAR DATOS DEL INTENT
        val nombre = intent.getStringExtra("nombre") ?: "Desconocido"
        val especieStr = intent.getStringExtra("especie") ?: ""
        val colorYDetalles = intent.getStringExtra("color") ?: ""
        val ubicacionStr = intent.getStringExtra("ciudad") ?: ""
        val estado = intent.getStringExtra("estado") ?: "PERDIDO"
        // TODO: En Fase futura decodificar la fotoBase64 del intent si está presente

        // 2. POBLAR LOS DATOS BÁSICOS
        tvDetalleNombre.text = nombre
        
        if (estado == "EN_CASA") {
            tvDetalleEstado.text = "✓ EN CASA"
            tvDetalleEstado.setTextColor(Color.parseColor("#4CAF50"))
            tvDetalleEstado.setBackgroundResource(R.drawable.bg_badge_en_casa)
            btnMarcarEnCasa.visibility = View.GONE // Ocultarlo si ya está en casa
        } else {
            tvDetalleEstado.text = "• PERDIDO"
            tvDetalleEstado.setTextColor(Color.parseColor("#F44336"))
            tvDetalleEstado.setBackgroundResource(R.drawable.bg_badge_perdido)
        }

        // 3. DESEMPAQUETAR LOS DATOS (Ya que backend los tiene unidos en el string)
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
        tvGridCiudad.text = ubicacionStr.split(",")[0].trim()
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
            // TODO: Hacer PATCH al backend para actualizar el estado a "EN_CASA"
            Toast.makeText(this, "Mascota marcada como En Casa", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
