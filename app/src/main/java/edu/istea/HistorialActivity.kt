package edu.istea

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.istea.adapter.HistorialAdapter
import edu.istea.dao.DBHelper

class HistorialActivity : AppCompatActivity() {

    private lateinit var historialAdapter: HistorialAdapter
    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.historial_layout)

        dbHelper = DBHelper(this)

        val rvHistorial: RecyclerView = findViewById(R.id.rv_historial)
        historialAdapter = HistorialAdapter()
        rvHistorial.adapter = historialAdapter
        rvHistorial.layoutManager = LinearLayoutManager(this)

        loadHistorial()
    }

    private fun loadHistorial() {
        val eventos = dbHelper.getAllHistorialEventos()
        historialAdapter.submitList(eventos)
    }
}