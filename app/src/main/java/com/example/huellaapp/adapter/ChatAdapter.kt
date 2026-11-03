package com.example.huellaapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.huellaapp.R
import com.example.huellaapp.model.ChatMessage

class ChatAdapter(private val listaMensajes: MutableList<ChatMessage>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val llAiMessage: LinearLayout = itemView.findViewById(R.id.llAiMessage)
        val tvAiText: TextView = itemView.findViewById(R.id.tvAiText)
        
        val llUserMessage: LinearLayout = itemView.findViewById(R.id.llUserMessage)
        val tvUserText: TextView = itemView.findViewById(R.id.tvUserText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val mensaje = listaMensajes[position]
        
        if (mensaje.esUsuario) {
            holder.llUserMessage.visibility = View.VISIBLE
            holder.llAiMessage.visibility = View.GONE
            holder.tvUserText.text = mensaje.texto
        } else {
            holder.llAiMessage.visibility = View.VISIBLE
            holder.llUserMessage.visibility = View.GONE
            holder.tvAiText.text = mensaje.texto
        }
    }

    override fun getItemCount(): Int = listaMensajes.size

    fun agregarMensaje(mensaje: ChatMessage) {
        listaMensajes.add(mensaje)
        notifyItemInserted(listaMensajes.size - 1)
    }
}
