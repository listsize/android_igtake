package com.instadownloader.instasave.igsave.ins.ui.main

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.instadownloader.instasave.igsave.ins.MyApp
import com.instadownloader.instasave.igsave.ins.MyFireBaseUtils
import com.instadownloader.instasave.igsave.ins.MyUtils
import com.instadownloader.instasave.igsave.ins.R
import com.instadownloader.instasave.igsave.ins.ViewActivity
import com.instadownloader.instasave.igsave.ins.exception.HomePageLinkException
import com.instadownloader.instasave.igsave.ins.exception.NetworkException
import com.instadownloader.instasave.igsave.ins.exception.NotFindPostException
import com.instadownloader.instasave.igsave.ins.exception.PrivateLinkException
import com.instadownloader.instasave.igsave.ins.exception.UnSupportLinkException
import com.instadownloader.instasave.igsave.ins.parse.IGCookieParser
import com.instadownloader.instasave.igsave.ins.parse.IGEmbedParser
import com.instadownloader.instasave.igsave.ins.parse.IgStoryParser
import com.instadownloader.instasave.igsave.ins.parse.new.IGEmbedParser2
import com.instadownloader.instasave.igsave.ins.ui.main.data.LoginTool
import com.instadownloader.instasave.igsave.ins.ui.main.data.PostBean
import com.instadownloader.instasave.igsave.ins.ui.main.data.Webservice
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloadListener
import com.liulishuo.filedownloader.FileDownloadQueueSet
import com.liulishuo.filedownloader.FileDownloader
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.Retrofit
import java.io.File
import java.util.LinkedList
import java.util.concurrent.Executors
import java.util.regex.Pattern

class LoadViewModel (application: Application) : AndroidViewModel(application) {

    private var generate = 1
    var mRepository = (application as MyApp).appContainer.myRepository

    private val STATE_IDLE = generate++
    private val STATE_LOADING = generate++
    private val STATE_DOWNLOADING = generate++
    private var state = STATE_IDLE

    var tempActivity :Activity? = null
    val E_DOWNLOAD_DONE = generate++
    val E_DOWNLOAD_FAIL = generate++
    val E_CLICK_ITEM = generate++

    val E_LOADING_START = generate++
    val E_LOADING_ERROR = generate++
    val E_LOADING_DONE = generate++
    val E_LOADING_SUC = generate++

    private var mCompositeDisposable = CompositeDisposable()

    var curUrl: String  = ""
    var loginStateCurUrl :String = ""
    var curSucBean:PostBean ? =null
    val executor = Executors.newSingleThreadExecutor()
    val handler = Handler(Looper.getMainLooper())

    val liveDataDownloadProgress = MutableLiveData<Int>()
    val livedataAlreadyDownload = MutableLiveData<Boolean>()
    val livedataFirstFile = MutableLiveData<String>()
    val liveDataMVideoIcon= MutableLiveData<Boolean>()
    val liveDataProfilePic = MutableLiveData<String>()
    val liveDataContent = MutableLiveData<String>()
    val liveDataName = MutableLiveData<String>()


    val liveDataShowLogin = MutableLiveData<Boolean>()
    val liveDataReadyLoad = MutableLiveData<Boolean>()
    val liveDataEditTextColor = MutableLiveData<Int>()

    val liveDataLoadSucAndDownload = MutableLiveData<Boolean>()
    val liveDataLoadSucAndSelect = MutableLiveData<Boolean>()


    val liveDataLoadError = MutableLiveData<Boolean>()
    val liveDataLoadDone = MutableLiveData<Boolean>()


    val liveDataManyPicIcon= MutableLiveData<Boolean>()

    val liveDataDownloadState = MutableLiveData<Boolean>()


    val liveDataShowPlsWaitDialog  = MutableLiveData<Boolean>()


    val liveDataSendBlobData =     mRepository.liveDataSendBlobData.map {
        it
    }

    val livedataDownloaded = mRepository.liveDataDownloaded.map {
        it
    }


    private val waitQueue:LinkedList<String> = LinkedList();

    private val retrofit: Webservice = Retrofit.Builder()
        .baseUrl("https://www.instagram.com")
        .build()
        .create(Webservice::class.java)

    private fun setState(s:Int){
        if(s == STATE_IDLE){  //当切换到IDEL状态时 ，设置默认值
            curUrl = ""
        }

        state =  s
    }

    fun showInterAds(activity: Activity) {
        MyUtils.showInterAds(activity)
        curSucBean?.let { bean->
            MyUtils.gotoUrl = bean.url
        }
    }


