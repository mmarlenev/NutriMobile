package edu.istea.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.istea.R
import edu.istea.model.HistorialEvento

class HistorialAdapter : ListAdapter<HistorialEvento, HistorialAdapter.HistorialViewHolder>(HistorialDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistorialViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.historial_list_item, parent, false)
        return HistorialViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistorialViewHolder, position: Int) {
        val evento = getItem(position)
        holder.bind(evento)
    }

    class HistorialViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val descripcion: TextView = itemView.findViewById(R.id.tv_evento_descripcion)
        private val info: TextView = itemView.findViewById(R.id.tv_evento_info)

        fun bind(evento: HistorialEvento) {
            descripcion.text = evento.descripcion
            info.text = "[${evento.tipoEvento}] - ${evento.fecha}"
        }
    }
}

class HistorialDiffCallback : DiffUtil.ItemCallback<HistorialEvento>() {
    override fun areItemsTheSame(oldItem: HistorialEvento, newItem: HistorialEvento): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: HistorialEvento, newItem: HistorialEvento): Boolean {
        return oldItem == newItem
    }
}