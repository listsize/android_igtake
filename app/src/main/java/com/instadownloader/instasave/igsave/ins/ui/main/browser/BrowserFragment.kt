package com.instadownloader.instasave.igsave.ins.ui.main.browser

import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.appcompat.view.ContextThemeWrapper
import com.google.gson.Gson
import com.instadownloader.instasave.igsave.ins.MainActivity
import com.instadownloader.instasave.igsave.ins.MyFireBaseUtils
import com.instadownloader.instasave.igsave.ins.R
import com.instadownloader.instasave.igsave.ins.safelyFromJson
import com.instadownloader.instasave.igsave.ins.ui.main.data.JsData
import com.instadownloader.instasave.igsave.ins.ui.main.data.LoginTool


class BrowserFragment : Fragment() {

    companion object {
        fun newInstance() = BrowserFragment()
        val HOME_HRL = "https://www.instagram.com/accounts/login/"
        val LOCK= "LOCK"
        val JS_RELOAD = "javascript:window.location.reload( true )"
    }
    private lateinit var mWebview: WebView

    private lateinit var btn_refresh: ImageButton
    private lateinit var progressBar: ProgressBar

    private lateinit var viewModel: BrowserViewModel

    val handle = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val contextThemeWrapper = ContextThemeWrapper(activity, R.style.Theme_fragment)
        var view = inflater.cloneInContext(contextThemeWrapper).inflate(R.layout.fragment_browser, container, false)

        mWebview = view.findViewById(R.id.webview)
        progressBar = view.findViewById(R.id.webview_progressbar)

        btn_refresh = view.findViewById(R.id.btn_refresh)

        initWebview()

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProvider(this)[BrowserViewModel::class.java]

        btn_refresh.setOnClickListener {
            mWebview.loadUrl( JS_RELOAD)
        }

        viewModel.liveDataToBrowserWithLink.observe(viewLifecycleOwner){
            if (it.isNotEmpty()){
                (activity as MainActivity).toBrowserTab()
                if (it == "help"){
                    showHelp()
                }else{
                    mWebview.loadUrl(it)
                }
                viewModel.resetLivedataBrowserWithLink()
            }
        }

