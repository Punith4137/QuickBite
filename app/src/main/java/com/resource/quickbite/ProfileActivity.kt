package com.resource.quickbite

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val tvName = findViewById<TextView>(R.id.tvName)
        val tvRoll = findViewById<TextView>(R.id.tvRoll)
        val tvPhone = findViewById<TextView>(R.id.tvPhone)
        val tvCourse = findViewById<TextView>(R.id.tvCourse)
        val btnEdit = findViewById<Button>(R.id.btnEdit)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        val prefs = getSharedPreferences("QuickBitePrefs", MODE_PRIVATE)
        var name = prefs.getString("name", "") ?: ""
        var roll = prefs.getString("roll", "") ?: ""
        var phone = prefs.getString("phone", "") ?: ""
        var course = prefs.getString("course", "") ?: ""

        fun updateTextViews() {
            tvName.text = "Name: $name"
            tvRoll.text = "Roll No: $roll"
            tvPhone.text = "Phone: $phone"
            tvCourse.text = "Course: $course"
        }

        updateTextViews()

        // ✅ Edit profile (all fields)
        btnEdit.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()

            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()

            val editName = dialogView.findViewById<EditText>(R.id.editName)
            val editRoll = dialogView.findViewById<EditText>(R.id.editRoll)
            val editCourse = dialogView.findViewById<EditText>(R.id.editCourse)
            val editPhone = dialogView.findViewById<EditText>(R.id.editPhone)
            val btnSave = dialogView.findViewById<Button>(R.id.btnSave)
            val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

            // Get existing data
            val prefs = getSharedPreferences("QuickBitePrefs", MODE_PRIVATE)
            editName.setText(prefs.getString("name", ""))
            editRoll.setText(prefs.getString("roll", ""))
            editCourse.setText(prefs.getString("course", ""))
            editPhone.setText(prefs.getString("phone", ""))

            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            btnSave.setOnClickListener {
                val newName = editName.text.toString()
                val newRoll = editRoll.text.toString()
                val newCourse = editCourse.text.toString()
                val newPhone = editPhone.text.toString()

                if (newName.isEmpty() || newRoll.isEmpty() || newCourse.isEmpty() || newPhone.isEmpty()) {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                prefs.edit()
                    .putString("name", newName)
                    .putString("roll", newRoll)
                    .putString("course", newCourse)
                    .putString("phone", newPhone)
                    .apply()

                // Update UI instantly
                tvName.text = "Name: $newName"
                tvRoll.text = "Roll: $newRoll"
                tvCourse.text = "Course: $newCourse"
                tvPhone.text = "Phone: $newPhone"

                Toast.makeText(this, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }


        // ✅ Logout button
        btnLogout.setOnClickListener {
            prefs.edit().putBoolean("isLoggedIn", false).apply()
            startActivity(Intent(this, StudentLoginActivity::class.java))
            finish()
        }
    }
}
