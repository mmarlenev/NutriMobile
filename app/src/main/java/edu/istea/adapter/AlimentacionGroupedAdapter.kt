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

// --- ViewModels for the list ---
sealed class AlimentacionListItem {
    abstract val id: String

    data class PlantaHeader(val planta: AlimentacionPlanta, var isExpanded: Boolean = false) : AlimentacionListItem() {
        override val id: String get() = planta.plantaId.toString()
    }

    data class FechaItem(val fecha: AlimentacionFecha) : AlimentacionListItem() {
        override val id: String get() = "${fecha.plantaId}-${fecha.fecha}"
    }
}

// --- Data classes for the adapter ---
data class AlimentacionPlanta(val plantaId: Int, val plantaNombre: String, val ultimaFecha: String, val fechas: List<AlimentacionFecha>)
data class AlimentacionFecha(val plantaId: Int, val plantaNombre: String, val fecha: String)


class AlimentacionGroupedAdapter(
    private val onHeaderClick: (AlimentacionListItem.PlantaHeader) -> Unit,
    private val onFechaClick: (AlimentacionFecha) -> Unit
) : ListAdapter<AlimentacionListItem, RecyclerView.ViewHolder>(AlimentacionDiffCallback()) {

    companion object {
        private const val TYPE_PLANTA = 0
        private const val TYPE_FECHA = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is AlimentacionListItem.PlantaHeader -> TYPE_PLANTA
            is AlimentacionListItem.FechaItem -> TYPE_FECHA
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
            is AlimentacionListItem.PlantaHeader -> (holder as PlantaViewHolder).bind(item)
            is AlimentacionListItem.FechaItem -> (holder as FechaViewHolder).bind(item.fecha)
        }
    }

    // --- ViewHolders ---

    class PlantaViewHolder(itemView: View, private val onHeaderClick: (AlimentacionListItem.PlantaHeader) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val nombre: TextView = itemView.findViewById(R.id.tv_planta_nombre_group)
        private val fecha: TextView = itemView.findViewById(R.id.tv_fecha_group)
        private val arrow: ImageView = itemView.findViewById(R.id.iv_expand_arrow)
        private val alertStatus: TextView = itemView.findViewById(R.id.tv_alert_status)

        fun bind(header: AlimentacionListItem.PlantaHeader) {
            nombre.text = header.planta.plantaNombre
            fecha.text = "Última alimentación: ${header.planta.ultimaFecha}"
            arrow.rotation = if (header.isExpanded) 180f else 0f
            alertStatus.visibility = View.GONE // Hide alert status as it's not used in this context
            itemView.setOnClickListener { onHeaderClick(header) }
        }
    }

    class FechaViewHolder(itemView: View, private val onFechaClick: (AlimentacionFecha) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val fecha: TextView = itemView.findViewById(R.id.tv_fecha)

        fun bind(fechaItem: AlimentacionFecha) {
            fecha.text = fechaItem.fecha
            itemView.setOnClickListener { onFechaClick(fechaItem) }
        }
    }

    // --- DiffUtil Callback ---

    class AlimentacionDiffCallback : DiffUtil.ItemCallback<AlimentacionListItem>() {
        override fun areItemsTheSame(oldItem: AlimentacionListItem, newItem: AlimentacionListItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AlimentacionListItem, newItem: AlimentacionListItem): Boolean {
            return oldItem == newItem
        }
    }
}