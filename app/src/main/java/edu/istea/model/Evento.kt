package edu.istea.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Evento(
    val id: Int = 0,
    val sujeto: String,
    val suceso: String,
    val fecha: String,
    val plantaId: Int? = null
) : Parcelable
