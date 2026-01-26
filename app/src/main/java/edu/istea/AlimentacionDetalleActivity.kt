package edu.istea

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.istea.adapter.AlimentacionAdapter
import edu.istea.dao.DBHelper
import edu.istea.model.Alimentacion
import edu.istea.views.AddAlimentacionDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlimentacionDetalleActivity : AppCompatActivity(), AddAlimentacionDialogFragment.AddAlimentacionDialogListener {

    private lateinit var alimentacionAdapter: AlimentacionAdapter
    private lateinit var dbHelper: DBHelper
    private var plantaId: Int = -1
    private lateinit var fecha: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alimentacion_detalle_layout)

        plantaId = intent.getIntExtra(EXTRA_PLANTA_ID, -1)
        fecha = intent.getStringExtra(EXTRA_FECHA) ?: ""
        val plantaNombre = intent.getStringExtra(EXTRA_PLANTA_NOMBRE) ?: "Detalles"

        supportActionBar?.title = "$plantaNombre - $fecha"

        dbHelper = DBHelper(this)

        val rvAlimentacionDetalle: RecyclerView = findViewById(R.id.rv_alimentacion_detalle)
        alimentacionAdapter = AlimentacionAdapter(
            onModifyClick = ::handleModify,
            onDeleteClick = ::handleDelete
        )
        rvAlimentacionDetalle.adapter = alimentacionAdapter
        rvAlimentacionDetalle.layoutManager = LinearLayoutManager(this)

        loadAlimentacionDetalles()
    }

    private fun loadAlimentacionDetalles() {
        lifecycleScope.launch {
            val alimentaciones = withContext(Dispatchers.IO) {
                dbHelper.getAlimentacionByPlantaAndFecha(plantaId, fecha)
            }
            alimentacionAdapter.submitList(alimentaciones)
        }
    }

    private fun handleModify(alimentacion: Alimentacion) {
        lifecycleScope.launch {
            val plantas = withContext(Dispatchers.IO) { dbHelper.getAllPlantas() }
            val dialog = AddAlimentacionDialogFragment.newInstance(plantas, alimentacion)
            dialog.show(supportFragmentManager, "ModifyAlimentacionDialogFragment")
        }
    }

    private fun handleDelete(alimentacion: Alimentacion) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                dbHelper.deleteAlimentacion(alimentacion.id)
            }
            loadAlimentacionDetalles()
            Toast.makeText(this@AlimentacionDetalleActivity, "Registro eliminado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onAlimentacionAdded(alimentacion: Alimentacion) {
        // This dialog is for modification, so this case is unlikely, but good to handle.
        lifecycleScope.launch {
            withContext(Dispatchers.IO) { dbHelper.saveAlimentacion(alimentacion) }
            loadAlimentacionDetalles()
        }
    }

    override fun onAlimentacionUpdated(alimentacion: Alimentacion) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) { dbHelper.updateAlimentacion(alimentacion) }
            loadAlimentacionDetalles()
        }
    }

    companion object {
        const val EXTRA_PLANTA_ID = "extra_planta_id"
        const val EXTRA_FECHA = "extra_fecha"
        const val EXTRA_PLANTA_NOMBRE = "extra_planta_nombre"
    }
}