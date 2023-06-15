package com.daryukim.plancation

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView

class ScheduleAdapter(scheduleList: ArrayList<ScheduleModel>) : RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {

  private var scheduleList: ArrayList<ScheduleModel> = ArrayList()

  init {
    this.scheduleList = scheduleList
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
    val itemView = LayoutInflater.from(parent.context).inflate(R.layout.schedule_item, parent, false)
    return ScheduleViewHolder(itemView)
  }

  override fun getItemCount(): Int {
    return scheduleList.size
  }

  @SuppressLint("ResourceAsColor", "NotifyDataSetChanged")
  override fun onBindViewHolder(holder: ScheduleViewHolder, @SuppressLint("RecyclerView") position: Int) {
    val schedule: ScheduleModel = scheduleList[position]
    holder.itemLayout.setCustomBackgroundTint(schedule.eventBackgroundColor)
    holder.itemView.setOnClickListener(View.OnClickListener {

    })
  }

  inner class ScheduleViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val itemLayout: View = itemView.findViewById(R.id.schedule_item_layout)
    val dayLayout: View = itemView.findViewById(R.id.day_layout)
  }

  private fun View.setCustomBackgroundTint(
    color: ScheduleColorModel
  ) {
    val tempColor = Color.argb(color.alphaColor, color.redColor, color.greenColor, color.blueColor)
    val tempColorStateList = ColorStateList.valueOf(tempColor)
    return ViewCompat.setBackgroundTintList(this, tempColorStateList)
  }
}