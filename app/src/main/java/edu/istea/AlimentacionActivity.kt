package edu.istea

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.istea.adapter.AlimentacionFecha
import edu.istea.adapter.AlimentacionGroupedAdapter
import edu.istea.adapter.AlimentacionListItem
import edu.istea.adapter.AlimentacionPlanta
import edu.istea.dao.DBHelper
import edu.istea.model.Alimentacion
import edu.istea.views.AddAlimentacionDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlimentacionActivity : AppCompatActivity(), AddAlimentacionDialogFragment.AddAlimentacionDialogListener {

    private lateinit var alimentacionAdapter: AlimentacionGroupedAdapter
    private lateinit var dbHelper: DBHelper
    private val plantHeaders = mutableListOf<AlimentacionListItem.PlantaHeader>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alimentacion_layout)

        supportActionBar?.title = "Alimentación"

        dbHelper = DBHelper(this)

        val rvAlimentacion: RecyclerView = findViewById(R.id.rv_alimentacion)
        alimentacionAdapter = AlimentacionGroupedAdapter(
            onHeaderClick = ::handleHeaderClick,
            onFechaClick = ::handleFechaClick
        )
        rvAlimentacion.adapter = alimentacionAdapter
        rvAlimentacion.layoutManager = LinearLayoutManager(this)

        val fabAddAlimentacion: FloatingActionButton = findViewById(R.id.fab_add_alimentacion)
        fabAddAlimentacion.setOnClickListener {
            lifecycleScope.launch {
                val plantas = withContext(Dispatchers.IO) { dbHelper.getAllPlantas() }
                if (plantas.isEmpty()) {
                    Toast.makeText(this@AlimentacionActivity, "Primero debes crear una planta", Toast.LENGTH_SHORT).show()
                } else {
                    val dialog = AddAlimentacionDialogFragment.newInstance(plantas)
                    dialog.show(supportFragmentManager, "AddAlimentacionDialogFragment")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadAlimentacion()
    }

    private fun handleHeaderClick(header: AlimentacionListItem.PlantaHeader) {
        plantHeaders.find { it.planta.plantaId == header.planta.plantaId }?.let {
            it.isExpanded = !it.isExpanded
        }
        updateRecyclerView()
    }

    private fun handleFechaClick(fecha: AlimentacionFecha) {
        val intent = Intent(this, AlimentacionDetalleActivity::class.java).apply {
            putExtra(AlimentacionDetalleActivity.EXTRA_PLANTA_ID, fecha.plantaId)
            putExtra(AlimentacionDetalleActivity.EXTRA_FECHA, fecha.fecha)
            putExtra(AlimentacionDetalleActivity.EXTRA_PLANTA_NOMBRE, fecha.plantaNombre)
        }
        startActivity(intent)
    }

    private fun loadAlimentacion() {
        lifecycleScope.launch(Dispatchers.Main) {
            val currentExpandedIds = plantHeaders.filter { it.isExpanded }.map { it.planta.plantaId }.toSet()
            
            val plantasConAlimentacion = withContext(Dispatchers.IO) {
                val allAlimentacion = dbHelper.getAllAlimentacion()
                allAlimentacion.groupBy { it.plantaId }
                    .map { (plantaId, alimentaciones) ->
                        val plantaNombre = alimentaciones.first().plantaNombre
                        val fechas = alimentaciones.map { it.fecha }.distinct().sortedDescending()
                        val ultimaFecha = fechas.firstOrNull() ?: "N/A"
                        
                        val fechasItems = fechas.map { AlimentacionFecha(plantaId, plantaNombre, it) }
                        AlimentacionPlanta(plantaId, plantaNombre, ultimaFecha, fechasItems)
                    }
            }

            plantHeaders.clear()
            plantHeaders.addAll(plantasConAlimentacion.map { planta ->
                AlimentacionListItem.PlantaHeader(planta, isExpanded = planta.plantaId in currentExpandedIds)
            })

            updateRecyclerView()
        }
    }

    private fun updateRecyclerView() {
        val displayList = mutableListOf<AlimentacionListItem>()
        plantHeaders.sortedBy { it.planta.plantaNombre }.forEach { header ->
            displayList.add(header)
            if (header.isExpanded) {
                displayList.addAll(header.planta.fechas.map { AlimentacionListItem.FechaItem(it) })
            }
        }
        alimentacionAdapter.submitList(displayList)
    }

    override fun onAlimentacionAdded(alimentacion: Alimentacion) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                dbHelper.saveAlimentacion(alimentacion)
            }
            loadAlimentacion() // Reload to show the new data
        }
    }

    override fun onAlimentacionUpdated(alimentacion: Alimentacion) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                dbHelper.updateAlimentacion(alimentacion)
            }
            loadAlimentacion() // Reload to show the updated data
        }
    }
}