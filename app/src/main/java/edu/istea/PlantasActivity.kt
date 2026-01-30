package edu.istea

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.BundleCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.istea.adapter.PlantaAdapter
import edu.istea.dao.DBHelper
import edu.istea.model.Planta
import edu.istea.views.AddPlantaDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PlantasActivity : AppCompatActivity() {

    private lateinit var plantaAdapter: PlantaAdapter
    private lateinit var dbHelper: DBHelper

    private val createCsvLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            it.data?.data?.let { uri ->
                writeCsvData(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.plantas_layout)

        dbHelper = DBHelper(this)

        setupResultListener()

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.plantas_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_export_csv -> {
                exportPlantasToCsv()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun exportPlantasToCsv() {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Plantas_$timeStamp.csv"
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
            putExtra(Intent.EXTRA_TITLE, fileName)
        }
        createCsvLauncher.launch(intent)
    }

    private fun writeCsvData(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val allPlantas = dbHelper.getAllPlantas()
                    val writer = outputStream.bufferedWriter()
                    // Header
                    writer.write("ID,Nombre,Tipo,Fecha de Origen,Etapa\n")
                    // Data
                    allPlantas.forEach { planta ->
                        writer.write("${planta.id},${planta.nombre},${planta.tipo},${planta.fechaOrigen},${planta.etapa}\n")
                    }
                    writer.flush()
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PlantasActivity, "Exportado a CSV con éxito", Toast.LENGTH_LONG).show()
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PlantasActivity, "Error al exportar: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupResultListener() {
        supportFragmentManager.setFragmentResultListener("requestKey", this) { _, bundle ->
            BundleCompat.getParcelable(bundle, "planta", Planta::class.java)?.let { planta ->
                if (planta.id == 0) {
                    dbHelper.savePlanta(planta)
                    loadPlantas()
                    Toast.makeText(this, "Planta añadida: ${planta.nombre}", Toast.LENGTH_SHORT).show()
                } else {
                    dbHelper.updatePlanta(planta)
                    loadPlantas()
                    Toast.makeText(this, "Planta actualizada: ${planta.nombre}", Toast.LENGTH_SHORT).show()
                }
            }
        }
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
}
