package edu.istea.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.istea.R
import edu.istea.model.Etapa

class EtapaAdapter(
    private val onModifyClick: (Etapa) -> Unit,
    private val onDeleteClick: (Etapa) -> Unit
) : ListAdapter<Etapa, EtapaAdapter.EtapaViewHolder>(EtapaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EtapaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.etapa_list_item, parent, false)
        return EtapaViewHolder(view, onModifyClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: EtapaViewHolder, position: Int) {
        val etapa = getItem(position)
        holder.bind(etapa)
    }

    class EtapaViewHolder(
        itemView: View,
        private val onModifyClick: (Etapa) -> Unit,
        private val onDeleteClick: (Etapa) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val descripcion: TextView = itemView.findViewById(R.id.tv_etapa_descripcion)
        private val fecha: TextView = itemView.findViewById(R.id.tv_etapa_fecha)
        private val modifyButton: ImageButton = itemView.findViewById(R.id.btn_modificar_etapa)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.btn_eliminar_etapa)
        private lateinit var currentEtapa: Etapa

        init {
            modifyButton.setOnClickListener {
                onModifyClick(currentEtapa)
            }
            deleteButton.setOnClickListener {
                AlertDialog.Builder(itemView.context)
                    .setTitle("Confirmar Eliminación")
                    .setMessage("¿Estás seguro de que quieres eliminar esta etapa?")
                    .setPositiveButton("Eliminar") { _, _ ->
                        onDeleteClick(currentEtapa)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }

        fun bind(etapa: Etapa) {
            currentEtapa = etapa
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