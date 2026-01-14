package edu.istea.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.istea.R
import edu.istea.model.Alimentacion

class AlimentacionAdapter : ListAdapter<Alimentacion, AlimentacionAdapter.AlimentacionViewHolder>(AlimentacionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlimentacionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.alimentacion_list_item, parent, false)
        return AlimentacionViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlimentacionViewHolder, position: Int) {
        val alimentacion = getItem(position)
        holder.bind(alimentacion)
    }

    class AlimentacionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val descripcion: TextView = itemView.findViewById(R.id.tv_alimentacion_descripcion)
        private val info: TextView = itemView.findViewById(R.id.tv_alimentacion_info)

        fun bind(alimentacion: Alimentacion) {
            descripcion.text = "Se añadió ${alimentacion.cantidad} ${alimentacion.unidad} de ${alimentacion.insumo}"
            info.text = "${alimentacion.plantaNombre} - ${alimentacion.fecha}"
        }
    }
}

class AlimentacionDiffCallback : DiffUtil.ItemCallback<Alimentacion>() {
    override fun areItemsTheSame(oldItem: Alimentacion, newItem: Alimentacion): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Alimentacion, newItem: Alimentacion): Boolean {
        return oldItem == newItem
    }
}