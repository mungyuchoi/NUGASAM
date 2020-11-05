package com.moon.nugasam.drawer

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.navigation.NavigationView
import com.infideap.drawerbehavior.AdvanceDrawerLayout
import com.moon.nugasam.*
import com.moon.nugasam.data.Rooms

class DrawerLayoutManager(private val activity: MainActivityV2) :
    NavigationView.OnNavigationItemSelectedListener {

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
                activity.startActivity(Intent(activity, CreateRoomActivity::class.java))
                return true
            }
        }
        val roomInfo = activity.roomInfo
        var index = 0
        for (room in roomInfo) {
            if (item.title == room.title) {
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
                menu.addSubMenu("Rooms").run {
                    var index = 0
                    for (room in roomInfo) {
                        add(0, index++, 0, room.title).apply {
                            Glide.with(activity).asBitmap()
                                .apply(RequestOptions.circleCropTransform()).load(room.imageUrl)
                                .into(object : CustomTarget<Bitmap>() {
                                    override fun onResourceReady(
                                        resource: Bitmap,
                                        transition: Transition<in Bitmap>?
                                    ) {
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
                }
            }
        }
    }

    companion object {
        private const val TAG = "DrawerLayoutManager"
    }

}