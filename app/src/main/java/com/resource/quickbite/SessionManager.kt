package com.resource.quickbite

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("QuickBitePrefs", Context.MODE_PRIVATE)

    fun saveLogin(role: String, rollNo: String) {
        prefs.edit().apply {
            putBoolean("isLoggedIn", true)
            putString("role", role)
            putString("rollNo", rollNo)
            apply()
        }
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("isLoggedIn", false)
    }

    fun getRole(): String? = prefs.getString("role", null)
    fun getRoll(): String? = prefs.getString("rollNo", null)

    fun logout() {
        prefs.edit().putBoolean("isLoggedIn", false).apply()
    }
}
