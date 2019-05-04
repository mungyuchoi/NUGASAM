package com.moon.nugasam

import android.animation.Animator
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
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*
import java.util.ArrayList
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.ads.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import com.kongzue.dialog.v2.SelectDialog
import com.moon.nugasam.data.UndoData
import com.moon.nugasam.data.User
import com.moon.nugasam.update.ForceUpdateChecker
import kotlinx.android.synthetic.main.content_main.*
import java.util.HashMap

class MainActivity : AppCompatActivity() {

    private var toolbar: Toolbar? = null
    private var recyclerView: RecyclerView? = null
    private var myAdapter: MyAdapter? = null
    private var dataIndex = ArrayList<String>()
    private var datas = ArrayList<User>()
    private var me: User? = null
    private var progress: LottieAnimationView? = null
    private var reorder: String? = null
    private var query: Query? = null

    private lateinit var mInterstitialAd: InterstitialAd
    private lateinit var mAdView : AdView

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

        val pref = getSharedPreferences("NUGASAM", Context.MODE_PRIVATE)
        reorder = pref.getString("reorder", "name")
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

        // ads
        MobileAds.initialize(this,
            "ca-app-pub-8549606613390169~4634260996")

        mInterstitialAd = InterstitialAd(this)
       // mInterstitialAd.adUnitId = "ca-app-pub-8549606613390169/5541870755"
        // for test
        mInterstitialAd.adUnitId = "ca-app-pub-3940256099942544/1033173712"
        mInterstitialAd.loadAd(AdRequest.Builder().build())
        mInterstitialAd.adListener = object: AdListener(){
            override fun onAdClosed() {
                super.onAdClosed()
                finish()
                Log.d("MQ!", "onAdClosed")
            }
        }
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)


        ForceUpdateChecker.with(this).onUpdateNeeded(ForceUpdateChecker.OnUpdateNeededListener {
            Log.d("MQ!", "updateNeedListener $this")
            val dialog = AlertDialog.Builder(this)
                .setTitle("강제 업데이트")
                .setMessage("업데이트는 필수입니다.")
                .setPositiveButton(
                    "Update"
                ) { _, _ -> redirectStore(it) }
//                  .setNegativeButton(
//                        "No, thanks"
//                    ) { _, _ -> finish() }.create()
            dialog.setCancelable(false)
            dialog.show()
        }).check()
    }

    private fun redirectStore(updateUrl: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }


    override fun onBackPressed() {
        if (isInActionMode) {
            clearActionMode()
            myAdapter?.notifyDataSetChanged()
        } else {
            if (mInterstitialAd.isLoaded) {
                mInterstitialAd.show()
            } else {
                Log.d(TAG, "The interstitial wasn't loaded yet.")
                super.onBackPressed()
            }
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
                var intent = Intent(
                    this@MainActivity,
                    GoogleSignInActivity::class.java
                )
                intent.putExtra("name", me?.name)
                startActivity(intent)
                return true
            }
            R.id.action_reorder -> {
                if (reorder.equals("name")) {
                    reorder = "nuga"
                } else {
                    reorder = "name"
                }
                val pref = getSharedPreferences("NUGASAM", Context.MODE_PRIVATE)
                var editor = pref.edit()
                editor.putString("reorder", reorder)
                editor.commit()
                loadFirebaseData()
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
                FirebaseDatabase.getInstance().getReference().child("users").orderByChild(reorder!!).apply {
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
            R.id.action_share -> {
                share()
                return true
            }
            R.id.action_cat -> {
                refresh?.let {
                    it.visibility = View.VISIBLE
                    it.repeatCount = 1
                    it.setAnimation(R.raw.cat)
                    it.playAnimation()
                    it.addAnimatorListener(object : Animator.AnimatorListener {
                        override fun onAnimationRepeat(animation: Animator?) {
                        }

                        override fun onAnimationCancel(animation: Animator?) {
                        }

                        override fun onAnimationStart(animation: Animator?) {
                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            it.visibility = View.INVISIBLE
                        }
                    })

                }
                return true
            }
            R.id.action_deer -> {
                refresh?.let {
                    it.visibility = View.VISIBLE
                    it.repeatCount = 1
                    it.setAnimation(R.raw.deer)
                    it.scaleType = ImageView.ScaleType.FIT_CENTER
                    it.playAnimation()
                    it.addAnimatorListener(object : Animator.AnimatorListener {
                        override fun onAnimationRepeat(animation: Animator?) {
                        }

                        override fun onAnimationCancel(animation: Animator?) {
                        }

                        override fun onAnimationStart(animation: Animator?) {
                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            it.visibility = View.INVISIBLE
                        }
                    })

                }
                return true
            }
            R.id.action_undo -> {
                startActivity(
                    Intent(
                        this@MainActivity,
                        UndoActivity::class.java
                    )
                )
                return true
            }
            R.id.item_done -> {
                Log.d(TAG, "done clicked selectionList:$selectionList")
                SelectDialog.build(
                    this@MainActivity, "정말 샀나요?", "", "샀음", DialogInterface.OnClickListener { dialog, inputText ->
                        var ref = FirebaseDatabase.getInstance().getReference()
                        var undoRef = FirebaseDatabase.getInstance().getReference().child("history").push()
                        var undoData = UndoData()
                        undoData.me = me
                        undoData.date = System.currentTimeMillis().toString()
                        var who = ArrayList<User>()
                        val childUpdates = HashMap<String, Any>()
                        for (user in selectionList) {
                            var key = getKey(user)
                            user.nuga += -1
                            me?.let {
                                it.nuga += 1
                            }
                            Log.d(TAG, "key:$key")
                            who.add(user)
                            childUpdates.put("/users/" + key, user)
                        }
                        Log.i(TAG, "done me : $me")
                        me?.let { childUpdates.put("/users/" + getKey(me!!), it) }
                        Log.i(TAG, "done childUpdates: $childUpdates")
                        ref.updateChildren(childUpdates)

                        undoData.who = who
                        undoRef.setValue(undoData)

                        clearActionMode()
                        myAdapter?.notifyDataSetChanged()
                        dialog.dismiss()

                        SelectDialog.build(
                            this@MainActivity,
                            "공유하시겠습니까?",
                            "",
                            "공유",
                            DialogInterface.OnClickListener { dialog, inputText ->
                                dialog.dismiss()
                                share()
                            },
                            "취소",
                            DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() }).apply {
                            setDialogStyle(1)
                            showDialog()
                        }
                    }, "취소", DialogInterface.OnClickListener { dialog, which ->
                        dialog.dismiss()
                        clearActionMode()
                    }).apply {
                    setDialogStyle(1)
                    showDialog()
                }
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun share() {
        var shareBody = ""
        var index = 0
        for (user in datas) {
            if (index == datas.lastIndex) {
                shareBody += (user.name + " " + user.nuga)
            } else {
                shareBody += (user.name + " " + user.nuga + "\n")
            }
            index++
        }
        shareBody += "\nby NUGA3"
        var intent = Intent(android.content.Intent.ACTION_SEND).apply {
            setType("text/plain")
            putExtra(android.content.Intent.EXTRA_TEXT, shareBody)
        }
        startActivity(Intent.createChooser(intent, System.currentTimeMillis().toString()))
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

    var postListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            updateUI(dataSnapshot)
            progress?.visibility = View.INVISIBLE
        }

        override fun onCancelled(p0: DatabaseError) {
        }
    }

    fun loadFirebaseData() {
        if (query == null) {
            query =
                FirebaseDatabase.getInstance().getReference().child("users").orderByChild(reorder!!).apply {
                    addValueEventListener(postListener)
                }
        } else {
            query?.removeEventListener(postListener)
            query = FirebaseDatabase.getInstance().getReference().child("users").orderByChild(reorder!!).apply {
                addValueEventListener(postListener)
            }
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

    companion object {
        private val TAG = "MainActivity"
    }

}
