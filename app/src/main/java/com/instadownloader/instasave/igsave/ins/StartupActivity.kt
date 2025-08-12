package com.instadownloader.instasave.igsave.ins

import android.content.Intent
import com.aos.main.AppSplashActivity

class StartupActivity: AppSplashActivity() {
    override fun startMainJourney() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun isVip(): Boolean = MyUtils.vip
}