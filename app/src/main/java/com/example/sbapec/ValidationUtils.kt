package com.example.sbapec

import android.util.Patterns
import android.widget.EditText
import android.widget.Toast


object ValidationUtils {

    fun emailValidator(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

}