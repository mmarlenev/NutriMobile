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
import edu.istea.model.Entorno

class EntornoAdapter(
    private val onModifyClick: (Entorno) -> Unit,
    private val onDeleteClick: (Entorno) -> Unit
) : ListAdapter<Entorno, EntornoAdapter.EntornoViewHolder>(EntornoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntornoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.entorno_list_item, parent, false)
        return EntornoViewHolder(view, onModifyClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: EntornoViewHolder, position: Int) {
        val entorno = getItem(position)
        holder.bind(entorno)
    }

    class EntornoViewHolder(
        itemView: View,
        private val onModifyClick: (Entorno) -> Unit,
        private val onDeleteClick: (Entorno) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val descripcion: TextView = itemView.findViewById(R.id.tv_entorno_descripcion)
        private val info: TextView = itemView.findViewById(R.id.tv_entorno_info)
        private val modifyButton: ImageButton = itemView.findViewById(R.id.btn_modificar_entorno)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.btn_eliminar_entorno)
        private lateinit var currentEntorno: Entorno

        init {
            modifyButton.setOnClickListener {
                onModifyClick(currentEntorno)
            }
            deleteButton.setOnClickListener {
                AlertDialog.Builder(itemView.context)
                    .setTitle("Confirmar Eliminación")
                    .setMessage("¿Estás seguro de que quieres eliminar esta medición de entorno?")
                    .setPositiveButton("Eliminar") { _, _ ->
                        onDeleteClick(currentEntorno)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }

        fun bind(entorno: Entorno) {
            currentEntorno = entorno
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