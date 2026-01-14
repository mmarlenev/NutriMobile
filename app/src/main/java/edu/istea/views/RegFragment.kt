package edu.istea.views

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import edu.istea.Intro
import edu.istea.R
import edu.istea.dao.DBHelper
import edu.istea.model.User

class RegFragment(val contextMain:Context) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.registar_layout, container, false)
        val user: EditText = view.findViewById(R.id.r_user)
        val nombrepila: EditText = view.findViewById(R.id.r_nombrepila)
        val surname: EditText = view.findViewById(R.id.r_surname)
        val pass: EditText = view.findViewById(R.id.r_pass)
        val register: Button = view.findViewById(R.id.r_saveuser)

        val db: DBHelper = DBHelper(contextMain)

        register.setOnClickListener(
            View.OnClickListener {
                db.saveUser(User(user.text.toString(),
                    surname.text.toString(),
                    nombrepila.text.toString(),
                    pass.text.toString()))
                Toast.makeText(view.context,"guardo el usuario",Toast.LENGTH_LONG).show()
                var intent = Intent(view.context, Intro::class.java)
                startActivity(intent)
            }
        )

        return view
    }
}