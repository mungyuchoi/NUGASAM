package com.moon.nugasam.menu

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kongzue.dialog.v2.SelectDialog
import com.moon.nugasam.MainActivityV2
import com.moon.nugasam.R
import com.moon.nugasam.SettingsActivity
import com.moon.nugasam.constant.PrefConstants
import com.moon.nugasam.data.FirebaseData
import com.moon.nugasam.data.SimpleUser

class HomeMenuImpl(private val activity: MainActivityV2) : IMeerkatMenu {

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        activity.menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (activity.me?.permission == 1) {
            menu?.findItem(R.id.action_manager)?.isVisible = true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_refresh -> {
                activity.progress?.visibility = View.VISIBLE
                activity.viewModel.loadUserRoomData()
                return true
            }
            android.R.id.home, R.id.item_cancel -> {
                activity.clearActionMode()
                activity.meerkatAdapter.notifyDataSetChanged()
                return true
            }
//            R.id.action_undo -> {
//                Log.d(TAG, "action_undo")
//                return true
//            }
            R.id.action_manager -> {
                Log.d(TAG, "action_manager")
                return true
            }
            R.id.action_setting -> {
                activity.startActivity(Intent(activity, SettingsActivity::class.java))
                return true
            }
            R.id.action_meerkat -> {
                if (activity.rewardedVideoAd.isLoaded) {
                    activity.rewardedVideoAd.show()
                } else {
                    Toast.makeText(activity, "광고준비가 아직 안되었습니다.", Toast.LENGTH_SHORT).show()
                }
                return true
            }
            R.id.item_done -> {
                Log.d(TAG, "done clicked selectionList:${activity.selectionList} me:${activity.me}")
                SelectDialog.build(activity, "정말 샀나요?", "", "샀음", { dialog, _ ->
                    val pref = activity.getSharedPreferences("NUGASAM", Context.MODE_PRIVATE)
                    val keyMe = pref.getString(PrefConstants.KEY_ME, "")
                    val keyRoom = pref.getString(PrefConstants.KEY_ROOM, "")
                    var simpleUsers = activity.viewModel._data.value!!.data!!
                    lateinit var simpleMe: SimpleUser
                    for (simpleUser in simpleUsers) {
                        if (simpleUser.key == keyMe) {
                            simpleMe = simpleUser
                            break
                        }
                    }


                    for (user in activity.selectionList) {
                        var key = activity.getKey(user)
                        for (simpleUser in simpleUsers) {
                            if (simpleUser.key == key) {
                                simpleUser.nuga += -1
                                simpleMe.nuga += 1
                                break
                            }
                        }
                    }
                    FirebaseDatabase.getInstance().reference.child("rooms").child(keyRoom)
                        .child("users").run {
                            setValue(simpleUsers)
                        }
                    activity.viewModel.loadUserRoomData()
                    activity.clearActionMode()
                    activity.meerkatAdapter.notifyDataSetChanged()
                    dialog.dismiss()

                    var point = activity.me?.point ?: 0
                    if (activity.selectionList.size < point) {
                        point -= activity.selectionList.size
                        FirebaseDatabase.getInstance().reference.child("tusers").child(keyMe).child("point")
                            .run {
                                setValue(point)
                            }
                        activity.shareDialog()
                    } else {
                        if (activity.shareAdView.isLoaded) {
                            activity.shareAdView.show()
                        } else {
                            activity.shareDialog()
                        }
                    }
                }, "취소", { dialog, _ ->
                    dialog.dismiss()
                    activity.clearActionMode()
                    activity.meerkatAdapter.notifyDataSetChanged()
                }).apply {
                    setDialogStyle(1)
                    showDialog()
                }
                return true
            }
        }
        return false
    }

    companion object {
        private val TAG = "HomeMenuImpl"
    }
}