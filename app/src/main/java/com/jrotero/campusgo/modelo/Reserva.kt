package com.jrotero.campusgo.modelo // Revisa que esta primera línea sea la de tu proyecto

/**
 * Clase de datos que representa la reserva de un asiento.
 * Actúa como "tabla intermedia" vinculando a un Pasajero con un Viaje específico.
 */
data class Reserva(
    val idReserva: String = "",       // ID único de esta reserva generado por Firebase
    val idViaje: String = "",         // ¿A qué viaje pertenece este ticket?
    val idPasajero: String = "",      // ¿Quién es el alumno que ha reservado?
    val timestamp: Long = 0L,         // Fecha y hora exacta en la que se hizo la reserva (para ordenar)
    val estado: String = "pendiente"  // Puede ser: "pendiente", "confirmada" o "cancelada"
)