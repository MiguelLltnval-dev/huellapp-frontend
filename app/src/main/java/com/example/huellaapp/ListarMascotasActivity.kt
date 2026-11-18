package com.example.huellaapp

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.huellaapp.adapter.MascotaAdapter
import com.example.huellaapp.model.Mascota
import com.example.huellaapp.network.RetrofitClient
import kotlinx.coroutines.launch

class ListarMascotasActivity : ResponsiveActivity() {

    private lateinit var rvMascotas: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var mascotaAdapter: MascotaAdapter
    private var listaMascotas = mutableListOf<Mascota>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listar_mascotas)

        // Conexión de vistas con findViewById
        rvMascotas = findViewById(R.id.rvMascotas)
        progressBar = findViewById(R.id.progressBar)
        tvError = findViewById(R.id.tvError)

        rvMascotas.layoutManager = LinearLayoutManager(this)
        mascotaAdapter = MascotaAdapter(listaMascotas)
        rvMascotas.adapter = mascotaAdapter

        // Cargar datos al iniciar la actividad
        obtenerMascotas()
    }

    private fun obtenerMascotas() {
        // Estado inicial: Mostramos el progreso y ocultamos la lista y el texto de error
        progressBar.visibility = View.VISIBLE
        rvMascotas.visibility = View.GONE
        tvError.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.obtenerMascotas()

                // Ocultamos la carga al recibir la respuesta
                progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val mascotas = response.body()
                    if (mascotas.isNullOrEmpty()) {
                        // Caso exitoso pero sin datos
                        tvError.text = "No hay mascotas registradas"
                        tvError.visibility = View.VISIBLE
                        rvMascotas.visibility = View.GONE
                    } else {
                        // Caso exitoso con datos
                        rvMascotas.visibility = View.VISIBLE
                        tvError.visibility = View.GONE
                        mascotaAdapter.actualizarLista(mascotas)
                    }
                } else {
                    // Error de respuesta del servidor (ej. 500, 404)
                    tvError.text = "Error al cargar mascotas: ${response.code()}"
                    tvError.visibility = View.VISIBLE
                    rvMascotas.visibility = View.GONE
                }
            } catch (e: Exception) {
                // Error de red (servidor caído, falta de internet, etc.)
                progressBar.visibility = View.GONE
                tvError.text = "Error de red: ${e.message}"
                tvError.visibility = View.VISIBLE
                rvMascotas.visibility = View.GONE
            }
        }
    }
}
