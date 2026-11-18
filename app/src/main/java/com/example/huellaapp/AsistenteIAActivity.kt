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
import com.example.huellaapp.model.MascotaCoincidencia
import com.example.huellaapp.model.PistaBorrador
import com.example.huellaapp.model.PistaRequest
import com.example.huellaapp.network.RetrofitClient
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import org.json.JSONObject
import com.google.gson.Gson
import com.example.huellaapp.model.listaParaPublicar
import android.widget.ScrollView
import androidx.appcompat.app.AlertDialog

class AsistenteIAActivity : ResponsiveActivity() {

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
    private var abriendoDetalle = false
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
        tvDraftDetails.movementMethod = android.text.method.ScrollingMovementMethod()
        findViewById<Button>(R.id.btnEditarPista).setOnClickListener { editarPista() }

        idMascota = intent.getStringExtra("ID_MASCOTA")

        chatAdapter = ChatAdapter(mensajes) { mensaje ->
            if (mensaje.resultado != null) {
                abrirDetalle(mensaje.resultado, mensaje.textoAvistamiento)
            } else if (mensaje.abrirReporte) {
                startActivity(CrearMascotaActivity.crearIntent(this, mensaje.borrador))
            }
        }
        rvChat.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        rvChat.adapter = chatAdapter

        val contextoMascota = intent.getStringExtra("CONTEXTO_MASCOTA")
        if (savedInstanceState == null) etChatInput.setText(intent.getStringExtra("TEXTO_AVISTAMIENTO"))
        if (!contextoMascota.isNullOrEmpty()) {
            chatAdapter.agregarMensaje(ChatMessage("¡Hola! Veo que quieres reportar una pista sobre $contextoMascota. Dime, ¿dónde lo viste o qué detalles recuerdas?", false))
        } else {
            chatAdapter.agregarMensaje(ChatMessage("¡Hola! Si se perdió tu mascota, dime cómo es y dónde se perdió. Buscaré reportes parecidos en HuellApp.", false))
        }

        savedInstanceState?.getString("chat")?.let {
            mensajes.clear()
            mensajes.addAll(Gson().fromJson(it, Array<ChatMessage>::class.java).toList())
            chatAdapter.notifyDataSetChanged()
        }
        savedInstanceState?.getString("pista")?.let {
            pistaBorradorActual = Gson().fromJson(it, PistaBorrador::class.java)
            mostrarBorrador()
        }
        ivBackChat.setOnClickListener { finish() }

        btnSendChat.setOnClickListener {
            val texto = etChatInput.text.toString().trim()
            if (texto.isNotEmpty()) {
                chatAdapter.agregarMensaje(ChatMessage(texto, true))
                etChatInput.text.clear()
                desplazarChat(mensajes.size - 1)

                consultarAI(texto)
            }
        }

