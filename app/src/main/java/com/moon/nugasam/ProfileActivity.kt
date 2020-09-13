package com.moon.nugasam

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.*
import com.moon.nugasam.data.User

class ProfileActivity : AppCompatActivity() {

    private var query: Query? = null
    private var reorder: String? = null

    private var thumbnail: ImageView? = null
    private var title: TextView? = null
    private var mealwormPoint: TextView? = null
    private var heartPoint: TextView? = null

    private var me: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val pref = getSharedPreferences("NUGASAM", Context.MODE_PRIVATE)
        reorder = pref.getString("reorder", "name")
        loadFirebaseData()

        thumbnail = findViewById(R.id.thumbnail)
        title = findViewById(R.id.title)
        mealwormPoint = findViewById(R.id.mealworm_point)
        heartPoint = findViewById(R.id.heart_point)
    }


    fun loadFirebaseData() {
        if (query == null) {
            query =
                FirebaseDatabase.getInstance().reference.child("users").orderByChild(reorder!!)
                    .apply {
                        addValueEventListener(postListener)
                    }
        } else {
            query?.removeEventListener(postListener)
            query = FirebaseDatabase.getInstance().reference.child("users").orderByChild(reorder!!)
                .apply {
                    addValueEventListener(postListener)
                }
        }
    }

    fun updateUI(dataSnapshot: DataSnapshot) {
        val pref = getSharedPreferences("NUGASAM", Context.MODE_PRIVATE)
        var name = pref.getString("name", "")
        for (postSnapshot in dataSnapshot.children) {
            val key = postSnapshot.key
            val user = postSnapshot.getValue(User::class.java)
            if (name == user!!.name || name == user.fullName) {
                me = user
                var editor = pref.edit()
                editor.putString("key", key)
                editor.commit()
                break
            }
        }

        me.run {
            Glide.with(this@ProfileActivity).load(me!!.imageUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(thumbnail!!)
            title?.text = name
            mealwormPoint?.text = this?.point!!.toString()
            heartPoint?.text = this?.nuga!!.toString()
        }
    }

    private var postListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            updateUI(dataSnapshot)
        }

        override fun onCancelled(p0: DatabaseError) {
        }
    }
}