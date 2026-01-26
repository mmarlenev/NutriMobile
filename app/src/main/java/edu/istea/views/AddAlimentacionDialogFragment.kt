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

class AddAlimentacionDialogFragment() : DialogFragment() {

    interface AddAlimentacionDialogListener {
        fun onAlimentacionAdded(alimentacion: Alimentacion)
        fun onAlimentacionUpdated(alimentacion: Alimentacion)
    }

    private var plantas: List<Planta> = emptyList()
    private var alimentacionToEdit: Alimentacion? = null

    private val insumos = listOf(
        Insumo("Tierra", "g"),
        Insumo("Agua", "ml"),
        Insumo("Fertilizante Vege", "ml"),
        Insumo("Jabón Potásico", "ml"),
        Insumo("Fertilizante Flora", "ml")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            plantas = it.getParcelableArrayList(ARG_PLANTAS)!!
            alimentacionToEdit = it.getParcelable(ARG_ALIMENTACION)
        }
    }

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

            val plantaNombres = plantas.map { it.nombre }
            plantaSpinner.adapter = ArrayAdapter(requireContext(), R.layout.custom_spinner_item, plantaNombres).apply {
                setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
            }

            val insumoNombres = insumos.map { it.nombre }
            insumoSpinner.adapter = ArrayAdapter(requireContext(), R.layout.custom_spinner_item, insumoNombres).apply {
                setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
            }

            insumoSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    unidadTextView.text = insumos[position].unidad
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            alimentacionToEdit?.let {
                setupForEditing(it, plantaSpinner, insumoSpinner, cantidadEditText, fechaPicker)
            }

            val buttonText = if (alimentacionToEdit == null) "Añadir" else "Actualizar"

            builder.setView(view)
                .setPositiveButton(buttonText) { _, _ ->
                    val selectedPlanta = plantas[plantaSpinner.selectedItemPosition]
                    val insumo = insumos[insumoSpinner.selectedItemPosition]
                    val cantidad = cantidadEditText.text.toString().toFloatOrNull() ?: 0f
                    val fecha = "${fechaPicker.dayOfMonth}/${fechaPicker.month + 1}/${fechaPicker.year}"
                    
                    val listener = activity as? AddAlimentacionDialogListener
                    if (alimentacionToEdit == null) {
                        val nuevaAlimentacion = Alimentacion(
                            plantaId = selectedPlanta.id,
                            plantaNombre = selectedPlanta.nombre,
                            fecha = fecha,
                            insumo = insumo.nombre,
                            cantidad = cantidad,
                            unidad = insumo.unidad
                        )
                        listener?.onAlimentacionAdded(nuevaAlimentacion)
                    } else {
                        val alimentacionActualizada = alimentacionToEdit!!.copy(
                            plantaId = selectedPlanta.id,
                            plantaNombre = selectedPlanta.nombre,
                            fecha = fecha,
                            insumo = insumo.nombre,
                            cantidad = cantidad,
                            unidad = insumo.unidad
                        )
                        listener?.onAlimentacionUpdated(alimentacionActualizada)
                    }
                }
                .setNegativeButton("Cancelar", null)
                .create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun setupForEditing(alimentacion: Alimentacion, plantaSpinner: Spinner, insumoSpinner: Spinner, cantidadEditText: EditText, fechaPicker: DatePicker) {
        val plantaPos = plantas.indexOfFirst { it.id == alimentacion.plantaId }
        if (plantaPos != -1) plantaSpinner.setSelection(plantaPos)

        val insumoPos = insumos.indexOfFirst { it.nombre == alimentacion.insumo }
        if (insumoPos != -1) insumoSpinner.setSelection(insumoPos)

        cantidadEditText.setText(alimentacion.cantidad.toString())

        val dateParts = alimentacion.fecha.split("/")
        if (dateParts.size == 3) {
            fechaPicker.updateDate(dateParts[2].toInt(), dateParts[1].toInt() - 1, dateParts[0].toInt())
        }
    }

    companion object {
        private const val ARG_PLANTAS = "plantas_list"
        private const val ARG_ALIMENTACION = "alimentacion_to_edit"

        fun newInstance(plantas: List<Planta>, alimentacion: Alimentacion? = null): AddAlimentacionDialogFragment {
            val fragment = AddAlimentacionDialogFragment()
            val args = Bundle()
            args.putParcelableArrayList(ARG_PLANTAS, ArrayList(plantas))
            args.putParcelable(ARG_ALIMENTACION, alimentacion)
            fragment.arguments = args
            return fragment
        }
    }
}