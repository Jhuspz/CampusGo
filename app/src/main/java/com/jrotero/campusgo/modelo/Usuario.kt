package com.jrotero.campusgo.modelo // Asegúrate de que esta línea coincida con tu proyecto

/**
 * Clase de datos que representa a un alumno en la plataforma.
 * Usamos una 'data class' de Kotlin porque su función principal es almacenar datos.
 */
data class Usuario(
    val id: String = "",           // ID único de Firebase Authentication
    val nombre: String = "",       // Nombre completo del alumno
    val email: String = "",        // Correo institucional
    val esConductor: Boolean = false, // Para saber si ofrece o busca viaje
    val coche: String? = null      // Modelo del coche (opcional, solo si es conductor)
)