        btnPublicarPista.setOnClickListener {
            if (pistaBorradorActual != null && idMascota != null) {
                if (!btnSendChat.isEnabled || pistaBorradorActual?.listaParaPublicar() != true) return@setOnClickListener
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
                            val aviso = if (res.code() == 409) "La mascota ya está en casa." else "Error al publicar"
                            Toast.makeText(this@AsistenteIAActivity, aviso, Toast.LENGTH_SHORT).show()
                            btnPublicarPista.isEnabled = true
                            btnPublicarPista.text = "Publicar Pista"
                        }
                    } catch(e: CancellationException) {
                        throw e
                    } catch(e: Exception) {
                        Toast.makeText(this@AsistenteIAActivity, "Error de red", Toast.LENGTH_SHORT).show()
                        btnPublicarPista.isEnabled = true
                        btnPublicarPista.text = "Publicar Pista"
                    }
                }
            }
        }
    }

    private fun abrirDetalle(resultado: MascotaCoincidencia, avistamiento: String?) {
        if (abriendoDetalle) return
        abriendoDetalle = true
        lifecycleScope.launch {
            try {
                val respuesta = RetrofitClient.apiService.obtenerMascota(resultado.id)
                val mascota = respuesta.body()
                if (respuesta.isSuccessful && mascota != null) {
                    startActivity(DetalleMascotaActivity.crearIntent(this@AsistenteIAActivity, mascota).apply {
                        putExtra("TEXTO_AVISTAMIENTO", avistamiento)
                    })
                    return@launch
                }
                Toast.makeText(this@AsistenteIAActivity, "No se pudo abrir el reporte.", Toast.LENGTH_SHORT).show()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Toast.makeText(this@AsistenteIAActivity, "Error de conexión al abrir el reporte.", Toast.LENGTH_SHORT).show()
            } finally {
                abriendoDetalle = false
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("chat", Gson().toJson(mensajes.filter { it.texto != "Escribiendo..." }))
        outState.putString("pista", pistaBorradorActual?.let { Gson().toJson(it) })
        super.onSaveInstanceState(outState)
    }

    private fun mostrarBorrador() {
        val pista = pistaBorradorActual
        cardDraftContainer.visibility = if (pista == null) View.GONE else View.VISIBLE
        tvDraftDetails.text = "Ubicación: ${pista?.ubicacion.orEmpty()}\nDetalles: ${pista?.descripcion.orEmpty()}"
        btnPublicarPista.isEnabled = pista?.listaParaPublicar() == true && btnSendChat.isEnabled
    }

    private fun editarPista() {
        val pista = pistaBorradorActual ?: return
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 8, 24, 8)
        }
        val ubicacion = EditText(this).apply { hint = "Ciudad, barrio o lugar"; setText(pista.ubicacion); maxLines = 3 }
        val descripcion = EditText(this).apply { hint = "Detalles del avistamiento"; setText(pista.descripcion); minLines = 2; maxLines = 5 }
        box.addView(ubicacion)
        box.addView(descripcion)
        val dialog = AlertDialog.Builder(this).setTitle("Revisar pista antes de publicar")
            .setView(ScrollView(this).apply { addView(box) })
            .setNegativeButton("Cancelar", null).setPositiveButton("Guardar cambios", null).create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val nueva = PistaBorrador(descripcion.text.toString().trim(), ubicacion.text.toString().trim())
                if (!nueva.listaParaPublicar()) {
                    ubicacion.error = if (nueva.ubicacion.isNullOrBlank()) "Indica dónde ocurrió" else null
                    descripcion.error = if (nueva.descripcion.isNullOrBlank()) "Describe lo que viste" else null
                } else {
                    pistaBorradorActual = nueva
                    mostrarBorrador()
                    dialog.dismiss()
                }
            }
        }
        dialog.show()
    }

    override fun onViewportChanged(heightDp: Int, keyboardVisible: Boolean) {
        findViewById<View>(R.id.headerChat)?.visibility =
            if (keyboardVisible && heightDp < 240) View.GONE else View.VISIBLE
    }

    private fun desplazarChat(position: Int) {
        rvChat.scrollToPosition(position)
        findViewById<androidx.core.widget.NestedScrollView>(R.id.scrollChat).post {
            findViewById<androidx.core.widget.NestedScrollView>(R.id.scrollChat).fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun consultarAI(mensajeUsuario: String) {
        btnSendChat.isEnabled = false
        btnPublicarPista.isEnabled = false
        findViewById<Button>(R.id.btnEditarPista).isEnabled = false
        val loadingIndex = mensajes.size
        chatAdapter.agregarMensaje(ChatMessage("Escribiendo...", false))
        desplazarChat(loadingIndex)

        lifecycleScope.launch {
            val anteriores = mensajes.subList(0, (loadingIndex - 1).coerceAtLeast(0))
                .filter { it.esUsuario }.takeLast(3).joinToString("\n") { it.texto }.takeLast(750)
            val contexto = listOfNotNull(
                intent.getStringExtra("CONTEXTO_MASCOTA")?.let { "Reporte abierto: $it" },
                anteriores.takeIf { it.isNotBlank() }?.let { "Mensajes anteriores del usuario:\n$it" }
            ).joinToString("\n")
            val request = ChatRequest(
                mensajeUsuario,
                contexto,
                modoPista = idMascota != null,
                pistaActual = pistaBorradorActual
            )
            val response = try {
                RetrofitClient.apiService.chatearConIA(request)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            } finally {
                mensajes.removeAt(loadingIndex)
                chatAdapter.notifyItemRemoved(loadingIndex)
                btnSendChat.isEnabled = true
                findViewById<Button>(R.id.btnEditarPista).isEnabled = true
                mostrarBorrador()
            }
            val respuestaIA = response?.body()
            if (respuestaIA != null) {
                chatAdapter.agregarMensaje(ChatMessage(respuestaIA.respuesta, false))
                for (resultado in respuestaIA.coincidencias.orEmpty()) {
                    val atributos = listOf(resultado.especie, resultado.raza, resultado.color, resultado.ciudad)
                        .filterNot { it.isNullOrBlank() }.joinToString(" · ")
                    chatAdapter.agregarMensaje(ChatMessage(
                        "${resultado.nombre}\n$atributos\nVer reporte", false, resultado = resultado,
                        textoAvistamiento = mensajeUsuario.takeIf { respuestaIA.avistamiento }
                    ))
                }
                if (respuestaIA.puedeReportar) {
                    chatAdapter.agregarMensaje(ChatMessage(
                        "Crear reporte de mascota perdida", false,
                        abrirReporte = true, borrador = respuestaIA.mascotaGenerada
                    ))
                }
                val pista = respuestaIA.pistaGenerada
                if (idMascota != null) {
                    pistaBorradorActual = pista?.takeIf {
                        !it.descripcion.isNullOrBlank() || !it.ubicacion.isNullOrBlank()
                    }
                    mostrarBorrador()
                }
            } else {
                val error = runCatching {
                    response?.errorBody()?.string()?.let { JSONObject(it).optString("error") }
                }.getOrNull()?.takeIf { it.isNotBlank() }
                chatAdapter.agregarMensaje(ChatMessage(error ?: "No se pudo consultar al asistente. Intenta de nuevo.", false))
            }
            desplazarChat(if (respuestaIA?.coincidencias != null) loadingIndex else mensajes.lastIndex)
        }
    }
}
