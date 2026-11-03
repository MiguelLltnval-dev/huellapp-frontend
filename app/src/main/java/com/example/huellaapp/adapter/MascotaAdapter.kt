package com.example.huellaapp.adapter

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.huellaapp.DetalleMascotaActivity
import com.example.huellaapp.R
import com.example.huellaapp.model.Mascota

class MascotaAdapter(private var listaMascotas: List<Mascota>) :
    RecyclerView.Adapter<MascotaAdapter.MascotaViewHolder>() {

    class MascotaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombreMascota)
        val tvEspecie: TextView = itemView.findViewById(R.id.tvEspecieMascota)
        val tvUbicacion: TextView = itemView.findViewById(R.id.tvUbicacionMascota)
        val tvEstado: TextView = itemView.findViewById(R.id.tvEstado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MascotaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mascota, parent, false)
        return MascotaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MascotaViewHolder, position: Int) {
        val mascota = listaMascotas[position]
        holder.tvNombre.text = mascota.nombre
        holder.tvEspecie.text = "${mascota.especie} • ${mascota.color}"
        holder.tvUbicacion.text = mascota.ciudad

        // Lógica visual del Estado (Insignia)
        if (mascota.estado == "EN_CASA") {
            holder.tvEstado.text = "✓ EN CASA"
            holder.tvEstado.setTextColor(Color.parseColor("#4CAF50"))
            holder.tvEstado.setBackgroundResource(R.drawable.bg_badge_en_casa)
        } else {
            holder.tvEstado.text = "• PERDIDO"
            holder.tvEstado.setTextColor(Color.parseColor("#F44336"))
            holder.tvEstado.setBackgroundResource(R.drawable.bg_badge_perdido)
        }

        // Navegación al detalle
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, DetalleMascotaActivity::class.java).apply {
                putExtra("nombre", mascota.nombre)
                putExtra("especie", mascota.especie)
                putExtra("color", mascota.color)
                putExtra("ciudad", mascota.ciudad)
                putExtra("estado", mascota.estado)
                putExtra("fotoUrl", mascota.fotoUrl)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = listaMascotas.size

    fun actualizarLista(nuevaLista: List<Mascota>) {
        listaMascotas = nuevaLista
        notifyDataSetChanged()
    }
}
