package com.jrotero.campusgo.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jrotero.campusgo.R
import com.jrotero.campusgo.modelo.Viaje
import java.util.Calendar // IMPORTANTE para obtener el día actual
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

        // Referencias a los nuevos campos de fecha y hora
        val etFecha = findViewById<EditText>(R.id.etFecha)
        val etHora = findViewById<EditText>(R.id.etHora)

        val etPlazas = findViewById<EditText>(R.id.etPlazas)
        val etPrecio = findViewById<EditText>(R.id.etPrecio)

        val btnConfirmar = findViewById<Button>(R.id.btnConfirmarViaje)
        val btnVolver = findViewById<Button>(R.id.btnVolver)

        // --- ACCIÓN: BOTÓN DE VOLVER ---
        btnVolver.setOnClickListener {
            finish()
        }

        // --- 📅 NUEVO: MOSTRAR CALENDARIO AL PULSAR LA FECHA ---
        etFecha.setOnClickListener {
            // Obtenemos la fecha de hoy para que el calendario se abra en el día actual
            val calendario = Calendar.getInstance()
            val anio = calendario.get(Calendar.YEAR)
            val mes = calendario.get(Calendar.MONTH)
            val dia = calendario.get(Calendar.DAY_OF_MONTH)

            // Creamos el diálogo del calendario
            val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
                // Cuando el usuario elige una fecha, la escribimos en el EditText
                // Nota: se suma 1 al mes porque en Java/Kotlin los meses van de 0 a 11
                val fechaSeleccionada = "$dayOfMonth/${month + 1}/$year"
                etFecha.setText(fechaSeleccionada)
            }, anio, mes, dia)

            // Opcional: Impedir que se seleccionen fechas del pasado
            datePickerDialog.datePicker.minDate = calendario.timeInMillis

            datePickerDialog.show()
        }

        // --- 🕒 NUEVO: MOSTRAR RELOJ AL PULSAR LA HORA ---
        etHora.setOnClickListener {
            // Obtenemos la hora actual
            val calendario = Calendar.getInstance()
            val horaActual = calendario.get(Calendar.HOUR_OF_DAY)
            val minutoActual = calendario.get(Calendar.MINUTE)

            // Creamos el diálogo del reloj
            val timePickerDialog = TimePickerDialog(this, { _, hourOfDay, minute ->
                // Formateamos para que siempre tenga 2 dígitos (ej: 08:05 en lugar de 8:5)
                val horaFormateada = String.format("%02d:%02d", hourOfDay, minute)
                etHora.setText(horaFormateada)
            }, horaActual, minutoActual, true) // true = formato 24 horas

            timePickerDialog.show()
        }

        // --- ACCIÓN: BOTÓN DE CONFIRMAR VIAJE ---
        btnConfirmar.setOnClickListener {
            val origen = etOrigen.text.toString()
            val destino = etDestino.text.toString()
            val fecha = etFecha.text.toString() // Sacamos la fecha real elegida
            val hora = etHora.text.toString()
            val plazas = etPlazas.text.toString().toIntOrNull() ?: 0
            val precioFinal = etPrecio.text.toString().toDoubleOrNull() ?: 0.0

            val userId = auth.currentUser?.uid ?: ""

            // Añadimos la fecha a la validación
            if (origen.isNotEmpty() && destino.isNotEmpty() && fecha.isNotEmpty() && hora.isNotEmpty() && plazas > 0) {

                val idViaje = UUID.randomUUID().toString()

                val nuevoViaje = Viaje(
                    idViaje = idViaje,
                    conductorId = userId,
                    origen = origen,
                    destino = destino,
                    fecha = fecha, // --> AHORA USAMOS LA FECHA REAL <--
                    hora = hora,
                    plazasTotales = plazas,
                    plazasDisponibles = plazas,
                    precio = precioFinal,
                    estado = "activo"
                )

                db.collection("viajes").document(idViaje).set(nuevoViaje)
                    .addOnSuccessListener {
                        Toast.makeText(this, "¡Viaje publicado con éxito!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error al publicar: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Por favor, completa todos los campos correctamente", Toast.LENGTH_SHORT).show()
            }
        }
    }
}