package com.instadownloader.instasave.igsave.ins.parse

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName


@Keep
class IGPostBean {
    @SerializedName("items")
    var items: List<ItemsDTO>? = null

    @Keep
    class ItemsDTO {

        @SerializedName("id")
        var id: String? = null

        @SerializedName("media_type")
        var mediaType: Int? = null

        @SerializedName("carousel_media_count")
        var carouselMediaCount: Int? = 0

        @SerializedName("carousel_media")
        var carouselMedia: List<CarouselMediaDTO>? = null

        @SerializedName("user")
        var user: UserDTO? = null



        @SerializedName("caption")
        var caption: CaptionDTO? = null


        @SerializedName("image_versions2")
        var imageVersion2: CarouselMediaDTO.ImageVersions2DTO? = null

        @SerializedName("video_versions")
        var videoVersions: List<CarouselMediaDTO.VideoVersionsDTO?>? = null

        @SerializedName("has_audio")
        var has_audio: Boolean = false

        @Keep
        class UserDTO {


            @SerializedName("username")
            var username: String? = null


            @SerializedName("profile_pic_url")
            var profilePicUrl: String? = null

        }

        @Keep
        class CaptionDTO {
            @SerializedName("text")
            var text: String? = null

            @SerializedName("type")
            var type: Int? = null

        }



        @Keep
        class CarouselMediaDTO {
            @SerializedName("id")
            var id: String? = null

            @SerializedName("media_type")
            var mediaType: Int? = null

            @SerializedName("image_versions2")
            var imageVersions2: ImageVersions2DTO? = null

            @SerializedName("video_versions")
            var videoVersions: List<VideoVersionsDTO>? = null


            @Keep
            class ImageVersions2DTO {
                @SerializedName("candidates")
                var candidates: List<CandidatesDTO>? = null

                @Keep
                class CandidatesDTO {
                    @SerializedName("width")
                    var width: Int? = null

                    @SerializedName("height")
                    var height: Int? = null

                    @SerializedName("url")
                    var url: String? = null
                }
            }

            @Keep
            class VideoVersionsDTO {
                @SerializedName("type")
                var type: Int? = null

                @SerializedName("width")
                var width: Int? = null

                @SerializedName("height")
                var height: Int? = null

                @SerializedName("url")
                var url: String? = null

                @SerializedName("id")
                var id: String? = null
            }
        }
    }
}