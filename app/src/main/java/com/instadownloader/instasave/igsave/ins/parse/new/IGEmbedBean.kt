package com.instadownloader.instasave.igsave.ins.parse.new

import androidx.annotation.Keep

import com.google.gson.annotations.SerializedName


@Keep
data class IGEmbedBean(
    @SerializedName("context")
    val context: Context,
    @SerializedName("gql_data")
    val gqlData: GqlData
)
@Keep
data class Context(
    @SerializedName("profile_pic_url")
    val profilePicUrl: String,
    @SerializedName("username")
    val username: String,
)
@Keep
data class GqlData(
    @SerializedName("shortcode_media")
    val shortcodeMedia: ShortcodeMedia
)
@Keep
data class ShortcodeMedia(
    @SerializedName("display_url")
    val displayUrl: String,
    @SerializedName("video_url")
    val videoUrl: String,
    @SerializedName("edge_media_to_caption")
    val edgeMediaToCaption: EdgeMediaToCaptionX,
    @SerializedName("edge_sidecar_to_children")
    val edgeSidecarToChildren: EdgeSidecarToChildrenX,
    @SerializedName("__typename")
    val typename: String
)

@Keep
data class EdgeMediaToCaptionX(
    @SerializedName("edges")
    val edges: List<Edge3X>
)
@Keep
data class EdgeSidecarToChildrenX(
    @SerializedName("edges")
    val edges: List<EdgeXXXXX>
)
@Keep
data class Edge3X(
    @SerializedName("node")
    val node: Node4X
)
@Keep
data class Node4X(
    @SerializedName("text")
    val text: String
)
@Keep
data class EdgeXXXXX(
    @SerializedName("node")
    val node: Node5X
)

@Keep
data class Node5X(
    @SerializedName("display_url")
    val displayUrl: String,
    @SerializedName("video_url")
    val videoUrl: String,
    @SerializedName("__typename")
    val typename: String
)







