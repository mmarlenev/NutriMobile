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
import edu.istea.model.Entorno

// --- ViewModels for the list ---
sealed class EntornoListItem {
    abstract val id: String

    data class PlantaHeader(val planta: EntornoPlanta, var isExpanded: Boolean = false) : EntornoListItem() {
        override val id: String get() = planta.plantaId.toString()
    }

    data class FechaItem(val fecha: EntornoFecha) : EntornoListItem() {
        override val id: String get() = "${fecha.plantaId}-${fecha.fecha}"
    }
}

// --- Data classes for the adapter ---
data class EntornoPlanta(val plantaId: Int, val plantaNombre: String, val ultimaFecha: String, val fechas: List<EntornoFecha>)
data class EntornoFecha(val plantaId: Int, val plantaNombre: String, val fecha: String)


class EntornoAdapter(
    private val onHeaderClick: (EntornoListItem.PlantaHeader) -> Unit,
    private val onFechaClick: (EntornoFecha) -> Unit
) : ListAdapter<EntornoListItem, RecyclerView.ViewHolder>(EntornoDiffCallback()) {

    companion object {
        private const val TYPE_PLANTA = 0
        private const val TYPE_FECHA = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is EntornoListItem.PlantaHeader -> TYPE_PLANTA
            is EntornoListItem.FechaItem -> TYPE_FECHA
            null -> throw IllegalStateException("Null item at position $position")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_PLANTA) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.entorno_list_item, parent, false)
            PlantaViewHolder(view, onHeaderClick)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.entorno_fecha_item, parent, false)
            FechaViewHolder(view, onFechaClick)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is EntornoListItem.PlantaHeader -> (holder as PlantaViewHolder).bind(item)
            is EntornoListItem.FechaItem -> (holder as FechaViewHolder).bind(item.fecha)
        }
    }

    // --- ViewHolders ---

    class PlantaViewHolder(itemView: View, private val onHeaderClick: (EntornoListItem.PlantaHeader) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val nombre: TextView = itemView.findViewById(R.id.tv_planta_nombre_group)
        private val fecha: TextView = itemView.findViewById(R.id.tv_fecha_group)
        private val arrow: ImageView = itemView.findViewById(R.id.iv_expand_arrow)

        fun bind(header: EntornoListItem.PlantaHeader) {
            nombre.text = header.planta.plantaNombre
            fecha.text = "Última medición: ${header.planta.ultimaFecha}"
            arrow.rotation = if (header.isExpanded) 180f else 0f
            itemView.setOnClickListener { onHeaderClick(header) }
        }
    }

    class FechaViewHolder(itemView: View, private val onFechaClick: (EntornoFecha) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val fecha: TextView = itemView.findViewById(R.id.tv_fecha)

        fun bind(fechaItem: EntornoFecha) {
            fecha.text = fechaItem.fecha
            itemView.setOnClickListener { onFechaClick(fechaItem) }
        }
    }

    // --- DiffUtil Callback ---

    class EntornoDiffCallback : DiffUtil.ItemCallback<EntornoListItem>() {
        override fun areItemsTheSame(oldItem: EntornoListItem, newItem: EntornoListItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: EntornoListItem, newItem: EntornoListItem): Boolean {
            return oldItem == newItem
        }
    }
}