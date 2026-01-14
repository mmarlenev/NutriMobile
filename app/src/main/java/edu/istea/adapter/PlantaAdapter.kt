package edu.istea.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.istea.R
import edu.istea.model.Planta

class PlantaAdapter : ListAdapter<Planta, PlantaAdapter.PlantaViewHolder>(PlantaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.planta_list_item, parent, false)
        return PlantaViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlantaViewHolder, position: Int) {
        val planta = getItem(position)
        holder.bind(planta)
    }

    class PlantaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nombre: TextView = itemView.findViewById(R.id.tv_planta_nombre)
        private val info: TextView = itemView.findViewById(R.id.tv_planta_info)

        fun bind(planta: Planta) {
            nombre.text = planta.nombre
            info.text = "${planta.genetica} - Origen: ${planta.fechaOrigen}"
        }
    }
}

class PlantaDiffCallback : DiffUtil.ItemCallback<Planta>() {
    override fun areItemsTheSame(oldItem: Planta, newItem: Planta): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Planta, newItem: Planta): Boolean {
        return oldItem == newItem
    }
}