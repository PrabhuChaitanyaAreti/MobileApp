package com.vsoft.goodmankotlin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vsoft.goodmankotlin.R
import com.vsoft.goodmankotlin.interfaces.RightMenuItemClickCallBack
import com.vsoft.goodmankotlin.model.RightMenuDataModel

class RightMenuAdapter(private val rightMenuItemList: ArrayList<RightMenuDataModel>, val rightMenuItemClickCallBack: RightMenuItemClickCallBack) : RecyclerView.Adapter<RightMenuAdapter.ViewHolder>()  {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RightMenuAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.right_menu_list_item_layout, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: RightMenuAdapter.ViewHolder, position: Int) {
        holder.bindItems(rightMenuItemList[position])
    }

    override fun getItemCount(): Int {
        return rightMenuItemList.size
    }
    //the class is holding the list view
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(rightMenuDataItem: RightMenuDataModel) {
            val punchId = itemView.findViewById(R.id.punchId) as TextView
            val punchType  = itemView.findViewById(R.id.punchType) as TextView
            punchId.text = rightMenuDataItem.id
            punchType.text = rightMenuDataItem.title
            itemView.setOnClickListener {
                rightMenuItemClickCallBack.onRightMenuItemClickCallBack(adapterPosition)
            }
        }
    }
}