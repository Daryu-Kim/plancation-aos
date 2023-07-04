package com.daryukim.plancation

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDate
import kotlin.coroutines.coroutineContext

class CalendarAdapter(dayList: ArrayList<LocalDate>) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

  private var dayList: ArrayList<LocalDate> = ArrayList()
  private var lastSelectedPosition = -1
  private var lastSelectedDate: LocalDate = LocalDate.of(1,1,1)

  init {
    this.dayList = dayList
    lastSelectedPosition = dayList.indexOf(CalendarUtil.selectedDate.value)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
    val itemView = LayoutInflater.from(parent.context).inflate(R.layout.calendar_cell, parent, false)
    return CalendarViewHolder(itemView)
  }

  override fun getItemCount(): Int {
    return dayList.size
  }

  @SuppressLint("ResourceAsColor", "NotifyDataSetChanged")
  override fun onBindViewHolder(holder: CalendarViewHolder, @SuppressLint("RecyclerView") position: Int) {
    val day: LocalDate = dayList[position]

    if (day.year == 1) {
      holder.dayTextView.text = ""
    } else {
      holder.dayTextView.text = day.dayOfMonth.toString()
    }

    if (LocalDate.now() == day) {
      holder.dayLayout.setBackgroundResource(R.drawable.calendar_border_day)
      holder.dayTextView.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.text))
    }
    else if (CalendarUtil.selectedDate.value == day) {
      holder.dayLayout.setBackgroundResource(R.drawable.calendar_solid_day)
      holder.dayTextView.setTextColor(Color.WHITE)
    } else {
      holder.dayLayout.background = null
      holder.dayTextView.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.text))
    }

    if ((position + 1) % 7 == 0) {
      holder.dayTextView.setTextColor(Color.BLUE)

    } else if (position == 0 || position % 7 == 0) {
      holder.dayTextView.setTextColor(Color.RED)
    }

    holder.itemView.setOnClickListener(View.OnClickListener {
      if (day.year != 1) {
        if (lastSelectedPosition != -1) {
          notifyItemChanged(lastSelectedPosition)
        }
        CalendarUtil.selectedDate.value = day

        holder.dayLayout.setBackgroundResource(R.drawable.calendar_solid_day)
        holder.dayTextView.setTextColor(Color.WHITE)
        lastSelectedPosition = position
        lastSelectedDate = day
      }
    })
  }

  inner class CalendarViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val dayTextView: TextView = itemView.findViewById<TextView>(R.id.day_text)
    val dayLayout: View = itemView.findViewById(R.id.day_layout)
  }
}