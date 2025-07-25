package com.instadownloader.instasave.igsave.ins.ui.main

import android.app.ProgressDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.instadownloader.instasave.igsave.ins.ui.main.data.PostBean
import java.util.*
import androidx.lifecycle.Observer
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.instadownloader.instasave.igsave.ins.*
import com.instadownloader.instasave.igsave.ins.databinding.DownloadsFragmentBinding

/**
 * A fragment representing a list of Items.
 */
class DownloadsFragment : Fragment() {

    companion object {
        fun newInstance() = DownloadsFragment()
    }
    private lateinit var viewModel: DownloadsViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter:DownloadsRecyclerViewAdapter
    private lateinit var help_layout:View

    private lateinit var undo:MenuItem
    private lateinit var entry_select:MenuItem
    private lateinit var select_or_no:MenuItem
    private lateinit var action_delete:MenuItem

    private lateinit var binding:DownloadsFragmentBinding

    private val help_cardview by lazy {
        binding.helpCardview
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        MyUtils.log("setHasOptionsMenu")

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.downloads_menu, menu)
        undo = menu.findItem(R.id.undo)
        entry_select = menu.findItem(R.id.entry_select)
        select_or_no = menu.findItem(R.id.select_or_no)
        action_delete = menu.findItem(R.id.action_delete)

        viewModel.onCreateOptionsMenu()
        super.onCreateOptionsMenu(menu, inflater)

    }

    private fun showSelectState(bool:Boolean){
        if (!this::entry_select.isInitialized){
            return
        }
        entry_select.isVisible = !bool
        undo.isVisible = bool
        select_or_no.isVisible = bool
        action_delete.isVisible = bool
        if (bool){
            viewModel.liveDataSelectNums.value?.let {
                activity?.title = "☑ : $it"
            }?: run {
                activity?.title = "☑ : 0"
            }

        }else{
            activity?.title = ""
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.entry_select->{
                viewModel.clickEntrySelect()
                showSelectState(true)
            }
            R.id.undo->{
                showSelectState(false)
                viewModel.undo()
            }
            R.id.select_or_no->{
                viewModel.selectAll()
            }
            R.id.action_delete->{
                showDeleteSelectedDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = DownloadsFragmentBinding.inflate(inflater)
        val view = binding.root
        recyclerView = view.findViewById(R.id.list)
        help_layout = view.findViewById(R.id.empty_data)
        return view
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(DownloadsViewModel::class.java)
        viewModel.setRepository((activity?.application as MyApp).appContainer.myRepository)

        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter= DownloadsRecyclerViewAdapter(requireContext(),LinkedList<PostBean>(),object :DownloadsRecyclerViewAdapter.IItemLisenter{
            override fun onClickPopupItem(id: Int, bean: PostBean) {
                viewModel.onPopupItem(context!!,id,bean)
            }

            override fun onClickPopupItem(bean: PostBean) {
                viewModel.onListItem(context!!,bean,activity)
            }

            override fun itemCheckChange(item: PostBean) {
                viewModel.itemCheckChange(item)

            }

            override fun getSelectState(): Int {
                return viewModel.getSelectState()
            }

        })
        recyclerView.adapter =adapter
        recyclerView.setHasFixedSize(true)
        viewModel.loadPosts()

        viewModel.livedataPostLoad.observe(viewLifecycleOwner, Observer {
            adapter.setData(it)
            showHelp(it.size)
        })

        viewModel.liveDataChange.observe(viewLifecycleOwner, Observer {
            adapter.setData(it)
            showHelp(it.size)
        })

        viewModel.livedataDeleteDialog.observe(viewLifecycleOwner, Observer {
            if (it){
                showDeleteDialog(viewModel.deleteBean)
                viewModel.livedataDeleteDialog.value = false
            }
        })


        viewModel.liveDataShowSelectState.observe(viewLifecycleOwner) {
            showSelectState(it)
        }

        viewModel.liveDataSelectNums.observe(viewLifecycleOwner) {
            activity?.title = "☑ : $it"
        }

        viewModel.liveDataShowPlsWaitDialog.observe(viewLifecycleOwner){
            if (it.second){
                showLoading(true,R.string.please_wait)
                Handler(Looper.getMainLooper()).postDelayed({
                    viewModel.showInterAds(it.first,requireActivity())
                    showLoading(false)
                },MyUtils.ADS_DELAY_TIME)
                viewModel.liveDataShowPlsWaitDialog.value = Pair(it.first,false)
            }
        }


        help_cardview.helpCardviewStory.howToUseStory.text = String.format(getString(R.string.how_to_download_something),
            getString(R.string.story))
        help_cardview.helpCardview.howToUsePost.text = String.format(getString(R.string.how_to_download_something),
            "")
        help_cardview.root.setOnClickListener {
            MyUtils.openInstagramApp(MyApp.gContext)
        }

    }

    private fun showHelp(size:Int){
        MyUtils.log("showhelp $size" )
        if(size> 0){
            help_layout.visibility = View.GONE
        }else{
            help_layout.visibility = View.VISIBLE
        }
    }



    private fun showDeleteSelectedDialog() {

        if (dialog != null){
            dialog?.dismiss()
            dialog = null
        }
        if (viewModel.liveDataSelectNums.value == null || viewModel.liveDataSelectNums.value == 0){
            Toast.makeText(context,"☑ : 0 ",Toast.LENGTH_SHORT).show()
            return
        }
        dialog =  MaterialAlertDialogBuilder(requireActivity()).setTitle("☑ : ${viewModel.liveDataSelectNums.value}")
            .setMessage(getString(R.string.are_u_sure_delete))
            .setNeutralButton(
                getString(android.R.string.cancel)
            ) { dialog, which -> dialog.dismiss() }.setPositiveButton(
                getString(android.R.string.ok)
            ) { dialog, which ->
                dialog.dismiss()
                viewModel.deleteSelectedBean()
                if (viewModel.downloadsIsEmpty()){
                    showSelectState(false)
                    viewModel.undo()
                }
                MyUtils.toast(R.string.com_operation_success)
            }.create()
        dialog?.show()
    }

    private var dialog: AlertDialog? = null
    private fun showDeleteDialog(bean: PostBean?) {
        if (bean == null){
            return
        }
        if (dialog != null){
            dialog?.dismiss()
            dialog = null
        }
        dialog =  MaterialAlertDialogBuilder(requireActivity()).setTitle(getString(R.string.are_u_sure_delete))
            .setIcon(R.drawable.ic_delete)
            .setNeutralButton(
                getString(android.R.string.cancel)
            ) { dialog, which -> dialog.dismiss() }.setPositiveButton(
                getString(android.R.string.ok)
            ) { dialog, which ->
                dialog.dismiss()
                viewModel.deleteBean(viewModel.deleteBean)
                MyUtils.toast(R.string.com_operation_success)

            }.create()
        dialog?.show()
    }


    private var progressBar: ProgressDialog? = null

    private fun hideLoginDialog(){
        if (dialog != null){
            dialog?.dismiss()
            dialog = null
        }
    }
    private fun showLoading(bool: Boolean,tip:Int = R.string.tip_loading) {
        try {
            hideLoginDialog()
            if (bool) {
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


}