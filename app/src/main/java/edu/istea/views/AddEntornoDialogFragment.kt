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

    private var entornoToEdit: Entorno? = null
    private var plantas: MutableList<Planta> = mutableListOf()
    private lateinit var dbHelper: DBHelper
    private var lastSelectedPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = DBHelper(requireContext())
        arguments?.let {
            entornoToEdit = it.getParcelable(ARG_ENTORNO)
            plantas = it.getParcelableArrayList<Planta>(ARG_PLANTAS)?.toMutableList() ?: mutableListOf()
        }
    }

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

            updatePlantaSpinner()

            plantaSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (position == 0) {
                        plantaSpinner.setSelection(lastSelectedPosition) // Revert selection
                        val dialog = AddPlantaDialogFragment.newInstance()
                        dialog.setTargetFragment(this@AddEntornoDialogFragment, 0)
                        dialog.show(parentFragmentManager, AddPlantaDialogFragment.TAG)
                    } else {
                        lastSelectedPosition = position
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

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

            entornoToEdit?.let { setupForEditing(it) }

            val buttonText = if (entornoToEdit == null) "Añadir" else "Actualizar"
            builder.setView(view)
                .setPositiveButton(buttonText, null)
                .setNegativeButton(if (entornoToEdit == null) "Finalizar" else "Cancelar", null)
                .create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun updatePlantaSpinner(newPlantaNombre: String? = null) {
        val plantaNombres = plantas.map { it.nombre }.toMutableList()
        plantaNombres.add(0, ADD_PLANTA_OPTION)
        val plantaAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, plantaNombres)
        plantaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        plantaSpinner.adapter = plantaAdapter

        val positionToSelect = if (newPlantaNombre != null) {
            plantaNombres.indexOf(newPlantaNombre)
        } else {
            1 // Default to the first actual planta if available
        }

        if (positionToSelect != -1 && positionToSelect < plantaNombres.size) {
            plantaSpinner.setSelection(positionToSelect)
            lastSelectedPosition = positionToSelect
        }
    }

    private fun setupForEditing(entorno: Entorno) {
        val plantaPosition = plantas.indexOfFirst { it.id == entorno.plantaId }
        if (plantaPosition != -1) {
            plantaSpinner.setSelection(plantaPosition + 1) // Add 1 to account for the "Add" option
            lastSelectedPosition = plantaPosition + 1
        }

        val tipoPosition = tiposMedicion.indexOfFirst { it.nombre == entorno.tipo }
        if (tipoPosition != -1) tipoSpinner.setSelection(tipoPosition)

        valorEditText.setText(entorno.valor)

        val dateParts = entorno.fecha.split("/")
        if (dateParts.size == 3) {
            fechaPicker.updateDate(dateParts[2].toInt(), dateParts[1].toInt() - 1, dateParts[0].toInt())
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as? AlertDialog
        dialog?.getButton(Dialog.BUTTON_POSITIVE)?.setOnClickListener { handlePositiveButtonClick() }
    }

    private fun handlePositiveButtonClick() {
        if (plantas.isEmpty()) {
            Toast.makeText(requireContext(), "Debe crear o seleccionar una planta primero", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedIndex = plantaSpinner.selectedItemPosition - 1 // Adjust for "Add" option
        if (selectedIndex < 0) return
        
        val selectedPlanta = plantas[selectedIndex]
        val tipo = tiposMedicion[tipoSpinner.selectedItemPosition]
        val valor = valorEditText.text.toString()

        if (valor.isBlank()) {
            Toast.makeText(requireContext(), "El valor no puede estar vacío", Toast.LENGTH_SHORT).show()
            return
        }

        val day = fechaPicker.dayOfMonth
        val month = fechaPicker.month
        val year = fechaPicker.year
        val fecha = "${day}/${month + 1}/${year}"

        if (entornoToEdit == null) {
            val nuevoEntorno = Entorno(
                plantaId = selectedPlanta.id,
                plantaNombre = selectedPlanta.nombre,
                fecha = fecha,
                tipo = tipo.nombre,
                valor = valor,
                unidad = tipo.unidad
            )
            (activity as? AddEntornoDialogListener)?.onEntornoAdded(nuevoEntorno)
            valorEditText.text.clear()
            tipoSpinner.setSelection(0)
            valorEditText.requestFocus()
            plantaSpinner.isEnabled = false
            fechaPicker.isEnabled = false
            Toast.makeText(requireContext(), "Medición añadida. Puede añadir otra.", Toast.LENGTH_SHORT).show()
        } else {
            val entornoActualizado = entornoToEdit!!.copy(
                plantaId = selectedPlanta.id,
                plantaNombre = selectedPlanta.nombre,
                fecha = fecha,
                tipo = tipo.nombre,
                valor = valor,
                unidad = tipo.unidad
            )
            (activity as? AddEntornoDialogListener)?.onEntornoUpdated(entornoActualizado)
            dismiss()
        }
    }

    override fun onPlantaAdded(planta: Planta) {
        dbHelper.savePlanta(planta)
        this.plantas = dbHelper.getAllPlantas().toMutableList()
        updatePlantaSpinner(planta.nombre)
        Toast.makeText(requireContext(), "Planta '${planta.nombre}' creada", Toast.LENGTH_SHORT).show()
    }

    override fun onPlantaUpdated(planta: Planta) {}

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