        viewModel.livedataLoginSucWithDialog.observe(viewLifecycleOwner){
            if (it){
                mWebview.loadUrl( JS_RELOAD)
                viewModel.resetLivedataLoginSuc()
            }
        }
        mWebview.loadUrl(HOME_HRL)
    }



    private fun initWebview(){
        mWebview.settings.allowFileAccessFromFileURLs = true
        mWebview.settings.allowFileAccess = true
        mWebview.settings.allowContentAccess = true;
        mWebview.settings.allowUniversalAccessFromFileURLs = true
        mWebview.getSettings().setPluginState(WebSettings.PluginState.ON);
        mWebview.settings.javaScriptEnabled = true
        mWebview.settings.userAgentString = "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Mobile Safari/537.36"
        mWebview.settings.databaseEnabled = true
        mWebview.settings.domStorageEnabled = true;
        mWebview.settings.loadWithOverviewMode = true
        mWebview.settings.useWideViewPort= false
        mWebview.addJavascriptInterface(this, "ADAPTATION_HOLDER")

        mWebview.webChromeClient = object : WebChromeClient(){
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.progress = newProgress
                if(newProgress == 100) {
                    progressBar.visibility = ProgressBar.GONE
                }else{
                    progressBar.visibility = ProgressBar.VISIBLE
                }
                super.onProgressChanged(view, newProgress)
            }

        }

        mWebview.webViewClient = object : WebViewClient(){

            override fun onPageFinished(webview: WebView?, url: String?) {
                super.onPageFinished(webview, url)
                progressBar.visibility = ProgressBar.GONE
                var isLoginOut = LoginTool.isLogout()
                Log.e("ads","isLoginOut " + isLoginOut)
                LoginTool.setCookieNew(url)
                try {
                    if (!LoginTool.isLogout()){
                        //如果刚刚没有登陆。现在登陆了。 说明第一次登陆。则刷新页面
                        if (isLoginOut){
                            Log.e("ads","跳过保存页面")
                            handle.post {
                                try {
                                    webview?.loadUrl(HOME_HRL)
                                }catch (e:Exception){
                                    e.printStackTrace()
                                    MyFireBaseUtils.recordException(e)
                                }
                            }
                            handle.post {
                                try {
                                    (activity as MainActivity).invalidateOptionsMenu()
                                }catch (e:Exception){
                                    e.printStackTrace()
                                }
                            }
                        }
                    }


                    if(url!!.contains("instagram.com")) {
                        Log.e("onPageFinished", url!!)
                        webview?.evaluateJavascript("javascript:" +"(function() {ADAPTATION_HOLDER.onDumpWebContent(document.body.innerHTML);})();",null)
//                            webview?.evaluateJavascript("javascript:" + JsData.decryptJS1(),null)
                        webview?.evaluateJavascript("javascript:" + JsData.decryptJS2(),null)
                    }
                }catch (e:Exception){
                    e.printStackTrace()
                }


            }

            override fun onLoadResource(view: WebView?, url: String?) {
                super.onLoadResource(view, url)
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                Log.i("ads", "shouldOverrideUrlLoading $url")
                view?.loadUrl(url!!)
                return true
            }
        }
    }


    @JavascriptInterface
    fun postParseEnd(){
        Log.e("ads","postParseEnd")
    }

    @JavascriptInterface
    fun userChangePage(str:String){
        Log.e("ads", "userChangePage$str")
    }

    @JavascriptInterface
    fun loadLocalJsFile(){
        Log.e("ads", "loadLocalJsFile")
    }

    @JavascriptInterface
    fun checkClipboard(){
        Log.e("ads", "checkClipboard")
    }

    @JavascriptInterface
    fun warn(tag:String,info:String){
        Log.e("ads", info)
    }

    @JavascriptInterface
    fun receiveJsParseResult(info:String){
        Log.e("ads","receiveJsParseResult :"+ info)
    }

    //当回调这个接口时候，说明是没有声音的reels. senddatajson用不了。 需要拿到str2 去用老办法解析下载
    @JavascriptInterface
    fun sendBlobData(displayurl: String,posturl: String){
        Log.e("ads","sendBlobData!! :"+displayurl +"   " + posturl)
        var bean = BrowserPostMediaBean()
        bean.displayUrl = displayurl
        bean.origin_post = posturl
        selectDialog?.let {
            it.addMedia(bean)
        }
    }

    @JavascriptInterface
    fun sendDataJson(str: String){
        Log.e("ads","sendDataJson :"+str)
        handle.post {
            selectDialog?.let {
                var bean = Gson().safelyFromJson(str,BrowserPostMediaBean::class.java)
                if(bean?.displayUrl?.isEmpty() == true){

                }
                it.addMedia(bean)
            }
        }
    }

    @JavascriptInterface
    fun reportError(str: String){
        Log.e("ads","reportError :"+str)
    }

    @JavascriptInterface
    fun endReceiveData(){
        Log.e("ads","endReceiveData------------------------------------")
        selectDialog?.let {
            it.endReceiveData()
        }
    }

    @JavascriptInterface
    fun startReceiveData(json: String,num:Int,isStory:Boolean){
        var userBean = Gson().safelyFromJson(json,BrowserPostDownloadRes::class.java)
            ?.let { BrowserPostInfo(it,num,isStory) }

        if (userBean != null) {
            Log.e("ads", "startReceiveData profile  ${userBean.infoRes.headUrl}  量：${userBean.nums}  story:${userBean.isStory}")

            Log.e("ads", "startReceiveData  ${userBean.infoRes.describe}  量：${userBean.nums}  story:${userBean.isStory}")
        }

        showSelectDialog()

        selectDialog?.let {
            it.setBrowserInfoBean(userBean)
        }

        handle.post {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mWebview.evaluateJavascript("javascript:startCollectData()",null)
            }else{
                mWebview.loadUrl("javascript:startCollectData()")
            }
        }
    }

    private var selectDialog: SelectPostDialogFragment? = null

    private fun showSelectDialog(){
        synchronized(LOCK){
            if (selectDialog == null || selectDialog?.isDestoryed == true){
                selectDialog = SelectPostDialogFragment()
                selectDialog?.show(parentFragmentManager,"dialog")
            }
        }
    }


    private var helpDialog : BrowserHelpFragment? = null
    private fun showHelp(){
        synchronized(LOCK){
            if (helpDialog == null ){
                helpDialog = BrowserHelpFragment()
                helpDialog?.show(parentFragmentManager,"help")
            }
        }
    }
}