package com.resource.quickbite

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Color

class OrderActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private val realtimeDb = FirebaseDatabase.getInstance().reference

    private lateinit var tvSummary: TextView
    private lateinit var tvTotal: TextView
    private lateinit var orderList: LinearLayout
    private lateinit var btnPlaceOrder: Button

    private var studentName = ""
    private var roll = ""
    private var phone = ""
    private var course = ""
    private var totalCost = 0
    private var itemsList = mutableListOf<Item>()
    private var currentOrderTimer: CountDownTimer? = null

    data class Item(
        val name: String,
        val price: Int,
        var quantity: Int
    )

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)

        tvSummary = findViewById(R.id.tvSummary)
        tvTotal = findViewById(R.id.tvTotal)
        orderList = findViewById(R.id.orderList)
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder)

        val items = intent.getStringArrayListExtra("items") ?: arrayListOf()
        studentName = intent.getStringExtra("name") ?: ""
        roll = intent.getStringExtra("roll") ?: ""
        phone = intent.getStringExtra("phone") ?: ""
        course = intent.getStringExtra("course") ?: ""

        if (roll.isEmpty()) {
            Toast.makeText(this, "Error: Roll number missing!", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Parse received items
        for (i in items) {
            val parts = i.split(":")
            if (parts.size >= 3) {
                val name = parts[0]
                val qty = parts[1].toInt()
                val price = parts[2].toInt()
                itemsList.add(Item(name, price, qty))
            }
        }

        setupOrderList()
        updateSummary()

        btnPlaceOrder.setOnClickListener { placeOrder() }
    }

    private fun setupOrderList() {
        orderList.removeAllViews()
        val inflater = LayoutInflater.from(this)

        for (item in itemsList) {
            val itemLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(20, 20, 20, 20)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 10, 0, 10)
                }
                background = getDrawable(R.drawable.item_background)
            }

            val tvName = TextView(this).apply {
                text = item.name
                textSize = 16f
                setTextColor(Color.BLACK) // ✅ black text
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val qtyLayout = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                gravity = android.view.Gravity.CENTER
            }

            val btnMinus = Button(this).apply {
                text = "-"
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(90, 90)
            }

            val tvQty = TextView(this).apply {
                text = item.quantity.toString()
                textSize = 16f
                setTextColor(Color.BLACK) // ✅ black text
                setPadding(20, 0, 20, 0)
                gravity = android.view.Gravity.CENTER
            }

            val btnPlus = Button(this).apply {
                text = "+"
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(90, 90)
            }

            qtyLayout.addView(btnMinus)
            qtyLayout.addView(tvQty)
            qtyLayout.addView(btnPlus)

            val tvPrice = TextView(this).apply {
                text = "₹${item.price * item.quantity}"
                textSize = 16f
                setTextColor(Color.BLACK) // ✅ black text
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                gravity = android.view.Gravity.END
            }

            btnMinus.setOnClickListener {
                if (item.quantity > 1) {
                    item.quantity--
                    tvQty.text = item.quantity.toString()
                    tvPrice.text = "₹${item.price * item.quantity}"
                    updateSummary()
                }
            }

            btnPlus.setOnClickListener {
                item.quantity++
                tvQty.text = item.quantity.toString()
                tvPrice.text = "₹${item.price * item.quantity}"
                updateSummary()
            }

            itemLayout.addView(tvName)
            itemLayout.addView(qtyLayout)
            itemLayout.addView(tvPrice)

            orderList.addView(itemLayout)
        }
    }

    private fun updateSummary() {
        totalCost = itemsList.sumOf { it.price * it.quantity }

        tvTotal.text = "Total Cost: ₹$totalCost"

        tvSummary.text = buildString {
            append("🛒 Order Summary\n\n")
            for (item in itemsList) {
                append("${item.name} x${item.quantity} - ₹${item.price * item.quantity}\n")
            }
            append("\nTotal: ₹$totalCost")
        }
    }

    private fun placeOrder() {
        val orderId = UUID.randomUUID().toString()
        val timestamp = Timestamp.now()

        val orderData = hashMapOf(
            "orderId" to orderId,
            "name" to studentName,
            "roll" to roll,
            "phone" to phone,
            "course" to course,
            "items" to itemsList.map { mapOf("name" to it.name, "qty" to it.quantity, "price" to it.price) },
            "totalCost" to totalCost,
            "timestamp" to timestamp,
            "status" to "pending"
        )

        val userRef = firestore.collection("users").document(roll)
        val orderRef = userRef.collection("orders").document(orderId)
        val globalOrderRef = firestore.collection("orders").document(orderId)

        val userProfile = hashMapOf(
            "name" to studentName,
            "roll" to roll,
            "phone" to phone,
            "course" to course,
            "lastOrder" to orderId,
            "updatedAt" to timestamp
        )

        userRef.set(userProfile, SetOptions.merge())
            .addOnSuccessListener {
                orderRef.set(orderData)
                    .addOnSuccessListener {
                        globalOrderRef.set(orderData)
                            .addOnSuccessListener {
                                saveToRealtimeDatabase(orderId)
                            }
                    }
            }
    }

    private fun saveToRealtimeDatabase(orderId: String) {
        val orderMap = mapOf(
            "orderId" to orderId,
            "roll" to roll,
            "totalCost" to totalCost,
            "orderedAt" to getCurrentTime(),
            "timer" to "00:05:00"
        )

        val userRef = realtimeDb.child("users").child(roll)

        userRef.child("currentOrder").setValue(orderMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Order placed successfully 🎉", Toast.LENGTH_SHORT).show()
                startOrderTimer(5 * 60 * 1000)

                val intent = Intent(this, OrderDetailsActivity::class.java)
                intent.putExtra("roll", roll)
                intent.putExtra("orderId", orderId)
                startActivity(intent)
                finish()
            }
    }

    private fun startOrderTimer(durationMillis: Long) {
        currentOrderTimer?.cancel()
        currentOrderTimer = object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = millisUntilFinished / 60000
                val seconds = (millisUntilFinished % 60000) / 1000
                findViewById<TextView>(R.id.tvSummary).text =
                    String.format("Time left: %02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                Toast.makeText(this@OrderActivity, "Your order is ready 😋", Toast.LENGTH_LONG).show()
            }
        }.start()
    }

    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    override fun onDestroy() {
        super.onDestroy()
        currentOrderTimer?.cancel()
    }
}
