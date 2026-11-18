package com.example.huellaapp

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DirectorioVetActivity : ResponsiveActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_directorio_vet)

        val ivBackDir: ImageView = findViewById(R.id.ivBackDir)
        val btnNotificar: Button = findViewById(R.id.btnNotificar)

        ivBackDir.setOnClickListener {
            finish()
        }

        btnNotificar.setOnClickListener {
            Toast.makeText(this, "¡Te notificaremos tan pronto lancemos el Directorio Vet!", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}
