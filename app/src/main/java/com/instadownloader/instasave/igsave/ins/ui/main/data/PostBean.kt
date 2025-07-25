package com.instadownloader.instasave.igsave.ins.ui.main.data

import android.text.TextUtils
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey


@Entity(tableName = PostBean.TABLE_NAME)
open class PostBean {

    companion object {
        const val TABLE_NAME = "post"
    }
    @PrimaryKey(autoGenerate = true)
    var id:Int = 0

    @ColumnInfo(name = "url")
    var url:String = ""

    @ColumnInfo(name = "name")
    var name:String = ""

    @ColumnInfo(name = "text")
    var text:String = ""

    @ColumnInfo(name = "profile_pic")
    var profile_pic:String = ""

    @ColumnInfo(name = "time")
    var time:Long = 1

    //这是帖子所有资源的url.但是不代表已经下载成功的url
    @ColumnInfo(name = "mediaurls")
    var mediaurls:String = ""

    //已经下载成功了的url
    @ColumnInfo(name = "mediaurls_downloaded")
    var mediaurls_downloaded:String = ""

    @ColumnInfo(name = "videotime")
    var videotime:Long = 1

    @ColumnInfo(name = "videothumb")
    var videothumb:String=""

    @ColumnInfo(name = "localuris")
    var localuris:String=""

    @Ignore
    val ITEM_IDEL = 0
    @Ignore
    val ITEM_CHECKED = 1
    @Ignore
    val ITEM_UNCHECKED = 2

    @Ignore
    var itemState:Int = ITEM_IDEL

    @Ignore
    private var imgsArry:ArrayList<String> = ArrayList()
    fun getPosturls():ArrayList<String>{
        imgsArry.clear()
        var list = mediaurls.split(",")
        for (l in list){
            if(!TextUtils.isEmpty(l)){
                imgsArry.add(l)
            }
        }
        return imgsArry
    }

    fun arrayUrlsToStringUrls(array:ArrayList<String>):String{
        var urls  = ""
        for (u in array){
            urls = "$urls$u,"
        }
        return urls
    }

    @Ignore
    var urisArry:ArrayList<String> = ArrayList()    //由于数据库储存的是原始数据。所以切割
    //由于数据库储存的是原始数据。所以切割
    fun getPostLocalUris():ArrayList<String>{
        if(urisArry.size > 0){
            return urisArry
        }

        var list = localuris.split(",")
        for (l in list){
            if(!TextUtils.isEmpty(l)){
                urisArry.add(l)
            }
        }
        return urisArry
    }


    @Ignore
    var downloaded_url_Array:ArrayList<String> = ArrayList()    //由于数据库储存的是原始数据。所以切割
    //由于数据库储存的是原始数据。所以切割
    fun getDownloadedUrls():ArrayList<String>{
        if(downloaded_url_Array.size > 0){
            return downloaded_url_Array
        }

        var list = mediaurls_downloaded.split(",")
        for (l in list){
            if(!TextUtils.isEmpty(l)){
                downloaded_url_Array.add(l)
            }
        }
        return downloaded_url_Array
    }

    override fun toString(): String {
        return "id $id  name $name  profile_pic$profile_pic   mediaurls$mediaurls  text$text"
    }

//    override fun equals(other: Any?): Boolean {
//        if(other is PostBean){
//            if (other.url == url && other.mediaurls == ){
//                return true
//            }
//        }
//        return super.equals(other)
//    }

}