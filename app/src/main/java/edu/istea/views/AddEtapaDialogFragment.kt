package edu.istea.views

import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import edu.istea.R
import edu.istea.model.Etapa
import edu.istea.model.Planta
import java.util.Calendar

class AddEtapaDialogFragment(private val plantas: List<Planta>) : DialogFragment() {

    interface AddEtapaDialogListener {
        fun onEtapaAdded(etapa: Etapa)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_add_etapa, null)

            val plantaSpinner = view.findViewById<Spinner>(R.id.spinner_planta_etapa)
            val estadoSpinner = view.findViewById<Spinner>(R.id.spinner_estado_etapa)
            val fechaDatePicker = view.findViewById<DatePicker>(R.id.dp_fecha_etapa)

            // Plantas
            val plantaNombres = plantas.map { it.nombre }
            val plantaAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, plantaNombres)
            plantaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            plantaSpinner.adapter = plantaAdapter

            // Estados
            val estados = arrayOf("germinacion", "plantula", "vegetativo", "flora", "cosecha", "plaga", "curado", "secado", "transplante", "poda pical", "poda LST")
            val estadoAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, estados)
            estadoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            estadoSpinner.adapter = estadoAdapter

            builder.setView(view)
                .setPositiveButton("Añadir") { _, _ ->
                    val selectedPlantaPosition = plantaSpinner.selectedItemPosition
                    val selectedPlanta = plantas[selectedPlantaPosition]
                    val estado = estadoSpinner.selectedItem.toString()

                    val day = fechaDatePicker.dayOfMonth
                    val month = fechaDatePicker.month
                    val year = fechaDatePicker.year
                    val calendar = Calendar.getInstance()
                    calendar.set(year, month, day)
                    val fecha = "${day}/${month + 1}/${year}"

                    val nuevaEtapa = Etapa(plantaId = selectedPlanta.id, plantaNombre = selectedPlanta.nombre, estado = estado, fecha = fecha)
                    (activity as? AddEtapaDialogListener)?.onEtapaAdded(nuevaEtapa)
                }
                .setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}