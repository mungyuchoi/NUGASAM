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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*
import java.util.ArrayList
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import com.kongzue.dialog.listener.InputDialogOkButtonClickListener
import com.kongzue.dialog.v2.InputDialog
import com.kongzue.dialog.v2.SelectDialog
import com.moon.nugasam.data.UndoData
import com.moon.nugasam.data.User
import com.moon.nugasam.update.ForceUpdateChecker
import kotlinx.android.synthetic.main.activity_google.*
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

    // ads
    private lateinit var mAdView: AdView
    private lateinit var mRewardedVideoAd: RewardedVideoAd
    private lateinit var mShareAdView: InterstitialAd


    // auth
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    // action mode
    var isInActionMode = false
    var selectionList = ArrayList<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FirebaseApp.initializeApp(this)
        toolbar = findViewById<Toolbar>(R.id.toolbar)?.apply {
            updateToolbar()
        }
        setSupportActionBar(toolbar)

        val pref = getSharedPreferences("NUGASAM", Context.MODE_PRIVATE)
        reorder = pref.getString("reorder", "name")
        // recyclerview
        recyclerView = findViewById<RecyclerView>(R.id.recyclerView)?.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@MainActivity)

            Log.d(TAG, "onCreate datas$datas")

            myAdapter = MyAdapter(this@MainActivity, datas.clone() as ArrayList<User>)
            adapter = myAdapter
            Log.d(TAG, "onCreate adapter:$adapter")
            loadFirebaseData()
            setRecycledViewPool(RecyclerView.RecycledViewPool())
        }
        progress = findViewById(R.id.refresh)


        checkFirebaseAuth()
        initAds()
        ForceUpdateChecker.with(this).onUpdateNeeded(ForceUpdateChecker.OnUpdateNeededListener {
            Log.d(TAG, "updateNeedListener $this")
            val dialog = AlertDialog.Builder(this)
                .setTitle("강제 업데이트")
                .setMessage("업데이트는 필수입니다.")
                .setPositiveButton(
                    "Update"
                ) { _, _ -> redirectStore(it) }
            dialog.setCancelable(false)
            dialog.show()
        }).check()
    }

    private fun checkFirebaseAuth() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser
        Log.d(TAG, "currentUser: $currentUser, image: ${currentUser?.photoUrl}, name: ${currentUser?.displayName} ")
        if (currentUser == null) {
            val signInIntent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        } else {
            val pref = getSharedPreferences("NUGASAM", Context.MODE_PRIVATE)
            var name = pref.getString("name", "")
            if (!mAuth.currentUser?.displayName.equals(name)) {
                val editor = pref.edit()
                editor.putString("name", mAuth.currentUser?.displayName)
                editor.commit()
            }
        }
    }

    private fun initAds() {
        MobileAds.initialize(this, "ca-app-pub-8549606613390169~4634260996")

        mAdView = findViewById(R.id.adView)
//        val adRequest = AdRequest.Builder().addTestDevice("ABEBCC8921F3ABA283C084A2954D0CAE").build()
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
        mAdView.adListener = (object : AdListener() {

            override fun onAdLoaded() {
                super.onAdLoaded()
                Log.d(TAG, "onAdLoaded 배너 성공")
            }

            override fun onAdFailedToLoad(errorCode: Int) {
                super.onAdFailedToLoad(errorCode)
                Log.d(TAG, "onAdFailedToLoad 배너 errorCode:$errorCode")
            }

            override fun onAdClosed() {
                super.onAdClosed()
                Log.d(TAG, "onAdClosed 배너 닫힘 ")
//                me?.let{
//                    var ref = FirebaseDatabase.getInstance().reference
//                    var key = getKey(it)
//                    var point = if(it.point == null) 0 else it.point
//                    point -= 1
//                    Log.d(TAG, "onAdOpened point:$point")
//                    ref.child("users").child(key).child("point").setValue(point)
//                    updateToolbar()
//                }
            }
        })

        // Use an activity context to get the rewarded video instance.
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this)
        mRewardedVideoAd.rewardedVideoAdListener = object : RewardedVideoAdListener {
            override fun onRewardedVideoAdClosed() {
                Log.d(TAG, "onRewardedVideoAdClosed")
                loadRewardedVideoAd()
            }

            override fun onRewardedVideoAdLeftApplication() {
                Log.d(TAG, "onRewardedVideoAdLeftApplication")
            }

            override fun onRewardedVideoAdLoaded() {
                Log.d(TAG, "onRewardedVideoAdLoaded")
            }

            override fun onRewardedVideoAdOpened() {
                Log.d(TAG, "onRewardedVideoAdOpened")
            }

            override fun onRewardedVideoCompleted() {
                Log.d(TAG, "onRewardedVideoCompleted")
            }

            override fun onRewarded(p0: RewardItem?) {
                Log.d(TAG, "onRewarded item:$p0")
                me?.let{
                    var ref = FirebaseDatabase.getInstance().reference
                    var key = getKey(it)
                    var point = if(it.point == null) 0 else it.point
                    point += 10
                    Log.d(TAG, "onRewarded point:$point")
                    ref.child("users").child(key).child("point").setValue(point)
                    updateToolbar()
                }
            }

            override fun onRewardedVideoStarted() {
                Log.d(TAG, "onRewardedVideoStarted")
            }

            override fun onRewardedVideoAdFailedToLoad(p0: Int) {
                Log.d(TAG, "onRewardedVideoAdFailedToLoad error:$p0")
            }
        }
        loadRewardedVideoAd()

        mShareAdView = InterstitialAd(this).apply{
            adUnitId = "ca-app-pub-8549606613390169/5837153647"
            loadAd(AdRequest.Builder().build())
            adListener = object : AdListener() {
                override fun onAdClosed() {
                    super.onAdClosed()
                    shareDialog()
                }
            }
        }
    }

    private fun loadRewardedVideoAd(){
        mRewardedVideoAd.loadAd(
//             "ca-app-pub-3940256099942544/5224354917",
            "ca-app-pub-8549606613390169/8372384890",
//            AdRequest.Builder().addTestDevice("ABEBCC8921F3ABA283C084A2954D0CAE").build())
                AdRequest.Builder().build())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        Log.d(TAG, "onActivityResult requestCode : $requestCode")
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
                // [START_EXCLUDE]
                finish()
                // [END_EXCLUDE]
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val pref = applicationContext.getSharedPreferences("NUGASAM", Context.MODE_PRIVATE)
                    Log.d(TAG, "name: " + pref.getString("name", "unknown"))
                    if (pref.getString("name", "unknown").equals("unknown")) {
                        InputDialog.build(
                            this@MainActivity,
                            "이름을 입력해주세요.", "채팅방에서 사용할 이름을 입력해주세요", "완료",
                            InputDialogOkButtonClickListener { dialog, inputText ->
                                dialog.dismiss()
                                val pref = applicationContext.getSharedPreferences("NUGASAM", Context.MODE_PRIVATE)
                                val editor = pref.edit()
                                editor.putString("name", mAuth.currentUser?.displayName)
                                editor.putString("image", mAuth.currentUser?.photoUrl.toString())
                                editor.commit()

                                Log.d(TAG, "name: $inputText")

                                // TODO 이때 DB에 inputText이름으로 0값으로 새롭게 추가한다!
                                var usersRef = FirebaseDatabase.getInstance().getReference().child("users").push()
                                usersRef.setValue(
                                    User(
                                        inputText,
                                        0,
                                        mAuth.currentUser?.photoUrl.toString(),
                                        mAuth.currentUser?.displayName!!
                                    )
                                )
                            }, "취소", DialogInterface.OnClickListener { dialog, which ->
                                dialog.dismiss()
                                finish()
                            }).apply {
                            setDialogStyle(1)
                            setDefaultInputHint(mAuth.currentUser?.displayName)
                            showDialog()
                        }
                    }

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Snackbar.make(main_layout, "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
                    finish()
                }
            }
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
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        Log.d(TAG, "onPrepareOptionsMenu me: ${me?.permission}")
        if (me?.permission == 1) {
            menu?.findItem(R.id.action_manager)?.isVisible = true
        }
        return super.onPrepareOptionsMenu(menu)
    }

    fun prepareToolbar(position: Int) {
        // prepare action mode
        toolbar?.getMenu()?.clear()
        toolbar?.inflateMenu(R.menu.main_action_mode)
        isInActionMode = true
        myAdapter?.notifyDataSetChanged()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
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
            toolbar?.run {
                title = counter.toString() + "명을 선택하셨습니다."
            }
        }
    }

    private fun getKey(user: User): String {
        var index = 0
        for (u in datas) {
            if (u.name == user.name) {
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
            R.id.action_refresh -> {
                progress?.visibility = View.VISIBLE
                FirebaseDatabase.getInstance().getReference().child("users").orderByChild(reorder!!).apply {
                    addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {
                        }

                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            updateUI(dataSnapshot)
                            progress?.visibility = View.INVISIBLE
                            invalidateOptionsMenu()
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
                Log.d(TAG, "done clicked selectionList:$selectionList me:$me")
                SelectDialog.build(
                    this@MainActivity, "정말 샀나요?", "", "샀음", { dialog, _ ->
                        var ref = FirebaseDatabase.getInstance().reference
                        var undoRef = FirebaseDatabase.getInstance().reference.child("history").push()
                        var undoData = UndoData()
                        undoData.me = me
                        undoData.date = System.currentTimeMillis().toString()
                        var who = ArrayList<User>()
                        for (user in selectionList) {
                            var key = getKey(user)
                            user.nuga += -1
                            me?.let {
                                it.nuga += 1
                            }
                            Log.d(TAG, "key:$key")
                            who.add(user)
                            ref.child("users").child(key).child("nuga").setValue(user.nuga)
                        }

                        undoData.who = who
                        undoRef.setValue(undoData)

                        clearActionMode()
                        myAdapter?.notifyDataSetChanged()
                        dialog.dismiss()

                        me?.let{
                            ref.child("users").child(getKey(it)).child("nuga").setValue(it.nuga)
                            if (who.size < it.point) {
                                var point = if (it.point == null) 0 else it.point
                                it.point -= who.size
                                ref.child("users").child(getKey(it)).child("point").setValue(it.point)
                                updateToolbar()
                                shareDialog()
                            } else {
                                if (mShareAdView.isLoaded) {
                                    mShareAdView.show()
                                } else {
                                    shareDialog()
                                }
                            }
                        }

                    }, "취소", { dialog, _ ->
                        dialog.dismiss()
                        clearActionMode()
                    }).apply {
                    setDialogStyle(1)
                    showDialog()
                }
                return true
            }
            R.id.action_manager -> {
                startActivity(Intent(this, SecondActivity::class.java))
                return true
            }
            R.id.action_setting -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                return true
            }
            R.id.action_smile -> {
                if (mRewardedVideoAd.isLoaded) {
                    mRewardedVideoAd.show()
                } else {
                    Toast.makeText(this, "광고준비가 아직 안되었습니다.", Toast.LENGTH_SHORT).show()
                }
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun shareDialog() {
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
        toolbar?.run {
            menu.clear()
            inflateMenu(R.menu.main)
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            updateToolbar()
            selectionList.clear()
        }
    }

    private fun updateToolbar(){
        toolbar?.run {
            var sb = StringBuilder()
            val point = if(me == null) 0 else if(me!!.point == null) 0 else me!!.point
            sb.append(resources.getString(R.string.point) + ": " + point)
            title = sb.toString()
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
            Log.d(TAG, "updateUI name:$name, user.name: ${user.name} , user.fullname:${user.fullName}")
            if (name == user.name || name == user.fullName) {
                me = user
                var editor = pref.edit()
                editor.putString("key", key)
                editor.commit()
            }
        }
        updateToolbar()
        Log.d(TAG, "onDataChange adapter:$myAdapter, datas:$datas, dataIndex:$dataIndex")
        Log.d(TAG, "myAdapter datas:$datas")
        myAdapter?.addAllData(datas)
    }

    companion object {
        private val TAG = "MainActivity"
        private val RC_SIGN_IN = 9001
    }

}
