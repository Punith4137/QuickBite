package com.resource.quickbite

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.resource.quickbite.R.layout.activity_canteen_login

class CanteenLoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_canteen_login)

        val etId = findViewById<EditText>(R.id.etId)
        val etPass = findViewById<EditText>(R.id.etPass)
        val btnEnter = findViewById<Button>(R.id.btnEnter)

        btnEnter.setOnClickListener {
            // prototype: hardcoded check (replace with Firebase Auth or Firestore staff)
            if (etId.text.toString() == "admin" && etPass.text.toString() == "1234") {
                startActivity(Intent(this, CanteenOrdersActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
