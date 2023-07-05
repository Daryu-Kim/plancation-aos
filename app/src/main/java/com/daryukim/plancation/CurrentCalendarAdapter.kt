package com.daryukim.plancation

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class CurrentCalendarAdapter(
  private val calendarItemList: List<CalendarModel>,
) :
  RecyclerView.Adapter<CurrentCalendarAdapter.CurrentCalendarViewHolder>() {
    private lateinit var calendarItem: CalendarModel
  private lateinit var onClickListener: (CalendarModel) -> Unit
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrentCalendarViewHolder {
    val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_type, parent, false)
    return CurrentCalendarViewHolder(itemView)
  }

  override fun getItemCount(): Int {
    return calendarItemList.size
  }

  @SuppressLint("ResourceType", "SetTextI18n")
  override fun onBindViewHolder(holder: CurrentCalendarViewHolder, @SuppressLint("RecyclerView") position: Int) {
    calendarItem = calendarItemList[position]

    holder.itemName.text = calendarItemList[position].calendarTitle
    holder.itemType.background =
      if (calendarItemList[position].calendarUsers.size == 1)
        ContextCompat.getDrawable(holder.itemView.context, R.drawable.ic_person)
      else
        ContextCompat.getDrawable(holder.itemView.context, R.drawable.ic_group)

    holder.itemLayout.setOnClickListener {
      onClickListener(calendarItemList[position])
    }
  }

  fun setOnClickListener(listener: (CalendarModel) -> Unit) {
    onClickListener = listener
  }

  inner class CurrentCalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val itemLayout: View = itemView.findViewById(R.id.side_item_current_calendar_layout)
    val itemName: TextView = itemView.findViewById(R.id.side_item_current_calendar_name)
    val itemType: ImageView = itemView.findViewById(R.id.side_item_current_calendar_type)
  }
}