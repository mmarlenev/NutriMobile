package edu.istea

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.istea.adapter.EntornoAdapter
import edu.istea.adapter.EntornoFecha
import edu.istea.adapter.EntornoListItem
import edu.istea.adapter.EntornoPlanta
import edu.istea.dao.DBHelper
import edu.istea.logic.AlertLogic
import edu.istea.model.Entorno
import edu.istea.views.AddEntornoDialogFragment

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
            val plantas = dbHelper.getAllPlantas()
            val dialog = AddEntornoDialogFragment.newInstance(plantas)
            dialog.show(supportFragmentManager, "AddEntornoDialogFragment")
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
        val currentExpandedIds = plantHeaders.filter { it.isExpanded }.map { it.planta.plantaId }.toSet()

        val allEntornos = dbHelper.getAllEntornos()
        val plantasConEntornos = allEntornos.groupBy { it.plantaId }
            .map { (plantaId, mediciones) ->
                val plantaNombre = mediciones.first().plantaNombre
                val fechas = mediciones.map { it.fecha }.distinct().sortedDescending()
                val ultimaFecha = fechas.firstOrNull() ?: "N/A"
                
                val latestMeasurements = mediciones.filter { it.fecha == ultimaFecha }
                val alertStatus = AlertLogic.getAlertStatus(latestMeasurements)

                val fechasItems = fechas.map { EntornoFecha(plantaId, plantaNombre, it) }
                EntornoPlanta(plantaId, plantaNombre, ultimaFecha, fechasItems, alertStatus)
            }
        
        plantHeaders.clear()
        plantHeaders.addAll(plantasConEntornos.map { planta ->
            EntornoListItem.PlantaHeader(planta, isExpanded = planta.plantaId in currentExpandedIds)
        })

        updateRecyclerView()
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

    override fun onEntornoAdded(entorno: Entorno) {
        dbHelper.saveEntorno(entorno)
        loadEntornos()
        Toast.makeText(this, "Medición añadida para ${entorno.plantaNombre}", Toast.LENGTH_SHORT).show()
    }

    override fun onEntornoUpdated(entorno: Entorno) {
        dbHelper.updateEntorno(entorno)
        loadEntornos()
        Toast.makeText(this, "Medición actualizada", Toast.LENGTH_SHORT).show()
    }
}