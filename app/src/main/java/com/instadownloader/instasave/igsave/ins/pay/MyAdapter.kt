package com.instadownloader.instasave.igsave.ins.pay

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.instadownloader.instasave.igsave.ins.R

class MyAdapter(private val itemList: List<Item>) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
        val price: TextView = itemView.findViewById(R.id.price)
        val description: TextView = itemView.findViewById(R.id.description)

        init {
            // 当点击item时，更新CheckBox的状态
//            itemView.setOnClickListener {
//                val position = adapterPosition
//                if (position != RecyclerView.NO_POSITION) {
//                    itemList[position].isChecked = !itemList[position].isChecked
//                    notifyItemChanged(position)
//                }
//            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.sub_item_layout, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = itemList[position]
        holder.price.text = currentItem.price
        holder.description.text = currentItem.description
        holder.checkBox.isChecked = currentItem.isChecked
        holder.checkBox.isClickable = false

        // 更新CheckBox状态时也需要更新Item状态
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            currentItem.isChecked = isChecked
        }
    }

    override fun getItemCount() = itemList.size
}
