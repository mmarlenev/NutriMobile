package edu.istea

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.istea.adapter.Adapter
import edu.istea.dao.DBHelper
import edu.istea.model.Comida
import java.text.SimpleDateFormat
import java.util.*

class Home : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_layout)

        val hand = DBHelper(this)
        val userId = intent.getSerializableExtra("userId") as Number

        // Layouts and Containers
        val botoneshome: LinearLayout = findViewById(R.id.botoneshome)
        val botoneshome2: LinearLayout = findViewById(R.id.botoneshome2)
        val cargar_comida_layout: View = findViewById(R.id.cargar_comida_layout)
        val pag_resumen_layout: View = findViewById(R.id.pag_resumen_layout)
        val usuario_legajo_layout: View = findViewById(R.id.usuario_legajo_layout)

        // Views inside layouts (using findViewById on the root if necessary, 
        // but since they are included with IDs, they are available in the main view tree)
        val spinner_comi: Spinner = findViewById(R.id.spinner_comi)
        val pregunta_postre: TextView = findViewById(R.id.pregunta_postre)
        val spinner_pos: Spinner = findViewById(R.id.spinner_pos)
        val insert_postre: EditText = findViewById(R.id.insert_postre)
        val spinner_tent: Spinner = findViewById(R.id.spinner_tent)
        val insert_tentacion: EditText = findViewById(R.id.insert_tentacion)
        val spinner_hambre: Spinner = findViewById(R.id.spinner_hambre)
        val insert_comida: EditText = findViewById(R.id.insert_comida)
        val insert_secundaria: EditText = findViewById(R.id.insert_secundaria)
        val bebida: EditText = findViewById(R.id.bebida)
        
        val btn_salir_resumen: Button = findViewById(R.id.btn_salir_resumen)
        val guardarComida: Button = findViewById(R.id.guardarComida)
        val btn_verhisto: Button = findViewById(R.id.btn_verhisto)
        val btn_volver_datos: Button = findViewById(R.id.btn_volver_datos)
        val btn_volver_comida: Button = findViewById(R.id.btn_volver_comida)
        val btn_cargarcomi: Button = findViewById(R.id.btn_cargarcomi)
        val btn_volvermenu: Button = findViewById(R.id.btn_volvermenu)
        val btn_verdatos: Button = findViewById(R.id.btn_verdatos)

        // Spinners Setup
        val comidas = arrayOf("Desayuno", "Almuerzo", "Merienda", "Cena")
        val comidasAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, comidas)
        comidasAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        spinner_comi.adapter = comidasAdapter

        spinner_comi.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (spinner_comi.selectedItem.toString() == "Almuerzo" || spinner_comi.selectedItem.toString() == "Cena") {
                    pregunta_postre.visibility = View.VISIBLE
                    spinner_pos.visibility = View.VISIBLE
                } else {
                    pregunta_postre.visibility = View.GONE
                    spinner_pos.visibility = View.GONE
                }
            }
        }

        val ingirioPostre = arrayOf("No", "Si")
        val ingirioPostreAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, ingirioPostre)
        ingirioPostreAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        spinner_pos.adapter = ingirioPostreAdapter

        spinner_pos.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                insert_postre.visibility = if (spinner_pos.selectedItem.toString() == "Si") View.VISIBLE else View.GONE
            }
        }

        val tentacion = arrayOf("No", "Si")
        val tentacionPostreAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tentacion)
        tentacionPostreAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        spinner_tent.adapter = tentacionPostreAdapter

        spinner_tent.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                insert_tentacion.visibility = if (spinner_tent.selectedItem.toString() == "Si") View.VISIBLE else View.GONE
            }
        }

        val quedoConHambre = arrayOf("No", "Si")
        val quedoConHambreAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, quedoConHambre)
        quedoConHambreAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)
        spinner_hambre.adapter = quedoConHambreAdapter

        // Button Actions
        btn_salir_resumen.setOnClickListener {
            botoneshome.visibility = View.VISIBLE
            botoneshome2.visibility = View.VISIBLE
            cargar_comida_layout.visibility = View.GONE
            pag_resumen_layout.visibility = View.GONE
        }

        guardarComida.setOnClickListener {
            val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
            val dia = sdf.format(Date())
            val comidaNueva = Comida(
                spinner_comi.selectedItem.toString(),
                insert_comida.text.toString(),
                insert_secundaria.text.toString(),
                bebida.text.toString(),
                spinner_pos.selectedItem.toString(),
                insert_postre.text.toString(),
                spinner_tent.selectedItem.toString(),
                insert_tentacion.text.toString(),
                spinner_hambre.selectedItem.toString(),
                dia,
                userId.toString()
            )
            hand.saveComida(comidaNueva)
            botoneshome.visibility = View.VISIBLE
            botoneshome2.visibility = View.VISIBLE
            cargar_comida_layout.visibility = View.GONE
        }

        btn_verhisto.setOnClickListener {
            val rvComidas: RecyclerView = findViewById(R.id.recycleview_comida)
            val comiInfo = hand.getComidaInfo(userId)
            rvComidas.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
            rvComidas.adapter = Adapter(comiInfo)

            botoneshome.visibility = View.GONE
            botoneshome2.visibility = View.GONE
            cargar_comida_layout.visibility = View.GONE
            pag_resumen_layout.visibility = View.VISIBLE
        }

        btn_volver_datos.setOnClickListener {
            botoneshome.visibility = View.VISIBLE
            botoneshome2.visibility = View.VISIBLE
            usuario_legajo_layout.visibility = View.GONE
        }

        btn_volver_comida.setOnClickListener {
            botoneshome.visibility = View.VISIBLE
            botoneshome2.visibility = View.VISIBLE
            cargar_comida_layout.visibility = View.GONE
        }

        btn_cargarcomi.setOnClickListener {
            botoneshome.visibility = View.GONE
            botoneshome2.visibility = View.GONE
            cargar_comida_layout.visibility = View.VISIBLE
        }

        btn_volvermenu.setOnClickListener {
            startActivity(Intent(this, Intro::class.java))
        }

        btn_verdatos.setOnClickListener {
            val datos = hand.getUserInfo(userId)
            findViewById<TextView>(R.id.username_legajo).text = "Usuario: ${datos.name}"
            findViewById<TextView>(R.id.email_legajo).text = "Password: ${datos.pass}"
            findViewById<TextView>(R.id.nombredepila_datos).text = "Nombre: ${datos.npila}"
            findViewById<TextView>(R.id.apellido_datos).text = "Apellido: ${datos.surname}"
            findViewById<TextView>(R.id.dni_datos).text = "Documento: ${datos.dni}"
            findViewById<TextView>(R.id.btirth_datos).text = "Fecha de nacimiento: ${datos.birth}"
            findViewById<TextView>(R.id.sexo_datos).text = "Sexo: ${datos.sexo}"
            findViewById<TextView>(R.id.city_datos).text = "Localidad: ${datos.city}"
            findViewById<TextView>(R.id.trata_datos).text = "Tratamiento: ${datos.tratamiento}"

            botoneshome.visibility = View.GONE
            botoneshome2.visibility = View.GONE
            cargar_comida_layout.visibility = View.GONE
            usuario_legajo_layout.visibility = View.VISIBLE
        }
    }
}
