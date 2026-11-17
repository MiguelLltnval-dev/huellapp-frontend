package com.example.huellaapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.huellaapp.adapter.MascotaAdapter
import com.example.huellaapp.model.Mascota
import com.example.huellaapp.model.esEstadoEnCasa
import com.example.huellaapp.network.RetrofitClient
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import android.content.Context
import com.example.huellaapp.utils.ImageUtils

class MainActivity : AppCompatActivity() {

    // Vistas
    private lateinit var rvDashboardMascotas: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var fabAgregar: FloatingActionButton

    // Nuevas Vistas (Botones IA y Perfil)
    private lateinit var btnAsistenteIAHeader: LinearLayout
    private lateinit var ivProfileUser: ImageView

    // Filtros
    private lateinit var chipTodos: TextView
    private lateinit var chipPerdidos: TextView
    private lateinit var chipEnCasa: TextView
    private lateinit var tvAlertaComunitaria: TextView
    private var esMisReportes: Boolean = false

    // Adaptador y Datos
    private lateinit var mascotaAdapter: MascotaAdapter
    private var listaOriginal = listOf<Mascota>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Enlazar vistas
        rvDashboardMascotas = findViewById(R.id.rvDashboardMascotas)
        progressBar = findViewById(R.id.progressBarMascotas)
        tvError = findViewById(R.id.tvErrorMascotas)
        fabAgregar = findViewById(R.id.fabAgregar)

        btnAsistenteIAHeader = findViewById(R.id.btnAsistenteIAHeader)
        ivProfileUser = findViewById(R.id.ivProfileUser)

        chipTodos = findViewById(R.id.chipTodos)
        chipPerdidos = findViewById(R.id.chipPerdidos)
        chipEnCasa = findViewById(R.id.chipEnCasa)
        tvAlertaComunitaria = findViewById(R.id.tvAlertaComunitaria)

        // Configurar RecyclerView
        rvDashboardMascotas.layoutManager = LinearLayoutManager(this)
        mascotaAdapter = MascotaAdapter(listaOriginal)
        rvDashboardMascotas.adapter = mascotaAdapter

        // Configurar Botón Flotante para ir a crear Mascota
        fabAgregar.setOnClickListener {
            startActivity(Intent(this, CrearMascotaActivity::class.java))
        }

        // ==========================================
        // ENLACES CORREGIDOS: BOTONES IA Y PERFIL
        // ==========================================

        // Botón grande naranja "Hablar con Asistente IA" en el header
        btnAsistenteIAHeader.setOnClickListener {
            startActivity(Intent(this, AsistenteIAActivity::class.java))
        }
        
        // Icono "IA" en el menú inferior
        findViewById<View>(R.id.navIA).setOnClickListener {
            startActivity(Intent(this, AsistenteIAActivity::class.java))
        }

        // Avatar de Perfil en la esquina superior derecha
        ivProfileUser.setOnClickListener {
            startActivity(Intent(this, MiPerfilActivity::class.java))
        }
        
        // Icono "Perfil" en el menú inferior
        findViewById<View>(R.id.navProfile).setOnClickListener {
            startActivity(Intent(this, MiPerfilActivity::class.java))
        }

        // OTROS BOTONES DEL BOTTOM NAV
        findViewById<View>(R.id.navDirectorio).setOnClickListener {
            startActivity(Intent(this, DirectorioVetActivity::class.java))
        }
        findViewById<View>(R.id.navHome).setOnClickListener {
            // Ya estamos en Home
        }

        // Configurar clicks en Filtros
        chipTodos.setOnClickListener { seleccionarFiltro(chipTodos, "TODOS") }
        chipPerdidos.setOnClickListener { seleccionarFiltro(chipPerdidos, "PERDIDO") }
        chipEnCasa.setOnClickListener { seleccionarFiltro(chipEnCasa, "EN_CASA") }

