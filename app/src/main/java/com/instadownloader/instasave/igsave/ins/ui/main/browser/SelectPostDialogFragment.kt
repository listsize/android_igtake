package com.instadownloader.instasave.igsave.ins.ui.main.browser

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.instadownloader.instasave.igsave.ins.R
import com.instadownloader.instasave.igsave.ins.databinding.FragmentSelectPostListBinding

/**
 * A fragment representing a list of Items.
 */
class SelectPostDialogFragment : BottomSheetDialogFragment() {

    var isDestoryed: Boolean = false
    lateinit var viewModel: SelectPostDialogViewModel
    private lateinit var myAdapter:SelectItemRecyclerViewAdapter

    lateinit var bingding:FragmentSelectPostListBinding



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val contextThemeWrapper = ContextThemeWrapper(activity, R.style.Theme_fragment)
        bingding = DataBindingUtil.inflate(inflater.cloneInContext(contextThemeWrapper),R.layout.fragment_select_post_list, container, false)
        isDestoryed = false
        return bingding.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        isDestoryed = true
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SelectPostDialogViewModel::class.java)
        bingding.viewModel = viewModel
        bingding.lifecycleOwner = viewLifecycleOwner

        myAdapter = SelectItemRecyclerViewAdapter(ArrayList())
        myAdapter.listener  = viewModel
        bingding.list.layoutManager = GridLayoutManager(context, 3)
        bingding.list.adapter = myAdapter
        if (cacheList.isNotEmpty()){
            viewModel.initData(cacheList)
        }
        cacheUserBean?.let {
            viewModel.userBean = it
        }
//        viewModel.isEndLoad.set(endRecevieData)  //目前存在bug。有时加载不了。 则需要弹出可以取消按钮
        viewModel.isEndLoad.set(true)
        viewModel.livedataClickDownload.observe(viewLifecycleOwner){
            if (it){
                dismiss()
                viewModel.livedataClickDownload.value = false
            }
        }
        viewModel.livedataClickClose.observe(viewLifecycleOwner){
            if (it){
                dismiss()
                viewModel.livedataClickClose.value = false
            }
        }

        viewModel.livedataMediasList.observe(viewLifecycleOwner) {list->
            myAdapter.setData(list)
        }



    }

    private var cacheList:ArrayList<BrowserPostMediaBean> = ArrayList()
    private var endRecevieData = false
    private var cacheUserBean:BrowserPostInfo ? = null
    fun addMedia(bean: BrowserPostMediaBean?) {
        if (::viewModel.isInitialized){
            viewModel.addMedia(bean)
        }else{
            bean?.let {
                cacheList.add(it)
            }
        }
    }

    fun setBrowserInfoBean(userBean: BrowserPostInfo?) {
        userBean?.let {
            if (::viewModel.isInitialized){
                viewModel.userBean = userBean
            }else{
                cacheUserBean = userBean
            }
        }
    }

    fun endReceiveData() {
        if (::viewModel.isInitialized){
            viewModel.isEndLoad.set(true)
        }else{
            endRecevieData = true
        }
        isCancelable = true
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.behavior.isDraggable = false
        isCancelable = false
        return dialog
    }
}