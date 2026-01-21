package edu.istea.views

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.DatePicker
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.BundleCompat
import androidx.fragment.app.DialogFragment
import edu.istea.R
import edu.istea.dao.DBHelper
import edu.istea.model.Planta

class AddPlantaDialogFragment : DialogFragment() {

    interface PlantaDialogListener {
        fun onPlantaAdded(planta: Planta)
        fun onPlantaUpdated(planta: Planta)
    }

    private var listener: PlantaDialogListener? = null
    private var planta: Planta? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Try to set the listener from the hosting activity or fragment
        listener = targetFragment as? PlantaDialogListener ?: context as? PlantaDialogListener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            planta = BundleCompat.getParcelable(it, ARG_PLANTA, Planta::class.java)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = activity ?: throw IllegalStateException("Activity cannot be null")
        val builder = AlertDialog.Builder(activity)
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_add_planta, null)

        val dbHelper = DBHelper(requireContext())

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

        val dialog = builder.setView(view)
            .setPositiveButton(actionButtonText, null) // Override to control dismiss
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener { 
                val nombre = nombreEditText.text.toString().trim()

                if (nombre.isBlank()) {
                    Toast.makeText(requireContext(), "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (dbHelper.plantaNombreExiste(nombre, planta?.id)) {
                    Toast.makeText(requireContext(), "Ya existe una planta con este nombre", Toast.LENGTH_SHORT).show()
                } else {
                    val selectedGeneticaId = geneticaRadioGroup.checkedRadioButtonId
                    val genetica = view.findViewById<RadioButton>(selectedGeneticaId).text.toString()

                    val day = fechaOrigenDatePicker.dayOfMonth
                    val month = fechaOrigenDatePicker.month
                    val year = fechaOrigenDatePicker.year
                    val fechaOrigen = "$day/${month + 1}/$year"

                    if (planta == null) {
                        val nuevaPlanta = Planta(nombre = nombre, genetica = genetica, fechaOrigen = fechaOrigen)
                        listener?.onPlantaAdded(nuevaPlanta)
                    } else {
                        val plantaActualizada = planta!!.copy(nombre = nombre, genetica = genetica, fechaOrigen = fechaOrigen)
                        listener?.onPlantaUpdated(plantaActualizada)
                    }
                    dialog.dismiss()
                }
            }
        }

        return dialog
    }

    companion object {
        const val TAG = "AddPlantaDialogFragment"
        private const val ARG_PLANTA = "planta"

        fun newInstance(planta: Planta? = null): AddPlantaDialogFragment {
            val fragment = AddPlantaDialogFragment()
            val args = Bundle()
            planta?.let { args.putParcelable(ARG_PLANTA, it) }
            fragment.arguments = args
            return fragment
        }
    }
}