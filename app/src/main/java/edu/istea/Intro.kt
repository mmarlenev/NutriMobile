package edu.istea

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import edu.istea.views.LogFragment
import edu.istea.views.RegFragment

class Intro : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("faso_prefs", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt("userId", -1)

        if (userId != -1) {
            val intent = Intent(this, Home::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
            finish()
            return
        }

        setContentView(R.layout.intro_layout)

        val registrar: Button = findViewById(R.id.b_registrar)
        val login: Button = findViewById(R.id.b_login)
        val manager = supportFragmentManager

        registrar.setOnClickListener {
            manager.beginTransaction()
                .replace(R.id.userchangeframe, RegFragment())
                .commit()
        }

        login.setOnClickListener {
            manager.beginTransaction()
                .replace(R.id.userchangeframe, LogFragment())
                .commit()
        }
    }
}