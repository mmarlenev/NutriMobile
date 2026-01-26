package edu.istea

import android.database.sqlite.SQLiteException
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.istea.adapter.EntornoDetalleAdapter
import edu.istea.dao.DBHelper
import edu.istea.model.Entorno
import edu.istea.views.AddEntornoDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EntornoDetalleActivity : AppCompatActivity(), AddEntornoDialogFragment.AddEntornoDialogListener {

    private lateinit var dbHelper: DBHelper
    private lateinit var entornoDetalleAdapter: EntornoDetalleAdapter
    private var plantaId: Int = -1
    private lateinit var fecha: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.entorno_detalle_layout)

        plantaId = intent.getIntExtra(EXTRA_PLANTA_ID, -1)
        fecha = intent.getStringExtra(EXTRA_FECHA) ?: ""
        val plantaNombre = intent.getStringExtra(EXTRA_PLANTA_NOMBRE) ?: ""

        supportActionBar?.title = "Mediciones de $plantaNombre"
        supportActionBar?.subtitle = fecha

        dbHelper = DBHelper(this)

        val rvDetalle: RecyclerView = findViewById(R.id.rv_entorno_detalle)
        entornoDetalleAdapter = EntornoDetalleAdapter(
            onModifyClick = ::handleModify,
            onDeleteClick = ::handleDelete
        )
        rvDetalle.layoutManager = LinearLayoutManager(this)
        rvDetalle.adapter = entornoDetalleAdapter

        loadMediciones()
    }

    private fun handleModify(entorno: Entorno) {
        lifecycleScope.launch {
            try {
                val plantas = withContext(Dispatchers.IO) {
                    dbHelper.getAllPlantas()
                }
                val dialog = AddEntornoDialogFragment.newInstance(plantas, entorno)
                dialog.show(supportFragmentManager, "ModifyEntornoDialogFragment")
            } catch (e: SQLiteException) {
                Toast.makeText(this@EntornoDetalleActivity, "Error al acceder a la base de datos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleDelete(entorno: Entorno) {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    dbHelper.deleteEntorno(entorno.id)
                }
                loadMediciones()
                Toast.makeText(this@EntornoDetalleActivity, "Medición eliminada", Toast.LENGTH_SHORT).show()
            } catch (e: SQLiteException) {
                Toast.makeText(this@EntornoDetalleActivity, "Error al eliminar la medición", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadMediciones() {
        lifecycleScope.launch {
            try {
                val mediciones = withContext(Dispatchers.IO) {
                    dbHelper.getEntornosByPlantaAndFecha(plantaId, fecha)
                }
                entornoDetalleAdapter.submitList(mediciones)
            } catch (e: SQLiteException) {
                Toast.makeText(this@EntornoDetalleActivity, "Error al cargar las mediciones", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDialogDataChanged() {
        loadMediciones()
    }

    companion object {
        const val EXTRA_PLANTA_ID = "extra_planta_id"
        const val EXTRA_FECHA = "extra_fecha"
        const val EXTRA_PLANTA_NOMBRE = "extra_planta_nombre"
    }
}