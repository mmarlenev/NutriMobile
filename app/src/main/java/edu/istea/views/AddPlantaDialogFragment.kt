package edu.istea.views

import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.BundleCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import edu.istea.R
import edu.istea.dao.DBHelper
import edu.istea.model.Planta

class AddPlantaDialogFragment : DialogFragment() {

    private var planta: Planta? = null

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
        val tipoRadioGroup = view.findViewById<RadioGroup>(R.id.rg_tipo)
        val etapaSpinner = view.findViewById<Spinner>(R.id.spinner_etapa)
        val fechaOrigenDatePicker = view.findViewById<DatePicker>(R.id.dp_fecha_origen)

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.etapas_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            etapaSpinner.adapter = adapter
        }

        planta?.let { existingPlanta ->
            nombreEditText.setText(existingPlanta.nombre)
            when (existingPlanta.tipo) {
                "Autofloreciente" -> view.findViewById<RadioButton>(R.id.rb_autofloreciente).isChecked = true
                "Fotoperiódica" -> view.findViewById<RadioButton>(R.id.rb_fotoperiodica).isChecked = true
            }

            val etapas = resources.getStringArray(R.array.etapas_array)
            val etapaPosition = etapas.indexOf(existingPlanta.etapa)
            if (etapaPosition >= 0) {
                etapaSpinner.setSelection(etapaPosition)
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

                val selectedTipoId = tipoRadioGroup.checkedRadioButtonId
                if (selectedTipoId == -1) {
                    Toast.makeText(requireContext(), "Debe seleccionar un tipo de planta", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (dbHelper.plantaNombreExiste(nombre, planta?.id)) {
                    Toast.makeText(requireContext(), "Ya existe una planta con este nombre", Toast.LENGTH_SHORT).show()
                } else {
                    val tipo = view.findViewById<RadioButton>(selectedTipoId).text.toString()
                    val etapa = etapaSpinner.selectedItem.toString()

                    val day = fechaOrigenDatePicker.dayOfMonth
                    val month = fechaOrigenDatePicker.month
                    val year = fechaOrigenDatePicker.year
                    val fechaOrigen = "$day/${month + 1}/$year"

                    val result = Bundle()
                    if (planta == null) {
                        val nuevaPlanta = Planta(nombre = nombre, tipo = tipo, fechaOrigen = fechaOrigen, etapa = etapa)
                        result.putParcelable("planta", nuevaPlanta)
                        setFragmentResult("requestKey", result)
                    } else {
                        val plantaActualizada = planta!!.copy(nombre = nombre, tipo = tipo, fechaOrigen = fechaOrigen, etapa = etapa)
                        result.putParcelable("planta", plantaActualizada)
                        setFragmentResult("requestKey", result)
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