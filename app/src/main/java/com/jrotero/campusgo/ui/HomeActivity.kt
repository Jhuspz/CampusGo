package com.jrotero.campusgo.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jrotero.campusgo.R

class HomeActivity : AppCompatActivity() {

    // 1. Declaramos las variables para Firebase (Autenticación y Base de datos)
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // 2. Inicializamos las instancias de Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // 3. Enlazamos las variables con los IDs de los botones y textos del XML
        val tvBienvenida = findViewById<TextView>(R.id.tvBienvenida)
        val btnBuscar = findViewById<Button>(R.id.btnBuscarViajes)
        val btnReservas = findViewById<Button>(R.id.btnMisReservas)
        val btnPublicar = findViewById<Button>(R.id.btnPublicarViaje)
        val btnMisViajes = findViewById<Button>(R.id.btnMisViajes)
        val btnCerrarSesion = findViewById<Button>(R.id.btnCerrarSesion)

        // 4. Llamamos a la función que consulta Firestore para saber si es conductor o pasajero
        // Le pasamos los botones y el texto por parámetro para que pueda modificarlos
        obtenerDatosUsuario(btnPublicar, btnMisViajes, tvBienvenida)

        // --- 5. CONFIGURACIÓN DE LOS CLICS DE LOS BOTONES ---

        // Botón 1: Buscar Viajes (Disponible para TODOS)
        btnBuscar.setOnClickListener {
            // Nota: Tendrás que crear BuscarViajesActivity más adelante (donde pondremos el RecyclerView)
            val intent = Intent(this, BuscarViajesActivity::class.java)
            startActivity(intent)
        }

        // Botón 2: Mis Reservas (Disponible para TODOS)
        btnReservas.setOnClickListener {
            val intent = Intent(this, MisReservasActivity::class.java)
            startActivity(intent)
        }

        // Botón 3: Publicar Viaje (SOLO para CONDUCTORES)
        btnPublicar.setOnClickListener {
            // Este ya lo tenemos funcionando
            startActivity(Intent(this, PublicarViajeActivity::class.java))
        }

        // Botón 4: Mis Viajes Publicados (SOLO para CONDUCTORES)
        btnMisViajes.setOnClickListener {
            val intent = Intent(this, MisViajesActivity::class.java)
            startActivity(intent)
        }

        // Botón 5: Cerrar Sesión
        btnCerrarSesion.setOnClickListener {
            // Cierra la sesión en Firebase
            auth.signOut()

            // Creamos un Intent para volver al Login (MainActivity)
            val intent = Intent(this, MainActivity::class.java)
            // Estas "flags" (banderas) borran el historial de pantallas.
            // Así, si el usuario le da al botón "Atrás" del móvil, no volverá a entrar al Home sin contraseña.
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish() // Cerramos esta Activity
        }
    }

    /**
     * Esta función se conecta a Firestore, busca el perfil del usuario logueado
     * y decide qué botones mostrar en la pantalla dependiendo de su rol.
     */
    private fun obtenerDatosUsuario(btnPub: Button, btnMisV: Button, tvBienvenida: TextView) {
        // Obtenemos el ID único del usuario que acaba de iniciar sesión
        val userId = auth.currentUser?.uid

        if (userId != null) {
            // Vamos a la colección "usuarios", buscamos su documento por ID y lo leemos (.get())
            db.collection("usuarios").document(userId).get()
                .addOnSuccessListener { documento ->
                    // Si el documento existe en la base de datos...
                    if (documento != null && documento.exists()) {

                        // Extraemos el nombre y el booleano 'esConductor'
                        // Si por algún motivo no hay nombre, ponemos "Usuario" por defecto
                        val nombre = documento.getString("nombre") ?: "Usuario"
                        val esConductor = documento.getBoolean("esConductor") ?: false

                        // Personalizamos el mensaje de bienvenida
                        tvBienvenida.text = "¡Hola, $nombre!"

                        // LÓGICA INTELIGENTE (Roles):
                        // Si la variable esConductor es TRUE, hacemos visibles los botones de coche
                        if (esConductor) {
                            btnPub.visibility = View.VISIBLE
                            btnMisV.visibility = View.VISIBLE
                        }
                        // Nota: No hace falta el 'else' poniéndolos en GONE porque
                        // en el archivo XML (activity_home.xml) ya los pusimos en "gone" por defecto.
                    }
                }
                .addOnFailureListener { e ->
                    // Si falla la conexión a internet o Firebase, mostramos un error
                    Toast.makeText(this, "Error al cargar perfil: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}