        // Traer datos de la API
        val prefs = getSharedPreferences("HuellAppPrefs", MODE_PRIVATE)
        RetrofitClient.authToken = prefs.getString("token", null)
        
        esMisReportes = intent.getStringExtra("FILTRO") == "MIS_REPORTES"
        
        cargarMascotas()
    }

    override fun onResume() {
        super.onResume()
        cargarMascotas()
        
        val prefs = getSharedPreferences("HuellAppPrefs", MODE_PRIVATE)
        val fotoGuardada = prefs.getString("fotoPerfil", "")
        if (!fotoGuardada.isNullOrEmpty()) {
            ImageUtils.cargarBase64EnImageView(fotoGuardada, ivProfileUser)
            ivProfileUser.imageTintList = null
            ivProfileUser.scaleType = ImageView.ScaleType.CENTER_CROP
        }
    }

    private fun cargarMascotas() {
        progressBar.visibility = View.VISIBLE
        rvDashboardMascotas.visibility = View.GONE
        tvError.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.obtenerMascotas()
                progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    listaOriginal = response.body() ?: emptyList()
                    
                    if (listaOriginal.isEmpty()) {
                        tvError.text = "Aún no hay reportes de mascotas."
                        tvError.visibility = View.VISIBLE
                        tvAlertaComunitaria.text = "Alerta Comunitaria: 0 mascotas perdidas."
                    } else {
                        chipTodos.text = "Todos (${listaOriginal.size})"
                        seleccionarFiltro(chipTodos, "TODOS") // Aplica el listado en la UI
                        val perdidosCount = listaOriginal.count { it.estado == "PERDIDO" }
                        tvAlertaComunitaria.text = "Alerta Comunitaria: $perdidosCount mascotas perdidas cerca."
                    }
                } else {
                    tvError.text = "Error al cargar reportes: ${response.code()}"
                    tvError.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                tvError.text = "Error de red: revisa tu conexión. (${e.message})"
                tvError.visibility = View.VISIBLE
            }
        }
    }

    private fun seleccionarFiltro(chipSeleccionado: TextView, estado: String) {
        // 1. Resetear diseño
        chipTodos.setBackgroundResource(R.drawable.bg_chip_unselected)
        chipTodos.setTextColor(getColor(R.color.huellapp_text_dark))

        chipPerdidos.setBackgroundResource(R.drawable.bg_chip_unselected)
        chipPerdidos.setTextColor(getColor(R.color.huellapp_text_dark))

        chipEnCasa.setBackgroundResource(R.drawable.bg_chip_unselected)
        chipEnCasa.setTextColor(getColor(R.color.huellapp_text_dark))

        // 2. Aplicar activo
        chipSeleccionado.setBackgroundResource(R.drawable.bg_chip_selected)
        chipSeleccionado.setTextColor(getColor(R.color.white))

        // 3. Filtrar
        var listaFiltrada = if (estado == "TODOS") {
            listaOriginal
        } else {
            listaOriginal.filter { if (estado.esEstadoEnCasa()) it.estado.esEstadoEnCasa() else it.estado == estado }
        }

        if (esMisReportes) {
            val prefs = getSharedPreferences("HuellAppPrefs", MODE_PRIVATE)
            val myUserId = prefs.getString("userId", "")
            listaFiltrada = listaFiltrada.filter { it.usuarioId == myUserId }
            tvError.text = "No tienes reportes creados."
        } else {
            tvError.text = "No hay resultados para este filtro."
        }

        // 4. Mostrar
        if (listaFiltrada.isEmpty()) {
            tvError.text = "No hay resultados para este filtro."
            tvError.visibility = View.VISIBLE
            rvDashboardMascotas.visibility = View.GONE
        } else {
            tvError.visibility = View.GONE
            rvDashboardMascotas.visibility = View.VISIBLE
            mascotaAdapter.actualizarLista(listaFiltrada)
        }
    }
}
