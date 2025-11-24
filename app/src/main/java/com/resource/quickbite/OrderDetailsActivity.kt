package com.resource.quickbite

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class OrderDetailsActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var listener: ListenerRegistration? = null
    private var timer: CountDownTimer? = null
    private val CHANNEL_ID = "quickbite_channel"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_details)
        createNotificationChannel()

        val orderId = intent.getStringExtra("orderId") ?: return
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val tvTimer = findViewById<TextView>(R.id.tvTimer)
        val tvItems = findViewById<TextView>(R.id.tvItems)

        listener = db.collection("orders").document(orderId)
            .addSnapshotListener { snap, err ->
                if (err != null) return@addSnapshotListener
                if (snap != null && snap.exists()) {
                    val status = snap.getString("status") ?: "pending"
                    val name = snap.getString("studentName") ?: ""
                    val items = snap.get("items") as? List<Map<String, Any>> ?: emptyList()

                    // 🔹 Update status text
                    tvStatus.text = "Status: ${status.uppercase()}"

                    // 🔹 Display all ordered items
                    val itemsText = items.joinToString("\n") {
                        val t = it["estimatedTime"]?.toString() ?: "00:10" // default 10 min
                        "${it["name"]} x${it["qty"]} - ₹${it["price"]} (Est: $t)"
                    }
                    tvItems.text = itemsText

                    // 🔹 Calculate MAX estimated time in minutes properly
                    val maxMillis = items.maxOfOrNull {
                        val t = it["estimatedTime"]?.toString()?.trim() ?: "00:10"
                        val parts = t.split(":")
                        val hr = parts.getOrNull(0)?.toIntOrNull() ?: 0
                        val min = parts.getOrNull(1)?.toIntOrNull() ?: 0
                        ((hr * 60) + min) * 60 * 1000L
                    } ?: 10 * 60 * 1000L

                    // 🔹 Cancel previous timer before starting new
                    timer?.cancel()

                    if (status != "ready") {
                        startCountdown(tvTimer, maxMillis, orderId, name)
                    } else {
                        tvTimer.text = "Your order is ready!"
                        showNotification("Your order is ready!", "Order for $name is ready to pick up.")
                    }
                }
            }
    }

    // 🔹 Countdown for the estimated time
    private fun startCountdown(tv: TextView, durationMillis: Long, orderId: String, name: String) {
        timer = object : CountDownTimer(durationMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val totalSeconds = millisUntilFinished / 1000
                val hours = totalSeconds / 3600
                val minutes = (totalSeconds % 3600) / 60
                val seconds = totalSeconds % 60

                when {
                    millisUntilFinished > durationMillis * 0.5 -> tv.setTextColor(getColor(android.R.color.holo_green_dark))
                    millisUntilFinished > durationMillis * 0.2 -> tv.setTextColor(getColor(android.R.color.holo_orange_dark))
                    else -> tv.setTextColor(getColor(android.R.color.holo_red_dark))
                }

                tv.text = String.format("Estimated Time Left: %02d:%02d:%02d", hours, minutes, seconds)
            }

            override fun onFinish() {
                // When countdown completes
                db.collection("orders").document(orderId)
                    .get()
                    .addOnSuccessListener { snap ->
                        val currentStatus = snap?.getString("status") ?: "pending"
                        if (currentStatus != "ready") {
                            tv.text = "Sorry for the delay (Extra 10 min)"
                            startDelayTimer(tv, orderId, name)
                        } else {
                            tv.text = "Your order is ready!"
                            showNotification("Your order is ready!", "Order for $name is ready to pick up.")
                        }
                    }
            }
        }.start()
    }

    // 🔹 Extra 10-minute delay timer
    private fun startDelayTimer(tv: TextView, orderId: String, name: String) {
        timer = object : CountDownTimer(10 * 60 * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val totalSeconds = millisUntilFinished / 1000
                val minutes = (totalSeconds % 3600) / 60
                val seconds = totalSeconds % 60
                tv.setTextColor(getColor(android.R.color.holo_red_dark))
                tv.text = String.format("Delay Time: %02d:%02d remaining", minutes, seconds)
            }

            override fun onFinish() {
                db.collection("orders").document(orderId)
                    .get()
                    .addOnSuccessListener { snap ->
                        val currentStatus = snap?.getString("status") ?: "pending"
                        if (currentStatus != "ready") {
                            tv.text = "Order still not ready"
                            showNotification("Order Delayed", "Order for $name is delayed further.")
                        } else {
                            tv.text = "Your order is ready!"
                            showNotification("Your order is ready!", "Order for $name is ready to pick up.")
                        }
                    }
            }
        }.start()
    }

    // 🔹 Create Notification Channel (for Android 8+)
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Quick Bite Channel"
            val desc = "Order updates"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = desc
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    // 🔹 Show notification
    private fun showNotification(title: String, text: String) {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            notify((System.currentTimeMillis() % 10000).toInt(), builder.build())
        }
    }

    override fun onDestroy() {
        listener?.remove()
        timer?.cancel()
        super.onDestroy()
    }
}
