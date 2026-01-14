package edu.istea

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.istea.adapter.PlantaAdapter
import edu.istea.dao.DBHelper
import edu.istea.model.Planta
import edu.istea.views.AddPlantaDialogFragment

class PlantasActivity : AppCompatActivity(), AddPlantaDialogFragment.AddPlantaDialogListener {

    private lateinit var plantaAdapter: PlantaAdapter
    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.plantas_layout)

        dbHelper = DBHelper(this)

        val rvPlantas: RecyclerView = findViewById(R.id.rv_plantas)
        plantaAdapter = PlantaAdapter()
        rvPlantas.adapter = plantaAdapter
        rvPlantas.layoutManager = LinearLayoutManager(this)

        val fabAddPlanta: FloatingActionButton = findViewById(R.id.fab_add_planta)
        fabAddPlanta.setOnClickListener {
            val dialog = AddPlantaDialogFragment()
            dialog.show(supportFragmentManager, "AddPlantaDialogFragment")
        }

        loadPlantas()
    }

    private fun loadPlantas() {
        val plantas = dbHelper.getAllPlantas()
        plantaAdapter.submitList(plantas)
    }

    override fun onPlantaAdded(planta: Planta) {
        dbHelper.savePlanta(planta)
        loadPlantas()
        Toast.makeText(this, "Planta añadida: ${planta.nombre}", Toast.LENGTH_SHORT).show()
    }
}