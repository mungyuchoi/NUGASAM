package com.moon.nugasam

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*
import java.util.ArrayList
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import com.kongzue.dialog.listener.InputDialogOkButtonClickListener
import com.kongzue.dialog.v2.InputDialog
import com.kongzue.dialog.v2.SelectDialog
import com.moon.nugasam.data.User
import java.util.HashMap


class MainActivity : AppCompatActivity() {

    private var toolbar: Toolbar? = null
    private var recyclerView: RecyclerView? = null
    private var myAdapter: MyAdapter? = null
    private var dataIndex = ArrayList<String>()
    private var datas = ArrayList<User>()
    private var me: User? = null
    private var progress: LottieAnimationView? = null

    // action mode
    var isInActionMode = false
    var selectionList = ArrayList<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FirebaseApp.initializeApp(this)
        toolbar = findViewById<Toolbar>(R.id.toolbar)?.apply {
            setTitle(R.string.app_name)
        }
        setSupportActionBar(toolbar)

        // recyclerview
        recyclerView = findViewById<RecyclerView>(R.id.recyclerView)?.apply {
            setHasFixedSize(true)
            setLayoutManager(LinearLayoutManager(this@MainActivity))

            Log.d(TAG, "onCreate datas$datas")

            myAdapter = MyAdapter(this@MainActivity, datas.clone() as ArrayList<User>?)
            setAdapter(myAdapter)
            Log.d(TAG, "onCreate adapter:$adapter")
            loadFirebaseData()
        }
        progress = findViewById(R.id.refresh)
    }

    override fun onBackPressed() {
        if (isInActionMode) {
            clearActionMode()
            myAdapter?.notifyDataSetChanged()
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    fun prepareToolbar(position: Int) {
        // prepare action mode
        toolbar?.getMenu()?.clear()
        toolbar?.inflateMenu(R.menu.main_action_mode)
        isInActionMode = true
        myAdapter?.notifyDataSetChanged()
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        prepareSelection(position)
    }

    fun prepareSelection(position: Int) {
        if (!selectionList.contains(datas.get(position))) {
            selectionList.add(datas.get(position))
        } else {
            selectionList.remove(datas.get(position))
        }
        updateViewCounter()
    }

    fun getUser(position: Int): User {
        return datas.get(position)
    }


    fun updateViewCounter() {
        selectionList?.apply {
            var counter = size
            toolbar?.apply {
                setTitle(counter.toString() + "명을 선택하셨습니다.")
            }
        }
    }

    private fun getKey(user: User): String {
        var index = 0
        for (u in datas) {
            if (u.name.equals(user.name)) {
                return dataIndex.get(index)
            }
            index++
        }
        return ""
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_profile -> {
                startActivity(
                    Intent(
                        this@MainActivity,
                        GoogleSignInActivity::class.java
                    )
                )
                return true
            }
            R.id.action_reorder -> {
                SelectDialog.show(
                    this@MainActivity,
                    "정렬",
                    "어느 기준으로 정렬하시겠습니까?",
                    "확인",
                    DialogInterface.OnClickListener { dialog, which ->
                        dialog.dismiss()
                    }
                    ,
                    "취소",
                    DialogInterface.OnClickListener { dialog, which ->
                        dialog.dismiss()
                    })
                return true
            }
            R.id.action_qna -> {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("https://moonque.tistory.com/105")
                startActivity(intent)
                return true
            }
            R.id.action_refresh -> {
                progress?.visibility = View.VISIBLE
                FirebaseDatabase.getInstance().getReference().child("users").apply {
                    addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {
                        }

                        override fun onDataChange(dataSnapshot: DataSnapshot) {

                            updateUI(dataSnapshot)
                            progress?.visibility = View.INVISIBLE
                        }
                    })
                }
                return true
            }
            android.R.id.home, R.id.item_cancel -> {
                clearActionMode()
                myAdapter?.notifyDataSetChanged()
                return true
            }
            R.id.item_done -> {
                Log.d(TAG, "done clicked selectionList:$selectionList")
                SelectDialog.build(
                    this@MainActivity, "정말 샀나요?", "", "샀음", DialogInterface.OnClickListener { dialog, inputText ->
                        var ref = FirebaseDatabase.getInstance().getReference()
                        val childUpdates = HashMap<String, Any>()
                        for (user in selectionList) {
                            var key = getKey(user)
                            user.nuga += -1
                            me?.let {
                                it.nuga += 1
                            }
                            Log.d(TAG, "key:$key")
                            childUpdates.put("/users/" + key, user)

                        }
                        Log.i(TAG, "done me : $me")
                        me?.let { childUpdates.put("/users/" + getKey(me!!), it) }
                        Log.i(TAG, "done childUpdates: $childUpdates")
                        ref.updateChildren(childUpdates)
                        clearActionMode()
                        myAdapter?.notifyDataSetChanged()
                        dialog.dismiss()
                    }, "취소", DialogInterface.OnClickListener { dialog, which ->
                        dialog.dismiss()
                        finish()
                    }).apply {
                    setDialogStyle(1)
                    showDialog()
                }
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    fun clearActionMode() {
        isInActionMode = false
        toolbar?.apply {
            getMenu().clear()
            inflateMenu(R.menu.main)
            getSupportActionBar()?.setDisplayHomeAsUpEnabled(false)
            setTitle(R.string.app_name)
            selectionList.clear()
        }
    }

    fun loadFirebaseData() {
        var usersRef = FirebaseDatabase.getInstance().getReference().child("users").apply {
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
    }

    private fun updateUI(dataSnapshot: DataSnapshot) {
        Log.d(TAG, "onDataChange:$dataSnapshot")
        val pref = getSharedPreferences("NUGASAM", Context.MODE_PRIVATE)
        var name = pref.getString("name", "")
        datas.clear()
        dataIndex.clear()
        for (postSnapshot in dataSnapshot.children) {
            val key = postSnapshot.key
            val user = postSnapshot.getValue(User::class.java)
            datas.add(user!!)
            dataIndex.add(key!!)

            if (name.equals(user.fullName)) {
                me = user
                var editor = pref.edit()
                editor.putString("key", key)
                editor.commit()
            }
        }
        Log.d(TAG, "onDataChange adapter:$myAdapter, datas:$datas, dataIndex:$dataIndex")
        Log.d(TAG, "myAdapter datas:$datas")
        myAdapter?.addAllData(datas)
    }

//    override fun onItemLongClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long): Boolean {
//        Log.d("MQ!", "ID: $ID")
//        ID = NugaUtils.getPositionString(arrayData?.get(position), 0)
//        Log.d("MQ!", "ID: $ID")
//        name = NugaUtils.getPositionString(arrayData?.get(position), 1)
//        nuga = Integer.parseInt(NugaUtils.getPositionString(arrayData?.get(position), 2)) + 1
//        mPostReference = FirebaseDatabase.getInstance().reference
//        var childUpdates = HashMap<String, Any>()
//        var postValues: Map<String, Any>? = null
//        var post = FirebasePost(ID, name, nuga)
//        postValues = post.toMap()
//        Log.d("MQ!", "postDB id:$ID, name:$name, nuga:$nuga")
//        childUpdates?.put("/id_list/" + ID, postValues!!)
//
//        cNuga--
//        var cpostValues: Map<String, Any>? = null
//        post = FirebasePost(cID, cName, cNuga)
//        cpostValues = post.toMap()
//        Log.d("MQ!", "postDB current id:$cID, name:$cName, nuga:$cNuga")
//        childUpdates?.put("/id_list/" + cID, cpostValues!!)
//        mPostReference?.updateChildren(childUpdates)
//
//
//        getFirebaseDatabase()
//        return true
//    }
//
//
//    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//        Log.d("MQ!", "ID: $ID")
//        ID = NugaUtils.getPositionString(arrayData?.get(position), 0)
//        Log.d("MQ!", "ID: $ID")
//        name = NugaUtils.getPositionString(arrayData?.get(position), 1)
//        nuga = Integer.parseInt(NugaUtils.getPositionString(arrayData?.get(position), 2)) - 1
//        postFirebaseDatabase(true)
//        getFirebaseDatabase()
//    }
//
//    fun getFirebaseDatabase() {
//        val postListener = object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                Log.e("getFirebaseDatabase", "key: " + dataSnapshot.childrenCount)
//                arrayData.clear()
//                arrayIndex.clear()
//                for (postSnapshot in dataSnapshot.children) {
//                    val key = postSnapshot.key
//                    val get = postSnapshot.getValue(FirebasePost::class.java)
//                    val info = arrayOf(get!!.id, get.name, get.nuga.toString())
//                    if (get?.name == "최문규") {
//                        cID = get.id
//                        cName = "최문규"
//                        cNuga = get.nuga
//                    }
//                    val Result = setTextLength(info[0], 10) + setTextLength(info[1], 10) + setTextLength(
//                        info[2],
//                        10
//                    )
//                    arrayData.add(Result)
//                    arrayIndex.add(key!!)
//                    Log.d("getFirebaseDatabase", "key: " + key!!)
//                    Log.d("getFirebaseDatabase", "info: " + info[0] + info[1] + info[2])
//                }
//                arrayAdapter?.clear()
//                arrayAdapter?.addAll(arrayData)
//                arrayAdapter?.notifyDataSetChanged()
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                Log.w("getFirebaseDatabase", "loadPost:onCancelled", databaseError.toException())
//            }
//        }
//        val sortbyAge = FirebaseDatabase.getInstance().reference.child("id_list").orderByChild("nuga")
//        sortbyAge.addListenerForSingleValueEvent(postListener)
//    }
//
//    private fun setTextLength(text: String, length: Int): String {
//        var text = text
//        if (text.length < length) {
//            val gap = length - text.length
//            for (i in 0 until gap) {
//                text = "$text "
//            }
//        }
//        return text
//    }
//
//
//    fun postFirebaseDatabase(add: Boolean) {
//        mPostReference = FirebaseDatabase.getInstance().reference
//        var childUpdates = HashMap<String, Any>()
//        var postValues: Map<String, Any>? = null
//        if (add) {
//            val post = FirebasePost(ID, name, nuga)
//            postValues = post.toMap()
//        }
//        Log.d("MQ!", "postDB id:$ID, name:$name, nuga:$nuga")
//        childUpdates?.put("/id_list/" + ID, postValues!!)
//
//        cNuga++
//        var cpostValues: Map<String, Any>? = null
//        if (add) {
//            val post = FirebasePost(cID, cName, cNuga)
//            cpostValues = post.toMap()
//        }
//        Log.d("MQ!", "postDB current id:$cID, name:$cName, nuga:$cNuga")
//        childUpdates?.put("/id_list/" + cID, cpostValues!!)
//        mPostReference?.updateChildren(childUpdates)
//    }

    companion object {
        private val TAG = "MainActivity"
    }

}
