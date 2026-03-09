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
import com.google.firebase.firestore.FirebaseFirestore
import com.jrotero.campusgo.R
import com.jrotero.campusgo.modelo.Viaje

class MisReservasActivity : AppCompatActivity() {

    // 1. Variables de Firebase y de la interfaz
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var rvReservas: RecyclerView
    private lateinit var viajeAdapter: ViajeAdapter
    private lateinit var tvSinReservas: TextView

    // Lista para guardar los viajes que mostraremos
    private var listaViajes = mutableListOf<Viaje>()

    // Un "Mapa" (Diccionario) para conectar el ID del Viaje con el ID de la Reserva.
    // Lo necesitamos para saber qué reserva borrar de la base de datos si el usuario cancela.
    private var mapaReservas = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_reservas)

        // 2. Inicializamos Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // 3. Vinculamos la interfaz
        tvSinReservas = findViewById(R.id.tvSinReservas)
        val btnVolver = findViewById<Button>(R.id.btnVolverHome)

        // Acción de volver atrás
        btnVolver.setOnClickListener { finish() }

        // 4. Configuramos el RecyclerView (la lista visual)
        rvReservas = findViewById(R.id.rvMisReservas)
        rvReservas.layoutManager = LinearLayoutManager(this)

        // 5. Inicializamos el adaptador.
        // LÓGICA DE CLIC: Si tocan una tarjeta, abrimos el diálogo para CANCELAR
        viajeAdapter = ViajeAdapter(listaViajes) { viajeSeleccionado ->
            mostrarDialogoCancelacion(viajeSeleccionado)
        }
        rvReservas.adapter = viajeAdapter

        // 6. Cargamos los datos desde Firestore
        cargarMisReservas()
    }

    /**
     * Función que busca en Firestore las reservas del usuario y luego descarga los viajes.
     */
    private fun cargarMisReservas() {
        val userId = auth.currentUser?.uid ?: return

        // PASO A: Buscamos todas las reservas donde 'idPasajero' sea mi ID
        db.collection("reservas")
            .whereEqualTo("idPasajero", userId)
            .get()
            .addOnSuccessListener { reservasSnapshot ->
                // Limpiamos la lista por si estamos recargando
                listaViajes.clear()
                mapaReservas.clear()

                // Si no hay ninguna reserva en Firestore...
                if (reservasSnapshot.isEmpty) {
                    tvSinReservas.visibility = View.VISIBLE // Mostramos el mensaje triste
                    viajeAdapter.notifyDataSetChanged() // Refrescamos la lista (que estará vacía)
                    return@addOnSuccessListener
                } else {
                    tvSinReservas.visibility = View.GONE // Ocultamos el mensaje triste
                }

                // PASO B: Si hay reservas, recorremos una a una
                for (documento in reservasSnapshot) {
                    val idViaje = documento.getString("idViaje") ?: continue
                    val idReserva = documento.id

                    // Guardamos el ID de la reserva asociado a este viaje
                    mapaReservas[idViaje] = idReserva

                    // PASO C: Descargamos la información del Viaje concreto usando su ID
                    db.collection("viajes").document(idViaje).get()
                        .addOnSuccessListener { viajeSnapshot ->
                            val viaje = viajeSnapshot.toObject(Viaje::class.java)
                            if (viaje != null) {
                                // Añadimos el viaje a la lista y avisamos al adaptador para que lo pinte
                                listaViajes.add(viaje)
                                viajeAdapter.notifyDataSetChanged()
                            }
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar reservas", Toast.LENGTH_SHORT).show()
            }
    }

    // --- LÓGICA DE CANCELACIÓN ---

    /**
     * Muestra un Popup (Diálogo) preguntando si realmente quiere cancelar.
     */
    private fun mostrarDialogoCancelacion(viaje: Viaje) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Cancelar Reserva")
        builder.setMessage("¿Estás seguro de que quieres cancelar tu plaza en el viaje de ${viaje.origen} a ${viaje.destino}?")

        // Si dice que SÍ
        builder.setPositiveButton("Sí, cancelar") { _, _ ->
            cancelarReservaSegura(viaje)
        }

        // Si dice que NO
        builder.setNegativeButton("No, mantener") { dialog, _ ->
            dialog.dismiss() // Cierra el mensaje y no hace nada
        }

        builder.show()
    }

    /**
     * Función que usa una "Transacción" segura de Firebase para devolver la plaza al conductor
     * y borrar el ticket de reserva.
     */
    private fun cancelarReservaSegura(viaje: Viaje) {
        // Obtenemos el ID de la reserva que guardamos antes en el mapa
        val idReserva = mapaReservas[viaje.idViaje] ?: return

        db.runTransaction { transaction ->
            // Referencias a los documentos que vamos a tocar
            val viajeRef = db.collection("viajes").document(viaje.idViaje)
            val reservaRef = db.collection("reservas").document(idReserva)

            // Leemos cuántas plazas disponibles tiene el viaje actualmente
            val snapshot = transaction.get(viajeRef)
            val plazasActuales = snapshot.getLong("plazasDisponibles") ?: 0

            // 1. Sumamos 1 plaza de vuelta al viaje
            transaction.update(viajeRef, "plazasDisponibles", plazasActuales + 1)

            // 2. Borramos la reserva de la base de datos
            transaction.delete(reservaRef)

        }.addOnSuccessListener {
            Toast.makeText(this, "Reserva cancelada correctamente", Toast.LENGTH_SHORT).show()
            // Volvemos a descargar las reservas para que el viaje desaparezca de la pantalla
            cargarMisReservas()
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Error al cancelar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}