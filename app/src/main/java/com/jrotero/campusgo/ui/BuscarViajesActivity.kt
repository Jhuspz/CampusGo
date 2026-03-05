package com.jrotero.campusgo.ui

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog // ¡NUEVA IMPORTACIÓN!
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jrotero.campusgo.R
import com.jrotero.campusgo.modelo.Reserva
import com.jrotero.campusgo.modelo.Viaje

class BuscarViajesActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var rvViajes: RecyclerView
    private lateinit var viajeAdapter: ViajeAdapter
    private var listaViajes = mutableListOf<Viaje>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buscar_viajes)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val btnVolver = findViewById<Button>(R.id.btnVolverHome)
        btnVolver.setOnClickListener { finish() }

        rvViajes = findViewById(R.id.rvViajesDisponibles)
        rvViajes.layoutManager = LinearLayoutManager(this)

        // CAMBIO: Ahora al hacer clic, primero verificamos si ya tiene reserva
        viajeAdapter = ViajeAdapter(listaViajes) { viajeSeleccionado ->
            verificarReservaPrevia(viajeSeleccionado)
        }
        rvViajes.adapter = viajeAdapter

        consultarViajesActivos()
    }

    private fun consultarViajesActivos() {
        db.collection("viajes")
            .whereEqualTo("estado", "activo")
            .get()
            .addOnSuccessListener { resultado ->
                listaViajes.clear()
                for (documento in resultado) {
                    val viaje = documento.toObject(Viaje::class.java)
                    if (viaje.plazasDisponibles > 0) {
                        listaViajes.add(viaje)
                    }
                }
                viajeAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar viajes: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // --- PASO 1: VERIFICAR SI YA ESTÁ APUNTADO ---
    private fun verificarReservaPrevia(viaje: Viaje) {
        val userId = auth.currentUser?.uid ?: return

        // Evitamos que el conductor se reserve a sí mismo
        if (viaje.conductorId == userId) {
            Toast.makeText(this, "No puedes reservar en tu propio viaje", Toast.LENGTH_SHORT).show()
            return
        }

        // Consultamos en la colección de reservas si ya existe una con este viaje y este usuario
        db.collection("reservas")
            .whereEqualTo("idViaje", viaje.idViaje)
            .whereEqualTo("idPasajero", userId)
            .get()
            .addOnSuccessListener { documentos ->
                if (!documentos.isEmpty) {
                    // Ya hay al menos un documento, significa que ya reservó
                    Toast.makeText(this, "Ya tienes una plaza confirmada en este viaje", Toast.LENGTH_LONG).show()
                } else {
                    // No hay reservas previas, pasamos a preguntar
                    mostrarDialogoConfirmacion(viaje)
                }
            }
    }

    // --- PASO 2: MOSTRAR EL POPUP DE CONFIRMACIÓN ---
    private fun mostrarDialogoConfirmacion(viaje: Viaje) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmar Reserva")
        builder.setMessage("¿Quieres reservar una plaza para ir de ${viaje.origen} a ${viaje.destino}?\n\n" +
                "📅 Fecha: ${viaje.fecha}\n" +
                "🕒 Hora: ${viaje.hora}\n" +
                "💶 Precio: ${viaje.precio}€")

        // Botón verde / aceptar
        builder.setPositiveButton("Sí, reservar") { _, _ ->
            ejecutarReservaSegura(viaje)
        }

        // Botón rojo / cancelar
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss() // Solo cierra la ventanita
        }

        // Mostramos el diálogo en pantalla
        builder.show()
    }

    // --- PASO 3: GUARDAR EN LA BASE DE DATOS ---
    private fun ejecutarReservaSegura(viaje: Viaje) {
        val userId = auth.currentUser?.uid ?: return

        val idReserva = db.collection("reservas").document().id
        val nuevaReserva = Reserva(
            idReserva = idReserva,
            idViaje = viaje.idViaje,
            idPasajero = userId,
            timestamp = System.currentTimeMillis(),
            estado = "confirmada"
        )

        db.runTransaction { transaction ->
            val viajeRef = db.collection("viajes").document(viaje.idViaje)
            val snapshot = transaction.get(viajeRef)
            val plazasActuales = snapshot.getLong("plazasDisponibles") ?: 0

            if (plazasActuales > 0) {
                transaction.update(viajeRef, "plazasDisponibles", plazasActuales - 1)
                val reservaRef = db.collection("reservas").document(idReserva)
                transaction.set(reservaRef, nuevaReserva)
            } else {
                throw Exception("Ya no quedan plazas")
            }
        }.addOnSuccessListener {
            Toast.makeText(this, "¡Plaza reservada con éxito!", Toast.LENGTH_SHORT).show()
            consultarViajesActivos()
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}