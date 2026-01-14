package edu.istea.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class User(var name: String,
           var surname: String,
           var npila: String,
           var pass: String) : Parcelable {
}