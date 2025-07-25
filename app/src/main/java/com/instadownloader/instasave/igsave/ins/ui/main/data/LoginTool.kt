package com.instadownloader.instasave.igsave.ins.ui.main.data

import android.text.TextUtils
import android.util.Log
import android.webkit.CookieManager
import com.instadownloader.instasave.igsave.ins.KVModel
import com.instadownloader.instasave.igsave.ins.MyApp
import com.instadownloader.instasave.igsave.ins.MyUtils

object LoginTool {
    private var cookie =""
    val REQUEST_CODE_LOGIN = 111
    var USER_AGENT = "Mozilla/5.0 (Linux; Android 10; SM-G973F Build/QP1A.190711.020; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/86.0.4240.198 Mobile Safari/537.36 Instagram 166.1.0.42.245 Android (29/10; 420dpi; 1080x2042; samsung; SM-G973F; beyond1; exynos9820; en_GB; 256099204)"

    var isHadSetCookie = false

    private fun clearLoginCookie() {
        cookie = ""
        KVModel.putString(MyApp.gContext,"cookie","")
    }

    fun logout(){
        clearLoginCookie()
        CookieManager.getInstance().removeAllCookie()
    }

    fun getLoginCookie(): String {
        if(TextUtils.isEmpty(cookie)){
            cookie = KVModel.getString(MyApp.gContext,"cookie","")!!
        }
        MyUtils.log("cookie ->$cookie")
        return cookie
    }

    fun isLogout():Boolean{
        return TextUtils.isEmpty(getLoginCookie())
    }

    fun saveCookie(string:String){
        cookie = string
        MyUtils.log("cookie $string")
        KVModel.putString(MyApp.gContext,"cookie",string)
    }

    fun isThereCookie(url:String):String{
        var cookie =  CookieManager.getInstance().getCookie(url)
        if(!TextUtils.isEmpty(cookie)){
            if(cookie.contains("sessionid") && cookie.contains("ds_user_id")){
                return cookie
            }
        }
        return ""
    }

    //当用户在登陆状态下失败后。 跳转到浏览器后。 重新保存一下cookie
    fun needSetCookieAgain(){
        isHadSetCookie = false
    }

    fun setCookieNew(url: String?) {
        if (isHadSetCookie){
            Log.e("ads","已经设置过")
            return
        }
        url?.let { url->
            var cookie = isThereCookie(url)
            if(!TextUtils.isEmpty(cookie)){
                saveCookie(cookie)
                isHadSetCookie = true
            }
        }

    }
}