    private fun executeEvent(event:Int,obj: Any){
        when(state){
            STATE_IDLE->{
                when(event){
                    E_CLICK_ITEM->{
                        try {
                            tempActivity?.let {
                                if (MyUtils.isNeedShowInterAds()){
                                    liveDataShowPlsWaitDialog.postValue(true)
                                    return
                                }
                            }

                            if (curSucBean != null){
                                val  intent = Intent(MyApp.gContext, ViewActivity::class.java)
                                var bundle = Bundle()
                                bundle.putString("url",curSucBean?.url)
                                intent.putExtras(bundle)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                MyApp.gContext.startActivity (intent)
                            }
                        }
                        catch (e:Exception){
                            e.printStackTrace()
                        }
                    }
                    E_LOADING_START->{
                        setState(STATE_LOADING)
                        if (!waitQueue.isEmpty()){
                            startLoadPost()
                        }else{
                            setState(STATE_IDLE)
                        }

                    }
                }
            }

            STATE_LOADING->{
                when(event){
                    E_LOADING_SUC->{
                        val bean = obj as PostBean
                        doLoadingSuc(bean)
                    }
                    E_LOADING_ERROR->{
                        liveDataLoadError.value = true
                        setState(STATE_IDLE)
                    }
                    E_LOADING_DONE->{
                        liveDataLoadDone.value = true
                        setState(STATE_IDLE)
                    }
                    E_LOADING_START->{
                        addLoadToQueue(obj as String)
                        MyUtils.toast(R.string.com_running)
                    }
                }
            }

            STATE_DOWNLOADING ->{
                when(event){
                    E_DOWNLOAD_DONE->{
                        setState(STATE_IDLE)
                        downloadSuc(obj as PostBean)
                        executeEvent(E_LOADING_START,"")  //下载等待队列的
                    }
                    E_DOWNLOAD_FAIL->{
                        setState(STATE_IDLE)
                        downloadFail()
                        executeEvent(E_LOADING_START,"") //下载等待队列的

                    }
                    E_LOADING_START->{
                        addLoadToQueue(obj as String)
                        MyUtils.toast(R.string.com_running)
                    }

                }
            }
        }
    }

    private val SYNC = ""
    var curSelectBean:PostBean?= null

    private fun doLoadingSuc(bean:PostBean){
        MyUtils.log(bean.mediaurls)

        if (bean.getPosturls().size > 1){
            curSelectBean = bean
            liveDataLoadSucAndSelect.value = true
        }else{
            startDownload(bean)
            liveDataLoadSucAndDownload.value = true
            setState(STATE_DOWNLOADING)
        }
    }

    fun selectedItems(selectedItems: ArrayList<String>) {
        curSelectBean?.let {
            it.mediaurls = it.arrayUrlsToStringUrls(selectedItems)
            startDownload(it)
            liveDataLoadSucAndDownload.value = true
            setState(STATE_DOWNLOADING)
        }

    }

    private fun getALoadUrl():String {
        synchronized(SYNC) {
            return waitQueue.removeFirst()
        }
    }

    private fun addLoadToQueue(s: String) {
        synchronized(SYNC){
            for (item in waitQueue){
                if (item == s){
                    MyUtils.log("已经存在队列 $s")
                    return
                }
            }
            waitQueue.addLast(s)
        }
    }

    //开始下载
    private fun startDownload(postBean: PostBean) {
        var imgs = postBean.getPosturls()
        val tasks = ArrayList<BaseDownloadTask>()
        if(imgs != null){
            for (i in 0 until imgs.size) {
                var path = MyUtils.getSavePath()+ File.separator +MyUtils.getPostFileName(imgs[i])
                MyUtils.log("path $path")
                tasks.add(FileDownloader.getImpl().create(imgs[i]).setPath(path).setTag(PostFileDownloadImp.DownloadTagBean(i,imgs.size,path,postBean)))
            }
        }
        queueSet.setAutoRetryTimes(1)// 所有任务在下载失败的时候都自动重试一次
        queueSet.downloadSequentially(tasks);
        queueSet.start()
        liveDataProfilePic.value = postBean.profile_pic
        liveDataContent.value = postBean.text
        liveDataName.value = postBean.name
        liveDataManyPicIcon.value =imgs.size > 1
        liveDataDownloadProgress.value = 1
    }

    private fun downloadSuc(postBean: PostBean){
        mRepository.insetPost(postBean)
        showDownloadSuc(postBean)
    }

