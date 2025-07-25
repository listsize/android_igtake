package com.instadownloader.instasave.igsave.ins

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.*
import android.widget.*
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.instadownloader.instasave.igsave.ins.ui.main.data.PostBean

open class ViewActivity : AppCompatActivity() {
    private val handle = Handler(Looper.getMainLooper())
    lateinit var mViewPager: ViewPager

    private var arrayList:ArrayList<String>? =  null

    var bean:PostBean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var bundle = intent.extras
        var url =  bundle?.getString("url")
        setContentView(R.layout.activity_view)
        title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mViewPager = findViewById(R.id.preview_pager)

        if (TextUtils.isEmpty(url)){
            finish()
            return
        }

        bean = (application as MyApp).appContainer.myRepository.getPostBeanByUrl(url!!)
        arrayList = bean?.getPostLocalUris()

        var list = ArrayList<View>()
        handle.post {
            if(arrayList != null){
                var index =0
                for (img in arrayList!!){
                    var isImage = false
                    var path:String  = img
                    if (path.contains("images/media")){
                        isImage = true
                    }


                    if(isImage){
                        run {
                            var imageView = ImageView(this)
                            Glide.with(this).load(Uri.parse(path)).into(imageView);
                            list.add(imageView)
                        }

                    }else{
                        var view = layoutInflater.inflate(R.layout.myvideolayout,null)
                        var videoView = view.findViewById<VideoView>(R.id.video_view)
                        initMediaControl(videoView)
                        videoView.setVideoURI(Uri.parse(path))
                        if(index == 0){
                            videoView.start()
                        }
                        list.add(view)
                    }
                    index++
                }
            }

            mViewPager.adapter = PreviewPagerAdapter(list)
            var curText = findViewById<TextView>(R.id.indicator)
            val cur = mViewPager.currentItem?.plus(1)
            curText.setText("$cur/${list.size}")
            if(list.size <2 ){
                curText.visibility = View.INVISIBLE
            }

            mViewPager?.addOnPageChangeListener(object :ViewPager.SimpleOnPageChangeListener(){
                override fun onPageSelected(position: Int) {
                    var showposition = position+1
                    curText.setText("$showposition/${list.size}")
                    var view= list.get(position)

                    if(view is RelativeLayout){
                        var video = view.findViewById<VideoView>(R.id.video_view)
                        video.start()
                    }

                    //暂停其他视频播放
                    for (v in list){
                        if(v != view){
                            if(v is RelativeLayout){
                                var video = v.findViewById<VideoView>(R.id.video_view)
                                video.pause()
                            }
                        }
                    }
                }
            })




        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.previewmenu, menu)

        bean?.let {
            if (!it.url.contains("https")){
                val item = menu.findItem(R.id.action_view_in_instagram)
                item.isVisible = false
            }
        }

        return true
    }

    private fun initMediaControl(view:VideoView) {
        val mediaController = MediaController(this)
        view.setMediaController(mediaController)
        mediaController.setMediaPlayer(view)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.action_repost -> {
                try {
                    MyUtils.shareFileToInstagram(this,Uri.parse(arrayList?.get(mViewPager?.currentItem!!)!!))
                }catch (e:Exception){
                }
                true
            }

            R.id.action_view_in_instagram -> {
                try {
                    MyUtils.viewInInstagram(this,bean!!.url)
                }catch (e:Exception){
                }
                true
            }

            R.id.action_share->{
                try {
                    MyUtils.shareFileToOtherApp(this,Uri.parse(arrayList?.get(mViewPager?.currentItem!!)!!),null)
                }catch (e:Exception){
                    e.printStackTrace()
                }
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    class PreviewPagerAdapter (viewList:ArrayList<View> ) : PagerAdapter() {
        var mViewList:ArrayList<View>? = null
        init {
            mViewList = viewList
        }

        override fun getCount(): Int {
            return mViewList?.size!!
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            container.addView(mViewList?.get(position),ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            return mViewList?.get(position)!!
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(mViewList?.get(position))
        }
    }

    override fun onStart() {
        super.onStart()
        MyUtils.isFromTheAppOtherActivityBack  = true
    }


}