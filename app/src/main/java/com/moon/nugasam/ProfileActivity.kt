package com.moon.nugasam

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.kongzue.dialog.v2.InputDialog
import com.moon.nugasam.data.User

class ProfileActivity : AppCompatActivity() {

    private var query: Query? = null
    private var reorder: String? = null

    private var thumbnail: ImageView? = null
    private var title: TextView? = null
    private var mealwormPoint: TextView? = null

    private var me: User? = null

    lateinit var uri : Uri
    private lateinit var storageRef: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        thumbnail = findViewById<ImageView>(R.id.thumbnail).apply {
            setOnClickListener {
                chooserFile()
            }
        }
        title = findViewById<TextView>(R.id.name).apply {
            setOnClickListener {
                InputDialog.build(
                    this@ProfileActivity,
                    "이름을 입력해주세요.", "채팅방에서 사용할 이름을 입력해주세요",
                "완료",{dialog, inputText ->
                        // TODO 입력받은 inputText로 아래 2가지 업데이트
                        // 1. preference 2. Firebasedb
                        val pref = applicationContext.getSharedPreferences("NUGASAM", Context.MODE_PRIVATE)
                        val editor = pref.edit()
                        editor.putString("name", inputText)
                        editor.commit()

                        var key = pref.getString("key","")
                        FirebaseDatabase.getInstance().reference.child("tusers")
                            .child(key).child("name").setValue(inputText)
                        dialog.dismiss()
                    }, "취소", {dialog, _ -> { dialog.dismiss()}}
                ).apply {
                    setDialogStyle(1)
                    showDialog()
                }
            }
        }
        mealwormPoint = findViewById(R.id.mealworm_point)

        val pref = getSharedPreferences("NUGASAM", Context.MODE_PRIVATE)
        reorder = pref.getString("reorder", "name")
        loadFirebaseData()
        storageRef = FirebaseStorage.getInstance().getReference("Profile")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.data != null) {
            uri = data.data
            Glide.with(this@ProfileActivity).load(uri)
                .apply(RequestOptions.circleCropTransform())
                .into(thumbnail!!)
            uploadFile()
        }
    }

    private fun chooserFile() {
        startActivityForResult(Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }, 1)
    }

    private fun uploadFile() {
        val pref = getSharedPreferences("NUGASAM", Context.MODE_PRIVATE)
        var key = pref.getString("key", "")
        Log.i(TAG, "uploadFile key:$key")
        val ref = storageRef.child(key +","+getExtension(uri))
        var uploadTask = ref.putFile(uri)
        ref.putFile(uri).addOnSuccessListener {
            Log.i(TAG, "addOnSuccessListener")
            uploadTask.continueWithTask{
                task ->
                if(!task.isSuccessful){
                    task.exception?.let{
                        throw it
                    }
                }
                ref.downloadUrl
            }.addOnCompleteListener{
                task->
                if(task.isSuccessful){
                    val downloadUri = task.result
                    Log.i(TAG, "downloadUri:$downloadUri, key:$key")
                    // me의 url에 업데이트 하기
                    FirebaseDatabase.getInstance().reference.child("tusers")
                        .child(key).child("imageUrl").setValue(downloadUri.toString())
                } else {
                    // todo handle
                }
            }
        }.addOnFailureListener {
            Log.i(TAG, "addOnFailureListener")
        }

    }

    private fun getExtension(uri: Uri): String =
        MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri))

    private fun loadFirebaseData() {
        if (query == null) {
            query =
                FirebaseDatabase.getInstance().reference.child("tusers").orderByChild(reorder!!)
                    .apply {
                        addValueEventListener(postListener)
                    }
        } else {
            query?.removeEventListener(postListener)
            query = FirebaseDatabase.getInstance().reference.child("tusers").orderByChild(reorder!!)
                .apply {
                    addValueEventListener(postListener)
                }
        }
    }

    fun updateUI(dataSnapshot: DataSnapshot) {
        val pref = getSharedPreferences("NUGASAM", Context.MODE_PRIVATE)
        var name = pref.getString("name", "")
        for (postSnapshot in dataSnapshot.children) {
            val key = postSnapshot.key
            val user = postSnapshot.getValue(User::class.java)
            if (name == user!!.name || name == user.fullName) {
                me = user
                var editor = pref.edit()
                editor.putString("key", key)
                editor.commit()
                break
            }
        }

        me.run {
            Glide.with(this@ProfileActivity).load(me!!.imageUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(thumbnail!!)
            Log.i(TAG, "name:$name, title:$title")
            title?.text = name
            mealwormPoint?.text = this?.point!!.toString()
        }
    }

    private var postListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            updateUI(dataSnapshot)
        }

        override fun onCancelled(p0: DatabaseError) {
        }
    }

    companion object {
        const val TAG = "ProfileActivity"
    }

}