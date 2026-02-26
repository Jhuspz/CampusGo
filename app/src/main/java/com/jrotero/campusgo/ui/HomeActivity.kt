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

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Referencias de la interfaz
    private lateinit var tvBienvenida: TextView
    private lateinit var btnIrAPublicar: Button
    private lateinit var btnCerrarSesion: Button
    private lateinit var rvViajes: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // 1. Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // 2. Vincular vistas
        tvBienvenida = findViewById(R.id.tvBienvenida)
       // btnIrAPublicar = findViewById(R.id.btnIrAPublicar)
        btnCerrarSesion = findViewById(R.id.btnCerrarSesion)
        rvViajes = findViewById(R.id.rvViajes)

        // 3. Configurar el RecyclerView (de momento vacío)
        rvViajes.layoutManager = LinearLayoutManager(this)

        // 4. Obtener datos del usuario actual desde Firestore
        obtenerDatosUsuario()

        // 5. Configurar botones
        btnIrAPublicar.setOnClickListener {
            //val intent = Intent(this, PublicarViajeActivity::class.java)
            startActivity(intent)
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
                    if (documento != null && documento.exists()) {
                        val nombre = documento.getString("nombre") ?: "Usuario"
                        val esConductor = documento.getBoolean("esConductor") ?: false

                        tvBienvenida.text = "¡Hola, $nombre!"

                        // LÓGICA INTELIGENTE: Solo mostramos el botón si es conductor
                        if (esConductor) {
                            btnIrAPublicar.visibility = View.VISIBLE
                        } else {
                            btnIrAPublicar.visibility = View.GONE
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al cargar perfil: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
