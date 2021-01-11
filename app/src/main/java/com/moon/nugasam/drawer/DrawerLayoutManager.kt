package com.moon.nugasam.drawer

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.FirebaseDatabase
import com.infideap.drawerbehavior.AdvanceDrawerLayout
import com.moon.nugasam.*
import com.moon.nugasam.constant.PrefConstants
import com.moon.nugasam.data.FirebaseData

class DrawerLayoutManager(private val activity: MainActivityV2) :
    NavigationView.OnNavigationItemSelectedListener {

    var subMenu: SubMenu? = null

    fun initialize() {
        activity.run {
            drawer = (findViewById<View>(R.id.drawer_layout) as AdvanceDrawerLayout).apply {
                ActionBarDrawerToggle(
                    activity, this, toolbar,
                    R.string.navigation_drawer_open,
                    R.string.navigation_drawer_close
                ).run {
                    addDrawerListener(this)
                    syncState()
                }
                setViewScale(Gravity.START, 0.9f)
                setRadius(Gravity.START, 35f)
                setViewElevation(Gravity.START, 20f)
            }

            navigationView = (findViewById<View>(R.id.nav_view) as NavigationView).apply {
                setNavigationItemSelectedListener(this@DrawerLayoutManager)
                getHeaderView(0).run {
                    navigationThumbnail = findViewById(R.id.thumbnail)
                    navigationMeerKat = findViewById(R.id.title)
                    navigationPoint = findViewById(R.id.point)
                    findViewById<TextView>(R.id.show_profile).run {
                        setOnClickListener {
                            startActivity(Intent(context, ProfileActivity::class.java))
                        }
                    }
                }
            }
        }

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onNavigationItemSelected item:${item.title}, ${item.itemId}")
        when (item.itemId) {
            R.id.nav_share -> {
                activity.shareDialog()
                activity.drawer!!.closeDrawer(GravityCompat.START)
                return true
            }
            R.integer.nav_add -> {
                activity.me?.run {
                    if (point >= 100) {
                        var intent = Intent(activity, CreateRoomActivity::class.java)
                        intent.putParcelableArrayListExtra(
                            "simpleRoom",
                            activity.viewModel.roomInfos
                        )
                        activity.startActivity(intent)
                        val pref = activity.getSharedPreferences("NUGASAM", Context.MODE_PRIVATE)
                        val key = pref.getString(PrefConstants.KEY_ME, "")
                        FirebaseDatabase.getInstance().reference.child("users").child(key)
                            .child("point").setValue(point - 100)
                    } else {
                        Toast.makeText(activity, "포인트 100점이상만 방을 만들 수 있습니다.", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                return true
            }
        }
        val roomInfo = activity.roomInfo
        var index = 0
        for (room in roomInfo) {
            if (item.title == room.title + " (#${room.code})") {
                activity.viewModel.loadRoomUserData(activity.roomKeys[index])
                activity.drawer!!.closeDrawer(GravityCompat.START)
                break
            }
            index++
        }
        return true
    }

    fun update() {
        val roomInfo = activity.roomInfo
        Log.i(TAG, "update: $roomInfo")
        activity.run {
            navigationView.run {
                if (subMenu != null) {
                    subMenu!!.clear()
                }
                menu.addSubMenu("Rooms").run {
                    for ((index, room) in roomInfo.withIndex()) {
                        add(0, index, 0, room.title + " (#${room.code})").apply {
                            if (room.imageUrl == null) {
                                icon = resources.getDrawable(R.drawable.icon_meerkat, null)
                                return@apply
                            }
                            Glide.with(activity).asBitmap()
                                .apply(RequestOptions.circleCropTransform()).load(room.imageUrl)
                                .into(object : CustomTarget<Bitmap>() {
                                    override fun onResourceReady(
                                        resource: Bitmap,
                                        transition: Transition<in Bitmap>?
                                    ) {
                                        Log.i(TAG, "onResourceReady resource:$resource")
                                        icon = BitmapDrawable(activity.resources, resource)
                                    }

                                    override fun onLoadCleared(placeholder: Drawable?) {
                                    }
                                })
                        }
                    }
                    add(0, R.integer.nav_add, 0, "방 만들기").apply {
                        icon = getDrawable(R.drawable.ic_add)
                    }
                    subMenu = this
                }
            }
        }
    }

    companion object {
        private const val TAG = "DrawerLayoutManager"
    }

}