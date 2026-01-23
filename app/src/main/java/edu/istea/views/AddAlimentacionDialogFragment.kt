package edu.istea.views

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import edu.istea.R
import edu.istea.model.Alimentacion
import edu.istea.model.Insumo
import edu.istea.model.Planta
import java.util.Calendar

class AddAlimentacionDialogFragment(private val plantas: List<Planta>) : DialogFragment() {

    interface AddAlimentacionDialogListener {
        fun onAlimentacionAdded(alimentacion: Alimentacion)
    }

    private val insumos = listOf(
        Insumo("Tierra", "g"),
        Insumo("Agua", "ml"),
        Insumo("Fertilizante Vege", "ml"),
        Insumo("Jabón Potásico", "ml"),
        Insumo("Fertilizante Flora", "ml")
    )

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_add_alimentacion, null)

            val plantaSpinner = view.findViewById<Spinner>(R.id.spinner_planta_alimentacion)
            val fechaPicker = view.findViewById<DatePicker>(R.id.dp_fecha_alimentacion)
            val insumoSpinner = view.findViewById<Spinner>(R.id.spinner_insumo)
            val cantidadEditText = view.findViewById<EditText>(R.id.et_cantidad_insumo)
            val unidadTextView = view.findViewById<TextView>(R.id.tv_unidad_insumo)

            // Configurar Spinner de Plantas
            val plantaNombres = plantas.map { it.nombre }
            val plantaAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, plantaNombres)
            plantaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            plantaSpinner.adapter = plantaAdapter

            // Configurar Spinner de Insumos
            val insumoNombres = insumos.map { it.nombre }
            val insumoAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, insumoNombres)
            insumoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            insumoSpinner.adapter = insumoAdapter

            insumoSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    unidadTextView.text = insumos[position].unidad
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            builder.setView(view)
                .setPositiveButton("Añadir") { _, _ ->
                    val selectedPlanta = plantas[plantaSpinner.selectedItemPosition]
                    val insumo = insumos[insumoSpinner.selectedItemPosition]
                    val cantidad = cantidadEditText.text.toString().toFloatOrNull() ?: 0f
                    val day = fechaPicker.dayOfMonth
                    val month = fechaPicker.month
                    val year = fechaPicker.year
                    val calendar = Calendar.getInstance()
                    calendar.set(year, month, day)
                    val fecha = "${day}/${month + 1}/${year}"

                    val nuevaAlimentacion = Alimentacion(
                        plantaId = selectedPlanta.id,
                        plantaNombre = selectedPlanta.nombre,
                        fecha = fecha,
                        insumo = insumo.nombre,
                        cantidad = cantidad,
                        unidad = insumo.unidad
                    )
                    (activity as? AddAlimentacionDialogListener)?.onAlimentacionAdded(nuevaAlimentacion)
                }
                .setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}