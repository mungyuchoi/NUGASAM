package com.moon.nugasam

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.moon.nugasam.data.History
import com.moon.nugasam.data.User
import kotlinx.android.synthetic.main.activity_undo.*
import java.util.ArrayList

class UndoActivity : AppCompatActivity() {

    private var recyclerView: RecyclerView? = null
    private var undoAdapter: UndoAdapter? = null
    private var progress: LottieAnimationView? = null

    private var dataIndex = ArrayList<String>()
    private var datas = ArrayList<History>()

    private var userDataIndex = ArrayList<String>()
    private var userDatas = ArrayList<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_undo)

        recyclerView = findViewById<RecyclerView>(R.id.recyclerView)?.apply {
            setHasFixedSize(true)
            setLayoutManager(LinearLayoutManager(this@UndoActivity))

            Log.d(TAG, "onCreate datas$datas")

            undoAdapter = UndoAdapter(this@UndoActivity, datas.clone() as ArrayList<History>?)
            setAdapter(undoAdapter)
            Log.d(TAG, "onCreate adapter:$adapter")
            loadFirebaseData()
        }
        progress = findViewById(R.id.refresh)
    }

    fun loadFirebaseData() {
        var query =
            FirebaseDatabase.getInstance().getReference().child("history").orderByChild("date").limitToLast(10).apply {
                var postListener = object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        updateUI(dataSnapshot)
                        progress?.visibility = View.INVISIBLE
                    }

                    override fun onCancelled(p0: DatabaseError) {
                    }
                }
                addValueEventListener(postListener)
            }
        FirebaseDatabase.getInstance().getReference().child("users").apply {
            var postListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    userDatas.clear()
                    userDataIndex.clear()
                    for (postSnapshot in dataSnapshot.children) {
                        val key = postSnapshot.key
                        val user = postSnapshot.getValue(User::class.java)
                        userDatas.add(user!!)
                        userDataIndex.add(key!!)
                    }
                }

                override fun onCancelled(p0: DatabaseError) {
                }
            }
            addValueEventListener(postListener)
        }
    }

    private fun updateUI(dataSnapshot: DataSnapshot) {
        datas.clear()
        dataIndex.clear()
        var children = dataSnapshot.children.reversed()
        Log.d(TAG, "updateUI children count: ${children.size} , progress:$progress")
        if (children.isEmpty()) {
            refresh?.apply {
                visibility = View.VISIBLE
                repeatCount = 1
                scaleType = ImageView.ScaleType.FIT_CENTER
                setAnimation(R.raw.cat)
                playAnimation()
            }
        }
        for (postSnapshot in children) {
            val key = postSnapshot.key
            val undo = postSnapshot.getValue(History::class.java)

            datas.add(undo!!)
            dataIndex.add(key!!)
        }
        undoAdapter?.addAllData(datas)
    }

    fun getKey(user: User): String {
        var index = 0
        for (u in userDatas) {
            if (u.fullName.equals(user.fullName)) {
                return userDataIndex.get(index)
            }
            index++
        }
        return ""
    }

    fun getUser(fullName: String): User? {
        var index = 0
        for (u in userDatas) {
            if (u.fullName.equals(fullName)) {
                return u
            }
        }
        return null
    }

    companion object {
        private val TAG = "UndoActivity"
    }
}