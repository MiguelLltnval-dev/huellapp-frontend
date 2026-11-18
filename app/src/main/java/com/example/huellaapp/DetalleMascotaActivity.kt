package com.example.huellaapp

import android.content.Intent
import android.net.Uri
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.huellaapp.adapter.PistaAdapter
import com.example.huellaapp.model.EstadoRequest
import com.example.huellaapp.model.esEstadoEnCasa
import com.example.huellaapp.network.RetrofitClient
import com.example.huellaapp.utils.ImageUtils
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.CancellationException
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle

class DetalleMascotaActivity : ResponsiveActivity() {

    companion object {
        fun crearIntent(context: android.content.Context, mascota: com.example.huellaapp.model.Mascota) =
            Intent(context, DetalleMascotaActivity::class.java).apply {
                putExtra("id", mascota.id)
                putExtra("nombre", mascota.nombre)
                putExtra("especie", mascota.especie)
                putExtra("color", mascota.color)
                putExtra("ciudad", mascota.ciudad)
                putExtra("estado", mascota.estado)
                putExtra("fotoUrl", mascota.fotoUrl)
                putExtra("usuarioId", mascota.usuarioId)
                putExtra("celularDueno", mascota.celularDueno)
            }
    }

    private lateinit var tvDetalleEstado: TextView
    private lateinit var btnMarcarEnCasa: TextView
    private var mascotaId: String? = null
    private var esMiMascota: Boolean = false

