package com.moon.nugasam

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.infideap.drawerbehavior.AdvanceDrawerLayout
import com.kongzue.dialog.v2.SelectDialog
import com.moon.nugasam.MainActivityV2.Companion.RC_SIGN_IN
import com.moon.nugasam.MainActivityV2.Companion.TAG
import com.moon.nugasam.ad.AdvertiseManager
import com.moon.nugasam.constant.PrefConstants
import com.moon.nugasam.data.Rooms
import com.moon.nugasam.data.SimpleRoom
import com.moon.nugasam.data.SimpleUser
import com.moon.nugasam.data.User
import com.moon.nugasam.drawer.DrawerLayoutManager
import com.moon.nugasam.extension.filter
import com.moon.nugasam.extension.map
import com.moon.nugasam.menu.HomeMenuImpl
import com.moon.nugasam.repository.SingleDataResponse
import com.moon.nugasam.repository.SingleDataStatus.*

class MainActivityV2 : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val homeMenu = HomeMenuImpl(this)

    lateinit var meerkatAdapter: MeerkatAdapter
    lateinit var viewModel: MeerkatViewModel

    lateinit var toolbar: Toolbar
    var progress: LottieAnimationView? = null
    val userKeys = ArrayList<String>()
    val userInfo = ArrayList<User>()
    val roomKeys = ArrayList<String>()
    val roomInfo = ArrayList<Rooms>()

    var me: User? = null
    var selectionList = ArrayList<User>()
    var isInActionMode = false

    lateinit var adView: AdView
    lateinit var rewardedVideoAd: RewardedVideoAd
    lateinit var shareAdView: InterstitialAd
    private val adManager = AdvertiseManager(this)

    lateinit var drawer: AdvanceDrawerLayout
    lateinit var navigationView: NavigationView
    var navigationThumbnail: ImageView? = null
    var navigationMeerKat: TextView? = null
    var navigationPoint: TextView? = null
    private val drawerManager = DrawerLayoutManager(this)

    private val updateRoomReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            viewModel.loadUserRoomData()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()

        adManager.initialize()
        drawerManager.initialize()
    }

    private fun initView() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        meerkatAdapter = MeerkatAdapter(this).apply {
            setHasStableIds(true)
        }

        progress = findViewById<LottieAnimationView>(R.id.refresh).apply {
            visibility = View.VISIBLE
        }

        recyclerView = findViewById<RecyclerView>(R.id.recyclerView).apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this@MainActivityV2)
            this.adapter = meerkatAdapter
            setRecycledViewPool(RecyclerView.RecycledViewPool())
        }

        viewModel = ViewModelProvider(this, MeerkatViewModel.Factory(application, this)).get(
            MeerkatViewModel::class.java
        )
        viewModel.run {
            loadUserRoomData()
            loading.observe(this@MainActivityV2, Observer { isLoading ->
                Log.i(TAG, "loading isLoading:$isLoading")
                progress?.visibility =
                    if (isLoading) View.VISIBLE else View.GONE
            })
            _roomInfo.observe(this@MainActivityV2, Observer {
                roomInfo.clear()
                roomKeys.clear()
                var size = it.size
                var index = 0
                it.let { list ->
                    for (simpleRoom in list) {
                        val ref = FirebaseDatabase.getInstance().reference.child("rooms")
                            .child(simpleRoom.key)
                        ref.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                Log.i(TAG, "onDataChange snapshot:$snapshot")
                                (snapshot.getValue(Rooms::class.java) as Rooms).run {
                                    roomInfo.add(this)
                                    roomKeys.add(snapshot.key!!)
                                    Log.i(TAG, "room:${roomInfo.size}, index:$index")
                                    index++
                                    if (size == index) {
                                        drawerManager.update()
                                        updateToolbar()
                                    }
                                }
                            }

                            override fun onCancelled(p0: DatabaseError) {
                            }
                        })
                    }
                }
            })
            _data.observe(this@MainActivityV2, Observer {
                Log.i(TAG, "data observe:$it size:${it.data?.size}")
                userInfo.clear()
                userKeys.clear()
                var size = it.data?.size
                var index = 0
                it.data?.let { list ->
                    for (simpleUser in list) {
                        val ref = FirebaseDatabase.getInstance().reference.child("tusers")
                            .child(simpleUser.key)
                        ref.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                (snapshot.getValue(User::class.java) as User).run {
                                    this.nuga = simpleUser.nuga
                                    userInfo.add(this)
                                    userKeys.add(snapshot.key!!)
                                    Log.i(TAG, "data userInfo:${userInfo.size}, index:$index")
                                    index++
                                    if (size == index) {
                                        Log.i(
                                            TAG,
                                            "submitList userInfo:$userInfo, userKeys:$userKeys"
                                        )
                                        meerkatAdapter.submitList(userInfo.distinct())
                                        meerkatAdapter.notifyDataSetChanged()
                                    }

                                    val pref = getSharedPreferences("NUGASAM", Context.MODE_PRIVATE)
                                    var userName = pref.getString(PrefConstants.KEY_NAME, "")
                                    if (name == userName) {
                                        me = this
                                        Glide.with(this@MainActivityV2).load(this.imageUrl)
                                            .apply(RequestOptions.circleCropTransform())
                                            .into(navigationThumbnail!!)
                                        navigationMeerKat?.text = me?.name
                                        navigationPoint?.text = "Point: " + me?.point
                                    }
                                }
                                invalidateOptionsMenu()
                            }

                            override fun onCancelled(error: DatabaseError) {
                            }
                        })
                    }
                }
            })
            empty.observe(this@MainActivityV2, Observer {
                Log.i(TAG, "empty observe:$it")
                // TODO 방만들기, 방검색으로 Layout짜서 만들기
            })
        }

        LocalBroadcastManager.getInstance(this)
            .registerReceiver(updateRoomReceiver, IntentFilter("updateRoom"))
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateRoomReceiver)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return homeMenu.onCreateOptionsMenu(menu!!)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        return homeMenu.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return homeMenu.onOptionsItemSelected(item!!)
    }

    override fun onBackPressed() {
        if (isInActionMode) {
            clearActionMode()
            meerkatAdapter?.notifyDataSetChanged()
        } else {
            super.onBackPressed()
        }
    }

    fun clearActionMode() {
        isInActionMode = false
        toolbar?.run {
            menu.clear()
            inflateMenu(R.menu.main)
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            selectionList.clear()
            updateToolbar()
        }
    }

    private fun updateToolbar() {
        toolbar?.run {
            title = getMyRoom()?.title
        }
    }

    private fun getMyRoom(): Rooms? {
        val pref = getSharedPreferences("NUGASAM", Context.MODE_PRIVATE)
        val keyRoom = pref.getString(PrefConstants.KEY_ROOM, "")
        if (keyRoom != null && keyRoom != "") {
            for ((index, roomKey) in roomKeys.withIndex()) {
                if (roomKey == keyRoom) {
                    return roomInfo[index]
                }
            }
        }
        return null
    }

    fun prepareToolbar(position: Int) {
        // prepare action mode
        toolbar?.menu?.clear()
        toolbar?.inflateMenu(R.menu.main_action_mode)
        isInActionMode = true
        meerkatAdapter?.notifyDataSetChanged()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        prepareSelection(position)
    }

    fun prepareSelection(position: Int) {

        if (!selectionList.contains(userInfo[position])) {
            selectionList.add(userInfo[position])
        } else {
            selectionList.remove(userInfo[position])
        }
        selectionList?.apply {
            var counter = size
            toolbar?.run {
                title = counter.toString() + "명을 선택하셨습니다."
            }
        }
    }

    fun getKey(user: User): String {
        for ((index, u) in userInfo.withIndex()) {
            if (u.name == user.name) {
                return userKeys[index]
            }
        }
        return ""
    }

    fun shareDialog() {
        SelectDialog.build(
            this@MainActivityV2,
            "공유하시겠습니까?",
            "",
            "공유",
            { dialog, _ ->
                dialog.dismiss()
                share()
            },
            "취소",
            { dialog, which -> dialog.dismiss() }).apply {
            setDialogStyle(1)
            showDialog()
        }
    }

    private fun share() {
        var shareBody = ""
        var index = 0
        for (user in userInfo) {
            if (index == userInfo.lastIndex) {
                shareBody += (user.name + " " + user.nuga)
            } else {
                shareBody += (user.name + " " + user.nuga + "\n")
            }
            index++
        }
        shareBody += "\nby NUGA3"
        var intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, shareBody)
        }
        startActivity(Intent.createChooser(intent, System.currentTimeMillis().toString()))
    }

    companion object {
        const val TAG = "MainActivity"
        val RC_SIGN_IN = 9001
    }
}

