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

class PlantasActivity : AppCompatActivity(), AddPlantaDialogFragment.PlantaDialogListener {

    private lateinit var plantaAdapter: PlantaAdapter
    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.plantas_layout)

        dbHelper = DBHelper(this)

        val rvPlantas: RecyclerView = findViewById(R.id.rv_plantas)
        plantaAdapter = PlantaAdapter(::handleModify, ::handleDelete)
        rvPlantas.adapter = plantaAdapter
        rvPlantas.layoutManager = LinearLayoutManager(this)

        val fabAddPlanta: FloatingActionButton = findViewById(R.id.fab_add_planta)
        fabAddPlanta.setOnClickListener {
            val dialog = AddPlantaDialogFragment.newInstance()
            dialog.show(supportFragmentManager, AddPlantaDialogFragment.TAG)
        }

        loadPlantas()
    }

    private fun loadPlantas() {
        val plantas = dbHelper.getAllPlantas()
        plantaAdapter.submitList(plantas)
    }

    private fun handleModify(planta: Planta) {
        val dialog = AddPlantaDialogFragment.newInstance(planta)
        dialog.show(supportFragmentManager, AddPlantaDialogFragment.TAG)
    }

    private fun handleDelete(planta: Planta) {
        dbHelper.deletePlanta(planta.id, planta.nombre)
        loadPlantas()
        Toast.makeText(this, "Planta eliminada: ${planta.nombre}", Toast.LENGTH_SHORT).show()
    }

    override fun onPlantaAdded(planta: Planta) {
        dbHelper.savePlanta(planta)
        loadPlantas()
        Toast.makeText(this, "Planta añadida: ${planta.nombre}", Toast.LENGTH_SHORT).show()
    }

    override fun onPlantaUpdated(planta: Planta) {
        dbHelper.updatePlanta(planta)
        loadPlantas()
        Toast.makeText(this, "Planta actualizada: ${planta.nombre}", Toast.LENGTH_SHORT).show()
    }
}