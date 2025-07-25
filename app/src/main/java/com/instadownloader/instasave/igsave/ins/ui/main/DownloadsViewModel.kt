package com.instadownloader.instasave.igsave.ins.ui.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.instadownloader.instasave.igsave.ins.*
import com.instadownloader.instasave.igsave.ins.ui.main.data.IPostsDataSource
import com.instadownloader.instasave.igsave.ins.ui.main.data.PostBean
import java.util.*

class DownloadsViewModel : ViewModel() {

    private lateinit var mRepository: MyRepository;

    val livedataPostLoad = MutableLiveData<LinkedList<PostBean>>()
    val livedataDeleteDialog = MutableLiveData<Boolean>()

    var liveDataChange = MutableLiveData<LinkedList<PostBean>>()
    val liveDataShowSelectState = MutableLiveData<Boolean>()

    val liveDataSelectNums = MutableLiveData<Int>()

    val liveDataShowPlsWaitDialog  = MutableLiveData<Pair<PostBean,Boolean>>()

    private val STATE_IDLE = 0  //初始状态
    private val STATE_SELECTING = 1

    private var state = STATE_IDLE

    var deleteBean:PostBean? = null
    fun setRepository(rep:MyRepository){
        mRepository = rep
        liveDataChange = mRepository.liveDataChange
    }

    fun loadPosts() {
        mRepository.loadPosts(object :IPostsDataSource.LoadPostsCallback{
            override fun onLoadMediasLoaded(list: LinkedList<PostBean>) {
                livedataPostLoad.value = list
            }
            override fun onDataNotAvailable() {
            }
        })
    }

    fun onPopupItem(context:Context,id:Int,bean:PostBean){
        when(id){
            R.id.action_copy_url->{
                MyUtils.setClipBoardContent(bean.url)
                MyUtils.toast(android.R.string.copy)
            }
            R.id.action_copy_all->{
                MyUtils.setClipBoardContent(bean.text)
                MyUtils.toast(android.R.string.copy)
            }
            R.id.action_view_in_instagram->{
                MyUtils.viewInInstagram(context,bean.url)
            }
            R.id.action_repost->{

                val path = Uri.parse(bean.getPostLocalUris()[0])
                MyUtils.shareFileToInstagram(context,path)

            }
            R.id.action_share->{
                try {
                    val path = Uri.parse(bean.getPostLocalUris()[0])
                    MyUtils.shareFileToOtherApp(context,path,null)
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }
            R.id.action_delete ->{
                deleteBean = bean
                livedataDeleteDialog.postValue(true)
            }

        }
    }

    fun deleteBean(bean:PostBean?){
        bean?.let {
            mRepository.deletePost(it)
        }

    }

    fun showInterAds(bean:PostBean,activity: Activity) {
        MyUtils.showInterAds(activity)
        MyUtils.gotoUrl=bean.url
    }


    fun onListItem(context: Context, bean: PostBean, activity: Activity?) {
        try {

            activity?.let { inactivity->
                if (MyUtils.isNeedShowInterAds()){
                    liveDataShowPlsWaitDialog.postValue(Pair(bean,true))
                    return
                }
            }

            val  intent = Intent(context, ViewActivity::class.java)
            var bundle = Bundle()
            bundle.putString("url",bean.url)
            intent.putExtras(bundle)
            context.startActivity (intent)
        }
        catch (e:Exception){
            e.printStackTrace()
        }
    }

    fun clickEntrySelect() {
        state = STATE_SELECTING
        for (post in  mRepository.getCachePosts()) {
            post.itemState = post.ITEM_UNCHECKED
        }
        mRepository.liveDataChange.postValue( mRepository.getCachePosts())
    }

    fun undo() {
        state = STATE_IDLE

        for (post in  mRepository.getCachePosts()) {
            post.itemState = post.ITEM_IDEL
        }
        mRepository.liveDataChange.postValue( mRepository.getCachePosts())
    }

    fun selectAll() {
        var isAllChecked = true
        for (item in mRepository.getCachePosts()){
            if(item.itemState != item.ITEM_CHECKED){
                isAllChecked = false
                break
            }
        }
        for (item in mRepository.getCachePosts()){
            if (isAllChecked){
                item.itemState = item.ITEM_UNCHECKED
            }else{
                item.itemState = item.ITEM_CHECKED

            }
        }
        mRepository.liveDataChange.postValue( mRepository.getCachePosts())
    }

    fun onCreateOptionsMenu() {
        when(state){
            STATE_IDLE->{
                liveDataShowSelectState.value = false
            }
            STATE_SELECTING->{
                liveDataShowSelectState.value = true
            }
        }
    }

    fun itemCheckChange(item: PostBean) {
        var count = 0
        for (item in mRepository.getCachePosts()){
            if(item.itemState == item.ITEM_CHECKED){
                count++
            }
        }
        liveDataSelectNums.value = count
    }

    fun deleteSelectedBean() {
        liveDataSelectNums.value = 0
        mRepository.deleteCheckedPosts()
    }

    fun getSelectState(): Int {
        return state
    }

    fun downloadsIsEmpty(): Boolean {
        return mRepository.getCachePosts().size ==0
    }
}