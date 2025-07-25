package com.instadownloader.instasave.igsave.ins.ui.main.browser

import android.app.Application
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.instadownloader.instasave.igsave.ins.MyApp
import com.instadownloader.instasave.igsave.ins.MyRepository
import com.instadownloader.instasave.igsave.ins.MyUtils
import com.instadownloader.instasave.igsave.ins.R
import com.instadownloader.instasave.igsave.ins.parse.ParseUtils
import com.instadownloader.instasave.igsave.ins.ui.main.PostFileDownloadImp
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloadListener
import com.liulishuo.filedownloader.FileDownloadQueueSet
import com.liulishuo.filedownloader.FileDownloader
import java.io.File
import java.util.LinkedList
import java.util.concurrent.Executors

class SelectPostDialogViewModel(application: Application) :AndroidViewModel(application),OnClickSelectPost {

    var userBean: BrowserPostInfo? = null
    private var mediasList = ArrayList<BrowserPostMediaBean>()
    private var mRepository: MyRepository = (application as MyApp).appContainer.myRepository

    var livedataMediasList = MutableLiveData<ArrayList<BrowserPostMediaBean>>()


    var downloadNums = ObservableInt(0)
    var isEndLoad = ObservableBoolean(false)
    val livedataClickDownload = MutableLiveData(false)

    val livedataClickClose = MutableLiveData(false)

    var selectedList :List<BrowserPostMediaBean> = ArrayList<BrowserPostMediaBean>()

    init {
    }

    private fun checkDownloaded(bean: BrowserPostMediaBean){
        var isDownloaded = false
        if (bean.videoUrl.isNotEmpty()){
            isDownloaded = mRepository.isHaveDownloadedMedia(bean.videoUrl)
        }else{
            isDownloaded =  mRepository.isHaveDownloadedMedia(bean.displayUrl)
        }
        if (isDownloaded){
            bean.state = BrowserPostMediaBean.STATE_DOWNLOADED
        }
    }

    fun addMedia(bean: BrowserPostMediaBean?) {
        bean?.let {
            checkDownloaded(it)
            mediasList.add(it)

            livedataMediasList.postValue(mediasList)
            dealSelectedItems()
        }
    }

    fun initData(cacheList: ArrayList<BrowserPostMediaBean>) {

        mediasList = cacheList
        for (item in mediasList){
            checkDownloaded(item)
        }
        livedataMediasList.postValue(mediasList)
        dealSelectedItems()
    }

    private fun dealSelectedItems(){
        selectedList = mediasList.filter {
            it.state == BrowserPostMediaBean.STATE_CHECKED
        }
        downloadNums.set(selectedList.size)
    }

    override fun onClickItem(bean: BrowserPostMediaBean) {
        dealSelectedItems()
    }

    fun onClickClose(view:View){
        Log.e("ads","onClickClose")
        livedataClickClose.postValue(true)
    }

    fun onClickDownload(view:View){
        Log.e("ads","onClickDownload")

        //如果是一个blob视频，则走老加载
        if (selectedList.size == 1 && selectedList[0].origin_post.isNotEmpty()){
            mRepository.liveDataSendBlobData.postValue(selectedList[0].origin_post)
        }else{
            userBean?.let { userBean->
                for (item in selectedList){
                    if (item.videoUrl.isNotEmpty()){
                        userBean.infoRes.mediaUrls.add(item.videoUrl)
                    }else{
                        userBean.infoRes.mediaUrls.add(item.displayUrl)
                    }
                }
                startDownload(userBean)
            }
        }

        livedataClickDownload.value = true
    }


    val executor = Executors.newSingleThreadExecutor()
    private val handl = Handler(Looper.getMainLooper())
    private val queueTarget: FileDownloadListener = object : PostFileDownloadImp() {
        var localUris: ArrayList<String> = ArrayList()
        var downloadedUrls: ArrayList<String> = ArrayList()

        override fun completed(task: BaseDownloadTask?) {
            super.completed(task)
            val bean = task?.tag as DownloadTagBean
            executor.execute {

                var uri: Uri?
                try {
                    uri = MyUtils.insertMedia(task.path)
                    MyUtils.deleteFile(task.path)
                }catch (e:java.lang.Exception){
                    e.printStackTrace()
                    curDownload = null
                    return@execute;
                }

                handl.post {
                    if(bean.isFirst()){ //如果第一张图片或视频完成则通知界面刷新。
                        localUris.clear()
                        downloadedUrls.clear()
                    }
                    localUris.add(uri.toString())
                    downloadedUrls.add(task.url)

                    MyUtils.log("done ${task.path}")

                    if(bean.isLast()){ //下载结束
                        MyUtils.log("isLast done ${task.path}")
                        var localUris = ""
                        for (localUri in this.localUris) {
                            localUris = "$localUris$localUri,"
                        }
                        bean.media?.localuris = localUris

                       var downloadUris = ""
                        for (downloadedUrl in downloadedUrls) {
                            downloadUris = "$downloadUris$downloadedUrl,"
                        }
                        bean.media?.mediaurls_downloaded = downloadUris

                        MyUtils.toast(R.string.tip_downloaded)
                        bean.media?.let {
                            mRepository.insetPost(it)
                        }
                        curDownload = null
                        continueDownloadQueue()

                        mRepository.liveDataDownloaded.postValue(true)
                    }
                }
            }
        }

        override fun error(task: BaseDownloadTask?, e: Throwable?) {
            super.error(task, e)
            e?.printStackTrace()
            curDownload = null
        }
    }


    val queueSet = FileDownloadQueueSet(queueTarget)

    private val queueList = LinkedList<BrowserPostInfo>()
    private var curDownload:BrowserPostInfo? = null

    private fun startDownload(browserBean: BrowserPostInfo) {
        if (!queueList.contains(browserBean)){
            queueList.addLast(browserBean)
            Log.e("ads","已添加到队列")
        }else{
            Log.e("ads","已经在下载队列")

        }

        if (curDownload == null){
            curDownload =  queueList.removeFirst()
            curDownload?.let {
                startTask(it)
            }
        }else{
            Log.e("ads","正在下载中 ")
        }

    }

    private fun continueDownloadQueue() {
        curDownload = null
        if (queueList.isNotEmpty()){
            Log.e("ads","continueDownloadQueue ")

            curDownload =  queueList.removeFirst()
            curDownload?.let {
                startTask(it)
            }
        }else{
            Log.e("ads","continueDownloadQueue is empty ")

        }

    }


    //开始下载
    private fun startTask(browserBean: BrowserPostInfo) {
        val postBean =  ParseUtils.browserBeanToPostBean(browserBean)
        MyUtils.toast(R.string.tip_start_download)
        var imgs = postBean.getPosturls()
        val tasks = ArrayList<BaseDownloadTask>()
        if(imgs != null){
            for (i in 0 until imgs.size) {
                var path = MyUtils.getSavePath()+ File.separator + MyUtils.getPostFileName(imgs[i])
                MyUtils.log("path $path")
                tasks.add(FileDownloader.getImpl().create(imgs[i]).setPath(path).setTag(
                    PostFileDownloadImp.DownloadTagBean(i,imgs.size,path,postBean)))
            }
        }
        queueSet.setAutoRetryTimes(1)// 所有任务在下载失败的时候都自动重试一次
        queueSet.downloadSequentially(tasks);
        queueSet.start()
    }


}