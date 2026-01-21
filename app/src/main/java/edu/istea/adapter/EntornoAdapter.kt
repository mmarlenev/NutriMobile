package edu.istea.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.istea.R

// New data class to represent the grouped item
data class EntornoAgrupado(
    val plantaId: Int,
    val plantaNombre: String,
    val fecha: String
)

class EntornoAdapter(
    private val onItemClick: (EntornoAgrupado) -> Unit // Click listener for the whole card
) : ListAdapter<EntornoAgrupado, EntornoAdapter.EntornoViewHolder>(EntornoAgrupadoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntornoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.entorno_list_item, parent, false)
        return EntornoViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: EntornoViewHolder, position: Int) {
        val entornoAgrupado = getItem(position)
        holder.bind(entornoAgrupado)
    }

    class EntornoViewHolder(
        itemView: View,
        private val onItemClick: (EntornoAgrupado) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val plantaNombreTextView: TextView = itemView.findViewById(R.id.tv_planta_nombre_group)
        private val fechaTextView: TextView = itemView.findViewById(R.id.tv_fecha_group)
        private lateinit var currentEntornoAgrupado: EntornoAgrupado

        init {
            // Set the click listener on the whole card view
            itemView.setOnClickListener {
                onItemClick(currentEntornoAgrupado)
            }
        }

        fun bind(entornoAgrupado: EntornoAgrupado) {
            currentEntornoAgrupado = entornoAgrupado
            plantaNombreTextView.text = entornoAgrupado.plantaNombre
            fechaTextView.text = entornoAgrupado.fecha
        }
    }
}

class EntornoAgrupadoDiffCallback : DiffUtil.ItemCallback<EntornoAgrupado>() {
    override fun areItemsTheSame(oldItem: EntornoAgrupado, newItem: EntornoAgrupado): Boolean {
        // A unique group is defined by the plant and the date
        return oldItem.plantaId == newItem.plantaId && oldItem.fecha == newItem.fecha
    }

    override fun areContentsTheSame(oldItem: EntornoAgrupado, newItem: EntornoAgrupado): Boolean {
        return oldItem == newItem
    }
}