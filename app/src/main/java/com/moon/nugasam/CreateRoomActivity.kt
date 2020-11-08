package com.moon.nugasam

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.moon.nugasam.data.Rooms
import com.moon.nugasam.data.SimpleRoom
import com.moon.nugasam.data.SimpleUser

class CreateRoomActivity : AppCompatActivity() {

    private lateinit var editText: EditText
    private lateinit var uri: Uri
    private lateinit var thumbnail: ImageView
    private lateinit var storageRef: StorageReference
    private lateinit var databaseRef: DatabaseReference
    private var downloadUrl: Uri? = null
    private var roomInfo: ArrayList<SimpleRoom>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_room)

        storageRef = FirebaseStorage.getInstance().getReference("Rooms")
        databaseRef = FirebaseDatabase.getInstance().reference.child("rooms").push()

        roomInfo = intent.getParcelableArrayListExtra<SimpleRoom>("simpleRoom")
        Log.i("MQ!", "roomInfo:$roomInfo")
        thumbnail = findViewById<ImageView>(R.id.dotted_circle).apply {
            setOnClickListener {
                chooserFile()
            }
        }
        findViewById<ImageView>(R.id.camera).run {
            setOnClickListener {
                chooserFile()
            }
        }
        editText = findViewById(R.id.edit_text)
        findViewById<TextView>(R.id.create_room_button).run {
            setOnClickListener {
                val pref = getSharedPreferences("NUGASAM", Context.MODE_PRIVATE)
                databaseRef.setValue(
                    Rooms(imageUrl = downloadUrl?.toString(),
                        title = editText.text.toString(),
                        description = "Introduce",
                        users = ArrayList<SimpleUser>().apply {
                            add(SimpleUser(pref.getString("key", ""), nuga = 0, permission = 1))
                        }
                    ))
                FirebaseDatabase.getInstance().reference.child("tusers").child(
                    pref.getString("key", "")
                ).child("rooms").setValue(
                    roomInfo?.apply {
                        add(SimpleRoom(databaseRef.key!!))
                    })

                Toast.makeText(context, "방을 만들었습니다.", Toast.LENGTH_SHORT).show()
                finish()

                LocalBroadcastManager.getInstance(context).sendBroadcast(Intent("updateRoom"))
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.data != null) {
            uri = data.data
            Glide.with(this@CreateRoomActivity).load(uri)
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
        val key = databaseRef.key
        pref.edit().run {
            putString("key_room", key)
            commit()
        }
        if (uri == null) {
            return
        }
        val ref = storageRef.child(key + "," + getExtension(uri))
        var uploadTask = ref.putFile(uri)
        ref.putFile(uri).addOnSuccessListener {
            Log.i(TAG, "addOnSuccessListener")
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                ref.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    downloadUrl = task.result
                } else {
                    // todo handle
                }
            }
        }.addOnFailureListener {
            Log.i(ProfileActivity.TAG, "addOnFailureListener")
        }
    }

    private fun getExtension(uri: Uri): String =
        MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri))

    companion object {
        const val TAG = "CreateRoomActivity"
    }
}