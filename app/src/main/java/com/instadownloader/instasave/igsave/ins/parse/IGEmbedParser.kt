package com.instadownloader.instasave.igsave.ins.parse


import com.google.gson.JsonParser
import com.instadownloader.instasave.igsave.ins.MyUtils
import com.instadownloader.instasave.igsave.ins.ui.main.data.PostBean
import com.instadownloader.instasave.igsave.ins.ui.main.data.Webservice
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.Retrofit


class IGEmbedParser {

    val API_END = "embed/captioned/"
    val retrofit: Webservice = Retrofit.Builder()
        .baseUrl("https://www.instagram.com")
        .build()
        .create(Webservice::class.java)

    fun parsePost(
        originurl: String,
        realUrl: String,body:String
    ): PostBean {
        var document = Jsoup.parse(body)
        document?.let {

            it.getElementsByClass("WatchOnInstagram").firstOrNull()?.let {
                throw Exception("Watch on Instagram")
            }

            var elements = document.getElementsByTag("script")


            for (e in elements) {
                var content = e.toString()
                if (content.contains("window.__additionalDataLoaded(") || content.contains("window.__additionalDataLoaded (")) {
                    var startIndex = content.indexOf("{")
                    var endIndex = content.lastIndexOf("}")
                    if (startIndex < 0) {
                        // 没有js加载额外数据，单张图片
                        var insItemList = parseHtmlTag(document)
                        insItemList.let {insItem->
                            insItem.instagram_url = originurl
                            var bean =ParseUtils.igBeanToPostBean(insItem)
                            if (insItem.downloadInfo.size > 0) {
                                return bean
                            }
                        }
                    } else {
                        var jsonContent = content.substring(startIndex, endIndex + 1)
                        MyUtils.log("parseWithUrl jsonContent $jsonContent")
                        var insItemList = parseJson(jsonContent)
                        insItemList?.let {insItem->
                            insItem.instagram_url = originurl
                         return ParseUtils.igBeanToPostBean(insItem)
                        }
                    }
                }
            }
        }
        throw Exception()
    }

    private fun parseHtmlTag(document: Document): MyIgBean {
        val result = MyIgBean()
        document.getElementsByClass("WatchOnInstagram").firstOrNull()?.let {
           throw Exception("Watch on Instagram")
        }
        document.getElementsByClass("Caption").firstOrNull()?.let { caption ->
            result.username = caption.getElementsByClass("CaptionUsername").text()
            result.content = caption.text()
        }
        document.getElementsByClass("Header").firstOrNull()?.let { header ->
            header.getElementsByClass("Avatar InsideRing").firstOrNull()?.let { avatar ->
                result.profile_pic = avatar.getElementsByTag("img").firstOrNull()?.attr("src").toString()
            }
        }
        document.getElementsByClass("EmbeddedMedia").firstOrNull()?.let { media ->
            media.getElementsByTag("video").firstOrNull()?.let {
                val info = VideoInfoBean(isVideo = true)
                info.downloadUrl = it.attr("src")
                result.downloadInfo.add(info)
            } ?: let {
                media.getElementsByTag("img").firstOrNull()?.let {
                    val info = VideoInfoBean(isVideo = false)
                    info.downloadUrl = it.attr("src")
                    result.downloadInfo.add(info)
                }
            }
        }
        return result
    }

    private fun parseJson(json: String): MyIgBean? {

        var jsonObject =
            JsonParser.parseString(json)?.asJsonObject?.get("shortcode_media")?.asJsonObject
        // 解析owner信息
        var result = MyIgBean()
        jsonObject?.get("owner")?.asJsonObject?.let {
            var username = it.get("username")?.asString
            var headerIcon = it.get("profile_pic_url")?.asString
            if (headerIcon != null) {
                result.profile_pic = headerIcon
            }
            if (username != null) {
                result.username = username
            }
        }
        // 解析发帖的文字
        jsonObject?.get("edge_media_to_caption")?.asJsonObject?.let {
            var text =
                it.get("edges")?.asJsonArray?.firstOrNull()?.asJsonObject?.get("node")?.asJsonObject?.get(
                    "text"
                )?.asString
            if (text != null) {
                result.content = text
            }
        }
        // 解析图片
        jsonObject?.let {
            return when (it.get("__typename").asString) {
                "GraphSidecar" -> {
                    jsonObject.get("edge_sidecar_to_children")?.asJsonObject?.get("edges")?.asJsonArray?.let { nodes ->
                        //TODO 进一步提取
                        val iterator = nodes.iterator()
                        while (iterator.hasNext()) {
                            var element = iterator.next().asJsonObject.get("node")
                            element.asJsonObject.let { element ->
                                var image = VideoInfoBean(true)
                                image.isVideo = element.get("is_video").asBoolean
                                if (image.isVideo) {
                                    image.downloadUrl = element.get("video_url").asString
                                } else {
                                    image.downloadUrl = element.get("display_url").asString
                                }
                                result.downloadInfo.add(image)
                            }
                        }
                        return result
                    }
                }
                "GraphImage" -> {
                    jsonObject.get("display_url")?.asString.let { url ->
                        MyUtils.log("parseJson-jsonDisplay:$url")
                        var image = VideoInfoBean(true)
                        if (url != null) {
                            image.downloadUrl = url
                        }
                        image.isVideo = false
                        result.downloadInfo.add(image)
                        return result
                    }
                }
                "GraphVideo" -> {
                    jsonObject.get("video_url")?.asString.let { url ->
                        MyUtils.log("parseJson-jsonDisplay:$url")
                        var image = VideoInfoBean(true)
                        if (url != null) {
                            image.downloadUrl = url
                        }
                        image.isVideo = true
                        image.video_duration = jsonObject.get("video_duration")?.asString.toString()
                        result.downloadInfo.add(image)
                        return result
                    }
                }
                else -> return null
            }
        }
        return null
    }

}