package com.instadownloader.instasave.igsave.ins

import com.instadownloader.instasave.igsave.ins.ui.main.data.ads.AdsBean
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Url

interface AdsWebservice {
    @GET("{user}/test/main/{id}_v10.json")
    fun listAds(@Path("user") user:String, @Path("id") id:String): Call<List<AdsBean>>
}