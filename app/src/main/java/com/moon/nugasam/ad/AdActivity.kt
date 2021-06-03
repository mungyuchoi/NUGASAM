package com.moon.nugasam.ad

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.firebase.database.FirebaseDatabase
import com.kakao.adfit.ads.ba.BannerAdView
import com.moon.nugasam.R

class AdActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ad)

        val kakaoAdView = findViewById<BannerAdView>(R.id.full_ad_view).apply {
            setClientId("DAN-xV4ARFAWpkhWpgfe")
            setAdListener(object: com.kakao.adfit.ads.AdListener  {
                override fun onAdLoaded() {
                    Log.d(TAG, "kakao onAdLoaded")
                }

                override fun onAdFailed(p0: Int) {
                    Log.d(TAG, "kakao onAdFailed error:$p0")
                }

                override fun onAdClicked() {
                    Log.d(TAG, "kakao onAdClicked")
                    finish()
                }

            })
            loadAd()
        }

        lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            fun onResume() {
                kakaoAdView.resume()
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            fun onPause() {
                kakaoAdView.pause()
            }

            @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            fun onDestroy() {
                kakaoAdView.destroy()
            }
        })
    }

    companion object {
        const val TAG ="AdActivity"
    }
}