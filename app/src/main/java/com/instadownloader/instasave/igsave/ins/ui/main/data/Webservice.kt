package com.instadownloader.instasave.igsave.ins.ui.main.data

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Url

interface Webservice {

    @GET()
    fun getHtmlText(@Header("Cookie") cookie:String, @Url url: String): Call<ResponseBody>

    @GET()
    fun getHtmlText(@Header("Cookie") cookie:String,  @Header("User-Agent") useragent:String, @Url url: String): Call<ResponseBody>


    @GET()
    fun getHtmlText( @Url url: String): Call<ResponseBody>


    @GET()
    fun getHtmlReelsText( @Url url: String,@Header("User-Agent") useragent:String): Call<ResponseBody>

    @GET()
    fun getStoryText(@Header("Cookie") cookie:String,  @Header("User-Agent") useragent:String, @Url storyUrl: String): Call<ResponseBody>

}