package edu.istea.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Planta(
    val id: Int = 0,
    val nombre: String,
    val genetica: String,
    val fechaOrigen: String
) : Parcelable
