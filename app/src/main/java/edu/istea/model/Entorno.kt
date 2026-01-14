package edu.istea.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Entorno(
    val id: Int = 0,
    val plantaId: Int,
    val plantaNombre: String,
    val fecha: String,
    val tipo: String,
    val valor: String,
    val unidad: String
) : Parcelable
