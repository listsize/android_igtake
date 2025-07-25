package com.instadownloader.instasave.igsave.ins.ui.main

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.ads.AdSplashApplication
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.material.button.MaterialButton

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.instadownloader.instasave.igsave.ins.*
import com.instadownloader.instasave.igsave.ins.MyUtils.getClipBoardContent
import com.instadownloader.instasave.igsave.ins.MyUtils.isInstagramUrl
import com.instadownloader.instasave.igsave.ins.MyUtils.log
import com.instadownloader.instasave.igsave.ins.databinding.LoadFragmentBinding
import com.instadownloader.instasave.igsave.ins.ui.main.browser.LoginInstagram
import com.instadownloader.instasave.igsave.ins.ui.main.data.LoginTool
import com.instadownloader.instasave.igsave.ins.ui.main.selectdownload.ItemModel
import com.instadownloader.instasave.igsave.ins.ui.main.selectdownload.SelectDownloadDialogAdapter
import com.jakewharton.rxbinding4.view.clicks
import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.concurrent.TimeUnit

class LoadFragment : Fragment() {

    companion object {
        fun newInstance() = LoadFragment()
    }
    private lateinit var binding:LoadFragmentBinding

    private lateinit var loadViewModel: LoadViewModel

    private val handler = Handler()

    private var mCompositeDisposable = CompositeDisposable()

    private lateinit var btnDownload: Button
    private lateinit var btnPaste: Button
    private lateinit var editText: TextInputEditText
    private lateinit var cardview: View
    private lateinit var mRepository: MyRepository;


    private var isBlobData = false

    private val image by lazy {
        binding.image
    }

    private val profile_pic by lazy {
        binding.profilePic
    }

    private val content by lazy {
        binding.content
    }

    private val name by lazy {
        binding.name
    }

    private val video_lt by lazy {
        binding.videoLt
    }

    private val progressbar by lazy {
        binding.progressbar
    }

    private val progressbar_percent by lazy {
        binding.progressbarPercent
    }

    private val manypic_rt by lazy {
        binding.manypicRt
    }

