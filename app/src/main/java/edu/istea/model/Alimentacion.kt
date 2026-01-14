package edu.istea.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Alimentacion(
    val id: Int = 0,
    val plantaId: Int,
    val plantaNombre: String,
    val fecha: String,
    val insumo: String,
    val cantidad: Float,
    val unidad: String
) : Parcelable
