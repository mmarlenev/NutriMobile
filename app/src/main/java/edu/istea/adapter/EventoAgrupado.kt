package edu.istea.adapter

import edu.istea.model.Evento

data class EventoGrupo(
    val nombre: String,
    val ultimaFecha: String,
    val fechas: List<EventoFecha>,
    var isExpanded: Boolean = false
)

data class EventoFecha(
    val groupNombre: String,
    val fecha: String,
    val eventos: List<EventoDetalle>,
    var isExpanded: Boolean = false
)

data class EventoDetalle(
    val evento: Evento
)

sealed class EventoListItem {
    data class GrupoHeader(val grupo: EventoGrupo) : EventoListItem()
    data class FechaHeader(val fecha: EventoFecha) : EventoListItem()
    data class DetalleItem(val detalle: EventoDetalle) : EventoListItem()
}