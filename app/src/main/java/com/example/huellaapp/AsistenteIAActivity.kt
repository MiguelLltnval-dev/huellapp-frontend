package com.example.huellaapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.huellaapp.adapter.ChatAdapter
import com.example.huellaapp.model.ChatMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AsistenteIAActivity : AppCompatActivity() {

    private lateinit var ivBackChat: ImageView
    private lateinit var rvChat: RecyclerView
    private lateinit var etChatInput: EditText
    private lateinit var btnSendChat: ImageView
    private lateinit var cardDraftContainer: LinearLayout
    private lateinit var btnPublicarPista: Button

    private lateinit var chatAdapter: ChatAdapter
    private val mensajes = mutableListOf<ChatMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asistente_ia)

        ivBackChat = findViewById(R.id.ivBackChat)
        rvChat = findViewById(R.id.rvChat)
        etChatInput = findViewById(R.id.etChatInput)
        btnSendChat = findViewById(R.id.btnSendChat)
        cardDraftContainer = findViewById(R.id.cardDraftContainer)
        btnPublicarPista = findViewById(R.id.btnPublicarPista)

        // Configurar RecyclerView
        chatAdapter = ChatAdapter(mensajes)
        rvChat.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        rvChat.adapter = chatAdapter

        // Mensaje inicial de la IA
        chatAdapter.agregarMensaje(ChatMessage("¡Hola! Soy tu asistente de HuellApp. ¿Viste alguna mascota perdida o necesitas ayuda?", false))

        ivBackChat.setOnClickListener { finish() }

        btnSendChat.setOnClickListener {
            val texto = etChatInput.text.toString().trim()
            if (texto.isNotEmpty()) {
                // Agregar mensaje de usuario
                chatAdapter.agregarMensaje(ChatMessage(texto, true))
                etChatInput.text.clear()
                rvChat.scrollToPosition(mensajes.size - 1)

                // Simular respuesta de la IA
                simularRespuestaIA()
            }
        }

        btnPublicarPista.setOnClickListener {
            Toast.makeText(this, "Avistamiento publicado oficialmente.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun simularRespuestaIA() {
        lifecycleScope.launch {
            delay(1000) // Simula que la IA está "escribiendo"
            chatAdapter.agregarMensaje(ChatMessage("Entendido. He analizado tu reporte y extraído los datos clave. Confirma si el borrador es correcto para publicarlo:", false))
            rvChat.scrollToPosition(mensajes.size - 1)
            
            delay(500)
            // Mostrar la tarjeta borrador generada por IA
            cardDraftContainer.visibility = View.VISIBLE
        }
    }
}
