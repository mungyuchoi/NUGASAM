package com.moon.nugasam.data

data class History(
    var date: String? = null,
    var me: User? = null,
    var who: List<User>? = null
)