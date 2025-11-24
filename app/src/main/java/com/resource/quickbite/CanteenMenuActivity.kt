package com.resource.quickbite

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class CanteenMenuActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_canteen_menu)

        val menuContainer = findViewById<LinearLayout>(R.id.menuEditorContainer)
        val btnAddItem = findViewById<Button>(R.id.btnAddItem)

        // 🔹 Real-time Firestore listener for menu
        db.collection("menu")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) return@addSnapshotListener
                menuContainer.removeAllViews()

                for (doc in snap!!.documents) {
                    val id = doc.id
                    val name = doc.getString("name") ?: "Unnamed Item"
                    val price = (doc.getLong("price") ?: 0L).toInt()
                    val estTime = doc.getString("estimatedTime") ?: "00:00"
                    val timestamp = doc.getTimestamp("timestamp")?.toDate()

                    val formattedTime = if (timestamp != null)
                        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(timestamp)
                    else "No Time Info"

                    // 🟡 Create a styled card for each menu item
                    val card = CardView(this).apply {
                        radius = 22f
                        cardElevation = 12f
                        setCardBackgroundColor(0xFFFFF8DC.toInt()) // light yellow
                        useCompatPadding = true
                        setContentPadding(24, 24, 24, 24)

                        val layout = LinearLayout(context).apply {
                            orientation = LinearLayout.VERTICAL

                            val tvName = TextView(context).apply {
                                text = "$name - ₹$price"
                                textSize = 20f
                                setTextColor(resources.getColor(android.R.color.black))
                            }

                            val tvEst = TextView(context).apply {
                                text = "Estimated Time: $estTime hrs"
                                textSize = 16f
                                setTextColor(resources.getColor(android.R.color.darker_gray))
                            }

                            val tvTime = TextView(context).apply {
                                text = "Updated: $formattedTime"
                                textSize = 12f
                                setTextColor(resources.getColor(android.R.color.darker_gray))
                            }

                            // 🟢 Edit and Delete buttons
                            val btnRow = LinearLayout(context).apply {
                                orientation = LinearLayout.HORIZONTAL
                                setPadding(0, 16, 0, 0)

                                val btnEdit = Button(context).apply {
                                    text = "Edit"
                                    setBackgroundColor(0xFF81C784.toInt()) // Green
                                    setTextColor(resources.getColor(android.R.color.white))
                                }

                                val btnDelete = Button(context).apply {
                                    text = "Delete"
                                    setBackgroundColor(0xFFE57373.toInt()) // Red
                                    setTextColor(resources.getColor(android.R.color.white))
                                }

                                btnEdit.setOnClickListener {
                                    openEditPopup(id, name, price, estTime)
                                }

                                btnDelete.setOnClickListener {
                                    AlertDialog.Builder(this@CanteenMenuActivity)
                                        .setTitle("Delete Item")
                                        .setMessage("Are you sure you want to delete \"$name\"?")
                                        .setPositiveButton("Yes") { d, _ ->
                                            db.collection("menu").document(id).delete()
                                            d.dismiss()
                                        }
                                        .setNegativeButton("No", null)
                                        .show()
                                }

                                addView(btnEdit)
                                addView(btnDelete)
                            }

                            addView(tvName)
                            addView(tvEst)
                            addView(tvTime)
                            addView(btnRow)
                        }
                        addView(layout)
                    }

                    menuContainer.addView(card)
                }
            }

        // ➕ Add new item button
        btnAddItem.setOnClickListener { openAddPopup() }
    }

    // 🔸 Popup for Adding Item
    private fun openAddPopup() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.item_canteen_menu, null)
        val etName = dialogView.findViewById<EditText>(R.id.etItemName)
        val etPrice = dialogView.findViewById<EditText>(R.id.etItemPrice)
        val etTimeH = dialogView.findViewById<EditText>(R.id.etHour)
        val etTimeM = dialogView.findViewById<EditText>(R.id.etMinute)

        AlertDialog.Builder(this, R.style.YellowDialog)
            .setTitle("Add New Item")
            .setView(dialogView)
            .setPositiveButton("Add") { d, _ ->
                val name = etName.text.toString().trim()
                val price = etPrice.text.toString().toIntOrNull() ?: 0
                val hr = etTimeH.text.toString().toIntOrNull() ?: 0
                val min = etTimeM.text.toString().toIntOrNull() ?: 0
                val estTime = String.format("%02d:%02d", hr, min)

                if (name.isEmpty()) {
                    Toast.makeText(this, "Please enter item name!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val newItem = mapOf(
                    "name" to name,
                    "price" to price,
                    "estimatedTime" to estTime,
                    "timestamp" to Date()
                )

                db.collection("menu").add(newItem)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Item added successfully!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to add item!", Toast.LENGTH_SHORT).show()
                    }

                d.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // 🔸 Popup for Editing Item
    private fun openEditPopup(id: String, name: String, price: Int, estTime: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.item_canteen_menu, null)
        val etName = dialogView.findViewById<EditText>(R.id.etItemName)
        val etPrice = dialogView.findViewById<EditText>(R.id.etItemPrice)
        val etTimeH = dialogView.findViewById<EditText>(R.id.etHour)
        val etTimeM = dialogView.findViewById<EditText>(R.id.etMinute)

        etName.setText(name)
        etPrice.setText(price.toString())
        val parts = estTime.split(":")
        if (parts.size == 2) {
            etTimeH.setText(parts[0])
            etTimeM.setText(parts[1])
        }

        AlertDialog.Builder(this, R.style.YellowDialog)
            .setTitle("Edit Item")
            .setView(dialogView)
            .setPositiveButton("Save") { d, _ ->
                val newName = etName.text.toString().trim()
                val newPrice = etPrice.text.toString().toIntOrNull() ?: price
                val hr = etTimeH.text.toString().toIntOrNull() ?: 0
                val min = etTimeM.text.toString().toIntOrNull() ?: 0
                val newEstTime = String.format("%02d:%02d", hr, min)

                val updatedItem = mapOf(
                    "name" to newName,
                    "price" to newPrice,
                    "estimatedTime" to newEstTime,
                    "timestamp" to Date()
                )

                db.collection("menu").document(id).set(updatedItem)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Item updated successfully!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Update failed!", Toast.LENGTH_SHORT).show()
                    }

                d.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
