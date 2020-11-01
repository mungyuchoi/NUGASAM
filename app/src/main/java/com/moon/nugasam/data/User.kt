package com.moon.nugasam.data

data class User(
    var name: String? = null,
    var imageUrl: String? = null,
    var fullName: String? = null,
    var permission: Int = 0,
    var nuga: Int = 0,
    var point: Int = 0,
    var rooms: List<SimpleRoom>? = null
)

data class SimpleRoom(
    var key: String = ""
)