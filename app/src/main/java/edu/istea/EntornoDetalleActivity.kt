package edu.istea

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.istea.adapter.EntornoDetalleAdapter
import edu.istea.dao.DBHelper
import edu.istea.model.Entorno
import edu.istea.views.AddEntornoDialogFragment

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
        val plantas = dbHelper.getAllPlantas()
        val dialog = AddEntornoDialogFragment.newInstance(plantas, entorno)
        dialog.show(supportFragmentManager, "ModifyEntornoDialogFragment")
    }

    private fun handleDelete(entorno: Entorno) {
        dbHelper.deleteEntorno(entorno.id)
        loadMediciones()
        Toast.makeText(this, "Medición eliminada", Toast.LENGTH_SHORT).show()
    }

    private fun loadMediciones() {
        val mediciones = dbHelper.getAllEntornos().filter { it.plantaId == plantaId && it.fecha == fecha }
        entornoDetalleAdapter.submitList(mediciones)
    }

    override fun onEntornoAdded(entorno: Entorno) {
        // This screen only edits/deletes, so this method will not be called.
    }

    override fun onEntornoUpdated(entorno: Entorno) {
        dbHelper.updateEntorno(entorno)
        loadMediciones()
        Toast.makeText(this, "Medición actualizada", Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val EXTRA_PLANTA_ID = "extra_planta_id"
        const val EXTRA_FECHA = "extra_fecha"
        const val EXTRA_PLANTA_NOMBRE = "extra_planta_nombre"
    }
}