package edu.istea

import android.app.Activity
import android.content.Intent
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import edu.istea.adapter.EventoAdapter
import edu.istea.adapter.EventoDetalle
import edu.istea.adapter.EventoFecha
import edu.istea.adapter.EventoGrupo
import edu.istea.adapter.EventoListItem
import edu.istea.dao.DBHelper
import edu.istea.databinding.ActivityEventoBinding
import edu.istea.model.Evento
import edu.istea.views.AddEventoDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventoActivity : AppCompatActivity(), AddEventoDialogFragment.AddEventoDialogListener {

    private lateinit var binding: ActivityEventoBinding
    private lateinit var eventoAdapter: EventoAdapter
    private lateinit var dbHelper: DBHelper
    private val groupHeaders = mutableListOf<EventoListItem.GrupoHeader>()

    private val createCsvLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            it.data?.data?.let { uri ->
                writeCsvData(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Eventos"

        dbHelper = DBHelper(this)

        eventoAdapter = EventoAdapter(
            onGrupoClick = ::handleGrupoClick,
            onFechaClick = ::handleFechaClick,
            onEditClick = ::handleEditClick,
            onDeleteClick = ::handleDeleteClick
        )
        binding.rvEventos.adapter = eventoAdapter
        binding.rvEventos.layoutManager = LinearLayoutManager(this)

        binding.fabAddEvento.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val plantas = withContext(Dispatchers.IO) {
                        dbHelper.getAllPlantas()
                    }
                    val dialog = AddEventoDialogFragment.newInstance(plantas)
                    dialog.show(supportFragmentManager, AddEventoDialogFragment.TAG)
                } catch (e: SQLiteException) {
                    Toast.makeText(this@EventoActivity, "Error al acceder a la base de datos", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.evento_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_export_csv -> {
                exportEventosToCsv()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun exportEventosToCsv() {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Eventos_$timeStamp.csv"
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
            putExtra(Intent.EXTRA_TITLE, fileName)
        }
        createCsvLauncher.launch(intent)
    }

    private fun writeCsvData(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val allEventos = dbHelper.getAllEventos()
                    val writer = outputStream.bufferedWriter()
                    // Header
                    writer.write("ID,Sujeto,Suceso,Fecha,Planta ID\n")
                    // Data
                    allEventos.forEach { evento ->
                        writer.write("${evento.id},${evento.sujeto},${evento.suceso},${evento.fecha},${evento.plantaId}\n")
                    }
                    writer.flush()
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EventoActivity, "Exportado a CSV con éxito", Toast.LENGTH_LONG).show()
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EventoActivity, "Error al exportar: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } catch (e: SQLiteException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EventoActivity, "Error en la base de datos al exportar", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadEventos()
    }

    private fun handleGrupoClick(header: EventoListItem.GrupoHeader) {
        val foundHeader = groupHeaders.find { it.grupo.nombre == header.grupo.nombre }
        foundHeader?.let {
            it.grupo.isExpanded = !it.grupo.isExpanded
        }
        updateRecyclerView()
    }

    private fun handleFechaClick(header: EventoListItem.FechaHeader) {
        groupHeaders.forEach { grupoHeader ->
            if (grupoHeader.grupo.nombre == header.fecha.groupNombre) {
                val foundFecha = grupoHeader.grupo.fechas.find { it.fecha == header.fecha.fecha }
                foundFecha?.let {
                    it.isExpanded = !it.isExpanded
                }
            }
        }
        updateRecyclerView()
    }

    private fun handleEditClick(evento: Evento) {
        lifecycleScope.launch {
            val plantas = withContext(Dispatchers.IO) {
                dbHelper.getAllPlantas()
            }
            val dialog = AddEventoDialogFragment.newInstance(plantas, evento)
            dialog.show(supportFragmentManager, AddEventoDialogFragment.TAG)
        }
    }

    private fun handleDeleteClick(evento: Evento) {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Borrado")
            .setMessage("¿Estás seguro de que quieres borrar este evento?")
            .setPositiveButton("Borrar") { _, _ ->
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        dbHelper.deleteEvento(evento.id)
                    }
                    loadEventos()
                    Toast.makeText(this@EventoActivity, "Evento borrado", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun loadEventos() {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                val currentExpandedGrupos = groupHeaders.filter { it.grupo.isExpanded }.map { it.grupo.nombre }.toSet()
                val currentExpandedFechas = groupHeaders.flatMap { it.grupo.fechas }.filter { it.isExpanded }.map { "${it.groupNombre}-${it.fecha}" }.toSet()

                val eventos = withContext(Dispatchers.IO) {
                    dbHelper.getAllEventos()
                }
                val plantas = withContext(Dispatchers.IO) {
                    dbHelper.getAllPlantas().associateBy { it.id }
                }

                val dateFormat = SimpleDateFormat("d/M/yyyy", Locale.getDefault())

                val groupedBySujeto = eventos.groupBy { if (it.plantaId != null && it.plantaId > 0) plantas[it.plantaId]?.nombre ?: "Planta Desconocida" else "Lugar de Cultivo" }

                val allGrupos = groupedBySujeto.map { (nombreGrupo, eventosGrupo) ->
                    val sortedFechas = eventosGrupo.map { it.fecha }.distinct()
                        .sortedByDescending {
                            try {
                                dateFormat.parse(it)
                            } catch (e: ParseException) {
                                null
                            }
                        }

                    val fechas = sortedFechas.map { fecha ->
                        val eventosEnFecha = eventosGrupo.filter { it.fecha == fecha }
                        EventoFecha(
                            nombreGrupo,
                            fecha,
                            eventosEnFecha.map { EventoDetalle(it) },
                            isExpanded = "$nombreGrupo-$fecha" in currentExpandedFechas
                        )
                    }
                    EventoGrupo(
                        nombreGrupo,
                        sortedFechas.firstOrNull() ?: "N/A",
                        fechas,
                        isExpanded = nombreGrupo in currentExpandedGrupos
                    )
                }

                groupHeaders.clear()
                groupHeaders.addAll(allGrupos.map { EventoListItem.GrupoHeader(it) }.sortedBy { it.grupo.nombre })

                updateRecyclerView()
            } catch (e: Exception) {
                Toast.makeText(this@EventoActivity, "Error al cargar el historial de eventos: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun updateRecyclerView() {
        val displayList = mutableListOf<EventoListItem>()
        groupHeaders.forEach { grupoHeader ->
            displayList.add(grupoHeader)
            if (grupoHeader.grupo.isExpanded) {
                grupoHeader.grupo.fechas.forEach { fecha ->
                    displayList.add(EventoListItem.FechaHeader(fecha))
                    if (fecha.isExpanded) {
                        displayList.addAll(fecha.eventos.map { EventoListItem.DetalleItem(it) })
                    }
                }
            }
        }
        eventoAdapter.submitList(displayList)
    }

    override fun onDialogDataChanged() {
        loadEventos()
    }
}
