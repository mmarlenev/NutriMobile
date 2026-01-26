package edu.istea

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.istea.adapter.HistorialAdapter
import edu.istea.dao.DBHelper
import edu.istea.views.HistorialFilter
import edu.istea.views.HistorialFilterDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistorialActivity : AppCompatActivity(), HistorialFilterDialogFragment.HistorialFilterDialogListener {

    private lateinit var historialAdapter: HistorialAdapter
    private lateinit var dbHelper: DBHelper
    private var currentFilter: HistorialFilter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.historial_layout)

        supportActionBar?.title = "Historial"

        dbHelper = DBHelper(this)

        val rvHistorial: RecyclerView = findViewById(R.id.rv_historial)
        historialAdapter = HistorialAdapter()
        rvHistorial.adapter = historialAdapter
        rvHistorial.layoutManager = LinearLayoutManager(this)

        loadHistorial()
    }

    private fun loadHistorial() {
        lifecycleScope.launch(Dispatchers.Main) {
            val eventos = withContext(Dispatchers.IO) {
                dbHelper.getFilteredHistorialEventos(currentFilter)
            }
            historialAdapter.submitList(eventos)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.historial_menu, menu)

        // Apply white tint to menu icons
        menu?.findItem(R.id.action_filter)?.icon?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)
        menu?.findItem(R.id.action_delete_all)?.icon?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_filter -> {
                val plantas = dbHelper.getAllPlantas()
                val dialog = HistorialFilterDialogFragment(plantas)
                dialog.show(supportFragmentManager, "HistorialFilterDialog")
                true
            }
            R.id.action_delete_all -> {
                showDeleteAllConfirmationDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDeleteAllConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Confirmar Eliminación")
            .setMessage("¿Estás seguro de que quieres borrar TODO el historial? Esta acción no se puede deshacer.")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("Sí, Borrar Todo") { _, _ ->
                dbHelper.clearHistorial()
                loadHistorial()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onFilterApplied(filter: HistorialFilter) {
        currentFilter = filter
        loadHistorial()
    }

    override fun onFilterCleared() {
        currentFilter = null
        loadHistorial()
    }
}