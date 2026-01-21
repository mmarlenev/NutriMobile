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
import edu.istea.model.Planta

class PlantaAdapter(
    private val onModifyClick: (Planta) -> Unit,
    private val onDeleteClick: (Planta) -> Unit
) : ListAdapter<Planta, PlantaAdapter.PlantaViewHolder>(PlantaDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.planta_list_item, parent, false)
        return PlantaViewHolder(view, onModifyClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: PlantaViewHolder, position: Int) {
        val planta = getItem(position)
        holder.bind(planta)
    }

    class PlantaViewHolder(
        itemView: View,
        private val onModifyClick: (Planta) -> Unit,
        private val onDeleteClick: (Planta) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val nombre: TextView = itemView.findViewById(R.id.tv_planta_nombre)
        private val info: TextView = itemView.findViewById(R.id.tv_planta_info)
        private val modifyButton: ImageButton = itemView.findViewById(R.id.btn_modificar_planta)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.btn_eliminar_planta)
        private lateinit var currentPlanta: Planta

        init {
            modifyButton.setOnClickListener {
                onModifyClick(currentPlanta)
            }
            deleteButton.setOnClickListener {
                AlertDialog.Builder(itemView.context)
                    .setTitle("Confirmar Eliminación")
                    .setMessage("¿Estás seguro de que quieres eliminar esta planta y todos sus eventos asociados?")
                    .setPositiveButton("Eliminar") { _, _ ->
                        onDeleteClick(currentPlanta)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }

        fun bind(planta: Planta) {
            currentPlanta = planta
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