class MeerkatViewModel(private val application: Application, activity: AppCompatActivity) :
    ViewModel() {

    private var auth: FirebaseAuth? = null
    private var query: Query? = null

    var roomInfos = ArrayList<SimpleRoom>()
    var _roomInfo = MutableLiveData<List<SimpleRoom>>()
    var simpleUserInfo = ArrayList<SimpleUser>()
    var _data = MutableLiveData<SingleDataResponse<List<SimpleUser>>>()
    val loading: LiveData<Boolean> = _data.map { it.status == LOADING }
    val empty: LiveData<Boolean> = _data.filter { it.status == SUCCESS }.map {
        it.data?.isEmpty() ?: true
    }

    fun loadUserRoomData() {
        roomInfos.clear()
        val pref = application.getSharedPreferences("NUGASAM", Context.MODE_PRIVATE)
        val key = pref.getString(PrefConstants.KEY_ME, "")
        FirebaseDatabase.getInstance().reference.child("tusers").child(key).child("rooms").run {
            addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (childDataSnapshot in dataSnapshot.children) {
                        roomInfos.add(childDataSnapshot.getValue(SimpleRoom::class.java)!!)
                    }
                    Log.d(TAG, "loadUserRoomData roomInfo size${roomInfos.size}")
                    if (roomInfos.size == 0) {
                        return
                    }
                    var choiceRoom = roomInfos[0]
                    when (roomInfos.size) {
                        1 -> {
                            choiceRoom = roomInfos[0]
                        }
                        else -> {
                            val keyRoom = pref.getString(PrefConstants.KEY_ROOM, "")
                            if (keyRoom != null && keyRoom != "") {
                                for (room in roomInfos) {
                                    if (keyRoom == room.key) {
                                        choiceRoom = room
                                        break
                                    }
                                }
                            }
                        }
                    }
                    _roomInfo.postValue(roomInfos)
                    Log.i(TAG, "choiceRoom:$choiceRoom")

                    pref.edit().apply {
                        putString(PrefConstants.KEY_ROOM, choiceRoom.key)
                        commit()
                    }
                    loadRoomUserData(choiceRoom.key)
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
    }

    private var postListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            simpleUserInfo.clear()
            for (childDataSnapshot in dataSnapshot.children) {
                simpleUserInfo.add(childDataSnapshot.getValue(SimpleUser::class.java)!!.apply {
                    Log.i(TAG, "postListener add $nuga")
                })
            }
            _data.postValue(SingleDataResponse.success(simpleUserInfo))
        }

        override fun onCancelled(p0: DatabaseError) {
        }
    }

    fun loadRoomUserData(roomKey: String) {
        if (query == null) {
            query =
                FirebaseDatabase.getInstance().reference.child("rooms").child(roomKey)
                    .child("users")
                    .apply {
                        addValueEventListener(postListener)
                    }
        } else {
            query?.removeEventListener(postListener)
            query =
                FirebaseDatabase.getInstance().reference.child("rooms").child(roomKey)
                    .child("users")
                    .apply {
                        addValueEventListener(postListener)
                    }
        }
    }

    init {
        val pref = application.getSharedPreferences("NUGASAM", Context.MODE_PRIVATE)
        //reorder = pref.getString("reorder", "name")

        FirebaseApp.initializeApp(application)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(application.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val client = GoogleSignIn.getClient(application, gso)
        auth = FirebaseAuth.getInstance().apply {
            if (currentUser == null) {
                activity.startActivityForResult(client.signInIntent, RC_SIGN_IN)
            } else {
                val authName = auth?.currentUser?.displayName
                val pref = activity.getSharedPreferences("NUGASAM", Context.MODE_PRIVATE)
                var name = pref.getString(PrefConstants.KEY_NAME, "")
                Log.i(TAG, "edit : $authName name:$name")
                if (authName != null && authName != name) {
                    pref.edit().apply {
                        putString(PrefConstants.KEY_NAME, authName)
                        commit()
                    }
                }
            }
        }
    }

    class Factory(private val application: Application, private val activity: AppCompatActivity) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MeerkatViewModel(application, activity) as T
        }
    }
}