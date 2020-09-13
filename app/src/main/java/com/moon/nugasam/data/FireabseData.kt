package com.moon.nugasam.data

import android.content.Context

class FirebaseData private constructor(context: Context) {

    fun getUsers() {}

    fun getMe() {

    }

    companion object {
        @Volatile
        private var INSTANCE: FirebaseData? = null
        @JvmStatic
        fun getInstance(context: Context): FirebaseData =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirebaseData(context).also {
                    INSTANCE = it
                }
            }
    }
}