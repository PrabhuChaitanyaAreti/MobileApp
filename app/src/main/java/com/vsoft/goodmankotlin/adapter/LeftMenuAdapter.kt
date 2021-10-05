package com.vsoft.goodmankotlin.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vsoft.goodmankotlin.MaskingActivity
import com.vsoft.goodmankotlin.R
import com.vsoft.goodmankotlin.model.LeftMenuDataModel

class LeftMenuAdapter(var context:Context,val leftMenuItemList: ArrayList<LeftMenuDataModel>,val recyclerView: RecyclerView) : RecyclerView.Adapter<LeftMenuAdapter.ViewHolder>() {
    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeftMenuAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.left_menu_list_layout, parent, false)
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

    //the class is holding the list view
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(leftMenuDataItem: LeftMenuDataModel) {
            val ivPunchColor = itemView.findViewById(R.id.leftMenuRecycler) as ImageView
            val punchTypeTitle = itemView.findViewById(R.id.punchTypeImage) as TextView
            val toggleMenu = itemView.findViewById(R.id.togglePunchType) as Switch
            val punchTypeCount = itemView.findViewById(R.id.punchTypeCount) as TextView
            punchTypeCount.text = "(" + leftMenuDataItem.count + ")"
            if (leftMenuDataItem.count == 0) {
                toggleMenu.visibility = View.GONE
            }
            ivPunchColor.setBackgroundColor(leftMenuDataItem.punchColor)
            punchTypeTitle.text = leftMenuDataItem.title
            toggleMenu.isChecked = leftMenuDataItem.toggleMenu
            toggleMenu.setOnCheckedChangeListener { _, isChecked ->
                // do whatever you need to do when the switch is toggled here
                if (isChecked) {
                    if (adapterPosition == 0) {
                        (recyclerView.findViewHolderForAdapterPosition(0)?.itemView?.findViewById(
                            R.id.togglePunchType
                        ) as Switch).isChecked = true
                        if ((recyclerView.findViewHolderForAdapterPosition(1)?.itemView?.findViewById(
                                R.id.togglePunchType
                            ) as Switch).isChecked
                        ) {
                            (recyclerView.findViewHolderForAdapterPosition(1)?.itemView?.findViewById(
                                R.id.togglePunchType
                            ) as Switch).isChecked = false
                        }
                        if ((recyclerView.findViewHolderForAdapterPosition(2)?.itemView?.findViewById(
                                R.id.togglePunchType
                            ) as Switch).isChecked
                        ) {
                            (recyclerView.findViewHolderForAdapterPosition(2)?.itemView?.findViewById(
                                R.id.togglePunchType
                            ) as Switch).isChecked = false
                        }
                        if ((recyclerView.findViewHolderForAdapterPosition(3)?.itemView?.findViewById(
                                R.id.togglePunchType
                            ) as Switch).isChecked
                        ) {
                            (recyclerView.findViewHolderForAdapterPosition(3)?.itemView?.findViewById(
                                R.id.togglePunchType
                            ) as Switch).isChecked = false
                        }
                        leftMenuItemList[0].toggleMenu = true
                        leftMenuItemList[1].toggleMenu = false
                        leftMenuItemList[2].toggleMenu = false
                        leftMenuItemList[3].toggleMenu = false
                    } else if (adapterPosition == 1) {
                        (recyclerView.findViewHolderForAdapterPosition(0)?.itemView?.findViewById(
                            R.id.togglePunchType
                        ) as Switch).isChecked = false
                        leftMenuItemList[0].toggleMenu = false
                        leftMenuItemList[1].toggleMenu = true
                    } else if (adapterPosition == 2) {
                        (recyclerView.findViewHolderForAdapterPosition(0)?.itemView?.findViewById(
                            R.id.togglePunchType
                        ) as Switch).isChecked = false
                        leftMenuItemList[0].toggleMenu = false
                        leftMenuItemList[2].toggleMenu = true
                    } else if (adapterPosition == 3) {
                        (recyclerView.findViewHolderForAdapterPosition(0)?.itemView?.findViewById(
                            R.id.togglePunchType
                        ) as Switch).isChecked = false
                        leftMenuItemList[0].toggleMenu = false
                        leftMenuItemList[3].toggleMenu = true
                    }
                } else {
                    if (adapterPosition == 1) {
                        leftMenuItemList[1].toggleMenu = false
                        if (!leftMenuItemList[1].toggleMenu && !leftMenuItemList[2].toggleMenu && !leftMenuItemList[3].toggleMenu) {
                            (recyclerView.findViewHolderForAdapterPosition(0)?.itemView?.findViewById(
                                R.id.togglePunchType
                            ) as Switch).isChecked = true
                            leftMenuItemList[0].toggleMenu = true
                        }
                    }
                    if (adapterPosition == 2) {
                        leftMenuItemList[2].toggleMenu = false
                        if (!leftMenuItemList[1].toggleMenu && !leftMenuItemList[2].toggleMenu && !leftMenuItemList[3].toggleMenu) {
                            (recyclerView.findViewHolderForAdapterPosition(0)?.itemView?.findViewById(
                                R.id.togglePunchType
                            ) as Switch).isChecked = true
                            leftMenuItemList[0].toggleMenu = true
                        }
                    }
                    if (adapterPosition == 3) {
                        leftMenuItemList[3].toggleMenu = false
                        if (!leftMenuItemList[1].toggleMenu && !leftMenuItemList[2].toggleMenu && !leftMenuItemList[3].toggleMenu) {
                            (recyclerView.findViewHolderForAdapterPosition(0)?.itemView?.findViewById(
                                R.id.togglePunchType
                            ) as Switch).isChecked = true
                            leftMenuItemList[0].toggleMenu = true
                        }
                    }
                }
                (context as MaskingActivity).drawMask()
            }
        }
    }
}