package com.instadownloader.instasave.igsave.ins.ui.main.browser

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
class BrowserPostMediaBean{

    companion object{
        val STATE_UNCHECKED = -1
        val STATE_CHECKED = 1
        val STATE_DOWNLOADED = 2
    }

    @SerializedName("displayUrl")
    var displayUrl:String = ""

    @SerializedName("videoUrl")
    var videoUrl:String = ""

    @SerializedName("origin_post")
    var origin_post: String =""

    var state = STATE_CHECKED

}
