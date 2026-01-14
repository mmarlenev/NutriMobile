package edu.istea.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HistorialEvento(
    val id: Int = 0,
    val fecha: String,
    val tipoEvento: String, // Ej: "Nueva Planta", "Etapa", "Entorno", "Alimentación"
    val descripcion: String // Ej: "Planta 'AK-47' creada" o "Riego 500ml"
) : Parcelable