    private fun readyLoad(){
        showLoading(true)
        Glide.with(this).clear(profile_pic)
        Glide.with(this).clear(image)
        editText!!.setTextColor(resources.getColor(R.color.black))
        cardview!!.visibility = View.INVISIBLE

        if (isBlobData){
            isBlobData = false
            return
        }
        (activity as MainActivity).toLoadTab()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LoadFragmentBinding.inflate(inflater)
        val root = binding.root
        btnDownload = root.findViewById(R.id.button_download)
        btnPaste = root.findViewById(R.id.button_paste)
        editText = root.findViewById(R.id.outlinedTextFieldEdit)
        cardview = root.findViewById(R.id.cardview)

        temple = root.findViewById(R.id.my_template)
        temple.visibility = View.GONE
        MainActivity.fragment = this
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        MainActivity.fragment = null

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        loadViewModel = ViewModelProvider(this).get(LoadViewModel::class.java)
        mRepository = (activity?.application as MyApp).appContainer.myRepository
        loadViewModel.tempActivity = activity


        loadViewModel.livedataAlreadyDownload.observe(viewLifecycleOwner) { bool ->
            if (bool) {
                loadViewModel.curSucBean?.let {
                    cardview.visibility = View.VISIBLE
                    Glide.with(this).load(Uri.parse(it.getPosturls()[0])).into(image);
                    Glide.with(this).load(it.profile_pic)
                        .apply(RequestOptions().centerCrop().transform(RoundedCorners(50)))
                        .into(profile_pic);
                    content.text = it.text
                    name.text = it.name
                    manypic_rt.visibility =
                        if (it.getPosturls().size > 0) View.VISIBLE else View.INVISIBLE
                }

            }
        }

        loadViewModel.liveDataSendBlobData.observe(viewLifecycleOwner){
            if (it.isNotEmpty()){
                isBlobData = true
                loadViewModel.requestLoadPost(originUrl =  it)
                loadViewModel.resetBlobData()
            }
        }

        loadViewModel.livedataFirstFile.observe(viewLifecycleOwner) {
            log("livedata first " + Uri.parse(it))
            Glide.with(this).load(Uri.parse(it)).into(image);
        }

        loadViewModel.liveDataMVideoIcon.observe(viewLifecycleOwner) {
            if (it) {
                video_lt.visibility = View.VISIBLE
            } else {
                video_lt.visibility = View.INVISIBLE
            }
        }

        loadViewModel.liveDataDownloadProgress.observe(viewLifecycleOwner) {
            if (it == 0) {
                progressbar.visibility = View.INVISIBLE;
                progressbar_percent.visibility = View.INVISIBLE
            } else {
                progressbar.visibility = View.VISIBLE;
                progressbar_percent.visibility = View.VISIBLE
                progressbar_percent.text = "$it%"
            }
            progressbar.progress = it
        }

        loadViewModel.liveDataDownloadState.observe(viewLifecycleOwner) {
            if (it) {
                (activity as MainActivity).showRedDot(true);
            } else {
                cardview!!.visibility = View.INVISIBLE
            }
        }

        loadViewModel.livedataDownloaded.observe(viewLifecycleOwner){
            if (it) {
                (activity as MainActivity).showRedDot(true);
            }
        }

        loadViewModel.liveDataShowLogin.observe(viewLifecycleOwner) {
            if (it) {
                showLoginDialog()
                loadViewModel.liveDataShowLogin.value = false
            }
        }

        loadViewModel.liveDataReadyLoad.observe(viewLifecycleOwner) {
            if (it) {
                readyLoad()
            }
        }

        loadViewModel.liveDataLoadSucAndDownload.observe(viewLifecycleOwner) {
            showLoading(false)
            cardview!!.visibility = View.VISIBLE
        }

        loadViewModel.liveDataLoadSucAndSelect.observe(viewLifecycleOwner){
            if (it){
                loadViewModel.liveDataLoadSucAndSelect.value = false
                showLoading(false)
                loadViewModel.curSelectBean?.let {
                    showSelectDialog(it.getPosturls())
                }
            }
        }

        loadViewModel.liveDataLoadDone.observe(viewLifecycleOwner) {
            showLoading(false)
        }
        loadViewModel.liveDataLoadError.observe(viewLifecycleOwner) {
            showLoading(false)
        }

        loadViewModel.liveDataEditTextColor.observe(viewLifecycleOwner) {
            editText!!.setTextColor(resources.getColor(it))

        }

        loadViewModel.liveDataContent.observe(viewLifecycleOwner, Observer {
            content.text = it
        })

        loadViewModel.liveDataProfilePic.observe(viewLifecycleOwner, Observer {
            Glide.with(this).load(it).apply(RequestOptions().centerCrop().transform(RoundedCorners(50))).into(profile_pic);
        })
        loadViewModel.liveDataManyPicIcon.observe(viewLifecycleOwner, Observer {
            if (it) {
                manypic_rt.visibility = View.VISIBLE
            } else {
                manypic_rt.visibility = View.INVISIBLE
            }
        })

        loadViewModel.liveDataShowPlsWaitDialog.observe(viewLifecycleOwner){
            if (it){
                showLoading(true,R.string.please_wait)
                handler.postDelayed({
                    showLoading(false)
                    loadViewModel.showInterAds(requireActivity())
                }, MyUtils.ADS_DELAY_TIME)
                loadViewModel.liveDataShowPlsWaitDialog.value = false
            }
        }

        loadViewModel.liveDataName.observe(viewLifecycleOwner, Observer {
            name.text = it
        })

        mCompositeDisposable.add(btnDownload!!.clicks()
            .throttleFirst(1,TimeUnit.SECONDS)
            .subscribe {
            val url = editText!!.text.toString()
            loadViewModel.requestLoadPost(url)
        })

        mCompositeDisposable.add(
            btnPaste!!.clicks()
            .throttleFirst(1,TimeUnit.SECONDS)
            .subscribe {
                val content =  getClipBoardContent()
                if (!TextUtils.isEmpty(content)){
                    editText!!.setText(content)
                    loadViewModel.requestLoadPost(content)

                }else{
                    MyUtils.toast(R.string.tip_clipboard_null)
                }

        })




        mCompositeDisposable.add(cardview!!.clicks().subscribe {
            loadViewModel.onClickItem()
        })

        loadAds()

    }


