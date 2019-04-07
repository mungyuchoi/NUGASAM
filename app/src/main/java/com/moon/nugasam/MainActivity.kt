package com.moon.nugasam

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.content.Intent
import android.util.Log
import android.widget.ArrayAdapter
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*
import java.util.ArrayList
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class MainActivity : AppCompatActivity(){

    private var toolbar: Toolbar? = null
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: RecyclerView.Adapter<*>? = null
    private var mLayoutManager: RecyclerView.LayoutManager? = null
    private var mData = ArrayList<FirebasePost>()

    // action mode
    var isInActionMode = false
    var selectionList = ArrayList<FirebasePost>()


    private var arrayAdapter: ArrayAdapter<String>? = null
    private var arrayIndex = ArrayList<String>()
    private var arrayData = ArrayList<String>()
    private var sort = "id"
    private var mPostReference: DatabaseReference? = null

    private var ID: String? = null
    private var name: String? = null
    private var nuga: Int = 0

    private var cID: String? = "9"
    private var cName: String? = "최문규"
    private var cNuga: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        toolbar = findViewById<Toolbar>(R.id.toolbar)?.apply{
            setTitle(R.string.app_name)
        }
        setSupportActionBar(toolbar)

        // recyclerview
        mRecyclerView = findViewById<RecyclerView>(R.id.recyclerView)?.apply {
            setHasFixedSize(true)
            mLayoutManager = LinearLayoutManager(this@MainActivity)
            setLayoutManager(mLayoutManager)

            mData.add(FirebasePost("1", "choimungyu", 3))
            mData.add(FirebasePost("2", "dadf", 2))
            mData.add(FirebasePost("3", "sdfsdf", 3))
            mData.add(FirebasePost("4", "sdfsf", 3))
            mData.add(FirebasePost("5", "choimsdfsdfsdfungyu", 3))

            mAdapter = MyAdapter(this@MainActivity, mData)
            setAdapter(mAdapter)

            // TODO data 로드해서 넣어야 합니다.
        }

        FirebaseApp.initializeApp(this)
        // TODO data load해야합니다.
    }

    override fun onBackPressed() {
        if (isInActionMode) {
            clearActionMode()
            mAdapter?.notifyDataSetChanged()
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
        mAdapter?.notifyDataSetChanged()
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        prepareSelection(position)
    }

    fun prepareSelection(position: Int) {
        if (!selectionList.contains(mData.get(position))) {
            selectionList.add(mData.get(position))
        } else {
            selectionList.remove(mData.get(position))
        }

        updateViewCounter()
    }

    fun updateViewCounter() {
        selectionList?.apply {
            var counter = size
            toolbar?.apply {
                if (counter == 1) {
                    // edit
                    getMenu()?.getItem(0)?.isVisible = true
                } else {
                    getMenu()?.getItem(0)?.isVisible = false
                }
                setTitle(counter.toString() + " item(s) selected")
            }
        }
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
            R.id.action_db -> {
                startActivity(
                    Intent(
                        this@MainActivity,
                        SecondActivity::class.java
                    )
                )
                return true
            }
            android.R.id.home, R.id.item_cancel ->{
                clearActionMode()
                mAdapter?.notifyDataSetChanged()
                return true
            }
            R.id.item_done ->{
                Log.d("MQ!", "done clicked")
                clearActionMode()
                mAdapter?.notifyDataSetChanged()
                // TODO 선택한 값을 반영해야합니다.
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    fun clearActionMode(){
        isInActionMode = false;
        toolbar?.apply {
            getMenu().clear()
            inflateMenu(R.menu.main)
            getSupportActionBar()?.setDisplayHomeAsUpEnabled(false)
            setTitle(R.string.app_name)
            selectionList.clear()
        }
    }




//
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

}
