package com.jrotero.campusgo.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jrotero.campusgo.R
import com.jrotero.campusgo.modelo.Viaje

class MisViajesAdapter(
    private val listaViajes: List<Viaje>,
    private val onVerPasajeros: (Viaje) -> Unit,
    private val onModificar: (Viaje) -> Unit,
    private val onEliminar: (Viaje) -> Unit
) : RecyclerView.Adapter<MisViajesAdapter.MiViajeViewHolder>() {

    class MiViajeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvRuta: TextView = view.findViewById(R.id.tvRutaMiViaje)
        val tvFechaHora: TextView = view.findViewById(R.id.tvFechaHoraMiViaje)
        val tvPlazas: TextView = view.findViewById(R.id.tvPlazasMiViaje)
        val btnPasajeros: Button = view.findViewById(R.id.btnVerPasajeros)
        val btnEliminar: Button = view.findViewById(R.id.btnEliminarViaje)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MiViajeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mi_viaje, parent, false)
        return MiViajeViewHolder(view)
    }

    override fun onBindViewHolder(holder: MiViajeViewHolder, position: Int) {
        val viaje = listaViajes[position]
        holder.tvRuta.text = "${viaje.origen} ➔ ${viaje.destino}"
        holder.tvFechaHora.text = "📅 ${viaje.fecha} - 🕒 ${viaje.hora}"
        holder.tvPlazas.text = "💺 Libres: ${viaje.plazasDisponibles}/${viaje.plazasTotales}"

        // Asignamos las acciones a cada botón
        holder.btnPasajeros.setOnClickListener { onVerPasajeros(viaje) }
        holder.btnEliminar.setOnClickListener { onEliminar(viaje) }
    }

    override fun getItemCount() = listaViajes.size
}