package edu.istea.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import edu.istea.dao.DBHelper
import edu.istea.model.Planta

class PlantaViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = DBHelper(application)

    private val _plantas = MutableLiveData<List<Planta>>()
    val plantas: LiveData<List<Planta>> = _plantas

    fun loadPlantas() {
        _plantas.value = dbHelper.getAllPlantas()
    }

    fun addPlanta(planta: Planta) {
        dbHelper.savePlanta(planta)
        loadPlantas()
    }

    fun updatePlanta(planta: Planta) {
        dbHelper.updatePlanta(planta)
        loadPlantas()
    }

    fun deletePlanta(planta: Planta) {
        dbHelper.deletePlanta(planta.id, planta.nombre)
        loadPlantas()
    }
}