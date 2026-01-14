package edu.istea.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.istea.R
import edu.istea.model.Entorno

class EntornoAdapter : ListAdapter<Entorno, EntornoAdapter.EntornoViewHolder>(EntornoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntornoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.entorno_list_item, parent, false)
        return EntornoViewHolder(view)
    }

    override fun onBindViewHolder(holder: EntornoViewHolder, position: Int) {
        val entorno = getItem(position)
        holder.bind(entorno)
    }

    class EntornoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val descripcion: TextView = itemView.findViewById(R.id.tv_entorno_descripcion)
        private val info: TextView = itemView.findViewById(R.id.tv_entorno_info)

        fun bind(entorno: Entorno) {
            descripcion.text = "${entorno.tipo}: ${entorno.valor} ${entorno.unidad}"
            info.text = "${entorno.plantaNombre} - ${entorno.fecha}"
        }
    }
}

class EntornoDiffCallback : DiffUtil.ItemCallback<Entorno>() {
    override fun areItemsTheSame(oldItem: Entorno, newItem: Entorno): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Entorno, newItem: Entorno): Boolean {
        return oldItem == newItem
    }
}