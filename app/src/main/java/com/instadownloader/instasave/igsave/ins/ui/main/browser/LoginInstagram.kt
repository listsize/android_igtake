package com.instadownloader.instasave.igsave.ins.ui.main.browser

import android.app.Activity
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.webkit.*
import com.instadownloader.instasave.igsave.ins.MyUtils
import com.instadownloader.instasave.igsave.ins.R
import com.instadownloader.instasave.igsave.ins.ui.main.data.LoginTool

class LoginInstagram : AppCompatActivity() {

    lateinit var  webview: WebView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_instagram)
        val mode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if(mode != Configuration.UI_MODE_NIGHT_YES) {
            this.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        setTitle(R.string.tip_login_title)
        setContentView(R.layout.activity_login_instagram)
        webview = findViewById<WebView>(R.id.webview)

        webview.settings.allowFileAccessFromFileURLs = true
        webview.settings.allowFileAccess = true
        webview.settings.allowContentAccess = true;
        webview.settings.allowUniversalAccessFromFileURLs = true
        webview.getSettings().setPluginState(WebSettings.PluginState.ON);
        webview.settings.javaScriptEnabled = true
//        mWebview.settings.userAgentString = "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Mobile Safari/537.36"
        webview.settings.databaseEnabled = true
        webview.settings.domStorageEnabled = true;
        webview.settings.loadWithOverviewMode = true
        webview.settings.useWideViewPort= false

        webview.loadUrl("https://www.instagram.com/accounts/login/")


        webview.webViewClient = object : WebViewClient(){

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                MyUtils.log("onPageFinished - > $url")
                //当访问 https://www.instagram.com/accounts/onetap/?next=%2F  标志着弹出了是否保存cookie对话框

                var cookie = LoginTool.isThereCookie(url!!)
                if(!TextUtils.isEmpty(cookie)){
                    LoginTool.saveCookie(cookie)
                }

                //当访问 https://www.instagram.com/  标志着用户已经点击保存 或者不保存登录状态

                if (url == "https://www.instagram.com/"){
                    setResult(Activity.RESULT_OK)
                    finish()
                }

            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String): Boolean {
                MyUtils.log(" shouldOverrideUrlLoading - > $url")

                view?.loadUrl(url)
                return true
            }

        }
    }

    override fun onStart() {
        super.onStart()
        MyUtils.isFromTheAppOtherActivityBack = true
    }


}