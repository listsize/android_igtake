package com.instadownloader.instasave.igsave.ins

import com.instadownloader.instasave.igsave.ins.ui.main.data.PostLocalDataSource
import com.instadownloader.instasave.igsave.ins.ui.main.data.Webservice
import retrofit2.Retrofit

class MyAppContainer {

    private val localDataSource = PostLocalDataSource()

    val myRepository = MyRepository(localDataSource)
}