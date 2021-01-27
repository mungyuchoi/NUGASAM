package com.moon.nugasam

import android.content.Context
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
import com.moon.nugasam.constant.PrefConstants
import com.moon.nugasam.data.SimpleUser
import com.moon.nugasam.data.User
import java.util.*

class SecondActivity : AppCompatActivity(), View.OnClickListener {
    var btn_Update: Button? = null
    var editName: EditText? = null
    var editNuga: EditText? = null
    var textName: TextView? = null
    var textNuga: TextView? = null
    var arrayAdapter: ArrayAdapter<String>? = null
    private var simpleUserInfo = ArrayList<SimpleUser>()

    val userKeys = ArrayList<String>()
    val userInfo = ArrayList<User>()

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
                    simpleUserInfo.clear()
                    for (postSnapshot in dataSnapshot.children) {
                        simpleUserInfo.add(
                            postSnapshot.getValue(
                                SimpleUser::class.java
                            )!!
                        )
                    }
                    arrayData.clear()
                    arrayIndex.clear()
                    userInfo.clear()
                    userKeys.clear()
                    var index = 0
                    for(simpleUser in simpleUserInfo){
                        val ref = FirebaseDatabase.getInstance().reference.child("users")
                            .child(simpleUser.key)
                        ref.addListenerForSingleValueEvent(object: ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                (snapshot.getValue(User::class.java) as User).run {
                                    if (userKeys.contains(snapshot.key) || arrayIndex.contains(snapshot.key)) {
                                        return
                                    }
                                    this.nuga = simpleUser.nuga
                                    userInfo.add(this)
                                    userKeys.add(snapshot.key!!)
                                    val info = arrayOf(this.name, this.nuga.toString())
                                    val result = setTextLength(info[0]!!, 10) + setTextLength(info[1]!!, 10)
                                    arrayData.add(result)
                                    arrayIndex.add(snapshot.key!!)
                                    index++
                                    if (simpleUserInfo.size == index) {
                                        sortUsers()
                                        arrayAdapter?.run {
                                            clear()
                                            addAll(arrayData)
                                            notifyDataSetChanged()
                                        }
                                    }

                                }
                            }
                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }
                        })
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w(
                        "getFirebaseDatabase",
                        "loadPost:onCancelled",
                        databaseError.toException()
                    )
                }
            }
            val pref = getSharedPreferences("NUGASAM", Context.MODE_PRIVATE)
            val keyRoom = pref.getString(PrefConstants.KEY_ROOM, "")
            FirebaseDatabase.getInstance().reference.child("rooms").child(keyRoom).child("users")
                .addValueEventListener(postListener)
        }

    private fun sortUsers(){
        (0 until userInfo.size - 1).forEach { i ->
            (0 until userInfo.size - 1).forEach { j ->
                val firstValue = userInfo[j]
                val secondValue = userInfo[j + 1]

                val firstValueKey = userKeys[j]
                val secondValueKey = userKeys[j + 1]

                if (firstValue.nuga > secondValue.nuga) {
                    userInfo.add(j, secondValue)
                    userInfo.removeAt(j + 1)
                    userInfo.add(j + 1, firstValue)
                    userInfo.removeAt(j + 2)

                    userKeys.add(j, secondValueKey)
                    userKeys.removeAt(j + 1)
                    userKeys.add(j + 1, firstValueKey)
                    userKeys.removeAt(j + 2)
                }
            }
        }
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

                val key = getKey(getUser(name)!!)
                for(u in simpleUserInfo) {
                    if(u.key == key){
                        u.nuga = nuga
                        break
                    }
                }

                val pref = getSharedPreferences("NUGASAM", Context.MODE_PRIVATE)
                val keyRoom = pref.getString(PrefConstants.KEY_ROOM, "")
                FirebaseDatabase.getInstance().reference.child("rooms").child(keyRoom)
                    .child("users").setValue(simpleUserInfo)
                Toast.makeText(this, "변경 되었습니다.", Toast.LENGTH_SHORT).show()
                setInsertMode()
            }
        }
    }

    private fun getUser(name: String): User? {
        for ((index, u) in userInfo.withIndex()) {
            if (u.name == name) {
                return userInfo[index]
            }
        }
        return null
    }

    fun getKey(user: User): String {
        for ((index, u) in userInfo.withIndex()) {
            if (u.name == user.name) {
                return userKeys[index]
            }
        }
        return ""
    }

    companion object {
        var arrayIndex = ArrayList<String>()
        var arrayData = ArrayList<String>()
    }
}