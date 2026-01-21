package edu.istea

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.istea.adapter.EntornoAdapter
import edu.istea.adapter.EntornoAgrupado
import edu.istea.dao.DBHelper
import edu.istea.model.Entorno
import edu.istea.views.AddEntornoDialogFragment

class EntornoActivity : AppCompatActivity(), AddEntornoDialogFragment.AddEntornoDialogListener {

    private lateinit var entornoAdapter: EntornoAdapter
    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.entorno_layout)

        supportActionBar?.title = "Entorno"

        dbHelper = DBHelper(this)

        val rvEntorno: RecyclerView = findViewById(R.id.rv_entorno)
        entornoAdapter = EntornoAdapter(::handleItemClick)
        rvEntorno.adapter = entornoAdapter
        rvEntorno.layoutManager = LinearLayoutManager(this)

        val fabAddEntorno: FloatingActionButton = findViewById(R.id.fab_add_entorno)
        fabAddEntorno.setOnClickListener {
            val plantas = dbHelper.getAllPlantas()
            val dialog = AddEntornoDialogFragment.newInstance(plantas)
            dialog.show(supportFragmentManager, "AddEntornoDialogFragment")
        }

        loadEntornos()
    }

    private fun handleItemClick(entornoAgrupado: EntornoAgrupado) {
        val intent = Intent(this, EntornoDetalleActivity::class.java).apply {
            putExtra(EntornoDetalleActivity.EXTRA_PLANTA_ID, entornoAgrupado.plantaId)
            putExtra(EntornoDetalleActivity.EXTRA_FECHA, entornoAgrupado.fecha)
            putExtra(EntornoDetalleActivity.EXTRA_PLANTA_NOMBRE, entornoAgrupado.plantaNombre)
        }
        startActivity(intent)
    }

    private fun loadEntornos() {
        val entornos = dbHelper.getAllEntornos()
        val entornosAgrupados = entornos.groupBy { it.plantaId to it.fecha }
            .map { (key, group) ->
                val (plantaId, fecha) = key
                val plantaNombre = group.first().plantaNombre
                EntornoAgrupado(plantaId, plantaNombre, fecha)
            }.sortedByDescending { it.fecha } // Sort by date descending
        entornoAdapter.submitList(entornosAgrupados)
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