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
import com.example.huellaapp.model.ChatRequest
import com.example.huellaapp.network.RetrofitClient
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

        chatAdapter = ChatAdapter(mensajes)
        rvChat.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        rvChat.adapter = chatAdapter

        val contextoMascota = intent.getStringExtra("CONTEXTO_MASCOTA")
        if (!contextoMascota.isNullOrEmpty()) {
            chatAdapter.agregarMensaje(ChatMessage("¡Hola! Veo que quieres reportar una pista sobre $contextoMascota. Dime, ¿dónde lo viste o qué detalles recuerdas?", false))
        } else {
            chatAdapter.agregarMensaje(ChatMessage("¡Hola! Soy tu asistente IA de HuellApp. ¿Viste alguna mascota perdida o necesitas ayuda?", false))
        }

        ivBackChat.setOnClickListener { finish() }

        btnSendChat.setOnClickListener {
            val texto = etChatInput.text.toString().trim()
            if (texto.isNotEmpty()) {
                chatAdapter.agregarMensaje(ChatMessage(texto, true))
                etChatInput.text.clear()
                rvChat.scrollToPosition(mensajes.size - 1)

                consultarAI(texto)
            }
        }

        btnPublicarPista.setOnClickListener {
            Toast.makeText(this, "La IA publicará esta información pronto en la mascota.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun consultarAI(mensajeUsuario: String) {
        // Mostramos un mensaje de "escribiendo"
        val loadingIndex = mensajes.size
        chatAdapter.agregarMensaje(ChatMessage("Escribiendo...", false))
        rvChat.scrollToPosition(loadingIndex)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.chatearConIA(ChatRequest(mensajeUsuario))
                
                // Removemos el mensaje de cargando
                mensajes.removeAt(loadingIndex)
                chatAdapter.notifyItemRemoved(loadingIndex)

                if (response.isSuccessful && response.body() != null) {
                    val respuestaIA = response.body()!!
                    
                    chatAdapter.agregarMensaje(ChatMessage(respuestaIA.respuesta, false))
                    rvChat.scrollToPosition(mensajes.size - 1)

                    if (respuestaIA.esBorrador) {
                        cardDraftContainer.visibility = View.VISIBLE
                    }
                } else {
                    chatAdapter.agregarMensaje(ChatMessage("Lo siento, tuve un problema procesando eso. Intenta de nuevo.", false))
                    rvChat.scrollToPosition(mensajes.size - 1)
                }
            } catch (e: Exception) {
                mensajes.removeAt(loadingIndex)
                chatAdapter.notifyItemRemoved(loadingIndex)
                chatAdapter.agregarMensaje(ChatMessage("Error de conexión con el servidor IA.", false))
                rvChat.scrollToPosition(mensajes.size - 1)
            }
        }
    }
}
