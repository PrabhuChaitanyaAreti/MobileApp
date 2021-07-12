package com.vsoft.goodmankotlin.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vsoft.goodmankotlin.R
import com.vsoft.goodmankotlin.model.LeftMenuDataModel
class LeftMenuAdapter(val leftMenuItemList: ArrayList<LeftMenuDataModel>,val recyclerView: RecyclerView) : RecyclerView.Adapter<LeftMenuAdapter.ViewHolder>() {
    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeftMenuAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.left_menu_list_layout, parent, false)
        return ViewHolder(v)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: LeftMenuAdapter.ViewHolder, position: Int) {
        holder.bindItems(leftMenuItemList[position])
    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return leftMenuItemList.size
    }

    //the class is hodling the list view
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(leftMenuDataItem: LeftMenuDataModel) {
            val ivPunchColor = itemView.findViewById(R.id.ivPunchColor) as ImageView
            val punchTypeTitle  = itemView.findViewById(R.id.punchTypeImage) as TextView
            val toggleMenu  = itemView.findViewById(R.id.toggleMenu) as Switch
            ivPunchColor.setBackgroundColor(leftMenuDataItem.punchColor)
            punchTypeTitle.setText(leftMenuDataItem.title)
            toggleMenu.isChecked=leftMenuDataItem.toggleMenu
            toggleMenu.setOnCheckedChangeListener { _, isChecked ->
                // do whatever you need to do when the switch is toggled here
                if(isChecked){
                    if(adapterPosition==0){
                        (recyclerView.findViewHolderForAdapterPosition(0)?.itemView?.findViewById(
                            R.id.toggleMenu
                        ) as Switch).isChecked=true
                        (recyclerView.findViewHolderForAdapterPosition(1)?.itemView?.findViewById(
                            R.id.toggleMenu
                        ) as Switch).isChecked=false
                        (recyclerView.findViewHolderForAdapterPosition(2)?.itemView?.findViewById(
                            R.id.toggleMenu
                        ) as Switch).isChecked=false
                        (recyclerView.findViewHolderForAdapterPosition(3)?.itemView?.findViewById(
                            R.id.toggleMenu
                        ) as Switch).isChecked=false
                        leftMenuItemList[0].toggleMenu=true
                        leftMenuItemList[1].toggleMenu=false
                        leftMenuItemList[2].toggleMenu=false
                        leftMenuItemList[3].toggleMenu=false
                    }else if(adapterPosition==1){
                        (recyclerView.findViewHolderForAdapterPosition(0)?.itemView?.findViewById(
                            R.id.toggleMenu
                        ) as Switch).isChecked=false
                        leftMenuItemList[0].toggleMenu=false
                        leftMenuItemList[1].toggleMenu=true
                    }else if(adapterPosition==2){
                        (recyclerView.findViewHolderForAdapterPosition(0)?.itemView?.findViewById(
                            R.id.toggleMenu
                        ) as Switch).isChecked=false
                        leftMenuItemList[0].toggleMenu=false
                        leftMenuItemList[2].toggleMenu=true
                    }else if(adapterPosition==3){
                        (recyclerView.findViewHolderForAdapterPosition(0)?.itemView?.findViewById(
                            R.id.toggleMenu
                        ) as Switch).isChecked=false
                        leftMenuItemList[0].toggleMenu=false
                        leftMenuItemList[3].toggleMenu=true
                    }
                }
            }
        }
    }
}


