package com.instadownloader.instasave.igsave.ins

import androidx.lifecycle.MutableLiveData
import com.instadownloader.instasave.igsave.ins.ui.main.data.*
import java.util.*

open class MyRepository (private val localDataSource: PostLocalDataSource) {

    var liveDataSendBlobData = MutableLiveData<String>()
    val liveDataChange = MutableLiveData<LinkedList<PostBean>>()
    val liveDataDownloaded = MutableLiveData(false)
    val liveDataToBrowserWithString= MutableLiveData("")

    val livedataLoginSucWithDialog= MutableLiveData(false)

    fun deleteAllPosts(){
        localDataSource.deleteAllPosts()
    }

    fun deleteCheckedPosts() {
        localDataSource.deleteCheckedPosts()
        liveDataChange.value = localDataSource.getCachePosts()
    }

    fun deletePost(post:PostBean){
        localDataSource.deletePost(post)
        liveDataChange.value = localDataSource.getCachePosts()
    }

    fun loadPosts(callback: IPostsDataSource.LoadPostsCallback){
        localDataSource.loadPosts(callback)
    }

    //插入一个
    fun insetPost(postBean: PostBean) {
        localDataSource.insert(postBean)
        liveDataChange.value = localDataSource.getCachePosts()
    }

    fun getCachePosts(): LinkedList<PostBean> {
       return localDataSource.getCachePosts();
    }

    fun getPostBeanByUrl(url: String): PostBean? {
        return localDataSource.getPostBeanByUrl(url)
    }

    fun isHaveTheTask(url: String): PostBean? {

        //TODO: 暂时check
        return null
//        return localDataSource.isHaveTheTask(url)
    }
    fun isHaveDownloadedMedia(url: String): Boolean {
        return localDataSource.isHaveDownloadedMedia(url)
    }

}