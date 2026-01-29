package edu.istea

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Home : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_layout)

        val btnHistorial: Button = findViewById(R.id.btn_historial)
        val btnPlantas: Button = findViewById(R.id.btn_plantas)
        val btnEntorno: Button = findViewById(R.id.btn_entorno)
        val btnAlimentacion: Button = findViewById(R.id.btn_alimentacion)
        val btnEventos: Button = findViewById(R.id.btn_eventos)

        btnHistorial.setOnClickListener {
            startActivity(Intent(this, HistorialActivity::class.java))
        }

        btnPlantas.setOnClickListener {
            startActivity(Intent(this, PlantasActivity::class.java))
        }

        btnEntorno.setOnClickListener {
            startActivity(Intent(this, EntornoActivity::class.java))
        }

        btnAlimentacion.setOnClickListener {
            startActivity(Intent(this, AlimentacionActivity::class.java))
        }

        btnEventos.setOnClickListener {
            startActivity(Intent(this, EventoActivity::class.java))
        }
    }
}