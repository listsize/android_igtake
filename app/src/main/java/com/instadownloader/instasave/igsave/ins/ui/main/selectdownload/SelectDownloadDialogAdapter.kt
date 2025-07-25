package com.instadownloader.instasave.igsave.ins.ui.main.selectdownload

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.instadownloader.instasave.igsave.ins.R


class SelectDownloadDialogAdapter : RecyclerView.Adapter<SelectDownloadDialogAdapter.ViewHolder>() {
    private var items = ArrayList<ItemModel>() // 你的图片URL列表


    fun setSrcs(array: ArrayList<ItemModel>){
       items = array
    }


    fun selectAll(isSelected: Boolean) {
        for (item in items){
            item.isChecked = isSelected
        }
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<ItemModel> {
        return items.filter { it.isChecked }
    }

    fun getUnSelectedItems():List<ItemModel>{
        return items.filter { !it.isChecked }
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val checkItem: CheckBox = itemView.findViewById(R.id.checkItem)
        val iconVideo:ImageView = itemView.findViewById(R.id.icon_video)

        fun bind(item: ItemModel) {
            Glide.with(itemView.context).load(item.url).into(imageView)

            if (item.url.contains(".mp4")){
                iconVideo.visibility = View.VISIBLE
            }else{
                iconVideo.visibility = View.GONE
            }

            checkItem.isChecked = item.isChecked


            checkItem.setOnCheckedChangeListener { _, isChecked ->
                item.isChecked = isChecked
                // 调用回调函数
                onItemCheckedChanged?.invoke()
            }

            imageView.setOnClickListener {
                checkItem.isChecked = !checkItem.isChecked
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.selectdown_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
       return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    // 定义一个回调接口，用于通知外部选中项的变化
    var onItemCheckedChanged: (() -> Unit)? = null
}
