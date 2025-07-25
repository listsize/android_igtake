package com.instadownloader.instasave.igsave.ins.parse


import android.text.TextUtils
import com.instadownloader.instasave.igsave.ins.MyUtils
import com.instadownloader.instasave.igsave.ins.ui.main.data.LoginTool
import com.instadownloader.instasave.igsave.ins.ui.main.data.PostBean
import com.instadownloader.instasave.igsave.ins.ui.main.data.Webservice


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import org.json.JSONObject
import org.jsoup.Jsoup
import retrofit2.Response
import retrofit2.Retrofit
import java.util.regex.Pattern


class IgStoryParser {

    val retrofit: Webservice = Retrofit.Builder()
        .baseUrl("https://www.instagram.com")
        .build()
        .create(Webservice::class.java)

    fun parseStory(url: String): PostBean {
        val response = retrofit.getHtmlText(LoginTool.getLoginCookie(), url).execute()
        MyUtils.log("parseStory"+response.code().toString())
        if (response.isSuccessful) {
            val htmlBody = response.body()?.string()!!

            var bean:PostBean = try {
                parseStoryOld(htmlBody,url)
            }catch (e:Exception){
                parseStoryNew(htmlBody,url)
            }
            return bean
        }
        throw Exception("dadwa")

    }

 //下面是重定向测试代码
//    fun parseStory(url: String): PostBean {
//        var url = "https://www.instagram.com/"
//        val client =  OkHttpClient.Builder()
////            .addInterceptor( RedirectInterceptor())
//            .followRedirects(false)
//            .build()
//        var request = Request.Builder()
//            .url(url)
//            .addHeader("Cookie",LoginTool.getLoginCookie())
//            .build()
//        do {
//            val response = client.newCall(request).execute()
//
//            if (response.isRedirect) {
//                val location = response.header("Location")
//                MyUtils.log("response " + response.toString())
//                if (location != null && shouldFollowRedirect(location)) { // 满足自定义的重定向条件
//                    request = request.newBuilder().url(location).addHeader("Cookie",LoginTool.getLoginCookie()).build()
//                } else {
//                    break
//                }
//            } else {
//                break
//            }
//        } while (true)
//        throw Exception("dadwa")
//
//    }
//    // 该函数根据重定向URL判断是否应该跟随重定向
//    fun shouldFollowRedirect(redirectUrl: String?): Boolean {
//        // 在此处添加您的条件
//        MyUtils.log("shouldFollowRedirect " +redirectUrl)
//        return true  // 例如：默认跟随重定向
//    }

    private fun parseStoryOld(htmlBody: String, url: String): PostBean {
        var document = Jsoup.parse(htmlBody)
        document?.let {
            var elements = document.getElementsByTag("script")
            for (e in elements) {
                var content = e.toString()
                if (content.contains("\"props\":") || content.contains("\"props\" :")) {
                    var startIndex = content.indexOf("\"props\":")
                    var endIndex = content.lastIndexOf("}")
                    var jsonContent = content.substring(startIndex, endIndex + 1)
                    jsonContent = MyUtils.findJsonString(jsonContent, '{', '}')
                    val jSONObject = JSONObject(jsonContent)
                    var userId = jSONObject.getJSONObject("user").optString("id")
                    var hlId = ""
                    var hl = jSONObject.optJSONObject("highlight")
                    hl?.let {
                        hlId = it.optString("id")
                    }
                    MyUtils.log("userId " + userId + "  hid " + hlId)
                    return parseStory(url, userId, hlId)
                }
            }
        }
        throw Exception("dadwa")
    }

   private fun parseStoryNew(htmlBody: String, url: String): PostBean {
        var hlId = ""
        var userId = ""
        if (htmlBody.contains("highlight_reel_id")) {
            // 定义正则表达式模式
            val pattern = Pattern.compile("\"highlight_reel_id\":\"([^\"]+)\"")
            val matcher = pattern.matcher(htmlBody)
            // 查找匹配项并提取值
            if (matcher.find()) {
                hlId = matcher.group(1)
                return parseStory(url, userId, hlId)
            }
        } else {
            val pattern = "\"user_id\":\"(\\d+)\"".toRegex()
            val matchResult = pattern.find(htmlBody)
            if (matchResult != null) {
                val userId = matchResult.groupValues[1]
                return parseStory(url, userId, hlId)
            }
        }
        throw Exception("dadwa")
    }

    private fun parseStory(curUrl: String, userID: String, hightlightId: String): PostBean {
        var ua = LoginTool.USER_AGENT
        val mediaid: String = getMediaID(curUrl)
        var url = ""
        url = if (!TextUtils.isEmpty(hightlightId)) {
            "https://i.instagram.com/api/v1/feed/reels_media/?reel_ids=highlight%3A$hightlightId"
        } else {
            "https://i.instagram.com/api/v1/feed/reels_media/?reel_ids=$userID"
        }
        val response = retrofit.getStoryText(LoginTool.getLoginCookie(), ua, url).execute()
        if (response.code() == 200) {
            val text = response.body()!!.string()
            val jsonObject = JSONObject(text)
            var profileurl = ""
            var username = ""
            val dataobj = jsonObject.optJSONObject("reels")
            val highObj = dataobj.optJSONObject("highlight:$hightlightId")
            val storyObj = dataobj.optJSONObject(userID)
            var dataObj = storyObj
            if (highObj != null) {
                dataObj = highObj
            }
            username = dataObj.optJSONObject("user").optString("username")
            profileurl = dataObj.optJSONObject("user").optString("profile_pic_url")
            val items = dataObj.optJSONArray("items")
            var item: JSONObject? = null
            var finalUrl = ""
            for (i in 0 until items.length()) {
                item = items.optJSONObject(i)
                item.optString("id")
                url = if (item.optJSONArray("video_versions") != null) {
                    item.optJSONArray("video_versions").getJSONObject(0).getString("url")
                } else {
                    item.getJSONObject("image_versions2").getJSONArray("candidates")
                        .getJSONObject(0).getString("url")
                }
                finalUrl = "$finalUrl$url,"
            }
            if (!TextUtils.isEmpty(finalUrl)) {
                val bean = PostBean()
                bean.url = curUrl
                bean.name = username
                bean.text = ""
                bean.profile_pic = profileurl
                bean.time = System.currentTimeMillis() / 1000
                bean.mediaurls = finalUrl

                return bean
            }
        }
        throw Exception("error")

    }


    private fun getMediaID(url: String?): String {
        var c = Pattern.compile("story_media_id=(\\d*_\\d*)")
        var matcher = c.matcher(url)
        if (matcher.find()) {
            return matcher.group(1)
        } else {
            c = Pattern.compile("/(\\d*)/\\?")
            matcher = c.matcher(url)
            if (matcher.find()) {
                return matcher.group(1)
            }
            c = Pattern.compile("/(\\d*)\\?")
            matcher = c.matcher(url)
            if (matcher.find()) {
                return matcher.group(1)
            }
        }
        return ""
    }


}