    private lateinit var rvPistas: RecyclerView
    private lateinit var tvEmptyPistas: TextView
    private lateinit var tvContadorPistas: TextView
    private lateinit var pistaAdapter: PistaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_mascota)

        // Botones de navegación
        val btnBackDetalle: ImageView = findViewById(R.id.btnBackDetalle)
        val btnShareDetalle: ImageView = findViewById(R.id.btnShareDetalle)
        btnMarcarEnCasa = findViewById(R.id.btnMarcarEnCasa) // Usamos TextView/Button

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

        rvPistas = findViewById(R.id.rvPistas)
        tvEmptyPistas = findViewById(R.id.tvEmptyPistas)
        tvContadorPistas = findViewById(R.id.tvContadorPistas)

        rvPistas.layoutManager = LinearLayoutManager(this)
        pistaAdapter = PistaAdapter(emptyList())
        rvPistas.adapter = pistaAdapter
        tvEmptyPistas.setOnClickListener { cargarPistas() }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                while (true) {
                    cargarPistas()
                    delay(8000)
                }
            }
        }

        // 1. RECUPERAR DATOS DEL INTENT
        mascotaId = intent.getStringExtra("id")
        val nombre = intent.getStringExtra("nombre") ?: "Desconocido"
        val especieStr = intent.getStringExtra("especie") ?: ""
        val colorYDetalles = intent.getStringExtra("color") ?: ""
        val ubicacionStr = intent.getStringExtra("ciudad") ?: ""
        val estado = intent.getStringExtra("estado") ?: "PERDIDO"
        val fotoBase64 = intent.getStringExtra("fotoUrl")
        val duenoId = intent.getStringExtra("usuarioId")

        // 1.5 VERIFICAR SI SOY EL DUEÑO DE LA MASCOTA
        val prefs = getSharedPreferences("HuellAppPrefs", MODE_PRIVATE)
        val miUserId = prefs.getString("userId", "")
        esMiMascota = (!duenoId.isNullOrEmpty() && duenoId == miUserId)

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
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "¡Ayúdame a encontrar a $nombre! Es un $especieStr perdido en $ubicacionStr. Descarga HuellApp para ver más detalles.")
                type = "text/plain"
            }
            startActivity(Intent.createChooser(shareIntent, "Compartir mascota vía..."))
        }

        val btnCompartir: Button = findViewById(R.id.btnCompartir)
        btnCompartir.setOnClickListener {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "¡Ayúdame a encontrar a $nombre! Es un $especieStr perdido en $ubicacionStr. Descarga HuellApp para ver más detalles.")
                type = "text/plain"
            }
            startActivity(Intent.createChooser(shareIntent, "Compartir mascota vía..."))
        }

        val ivMapaEstatico = findViewById<ImageView>(R.id.ivMapaEstatico)
        ivMapaEstatico.setOnClickListener {
            val mapIntentUri = Uri.parse("geo:0,0?q=$ubicacionStr")
            val mapIntent = Intent(Intent.ACTION_VIEW, mapIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent)
            } else {
                // Fallback si no tiene Google Maps instalado
                startActivity(Intent(Intent.ACTION_VIEW, mapIntentUri))
            }
        }

        val btnAgregarPista = findViewById<Button>(R.id.btnAgregarPista)
        btnAgregarPista.setOnClickListener {
            val intentPista = Intent(this, AsistenteIAActivity::class.java)
            intentPista.putExtra("CONTEXTO_MASCOTA", nombre)
            intentPista.putExtra("ID_MASCOTA", mascotaId)
            intentPista.putExtra("TEXTO_AVISTAMIENTO", intent.getStringExtra("TEXTO_AVISTAMIENTO"))
            startActivity(intentPista)
            Toast.makeText(this, "Dile a la IA dónde lo viste", Toast.LENGTH_SHORT).show()
        }

        btnMarcarEnCasa.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("¿La mascota ya está en casa?")
                .setMessage("El reporte se actualizará para toda la comunidad.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Confirmar") { _, _ -> marcarMascotaComoEnCasa() }
                .show()
        }
    }

    private var cargandoPistas = false
    private var pistasCargadas = false

    private fun cargarPistas() {
        if (mascotaId.isNullOrEmpty()) return
        if (cargandoPistas) return
        cargandoPistas = true
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.obtenerPistas(mascotaId!!)
                if (response.isSuccessful) {
                    val lista = response.body() ?: emptyList()
                    pistaAdapter.actualizarPistas(lista)
                    pistasCargadas = true
                    tvEmptyPistas.text = "Aún no hay pistas registradas para esta mascota."

                    tvContadorPistas.text = "${lista.size} registradas"

                    if (lista.isEmpty()) {
                        tvEmptyPistas.visibility = View.VISIBLE
                        rvPistas.visibility = View.GONE
                    } else {
                        tvEmptyPistas.visibility = View.GONE
                        rvPistas.visibility = View.VISIBLE
                    }
                } else error("HTTP " + response.code())
                val reporte = RetrofitClient.apiService.obtenerMascota(mascotaId!!)
                if (reporte.isSuccessful) reporte.body()?.let {
                    intent.putExtra("estado", it.estado)
                    actualizarUIEstado(it.estado)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                tvContadorPistas.text = "Sin actualizar"
                if (!pistasCargadas) {
                    tvEmptyPistas.text = "No se pudieron cargar las pistas. Toca aquí para reintentar."
                    tvEmptyPistas.visibility = View.VISIBLE
                }
            } finally {
                cargandoPistas = false
            }
        }
    }

    private fun actualizarUIEstado(estado: String) {
        if (estado.esEstadoEnCasa()) {
            tvDetalleEstado.text = "✓ EN CASA"
            tvDetalleEstado.setTextColor(Color.parseColor("#4CAF50"))
            tvDetalleEstado.setBackgroundResource(R.drawable.bg_badge_en_casa)
            btnMarcarEnCasa.visibility = View.GONE
        } else {
            tvDetalleEstado.text = "• PERDIDO"
            tvDetalleEstado.setTextColor(Color.parseColor("#F44336"))
            tvDetalleEstado.setBackgroundResource(R.drawable.bg_badge_perdido)
            // Solo mostrar el botón de "Marcar en Casa" si soy el dueño
            btnMarcarEnCasa.visibility = if (esMiMascota) View.VISIBLE else View.GONE
        }
        findViewById<Button>(R.id.btnAgregarPista).visibility =
            if (estado.esEstadoEnCasa()) View.GONE else View.VISIBLE
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

                val estadoGuardado = response.body()?.estado
                if (response.isSuccessful && estadoGuardado != null) {
                    Toast.makeText(this@DetalleMascotaActivity, "¡Mascota marcada como EN CASA!", Toast.LENGTH_SHORT).show()
                    intent.putExtra("estado", estadoGuardado)
                    actualizarUIEstado(estadoGuardado)
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
