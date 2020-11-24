package com.moon.nugasam

import android.app.Application
import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.moon.nugasam.update.ForceUpdateChecker
import com.facebook.stetho.Stetho
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings

class NugasamApplication : Application() {

    private val TAG = "NugasamApplication"

    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)

        val remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)

        // set in-app defaults
        val remoteConfigDefaults = HashMap<String, Any>()
        remoteConfigDefaults[ForceUpdateChecker.KEY_UPDATE_REQUIRED] = false
        remoteConfigDefaults[ForceUpdateChecker.KEY_CURRENT_VERSION] = "1.0.0"
        remoteConfigDefaults[ForceUpdateChecker.KEY_UPDATE_URL] =
            "https://play.google.com/store/apps/details?id=com.moon.nugasam"

        remoteConfig.setDefaultsAsync(remoteConfigDefaults)

        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "remote config is fetched.")
                }
            }
    }
}