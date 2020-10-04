package com.moon.nugasam

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.moon.nugasam.data.User
import java.util.*

class SecondActivity : AppCompatActivity(), View.OnClickListener {
    var btn_Update: Button? = null
    var editName: EditText? = null
    var editNuga: EditText? = null
    var textName: TextView? = null
    var textNuga: TextView? = null
    var arrayAdapter: ArrayAdapter<String>? = null
    private var datas = ArrayList<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_second)
        title = "관리자 모드"
        btn_Update = findViewById<View>(R.id.btn_update) as Button
        btn_Update!!.setOnClickListener(this)
        editName = findViewById<View>(R.id.edit_name) as EditText
        editNuga = findViewById<View>(R.id.edit_nuga) as EditText
        textName = findViewById<View>(R.id.text_name) as TextView
        textNuga = findViewById<View>(R.id.text_nuga) as TextView
        arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        val listView =
            findViewById<View>(R.id.db_list_view) as ListView
        listView.adapter = arrayAdapter
        listView.onItemClickListener = onClickListener
        firebaseDatabase
        btn_Update!!.isEnabled = false
    }

    fun setInsertMode() {
        editName!!.setText("")
        editNuga!!.setText("")
        btn_Update!!.isEnabled = false
    }

    private val onClickListener = OnItemClickListener { parent, view, position, id ->
        Log.e("On Click", "position = $position")
        Log.e(
            "On Click",
            "Data: " + arrayData[position]
        )
        val tempData =
            arrayData[position].split("\\s+".toRegex())
                .toTypedArray()
        Log.e("On Click", "Split Result = $tempData")
        editName!!.setText(tempData[0].trim { it <= ' ' })
        editNuga!!.setText(tempData[1].trim { it <= ' ' })
        btn_Update!!.isEnabled = true
    }

    val firebaseDatabase: Unit
        get() {
            val postListener: ValueEventListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Log.d("getFirebaseDatabase", "key: " + dataSnapshot.childrenCount)
                    arrayData.clear()
                    arrayIndex.clear()
                    datas.clear()
                    for (postSnapshot in dataSnapshot.children) {
                        val key = postSnapshot.key
                        val user =
                            postSnapshot.getValue(
                                User::class.java
                            )
                        datas.add(user!!)
                        val info =
                            arrayOf(user!!.name, user.nuga.toString())
                        val Result =
                            setTextLength(info[0]!!, 10) + setTextLength(info[1]!!, 10)
                        arrayData.add(Result)
                        arrayIndex.add(key!!)
                        Log.d("getFirebaseDatabase", "key: $key")
                        Log.d("getFirebaseDatabase", "info: " + info[0] + info[1])
                    }
                    arrayAdapter!!.clear()
                    arrayAdapter!!.addAll(arrayData)
                    arrayAdapter!!.notifyDataSetChanged()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w(
                        "getFirebaseDatabase",
                        "loadPost:onCancelled",
                        databaseError.toException()
                    )
                }
            }
            FirebaseDatabase.getInstance().reference.child("users").orderByChild("name")
                .addValueEventListener(postListener)
        }

    fun setTextLength(text: String, length: Int): String {
        var text = text
        if (text.length < length) {
            val gap = length - text.length
            for (i in 0 until gap) {
                text = "$text "
            }
        }
        return text
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_update -> {
                val name = editName!!.text.toString()
                val nuga = editNuga!!.text.toString().toInt()

                getUser(name)?.let{
                    var ref = FirebaseDatabase.getInstance().reference
                    var key = getKey(it)
                    ref.child("users").child(key).child("nuga").setValue(nuga)
                }

                Toast.makeText(this, "변경 되었습니다.", Toast.LENGTH_SHORT).show()
                setInsertMode()
            }
        }
    }

    private fun getUser(name: String): User? {
        for (u in datas) {
            if (u.name == name) {
                return u
            }
        }
        return null
    }

    private fun getKey(user: User): String {
        var index = 0
        for (u in datas) {
            if (u.name == user.name) {
                return arrayIndex[index]
            }
            index++
        }
        return ""
    }

    companion object {
        var arrayIndex = ArrayList<String>()
        var arrayData = ArrayList<String>()
    }
}