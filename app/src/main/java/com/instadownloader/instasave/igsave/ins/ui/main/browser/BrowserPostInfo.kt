package com.instadownloader.instasave.igsave.ins.ui.main.browser

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
class BrowserPostInfo(var infoRes:BrowserPostDownloadRes, var nums:Int, var isStory:Boolean){

    override fun equals(other: Any?): Boolean {

        other?.let { other->
            if (other is BrowserPostInfo){
                if (other.infoRes.mediaUrls.size != infoRes.mediaUrls.size){
                    return false
                }
                for (index in infoRes.mediaUrls.indices) {
                    if (other.infoRes.mediaUrls[index] != infoRes.mediaUrls[index]){
                        return false
                    }
                }
                return true
            }
        }

        return super.equals(other)
    }
}

@Keep
class BrowserPostDownloadRes{
    @SerializedName("headUrl")
    var headUrl:String = ""

    @SerializedName("userName")
    var userName:String=""

    @SerializedName("describe")
    var describe:String=""

    var mediaUrls = ArrayList<String>()


}