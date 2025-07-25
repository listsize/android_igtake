package com.instadownloader.instasave.igsave.ins

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.Keyframe
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager.widget.ViewPager
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.aos.module.billing.BaseBillingActivity
import com.bumptech.glide.Glide
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.instadownloader.instasave.igsave.ins.ui.main.LoadFragment
import com.instadownloader.instasave.igsave.ins.ui.main.SectionsPagerAdapter
import com.instadownloader.instasave.igsave.ins.ui.main.browser.BrowserFragment
import com.instadownloader.instasave.igsave.ins.ui.main.data.LoginTool
import com.instadownloader.instasave.igsave.ins.ui.main.data.ads.AdsBean
import org.greenrobot.eventbus.EventBus
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Method


class MainActivity : BaseBillingActivity(), ViewPager.OnPageChangeListener,
    NavigationView.OnNavigationItemSelectedListener,
    DrawerLayout.DrawerListener {
    companion object {
        private var ads: List<AdsBean> = ArrayList()
        var fragment: LoadFragment? = null
    }

    lateinit var tabDownloads: TabLayout.Tab
    lateinit var tabLoad: TabLayout.Tab
    lateinit var tabBrowser: TabLayout.Tab

    var badge: BadgeDrawable? = null
    private lateinit var viewPager: CustomViewPager
    private val handle = Handler(Looper.getMainLooper())
    private lateinit var adView: RelativeLayout
    private lateinit var adImage: ImageView
    private lateinit var adName: TextView
    private lateinit var adText: TextView
    private lateinit var privacy: TextView


    var selectState = 0
    private lateinit var navView: NavigationView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        // Creates a button that mimics a crash when pressed
        val crashButton = Button(this)
        crashButton.text = "Test Crash"
        crashButton.setOnClickListener {
            throw RuntimeException("Test Crash") // Force a crash
        }

        addContentView(crashButton, ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT))

        val mode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (mode != Configuration.UI_MODE_NIGHT_YES) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        val testDeviceIds = listOf("9C73DEC494D25DD03F60FC448A339FF8")
        val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
        MobileAds.setRequestConfiguration(configuration)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)


        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        adView = navView.findViewById(R.id.banner_ad)
        adView.visibility = View.INVISIBLE
        adName = navView.findViewById(R.id.ad_name)
        adText = navView.findViewById(R.id.ad_text)
        adImage = navView.findViewById(R.id.ad_image)
        privacy = navView.findViewById(R.id.privacy)
        privacy.text = Html.fromHtml("<u>" + getString(R.string.privacy) + "</u>")
        adView.setOnClickListener {
            onClickGift()
        }
        privacy.setOnClickListener {
            closeDrawer()
            val intent = Intent(MyApp.gContext, PrivacyActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            MyApp.gContext.startActivity(intent)
        }

        navView.setNavigationItemSelectedListener(this)
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        drawerLayout.addDrawerListener(this)
        toggle.syncState()


        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        viewPager = findViewById(R.id.view_pager)
        viewPager.mIsCanScroll = !MyUtils.isUseBrowserMode
        viewPager.offscreenPageLimit = 2
        viewPager.adapter = sectionsPagerAdapter
        viewPager.setOnPageChangeListener(this)
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
        tabs.setOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {

                if (tab == tabDownloads) {
                    badge?.isVisible = false
                    entryDownloadsTab()
                } else if (tab == tabLoad) {
                    leaveDownloadsTab()
                } else if (tab == tabBrowser) {
                    showBrowserHelp()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
        if (MyUtils.isUseBrowserMode) {
            tabLoad = tabs.getTabAt(0)!!
            tabBrowser = tabs.getTabAt(1)!!

            tabDownloads = tabs.getTabAt(2)!!
        } else {
            tabLoad = tabs.getTabAt(0)!!
            tabDownloads = tabs.getTabAt(1)!!
        }

        badge = tabDownloads.orCreateBadge
        badge?.isVisible = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mHasPermission = true
        }

        if (!mHasPermission) {
            checkAndRequestPermission(REQ_CODE)
        }
        getShareUrl(intent)
        MyUtils.log("Main oncreate &$shareUrl")

        requestRecommand()
        MyUtils.initInterAds(this)
        showRecommand()
        mAdAnimA = setAnim(adImage, 1f, 2000, 2000);
        mAdAnimA?.let { mObjAnimatorList.add(it) }

        requestMonthlySubPrice(SUBS_MONTHY, BillingClient.ProductType.SUBS)
    }


    private fun showBrowserHelp() {
        if (!MyUtils.isNeedShowBrowserHelp) {
            return
        }
        Log.e("ads", "showBrowserHelp")
        if (KVModel.getBoolean(this, "isneedshowbrowserhelp", true)) {
            (application as MyApp).appContainer.myRepository.liveDataToBrowserWithString.postValue("help")
            KVModel.putBoolean(this, "isneedshowbrowserhelp", false)
        }
        MyUtils.isNeedShowBrowserHelp = false

    }


    private fun showRecommand() {
        handle.post {
            if (ads.isNullOrEmpty()) {
                return@post
            }
            adView.visibility = View.VISIBLE
            adName.text = ads[0].name
            adText.text = ads[0].text
            val image = ads[0].imgurl
            if (MyUtils.vip) {
                adView.visibility = View.GONE
            }
            Log.e("instake", ads[0].imgurl)
            try {
//                giftMenu?.isVisible = ads.isNotEmpty()
                Glide.with(this@MainActivity).load(image).into(adImage);
                start(objectAnimator = mObjAnimatorList[0])
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }


    private var mAdAnimA: ObjectAnimator? = null
    private val mObjAnimatorList: ArrayList<ObjectAnimator> = ArrayList()
    private fun setAnim(
        view: View,
        shakeFactor: Float,
        startDelay: Int,
        duration: Int
    ): ObjectAnimator? {
        val pvhRotate: PropertyValuesHolder = PropertyValuesHolder.ofKeyframe(
            View.ROTATION,
            Keyframe.ofFloat(0f, 0f),
            Keyframe.ofFloat(.1f, -3f * shakeFactor),
            Keyframe.ofFloat(.2f, -3f * shakeFactor),
            Keyframe.ofFloat(.3f, 3f * shakeFactor),
            Keyframe.ofFloat(.4f, -3f * shakeFactor),
            Keyframe.ofFloat(.5f, 3f * shakeFactor),
            Keyframe.ofFloat(.6f, -3f * shakeFactor),
            Keyframe.ofFloat(.7f, 3f * shakeFactor),
            Keyframe.ofFloat(.8f, -3f * shakeFactor),
            Keyframe.ofFloat(.9f, 3f * shakeFactor),
            Keyframe.ofFloat(1f, 0F)
        )
        val objectAnimator: ObjectAnimator = ObjectAnimator.ofPropertyValuesHolder(view, pvhRotate)
        objectAnimator.setDuration(duration.toLong())
        objectAnimator.setStartDelay(startDelay.toLong())
        return objectAnimator
    }

    private fun start(objectAnimator: ObjectAnimator) {
        val animatorSet = AnimatorSet()
        animatorSet.play(objectAnimator)
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                start(mObjAnimatorList[0])
            }
        })
        animatorSet.start()
    }


    private fun requestRecommand() {
        if (ads.isNotEmpty()) {
            return
        }
        val retrofit = Retrofit.Builder()
            .baseUrl("https://raw.githubusercontent.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        Thread {
            try {
                val service: AdsWebservice = retrofit.create(AdsWebservice::class.java)
                val call =
                    service.listAds("izhifei", packageName.replace(".debug", "").replace(".", ""))
                var ret = call.execute()
                if (ret.isSuccessful) {
                    ret.body()?.let {
                        ads = it
                        showRecommand()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()

    }

    override fun onBackPressed() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            myBackPressed()
        }
    }

    private fun closeDrawer() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    private fun myBackPressed() {

        if (MyUtils.isHaveSucDownload && MyUtils.isCanShowRate()) {
            showRateDialog()
        } else {
            showBottomDialog()
        }

    }

    private fun entryDownloadsTab() {
        badge?.isVisible = false
    }

    private fun leaveDownloadsTab() {
    }


    fun showRedDot(bool: Boolean) {
        Log.e("Ads", "showRedDot " + bool)
        badge?.isVisible = true

    }


    fun toLoadTab() {
        if (viewPager.currentItem != 0) {
            viewPager.setCurrentItem(0, true)
        }
    }

    fun toBrowserTab() {
        if (viewPager.currentItem != 1) {
            viewPager.setCurrentItem(1, true)
        }
    }

    private fun getShareUrl(intent: Intent?) {
        if (intent != null) {
            shareUrl = intent.getStringExtra("url")
        }
    }

    var shareUrl: String? = null
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        getShareUrl(intent)
    }

    override fun onMenuOpened(featureId: Int, menu: Menu): Boolean {
        if (menu.javaClass.simpleName.equals("MenuBuilder", ignoreCase = true)) {
            try {
                val method: Method = menu.javaClass.getDeclaredMethod(
                    "setOptionalIconsVisible",
                    java.lang.Boolean.TYPE
                )
                method.isAccessible = true
                method.invoke(menu, true)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return super.onMenuOpened(featureId, menu)
    }


    private var tipDialog: AlertDialog? = null

    private fun showTipDialog(text: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(text)
        builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
            // 在确定按钮点击事件中，可以执行你想要的操作
            dialog.dismiss() // 关闭对话框
        }
        tipDialog = builder.create()
        tipDialog?.show()
    }


    override fun onPause() {
        super.onPause()
        tipDialog?.takeIf { it.isShowing }?.dismiss()

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_instagram -> {
                var isSuc = MyUtils.openInstagramApp(this)
                if (!isSuc) {
                    showTipDialog(R.string.no_install_instagram)
                }
                true
            }


            R.id.action_help -> {
                val intent = Intent(MyApp.gContext, HelpActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                MyApp.gContext.startActivity(intent)
                true
            }

            R.id.action_remove_ads->{
                if (MyUtils.vip) {
                    MyUtils.toast(R.string.vip_tip)
                } else {
                    showSubTipDialog()
                }
                 true
            }

            R.id.action_vip -> {
                MyUtils.toast(R.string.vip_tip)
                 true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onClickGift() {
        try {
            if (ads.isNotEmpty()) {
                Log.e("instake", ads[0].toString())
                MyUtils.openInGooglePlay(applicationContext, ads[0].link)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun backpressed() {
//        moveTaskToBack(true)
        super.onBackPressed()
    }

    private var vipFlag: MenuItem? = null
    private var removeAdsMenu: MenuItem? = null

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)

        if (LoginTool.getLoginCookie().isNotEmpty()) {
            val item = navView.menu.findItem(R.id.nav_logout)
            item?.isVisible = true
        }

        vipFlag = menu.findItem(R.id.action_vip)
        removeAdsMenu = menu.findItem(R.id.action_remove_ads)

        vipFlag?.isVisible = MyUtils.vip

        removeAdsMenu?.isVisible = !MyUtils.vip


        return true
    }

    var mHasPermission = false
    val REQ_CODE = 1001
    private fun checkAndRequestPermission(code: Int) {
        val lackedPermission = java.util.ArrayList<String>()
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            lackedPermission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            lackedPermission.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        // 权限都已经有了，那么直接调用SDK
        if (lackedPermission.size == 0) {
            mHasPermission = true
        } else {
            // 请求所缺少的权限，在onRequestPermissionsResult中再看是否获得权限，如果获得权限就可以调用SDK，否则不要调用SDK。
            ActivityCompat.requestPermissions(this, lackedPermission.toTypedArray(), code)
        }
    }

    private fun hasAllPermissionsGranted(grantResults: IntArray): Boolean {
        for (grantResult in grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false
            }
        }
        return true
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_CODE && hasAllPermissionsGranted(grantResults)) {
            //TODO:当获取了权限
            mHasPermission = true
            EventBus.getDefault().post("autoload")
        } else {
            finish()
        }
    }

    var bottomDialog: Dialog? = null
    private fun showBottomDialog() {

        if (bottomDialog != null) {
            if (bottomDialog!!.isShowing) {
                bottomDialog?.dismiss()
            }
            bottomDialog = null;
        }

        if (isFinishing) {
            return
        }

        try {
            bottomDialog = Dialog(this, R.style.Bottom)
            val contentView = LayoutInflater.from(this).inflate(R.layout.dialog_exit, null)
            var tv_quit = contentView.findViewById<TextView>(R.id.tv_quit)
            var adContainer = contentView.findViewById<ViewGroup>(R.id.ly_card_ad)
            bottomDialog?.setContentView(contentView)
            val layoutParams = contentView.layoutParams
            layoutParams.width = resources.displayMetrics.widthPixels
            contentView.layoutParams = layoutParams
            bottomDialog?.window?.setGravity(Gravity.BOTTOM)
            bottomDialog?.window?.setWindowAnimations(R.style.Bottom_Animation)
            bottomDialog?.show()

            bottomDialog?.setOnKeyListener(object : DialogInterface.OnKeyListener {
                override fun onKey(
                    dialog: DialogInterface?,
                    keyCode: Int,
                    event: KeyEvent?
                ): Boolean {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {

                        try {
                            bottomDialog?.dismiss()
                            backpressed()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        return true
                    }
                    return false
                }

            })
            bottomDialog?.setOnDismissListener {
                adContainer.removeAllViews()
            }

            tv_quit.setOnClickListener {
                bottomDialog?.dismiss()
                backpressed()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun showRateDialog() {

        MaterialAlertDialogBuilder(this)
            .setTitle(resources.getString(R.string.rateus))
            .setCancelable(false)
            .setMessage("⭐ ⭐ ⭐ ⭐ ⭐")
            .setNeutralButton(resources.getString(android.R.string.cancel)) { dialog, which ->
                MyUtils.setCanShowRate(false)
            }
            .setPositiveButton(resources.getString(android.R.string.ok)) { dialog, which ->
                MyUtils.setCanShowRate(false)
                MyUtils.openAppInGooglePlay(MyApp.gContext, packageName)
            }
            .show()
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        if (position == 1) {
            title = ""
//            (viewPager.adapter as SectionsPagerAdapter).getItem(1).setHasOptionsMenu(true)
        } else {
            setTitle(R.string.app_name)
        }
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    fun onAdDismissedFullScreenContent() {


    }

    override fun onResume() {
        super.onResume()
        handle.postDelayed({
            gotoDetailWhenAdBack()
        }, 100)

    }


    private fun gotoDetailWhenAdBack() {
        if (!TextUtils.isEmpty(MyUtils.gotoUrl)) {
            var intent = Intent(this, ViewActivity::class.java)
            var bundle = Bundle()
            bundle.putString("url", MyUtils.gotoUrl)
            intent.putExtras(bundle)
            MyUtils.gotoUrl = ""
            startActivity(intent)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.nav_removeads -> {
                if (MyUtils.vip) {
                    MyUtils.toast(R.string.vip_tip)
                } else {
                    showSubTipDialog()
                }
            }

            R.id.nav_help -> {
                val intent = Intent(MyApp.gContext, HelpActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                MyApp.gContext.startActivity(intent)
                return false
            }

            R.id.nav_share -> {
                MyUtils.shareText(
                    this,
                    "https://play.google.com/store/apps/details?id=$packageName"
                )
                return false
            }

            R.id.nav_feed_back -> {
                MyUtils.sendFeedbackByEmail(this)
                return false
            }

            R.id.nav_logout -> {
                LoginTool.logout()
                item.isVisible = false
                (application as MyApp).appContainer.myRepository.liveDataToBrowserWithString.postValue(
                    BrowserFragment.HOME_HRL
                )
                MyUtils.toast(MyApp.gContext.getString(android.R.string.ok))

            }
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return false
    }

    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
    }

    override fun onDrawerOpened(drawerView: View) {
    }

    override fun onDrawerClosed(drawerView: View) {
    }

    override fun onDrawerStateChanged(newState: Int) {
    }

    override fun onPurchaseSuc(item: Purchase) {
//        MyUtils.toast("订阅成功")
        removeAds()
    }

    private fun removeAds() {
        KVModel.putBoolean(applicationContext, KVModel.VIP, true)
        try {
            fragment?.temple?.visibility = View.GONE
            MyUtils.vip = true
            adView.visibility = View.GONE
            vipFlag?.isVisible = true
            removeAdsMenu?.isVisible = false

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPurchaseFail() {
        MyUtils.toast("购买失败")
    }

    override fun onCancelPurchase() {
        if (!MyUtils.vip) {
            return
        }
        handle.post {
            MyUtils.vip = false
            KVModel.putBoolean(applicationContext, KVModel.VIP, false)
            vipFlag?.isVisible = MyUtils.vip
            removeAdsMenu?.isVisible = !MyUtils.vip
        }

    }


    override fun onResumePurchase(item: Purchase) {
        when (item.products[0]) {
            SUBS_MONTHY -> {
//                MyUtils.toast("恢复了包月"+ item.isAutoRenewing)
                MyUtils.vip = true
                handle.post {
                    removeAds()
                }
            }
        }
    }


}