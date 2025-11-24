package com.resource.quickbite

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class MenuActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var menuContainer: LinearLayout
    private lateinit var previousOrdersSection: LinearLayout
    private lateinit var previousOrdersContainer: LinearLayout
    private lateinit var currentOrderSection: LinearLayout
    private lateinit var currentOrderContainer: LinearLayout
    private val selectedItems = mutableListOf<CanteenMenuItem>()

    private lateinit var prefs: android.content.SharedPreferences
    private var studentName = ""
    private var roll = ""
    private var phone = ""
    private var course = ""

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        prefs = getSharedPreferences("QuickBitePrefs", MODE_PRIVATE)

        menuContainer = findViewById(R.id.menuContainer)
        previousOrdersSection = findViewById(R.id.previousOrdersSection)
        previousOrdersContainer = findViewById(R.id.previousOrdersContainer)
        currentOrderSection = findViewById(R.id.currentOrderSection)
        currentOrderContainer = findViewById(R.id.currentOrderContainer)

        val btnProfile = findViewById<Button>(R.id.btnProfile)
        val btnGoOrder = findViewById<Button>(R.id.btnGoOrder)

        studentName = prefs.getString("name", "") ?: ""
        roll = prefs.getString("roll", "") ?: ""
        phone = prefs.getString("phone", "") ?: ""
        course = prefs.getString("course", "") ?: ""

        btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        btnGoOrder.setOnClickListener {
            if (selectedItems.isEmpty()) {
                Toast.makeText(this, "Select at least one item!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val itemsToSend = ArrayList<String>()
            for (item in selectedItems) {
                itemsToSend.add("${item.name}:1:${item.price}")
            }

            val i = Intent(this, OrderActivity::class.java)
            i.putStringArrayListExtra("items", itemsToSend)
            i.putExtra("name", studentName)
            i.putExtra("roll", roll)
            i.putExtra("phone", phone)
            i.putExtra("course", course)
            startActivity(i)
        }

        loadUserOrders()
        loadMenuRealtime()
    }

    /** 🔥 Load user's orders and separate pending vs completed properly */
    /** 🔥 Load user's orders and show only latest pending order in "Current" */
    private fun loadUserOrders() {
        if (roll.isEmpty()) {
            Toast.makeText(this, "Roll number missing", Toast.LENGTH_SHORT).show()
            return
        }

        val userRef = db.collection("users").document(roll)
        userRef.collection("orders")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                previousOrdersContainer.removeAllViews()
                currentOrderContainer.removeAllViews()

                var hasCurrentOrder = false
                var hasPreviousOrder = false
                var latestPendingAdded = false

                for (doc in snapshot.documents) {
                    val data = doc.data ?: continue
                    val status = data["status"]?.toString()?.lowercase(Locale.ROOT) ?: "pending"

                    // 🟢 Show only the *most recent* pending/preparing order in Current Orders
                    if (!latestPendingAdded && (status == "pending" || status == "preparing")) {
                        currentOrderContainer.addView(createCurrentOrderCard(data))
                        hasCurrentOrder = true
                        latestPendingAdded = true // ensure only one (latest) order goes here
                    } else {
                        // ⚪ All others go to Previous Orders
                        previousOrdersContainer.addView(createPreviousOrderCard(data))
                        hasPreviousOrder = true
                    }
                }

                currentOrderSection.visibility = if (hasCurrentOrder) LinearLayout.VISIBLE else LinearLayout.GONE
                previousOrdersSection.visibility = if (hasPreviousOrder) LinearLayout.VISIBLE else LinearLayout.GONE
            }
    }


    /** 🟢 Current Orders Card */
    @SuppressLint("SetTextI18n")
    private fun createCurrentOrderCard(order: Map<String, Any>): CardView {
        val card = CardView(this).apply {
            radius = 20f
            cardElevation = 8f
            setCardBackgroundColor(Color.parseColor("#D1FFD1"))
            setContentPadding(24, 24, 24, 24)
        }

        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val dateStr = (order["timestamp"] as? Timestamp)?.toDate()?.let { sdf.format(it) } ?: "Unknown time"

        val tvTitle = TextView(this).apply {
            text = "Ordered On: $dateStr"
            textSize = 16f
            setTextColor(Color.BLACK)
        }

        val items = order["items"] as? List<Map<String, Any>> ?: emptyList()
        val tvItems = TextView(this).apply {
            text = items.joinToString("\n") { "${it["name"]} x${it["qty"]} - ₹${it["price"]}" }
            textSize = 15f
            setTextColor(Color.DKGRAY)
        }

        val orderId = order["orderId"]?.toString() ?: ""
        val status = order["status"]?.toString()?.replaceFirstChar { it.uppercase() } ?: "Pending"
        val tvStatus = TextView(this).apply {
            text = "Status: $status"
            textSize = 16f
            setTextColor(Color.parseColor("#00796B"))
        }

        card.setOnClickListener {
            val intent = Intent(this, OrderDetailsActivity::class.java)
            intent.putExtra("roll", roll)
            intent.putExtra("orderId", orderId)
            startActivity(intent)
        }

        layout.addView(tvTitle)
        layout.addView(tvItems)
        layout.addView(tvStatus)
        card.addView(layout)

        return card
    }

    /** ⚪ Previous Orders Card */
    @SuppressLint("SetTextI18n")
    private fun createPreviousOrderCard(order: Map<String, Any>): CardView {
        val card = CardView(this).apply {
            radius = 20f
            cardElevation = 8f
            setCardBackgroundColor(Color.WHITE)
            setContentPadding(24, 24, 24, 24)
        }

        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val dateStr = (order["timestamp"] as? Timestamp)?.toDate()?.let { sdf.format(it) } ?: "Unknown time"

        val tvTime = TextView(this).apply {
            text = "Ordered On: $dateStr"
            textSize = 16f
            setTextColor(Color.BLACK)
        }

        val items = order["items"] as? List<Map<String, Any>> ?: emptyList()
        val tvItems = TextView(this).apply {
            text = items.joinToString("\n") { "${it["name"]} x${it["qty"]} - ₹${it["price"]}" }
            textSize = 15f
            setTextColor(Color.DKGRAY)
        }

        val totalCost = order["totalCost"]?.toString() ?: "0"
        val tvCost = TextView(this).apply {
            text = "Total: ₹$totalCost"
            textSize = 16f
            setTextColor(Color.parseColor("#00796B"))
        }

        layout.addView(tvTime)
        layout.addView(tvItems)
        layout.addView(tvCost)
        card.addView(layout)

        return card
    }

    /** 🍴 Menu load — real-time updates */
    private fun loadMenuRealtime() {
        db.collection("menu")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                menuContainer.removeAllViews()
                for (doc in snapshot.documents) {
                    val item = CanteenMenuItem(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        price = (doc.getLong("price") ?: 0L).toInt()
                    )
                    val card = createItemCard(item)
                    menuContainer.addView(card)
                }
            }
    }

    /** 🍛 Menu item cards */
    @SuppressLint("SetTextI18n")
    private fun createItemCard(item: CanteenMenuItem): CardView {
        val inflater = layoutInflater
        val cardView = inflater.inflate(R.layout.activity_menu_item, null) as CardView

        val tvName = cardView.findViewById<TextView>(R.id.tvItemName)
        val tvPrice = cardView.findViewById<TextView>(R.id.tvPrice)

        tvName.text = item.name
        tvPrice.text = "₹${item.price}"

        cardView.setOnClickListener {
            if (selectedItems.contains(item)) {
                selectedItems.remove(item)
                cardView.setCardBackgroundColor(Color.WHITE)
            } else {
                selectedItems.add(item)
                cardView.setCardBackgroundColor(Color.parseColor("#A5FF7A"))
            }
        }
        return cardView
    }
}
