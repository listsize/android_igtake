package com.instadownloader.instasave.igsave.ins.parse.new
import com.google.gson.Gson
import com.instadownloader.instasave.igsave.ins.MyUtils
import com.instadownloader.instasave.igsave.ins.parse.MyIgBean
import com.instadownloader.instasave.igsave.ins.parse.ParseUtils
import com.instadownloader.instasave.igsave.ins.parse.VideoInfoBean
import com.instadownloader.instasave.igsave.ins.safelyFromJson
import com.instadownloader.instasave.igsave.ins.ui.main.data.PostBean
import com.instadownloader.instasave.igsave.ins.ui.main.data.Webservice
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.Retrofit
import java.util.*
// EmbeddedMediaImage,EmbedSidecar
// json key: edge_sidecar_to_children 多张图片看这个, 然后：display_url
// video key: video_url
class IGEmbedParser2  {

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
                if (content.contains("contextJSON")) {
                    if (body.contains("\"contextJSON\":null")) {
                        // 没有js加载额外数据，单张图片
                        var insItemList = parseHtmlTag(document)
                        insItemList.let {
                            it.instagram_url = originurl
                            var bean =ParseUtils.igBeanToPostBean(it)
                            if (it.downloadInfo.size > 0) {
                                return bean
                            }
                        }
                    } else {
                        var startIndex = content.indexOf("{")
                        var endIndex = content.lastIndexOf("}")
                        if (content.contains("contextJSON")) {
                            startIndex = content.indexOf("contextJSON")
                        }
                        var jsonContent = content.substring(startIndex, endIndex + 1).replace("\\\"", "\"")
                        var check = Stack<Char>()

                        var leftIndex = -1
                        var rightIndex = -1
                        // 获取json串
                        for (i in jsonContent.indices) {
                            if (jsonContent[i] == '{') {
                                check.push('{')
                                if (leftIndex == -1) {
                                    leftIndex = i
                                    rightIndex = leftIndex
                                }
                            } else if (jsonContent[i] == '}' && !check.isEmpty()) {
                                check.pop()
                                rightIndex = i
                            }
                            if (leftIndex != -1 && check.isEmpty()) {
                                jsonContent = jsonContent.substring(leftIndex, rightIndex + 1)
                                break
                            }
                        }

                        MyUtils.log(" jsonContent $jsonContent")
                        var insItemList = parseJson(jsonContent)
                        insItemList?.let {
                            it.instagram_url = originurl
                            return ParseUtils.igBeanToPostBean(it)
                        }
                    }

                }
            }
        }
        throw Exception()
    }


    private fun parseHtmlTag(document: Document): MyIgBean {
        val result = MyIgBean()
        document.getElementsByClass("Caption").firstOrNull()?.let { caption ->
            result.username = caption.getElementsByClass("CaptionUsername").text()
            result.content = caption.ownText()
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
        var json1 = json
        if (json.contains("caption_title_linkified")) {
            var start = json.indexOf("caption_title_linkified")
            var end = json.indexOf("display_src", start)
            json1 = json.substring(0, start) + json.substring(end)
        }
        var data:IGEmbedBean? = null
        kotlin.runCatching {
            data =  Gson().fromJson(json1, IGEmbedBean::class.java)

        }.onFailure {
            json1 = json1.replace("\\\\\"","\\\"")
            data =  Gson().safelyFromJson(json1, IGEmbedBean::class.java)
        }
        // 解析owner信息
        var result = MyIgBean()
        data?.let {
            it.context.let { context ->
                result.profile_pic = context.profilePicUrl.replace("\\", "")
                result.username = String(context.username.toByteArray())
            }

            it.gqlData.let { gqlData ->
                var content = gqlData.shortcodeMedia.edgeMediaToCaption.edges.firstOrNull()?.node?.text ?: ""
                result.content = ""
                unescapeJava(content)?.let {
                    result.content = it
                }
                // 多张的话 edgeSidecarToChildren不为空
                gqlData.shortcodeMedia.let { media ->
                    when (media.typename) {
                        "GraphImage" -> {
                            var image = VideoInfoBean(false)
                            image.downloadUrl = media.displayUrl.replace("\\", "")
                            result.downloadInfo.add(image)
                            return@let
                        }
                        "GraphVideo" -> {
                            var image = VideoInfoBean(true)
                            image.downloadUrl = media.videoUrl.replace("\\", "")
                            result.downloadInfo.add(image)
                            return@let
                        }
                    }
                    media.edgeSidecarToChildren.edges.forEach { edge ->
                        when (edge.node.typename) {
                            "GraphImage" -> {
                                var image = VideoInfoBean(false)
                                image.downloadUrl = edge.node.displayUrl.replace("\\", "")
                                result.downloadInfo.add(image)
                            }
                            "GraphVideo" -> {
                                var image = VideoInfoBean(true)
                                image.downloadUrl = edge.node.videoUrl.replace("\\", "")
                                result.downloadInfo.add(image)
                            }
                        }
                    }
                }
            }
        }

        return result
    }

    private fun unescapeJava(escaped: String): String? {
        var escaped = escaped
        if (escaped.indexOf("\\u") == -1) return escaped
        var processed = ""
        var position = escaped.indexOf("\\u")
        while (position != -1) {
            if (position != 0) processed += escaped.substring(0, position)
            val token = escaped.substring(position + 2, position + 6)
            escaped = escaped.substring(position + 6)
            processed += token.toInt(16).toChar()
            position = escaped.indexOf("\\u")
        }
        processed += escaped
        return processed
    }

}