    private fun showDownloadSuc(postBean: PostBean){
        liveDataDownloadProgress.value =0
        MyUtils.clearClipBoardContent(postBean.url)
        curSucBean = postBean
        liveDataDownloadState.value = true
        MyUtils.isHaveSucDownload = true
    }

    private fun downloadFail() {
        MyUtils.toast(R.string.com_occur_error)
        liveDataDownloadProgress.value =0
        liveDataDownloadState.value = false
    }

    private fun calcDownloadProgress(size:Int,index:Int,soFarBytes:Int,totalBytes:Int):Int{//计算下载进度
        var unit = 100.0/size  //每个资源占的百分比
        var curUnit = index *unit.toInt() //当前下载的区间
        var per = (curUnit +soFarBytes.toDouble() / totalBytes * unit).toInt()
        return per
    }

    fun onClickItem() {
        executeEvent(E_CLICK_ITEM,"")
    }

    private val queueTarget: FileDownloadListener = object : PostFileDownloadImp() {
        var localUris: ArrayList<String> = ArrayList()
        var downloadedUrls: ArrayList<String> = ArrayList()

        override fun progress(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
            super.progress(task, soFarBytes, totalBytes)
            val bean = task?.tag as DownloadTagBean
            var progress =  calcDownloadProgress(bean.size,bean.index,soFarBytes,totalBytes)
//            MyUtils.log("pro $progress  $soFarBytes,  $totalBytes" )
            if (progress > 1){
                liveDataDownloadProgress.value = progress
            }
        }
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
                    handler.post {
                        executeEvent(E_DOWNLOAD_FAIL,"")
                    }
                    return@execute;
                }

