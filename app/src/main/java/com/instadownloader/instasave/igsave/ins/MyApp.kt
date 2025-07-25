package com.instadownloader.instasave.igsave.ins

import android.content.Context
import android.os.Build
import android.webkit.WebView
import com.google.android.ads.AdSplashApplication
import com.google.android.ads.AdUnitID
import com.liulishuo.filedownloader.FileDownloader

class MyApp : AdSplashApplication(){
    lateinit var appContainer:MyAppContainer
    companion object{
        lateinit var gContext: Context
    }

    private fun configWebViewCacheDirWithAndroidP() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                var processName = getProcessName()
                if (packageName != processName) {
                    WebView.setDataDirectorySuffix(processName)
                }
            }
        }catch (e:Exception){
            e.printStackTrace()
        }

    }
    override fun onCreate() {
        configWebViewCacheDirWithAndroidP()

        super.onCreate()
        gContext = applicationContext

        appContainer = MyAppContainer()


        Thread{
            try {
                MyUtils.vip = KVModel.getBoolean(applicationContext, KVModel.VIP, false)
            }catch (e:Exception){
                e.printStackTrace()
            }
        }.start()


        FileDownloader.setup(applicationContext)

    }

    override fun initAdUnitConfig() {
        AdUnitID.AD_SPLASH_ID = "ca-app-pub-8166307674328646/1832889051"
        AdUnitID.AD_SPLASH_INTER_ID = "ca-app-pub-8166307674328646/4078663963"
    }

}