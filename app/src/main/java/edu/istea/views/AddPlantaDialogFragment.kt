package edu.istea.views

import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.BundleCompat
import androidx.fragment.app.DialogFragment
import edu.istea.R
import edu.istea.model.Planta

class AddPlantaDialogFragment : DialogFragment() {

    interface PlantaDialogListener {
        fun onPlantaAdded(planta: Planta)
        fun onPlantaUpdated(planta: Planta)
    }

    private var planta: Planta? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            planta = BundleCompat.getParcelable(it, ARG_PLANTA, Planta::class.java)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let { activity ->
            val builder = AlertDialog.Builder(activity)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_add_planta, null)

            val nombreEditText = view.findViewById<EditText>(R.id.et_nombre_planta)
            val geneticaRadioGroup = view.findViewById<RadioGroup>(R.id.rg_genetica)
            val fechaOrigenDatePicker = view.findViewById<DatePicker>(R.id.dp_fecha_origen)

            planta?.let { existingPlanta ->
                nombreEditText.setText(existingPlanta.nombre)
                when (existingPlanta.genetica) {
                    "Autofloreciente" -> view.findViewById<RadioButton>(R.id.rb_autofloreciente).isChecked = true
                    "Fotoperiódica" -> view.findViewById<RadioButton>(R.id.rb_fotoperiodica).isChecked = true
                }
                val dateParts = existingPlanta.fechaOrigen.split("/")
                fechaOrigenDatePicker.updateDate(dateParts[2].toInt(), dateParts[1].toInt() - 1, dateParts[0].toInt())
            }

            val actionButtonText = if (planta == null) "Añadir" else "Actualizar"

            builder.setView(view)
                .setPositiveButton(actionButtonText) { _, _ ->
                    val nombre = nombreEditText.text.toString()
                    val selectedGeneticaId = geneticaRadioGroup.checkedRadioButtonId
                    val genetica = view.findViewById<RadioButton>(selectedGeneticaId).text.toString()

                    val day = fechaOrigenDatePicker.dayOfMonth
                    val month = fechaOrigenDatePicker.month
                    val year = fechaOrigenDatePicker.year
                    val fechaOrigen = "$day/${month + 1}/$year"

                    if (planta == null) {
                        val nuevaPlanta = Planta(nombre = nombre, genetica = genetica, fechaOrigen = fechaOrigen)
                        (activity as? PlantaDialogListener)?.onPlantaAdded(nuevaPlanta)
                    } else {
                        val plantaActualizada = planta!!.copy(nombre = nombre, genetica = genetica, fechaOrigen = fechaOrigen)
                        (activity as? PlantaDialogListener)?.onPlantaUpdated(plantaActualizada)
                    }
                }
                .setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    companion object {
        private const val ARG_PLANTA = "planta"

        fun newInstance(planta: Planta? = null): AddPlantaDialogFragment {
            val fragment = AddPlantaDialogFragment()
            val args = Bundle()
            planta?.let {
                args.putParcelable(ARG_PLANTA, it)
            }
            fragment.arguments = args
            return fragment
        }
    }
}