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

class AddEntornoDialogFragment : DialogFragment() {

    interface AddEntornoDialogListener {
        fun onEntornoAdded(entorno: Entorno)
        fun onEntornoUpdated(entorno: Entorno)
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

    private var entornoToEdit: Entorno? = null
    private var plantas: List<Planta> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            entornoToEdit = it.getParcelable(ARG_ENTORNO)
            plantas = it.getParcelableArrayList(ARG_PLANTAS) ?: emptyList()
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

            entornoToEdit?.let { entorno ->
                val plantaPosition = plantas.indexOfFirst { it.id == entorno.plantaId }
                if (plantaPosition != -1) {
                    plantaSpinner.setSelection(plantaPosition)
                }
                val tipoPosition = tiposMedicion.indexOfFirst { it.nombre == entorno.tipo }
                if (tipoPosition != -1) {
                    tipoSpinner.setSelection(tipoPosition)
                }
                valorEditText.setText(entorno.valor)
                val dateParts = entorno.fecha.split("/")
                if (dateParts.size == 3) {
                    fechaPicker.updateDate(dateParts[2].toInt(), dateParts[1].toInt() - 1, dateParts[0].toInt())
                }
            }

            val buttonText = if (entornoToEdit == null) "Añadir" else "Actualizar"
            builder.setView(view)
                .setPositiveButton(buttonText, null) // Will be overridden
                .setNegativeButton("Cancelar") { dialog, _ ->
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
                    dismiss()
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
        }
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