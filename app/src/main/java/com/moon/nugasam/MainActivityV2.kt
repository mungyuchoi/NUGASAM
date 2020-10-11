package com.moon.nugasam

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.moon.nugasam.MainActivityV2.Companion.RC_SIGN_IN

class MainActivityV2 : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var adapter: MyAdapter

    private lateinit var viewModel: MeerkatViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
    }

    private fun initView() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        viewModel = ViewModelProviders.of(this).get(MeerkatViewModel::class.java)



    }


    companion object {
        private val TAG = "MainActivity"
        val RC_SIGN_IN = 9001
    }
}

class MeerkatViewModel(application: Application, activity: AppCompatActivity) : ViewModel() {

    private lateinit var auth: FirebaseAuth
    private lateinit var reorder: String
    init {
        val pref = application.getSharedPreferences("NUGASAM", Context.MODE_PRIVATE)
        reorder = pref.getString("reorder", "name")

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
                val pref = activity.getSharedPreferences("NUGASAM", Context.MODE_PRIVATE)
                pref.edit().apply {
                    putString("name", auth.currentUser?.displayName)
                    commit()
                }
            }
        }

    }
}