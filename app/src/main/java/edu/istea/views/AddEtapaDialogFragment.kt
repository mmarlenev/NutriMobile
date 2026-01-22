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

class AddEtapaDialogFragment(private val plantas: List<Planta>) : DialogFragment() {

    interface AddEtapaDialogListener {
        fun onEtapaAdded(etapa: Etapa)
    }

    private val etapasCicloVida = arrayOf("Germinación", "Plántula", "Vegetativo", "Floración", "Cosecha")
    private val sucesosCultivo = arrayOf("Poda Apical", "Poda LST", "Transplante", "Plaga Detectada", "Inicio de Secado", "Inicio de Curado")

    private lateinit var tipoRegistroRadioGroup: RadioGroup
    private lateinit var estadoSpinner: Spinner

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.dialog_add_etapa, null)

            val plantaSpinner = view.findViewById<Spinner>(R.id.spinner_planta_etapa)
            tipoRegistroRadioGroup = view.findViewById(R.id.rg_tipo_registro)
            estadoSpinner = view.findViewById(R.id.spinner_estado_etapa)
            val fechaDatePicker = view.findViewById<DatePicker>(R.id.dp_fecha_etapa)

            // Plantas Spinner
            val plantaNombres = plantas.map { it.nombre }
            plantaSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, plantaNombres).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

            // Logic for RadioGroup
            tipoRegistroRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                updateEstadoSpinner(checkedId)
            }
            
            // Set initial state for the spinner
            view.findViewById<RadioButton>(R.id.rb_etapa).isChecked = true

            builder.setView(view)
                .setPositiveButton("Añadir") { _, _ ->
                    val selectedPlanta = plantas[plantaSpinner.selectedItemPosition]
                    val selectedEstado = estadoSpinner.selectedItem.toString()
                    
                    val tipo = if (tipoRegistroRadioGroup.checkedRadioButtonId == R.id.rb_etapa) "Etapa" else "Suceso"
                    val estadoFinal = "$tipo: $selectedEstado"

                    val day = fechaDatePicker.dayOfMonth
                    val month = fechaDatePicker.month
                    val year = fechaDatePicker.year
                    val fecha = "${day}/${month + 1}/${year}"

                    val nuevaEtapa = Etapa(plantaId = selectedPlanta.id, plantaNombre = selectedPlanta.nombre, estado = estadoFinal, fecha = fecha)
                    (activity as? AddEtapaDialogListener)?.onEtapaAdded(nuevaEtapa)
                }
                .setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun updateEstadoSpinner(checkedId: Int) {
        val listToUse = if (checkedId == R.id.rb_etapa) {
            etapasCicloVida
        } else {
            sucesosCultivo
        }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listToUse)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        estadoSpinner.adapter = adapter
    }
}