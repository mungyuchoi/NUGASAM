package com.moon.nugasam

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import com.google.android.material.snackbar.Snackbar
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.kongzue.dialog.listener.InputDialogOkButtonClickListener
import com.kongzue.dialog.v2.InputDialog
import kotlinx.android.synthetic.main.activity_google.*
import android.content.Context
import com.google.firebase.database.FirebaseDatabase
import com.moon.nugasam.data.User


class SplashActivity : AppCompatActivity() {

    var SPLASH_TIME = 3000L //This is 1 seconds
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        mAuth = FirebaseAuth.getInstance()
        val currentUser = mAuth.currentUser

        Log.d(TAG, "currentUser: $currentUser, image: ${currentUser?.photoUrl}, name: ${currentUser?.displayName} ")
        if (currentUser == null) {
            signIn()
        } else {
            val pref = getSharedPreferences("NUGASAM", Context.MODE_PRIVATE)
            var name = pref.getString("name", "")
            if (!mAuth.currentUser?.displayName.equals(name)) {
                val editor = pref.edit()
                editor.putString("name", mAuth.currentUser?.displayName)
                editor.commit()
            }
            startMainActivity()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        Log.d("MQ!", "onActivityResult requestCode : $requestCode")
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
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

                    InputDialog.build(
                        this@SplashActivity,
                        "이름을 입력해주세요.", "채팅방에서 사용할 이름을 입력해주세요", "완료",
                        InputDialogOkButtonClickListener { dialog, inputText ->
                            dialog.dismiss()
                            val pref = applicationContext.getSharedPreferences("NUGASAM", Context.MODE_PRIVATE)
                            val editor = pref.edit()
                            editor.putString("name", inputText)
                            editor.putString("image", mAuth.currentUser?.photoUrl.toString())
                            editor.commit()

                            Log.d(TAG, "name: $inputText")
                            startMainActivity()

                            // TODO 이때 DB에 inputText이름으로 0값으로 새롭게 추가한다!
                            var usersRef = FirebaseDatabase.getInstance().getReference().child("users").push()
                            usersRef.setValue(User(inputText, 0, mAuth.currentUser?.photoUrl.toString(),mAuth.currentUser?.displayName!!))
                        }, "취소", DialogInterface.OnClickListener { dialog, which ->
                            dialog.dismiss()
                            finish()
                        }).apply {
                        setDialogStyle(1)
                        setDefaultInputHint(mAuth.currentUser?.displayName)
                        showDialog()
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Snackbar.make(main_layout, "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
                    finish()
                }
            }
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun startMainActivity() {
        Handler().postDelayed({
            val mySuperIntent = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(mySuperIntent)
            finish()
        }, SPLASH_TIME)
    }

    companion object {
        private val TAG = "SplashActivity"
        private val RC_SIGN_IN = 9001
    }
}