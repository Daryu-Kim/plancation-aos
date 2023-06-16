package com.daryukim.plancation

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.location.Geocoder
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnCreateContextMenuListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import java.lang.Exception
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit.DAYS
import java.util.Locale

class ScheduleAdapter(
  private val scheduleList: List<ScheduleModel>,
) :
  RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
    val itemView = LayoutInflater.from(parent.context).inflate(R.layout.schedule_item, parent, false)
    return ScheduleViewHolder(itemView)
  }

  override fun getItemCount(): Int {
    return scheduleList.size
  }

  @SuppressLint("ResourceType", "SetTextI18n")
  override fun onBindViewHolder(holder: ScheduleViewHolder, @SuppressLint("RecyclerView") position: Int) {
    val schedule = scheduleList[position]
    val scheduleDate = schedule.eventTime.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    val scheduleDDay = DAYS.between(LocalDate.now(), scheduleDate)
    val geocoder = Geocoder(holder.itemView.context, Locale.getDefault())

    holder.itemLayout.setCustomBackgroundTint(schedule.eventBackgroundColor)
    holder.itemTitle.text = schedule.eventTitle
    when {
      scheduleDDay == 0L -> holder.itemDate.text = "D-Day"
      scheduleDDay > 0 -> holder.itemDate.text = "D-$scheduleDDay"
      else -> holder.itemDate.text = "이미 지난 날짜입니다!"
    }
    try {
      val addresses = geocoder.getFromLocation(schedule.eventLocation.latitude, schedule.eventLocation.longitude, 1)
      if (addresses!!.isNotEmpty()) {
        val address = addresses[0]
        val placeAddress = address.getAddressLine(0)
        holder.itemLocation.text = "장소: $placeAddress"
      } else {
        holder.itemLocation.text = "장소를 지정하지 않았습니다!"
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
    holder.itemView.setOnClickListener(View.OnClickListener {
      Toast.makeText(holder.itemView.context, schedule.eventTitle, Toast.LENGTH_SHORT).show()
    })
  }

  inner class ScheduleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), OnCreateContextMenuListener {
    val itemLayout: View = itemView.findViewById(R.id.schedule_item_layout)
    val itemTitle: TextView = itemView.findViewById(R.id.schedule_item_title)
    val itemDate: TextView = itemView.findViewById(R.id.schedule_item_date)
    val itemLocation: TextView = itemView.findViewById(R.id.schedule_item_location)

    init {
      itemLayout.setOnCreateContextMenuListener(this)
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
      menu?.add(adapterPosition, 0, 0 ,"수정")
      menu?.add(adapterPosition, 1, 1 ,"삭제")
    }
  }

  private fun View.setCustomBackgroundTint(
    color: Map<String, Int>
  ) {
    val tempColor = Color.argb(color["alphaColor"]!!, color["redColor"]!!, color["greenColor"]!!, color["blueColor"]!!)
    val tempColorStateList = ColorStateList.valueOf(tempColor)
    return ViewCompat.setBackgroundTintList(this, tempColorStateList)
  }
}