package com.instadownloader.instasave.igsave.ins

import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.instadownloader.instasave.igsave.ins.ui.main.data.PostBean
import java.io.*
import java.net.ConnectException
import java.net.ProtocolException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.*
import javax.net.ssl.SSLHandshakeException

object MyUtils {

    val ADS_DELAY_TIME :Long= 800
    val PKG_INSTAGRAM: String = "com.instagram.android"
    val PKG_GOOGLEPLAY: String = "com.android.vending"
    val FOLDER_NAME:String = "InstagramSave"

    //是否使用浏览器模式
    var isUseBrowserMode = true

    var vip = false

    var isNeedShowBrowserHelp = true

    fun isNetworkException(throwable:Throwable): Boolean {
        if (throwable is ConnectException ||
            throwable is SocketTimeoutException ||
            throwable is UnknownHostException ||
            throwable is SocketException || throwable is SSLHandshakeException
        ){
            return true
        }

        return false
    }

    fun getSavePath(): String {
        return MyApp.gContext.filesDir!!.absolutePath+ File.separator + FOLDER_NAME
    }

    fun deleteFile(path: String?) {
        var file = File(path)
        if(file.exists()){
            file.delete()
        }
    }

    var isHaveSucDownload = false
    fun isCanShowRate():Boolean{
        return KVModel.getBoolean(MyApp.gContext, "rate", true)
    }

    fun setCanShowRate(bool: Boolean){
        KVModel.putBoolean(MyApp.gContext, "rate", bool)
    }


    fun isCanOpenInstagram(context: Context):Boolean{
        var intent = context.packageManager.getLaunchIntentForPackage(PKG_INSTAGRAM)
        return intent!=null
    }

