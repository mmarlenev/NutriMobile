package edu.istea.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.istea.R
import edu.istea.model.Alimentacion

class AlimentacionAdapter(
    private val onModifyClick: (Alimentacion) -> Unit,
    private val onDeleteClick: (Alimentacion) -> Unit
) : ListAdapter<Alimentacion, AlimentacionAdapter.AlimentacionViewHolder>(AlimentacionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlimentacionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.alimentacion_list_item, parent, false)
        return AlimentacionViewHolder(view, onModifyClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: AlimentacionViewHolder, position: Int) {
        val alimentacion = getItem(position)
        holder.bind(alimentacion)
    }

    class AlimentacionViewHolder(
        itemView: View,
        private val onModifyClick: (Alimentacion) -> Unit,
        private val onDeleteClick: (Alimentacion) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val descripcion: TextView = itemView.findViewById(R.id.tv_alimentacion_descripcion)
        private val info: TextView = itemView.findViewById(R.id.tv_alimentacion_info)
        private val modifyButton: ImageView = itemView.findViewById(R.id.btn_modificar_alimentacion)
        private val deleteButton: ImageView = itemView.findViewById(R.id.btn_eliminar_alimentacion)
        private lateinit var currentAlimentacion: Alimentacion

        init {
            modifyButton.setOnClickListener {
                onModifyClick(currentAlimentacion)
            }
            deleteButton.setOnClickListener {
                AlertDialog.Builder(itemView.context)
                    .setTitle("Confirmar Eliminación")
                    .setMessage("¿Estás seguro de que quieres eliminar este registro de alimentación?")
                    .setPositiveButton("Eliminar") { _, _ ->
                        onDeleteClick(currentAlimentacion)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }

        fun bind(alimentacion: Alimentacion) {
            currentAlimentacion = alimentacion
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