// CanteenMenuItem.kt
package com.resource.quickbite

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CanteenMenuItem(
    val id: String = "",
    val name: String = "",
    val price: Int = 0,
    var qty: Int = 0,
    val timestamp: Long = 0
) : Parcelable
