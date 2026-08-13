package com.dug.sun

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etMobile = findViewById<EditText>(R.id.etMobile)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvForgotPassword = findViewById<View>(R.id.tvForgotPassword)
        val tvRegister = findViewById<View>(R.id.tvRegister)

        btnLogin.setOnClickListener {
            val mobile = etMobile.text.toString()
            val password = etPassword.text.toString()

            if (mobile.isNotEmpty() && password.isNotEmpty()) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Please enter mobile number and password", Toast.LENGTH_SHORT).show()
            }
        }

        tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Forgot Password clicked", Toast.LENGTH_SHORT).show()
        }

        tvRegister.setOnClickListener {
            Toast.makeText(this, "Register Now clicked", Toast.LENGTH_SHORT).show()
        }
    }
}