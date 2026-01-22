package edu.istea.model

data class TipoMedicion(
    val nombre: String,
    val unidad: String,
    val isNumeric: Boolean,
    val levels: List<String>? = null
)