package com.moon.nugasam.data

data class Rooms(
    var imageUrl: String?,
    var title: String,
    var description: String? = null,
    var users: List<SimpleUser>?
)

data class SimpleUser(
    var key: String ="",
    var nuga: Int = 0,
    var permission: Int = 0
)