package com.moon.nugasam

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*
import com.moon.nugasam.utils.NugaUtils
import java.util.ArrayList
import java.util.HashMap


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {




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
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        val listView = findViewById(R.id.db_list_view) as ListView
        listView.adapter = arrayAdapter
        listView.onItemClickListener = this
        listView.onItemLongClickListener = this

        FirebaseApp.initializeApp(this)
        getFirebaseDatabase()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
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
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_camera -> {
                // Handle the camera action
            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_manage -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }


    override fun onItemLongClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long): Boolean {
        Log.d("MQ!", "ID: $ID")
        ID = NugaUtils.getPositionString(arrayData?.get(position), 0)
        Log.d("MQ!", "ID: $ID")
        name = NugaUtils.getPositionString(arrayData?.get(position), 1)
        nuga = Integer.parseInt(NugaUtils.getPositionString(arrayData?.get(position), 2)) + 1
        mPostReference = FirebaseDatabase.getInstance().reference
        var childUpdates = HashMap<String, Any>()
        var postValues: Map<String, Any>? = null
        var post = FirebasePost(ID, name, nuga)
        postValues = post.toMap()
        Log.d("MQ!", "postDB id:$ID, name:$name, nuga:$nuga")
        childUpdates?.put("/id_list/" + ID, postValues!!)

        cNuga--
        var cpostValues: Map<String, Any>? = null
        post = FirebasePost(cID, cName, cNuga)
        cpostValues = post.toMap()
        Log.d("MQ!", "postDB current id:$cID, name:$cName, nuga:$cNuga")
        childUpdates?.put("/id_list/" + cID, cpostValues!!)
        mPostReference?.updateChildren(childUpdates)


        getFirebaseDatabase()
        return true
    }


    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        Log.d("MQ!", "ID: $ID")
        ID = NugaUtils.getPositionString(arrayData?.get(position), 0)
        Log.d("MQ!", "ID: $ID")
        name = NugaUtils.getPositionString(arrayData?.get(position), 1)
        nuga = Integer.parseInt(NugaUtils.getPositionString(arrayData?.get(position), 2)) - 1
        postFirebaseDatabase(true)
        getFirebaseDatabase()
    }

    fun getFirebaseDatabase() {
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.e("getFirebaseDatabase", "key: " + dataSnapshot.childrenCount)
                arrayData.clear()
                arrayIndex.clear()
                for (postSnapshot in dataSnapshot.children) {
                    val key = postSnapshot.key
                    val get = postSnapshot.getValue(FirebasePost::class.java)
                    val info = arrayOf(get!!.id, get.name, get.nuga.toString())
                    if(get?.name == "최문규"){
                        cID = get.id
                        cName = "최문규"
                        cNuga = get.nuga
                    }
                    val Result = setTextLength(info[0], 10) + setTextLength(info[1], 10) + setTextLength(
                        info[2],
                        10
                    )
                    arrayData.add(Result)
                    arrayIndex.add(key!!)
                    Log.d("getFirebaseDatabase", "key: " + key!!)
                    Log.d("getFirebaseDatabase", "info: " + info[0] + info[1] + info[2])
                }
                arrayAdapter?.clear()
                arrayAdapter?.addAll(arrayData)
                arrayAdapter?.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("getFirebaseDatabase", "loadPost:onCancelled", databaseError.toException())
            }
        }
        val sortbyAge = FirebaseDatabase.getInstance().reference.child("id_list").orderByChild("nuga")
        sortbyAge.addListenerForSingleValueEvent(postListener)
    }

    private fun setTextLength(text: String, length: Int): String {
        var text = text
        if (text.length < length) {
            val gap = length - text.length
            for (i in 0 until gap) {
                text = "$text "
            }
        }
        return text
    }


    fun postFirebaseDatabase(add: Boolean) {
        mPostReference = FirebaseDatabase.getInstance().reference
        var childUpdates = HashMap<String, Any>()
        var postValues: Map<String, Any>? = null
        if (add) {
            val post = FirebasePost(ID, name, nuga)
            postValues = post.toMap()
        }
        Log.d("MQ!", "postDB id:$ID, name:$name, nuga:$nuga")
        childUpdates?.put("/id_list/" + ID, postValues!!)

        cNuga++
        var cpostValues: Map<String, Any>? = null
        if (add) {
            val post = FirebasePost(cID, cName, cNuga)
            cpostValues = post.toMap()
        }
        Log.d("MQ!", "postDB current id:$cID, name:$cName, nuga:$cNuga")
        childUpdates?.put("/id_list/" + cID, cpostValues!!)
        mPostReference?.updateChildren(childUpdates)
    }

}
