package com.instadownloader.instasave.igsave.ins

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class Share : AppCompatActivity() {


    private fun getUrl(url:String):String{
        MyUtils.log("share $url")
        try {
            if(url.startsWith("https://")){
                return url
            }
            var array =  url.split("https://")
            return "https://"+array[1]
        }catch (e:Exception){
            e.printStackTrace()
        }
        return ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            if(intent.action.equals(Intent.ACTION_SEND) && intent.type.equals("text/plain")){
                var share = intent.getStringExtra(Intent.EXTRA_TEXT)
                var intent = Intent(this,MainActivity::class.java)
                share = getUrl(share!!)
                MyUtils.log("put $share")

                intent.putExtra("url",share)
                startActivity(intent)
                finish()
            }
        }catch (e:Exception){
            e.printStackTrace()
        }

    }

}
