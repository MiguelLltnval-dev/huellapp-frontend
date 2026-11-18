package com.example.huellaapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.huellaapp.R
import com.example.huellaapp.model.Pista

class PistaAdapter(private var pistas: List<Pista>) : RecyclerView.Adapter<PistaAdapter.PistaViewHolder>() {

    class PistaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvPistaDesc: TextView = view.findViewById(R.id.tvPistaDesc)
        val tvPistaUbi: TextView = view.findViewById(R.id.tvPistaUbi)
        val tvPistaUser: TextView = view.findViewById(R.id.tvPistaUser)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PistaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pista, parent, false)
        return PistaViewHolder(view)
    }

    override fun onBindViewHolder(holder: PistaViewHolder, position: Int) {
        val pista = pistas[position]
        holder.tvPistaDesc.text = pista.descripcion
        holder.tvPistaUbi.text = "📍 ${pista.ubicacion}"
        holder.tvPistaUser.text = "Aportado por: ${pista.usuarioId?.nombre ?: "Usuario"}"
    }

    override fun getItemCount() = pistas.size

    fun actualizarPistas(nuevas: List<Pista>) {
        if (pistas == nuevas) return
        pistas = nuevas
        notifyDataSetChanged()
    }
}
