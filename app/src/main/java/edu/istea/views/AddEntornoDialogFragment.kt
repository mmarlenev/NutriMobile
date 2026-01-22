package edu.istea.views

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import edu.istea.R
import edu.istea.dao.DBHelper
import edu.istea.model.Entorno
import edu.istea.model.Planta
import edu.istea.model.TipoMedicion
import java.util.Calendar

class AddEntornoDialogFragment : DialogFragment(), AddPlantaDialogFragment.PlantaDialogListener {

    interface AddEntornoDialogListener {
        fun onEntornoAdded(entorno: Entorno)
        fun onEntornoUpdated(entorno: Entorno)
    }

    private val ADD_PLANTA_OPTION = "+ Agregar planta"

    // Define measurement types and their input methods
    private val tiposMedicion = listOf(
        TipoMedicion("Acidez de Tierra", "ph", isNumeric = true),
        TipoMedicion("Temperatura de Tierra", "°C", isNumeric = true),
        TipoMedicion("Humedad de Tierra", "", isNumeric = false, levels = listOf("DRY+", "DRY", "NOR", "WET", "WET+")),
        TipoMedicion("Luz a Hoja", "", isNumeric = false, levels = listOf("LOW-", "LOW", "LOW+", "NOR-", "NOR", "NOR+", "HGH-", "HGH", "HGH+")),
        TipoMedicion("Humedad Ambiente", "%", isNumeric = true),
        TipoMedicion("Temperatura ambiente", "°C", isNumeric = true)
    )

    // Views
    private lateinit var plantaSpinner: Spinner
    private lateinit var fechaPicker: DatePicker
    private lateinit var tipoSpinner: Spinner
    private lateinit var valorEditText: EditText
    private lateinit var unidadTextView: TextView
    private lateinit var valorNivelSpinner: Spinner
    private lateinit var numericInputContainer: View

    // State
    private var entornoToEdit: Entorno? = null
    private var plantas: MutableList<Planta> = mutableListOf()
    private lateinit var dbHelper: DBHelper
    private var lastSelectedPlantaPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = DBHelper(requireContext())
        arguments?.let {
            entornoToEdit = it.getParcelable(ARG_ENTORNO)
            plantas = it.getParcelableArrayList<Planta>(ARG_PLANTAS)?.toMutableList() ?: mutableListOf()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_add_entorno, null)
        setupViews(view)
        setupPlantaSpinner()
        setupTipoSpinner()
        entornoToEdit?.let { setupForEditing(it) }

        val buttonText = if (entornoToEdit == null) "Añadir" else "Actualizar"
        return AlertDialog.Builder(requireContext())
            .setView(view)
            .setPositiveButton(buttonText, null) // Override in onStart
            .setNegativeButton(if (entornoToEdit == null) "Finalizar" else "Cancelar", null)
            .create()
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
                    plantaSpinner.setSelection(lastSelectedPlantaPosition) // Revert selection
                    AddPlantaDialogFragment.newInstance().apply {
                        setTargetFragment(this@AddEntornoDialogFragment, 0)
                        show(parentFragmentManager, AddPlantaDialogFragment.TAG)
                    }
                } else {
                    lastSelectedPlantaPosition = position
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupTipoSpinner() {
        val tipoNombres = tiposMedicion.map { it.nombre }
        val adapter = ArrayAdapter(requireContext(), R.layout.custom_spinner_item, tipoNombres)
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
        // Set Planta
        val plantaPos = plantas.indexOfFirst { it.id == entorno.plantaId }
        if (plantaPos != -1) {
            plantaSpinner.setSelection(plantaPos + 1)
            lastSelectedPlantaPosition = plantaPos + 1
        }

        // Set Tipo and Value
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

        // Set Date
        val dateParts = entorno.fecha.split("/")
        if (dateParts.size == 3) {
            fechaPicker.updateDate(dateParts[2].toInt(), dateParts[1].toInt() - 1, dateParts[0].toInt())
        }
    }

    override fun onStart() {
        super.onStart()
        (dialog as? AlertDialog)?.getButton(Dialog.BUTTON_POSITIVE)?.setOnClickListener { handlePositiveButtonClick() }
    }

    private fun handlePositiveButtonClick() {
        if (plantas.isEmpty() || plantaSpinner.selectedItemPosition == 0) {
            Toast.makeText(requireContext(), "Debe crear o seleccionar una planta", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedPlanta = plantas[plantaSpinner.selectedItemPosition - 1]
        val selectedTipo = tiposMedicion[tipoSpinner.selectedItemPosition]
        
        val valor = if (selectedTipo.isNumeric) {
            valorEditText.text.toString()
        } else {
            valorNivelSpinner.selectedItem.toString()
        }

        if (valor.isBlank()) {
            Toast.makeText(requireContext(), "El valor no puede estar vacío", Toast.LENGTH_SHORT).show()
            return
        }

        val fecha = "${fechaPicker.dayOfMonth}/${fechaPicker.month + 1}/${fechaPicker.year}"
        val listener = activity as? AddEntornoDialogListener

        val entorno = entornoToEdit?.copy(
            plantaId = selectedPlanta.id,
            plantaNombre = selectedPlanta.nombre,
            fecha = fecha,
            tipo = selectedTipo.nombre,
            valor = valor,
            unidad = selectedTipo.unidad
        ) ?: Entorno(
            plantaId = selectedPlanta.id,
            plantaNombre = selectedPlanta.nombre,
            fecha = fecha,
            tipo = selectedTipo.nombre,
            valor = valor,
            unidad = selectedTipo.unidad
        )

        if (entornoToEdit == null) {
            listener?.onEntornoAdded(entorno)
            // Reset for next entry
            tipoSpinner.setSelection(0)
            valorEditText.setText("")
            plantaSpinner.isEnabled = false
            fechaPicker.isEnabled = false
            Toast.makeText(requireContext(), "Medición añadida. Puede añadir otra.", Toast.LENGTH_SHORT).show()
        } else {
            listener?.onEntornoUpdated(entorno)
            dismiss()
        }
    }

    override fun onPlantaAdded(planta: Planta) {
        dbHelper.savePlanta(planta)
        this.plantas = dbHelper.getAllPlantas().toMutableList()
        updatePlantaSpinnerUI(planta.nombre)
        Toast.makeText(requireContext(), "Planta '${planta.nombre}' creada", Toast.LENGTH_SHORT).show()
    }

    override fun onPlantaUpdated(planta: Planta) {}

    private fun updatePlantaSpinnerUI(newPlantaNombre: String? = null) {
        val plantaNombres = plantas.map { it.nombre }.toMutableList().apply {
            add(0, ADD_PLANTA_OPTION)
        }
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