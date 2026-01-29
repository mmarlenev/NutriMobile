package edu.istea.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.istea.R
import edu.istea.model.Evento

class EventoAdapter(
    private val onGrupoClick: (EventoListItem.GrupoHeader) -> Unit,
    private val onFechaClick: (EventoListItem.FechaHeader) -> Unit,
    private val onEditClick: (Evento) -> Unit,
    private val onDeleteClick: (Evento) -> Unit
) : ListAdapter<EventoListItem, RecyclerView.ViewHolder>(EventoDiffCallback()) {

    override fun getItemViewType(position: Int):
        Int {
        return when (getItem(position)) {
            is EventoListItem.GrupoHeader -> R.layout.evento_grupo_item
            is EventoListItem.FechaHeader -> R.layout.evento_fecha_item
            is EventoListItem.DetalleItem -> R.layout.evento_detalle_item
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return when (viewType) {
            R.layout.evento_grupo_item -> GrupoViewHolder(view, onGrupoClick)
            R.layout.evento_fecha_item -> FechaViewHolder(view, onFechaClick)
            R.layout.evento_detalle_item -> DetalleViewHolder(view, onEditClick, onDeleteClick)
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is EventoListItem.GrupoHeader -> (holder as GrupoViewHolder).bind(item)
            is EventoListItem.FechaHeader -> (holder as FechaViewHolder).bind(item)
            is EventoListItem.DetalleItem -> (holder as DetalleViewHolder).bind(item)
        }
    }

    class GrupoViewHolder(
        itemView: View,
        private val onGrupoClick: (EventoListItem.GrupoHeader) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val nombre: TextView = itemView.findViewById(R.id.tv_grupo_nombre)
        private val fecha: TextView = itemView.findViewById(R.id.tv_fecha_group)
        private val expandArrow: ImageView = itemView.findViewById(R.id.iv_expand_arrow)

        fun bind(header: EventoListItem.GrupoHeader) {
            nombre.text = header.grupo.nombre
            fecha.text = header.grupo.ultimaFecha
            expandArrow.rotation = if (header.grupo.isExpanded) 180f else 0f
            itemView.setOnClickListener { onGrupoClick(header) }
        }
    }

    class FechaViewHolder(
        itemView: View,
        private val onFechaClick: (EventoListItem.FechaHeader) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val fecha: TextView = itemView.findViewById(R.id.tv_fecha)
        private val expandArrow: ImageView = itemView.findViewById(R.id.iv_expand_arrow_fecha)

        fun bind(header: EventoListItem.FechaHeader) {
            fecha.text = header.fecha.fecha
            expandArrow.rotation = if (header.fecha.isExpanded) 180f else 0f
            itemView.setOnClickListener { onFechaClick(header) }
        }
    }

    class DetalleViewHolder(
        itemView: View,
        private val onEditClick: (Evento) -> Unit,
        private val onDeleteClick: (Evento) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val suceso: TextView = itemView.findViewById(R.id.tv_suceso)
        private val editButton: ImageView = itemView.findViewById(R.id.iv_edit_evento)
        private val deleteButton: ImageView = itemView.findViewById(R.id.iv_delete_evento)

        fun bind(item: EventoListItem.DetalleItem) {
            suceso.text = item.detalle.evento.suceso
            editButton.setOnClickListener { onEditClick(item.detalle.evento) }
            deleteButton.setOnClickListener { onDeleteClick(item.detalle.evento) }
        }
    }

    class EventoDiffCallback : DiffUtil.ItemCallback<EventoListItem>() {
        override fun areItemsTheSame(oldItem: EventoListItem, newItem: EventoListItem): Boolean {
            return when {
                oldItem is EventoListItem.GrupoHeader && newItem is EventoListItem.GrupoHeader ->
                    oldItem.grupo.nombre == newItem.grupo.nombre
                oldItem is EventoListItem.FechaHeader && newItem is EventoListItem.FechaHeader ->
                    oldItem.fecha.groupNombre == newItem.fecha.groupNombre && oldItem.fecha.fecha == newItem.fecha.fecha
                oldItem is EventoListItem.DetalleItem && newItem is EventoListItem.DetalleItem ->
                    oldItem.detalle.evento.id == newItem.detalle.evento.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: EventoListItem, newItem: EventoListItem): Boolean {
            return oldItem == newItem
        }
    }
}