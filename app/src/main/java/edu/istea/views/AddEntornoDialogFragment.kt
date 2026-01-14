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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_add_entorno, null)

            val plantaSpinner = view.findViewById<Spinner>(R.id.spinner_planta_entorno)
            val fechaPicker = view.findViewById<DatePicker>(R.id.dp_fecha_entorno)
            val tipoSpinner = view.findViewById<Spinner>(R.id.spinner_tipo_medicion)
            val valorEditText = view.findViewById<EditText>(R.id.et_valor_medicion)
            val unidadTextView = view.findViewById<TextView>(R.id.tv_unidad_medicion)

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
                .setPositiveButton("Añadir") { _, _ ->
                    val selectedPlanta = plantas[plantaSpinner.selectedItemPosition]
                    val tipo = tiposMedicion[tipoSpinner.selectedItemPosition]
                    val valor = valorEditText.text.toString()
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
                }
                .setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}