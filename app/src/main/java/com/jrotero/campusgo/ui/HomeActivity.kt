package com.jrotero.campusgo.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jrotero.campusgo.R
import com.jrotero.campusgo.modelo.Viaje

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var tvBienvenida: TextView
    private lateinit var btnIrAPublicar: Button
    private lateinit var btnCerrarSesion: Button

    // --- NUEVO PARA EL RECYCLERVIEW ---
    private lateinit var rvViajes: RecyclerView
    private lateinit var viajeAdapter: ViajeAdapter
    private var listaViajes = mutableListOf<Viaje>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        tvBienvenida = findViewById(R.id.tvBienvenida)
        btnIrAPublicar = findViewById(R.id.btnIrAPublicar)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)

        // Configurar RecyclerView
        rvViajes = findViewById(R.id.rvViajes)
        rvViajes.layoutManager = LinearLayoutManager(this)

        // Inicializamos el adaptador con nuestra lista vacía
        viajeAdapter = ViajeAdapter(listaViajes) { viaje ->
            confirmarReserva(viaje)
        }
        rvViajes.adapter = viajeAdapter

        obtenerDatosUsuario()
        consultarViajes() // <-- Llamamos a la carga de datos

        btnIrAPublicar.setOnClickListener {
            startActivity(Intent(this, PublicarViajeActivity::class.java))
        }

        btnCerrarSesion.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun obtenerDatosUsuario() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("usuarios").document(userId).get()
                .addOnSuccessListener { documento ->
                    if (documento.exists()) {
                        val nombre = documento.getString("nombre") ?: "Usuario"
                        val esConductor = documento.getBoolean("esConductor") ?: false
                        tvBienvenida.text = "¡Hola, $nombre!"
                        btnIrAPublicar.visibility = if (esConductor) View.VISIBLE else View.GONE
                    }
                }
        }
    }

    // --- NUEVA FUNCIÓN: Traer viajes de Firestore ---
    private fun consultarViajes() {
        // Consultamos viajes activos y con plazas disponibles
        db.collection("viajes")
            .whereEqualTo("estado", "activo")
            .get()
            .addOnSuccessListener { resultado ->
                listaViajes.clear() // Limpiamos para evitar duplicados
                for (documento in resultado) {
                    val viaje = documento.toObject(Viaje::class.java)
                    listaViajes.add(viaje)
                }
                // Avisamos al adaptador que ya hay datos nuevos
                viajeAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar viajes: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Y añade esta función al final de la clase:
    private fun confirmarReserva(viaje: Viaje) {
        val userId = auth.currentUser?.uid ?: return

        if (viaje.plazasDisponibles > 0) {
            // 1. Crear el objeto Reserva
            val idReserva = db.collection("reservas").document().id
            val nuevaReserva = com.jrotero.campusgo.modelo.Reserva(
                idReserva = idReserva,
                idViaje = viaje.idViaje,
                idPasajero = userId,
                timestamp = System.currentTimeMillis()
            )

            // 2. Guardar reserva y actualizar plazas en una "Transacción" (para que sea seguro)
            db.runTransaction { transaction ->
                val viajeRef = db.collection("viajes").document(viaje.idViaje)

                // Restamos una plaza
                transaction.update(viajeRef, "plazasDisponibles", viaje.plazasDisponibles - 1)

                // Guardamos la reserva
                val reservaRef = db.collection("reservas").document(idReserva)
                transaction.set(reservaRef, nuevaReserva)
            }.addOnSuccessListener {
                Toast.makeText(this, "¡Reserva realizada con éxito!", Toast.LENGTH_SHORT).show()
                consultarViajes() // Refrescamos la lista
            }.addOnFailureListener {
                Toast.makeText(this, "Error al reservar", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No quedan plazas", Toast.LENGTH_SHORT).show()
        }
    }

    // Opcional: Recargar la lista al volver a esta pantalla
    override fun onResume() {
        super.onResume()
        consultarViajes()
    }
}