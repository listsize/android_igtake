package com.instadownloader.instasave.igsave.ins

import android.os.Bundle
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.crashlytics.crashlytics


object MyFireBaseUtils {
    fun logEvent(eventName:String,param:String){
        try {
            Firebase.analytics.logEvent(eventName, Bundle().apply { putString("url",param) })
        }catch (e:Throwable){
            e.printStackTrace()
        }
    }

    fun log(info:String){
        try {
            Firebase.crashlytics.log(info)
        }catch (e:Throwable){
            e.printStackTrace()
        }
    }

    fun recordException(e:Throwable){
        try {
            Firebase.crashlytics.recordException(e)
        }catch (e:Throwable){
            e.printStackTrace()
        }
    }

    fun logError(curUrl: String) {
        if ("/s/" in curUrl || "stories" in curUrl){
            logEvent("StoryError",curUrl)
        }else{
            logEvent("PostError",curUrl)
        }
    }
}