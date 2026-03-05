package com.jrotero.campusgo.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jrotero.campusgo.R
import com.jrotero.campusgo.modelo.Viaje

class ViajeAdapter(
    private val listaViajes: List<Viaje>,
    private val onItemClick: (Viaje) -> Unit
) : RecyclerView.Adapter<ViajeAdapter.ViajeViewHolder>() {

    class ViajeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRuta: TextView = view.findViewById(R.id.tvRuta)
        // --- NUEVO: Vinculamos el texto de la fecha ---
        val tvFecha: TextView = view.findViewById(R.id.tvFecha)
        val tvHora: TextView = view.findViewById(R.id.tvHora)
        val tvPlazas: TextView = view.findViewById(R.id.tvPlazas)
        val tvPrecio: TextView = view.findViewById(R.id.tvPrecio)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViajeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_viaje, parent, false)
        return ViajeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViajeViewHolder, position: Int) {
        val viaje = listaViajes[position]
        holder.tvRuta.text = "${viaje.origen} ➔ ${viaje.destino}"
        // --- NUEVO: Pasamos el dato de la fecha a la tarjeta ---
        holder.tvFecha.text = "📅 ${viaje.fecha}"
        holder.tvHora.text = "🕒 ${viaje.hora}"
        holder.tvPlazas.text = "💺 ${viaje.plazasDisponibles} plazas"
        holder.tvPrecio.text = "${viaje.precio}€"

        // EVENTO DE CLIC
        holder.itemView.setOnClickListener {
            onItemClick(viaje)
        }
    }

    override fun getItemCount() = listaViajes.size
}