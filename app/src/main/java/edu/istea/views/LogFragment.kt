package edu.istea.views

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import edu.istea.Home
import edu.istea.R
import edu.istea.dao.DBHelper
import edu.istea.model.User

class LogFragment(val contextMain: Context) :Fragment() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(

        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.login_layout,container,false)
        val user:EditText = view.findViewById(R.id.l_user)
        val pass:EditText = view.findViewById(R.id.l_pass)
        val login:Button = view.findViewById(R.id.l_login)

        val db: DBHelper = DBHelper(contextMain)
        sharedPreferences = contextMain.getSharedPreferences("faso_prefs", Context.MODE_PRIVATE)

        login.setOnClickListener(
            View.OnClickListener {

                val userId = db.userActual(user.text.toString(),pass.text.toString())
                if(db.validateUser(User(user.text.toString(),
                        "x",
                        "x",
                        pass.text.toString()))){

                    sharedPreferences.edit().putInt("userId", userId).apply()

                    Toast.makeText(view.context,"Bienvenido!", Toast.LENGTH_SHORT).show()
                    var intent = Intent(view.context, Home::class.java)
                    intent.putExtra("userId",userId)
                    startActivity(intent)
                    activity?.finish()
                }else{
                    Toast.makeText(view.context,"Usuario no encontrado, REGISTRESE", Toast.LENGTH_LONG).show()
                }

            }
        )


        return view
    }
}