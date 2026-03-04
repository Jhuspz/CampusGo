package com.jrotero.campusgo.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jrotero.campusgo.R
import com.jrotero.campusgo.modelo.Viaje
import java.util.UUID

class PublicarViajeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publicar_viaje)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etOrigen = findViewById<EditText>(R.id.etOrigen)
        val etDestino = findViewById<EditText>(R.id.etDestino)
        val etHora = findViewById<EditText>(R.id.etHora)
        val etPlazas = findViewById<EditText>(R.id.etPlazas)
        val btnConfirmar = findViewById<Button>(R.id.btnConfirmarViaje)

        btnConfirmar.setOnClickListener {
            val origen = etOrigen.text.toString()
            val destino = etDestino.text.toString()
            val hora = etHora.text.toString()
            val plazas = etPlazas.text.toString().toIntOrNull() ?: 0
            val userId = auth.currentUser?.uid ?: ""

            if (origen.isNotEmpty() && destino.isNotEmpty() && plazas > 0) {
                // Generamos un ID único para el viaje
                val idViaje = UUID.randomUUID().toString()

                // Creamos el objeto usando TU modelo
                val nuevoViaje = Viaje(
                    idViaje = idViaje,
                    conductorId = userId,
                    origen = origen,
                    destino = destino,
                    fecha = "Hoy", // Por ahora simplificado
                    hora = hora,
                    plazasTotales = plazas,
                    plazasDisponibles = plazas,
                    precio = 0.0,
                    estado = "activo"
                )

                // Guardamos en una nueva colección llamada "viajes"
                db.collection("viajes").document(idViaje).set(nuevoViaje)
                    .addOnSuccessListener {
                        Toast.makeText(this, "¡Viaje publicado!", Toast.LENGTH_SHORT).show()
                        finish() // Volvemos al Home
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Completa los campos correctamente", Toast.LENGTH_SHORT).show()
            }
        }
    }
}