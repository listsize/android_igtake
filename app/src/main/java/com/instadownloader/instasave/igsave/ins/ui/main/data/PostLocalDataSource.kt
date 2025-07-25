package com.instadownloader.instasave.igsave.ins.ui.main.data

import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.instadownloader.instasave.igsave.ins.MyApp
import com.instadownloader.instasave.igsave.ins.MyUtils
import java.util.*

class PostLocalDataSource : IPostsDataSource {


    val MIGRATION_1_2  = object :Migration(1,2){
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE ${PostBean.TABLE_NAME} ADD COLUMN mediaurls_downloaded TEXT NOT NULL DEFAULT \"\"")
        }
    }

    private val mediasLocalDatabase: PostDataBase =
            Room.databaseBuilder(MyApp.gContext,PostDataBase::class.java, PostDataBase.DATABASE_NAME).addMigrations(MIGRATION_1_2).build()


    private val handle = Handler(Looper.getMainLooper())

    private val cacheData: LinkedList<PostBean> = LinkedList()

    private val sock = "Sock"


    override fun loadPosts(callback: IPostsDataSource.LoadPostsCallback) {
        if (!cacheData.isEmpty()){
            callback.onLoadMediasLoaded(cacheData)
        }else{
            MyUtils.log("load local posts")
            AsyncTask.execute {
                try {
                    val todoDao = mediasLocalDatabase?.getDao()
                    var listbean = todoDao?.getAll()
                    if (listbean != null) {
                        for (bean in listbean){
                            synchronized(sock) {
                                cacheData.push(bean)
                            }
                        }
                    }
                    handle.post {
                        callback.onLoadMediasLoaded(cacheData)
                    }
                }catch (e:Exception){
                    handle.post {
                        callback.onDataNotAvailable()
                    }
                }
            }
        }

    }

    override fun insert(postBean: PostBean) {
        synchronized(sock){
            if(!cacheData.contains(postBean)){
                cacheData.push(postBean)
                AsyncTask.execute {
                    val todoDao = mediasLocalDatabase?.getDao()
                    var id =  todoDao?.insert(postBean)
                    if (id != null) {
                        postBean.id = id.toInt()
                    }
                }
            }
        }

    }

    override fun updatePost(media: PostBean) {
    }

    override fun refreshPosts() {
    }

    override fun deleteAllPosts() {
        synchronized(sock) {
            cacheData.clear()
        }

        AsyncTask.execute {
            val todoDao = mediasLocalDatabase?.getDao()
            todoDao?.deleteAll()
            MyUtils.deleteAllLocalPosts()
        }
    }

    override fun deleteCheckedPosts() {

        synchronized(sock){
            val deletes =  cacheData.filter { it.itemState == it.ITEM_CHECKED }

            for (temp in  deletes){
                cacheData.remove(temp)
            }
            AsyncTask.execute {
                for (temp in  deletes){
                    val todoDao = mediasLocalDatabase?.getDao()
                    todoDao?.delete(temp)
                    MyUtils.deleteMedias(temp)
                }
            }
        }
    }


    override fun deletePost(post: PostBean) {
        synchronized(sock){
            if(cacheData.contains(post)){
                cacheData.remove(post)
                AsyncTask.execute {
                    val todoDao = mediasLocalDatabase?.getDao()
                    todoDao?.delete(post!!)
                    MyUtils.deleteMedias(post)
                }
            }
        }
    }

    override fun getCachePosts(): LinkedList<PostBean> {
        return cacheData
    }


    fun getPostBeanByUrl(url: String): PostBean? {
        synchronized(sock){
            for (bean in cacheData){
                if (bean.url.equals(url)){
                    return bean
                }
            }
        }
        return null
    }

    fun isHaveTheTask(url: String): PostBean? {
        synchronized(sock){
            for (bean in cacheData){
                if (bean.url == url){
                    return bean
                }
            }
        }
        return null
    }

    fun isHaveDownloadedMedia(url: String): Boolean {
        synchronized(sock){
            for (bean in cacheData){
                for (item in bean.getDownloadedUrls()){
                    if (item == url){
                        return true
                    }
                }
            }
        }
        return false
    }


}