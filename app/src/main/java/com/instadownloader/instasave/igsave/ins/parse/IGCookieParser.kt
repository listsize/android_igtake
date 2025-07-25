package com.instadownloader.instasave.igsave.ins.parse


import com.google.gson.Gson
import com.instadownloader.instasave.igsave.ins.*
import com.instadownloader.instasave.igsave.ins.ui.main.data.LoginTool
import com.instadownloader.instasave.igsave.ins.ui.main.data.PostBean
import com.instadownloader.instasave.igsave.ins.ui.main.data.Webservice
import org.jsoup.Jsoup
import retrofit2.Retrofit


class IGCookieParser {

    val retrofit: Webservice = Retrofit.Builder()
        .baseUrl("https://www.instagram.com")
        .build()
        .create(Webservice::class.java)

    private val POST_URL = "https://i.instagram.com/api/v1/media/%s/info/";

    fun parseCommonPhoto(
        originUrl: String,
        realUrl:String
    ): PostBean {
        val response = retrofit.getHtmlText(LoginTool.getLoginCookie(),realUrl).execute()
        var body = response.body()!!.string()

        var document = Jsoup.parse(body)
        document?.let {
            var elements = document.getElementsByTag("script")
            for (e in elements) {
                var content = e.toString()
                if (content.contains("\"props\":") || content.contains("\"props\" :")) {
                    var startIndex = content.indexOf("\"props\":")
                    var endIndex = content.lastIndexOf("}")
                    var jsonContent = content.substring(startIndex, endIndex + 1)
                    jsonContent = MyUtils.findJsonString(jsonContent, '{', '}')

                    MyUtils.i(" jsonContent $jsonContent")

                    val postinfo =  Gson().safelyFromJson(jsonContent, IGPropsBean::class.java)
                    val myurl = String.format(POST_URL,postinfo?.media_id)
                    MyUtils.i(" jsonContent $myurl")

                    val response = retrofit.getHtmlText(LoginTool.getLoginCookie(),LoginTool.USER_AGENT,myurl).execute()
                    val json  =response.body()?.string()?:"{}"
                    MyUtils.i(" jsonjson $json")

                    var insItemList = parseJson(json)

                    insItemList?.let {insItem->
                        insItem.instagram_url = originUrl
                        return ParseUtils.igBeanToPostBean(insItem)
                    }
                }
            }
        }

        throw Exception("some error")
    }


    private fun parseJson(json: String): MyIgBean? {
        val data = Gson().safelyFromJson(json, IGPostBean::class.java)
        // 解析owner信息
        return data?.items?.firstOrNull()?.let {
            var result = MyIgBean()
            result.profile_pic = it.user?.profilePicUrl.toString()
            result.username = it.user?.username.toString()
            result.content = it.caption?.text.toString()

            if (it.carouselMediaCount?: 0 > 1) {
                it.carouselMedia?.forEach { media ->
                    var image = VideoInfoBean(media.mediaType == 2)
                    image.isVideo = media.mediaType == 2
                    if (!image.isVideo) {
                        image.downloadUrl =
                            media.imageVersions2?.candidates?.firstOrNull()?.url.toString()
                    } else {
                        image.downloadUrl = media.videoVersions?.firstOrNull()?.url.toString()
                    }
                    result.downloadInfo.add(image)
                }
            } else {
                var image = VideoInfoBean(it.mediaType == 2 || it.has_audio)
                image.isVideo = it.mediaType == 2 || it.has_audio
                if (!image.isVideo) {
                    image.downloadUrl =
                        it.imageVersion2?.candidates?.firstOrNull()?.url.toString()
                } else {
                    image.downloadUrl = it.videoVersions?.firstOrNull()?.url.toString()
                }
                result.downloadInfo.add(image)
            }
            result
        }
    }


}