package com.instadownloader.instasave.igsave.ins

import android.content.Context
import android.content.SharedPreferences

object KVModel {
    val strModelName = "keyvalues_v2"
    val VIP = "vip"
    private var sharedPreferences: SharedPreferences? = null
    private fun getSharedPreferences(context: Context): SharedPreferences {
        if (sharedPreferences == null) {
            sharedPreferences = context.applicationContext.getSharedPreferences(strModelName, Context.MODE_PRIVATE)
        }
        return sharedPreferences!!
    }

    fun getBoolean(context: Context, paramString: String, paramBoolean: Boolean): Boolean {
        return getSharedPreferences(context).getBoolean(paramString, paramBoolean)
    }

    fun putBoolean(context: Context, paramString: String, paramBoolean: Boolean) {
        getSharedPreferences(context).edit().putBoolean(paramString, paramBoolean).commit()
    }

    fun getString(context: Context, paramString1: String, paramString2: String): String? {
        return getSharedPreferences(context).getString(paramString1, paramString2)
    }

    fun putString(context: Context, paramString1: String, paramString2: String) {
        getSharedPreferences(context).edit().putString(paramString1, paramString2).commit()
    }

    fun putInt(context: Context, paramString1: String, paramString2: Int) {
        getSharedPreferences(context).edit().putInt(paramString1, paramString2).commit()
    }
    fun getInt(context: Context, paramString1: String, paramString2: Int): Int {
        return getSharedPreferences(context).getInt(paramString1, paramString2)
    }
}