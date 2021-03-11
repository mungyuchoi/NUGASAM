package com.moon.nugasam.ad

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAdListener
import com.google.firebase.database.FirebaseDatabase
import com.kongzue.dialog.v2.SelectDialog
import com.moon.nugasam.MainActivityV2
import com.moon.nugasam.R
import com.moon.nugasam.constant.PrefConstants


class AdvertiseManager(private val activity: MainActivityV2) {
    fun initialize() {
        MobileAds.initialize(activity, "ca-app-pub-8549606613390169~4634260996")


        val pref = activity.getSharedPreferences("NUGASAM", Context.MODE_PRIVATE)
        val adOrder = pref.getBoolean(PrefConstants.AD_ORDER, false)
        if (adOrder) {
            initBottom()
        } else {
            initCoupang()
        }
//        initRewardsVideo()
        initShareAd()
    }

    private fun initBottom() {
        activity.run {
            adView = findViewById(R.id.adView)
            adView.visibility = View.VISIBLE
//            val adRequest =
//                AdRequest.Builder().addTestDevice("ABEBCC8921F3ABA283C084A2954D0CAE").build()
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
            adView.adListener = (object : AdListener() {

                override fun onAdLoaded() {
                    super.onAdLoaded()
                    Log.d(TAG, "onAdLoaded 배너 성공")
                }

                override fun onAdFailedToLoad(errorCode: Int) {
                    super.onAdFailedToLoad(errorCode)
                    Log.d(TAG, "onAdFailedToLoad 배너 errorCode:$errorCode")
                }

                override fun onAdClosed() {
                    super.onAdClosed()
                    Log.d(TAG, "onAdClosed 배너 닫힘 ")
                    val pref = application.getSharedPreferences("NUGASAM", Context.MODE_PRIVATE)
                    val key = pref.getString("key", "")
                    Log.d(TAG, "onRewarded me:${activity.me}, key:$key")
                    activity.me?.let {
                        var point = if (it.point == null) 0 else it.point
                        point += 2
                        FirebaseDatabase.getInstance().reference.child("users").child(key)
                            .child("point").setValue(point)
                    }
                }
            })
        }
        val pref = activity.getSharedPreferences("NUGASAM", Context.MODE_PRIVATE)
        pref.edit().putBoolean(PrefConstants.AD_ORDER, false).commit()
    }

    private fun initCoupang() {
        val webView: WebView = activity.findViewById(R.id.webView)
        webView.visibility = View.VISIBLE
        webView.settings.javaScriptEnabled = true

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView,
                request: WebResourceRequest
            ): Boolean {
                val intent = Intent(Intent.ACTION_VIEW, request.url)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                activity.startActivity(intent)
                return true
            }
        }

        val html = """<div style="height: 60px">
				<script src="https://ads-partners.coupang.com/g.js"></script>
				<script>
					new PartnersCoupang.G({
						"id": 440164
					});
				</script>		
		</div>"""
        webView.loadData(html, "text/html", "UTF8")
        val pref = activity.getSharedPreferences("NUGASAM", Context.MODE_PRIVATE)
        pref.edit().putBoolean(PrefConstants.AD_ORDER, true).commit()
    }

    private fun initRewardsVideo() {
        activity.run {
            rewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this)
            rewardedVideoAd.rewardedVideoAdListener = object : RewardedVideoAdListener {
                override fun onRewardedVideoAdClosed() {
                    Log.d(TAG, "onRewardedVideoAdClosed")
                    loadRewardedVideoAd()
                }

                override fun onRewardedVideoAdLeftApplication() {
                    Log.d(TAG, "onRewardedVideoAdLeftApplication")
                }

                override fun onRewardedVideoAdLoaded() {
                    Log.d(TAG, "onRewardedVideoAdLoaded")
                }

                override fun onRewardedVideoAdOpened() {
                    Log.d(TAG, "onRewardedVideoAdOpened")
                }

                override fun onRewardedVideoCompleted() {
                    Log.d(TAG, "onRewardedVideoCompleted")
                }

                override fun onRewarded(p0: RewardItem?) {
                    val pref = application.getSharedPreferences("NUGASAM", Context.MODE_PRIVATE)
                    val key = pref.getString("key", "")
                    Log.d(TAG, "onRewarded me:${activity.me}, key:$key")
                    activity.me?.let {
                        var point = if (it.point == null) 0 else it.point
                        point += 10
                        FirebaseDatabase.getInstance().reference.child("users").child(key)
                            .child("point").setValue(point)
                    }
                }

                override fun onRewardedVideoStarted() {
                    Log.d(TAG, "onRewardedVideoStarted")
                }

                override fun onRewardedVideoAdFailedToLoad(p0: Int) {
                    Log.d(TAG, "onRewardedVideoAdFailedToLoad error:$p0")
                }
            }
            loadRewardedVideoAd()
        }

    }

    private fun initShareAd() {
        activity.run {
            shareAdView = InterstitialAd(this).apply {
                adUnitId = "ca-app-pub-8549606613390169/5837153647"
                loadAd(AdRequest.Builder().build())
                adListener = object : AdListener() {
                    override fun onAdClosed() {
                        super.onAdClosed()
                        shareDialog()
                    }
                }
            }
        }
    }

    private fun loadRewardedVideoAd() {
        activity.rewardedVideoAd.loadAd(
//             "ca-app-pub-3940256099942544/5224354917",
            "ca-app-pub-8549606613390169/3251070406",
//            AdRequest.Builder().addTestDevice("ABEBCC8921F3ABA283C084A2954D0CAE").build())
            AdRequest.Builder().build()
        )
    }

    private fun shareDialog() {
        SelectDialog.build(
            activity,
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
        for (user in activity.userInfo) {
            if (index == activity.userInfo.lastIndex) {
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
        activity.startActivity(Intent.createChooser(intent, System.currentTimeMillis().toString()))
    }

    companion object {
        private const val TAG = "AdvertiseManager"
    }
}