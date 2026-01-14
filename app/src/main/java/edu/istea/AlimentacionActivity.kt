package edu.istea

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.istea.adapter.AlimentacionAdapter
import edu.istea.dao.DBHelper
import edu.istea.model.Alimentacion
import edu.istea.model.Planta
import edu.istea.views.AddAlimentacionDialogFragment

class AlimentacionActivity : AppCompatActivity(), AddAlimentacionDialogFragment.AddAlimentacionDialogListener {

    private lateinit var alimentacionAdapter: AlimentacionAdapter
    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alimentacion_layout)

        supportActionBar?.title = "Alimentación"

        dbHelper = DBHelper(this)

        val rvAlimentacion: RecyclerView = findViewById(R.id.rv_alimentacion)
        alimentacionAdapter = AlimentacionAdapter()
        rvAlimentacion.adapter = alimentacionAdapter
        rvAlimentacion.layoutManager = LinearLayoutManager(this)

        val fabAddAlimentacion: FloatingActionButton = findViewById(R.id.fab_add_alimentacion)
        fabAddAlimentacion.setOnClickListener {
            val plantas = dbHelper.getAllPlantas()
            val dialog = AddAlimentacionDialogFragment(plantas)
            dialog.show(supportFragmentManager, "AddAlimentacionDialogFragment")
        }

        loadAlimentacion()
    }

    private fun loadAlimentacion() {
        val alimentacion = dbHelper.getAllAlimentacion()
        alimentacionAdapter.submitList(alimentacion)
    }

    override fun onAlimentacionAdded(alimentacion: Alimentacion) {
        dbHelper.saveAlimentacion(alimentacion)
        loadAlimentacion()
        Toast.makeText(this, "Alimentación añadida para ${alimentacion.plantaNombre}", Toast.LENGTH_SHORT).show()
    }
}