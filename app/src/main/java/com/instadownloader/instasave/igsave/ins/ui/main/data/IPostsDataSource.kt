package com.instadownloader.instasave.igsave.ins.ui.main.data

import java.util.*

interface IPostsDataSource {
    interface LoadPostsCallback{
        fun onLoadMediasLoaded(list: LinkedList<PostBean>)
        fun onDataNotAvailable()
    }

    interface GetPostCallback{
        fun onPostLoaded(mediaBean: PostBean)
        fun onDataNotAvailable()
    }

    fun loadPosts(callback: LoadPostsCallback)

    fun insert(media: PostBean)

    fun updatePost(media:PostBean)

    fun refreshPosts()

    fun deleteAllPosts()

    fun deletePost(media:PostBean)
    fun getCachePosts(): LinkedList<PostBean>
    fun deleteCheckedPosts()
}