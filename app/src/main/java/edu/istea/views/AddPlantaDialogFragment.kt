package edu.istea.views

import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import edu.istea.R
import edu.istea.model.Planta
import java.util.Calendar

class AddPlantaDialogFragment : DialogFragment() {

    interface AddPlantaDialogListener {
        fun onPlantaAdded(planta: Planta)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_add_planta, null)

            val nombreEditText = view.findViewById<EditText>(R.id.et_nombre_planta)
            val geneticaRadioGroup = view.findViewById<RadioGroup>(R.id.rg_genetica)
            val fechaOrigenDatePicker = view.findViewById<DatePicker>(R.id.dp_fecha_origen)

            builder.setView(view)
                .setPositiveButton("Añadir") { _, _ ->
                    val nombre = nombreEditText.text.toString()
                    val selectedGeneticaId = geneticaRadioGroup.checkedRadioButtonId
                    val genetica = view.findViewById<RadioButton>(selectedGeneticaId).text.toString()

                    val day = fechaOrigenDatePicker.dayOfMonth
                    val month = fechaOrigenDatePicker.month
                    val year = fechaOrigenDatePicker.year
                    val calendar = Calendar.getInstance()
                    calendar.set(year, month, day)
                    val fechaOrigen = "${day}/${month + 1}/${year}"

                    val nuevaPlanta = Planta(nombre = nombre, genetica = genetica, fechaOrigen = fechaOrigen)
                    (activity as? AddPlantaDialogListener)?.onPlantaAdded(nuevaPlanta)
                }
                .setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}