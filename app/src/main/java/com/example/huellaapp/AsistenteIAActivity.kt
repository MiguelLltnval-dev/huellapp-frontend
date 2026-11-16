package com.example.huellaapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.huellaapp.adapter.ChatAdapter
import com.example.huellaapp.model.ChatMessage
import com.example.huellaapp.model.ChatRequest
import com.example.huellaapp.model.PistaBorrador
import com.example.huellaapp.model.PistaRequest
import com.example.huellaapp.network.RetrofitClient
import kotlinx.coroutines.launch
import org.json.JSONObject

class AsistenteIAActivity : AppCompatActivity() {

    private lateinit var ivBackChat: ImageView
    private lateinit var rvChat: RecyclerView
    private lateinit var etChatInput: EditText
    private lateinit var btnSendChat: ImageView
    private lateinit var cardDraftContainer: LinearLayout
    private lateinit var btnPublicarPista: Button

    private lateinit var chatAdapter: ChatAdapter
    private val mensajes = mutableListOf<ChatMessage>()
    
    private var pistaBorradorActual: PistaBorrador? = null
    private var idMascota: String? = null
    private lateinit var tvDraftDetails: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_asistente_ia)

        ivBackChat = findViewById(R.id.ivBackChat)
        rvChat = findViewById(R.id.rvChat)
        etChatInput = findViewById(R.id.etChatInput)
        btnSendChat = findViewById(R.id.btnSendChat)
        cardDraftContainer = findViewById(R.id.cardDraftContainer)
        btnPublicarPista = findViewById(R.id.btnPublicarPista)
        tvDraftDetails = findViewById(R.id.tvDraftDetails)
        
        idMascota = intent.getStringExtra("ID_MASCOTA")

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
            if (pistaBorradorActual != null && idMascota != null) {
                btnPublicarPista.isEnabled = false
                btnPublicarPista.text = "Publicando..."
                
                lifecycleScope.launch {
                    try {
                        val req = PistaRequest(
                            descripcion = pistaBorradorActual!!.descripcion ?: "",
                            ubicacion = pistaBorradorActual!!.ubicacion ?: ""
                        )
                        val res = RetrofitClient.apiService.registrarPista(idMascota!!, req)
                        if (res.isSuccessful) {
                            cardDraftContainer.visibility = View.GONE
                            Toast.makeText(this@AsistenteIAActivity, "¡Pista guardada con éxito!", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@AsistenteIAActivity, "Error al publicar", Toast.LENGTH_SHORT).show()
                            btnPublicarPista.isEnabled = true
                            btnPublicarPista.text = "Publicar Pista"
                        }
                    } catch(e: Exception) {
                        Toast.makeText(this@AsistenteIAActivity, "Error de red", Toast.LENGTH_SHORT).show()
                        btnPublicarPista.isEnabled = true
                        btnPublicarPista.text = "Publicar Pista"
                    }
                }
            } else {
                cardDraftContainer.visibility = View.GONE
                Toast.makeText(this, "La IA publicará esta información pronto en la mascota.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun consultarAI(mensajeUsuario: String) {
        // Mostramos un mensaje de "escribiendo"
        val loadingIndex = mensajes.size
        chatAdapter.agregarMensaje(ChatMessage("Escribiendo...", false))
        rvChat.scrollToPosition(loadingIndex)

        lifecycleScope.launch {
            try {
                // Aumentamos los timeouts en Retrofit para esta llamada, 
                // ya que las IA pueden tardar varios segundos en generar respuestas completas.
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
                    } else if (respuestaIA.esPista && respuestaIA.pistaGenerada != null) {
                        cardDraftContainer.visibility = View.VISIBLE
                        pistaBorradorActual = respuestaIA.pistaGenerada
                        tvDraftDetails.text = "Ubicación: ${pistaBorradorActual?.ubicacion}\nDetalles: ${pistaBorradorActual?.descripcion}"
                    }
                } else {
                    var errorMsg = response.message()
                    try {
                        val errorStr = response.errorBody()?.string()
                        if (!errorStr.isNullOrEmpty()) {
                            // Intentamos parsear el JSON de error que manda Node.js
                            val jsonObj = JSONObject(errorStr)
                            errorMsg = jsonObj.optString("error", jsonObj.optString("mensaje", errorStr))
                        }
                    } catch (e: Exception) {
                        // Fallback si no es JSON
                    }
                    
                    chatAdapter.agregarMensaje(ChatMessage("Error: $errorMsg", false))
                    rvChat.scrollToPosition(mensajes.size - 1)
                }
            } catch (e: Exception) {
                mensajes.removeAt(loadingIndex)
                chatAdapter.notifyItemRemoved(loadingIndex)
                chatAdapter.agregarMensaje(ChatMessage("Error de conexión: ${e.message}", false))
                rvChat.scrollToPosition(mensajes.size - 1)
            }
        }
    }
}
