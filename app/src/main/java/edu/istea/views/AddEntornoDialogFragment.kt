package edu.istea.views

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import edu.istea.R
import edu.istea.model.Entorno
import edu.istea.model.Planta
import edu.istea.model.TipoMedicion
import java.util.Calendar

class AddEntornoDialogFragment(private val plantas: List<Planta>) : DialogFragment() {

    interface AddEntornoDialogListener {
        fun onEntornoAdded(entorno: Entorno)
    }

    private val tiposMedicion = listOf(
        TipoMedicion("Acidez de Tierra", "ph"),
        TipoMedicion("Temperatura de Tierra", "°C"),
        TipoMedicion("Humedad de Tierra", "%"),
        TipoMedicion("Luz a Hoja", "lux"),
        TipoMedicion("Humedad Ambiente", "%"),
        TipoMedicion("Temperatura ambiente", "°C")
    )

    private lateinit var plantaSpinner: Spinner
    private lateinit var fechaPicker: DatePicker
    private lateinit var tipoSpinner: Spinner
    private lateinit var valorEditText: EditText
    private lateinit var unidadTextView: TextView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_add_entorno, null)

            plantaSpinner = view.findViewById(R.id.spinner_planta_entorno)
            fechaPicker = view.findViewById(R.id.dp_fecha_entorno)
            tipoSpinner = view.findViewById(R.id.spinner_tipo_medicion)
            valorEditText = view.findViewById(R.id.et_valor_medicion)
            unidadTextView = view.findViewById(R.id.tv_unidad_medicion)

            // Configurar Spinner de Plantas
            val plantaNombres = plantas.map { it.nombre }
            val plantaAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, plantaNombres)
            plantaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            plantaSpinner.adapter = plantaAdapter

            // Configurar Spinner de Tipos de Medición
            val tipoNombres = tiposMedicion.map { it.nombre }
            val tipoAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, tipoNombres)
            tipoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            tipoSpinner.adapter = tipoAdapter

            tipoSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    unidadTextView.text = tiposMedicion[position].unidad
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            builder.setView(view)
                .setPositiveButton("Añadir", null) // Will be overridden
                .setNegativeButton("Finalizar") { dialog, _ ->
                    dialog.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as? AlertDialog
        dialog?.let {
            val positiveButton = it.getButton(Dialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener { 
                val selectedPlanta = plantas[plantaSpinner.selectedItemPosition]
                val tipo = tiposMedicion[tipoSpinner.selectedItemPosition]
                val valor = valorEditText.text.toString()

                if (valor.isBlank()) {
                    Toast.makeText(requireContext(), "El valor no puede estar vacío", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val day = fechaPicker.dayOfMonth
                val month = fechaPicker.month
                val year = fechaPicker.year
                val calendar = Calendar.getInstance()
                calendar.set(year, month, day)
                val fecha = "${day}/${month + 1}/${year}"

                val nuevoEntorno = Entorno(
                    plantaId = selectedPlanta.id,
                    plantaNombre = selectedPlanta.nombre,
                    fecha = fecha,
                    tipo = tipo.nombre,
                    valor = valor,
                    unidad = tipo.unidad
                )
                (activity as? AddEntornoDialogListener)?.onEntornoAdded(nuevoEntorno)

                // Clear the value and set focus
                valorEditText.text.clear()
                tipoSpinner.setSelection(0) // Reset to first item
                tipoSpinner.requestFocus()

                // Disable plant and date pickers after first entry
                plantaSpinner.isEnabled = false
                fechaPicker.isEnabled = false

                Toast.makeText(requireContext(), "Medición añadida. Puede añadir otra.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}