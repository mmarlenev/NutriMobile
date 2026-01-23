package edu.istea.views

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import edu.istea.R
import edu.istea.model.Planta
import java.util.Calendar

data class HistorialFilter(
    val plantaId: Int?,
    val tipoRegistro: String?,
    val fechaDesde: String?,
    val fechaHasta: String?
)

class HistorialFilterDialogFragment(private val plantas: List<Planta>) : DialogFragment() {

    interface HistorialFilterDialogListener {
        fun onFilterApplied(filter: HistorialFilter)
        fun onFilterCleared()
    }

    private lateinit var plantaSpinner: Spinner
    private lateinit var tipoSpinner: Spinner
    private lateinit var desdeDatePicker: DatePicker
    private lateinit var hastaDatePicker: DatePicker

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_historial_filter, null)
        setupViews(view)

        val tipos = arrayOf("Todos", "Etapa", "Suceso", "Alimentación", "Entorno")
        tipoSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, tipos).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        val plantaNombres = mutableListOf("Todas").apply { addAll(plantas.map { it.nombre }) }
        plantaSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, plantaNombres).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        
        return AlertDialog.Builder(requireContext())
            .setView(view)
            .setPositiveButton("Aplicar") { _, _ ->
                val planta = if (plantaSpinner.selectedItemPosition == 0) null else plantas[plantaSpinner.selectedItemPosition - 1]
                val tipo = if (tipoSpinner.selectedItemPosition == 0) null else tipos[tipoSpinner.selectedItemPosition]
                
                val desde = "${desdeDatePicker.dayOfMonth}/${desdeDatePicker.month + 1}/${desdeDatePicker.year}"
                val hasta = "${hastaDatePicker.dayOfMonth}/${hastaDatePicker.month + 1}/${hastaDatePicker.year}"

                val filter = HistorialFilter(planta?.id, tipo, desde, hasta)
                (activity as? HistorialFilterDialogListener)?.onFilterApplied(filter)
            }
            .setNegativeButton("Limpiar") { _, _ ->
                (activity as? HistorialFilterDialogListener)?.onFilterCleared()
            }
            .setNeutralButton("Cancelar", null)
            .create()
    }

    private fun setupViews(view: View) {
        plantaSpinner = view.findViewById(R.id.spinner_filter_planta)
        tipoSpinner = view.findViewById(R.id.spinner_filter_tipo)
        desdeDatePicker = view.findViewById(R.id.dp_filter_desde)
        hastaDatePicker = view.findViewById(R.id.dp_filter_hasta)
    }
}