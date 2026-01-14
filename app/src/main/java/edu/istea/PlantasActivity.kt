package edu.istea

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.istea.adapter.PlantaAdapter
import edu.istea.model.Planta
import edu.istea.viewmodel.PlantaViewModel
import edu.istea.views.AddPlantaDialogFragment

class PlantasActivity : AppCompatActivity() {

    private lateinit var plantaAdapter: PlantaAdapter
    private val plantaViewModel: PlantaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.plantas_layout)

        val rvPlantas: RecyclerView = findViewById(R.id.rv_plantas)
        plantaAdapter = PlantaAdapter(::handleModify, ::handleDelete)
        rvPlantas.adapter = plantaAdapter
        rvPlantas.layoutManager = LinearLayoutManager(this)

        val fabAddPlanta: FloatingActionButton = findViewById(R.id.fab_add_planta)
        fabAddPlanta.setOnClickListener {
            val dialog = AddPlantaDialogFragment.newInstance()
            dialog.show(supportFragmentManager, "AddPlantaDialogFragment")
        }

        plantaViewModel.plantas.observe(this, Observer { plantas ->
            plantaAdapter.submitList(plantas)
        })

        plantaViewModel.loadPlantas()
    }

    private fun handleModify(planta: Planta) {
        val dialog = AddPlantaDialogFragment.newInstance(planta)
        dialog.show(supportFragmentManager, "ModifyPlantaDialogFragment")
    }

    private fun handleDelete(planta: Planta) {
        plantaViewModel.deletePlanta(planta)
        Toast.makeText(this, "Planta eliminada: ${planta.nombre}", Toast.LENGTH_SHORT).show()
    }
}