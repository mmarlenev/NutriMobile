package edu.istea

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.istea.adapter.EntornoAdapter
import edu.istea.dao.DBHelper
import edu.istea.model.Entorno
import edu.istea.model.Planta
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
        entornoAdapter = EntornoAdapter()
        rvEntorno.adapter = entornoAdapter
        rvEntorno.layoutManager = LinearLayoutManager(this)

        val fabAddEntorno: FloatingActionButton = findViewById(R.id.fab_add_entorno)
        fabAddEntorno.setOnClickListener {
            val plantas = dbHelper.getAllPlantas()
            val dialog = AddEntornoDialogFragment(plantas)
            dialog.show(supportFragmentManager, "AddEntornoDialogFragment")
        }
        
        loadEntornos()
    }

    private fun loadEntornos() {
        val entornos = dbHelper.getAllEntornos()
        entornoAdapter.submitList(entornos)
    }

    override fun onEntornoAdded(entorno: Entorno) {
        dbHelper.saveEntorno(entorno)
        loadEntornos()
        Toast.makeText(this, "Medición añadida para ${entorno.plantaNombre}", Toast.LENGTH_SHORT).show()
    }
}