    //是否限制广告
    fun isLimitAds():Boolean{
        val lastClickTime =  KVModel.getInt(requireActivity().applicationContext,"clickTime",0)
        val curTime = System.currentTimeMillis() / 1000
        var time = 1*60*60
        if (curTime - lastClickTime < time){
            return true
        }
        return false
    }


    private fun loadAds(){
        val adLoader = AdLoader.Builder(requireContext(), "ca-app-pub-8166307674328646/4356765603")
            .forNativeAd {
                try {
                    var styles = NativeTemplateStyle.Builder().build();
                    temple.visibility = View.VISIBLE
                    temple.setStyles(styles);
                    temple.setNativeAd(it);
                    if (MyUtils.vip){
                        temple.visibility = View.GONE

                    }
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }

            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad( p0: LoadAdError) {
                    // Handle the failure by logging, altering the UI, and so on.
                }

                override fun onAdClicked() {
                    super.onAdClicked()
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    // Methods in the NativeAdOptions.Builder class can be
                    // used here to specify individual options settings.
                    .build())
            .build()
        adLoader.loadAd(AdRequest.Builder().build());
    }

    private var isEnterSplashAd = false

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this);

        activity?.let { act->
            val application = act.application as? AdSplashApplication

            if (MyUtils.vip){
                return
            }

            if (AppSplashActivity.isDisableSplashAdNextTime){
                AppSplashActivity.isDisableSplashAdNextTime = false
                return
            }

            if (MyUtils.isFromTheAppOtherActivityBack ){
                MyUtils.isFromTheAppOtherActivityBack =false
                return
            }

            if (!MyUtils.isShowedSplash  && application != null ) {
                isEnterSplashAd = true
                application.showAdIfAvailable(
                    act,
                    object : AdSplashApplication.OnShowAdCompleteListener {
                        override fun onShowAdComplete() {
                            check()
                        }

                        override fun onNotReady() {
                            isEnterSplashAd = false
                        }
                    })
                return
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (isEnterSplashAd){
            isEnterSplashAd = false
            return
        }

        check()


    }

    private fun check(){
        if (isCancelLogin) { //如果取消登陆了。这时不需要自动检测了。
            log("取消登陆了~")
            isCancelLogin = false;
            return
        }
        autoLoad()
    }

    private fun autoLoad(){
        if (!(activity as MainActivity).mHasPermission){
            log("没有存储权限。")
        }
        handler.postDelayed({
            try {
                var content = getClipBoardContent()
                log("content-> $content")
                var share =  (activity as MainActivity).shareUrl
                log("share $share")
                if (!TextUtils.isEmpty(share)){
                    content = share!!
                    (activity as MainActivity).shareUrl = "" //当使用完后销毁。

                }
                if (isInstagramUrl(content)){
                    editText!!.setText(content)
                    loadViewModel.requestLoadPost(originUrl =  content)
                }
            }catch (e:java.lang.Exception){
                e.printStackTrace();
            }
        },500)

    }

    private var isCancelLogin = false
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LoginTool.REQUEST_CODE_LOGIN ) {
            if (resultCode == Activity.RESULT_OK){
                (activity as MainActivity).invalidateOptionsMenu()
                loadViewModel.loginSucWithDialog()
            }else{
                isCancelLogin = true
            }
        }

