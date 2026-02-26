package com.jrotero.campusgo.modelo // Revisa que esta primera línea sea la de tu proyecto

/**
 * Clase de datos que representa un viaje publicado en CampusGo.
 * Contiene toda la información necesaria para que un pasajero decida reservar.
 */
data class Viaje(
    val idViaje: String = "",           // ID único generado por Firebase para este viaje
    val conductorId: String = "",       // El ID del Usuario que conduce (crea la relación)
    val origen: String = "",            // Ej: "Centro Ciudad"
    val destino: String = "",           // Ej: "Campus Norte"
    val fecha: String = "",             // Ej: "20/10/2026" (Lo guardamos como texto por simplicidad ahora)
    val hora: String = "",              // Ej: "08:30"
    val plazasTotales: Int = 0,         // Plazas originales del coche
    val plazasDisponibles: Int = 0,     // Plazas que van quedando libres tras las reservas
    val precio: Double = 0.0,           // Precio opcional por compartir gastos
    val estado: String = "activo"       // Puede ser "activo", "completado" o "cancelado"
)