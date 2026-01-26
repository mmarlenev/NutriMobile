package edu.istea.views

import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.os.BundleCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import edu.istea.R
import edu.istea.dao.DBHelper
import edu.istea.model.Entorno
import edu.istea.model.Planta
import edu.istea.model.TipoMedicion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddEntornoDialogFragment : DialogFragment() {

    interface AddEntornoDialogListener {
        fun onDialogDataChanged()
    }

    private val addPlantaOption = "+ Agregar planta"

    private val tiposMedicion = listOf(
        TipoMedicion("Acidez de Tierra", "ph", isNumeric = true),
        TipoMedicion("Temperatura de Tierra", "°C", isNumeric = true),
        TipoMedicion("Humedad de Tierra", "", isNumeric = false, levels = listOf("DRY+", "DRY", "NOR", "WET", "WET+")),
        TipoMedicion("Luz a Hoja", "", isNumeric = false, levels = listOf("LOW-", "LOW", "LOW+", "NOR-", "NOR", "NOR+", "HGH-", "HGH", "HGH+")),
        TipoMedicion("Humedad Ambiente", "%", isNumeric = true),
        TipoMedicion("Temperatura ambiente", "°C", isNumeric = true)
    )

    private lateinit var plantaSpinner: Spinner
    private lateinit var fechaPicker: DatePicker
    private lateinit var tipoSpinner: Spinner
    private lateinit var valorEditText: EditText
    private lateinit var unidadTextView: TextView
    private lateinit var valorNivelSpinner: Spinner
    private lateinit var numericInputContainer: View

    private var entornoToEdit: Entorno? = null
    private var plantas: MutableList<Planta> = mutableListOf()
    private lateinit var dbHelper: DBHelper
    private var lastSelectedPlantaPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = DBHelper(requireContext())
        arguments?.let {
            entornoToEdit = BundleCompat.getParcelable(it, ARG_ENTORNO, Entorno::class.java)
            plantas = BundleCompat.getParcelableArrayList(it, ARG_PLANTAS, Planta::class.java)?.toMutableList() ?: mutableListOf()
        }

        setFragmentResultListener("requestKey") { _, bundle ->
            val planta = BundleCompat.getParcelable(bundle, "planta", Planta::class.java)
            if (planta != null) {
                onPlantaAdded(planta)
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_add_entorno, null)
        setupViews(view)
        setupPlantaSpinner()
        setupTipoSpinner()
        entornoToEdit?.let { setupForEditing(it) }

        val builder = AlertDialog.Builder(requireContext()).setView(view)

        if (entornoToEdit == null) {
            builder.setPositiveButton("Guardar y Finalizar") { _, _ -> handleSave(true) }
            builder.setNeutralButton("Guardar y Añadir Otro", null) // Listener nulo para evitar cierre
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
        if (plantas.isEmpty() || plantaSpinner.selectedItemPosition == 0) {
            Toast.makeText(requireContext(), "Debe crear o seleccionar una planta", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedPlanta = plantas[plantaSpinner.selectedItemPosition - 1]
        val selectedTipo = tiposMedicion[tipoSpinner.selectedItemPosition]

        val valorString = if (selectedTipo.isNumeric) {
            valorEditText.text.toString()
        } else {
            valorNivelSpinner.selectedItem.toString()
        }

        if (valorString.isBlank()) {
            Toast.makeText(requireContext(), "El valor no puede estar vacío", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedTipo.isNumeric) {
            try {
                valorString.toDouble()
            } catch (_: NumberFormatException) {
                Toast.makeText(requireContext(), "Por favor, ingrese un número válido para '${selectedTipo.nombre}'", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val fecha = "${fechaPicker.dayOfMonth}/${fechaPicker.month + 1}/${fechaPicker.year}"

        val entorno = entornoToEdit?.copy(
            plantaId = selectedPlanta.id, plantaNombre = selectedPlanta.nombre, fecha = fecha,
            tipo = selectedTipo.nombre, valor = valorString, unidad = selectedTipo.unidad
        ) ?: Entorno(
            plantaId = selectedPlanta.id, plantaNombre = selectedPlanta.nombre, fecha = fecha,
            tipo = selectedTipo.nombre, valor = valorString, unidad = selectedTipo.unidad
        )

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                if (entornoToEdit == null) {
                    dbHelper.saveEntorno(entorno)
                } else {
                    dbHelper.updateEntorno(entorno)
                }
            }
            
            (activity as? AddEntornoDialogListener)?.onDialogDataChanged()

            val toastMessage = when {
                entornoToEdit != null -> "Medición actualizada"
                shouldDismiss -> "Medición añadida"
                else -> "Medición añadida. Puede añadir otra."
            }
            Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_SHORT).show()

            if (shouldDismiss) {
                dismiss()
            } else {
                tipoSpinner.setSelection(0)
                valorEditText.setText("")
            }
        }
    }

    private fun setupViews(view: View) {
        plantaSpinner = view.findViewById(R.id.spinner_planta_entorno)
        fechaPicker = view.findViewById(R.id.dp_fecha_entorno)
        tipoSpinner = view.findViewById(R.id.spinner_tipo_medicion)
        valorEditText = view.findViewById(R.id.et_valor_medicion)
        unidadTextView = view.findViewById(R.id.tv_unidad_medicion)
        valorNivelSpinner = view.findViewById(R.id.spinner_valor_nivel)
        numericInputContainer = view.findViewById(R.id.numeric_input_container)
    }

    private fun setupPlantaSpinner() {
        updatePlantaSpinnerUI()
        plantaSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (position == 0) {
                    plantaSpinner.setSelection(lastSelectedPlantaPosition)
                    AddPlantaDialogFragment.newInstance().show(parentFragmentManager, AddPlantaDialogFragment.TAG)
                } else {
                    lastSelectedPlantaPosition = position
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupTipoSpinner() {
        val adapter = ArrayAdapter(requireContext(), R.layout.custom_spinner_item, tiposMedicion.map { it.nombre })
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
        tipoSpinner.adapter = adapter
        tipoSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateInputForMeasurementType(tiposMedicion[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun updateInputForMeasurementType(tipo: TipoMedicion) {
        if (tipo.isNumeric) {
            numericInputContainer.visibility = View.VISIBLE
            valorNivelSpinner.visibility = View.GONE
            unidadTextView.text = tipo.unidad
            valorEditText.setText("")
            valorEditText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        } else {
            numericInputContainer.visibility = View.GONE
            valorNivelSpinner.visibility = View.VISIBLE
            val adapter = ArrayAdapter(requireContext(), R.layout.custom_spinner_item, tipo.levels!!)
            adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
            valorNivelSpinner.adapter = adapter
            unidadTextView.text = ""
        }
    }

    private fun setupForEditing(entorno: Entorno) {
        val plantaPos = plantas.indexOfFirst { it.id == entorno.plantaId }
        if (plantaPos != -1) {
            plantaSpinner.setSelection(plantaPos + 1)
            lastSelectedPlantaPosition = plantaPos + 1
        }

        val tipoPos = tiposMedicion.indexOfFirst { it.nombre == entorno.tipo }
        if (tipoPos != -1) {
            tipoSpinner.setSelection(tipoPos)
            val tipo = tiposMedicion[tipoPos]
            updateInputForMeasurementType(tipo)
            if (tipo.isNumeric) {
                valorEditText.setText(entorno.valor)
            } else {
                val levelPos = tipo.levels!!.indexOf(entorno.valor)
                if (levelPos != -1) valorNivelSpinner.setSelection(levelPos)
            }
        }

        val dateParts = entorno.fecha.split("/")
        if (dateParts.size == 3) {
            fechaPicker.updateDate(dateParts[2].toInt(), dateParts[1].toInt() - 1, dateParts[0].toInt())
        }
    }

    private fun onPlantaAdded(planta: Planta) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                dbHelper.savePlanta(planta)
                plantas = dbHelper.getAllPlantas().toMutableList()
            }
            updatePlantaSpinnerUI(planta.nombre)
            Toast.makeText(requireContext(), "Planta '${planta.nombre}' creada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updatePlantaSpinnerUI(newPlantaNombre: String? = null) {
        val plantaNombres = plantas.map { it.nombre }.toMutableList().apply { add(0, addPlantaOption) }
        val adapter = ArrayAdapter(requireContext(), R.layout.custom_spinner_item, plantaNombres)
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
        plantaSpinner.adapter = adapter

        val position = newPlantaNombre?.let { plantaNombres.indexOf(it) } ?: if (plantas.isNotEmpty()) 1 else 0
        plantaSpinner.setSelection(position)
        lastSelectedPlantaPosition = position
    }

    companion object {
        private const val ARG_ENTORNO = "entorno_to_edit"
        private const val ARG_PLANTAS = "plantas_list"
        const val TAG = "AddEntornoDialogFragment"

        fun newInstance(plantas: List<Planta>, entorno: Entorno? = null): AddEntornoDialogFragment {
            val fragment = AddEntornoDialogFragment()
            val args = Bundle()
            args.putParcelable(ARG_ENTORNO, entorno)
            args.putParcelableArrayList(ARG_PLANTAS, ArrayList(plantas))
            fragment.arguments = args
            return fragment
        }
    }
}