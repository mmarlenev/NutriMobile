package edu.istea.views

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.os.BundleCompat
import androidx.fragment.app.DialogFragment
import edu.istea.R
import edu.istea.model.Alimentacion
import edu.istea.model.Insumo
import edu.istea.model.Planta

class AddAlimentacionDialogFragment : DialogFragment() {

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

    private lateinit var plantaSpinner: Spinner
    private lateinit var fechaPicker: DatePicker
    private lateinit var insumoSpinner: Spinner
    private lateinit var cantidadEditText: EditText
    private lateinit var unidadTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            plantas = BundleCompat.getParcelableArrayList(it, ARG_PLANTAS, Planta::class.java)!!
            alimentacionToEdit = BundleCompat.getParcelable(it, ARG_ALIMENTACION, Alimentacion::class.java)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_add_alimentacion, null)
        setupViews(view)
        setupSpinners()
        alimentacionToEdit?.let { setupForEditing(it) }

        val builder = AlertDialog.Builder(requireContext()).setView(view)

        if (alimentacionToEdit == null) {
            builder.setPositiveButton("Guardar y Finalizar") { _, _ -> handleSave(true) }
            builder.setNeutralButton("Guardar y Añadir Otro", null)
        } else {
            builder.setPositiveButton("Actualizar") { _, _ -> handleSave(true) }
        }

        return builder.create()
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as? AlertDialog
        dialog?.let {
            val neutralButton = it.getButton(Dialog.BUTTON_NEUTRAL)
            neutralButton?.setOnClickListener {
                handleSave(false)
            }
        }
    }

    private fun handleSave(shouldDismiss: Boolean) {
        if (plantas.isEmpty()) {
            Toast.makeText(requireContext(), "Debe crear al menos una planta primero", Toast.LENGTH_SHORT).show()
            return
        }

        val cantidadStr = cantidadEditText.text.toString()
        if (cantidadStr.isBlank()) {
            Toast.makeText(requireContext(), "La cantidad no puede estar vacía", Toast.LENGTH_SHORT).show()
            return
        }
        val cantidad = cantidadStr.toFloatOrNull()
        if (cantidad == null || cantidad <= 0f) {
            Toast.makeText(requireContext(), "Por favor, ingrese una cantidad válida", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedPlanta = plantas[plantaSpinner.selectedItemPosition]
        val selectedInsumo = insumos[insumoSpinner.selectedItemPosition]
        val fecha = "${fechaPicker.dayOfMonth}/${fechaPicker.month + 1}/${fechaPicker.year}"
        val listener = activity as? AddAlimentacionDialogListener

        val alimentacion = alimentacionToEdit?.copy(
            plantaId = selectedPlanta.id,
            plantaNombre = selectedPlanta.nombre,
            fecha = fecha,
            insumo = selectedInsumo.nombre,
            cantidad = cantidad,
            unidad = selectedInsumo.unidad
        ) ?: Alimentacion(
            plantaId = selectedPlanta.id,
            plantaNombre = selectedPlanta.nombre,
            fecha = fecha,
            insumo = selectedInsumo.nombre,
            cantidad = cantidad,
            unidad = selectedInsumo.unidad
        )

        if (alimentacionToEdit == null) {
            listener?.onAlimentacionAdded(alimentacion)
        } else {
            listener?.onAlimentacionUpdated(alimentacion)
        }

        val toastMessage = when {
            alimentacionToEdit != null -> "Alimentación actualizada"
            shouldDismiss -> "Alimentación añadida"
            else -> "Alimentación añadida. Puede añadir otra."
        }
        Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_SHORT).show()

        if (shouldDismiss) {
            dismiss()
        } else {
            // Clear the quantity field
            cantidadEditText.setText("")

            // Advance to the next insumo type
            val currentPosition = insumoSpinner.selectedItemPosition
            val totalItems = insumoSpinner.adapter.count
            if (totalItems > 0) {
                val nextPosition = (currentPosition + 1) % totalItems
                insumoSpinner.setSelection(nextPosition)
            }

            // Request focus on the quantity field for faster typing
            cantidadEditText.requestFocus()
        }
    }

    private fun setupViews(view: View) {
        plantaSpinner = view.findViewById(R.id.spinner_planta_alimentacion)
        fechaPicker = view.findViewById(R.id.dp_fecha_alimentacion)
        insumoSpinner = view.findViewById(R.id.spinner_insumo)
        cantidadEditText = view.findViewById(R.id.et_cantidad_insumo)
        unidadTextView = view.findViewById(R.id.tv_unidad_insumo)
    }

    private fun setupSpinners() {
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
    }

    private fun setupForEditing(alimentacion: Alimentacion) {
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