package com.resource.quickbite

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash) // optional: if you have a splash layout

        prefs = getSharedPreferences("QuickBitePrefs", MODE_PRIVATE)

        // 3-second delay before navigation
        Handler(Looper.getMainLooper()).postDelayed({
            val password = prefs.getString("password", null)
            val isLoggedIn = prefs.getBoolean("isLoggedIn", false)

            when {
                password == null -> {
                    // No registration yet
                    startActivity(Intent(this, StudentLoginActivity::class.java))
                }
                isLoggedIn -> {
                    // Registered and already logged in
                    startActivity(Intent(this, MenuActivity::class.java))
                }
                else -> {
                    // Registered but not logged in currently
                    startActivity(Intent(this, StudentLoginActivity::class.java))
                }
            }
            finish()
        }, 2000) // 3000 milliseconds = 3 seconds
    }
}