    fun openInstagramApp(context: Context): Boolean {
        try {
            var intent = context.packageManager.getLaunchIntentForPackage(PKG_INSTAGRAM)
            if (intent != null) {
                context.startActivity(intent)
            }else{
                return false
            }

        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }


    fun deleteAllLocalPosts() {
        var file = File(getSavePath())
        if (file.exists()) {
            if (file.isDirectory) {
                var files = file.listFiles()
                for (f in files) {
                    if (f.name.endsWith(".jpg") || f.name.endsWith(".mp4")) {
                        f.delete()
                    }
                }
            }
        }
    }
    fun deletePostFiles(array: List<String>) {
        for (url in array) {
            var path = getSavePath() + File.separator + getPostFileName(url)
            try {
                var file = File(path)
                if (file.exists()) {
                    file.delete()
                    notifyScanFile(path)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteMedias(bean: PostBean){
        deletePostUris(bean.getPostLocalUris())
    }

    fun deletePostUris(array: List<String>){
        for (url in array) {
            deleteUri(url)
        }
    }


    fun deleteUri(string: String) {
        try {
            val row: Int = MyApp.gContext.contentResolver.delete(
                Uri.parse(string),
                null,
                null
            )
        }catch (e:Exception){
            e.printStackTrace()
        }

    }


    fun getPostFileName(url: String): String {
        if (url.contains(".mp4") ) {
            return url.hashCode().toString() + ".mp4"
        } else {
            return url.hashCode().toString() + ".jpg"
        }
    }

    fun getReadableFileAbsPath(url: String):String{
        return MyUtils.getSavePath() + File.separator+ getPostFileName(url)
    }

    fun isThereThisApp(context: Context, pkg: String): Boolean {
        var pkg = pkg
        try {
            val packageInfo = context.packageManager.getPackageInfo(
                pkg,
                PackageManager.GET_META_DATA
            )
            if (null != packageInfo) {
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun openInGooglePlay(context: Context, link: String) {
        try {

            val uri = Uri.parse(link)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = uri
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.setPackage(PKG_GOOGLEPLAY)
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openAppInGooglePlay(context: Context, desApp: String) {
        try {
            var uristring = "market://details?id=$desApp"
            val uri = Uri.parse(uristring)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = uri
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.setPackage(PKG_GOOGLEPLAY)
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 在Instagram中查看
     */
    fun viewInInstagram(context: Context, url: String){
        try {
//            if(!isThereThisApp(context, PKG_INSTAGRAM)){
//                openAppInGooglePlay(context, PKG_INSTAGRAM)
//                return
//            }
            val uri = Uri.parse(url)
            val likeIng = Intent(Intent.ACTION_VIEW, uri)
            likeIng.setPackage(PKG_INSTAGRAM)
            context.startActivity(likeIng)

        } catch (e: Exception) {
            Toast.makeText(context, R.string.error, Toast.LENGTH_LONG).show()
        }

    }

    fun shareFileToInstagram(context: Context, path: String){
        shareFileToOtherApp(context, path, PKG_INSTAGRAM)
    }

    fun shareFileToOtherApp(context: Context, path: String, app: String?){
        try {
            var intent = Intent(Intent.ACTION_SEND)
            MyUtils.log("share ----> "+path)
            if(path.contains(".jpg")){
                intent.setType("image/*");
            }else{
                intent.setType("video/*");
            }
            val photoUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                File(path)
            )
            intent.putExtra("android.intent.extra.STREAM", photoUri);
            if(app != null){
                intent.setPackage(app)
            }
            context.startActivity(Intent.createChooser(intent, "Share"));
        }catch (exception: java.lang.Exception){
            exception.printStackTrace()
        }
    }

    fun shareFileToInstagram(context: Context, path: Uri){
        shareFileToOtherApp(context, path, PKG_INSTAGRAM)
    }

    fun shareFileToOtherApp(context: Context, path: Uri, app: String?){
        try {
            MyUtils.log("share ----> "+path)

            var intent = Intent(Intent.ACTION_SEND)

            if(path.toString().contains("image")){
                intent.setType("image/*");
            }else{
                intent.setType("video/*");
            }
            intent.putExtra( Intent.EXTRA_STREAM, path);
            if(app != null){
                intent.setPackage(app)
            }
            context.startActivity(Intent.createChooser(intent, "Share"));
        }catch (exception: java.lang.Exception){
            exception.printStackTrace()
        }
    }

    /**
     * TODO通知扫描媒体文件
     *
     */
    fun notifyScanFile(saveAs: String){
        var contentUri = Uri.fromFile(File(saveAs));
        var mediaScanIntent =  Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, contentUri);
        MyApp.gContext.sendBroadcast(mediaScanIntent);
    }

    fun setClipBoardContent(content: String) {
        val cm = MyApp.gContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val mClipData = ClipData.newPlainText("Label", content)
        cm?.setPrimaryClip(mClipData)
    }

    fun getClipBoardContent(): String {
        try {
            val cm = MyApp.gContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            var data = cm.primaryClip
            var item = data?.getItemAt(0)
            if(TextUtils.isEmpty(item?.text)){
                return ""
            }
            return item?.text.toString()
        }catch (e: Exception){
            return ""
        }
        return ""
    }

    fun sendFeedbackByEmail(context: Context){
        var uri = Uri.parse("mailto:" + context.getString(R.string.feedback_email))
        var intent = Intent(Intent.ACTION_SENDTO, uri);
        intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.ins_feedback_email_title));
        intent.putExtra(Intent.EXTRA_TEXT, "");
        context.startActivity(Intent.createChooser(intent, "Choice"))
    }


    fun shareText(context: Context, extraText: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.action_share))
        intent.putExtra(Intent.EXTRA_TEXT, extraText)//extraText为文本的内容
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK//为Activity新建一个任务栈
        context.startActivity(
            Intent.createChooser(intent, context.getString(R.string.action_share))
        )//R.string.action_share同样是标题
    }


    fun clearClipBoardContent(){
        try {
            var p = getClipBoardContent()
            if(isInstagramUrl(p)){
                setClipBoardContent("")
            }
        }catch (e: Exception){
        }
    }

    fun clearClipBoardContent(url:String){
        try {
            var p = getClipBoardContent()
            if(isInstagramUrl(p)){
                if (url == p.replace("/reel/","/p/")){
                    setClipBoardContent("")
                }
            }
        }catch (e: Exception){
        }
    }

    fun isInstagramUrl(url: String): Boolean {
        if (url.startsWith("https://www.instagram.com/") || url.startsWith("https://instagram.com/")
            ||url.startsWith("http://www.instagram.com/") || url.startsWith("http://instagram.com/")) {
            return true
        }
        return false
    }

    fun log(s: String) {
        Log.w("instake", s)
    }

    fun d(s: String) {
        Log.w("instake", s)
    }

    fun i(s: String) {
        Log.w("instake", s)
    }

    val handle = Handler(Looper.getMainLooper())
    fun toast(toast: String) {
        handle.post {
            Toast.makeText(MyApp.gContext, toast, Toast.LENGTH_SHORT).show()
        }
    }
    fun toast(toast: Int) {
        handle.post {
            Toast.makeText(MyApp.gContext, toast, Toast.LENGTH_SHORT).show()
        }
    }


    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    fun dip2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun px2dip(context: Context, pxValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    fun getDarkModeStatus(context: Context): Boolean {
        val mode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return mode == Configuration.UI_MODE_NIGHT_YES
    }

    fun insertMp4(path: String?):Uri {
        val displayName = path.hashCode().toString()+".mp4"
        val mimeType = "video/mp4"
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Video.VideoColumns.DISPLAY_NAME, displayName)
        contentValues.put(MediaStore.Video.VideoColumns.MIME_TYPE, mimeType)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            contentValues.put(
                MediaStore.Video.VideoColumns.RELATIVE_PATH,
                Environment.DIRECTORY_MOVIES + File.separator+FOLDER_NAME
            )
        }else{
            val dstPath: String = (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).path + File.separator + FOLDER_NAME)
            val tempfile = File(dstPath)
            if (!tempfile.exists()){
                tempfile.mkdirs()
            }
            contentValues.put(MediaStore.Video.VideoColumns.DATA, dstPath+ File.separator + displayName)
        }

        val resUri = MyApp.gContext.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
        Log.i("mpr4", "" + resUri)
        try {
            MyApp.gContext.contentResolver.openFileDescriptor(resUri!!, "w").use { descriptor ->
                descriptor?.let {
                    FileOutputStream(descriptor.fileDescriptor).use { out ->
                        val videoFile = File(path)
                        FileInputStream(videoFile).use { inputStream ->
                            val buf = ByteArray(8192)
                            while (true) {
                                val sz = inputStream.read(buf)
                                if (sz <= 0) break
                                out.write(buf, 0, sz)
                            }
                        }
                    }
                }
            }

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return resUri!!
    }

    fun insertMedia(path: String?):Uri {
        if (path?.contains(".mp4")!!){
            return insertMp4(path)
        }
        return insertJpg(path);
    }


    fun insertJpg(path: String?):Uri {
        val bitmap = BitmapFactory.decodeFile(path)
        val displayName = path.hashCode().toString()+".jpg"
        val mimeType = "image/jpeg"
        val contentValues = ContentValues()
        contentValues.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, displayName)
        contentValues.put(MediaStore.Images.ImageColumns.MIME_TYPE, mimeType)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            contentValues.put( MediaStore.Images.ImageColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + FOLDER_NAME
            )
        }else{
            val dstPath: String = (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path + File.separator + FOLDER_NAME)
            val tempfile = File(dstPath)
            if (!tempfile.exists()){
                tempfile.mkdirs()
            }
            contentValues.put(MediaStore.Images.ImageColumns.DATA, dstPath + File.separator + displayName)
        }


        var resUri = MyApp.gContext.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        MyUtils.log( "resuri "+ resUri.toString())
        try {
            val outputStream: OutputStream = MyApp.gContext.contentResolver!!.openOutputStream(resUri!!)!!
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return resUri!!
    }


    val LOCK = "saisjai"


    private var interstitialAd: InterstitialAd? = null
    var gotoUrl = ""

    var isShowedSplash = false

    var isFromTheAppOtherActivityBack = false

    private var isShowedInterAd =false
    fun isNeedShowInterAds():Boolean{
        return !isShowedInterAd && interstitialAd!=null && !vip
    }

    fun showInterAds(activity:Activity){

        if (vip){
            return
        }

        if (!isShowedInterAd){
            isFromTheAppOtherActivityBack  = true
            interstitialAd?.show(activity)
            isShowedInterAd = true
        }
    }

     fun initInterAds(activity: MainActivity){
         var adRequest = AdRequest.Builder().build()

         InterstitialAd.load(activity,"ca-app-pub-8166307674328646/2285044140", adRequest, object : InterstitialAdLoadCallback() {
             override fun onAdFailedToLoad(adError: LoadAdError) {
                 adError?.message?.let { Log.d("initInterAds","onAdFailedToLoad"+ adError.code) }
                 interstitialAd = null
             }

             override fun onAdLoaded(ad: InterstitialAd) {
                 Log.d("ads", "initInterAds Ad was loaded.")
                 interstitialAd = ad
                 interstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
                     override fun onAdDismissedFullScreenContent() {
                         activity.onAdDismissedFullScreenContent()
                     }



                     override fun onAdShowedFullScreenContent() {
                         Log.d("ads", " initInterAds Ad showed fullscreen content.")
                         interstitialAd = null
                     }
                 }
             }
         })
    }


    fun getEmbedApiUrl(url: String): String {
        var key = "/p/"
        if (url.contains("/reel/")){
            key = "/reel/"
        }else if (url.contains("/tv/")){
            key = "/tv/"
        }
        return "https://www.instagram.com/p/"+ url.split(key)[1].split("/")[0]+ "/embed/captioned/"
    }

    fun findJsonString(src: String, startChar: Char, endChar: Char): String {
        val stack = Stack<Char>()
        var hasData = false
        val result = StringBuffer()
        for (i in src.indices) {
            if (src[i] == startChar) {
                stack.push(src[i])
                hasData = true
            } else if (hasData && src[i] == endChar) {
                stack.pop()
                if (stack.empty()) {
                    result.append(src[i])
                    break
                }
            }
            if (hasData) {
                result.append(src[i])
            }
        }
        return result.toString()
    }
    fun dp2px(dpValue: Float): Int {
        val scale = MyApp.gContext.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }


}