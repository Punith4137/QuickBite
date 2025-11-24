package com.resource.quickbite

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class StudentLoginActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var db: FirebaseFirestore

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_login)

        prefs = getSharedPreferences("QuickBitePrefs", MODE_PRIVATE)
        db = FirebaseFirestore.getInstance()

        val etRoll = findViewById<EditText>(R.id.etRoll)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnCanteen = findViewById<Button>(R.id.btnCanteen)

        // ✅ Auto-login check
        if (prefs.getBoolean("isLoggedIn", false)) {
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        btnLogin.setOnClickListener {
            val roll = etRoll.text.toString().trim()
            val pass = passwordInput.text.toString().trim()

            if (roll.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Enter Roll Number and Password!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            db.collection("users").document(roll).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val savedPass = doc.getString("password")
                        val name = doc.getString("name") ?: ""
                        val phone = doc.getString("phone") ?: ""
                        val course = doc.getString("course") ?: ""

                        if (pass == savedPass) {
                            prefs.edit().apply {
                                putString("roll", roll)
                                putString("name", name)
                                putString("phone", phone)
                                putString("course", course)
                                putBoolean("isLoggedIn", true)
                                apply()
                            }

                            Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()

                            val i = Intent(this, MenuActivity::class.java)
                            startActivity(i)
                            finish()
                        } else {
                            Toast.makeText(this, "Incorrect password!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Roll number not registered!", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }

        btnCanteen.setOnClickListener {
            startActivity(Intent(this, CanteenLoginActivity::class.java))
        }
    }
}
