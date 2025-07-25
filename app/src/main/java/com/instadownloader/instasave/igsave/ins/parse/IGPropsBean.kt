package com.instadownloader.instasave.igsave.ins.parse

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName


@Keep
class IGPropsBean {

    @SerializedName("media_id")
    var media_id: String? = null

    @SerializedName("media_owner_id")
    var media_owner_id: String? = null

    @SerializedName("media_type")
    var media_type: Int? = null

    @SerializedName("qr")
    var qr: Boolean? = null
}