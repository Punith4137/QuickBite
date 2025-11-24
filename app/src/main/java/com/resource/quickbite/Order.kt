package com.resource.quickbite

data class Order(
    var name: String = "",
    var item: String = "",
    val rollNumber: String = "",
    var status: String = "",
    var time: String = ""
)
