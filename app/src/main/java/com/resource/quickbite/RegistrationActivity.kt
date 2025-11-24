package com.resource.quickbite

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class RegistrationActivity : AppCompatActivity() {
    private lateinit var prefs: SharedPreferences
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        db = FirebaseFirestore.getInstance()
        prefs = getSharedPreferences("QuickBitePrefs", MODE_PRIVATE)

        val etName = findViewById<EditText>(R.id.etName)
        val etRoll = findViewById<EditText>(R.id.etRoll)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etCourse = findViewById<EditText>(R.id.etCourse)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val confirmPasswordInput = findViewById<EditText>(R.id.confirmPasswordInput)
        val btnRegisterSubmit = findViewById<Button>(R.id.btnRegisterSubmit)

        btnRegisterSubmit.setOnClickListener {
            val name = etName.text.toString().trim()
            val roll = etRoll.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val course = etCourse.text.toString().trim()
            val pass = passwordInput.text.toString().trim()
            val confirm = confirmPasswordInput.text.toString().trim()

            if (name.isEmpty() || roll.isEmpty() || phone.isEmpty() || course.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Fill all fields!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass != confirm) {
                Toast.makeText(this, "Passwords don't match!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userData = hashMapOf(
                "name" to name,
                "roll" to roll,
                "phone" to phone,
                "course" to course,
                "password" to pass
            )

            db.collection("users").document(roll).set(userData)
                .addOnSuccessListener {
                    prefs.edit().apply {
                        putString("name", name)
                        putString("roll", roll)
                        putString("phone", phone)
                        putString("course", course)
                        putBoolean("isLoggedIn", true)
                        apply()
                    }

                    Toast.makeText(this, "Registered Successfully!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MenuActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error saving user: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
