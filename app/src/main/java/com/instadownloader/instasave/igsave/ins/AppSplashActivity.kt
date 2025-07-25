package com.instadownloader.instasave.igsave.ins

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.aos.module.base.ui.SplashActivity
import com.google.android.ads.AdUnitID
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class AppSplashActivity: SplashActivity() {
    private var interstitialAd: InterstitialAd? = null

    private var loadAdTimeout = false

    companion object{
        //如果显示了开屏插屏广告。则进入app先不现实admob开屏
        var isDisableSplashAdNextTime = false
    }

    private var skipSplash = false

    override fun onProgress(progress: Int) {

        if (isShowedInterAd){
            return
        }

        if (skipSplash){
            MyUtils.log("加载广告错误")
            isShowedInterAd  = true
            startMainJourney()
            return
        }



        if (progress >= 20){
            if (MyUtils.vip){
                startMainJourney()
            }else{
                showInterAds(this)
            }
        }

        if (progress == 100) {
            MyUtils.log("加载超时")
            loadAdTimeout = true
            startMainJourney()
        }
    }

    private fun startMainJourney(){
        val intent = Intent( this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        if (MyUtils.vip){

            return
        }
        initInterAds(this)
    }

    fun initInterAds(activity: Activity){
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(activity,AdUnitID.AD_SPLASH_INTER_ID,
            adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                adError?.message?.let { Log.d("ads", it) }
                MyUtils.log("load erro adError?.message r " + adError?.message)
                skipSplash = true
                interstitialAd = null
            }


            override fun onAdLoaded(ad: InterstitialAd) {
                interstitialAd = ad
                interstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
//                        activity.onAdDismissedFullScreenContent()
                            startMainJourney()
                    }

                    override fun onAdShowedFullScreenContent() {
                        interstitialAd = null
                    }
                }
            }
        })
    }

    private var isShowedInterAd =false

    fun showInterAds(activity: Activity){

        if (loadAdTimeout ){
            MyUtils.log("已经超时不需要显示广告了")
            return
        }

        interstitialAd?.let {it->
            if (!isShowedInterAd){
                isDisableSplashAdNextTime = true
                isShowedInterAd = true
                it.show(activity)
            }
        }

    }
}