        log("onActivityResult loadfragment")
    }


    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: String) { /* Do something */
        if (event == "autoload"){
            autoLoad()
            log("自动下载")
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        mCompositeDisposable.clear()
    }

    private var progressBar: ProgressDialog? = null

    private fun showLoading(bool: Boolean,tip:Int = R.string.tip_loading) {
        try {
            hideLoginDialog()
            if (bool) {
                dismissSelectDialog()
                if (progressBar == null) {
                    progressBar = ProgressDialog(activity)
                    progressBar!!.setMessage(getString(tip))
                    progressBar!!.setCanceledOnTouchOutside(false)
                    progressBar!!.setCancelable(false)
                    progressBar!!.show()
                }
            } else {
                if (progressBar != null) {
                    progressBar!!.dismiss()
                    progressBar = null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private var dialog:AlertDialog? = null
    private fun showLoginDialog() {
        hideLoginDialog()
        var title = getString(R.string.login)
//        title = if ("stories" in loadViewModel.loginStateCurUrl){
//            String.format(getString(R.string.thisisa), getString(R.string.stories) )
//        }else{
//            String.format(getString(R.string.thisisaprivate), getString(R.string.posts) )
//        }
        dialog =  MaterialAlertDialogBuilder(requireActivity()).setTitle(title)
            .setCancelable(false)
            .setMessage(getString(R.string.tip_need_login))
            .setNegativeButton(
                getString(android.R.string.cancel)
            ) { dialog, which -> dialog.dismiss() }.setPositiveButton(
                getString(android.R.string.ok)
            ) { dialog, which ->
                dialog.dismiss()
                val intent = Intent(activity, LoginInstagram::class.java)
                startActivityForResult(intent, LoginTool.REQUEST_CODE_LOGIN)
            }.create()
        dialog?.show()
    }



    private fun hideLoginDialog(){
        if (dialog != null){
            dialog?.dismiss()
            dialog = null
        }
    }



   private var selectDownloadDialog:AlertDialog ? = null
   @SuppressLint("MissingInflatedId")
   private fun showSelectDialog(urls:ArrayList<String>) {

       dismissSelectDialog()

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.selectdown_dialog_layout, null)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerView)
        val checkAll = dialogView.findViewById<CheckBox>(R.id.checkAll)
        val profile = dialogView.findViewById<ImageView>(R.id.profile_pic)
        val name = dialogView.findViewById<TextView>(R.id.name)
        val count = dialogView.findViewById<TextView>(R.id.count)

       val buttonDownload = dialogView.findViewById<MaterialButton>(R.id.button_download)
        checkAll.isChecked = true

       val itemArray = ArrayList<ItemModel>()
       for (url in urls){
           itemArray.add(ItemModel(url,true))
       }
       count.text = itemArray.size.toString()

        // 初始化RecyclerView
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 3) // 2列的gridlayout
        recyclerView.adapter = SelectDownloadDialogAdapter().apply {
            setSrcs(itemArray)
            onItemCheckedChanged = {
                count.text = getSelectedItems().size.toString()
            }
        } // 你的adapter



       loadViewModel.curSelectBean?.let {
           name.text = it.name
           Glide.with(requireContext()).load(it.profile_pic).apply(RequestOptions().centerCrop().transform( RoundedCorners(50))).into(profile)
       }

        // 设置全选功能
        checkAll.setOnCheckedChangeListener { _, isChecked ->
            (recyclerView.adapter as SelectDownloadDialogAdapter).selectAll(isChecked)
        }


       buttonDownload.setOnClickListener {
           val selectedItems = (recyclerView.adapter as SelectDownloadDialogAdapter).getSelectedItems()
           if (selectedItems.isEmpty()) {
               Toast.makeText(requireContext(), requireContext().getString(R.string.least_one_item), Toast.LENGTH_SHORT).show()
           } else {
               selectDownloadDialog?.dismiss()

               val finalArry = selectedItems.filter { it.isChecked }
               val urlArray = ArrayList<String>()
               for (item in finalArry){
                   urlArray.add(item.url)
               }
               loadViewModel.selectedItems(urlArray)
           }
       }

        // 创建对话框
       selectDownloadDialog =  AlertDialog.Builder(requireContext())
            .setView(dialogView).create()
       selectDownloadDialog?.show()
    }

    private fun dismissSelectDialog(){
        selectDownloadDialog?.let {
            selectDownloadDialog?.dismiss()
            selectDownloadDialog = null
        }
    }





    lateinit var temple: TemplateView

}