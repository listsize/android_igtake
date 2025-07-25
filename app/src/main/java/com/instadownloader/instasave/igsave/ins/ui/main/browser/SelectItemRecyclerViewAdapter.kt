package com.instadownloader.instasave.igsave.ins.ui.main.browser

import android.media.Image
import android.net.Uri
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatCheckBox
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.instadownloader.instasave.igsave.ins.databinding.FragmentSelectPostItemBinding

import java.util.*
import kotlin.collections.ArrayList

/**
 * [RecyclerView.Adapter] that can display a [PlaceholderItem].
 * TODO: Replace the implementation with code for your data type.
 */
class SelectItemRecyclerViewAdapter(
    private var values: ArrayList<BrowserPostMediaBean>
) : RecyclerView.Adapter<SelectItemRecyclerViewAdapter.ViewHolder>() {

    var listener:OnClickSelectPost ? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentSelectPostItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        Glide.with(holder.image.context).load(Uri.parse(if (item.displayUrl.isEmpty()) item.videoUrl else item.displayUrl)).into(holder.image);
        when(item.state){
            BrowserPostMediaBean.STATE_UNCHECKED->{
                holder.checkBox.visibility = View.VISIBLE
                holder.icon_done.visibility = View.INVISIBLE
                holder.checkBox.isChecked =false
            }
            BrowserPostMediaBean.STATE_CHECKED->{
                holder.checkBox.visibility = View.VISIBLE
                holder.icon_done.visibility = View.INVISIBLE
                holder.checkBox.isChecked =true

            }
            BrowserPostMediaBean.STATE_DOWNLOADED->{
                holder.checkBox.visibility = View.INVISIBLE
                holder.icon_done.visibility = View.VISIBLE
            }
        }

        holder.icon_video.visibility = if (item.videoUrl.isNotEmpty()) View.VISIBLE else View.INVISIBLE
        holder.itemView.setOnClickListener {
            if (item.state != BrowserPostMediaBean.STATE_DOWNLOADED){
                item.state = if (item.state == BrowserPostMediaBean.STATE_CHECKED) BrowserPostMediaBean.STATE_UNCHECKED else BrowserPostMediaBean.STATE_CHECKED
                holder.checkBox.isChecked =  item.state > 0
                listener?.onClickItem(item)
            }
        }

    }

    fun setData(list: ArrayList<BrowserPostMediaBean>){
        values = list
        notifyDataSetChanged()
    }
    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentSelectPostItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val image: ImageView = binding.itemImage
        val icon_video:ImageView = binding.itemImageVideo
        val checkBox: AppCompatCheckBox = binding.itemSelect
        val icon_done: ImageView = binding.itemDownloaded

    }

}