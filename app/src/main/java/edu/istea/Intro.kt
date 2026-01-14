package edu.istea

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
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


        // indexamos los fragmentos a nuestro main activity

        val loginFrg: LogFragment = LogFragment(this)
        val registerFrg: RegFragment = RegFragment(this)
        val registrar: Button = findViewById(R.id.b_registrar)
        val login: Button = findViewById(R.id.b_login)
        // explicarle a la actividad que va a aceptar fragmento con el supportManagerFragment
        val manager = supportFragmentManager

        registrar.setOnClickListener(
                View.OnClickListener {
                    val transaction = manager.beginTransaction()

                    transaction.replace(R.id.userchangeframe, registerFrg)
                    transaction.commit()
                }
        )
        login.setOnClickListener(
                View.OnClickListener {
                    val transaction = manager.beginTransaction()

                    transaction.replace(R.id.userchangeframe, loginFrg)
                    transaction.commit()
                }
        )


    }


}