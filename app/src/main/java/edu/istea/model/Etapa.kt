package edu.istea.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Etapa(
    val id: Int = 0,
    val plantaId: Int,
    val plantaNombre: String, // To show in the list
    val estado: String,
    val fecha: String
) : Parcelable
