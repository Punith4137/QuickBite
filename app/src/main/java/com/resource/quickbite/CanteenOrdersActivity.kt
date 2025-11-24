package com.resource.quickbite

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class CanteenOrdersActivity : AppCompatActivity() {
    private val db = FirebaseFirestore.getInstance()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_canteen_orders)

        val pendingContainer = findViewById<LinearLayout>(R.id.pendingOrdersContainer)
        val readyContainer = findViewById<LinearLayout>(R.id.readyOrdersContainer)
        val btnMenuEditor = findViewById<Button>(R.id.btnMenuEditor)

        // Real-time updates
        db.collection("orders").orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null || snap == null) return@addSnapshotListener

                pendingContainer.removeAllViews()
                readyContainer.removeAllViews()

                for (doc in snap.documents) {
                    val id = doc.id
                    val name = doc.getString("studentName")
                        ?: doc.getString("name")
                        ?: "Unknown"
                    val roll = doc.getString("rollNo")
                        ?: doc.getString("roll")
                        ?: "—"
                    val phone = doc.getString("phone")
                        ?: doc.getString("phoneNo")
                        ?: "—"
                    val status = doc.getString("status") ?: "pending"

                    // Handle timestamp (may be Timestamp or Long)
                    val time = (doc.getTimestamp("timestamp")?.toDate()
                        ?: doc.getLong("timestamp")?.let { Date(it) })
                    val formatted = if (time != null)
                        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(time)
                    else "—"

                    // Parse items safely
                    val rawItems = doc.get("items") as? List<*> ?: emptyList<Any>()
                    var totalComputed = 0
                    val itemsLayout = LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL
                        setPadding(10, 0, 0, 10)
                    }

                    for (raw in rawItems) {
                        val map = raw as? Map<*, *> ?: continue
                        val itemName = (map["name"] ?: map["itemName"] ?: "").toString()
                        val qty = parseInt(map["qty"] ?: map["quantity"] ?: map["count"])
                        val price = parseInt(map["price"] ?: map["cost"])
                        totalComputed += price * qty

                        val itemText = TextView(this).apply {
                            text = "• $itemName  (x$qty)  -  ₹${price * qty}"
                            textSize = 15f
                            setTextColor(0xFF000000.toInt())
                        }
                        itemsLayout.addView(itemText)
                    }

                    // Use Firestore total if available, else computed total
                    val totalFromDoc =
                        parseInt(doc.get("total") ?: doc.get("totalCost") ?: doc.get("amount"))
                    val total = if (totalFromDoc > 0) totalFromDoc else totalComputed

                    // Create card view
                    val card = CardView(this).apply {
                        radius = 20f
                        cardElevation = 10f
                        useCompatPadding = true
                        setCardBackgroundColor(
                            if (status == "ready") 0xFFDFFFD6.toInt() else 0xFFFFF8DC.toInt()
                        )
                        setContentPadding(40, 30, 40, 30)

                        val layout = LinearLayout(context).apply {
                            orientation = LinearLayout.VERTICAL

                            // Student details
                            val tvHeader = TextView(context).apply {
                                text = "👤 $name"
                                textSize = 20f
                                setTextColor(0xFF000000.toInt())
                                setTypeface(null, android.graphics.Typeface.BOLD)
                            }
                            val tvRoll = TextView(context).apply {
                                text = "Roll No: $roll"
                                textSize = 16f
                                setTextColor(0xFF333333.toInt())
                            }
                            val tvPhone = TextView(context).apply {
                                text = "Phone: $phone"
                                textSize = 16f
                                setTextColor(0xFF333333.toInt())
                            }

                            val tvItemsTitle = TextView(context).apply {
                                text = "\nItems Ordered:"
                                textSize = 17f
                                setTextColor(0xFF000000.toInt())
                                setTypeface(null, android.graphics.Typeface.BOLD)
                                setPadding(0, 10, 0, 5)
                            }

                            val tvTotal = TextView(context).apply {
                                text = "\nTotal: ₹$total"
                                textSize = 17f
                                setTextColor(0xFF000000.toInt())
                                setTypeface(null, android.graphics.Typeface.BOLD)
                            }

                            val tvStatus = TextView(context).apply {
                                text = "Status: ${status.uppercase()}"
                                textSize = 15f
                                setTypeface(null, android.graphics.Typeface.BOLD)
                                setTextColor(
                                    if (status == "ready") 0xFF2E7D32.toInt() else 0xFFE65100.toInt()
                                )
                            }

                            val tvTime = TextView(context).apply {
                                text = "Ordered on: $formatted"
                                textSize = 13f
                                setTextColor(0xFF666666.toInt())
                                setPadding(0, 5, 0, 10)
                            }

                            // Add views
                            addView(tvHeader)
                            addView(tvRoll)
                            addView(tvPhone)
                            addView(tvItemsTitle)
                            addView(itemsLayout)
                            addView(tvTotal)
                            addView(tvStatus)
                            addView(tvTime)

                            // Delete button (only for ready)
                            if (status == "ready") {
                                val btnDelete = Button(context).apply {
                                    text = "Delete Order"
                                    setBackgroundColor(0xFFD32F2F.toInt())
                                    setTextColor(0xFFFFFFFF.toInt())
                                    textSize = 14f
                                }
                                btnDelete.setOnClickListener {
                                    db.collection("orders").document(id).delete()
                                }

                                val btnRow = LinearLayout(context).apply {
                                    gravity = Gravity.END
                                    addView(btnDelete)
                                }
                                addView(btnRow)
                            }
                        }
                        addView(layout)

                        // Open details (only for pending)
                        if (status != "ready") {
                            setOnClickListener {
                                val i = Intent(
                                    this@CanteenOrdersActivity,
                                    CanteenOrderDetailsActivity::class.java
                                )
                                i.putExtra("orderId", id)
                                startActivity(i)
                            }
                        }
                    }

                    // Add to respective section
                    if (status == "ready") readyContainer.addView(card)
                    else pendingContainer.addView(card)
                }
            }

        btnMenuEditor.setOnClickListener {
            startActivity(Intent(this, CanteenMenuActivity::class.java))
        }
    }

    private fun parseInt(value: Any?): Int {
        return when (value) {
            is Number -> value.toInt()
            is String -> value.toIntOrNull() ?: 0
            else -> 0
        }
    }
}
