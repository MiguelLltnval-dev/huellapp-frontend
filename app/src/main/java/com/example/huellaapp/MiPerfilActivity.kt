package com.example.huellaapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MiPerfilActivity : AppCompatActivity() {

    private lateinit var tvNombrePerfil: TextView
    private lateinit var tvCiudadPerfil: TextView
    private lateinit var tvCorreoPerfil: TextView
    private lateinit var tvCelularPerfil: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mi_perfil)

        val ivBackPerfil: ImageView = findViewById(R.id.ivBackPerfil)
        val tvEditar: TextView = findViewById(R.id.tvEditar)
        
        tvNombrePerfil = findViewById(R.id.tvNombrePerfil)
        tvCiudadPerfil = findViewById(R.id.tvCiudadPerfil)
        tvCorreoPerfil = findViewById(R.id.tvCorreoPerfil)
        tvCelularPerfil = findViewById(R.id.tvCelularPerfil)

        // Opciones del Menú
        val opcReportes: TextView = findViewById(R.id.opcReportes)
        val opcDirectorio: TextView = findViewById(R.id.opcDirectorio)
        val opcAsistente: TextView = findViewById(R.id.opcAsistente)
        val opcEditar: TextView = findViewById(R.id.opcEditar)
        val opcConfig: TextView = findViewById(R.id.opcConfig)

        val btnCerrarSesion: Button = findViewById(R.id.btnCerrarSesion)

        // Funciones de navegación
        ivBackPerfil.setOnClickListener { finish() }

        // Múltiples botones que van a Editar Perfil
        tvEditar.setOnClickListener {
            startActivity(Intent(this, EditarPerfilActivity::class.java))
        }
        opcEditar.setOnClickListener {
            startActivity(Intent(this, EditarPerfilActivity::class.java))
        }

        // Mis Reportes: Ir a Home y seleccionar filtro por Intent Extra (A ser manejado en MainActivity)
        opcReportes.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("FILTRO", "MIS_REPORTES")
            startActivity(intent)
        }

        opcDirectorio.setOnClickListener {
            startActivity(Intent(this, DirectorioVetActivity::class.java))
        }

        opcAsistente.setOnClickListener {
            startActivity(Intent(this, AsistenteIAActivity::class.java))
        }

        opcConfig.setOnClickListener {
            Toast.makeText(this, "Configuración y Privacidad", Toast.LENGTH_SHORT).show()
        }

        // Lógica Cerrar Sesión con Confirmación
        btnCerrarSesion.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Cerrar Sesión")
            builder.setMessage("¿Estás seguro que deseas cerrar tu sesión en HuellApp?")
            builder.setPositiveButton("Sí, salir") { dialog, _ ->
                dialog.dismiss()
                cerrarSesion()
            }
            builder.setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            val dialog = builder.create()
            dialog.show()
        }
    }

    override fun onResume() {
        super.onResume()
        cargarDatosPerfil()
    }

    private fun cargarDatosPerfil() {
        val prefs = getSharedPreferences("HuellAppPrefs", MODE_PRIVATE)
        val nombre = prefs.getString("nombre", "Sin Nombre")
        val correo = prefs.getString("email", "sin_correo@ejemplo.com")
        val celular = prefs.getString("celular", "+57 000 000 0000")
        
        val depto = prefs.getString("departamento", "Departamento")
        val ciudad = prefs.getString("ciudad", "Ciudad no definida")

        tvNombrePerfil.text = nombre
        tvCorreoPerfil.text = correo
        tvCelularPerfil.text = celular
        
        // Unimos Ciudad y Departamento para mostrar en el perfil
        if (ciudad == "Ciudad no definida" || depto == "Departamento") {
            tvCiudadPerfil.text = "Ubicación incompleta"
        } else {
            tvCiudadPerfil.text = "$ciudad, $depto"
        }
    }

    private fun cerrarSesion() {
        val prefs = getSharedPreferences("HuellAppPrefs", MODE_PRIVATE)
        prefs.edit().clear().apply() // Limpiar datos locales al salir

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