                handler.post {
                    if(bean.isFirst()){ //如果第一张图片或视频完成则通知界面刷新。
                        localUris.clear()
                        downloadedUrls.clear()
                        livedataFirstFile.value = uri.toString()
                        liveDataMVideoIcon.value = bean.path.contains(".mp4")
                    }
                    localUris.add(uri.toString())
                    downloadedUrls.add(task.url)

                    MyUtils.log("done ${task.path}")
                    if(bean.isLast()){ //下载结束
                        MyUtils.log("isLast done ${task.path}")
                        var uris = ""
                        for (url in localUris) {
                            uris = "$uris$url,"
                        }
                        bean.media?.localuris = uris

                        var downloadedUris = ""
                        for (downloadedUrl in downloadedUrls) {
                            downloadedUris = "$downloadedUris$downloadedUrl,"
                        }
                        bean.media?.mediaurls_downloaded = downloadedUris

                        MyUtils.toast(R.string.tip_downloaded)
                        executeEvent(E_DOWNLOAD_DONE,bean.media!!)
                    }
                }
            }
        }
        override fun error(task: BaseDownloadTask?, e: Throwable?) {
            super.error(task, e)
            e?.printStackTrace()
            executeEvent(E_DOWNLOAD_FAIL,"")
        }
    }
    val queueSet = FileDownloadQueueSet(queueTarget)

    fun requestLoadPost(originUrl: String) {
        val url = originUrl.replace("/reel/","/p/")
        if (MyUtils.isInstagramUrl(url)) {
            var bean:PostBean? = mRepository.isHaveTheTask(url)
            if (bean != null){
                alreadyDownload(bean)
                MyUtils.toast(MyApp.gContext.getString(R.string.toast_aleady_exists))
            }else{
                if (url != curUrl){
                    addLoadToQueue(url)
                    executeEvent(E_LOADING_START,url)
                }
            }
        }else{
            liveDataEditTextColor.postValue(R.color.red)
            MyUtils.toast(R.string.tip_illegal_url)
        }
    }

    private fun alreadyDownload(bean: PostBean) {
        liveDataDownloadProgress.value =0
        curSucBean = bean
        livedataAlreadyDownload.value = true

        liveDataDownloadState.value = true
        MyUtils.isHaveSucDownload = true
    }

    private fun startLoadPost() {
        liveDataReadyLoad.postValue(true)
        curUrl  = getALoadUrl()
        executeLoadPosts()
    }

    private fun scraperEmbedNoLogin():PostBean{
        val realUrl =  MyUtils.getEmbedApiUrl(curUrl)
        val response = retrofit.getHtmlText(realUrl).execute()
        val body = response.body()!!.string()

        try {
            val api = IGEmbedParser()
            MyUtils.log(" scraperEmbedNoLogin   $realUrl")
            var bean = api.parsePost(curUrl ,realUrl,body)
            if (TextUtils.isEmpty(bean.mediaurls)){
                throw Exception("null")
            }
            return bean
        }catch (e:Exception){
            MyUtils.log(" scraperEmbedNoLogin1   失败")

        }

        val api = IGEmbedParser2()
        MyUtils.log(" scraperEmbedNoLogin2   $realUrl")
        var bean = api.parsePost(curUrl ,realUrl,body)
        if (TextUtils.isEmpty(bean.mediaurls)){
            throw Exception("null")
        }

        return bean
    }

    private fun scraperIGWithLogin() :PostBean{
        val api = IGCookieParser()
        val realUrl =  curUrl
        MyUtils.log(" scraperIGWithCookie   $realUrl")
        var bean = api.parseCommonPhoto(curUrl,realUrl)
        return bean
    }

    private fun scraperStory():PostBean {
       val api = IgStoryParser()
       return api.parseStory(curUrl)
    }

    private fun executeLoadPosts(){
        val disposable: Disposable = Observable.just(curUrl)
            .subscribeOn(AndroidSchedulers.mainThread())
            .observeOn(Schedulers.io())
            .map {
                if (curUrl.contains("/p/")
                    || curUrl.contains("/reel/")
                    || curUrl.contains("/tv/")){
                    //普通帖子
                    try {
                       return@map scraperEmbedNoLogin()
                    }catch (e:Throwable){
                        e.printStackTrace()
                        //如果是网络错误则直接结束事件流
                        if (MyUtils.isNetworkException(e)){ throw e }
                    }
                   return@map scraperIGWithLogin()

                }else if (curUrl.contains("/s/") or curUrl.contains("stories") or curUrl.contains("story_media_id")){
                    return@map scraperStory()
                }
                else if (getHomePageNameByUrl(curUrl).isNotEmpty()){
                    throw HomePageLinkException()
                }
                else{
                    throw UnSupportLinkException()
                }

            }.observeOn(AndroidSchedulers.mainThread())
            .subscribe({ bean ->
                MyUtils.log("成功 " )
                executeEvent(E_LOADING_SUC,bean!!)
            }, {throwable->

                val errorLink = curUrl
                executeEvent(E_LOADING_ERROR,"")

                dealLoadException(throwable,errorLink)

            }, {
                MyUtils.log("成功 ERROR" )
                executeEvent(E_LOADING_DONE,"")
            })
        mCompositeDisposable.add(disposable)

    }

    private fun dealLoadException(throwable:Throwable,link:String=""){
        if (throwable is PrivateLinkException){
            MyUtils.toast(R.string.toast_private_post)
        }
        else if (throwable is NotFindPostException){
            MyUtils.toast(R.string.toast_not_find)
        }
        else if (throwable is NetworkException){
            MyUtils.toast(R.string.toast_network_error)
        }
        else if (throwable is UnSupportLinkException){
            MyUtils.toast(R.string.toast_unsupport_link)
        }
        else if (MyUtils.isNetworkException(throwable)){
            MyUtils.toast(R.string.toast_network_error)
        }
        else if (throwable is HomePageLinkException){
            MyUtils.clearClipBoardContent(link)
            mRepository.liveDataToBrowserWithString.postValue(link)
            MyUtils.toast(R.string.toast_unsupport_link)
        }
        else{
            //如果出现未知异常且用户没有登陆，提示登陆！
            if (LoginTool.isLogout() or (throwable.message?.contains("Too many follow-up requests") == true)){
                throwable.message?.let {
                    MyUtils.log(it)
                }
                loginStateCurUrl = link
                liveDataShowLogin.postValue(true)
            }else{
                MyUtils.toast(R.string.com_occur_error)
                MyUtils.clearClipBoardContent(link)
                LoginTool.needSetCookieAgain()
                MyFireBaseUtils.logError(link)
                MyFireBaseUtils.recordException(throwable)
            }
        }

    }

    fun resetBlobData() {
        mRepository.liveDataSendBlobData.value = ""
    }

    fun loginSucWithDialog() {
        mRepository.livedataLoginSucWithDialog.postValue(true)
    }

   private fun getHomePageNameByUrl(url:String):String{
        var match = Pattern.compile("instagram.com/(.{3,33})(\\?)").matcher(url)
        if (match.find()){
            return match.group(1)
        }
        return ""
    }

    private fun getHomePageStoryUrl(url:String):String{
       val userName = getHomePageNameByUrl(url)
        if (userName.isNotEmpty()){
            return "https://www.instagram.com/stories/$userName/"
        }
        return ""
    }



}