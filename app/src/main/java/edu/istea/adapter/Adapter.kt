package edu.istea.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.istea.R
import edu.istea.model.Comida

class Adapter (private val dataSet: ArrayList<Comida>) : RecyclerView.Adapter<Adapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val TipoComida: TextView = view.findViewById(R.id.tipo_comida_tabla)
        val comidaPrincipal: TextView = view.findViewById(R.id.comida_principal_tabla)
        val comidaSecundaria: TextView = view.findViewById(R.id.comida_secundaria_tabla)
        val bebida: TextView = view.findViewById(R.id.bebida_tabla)
        val postreBoolean: TextView = view.findViewById(R.id.ingirio_postre_tabla)
        val postre: TextView = view.findViewById(R.id.postre_tabla)
        val tentacionBoolean: TextView = view.findViewById(R.id.tentacion_pregunta_tabla)
        val tentacion: TextView = view.findViewById(R.id.tentacion_tabla)
        val hambreBoolean: TextView = view.findViewById(R.id.hambre_pregunta_tabla)
        val rowPostre: TableRow = view.findViewById(R.id.row_postre)
        val rowTentacion: TableRow = view.findViewById(R.id.row_tentacion)
        val rowPostreBoolean: TableRow = view.findViewById(R.id.row_ingirio_postre)
        val dia: TextView = view.findViewById(R.id.fecha_tabla)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataSet[position]

        holder.rowPostre.visibility = if(item.postreBoolean != "Si") View.GONE else View.VISIBLE
        holder.rowTentacion.visibility = if(item.tentacionBoolean != "Si") View.GONE else View.VISIBLE
        holder.rowPostreBoolean.visibility = if(item.tipoComida != "Almuerzo" && item.tipoComida != "Cena") View.GONE else View.VISIBLE

        holder.TipoComida.text = item.tipoComida
        holder.comidaPrincipal.text = item.comidaPrincipal
        holder.comidaSecundaria.text = item.comidaSecundaria
        holder.bebida.text = item.bebida
        holder.postreBoolean.text = item.postreBoolean
        holder.postre.text = item.postre
        holder.tentacionBoolean.text = item.tentacionBoolean
        holder.tentacion.text = item.tentacion
        holder.hambreBoolean.text = item.hambreBoolean
        holder.dia.text = item.dia
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.resumen_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }
}