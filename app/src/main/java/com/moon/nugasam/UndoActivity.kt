package com.moon.nugasam

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.moon.nugasam.constant.PrefConstants
import com.moon.nugasam.data.History
import kotlinx.android.synthetic.main.activity_undo.*
import java.util.*


class UndoActivity : AppCompatActivity() {

    private var recyclerView: RecyclerView? = null
    private var undoAdapter: UndoAdapter? = null
    private var progress: LottieAnimationView? = null

    private var dataIndex = ArrayList<String>()
    private var datas = ArrayList<History>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_undo)

        supportActionBar?.run {
            title = "히스토리"
            setDisplayHomeAsUpEnabled(true)
        }

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

    private fun loadFirebaseData() {
        val pref = getSharedPreferences("NUGASAM", Context.MODE_PRIVATE)
        val keyRoom = pref.getString(PrefConstants.KEY_ROOM, "")
        FirebaseDatabase.getInstance().reference.child("history").child(keyRoom).orderByChild("date").limitToLast(
            10
        ).apply {
            addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    updateUI(snapshot)
                    progress?.visibility = View.INVISIBLE
                }
            })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
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

    companion object {
        private val TAG = "UndoActivity"
    }
}
