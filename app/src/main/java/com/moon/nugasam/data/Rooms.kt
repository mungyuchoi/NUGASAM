package com.moon.nugasam.data

data class Rooms(
    var imageUrl: String? = null,
    var description: String? = null,
    var title: String? = null,
    var code: Int ? = 9999,
    var users: List<SimpleUser>?= null
)

data class SimpleUser(
    var key: String ="",
    var nuga: Int = 0,
    var permission: Int = 0
)