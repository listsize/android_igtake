package com.instadownloader.instasave.igsave.ins.ui.main

import com.instadownloader.instasave.igsave.ins.ui.main.data.PostBean
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloadListener

open class PostFileDownloadImp : FileDownloadListener() {
    override fun warn(task: BaseDownloadTask?) {
    }

    override fun completed(task: BaseDownloadTask?) {
    }

    override fun pending(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
    }

    override fun error(task: BaseDownloadTask?, e: Throwable?) {
    }

    override fun progress(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
    }

    override fun paused(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
    }

    internal class DownloadTagBean ( index:Int,size:Int,path:String, media:PostBean){
        var post:String? = ""
        var index = 0
        var size = 0
        var path = ""
        var media:PostBean? = null;
        init {
            this.post = post
            this.index = index
            this.size = size
            this.path = path
            this.media = media
        }

        /**
         * 返回是否是最后一个
         *
         * @param index
         * @return
         */
        fun isLast():Boolean{
            if(index == size -1){
                return true
            }

            return false

        }

        fun isFirst():Boolean{
            if(index == 0){
                return true
            }
            return false
        }

    }

}