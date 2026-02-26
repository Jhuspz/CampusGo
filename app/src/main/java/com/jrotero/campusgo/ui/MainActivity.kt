package com.jrotero.campusgo.ui // Revisa tu paquete

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.jrotero.campusgo.R

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializamos el portero de Firebase
        auth = FirebaseAuth.getInstance()

        // Referencias a los elementos del diseño XML
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvIrRegistro = findViewById<TextView>(R.id.tvIrRegistro)

        // Botón para ir a la pantalla de Registro (el que ya tenías)
        tvIrRegistro.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }

        // --- LÓGICA DE INICIO DE SESIÓN ---
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Le pedimos a Firebase que verifique las credenciales
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { tarea ->
                        if (tarea.isSuccessful) {
                            // ¡LOGIN CORRECTO!
                            Toast.makeText(this, "Bienvenido a CampusGo", Toast.LENGTH_SHORT).show()

                            // ESTO ES LO NUEVO: se dirige a la pantalla principal de la aplicacion
                            val intent = Intent(this, HomeActivity::class.java)
                            startActivity(intent)
                            finish() // Cerramos el login para que no pueda volver atrás con el botón físico


                            // AQUÍ IRÍAMOS A LA PANTALLA PRINCIPAL (Home)
                            // Por ahora, solo lanzamos un mensaje de éxito
                        } else {
                            // ERROR: Contraseña mal, usuario no existe, etc.
                            Toast.makeText(this, "Error: ${tarea.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Escribe tu email y contraseña", Toast.LENGTH_SHORT).show()
            }
        }
    }
}