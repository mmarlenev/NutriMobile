package edu.istea.views

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.os.BundleCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import edu.istea.R
import edu.istea.dao.DBHelper
import edu.istea.model.Evento
import edu.istea.model.Planta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddEventoDialogFragment : DialogFragment() {

    interface AddEventoDialogListener {
        fun onDialogDataChanged()
    }

    private lateinit var sujetoSpinner: Spinner
    private lateinit var sucesoSpinner: Spinner
    private lateinit var plantaSpinner: Spinner
    private lateinit var potenciaLuzSpinner: Spinner
    private lateinit var ventiladorModoSpinner: Spinner
    private lateinit var mantenimientoSpinner: Spinner
    private lateinit var fechaPicker: DatePicker
    private lateinit var plantaLabel: TextView
    private lateinit var sucesoLabel: TextView
    private lateinit var potenciaLuzLabel: TextView
    private lateinit var ventiladorModoLabel: TextView
    private lateinit var mantenimientoLabel: TextView
    private lateinit var dbHelper: DBHelper
    private var plantas: MutableList<Planta> = mutableListOf()
    private var eventoToEdit: Evento? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = DBHelper(requireContext())
        arguments?.let {
            eventoToEdit = BundleCompat.getParcelable(it, ARG_EVENTO, Evento::class.java)
            plantas = BundleCompat.getParcelableArrayList(it, ARG_PLANTAS, Planta::class.java)?.toMutableList() ?: mutableListOf()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_add_evento, null)
        setupViews(view)
        setupSujetoSpinner()
        setupPlantaSpinner()
        setupPotenciaLuzSpinner()
        setupVentiladorModoSpinner()
        setupMantenimientoSpinner()
        eventoToEdit?.let { setupForEditing(it) }

        val builder = AlertDialog.Builder(requireContext()).setView(view)

        if (eventoToEdit == null) {
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
            if (eventoToEdit == null) {
                val selectedSujeto = sujetoSpinner.selectedItem.toString()
                neutralButton?.visibility = if (selectedSujeto == "Plantas") View.VISIBLE else View.GONE
            }
        }
    }

    private fun handleSave(shouldDismiss: Boolean) {
        val sujeto = sujetoSpinner.selectedItem.toString()
        var suceso = sucesoSpinner.selectedItem.toString()
        val fecha = "${fechaPicker.dayOfMonth}/${fechaPicker.month + 1}/${fechaPicker.year}"

        if (sujeto == "Lugar de Cultivo") {
            when (suceso) {
                "Mantenimiento" -> suceso += " - ${mantenimientoSpinner.selectedItem}"
                "Potencia Luz (JX150)" -> suceso += " - ${potenciaLuzSpinner.selectedItem}"
                "Ventilador / Extracción" -> suceso += " - ${ventiladorModoSpinner.selectedItem}"
            }
        }

        var plantaId: Int? = null
        if (sujeto == "Plantas") {
            if (plantas.isNotEmpty()) {
                plantaId = plantas[plantaSpinner.selectedItemPosition].id
            } else {
                Toast.makeText(requireContext(), "No hay plantas para seleccionar", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val evento = eventoToEdit?.copy(
            sujeto = sujeto, suceso = suceso, fecha = fecha, plantaId = plantaId
        ) ?: Evento(
            sujeto = sujeto, suceso = suceso, fecha = fecha, plantaId = plantaId
        )

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                if (eventoToEdit == null) {
                    dbHelper.saveEvento(evento)
                } else {
                    dbHelper.updateEvento(evento)
                }
            }

            (activity as? AddEventoDialogListener)?.onDialogDataChanged()

            val toastMessage = when {
                eventoToEdit != null -> "Evento actualizado"
                shouldDismiss -> "Evento añadido"
                else -> "Evento añadido. Puede añadir otro."
            }
            Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_SHORT).show()

            if (shouldDismiss) {
                dismiss()
            } else {
                resetForm()
            }
        }
    }

    private fun resetForm() {
        sucesoSpinner.setSelection(0)
    }

    private fun setupViews(view: View) {
        sujetoSpinner = view.findViewById(R.id.spinner_sujeto_evento)
        sucesoSpinner = view.findViewById(R.id.spinner_suceso_evento)
        plantaSpinner = view.findViewById(R.id.spinner_planta_evento)
        potenciaLuzSpinner = view.findViewById(R.id.spinner_potencia_luz)
        ventiladorModoSpinner = view.findViewById(R.id.spinner_ventilador_modo)
        mantenimientoSpinner = view.findViewById(R.id.spinner_mantenimiento)
        fechaPicker = view.findViewById(R.id.dp_fecha_evento)
        plantaLabel = view.findViewById(R.id.tv_planta_evento_label)
        sucesoLabel = view.findViewById(R.id.tv_suceso_evento_label)
        potenciaLuzLabel = view.findViewById(R.id.tv_potencia_luz_label)
        ventiladorModoLabel = view.findViewById(R.id.tv_ventilador_modo_label)
        mantenimientoLabel = view.findViewById(R.id.tv_mantenimiento_label)
    }

    private fun setupSujetoSpinner() {
        val sujetoOptions = arrayOf("Plantas", "Lugar de Cultivo")
        val adapter = ArrayAdapter(requireContext(), R.layout.custom_spinner_item, sujetoOptions)
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
        sujetoSpinner.adapter = adapter
        sujetoSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedSujeto = sujetoOptions[position]
                updateSucesoSpinner(selectedSujeto)
                if (eventoToEdit == null) {
                    (dialog as? AlertDialog)?.getButton(Dialog.BUTTON_NEUTRAL)?.visibility =
                        if (selectedSujeto == "Plantas") View.VISIBLE else View.GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun updateSucesoSpinner(sujeto: String) {
        mantenimientoSpinner.visibility = View.GONE
        mantenimientoLabel.visibility = View.GONE
        potenciaLuzSpinner.visibility = View.GONE
        potenciaLuzLabel.visibility = View.GONE
        ventiladorModoSpinner.visibility = View.GONE
        ventiladorModoLabel.visibility = View.GONE

        if (sujeto == "Plantas") {
            plantaSpinner.visibility = View.VISIBLE
            plantaLabel.visibility = View.VISIBLE
            sucesoLabel.text = "Técnica / Estado:"
            val sucesoOptions = arrayOf("Poda Apical", "Poda LST", "Trasplante", "Plaga Detectada", "Inicio de Secado", "Inicio de Curado")
            val adapter = ArrayAdapter(requireContext(), R.layout.custom_spinner_item, sucesoOptions)
            adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
            sucesoSpinner.adapter = adapter
        } else {
            plantaSpinner.visibility = View.GONE
            plantaLabel.visibility = View.GONE
            sucesoLabel.text = "Categoría:"
            val sucesoOptions = arrayOf("Mantenimiento", "Potencia Luz (JX150)", "Ventilador / Extracción")
            val adapter = ArrayAdapter(requireContext(), R.layout.custom_spinner_item, sucesoOptions)
            adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
            sucesoSpinner.adapter = adapter
            sucesoSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    when (sucesoOptions[position]) {
                        "Mantenimiento" -> {
                            mantenimientoSpinner.visibility = View.VISIBLE
                            mantenimientoLabel.visibility = View.VISIBLE
                            potenciaLuzSpinner.visibility = View.GONE
                            potenciaLuzLabel.visibility = View.GONE
                            ventiladorModoSpinner.visibility = View.GONE
                            ventiladorModoLabel.visibility = View.GONE
                        }
                        "Potencia Luz (JX150)" -> {
                            potenciaLuzSpinner.visibility = View.VISIBLE
                            potenciaLuzLabel.visibility = View.VISIBLE
                            mantenimientoSpinner.visibility = View.GONE
                            mantenimientoLabel.visibility = View.GONE
                            ventiladorModoSpinner.visibility = View.GONE
                            ventiladorModoLabel.visibility = View.GONE
                        }
                        "Ventilador / Extracción" -> {
                            ventiladorModoSpinner.visibility = View.VISIBLE
                            ventiladorModoLabel.visibility = View.VISIBLE
                            mantenimientoSpinner.visibility = View.GONE
                            mantenimientoLabel.visibility = View.GONE
                            potenciaLuzSpinner.visibility = View.GONE
                            potenciaLuzLabel.visibility = View.GONE
                        }
                        else -> {
                            mantenimientoSpinner.visibility = View.GONE
                            mantenimientoLabel.visibility = View.GONE
                            potenciaLuzSpinner.visibility = View.GONE
                            potenciaLuzLabel.visibility = View.GONE
                            ventiladorModoSpinner.visibility = View.GONE
                            ventiladorModoLabel.visibility = View.GONE
                        }
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private fun setupPlantaSpinner() {
        if (plantas.isEmpty()) {
            val noPlantas = listOf("Añadir Planta")
            val adapter = ArrayAdapter(requireContext(), R.layout.custom_spinner_item, noPlantas)
            adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
            plantaSpinner.adapter = adapter
            plantaSpinner.isEnabled = false
        } else {
            val plantaNombres = plantas.map { it.nombre }
            val adapter = ArrayAdapter(requireContext(), R.layout.custom_spinner_item, plantaNombres)
            adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
            plantaSpinner.adapter = adapter
            plantaSpinner.isEnabled = true
        }
    }

    private fun setupPotenciaLuzSpinner() {
        val potenciaOptions = arrayOf("25%", "50%", "75%", "100%")
        val adapter = ArrayAdapter(requireContext(), R.layout.custom_spinner_item, potenciaOptions)
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
        potenciaLuzSpinner.adapter = adapter
    }

    private fun setupVentiladorModoSpinner() {
        val modoOptions = arrayOf("24hs", "Intermitente", "Apagado")
        val adapter = ArrayAdapter(requireContext(), R.layout.custom_spinner_item, modoOptions)
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
        ventiladorModoSpinner.adapter = adapter
    }

    private fun setupMantenimientoSpinner() {
        val mantenimientoOptions = arrayOf("Limpieza Piso", "Limpieza Paredes", "Limpieza General", "Limpieza Luz")
        val adapter = ArrayAdapter(requireContext(), R.layout.custom_spinner_item, mantenimientoOptions)
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
        mantenimientoSpinner.adapter = adapter
    }

    private fun setupForEditing(evento: Evento) {
        val sujetoPos = if (evento.sujeto == "Plantas") 0 else 1
        sujetoSpinner.setSelection(sujetoPos)

        if (evento.sujeto == "Plantas") {
            val plantaPos = plantas.indexOfFirst { it.id == evento.plantaId }
            if (plantaPos != -1) {
                plantaSpinner.setSelection(plantaPos)
            }
        } 
        val sucesoOptions = if (evento.sujeto == "Plantas") {
            arrayOf("Poda Apical", "Poda LST", "Trasplante", "Plaga Detectada", "Inicio de Secado", "Inicio de Curado")
        } else {
            arrayOf("Mantenimiento", "Potencia Luz (JX150)", "Ventilador / Extracción")
        }
        val sucesoPos = sucesoOptions.indexOfFirst { evento.suceso.startsWith(it) }
        if (sucesoPos != -1) {
            sucesoSpinner.setSelection(sucesoPos)
            if (evento.suceso.startsWith("Mantenimiento")) {
                mantenimientoSpinner.visibility = View.VISIBLE
                val mantenimientoValue = evento.suceso.substringAfter(" - ")
                val mantenimientoOptions = arrayOf("Limpieza Piso", "Limpieza Paredes", "Limpieza General", "Limpieza Luz")
                val mantenimientoPos = mantenimientoOptions.indexOf(mantenimientoValue)
                if (mantenimientoPos != -1) {
                    mantenimientoSpinner.setSelection(mantenimientoPos)
                }
            } else if (evento.suceso.startsWith("Potencia Luz")) {
                potenciaLuzSpinner.visibility = View.VISIBLE
                val potenciaValue = evento.suceso.substringAfter(" - ")
                val potenciaOptions = arrayOf("25%", "50%", "75%", "100%")
                val potenciaPos = potenciaOptions.indexOf(potenciaValue)
                if (potenciaPos != -1) {
                    potenciaLuzSpinner.setSelection(potenciaPos)
                }
            } else if (evento.suceso.startsWith("Ventilador")) {
                ventiladorModoSpinner.visibility = View.VISIBLE
                val modoValue = evento.suceso.substringAfter(" - ")
                val modoOptions = arrayOf("24hs", "Intermitente", "Apagado")
                val modoPos = modoOptions.indexOf(modoValue)
                if (modoPos != -1) {
                    ventiladorModoSpinner.setSelection(modoPos)
                }
            }
        }

        val dateParts = evento.fecha.split("/")
        if (dateParts.size == 3) {
            fechaPicker.updateDate(dateParts[2].toInt(), dateParts[1].toInt() - 1, dateParts[0].toInt())
        }
    }


    companion object {
        private const val ARG_EVENTO = "evento_to_edit"
        private const val ARG_PLANTAS = "plantas_list"
        const val TAG = "AddEventoDialogFragment"

        fun newInstance(plantas: List<Planta>, evento: Evento? = null): AddEventoDialogFragment {
            val fragment = AddEventoDialogFragment()
            val args = Bundle()
            args.putParcelable(ARG_EVENTO, evento)
            args.putParcelableArrayList(ARG_PLANTAS, ArrayList(plantas))
            fragment.arguments = args
            return fragment
        }
    }
}