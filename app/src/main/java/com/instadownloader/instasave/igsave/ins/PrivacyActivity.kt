package com.instadownloader.instasave.igsave.ins

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class PrivacyActivity : AppCompatActivity() {
    lateinit var  webview: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy)
        val mode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if(mode != Configuration.UI_MODE_NIGHT_YES) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        title = getString(R.string.privacy)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        webview = findViewById(R.id.webview)
        webview.webViewClient = object : WebViewClient(){

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String): Boolean {
                view?.loadUrl(url)
                return true
            }

        }
        webview.loadUrl("https://aosdoc-74380.web.app/privacy.html")
    }
}