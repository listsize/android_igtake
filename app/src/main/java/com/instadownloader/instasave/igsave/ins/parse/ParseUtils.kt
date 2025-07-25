package com.instadownloader.instasave.igsave.ins.parse

import android.text.TextUtils
import com.instadownloader.instasave.igsave.ins.ui.main.browser.BrowserPostInfo
import com.instadownloader.instasave.igsave.ins.ui.main.data.PostBean
import java.util.*

object ParseUtils {
    fun igBeanToPostBean(insItem:MyIgBean):PostBean{
        var bean = PostBean().apply {
            profile_pic = insItem.profile_pic
            name = insItem.username
            text = if (TextUtils.isEmpty(insItem.content) || insItem.content == "null") "" else insItem.content
            url = insItem.instagram_url
            time = System.currentTimeMillis() / 1000
        }

        var urls  = ""
        for (u in insItem.downloadInfo){
            urls = urls + u.downloadUrl +","
        }
        bean.mediaurls = urls
        return bean
    }

    fun browserBeanToPostBean(insItem:BrowserPostInfo):PostBean{
        var bean = PostBean().apply {
            profile_pic = insItem.infoRes.headUrl
            name = insItem.infoRes.userName
            text = insItem.infoRes.describe
            url =UUID.randomUUID().toString()
            time = System.currentTimeMillis() / 1000
        }

        var urls  = ""
        for (u in insItem.infoRes.mediaUrls){
            urls = urls + u +","
        }
        bean.mediaurls = urls
        return bean
    }
}