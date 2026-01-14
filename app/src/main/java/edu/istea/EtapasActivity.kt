package edu.istea

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.istea.adapter.EtapaAdapter
import edu.istea.dao.DBHelper
import edu.istea.model.Etapa
import edu.istea.model.Planta
import edu.istea.views.AddEtapaDialogFragment

class EtapasActivity : AppCompatActivity(), AddEtapaDialogFragment.AddEtapaDialogListener {

    private lateinit var etapaAdapter: EtapaAdapter
    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.etapas_layout)

        dbHelper = DBHelper(this)

        val rvEtapas: RecyclerView = findViewById(R.id.rv_etapas)
        etapaAdapter = EtapaAdapter()
        rvEtapas.adapter = etapaAdapter
        rvEtapas.layoutManager = LinearLayoutManager(this)

        val fabAddEtapa: FloatingActionButton = findViewById(R.id.fab_add_etapa)
        fabAddEtapa.setOnClickListener {
            val plantas = dbHelper.getAllPlantas()
            val dialog = AddEtapaDialogFragment(plantas)
            dialog.show(supportFragmentManager, "AddEtapaDialogFragment")
        }
        
        loadEtapas()
    }

    private fun loadEtapas() {
        val etapas = dbHelper.getAllEtapas()
        etapaAdapter.submitList(etapas)
    }

    override fun onEtapaAdded(etapa: Etapa) {
        dbHelper.saveEtapa(etapa)
        loadEtapas()
        Toast.makeText(this, "Etapa añadida: ${etapa.estado}", Toast.LENGTH_SHORT).show()
    }
}