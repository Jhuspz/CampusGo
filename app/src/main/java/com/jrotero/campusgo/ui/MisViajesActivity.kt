package com.jrotero.campusgo.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.jrotero.campusgo.R
import com.jrotero.campusgo.modelo.Viaje

class MisViajesActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var rvMisViajes: RecyclerView

    // ATENCIÓN: Ahora usamos MisViajesAdapter
    private lateinit var misViajesAdapter: MisViajesAdapter
    private lateinit var tvSinViajes: TextView
    private var listaViajes = mutableListOf<Viaje>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_viajes)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        tvSinViajes = findViewById(R.id.tvSinViajes)
        findViewById<Button>(R.id.btnVolverHome).setOnClickListener { finish() }

        rvMisViajes = findViewById(R.id.rvMisViajes)
        rvMisViajes.layoutManager = LinearLayoutManager(this)

        // CONFIGURAMOS LOS BOTONES DEL ADAPTADOR
        misViajesAdapter = MisViajesAdapter(
            listaViajes,
            onVerPasajeros = { viaje -> verPasajeros(viaje) },
            onEliminar = { viaje -> mostrarDialogoEliminar(viaje) }
        )
        rvMisViajes.adapter = misViajesAdapter

        cargarMisViajesPublicados()
    }

    private fun cargarMisViajesPublicados() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("viajes").whereEqualTo("conductorId", userId).whereEqualTo("estado", "activo").get()
            .addOnSuccessListener { resultado ->
                listaViajes.clear()
                if (resultado.isEmpty) tvSinViajes.visibility = View.VISIBLE else tvSinViajes.visibility = View.GONE
                for (documento in resultado) listaViajes.add(documento.toObject(Viaje::class.java))
                misViajesAdapter.notifyDataSetChanged()
            }
    }

    // --- ACCIÓN 1: VER PASAJEROS ---
    private fun verPasajeros(viaje: Viaje) {
        // 1. Buscamos las reservas para este viaje
        db.collection("reservas").whereEqualTo("idViaje", viaje.idViaje).get()
            .addOnSuccessListener { reservas ->
                if (reservas.isEmpty) {
                    Toast.makeText(this, "Aún no hay pasajeros apuntados", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // 2. Extraemos los IDs de los pasajeros
                val idsPasajeros = reservas.documents.mapNotNull { it.getString("idPasajero") }

                // 3. Buscamos los nombres de esos usuarios en la colección 'usuarios'
                db.collection("usuarios").whereIn(FieldPath.documentId(), idsPasajeros).get()
                    .addOnSuccessListener { usuarios ->
                        val nombres = usuarios.documents.mapNotNull { it.getString("nombre") }

                        // 4. Mostramos los nombres en un Dialog
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("Pasajeros Confirmados")
                        builder.setItems(nombres.toTypedArray(), null)
                        builder.setPositiveButton("Cerrar") { dialog, _ -> dialog.dismiss() }
                        builder.show()
                    }
            }
    }

    // --- ACCIÓN ELIMINAR ---
    private fun mostrarDialogoEliminar(viaje: Viaje) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Eliminar Viaje")
        builder.setMessage("¿Estás seguro de que quieres cancelar este viaje?\nLos pasajeros serán avisados.")
        builder.setPositiveButton("Sí, eliminar") { _, _ ->
            db.collection("viajes").document(viaje.idViaje).delete().addOnSuccessListener {
                Toast.makeText(this, "Viaje eliminado", Toast.LENGTH_SHORT).show()
                cargarMisViajesPublicados()
            }
        }
        builder.setNegativeButton("No", null)
        builder.show()
    }
}