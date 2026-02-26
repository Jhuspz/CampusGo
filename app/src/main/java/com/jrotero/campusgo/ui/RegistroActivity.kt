package com.jrotero.campusgo.ui // Revisa que coincida con tu carpeta

import android.os.Bundle
import android.view.View // Importante para la visibilidad
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jrotero.campusgo.R
import com.jrotero.campusgo.modelo.Usuario

class RegistroActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        // 1. Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // 2. Referencias a la interfaz
        val etNombre = findViewById<EditText>(R.id.etNombreRegistro)
        val etEmail = findViewById<EditText>(R.id.etEmailRegistro)
        val etPassword = findViewById<EditText>(R.id.etPasswordRegistro)
        val cbEsConductor = findViewById<CheckBox>(R.id.cbEsConductor)
        val etCoche = findViewById<EditText>(R.id.etCocheRegistro) // El que pusimos 'gone' en el XML
        val btnRegistrar = findViewById<Button>(R.id.btnRegistrar)
        val tvVolverLogin = findViewById<TextView>(R.id.tvVolverLogin)

        // --- LÓGICA DE VISIBILIDAD DINÁMICA ---
        cbEsConductor.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Si marca el checkbox, mostramos el campo
                etCoche.visibility = View.VISIBLE
            } else {
                // Si lo desmarca, lo ocultamos y limpiamos el texto
                etCoche.visibility = View.GONE
                etCoche.text.clear()
            }
        }
        // --------------------------------------

        tvVolverLogin.setOnClickListener { finish() }

        btnRegistrar.setOnClickListener {
            val nombre = etNombre.text.toString()
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            val esConductor = cbEsConductor.isChecked
            val coche = etCoche.text.toString()

            if (nombre.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {

                // Crear usuario en Firebase Auth
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { tarea ->
                        if (tarea.isSuccessful) {
                            val userId = auth.currentUser?.uid ?: ""

                            // Crear el objeto Usuario con nuestro modelo
                            val nuevoUsuario = Usuario(
                                id = userId,
                                nombre = nombre,
                                email = email,
                                esConductor = esConductor,
                                coche = if (esConductor) coche else null
                            )

                            // Guardar en Firestore
                            db.collection("usuarios").document(userId).set(nuevoUsuario)
                                // ... dentro del onSuccess de Firestore ...
                                .addOnSuccessListener {
                                    // 1. Primero lanzamos el mensaje (Toast)
                                    Toast.makeText(applicationContext, "¡Cuenta creada correctamente!", Toast.LENGTH_LONG).show()

                                    // 2. Imprimimos en la consola de Android Studio (Logcat) para estar 100% seguros
                                    println("DEBUG: Usuario guardado con éxito en Firestore")

                                    // 3. Volvemos al Login
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Error al guardar perfil: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        } else {
                            Toast.makeText(this, "Error: ${tarea.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Rellena los campos obligatorios", Toast.LENGTH_SHORT).show()
            }
        }
    }
}