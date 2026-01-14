package edu.istea.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.istea.R
import edu.istea.model.Etapa

class EtapaAdapter : ListAdapter<Etapa, EtapaAdapter.EtapaViewHolder>(EtapaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EtapaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.etapa_list_item, parent, false)
        return EtapaViewHolder(view)
    }

    override fun onBindViewHolder(holder: EtapaViewHolder, position: Int) {
        val etapa = getItem(position)
        holder.bind(etapa)
    }

    class EtapaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val descripcion: TextView = itemView.findViewById(R.id.tv_etapa_descripcion)
        private val fecha: TextView = itemView.findViewById(R.id.tv_etapa_fecha)

        fun bind(etapa: Etapa) {
            descripcion.text = "'${etapa.plantaNombre}' pasó a la etapa de ${etapa.estado}"
            fecha.text = etapa.fecha
        }
    }
}

class EtapaDiffCallback : DiffUtil.ItemCallback<Etapa>() {
    override fun areItemsTheSame(oldItem: Etapa, newItem: Etapa): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Etapa, newItem: Etapa): Boolean {
        return oldItem == newItem
    }
}