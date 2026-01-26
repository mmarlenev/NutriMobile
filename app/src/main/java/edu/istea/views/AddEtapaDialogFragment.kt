package edu.istea.views

import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import edu.istea.R
import edu.istea.model.Etapa
import edu.istea.model.Planta
import java.util.Calendar

class AddEtapaDialogFragment() : DialogFragment() {

    interface AddEtapaDialogListener {
        fun onEtapaAdded(etapa: Etapa)
        fun onEtapaUpdated(etapa: Etapa)
    }

    private val etapasCicloVida = arrayOf("Germinación", "Plántula", "Vegetativo", "Floración", "Cosecha")
    private val sucesosCultivo = arrayOf("Poda Apical", "Poda LST", "Transplante", "Plaga Detectada", "Inicio de Secado", "Inicio de Curado")

    private var plantas: List<Planta> = emptyList()
    private var etapaToEdit: Etapa? = null

    private lateinit var tipoRegistroRadioGroup: RadioGroup
    private lateinit var estadoSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            plantas = it.getParcelableArrayList(ARG_PLANTAS)!!
            etapaToEdit = it.getParcelable(ARG_ETAPA)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_add_etapa, null)

            val plantaSpinner = view.findViewById<Spinner>(R.id.spinner_planta_etapa)
            tipoRegistroRadioGroup = view.findViewById(R.id.rg_tipo_registro)
            estadoSpinner = view.findViewById(R.id.spinner_estado_etapa)
            val fechaDatePicker = view.findViewById<DatePicker>(R.id.dp_fecha_etapa)

            val plantaNombres = plantas.map { it.nombre }
            plantaSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, plantaNombres).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

            tipoRegistroRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                updateEstadoSpinner(checkedId)
            }

            etapaToEdit?.let {
                setupForEditing(it, plantaSpinner, fechaDatePicker)
            } ?: run {
                view.findViewById<RadioButton>(R.id.rb_etapa).isChecked = true
            }

            val buttonText = if (etapaToEdit == null) "Añadir" else "Actualizar"
            builder.setView(view)
                .setPositiveButton(buttonText) { _, _ ->
                    val selectedPlanta = plantas[plantaSpinner.selectedItemPosition]
                    val selectedEstado = estadoSpinner.selectedItem.toString()
                    val tipo = if (tipoRegistroRadioGroup.checkedRadioButtonId == R.id.rb_etapa) "Etapa" else "Suceso"
                    val estadoFinal = "$tipo: $selectedEstado"
                    val fecha = "${fechaDatePicker.dayOfMonth}/${fechaDatePicker.month + 1}/${fechaDatePicker.year}"

                    val listener = activity as? AddEtapaDialogListener

                    if (etapaToEdit == null) {
                        val nuevaEtapa = Etapa(plantaId = selectedPlanta.id, plantaNombre = selectedPlanta.nombre, estado = estadoFinal, fecha = fecha)
                        listener?.onEtapaAdded(nuevaEtapa)
                    } else {
                        val etapaActualizada = etapaToEdit!!.copy(
                            plantaId = selectedPlanta.id,
                            plantaNombre = selectedPlanta.nombre,
                            estado = estadoFinal,
                            fecha = fecha
                        )
                        listener?.onEtapaUpdated(etapaActualizada)
                    }
                }
                .setNegativeButton("Cancelar", null)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun setupForEditing(etapa: Etapa, plantaSpinner: Spinner, fechaDatePicker: DatePicker) {
        val plantaPos = plantas.indexOfFirst { it.id == etapa.plantaId }
        if (plantaPos != -1) plantaSpinner.setSelection(plantaPos)

        val parts = etapa.estado.split(": ")
        val tipo = parts.getOrNull(0)
        val valor = parts.getOrNull(1)

        val checkedId = if (tipo == "Etapa") R.id.rb_etapa else R.id.rb_suceso
        tipoRegistroRadioGroup.check(checkedId)
        updateEstadoSpinner(checkedId)

        val listToUse = if (tipo == "Etapa") etapasCicloVida else sucesosCultivo
        val estadoPos = listToUse.indexOf(valor)
        if (estadoPos != -1) estadoSpinner.setSelection(estadoPos)
        
        val dateParts = etapa.fecha.split("/")
        if (dateParts.size == 3) {
            fechaDatePicker.updateDate(dateParts[2].toInt(), dateParts[1].toInt() - 1, dateParts[0].toInt())
        }
    }

    private fun updateEstadoSpinner(checkedId: Int) {
        val listToUse = if (checkedId == R.id.rb_etapa) etapasCicloVida else sucesosCultivo
        estadoSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listToUse).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    companion object {
        private const val ARG_PLANTAS = "plantas_list"
        private const val ARG_ETAPA = "etapa_to_edit"

        fun newInstance(plantas: List<Planta>, etapa: Etapa? = null): AddEtapaDialogFragment {
            val fragment = AddEtapaDialogFragment()
            val args = Bundle()
            args.putParcelableArrayList(ARG_PLANTAS, ArrayList(plantas))
            args.putParcelable(ARG_ETAPA, etapa)
            fragment.arguments = args
            return fragment
        }
    }
}