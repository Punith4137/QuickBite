package com.resource.quickbite

import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class CanteenOrderDetailsActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_canteen_order_details)

        val tvStudent = findViewById<TextView>(R.id.tvStudent)
        val tvOrderItems = findViewById<TextView>(R.id.tvOrderItems)
        val tvTotal = findViewById<TextView>(R.id.tvTotal)
        val tvTime = findViewById<TextView>(R.id.tvTime)
        val btnReady = findViewById<Button>(R.id.btnReady)

        val orderId = intent.getStringExtra("orderId") ?: run {
            Toast.makeText(this, "Order ID missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val docRef = db.collection("orders").document(orderId)
        docRef.get().addOnSuccessListener { doc ->
            if (!doc.exists()) {
                Toast.makeText(this, "Order not found", Toast.LENGTH_SHORT).show()
                finish()
                return@addOnSuccessListener
            }

            // --- student / contact / roll (try multiple possible keys) ---
            val name = doc.getString("studentName")
                ?: doc.getString("name")
                ?: doc.getString("student")
                ?: "—"
            val roll = doc.getString("rollNo")
                ?: doc.getString("roll")
                ?: doc.getString("rollNumber")
                ?: "—"
            val phone = doc.getString("phone")
                ?: doc.getString("phoneNo")
                ?: doc.getString("phoneNumber")
                ?: "—"

            // --- items (defensively parse) ---
            val rawItems = doc.get("items") as? List<*> ?: emptyList<Any>()
            val itemLines = StringBuilder()
            var computedTotal = 0

            for (raw in rawItems) {
                val map = when (raw) {
                    is Map<*, *> -> raw as Map<*, *>
                    else -> null
                } ?: continue

                // name: try several keys
                val itemName = (map["name"] ?: map["itemName"] ?: map["title"] ?: "").toString()

                // qty: handle Long/Double/Int/String keys (qty, quantity, count)
                val qtyAny = map["qty"] ?: map["quantity"] ?: map["count"] ?: map["q"] ?: 0
                val qty = parseIntSafely(qtyAny)

                // price: handle numeric variants and different keys
                val priceAny = map["price"] ?: map["cost"] ?: map["amount"] ?: 0
                val price = parseIntSafely(priceAny)

                val line = "• $itemName  (x$qty)  -  ₹$price"
                itemLines.append(line).append("\n")
                computedTotal += price * qty
            }

            // --- total: try multiple field names, fall back to computedTotal ---
            val totalFromDoc = extractIntFromDoc(doc, listOf("total", "totalCost", "amount", "grandTotal"))
            val finalTotal = if (totalFromDoc > 0) totalFromDoc else computedTotal

            // --- timestamp formatting (try different timestamp keys) ---
            val timestamp = (doc.get("timestamp") as? com.google.firebase.Timestamp)
                ?: doc.get("time") as? com.google.firebase.Timestamp
                ?: run {
                    // sometimes it's stored as numeric millis
                    val longVal = (doc.getLong("timestamp") ?: doc.getLong("time") ?: 0L)
                    if (longVal > 0) Timestamp(Date(longVal)) else null
                }

            val formattedTime = timestamp?.toDate()?.let {
                SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(it)
            } ?: "—"

            // --- display ---
            tvStudent.typeface = Typeface.DEFAULT_BOLD
            tvStudent.textSize = 18f
            tvStudent.text = "Name: $name\nRoll no: $roll\nPhone no: $phone"

            tvOrderItems.textSize = 16f
            tvOrderItems.text = if (itemLines.isNotEmpty()) itemLines.toString().trim() else "No items"

            tvTotal.typeface = Typeface.DEFAULT_BOLD
            tvTotal.textSize = 17f
            tvTotal.text = "💰 Total: ₹$finalTotal"

            tvTime.textSize = 14f
            tvTime.text = "🕒 Ordered on: $formattedTime"
        }.addOnFailureListener { e ->
            Toast.makeText(this, "Failed to load order: ${e.message}", Toast.LENGTH_LONG).show()
        }

        btnReady.setOnClickListener {
            // Update status field (defensively: several code paths read/write "status")
            docRef.update("status", "ready")
                .addOnSuccessListener {
                    Toast.makeText(this, "Order marked as Ready ✅", Toast.LENGTH_SHORT).show()
                    showNotification()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to update status: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    private fun showNotification() {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "order_ready_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Order Updates", NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Order Ready 🍔")
            .setContentText("An order has been marked ready for pickup.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        manager.notify((System.currentTimeMillis() % 10000).toInt(), builder.build())
    }

    // Helper: parse various numeric shapes into Int
    private fun parseIntSafely(value: Any?): Int {
        return when (value) {
            is Number -> value.toInt()
            is String -> value.toIntOrNull() ?: 0
            else -> 0
        }
    }

    // Helper: try several keys on document for integer value
    private fun extractIntFromDoc(doc: com.google.firebase.firestore.DocumentSnapshot, keys: List<String>): Int {
        for (k in keys) {
            val v = doc.get(k) ?: continue
            val parsed = when (v) {
                is Number -> v.toInt()
                is String -> v.toIntOrNull() ?: 0
                else -> 0
            }
            if (parsed > 0) return parsed
        }
        return 0
    }
}
