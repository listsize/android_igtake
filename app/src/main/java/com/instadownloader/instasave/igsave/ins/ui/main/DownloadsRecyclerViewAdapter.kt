package com.instadownloader.instasave.igsave.ins.ui.main

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.instadownloader.instasave.igsave.ins.MyUtils
import com.instadownloader.instasave.igsave.ins.R
import com.instadownloader.instasave.igsave.ins.ui.main.data.PostBean
import java.io.File
import java.util.*

/**
 * [RecyclerView.Adapter] that can display a [DummyItem].
 * TODO: Replace the implementation with code for your data type.
 */
class DownloadsRecyclerViewAdapter(val context:Context, private var values: LinkedList<PostBean>, val lisenter:IItemLisenter
       )
    : RecyclerView.Adapter<DownloadsRecyclerViewAdapter.ViewHolder>() {


    interface IItemLisenter{
        fun onClickPopupItem(id:Int, bean:PostBean)
        fun onClickPopupItem( bean:PostBean)
        fun itemCheckChange(item: PostBean)
        fun getSelectState(): Int
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.downloads_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var item = values.get(position)
        holder.name.text = item.name
        holder.contentView.text = item.text
        holder.menu.tag = item
        holder.cardview.tag = item

        holder.videoicon.visibility = View.INVISIBLE
        holder.manypicicon.visibility = View.INVISIBLE

        holder.cardview.setOnClickListener {
            lisenter.onClickPopupItem(holder.cardview.tag as PostBean )
        }

        Glide.with(context).clear(holder.image)

       val firstMedia = item.getPostLocalUris()[0]
       if (firstMedia.contains("video/media")){
           holder.videoicon.visibility = View.VISIBLE
       }
       MyUtils.log("first media $firstMedia")
       Glide.with(context).load(Uri.parse(firstMedia)).into(holder.image);


        if (item.getPosturls().size >1){
            holder.manypicicon.visibility = View.VISIBLE
        }


        Glide.with(context).clear(holder.profile_pic)
        Glide.with(context).load(item.profile_pic).apply(RequestOptions().centerCrop().transform( RoundedCorners(50))).into(holder.profile_pic)

        holder.item_select.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                item.itemState = item.ITEM_CHECKED
                lisenter.itemCheckChange(item)

            }else{
                item.itemState = item.ITEM_UNCHECKED
                lisenter.itemCheckChange(item)
            }
        }
        when(item.itemState){
            item.ITEM_IDEL->{
                if (lisenter.getSelectState() == 1){
                    holder.item_select.visibility = View.VISIBLE
                    holder.item_select.isChecked = false
                    holder.menu.visibility = View.INVISIBLE
                }else{
                    holder.menu.visibility = View.VISIBLE
                    holder.item_select.visibility = View.INVISIBLE
                }
            }
            item.ITEM_UNCHECKED->{
                holder.item_select.visibility = View.VISIBLE
                holder.item_select.isChecked = false
                holder.menu.visibility = View.INVISIBLE
            }
            item.ITEM_CHECKED->{
                holder.item_select.visibility = View.VISIBLE
                holder.item_select.isChecked = true
                holder.menu.visibility = View.INVISIBLE
            }
        }

        holder.menu.setOnClickListener {
            val popupMenu = PopupMenu(context, holder.menu).also {
                it.inflate(R.menu.popup)
            }
            if (!item.url.contains("https")){
                popupMenu.menu.removeItem(R.id.action_copy_url)
                popupMenu.menu.removeItem(R.id.action_view_in_instagram)

            }

            if(TextUtils.isEmpty(item.text)){
                popupMenu.menu.removeItem(R.id.action_copy_all)
            }

            try {
                val fiedlMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
                fiedlMPopup.isAccessible = true
                val mPopup = fiedlMPopup.get(popupMenu)
                mPopup.javaClass.getDeclaredMethod("setForceShowIcon",Boolean::class.java).invoke(mPopup,true)
            }catch (ex:Exception){
                ex.printStackTrace()
            }
            popupMenu.setOnMenuItemClickListener { it ->
                lisenter.onClickPopupItem(it.itemId,holder.menu.tag as PostBean )
                true
            }

            popupMenu.show()
        }

    }

    fun setData(list:LinkedList<PostBean>){
        values = list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardview:CardView = view.findViewById(R.id.cardview)
        val image:ImageView = view.findViewById(R.id.image)
        val name: TextView = view.findViewById(R.id.name)
        val contentView: TextView = view.findViewById(R.id.content)
        val menu: ImageButton = view.findViewById(R.id.menu)
        val videoicon: ImageView = view.findViewById(R.id.video_lt)
        val manypicicon: ImageView = view.findViewById(R.id.manypic_rt)
        val profile_pic: ImageView = view.findViewById(R.id.profile_pic)
        val item_select: CheckBox = view.findViewById(R.id.item_select)


        override fun toString(): String {
            return super.toString() + " '" + contentView.text + "'"
        }
    }


}