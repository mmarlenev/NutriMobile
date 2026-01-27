package edu.istea

import android.content.Intent
import android.database.sqlite.SQLiteException
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.istea.adapter.EntornoAdapter
import edu.istea.adapter.EntornoFecha
import edu.istea.adapter.EntornoListItem
import edu.istea.adapter.EntornoPlanta
import edu.istea.dao.DBHelper
import edu.istea.logic.AlertLogic
import edu.istea.logic.AlertState
import edu.istea.logic.AlertStatus
import edu.istea.model.Planta
import edu.istea.views.AddEntornoDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EntornoActivity : AppCompatActivity(), AddEntornoDialogFragment.AddEntornoDialogListener {

    private lateinit var entornoAdapter: EntornoAdapter
    private lateinit var dbHelper: DBHelper
    private val plantHeaders = mutableListOf<EntornoListItem.PlantaHeader>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.entorno_layout)

        supportActionBar?.title = "Entorno"

        dbHelper = DBHelper(this)

        val rvEntorno: RecyclerView = findViewById(R.id.rv_entorno)
        entornoAdapter = EntornoAdapter(
            onHeaderClick = ::handleHeaderClick,
            onFechaClick = ::handleFechaClick
        )
        rvEntorno.adapter = entornoAdapter
        rvEntorno.layoutManager = LinearLayoutManager(this)

        val fabAddEntorno: FloatingActionButton = findViewById(R.id.fab_add_entorno)
        fabAddEntorno.setOnClickListener {
            lifecycleScope.launch {
                try {
                    val plantas = withContext(Dispatchers.IO) {
                        dbHelper.getAllPlantas()
                    }
                    val dialog = AddEntornoDialogFragment.newInstance(plantas)
                    dialog.show(supportFragmentManager, AddEntornoDialogFragment.TAG)
                } catch (e: SQLiteException) {
                    Toast.makeText(this@EntornoActivity, "Error al acceder a la base de datos", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadEntornos()
    }

    private fun handleHeaderClick(header: EntornoListItem.PlantaHeader) {
        plantHeaders.find { it.planta.plantaId == header.planta.plantaId }?.let {
            it.isExpanded = !it.isExpanded
        }
        updateRecyclerView()
    }

    private fun handleFechaClick(fecha: EntornoFecha) {
        val intent = Intent(this, EntornoDetalleActivity::class.java).apply {
            putExtra(EntornoDetalleActivity.EXTRA_PLANTA_ID, fecha.plantaId)
            putExtra(EntornoDetalleActivity.EXTRA_FECHA, fecha.fecha)
            putExtra(EntornoDetalleActivity.EXTRA_PLANTA_NOMBRE, fecha.plantaNombre)
        }
        startActivity(intent)
    }

    private fun loadEntornos() {
        lifecycleScope.launch(Dispatchers.Main) {
            val currentExpandedIds = plantHeaders.filter { it.isExpanded }.map { it.planta.plantaId }.toSet()
            try {
                val plantasConEntornos = withContext(Dispatchers.IO) {
                    val allEntornos = dbHelper.getAllEntornos()
                    allEntornos.groupBy { it.plantaId }
                        .map { (plantaId, mediciones) ->
                            val plantaNombre = mediciones.first().plantaNombre
                            val fechas = mediciones.map { it.fecha }.distinct().sortedDescending()
                            val ultimaFecha = fechas.firstOrNull() ?: "N/A"

                            val alertStatus = AlertStatus(AlertState.NORMAL, "")

                            val fechasItems = fechas.map { EntornoFecha(plantaId, plantaNombre, it) }
                            EntornoPlanta(plantaId, plantaNombre, ultimaFecha, fechasItems, alertStatus)
                        }
                }

                plantHeaders.clear()
                plantHeaders.addAll(plantasConEntornos.map { planta ->
                    EntornoListItem.PlantaHeader(planta, isExpanded = planta.plantaId in currentExpandedIds)
                })

                updateRecyclerView()
            } catch (e: SQLiteException) {
                Toast.makeText(this@EntornoActivity, "Error al cargar el historial de entorno", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateRecyclerView() {
        val displayList = mutableListOf<EntornoListItem>()
        plantHeaders.forEach { header ->
            displayList.add(header)
            if (header.isExpanded) {
                displayList.addAll(header.planta.fechas.map { EntornoListItem.FechaItem(it) })
            }
        }
        entornoAdapter.submitList(displayList)
    }

    override fun onDialogDataChanged() {
        loadEntornos()
    }
}