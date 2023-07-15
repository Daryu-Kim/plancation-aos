package com.daryukim.plancation

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit.DAYS
import kotlin.math.abs

class SearchAdapter(
  private val eventList: List<ScheduleModel>,
) :
  RecyclerView.Adapter<SearchAdapter.RecentAlertViewHolder>() {
    private lateinit var onItemClickListener: (Int, ScheduleModel) -> Unit
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentAlertViewHolder {
    val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_search, parent, false)
    return RecentAlertViewHolder(itemView)
  }

  override fun getItemCount(): Int {
    return eventList.size
  }

  @SuppressLint("ResourceType", "SetTextI18n")
  override fun onBindViewHolder(holder: RecentAlertViewHolder, @SuppressLint("RecyclerView") position: Int) {
    val schedule = eventList[position]
    val date = eventList[position].eventTime.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

    holder.itemTitle.text = eventList[position].eventTitle
    holder.itemDate.text = DateTimeFormatter.ofPattern("M월 d일").format(date)

    holder.itemLayout.setOnClickListener {
        onItemClickListener(R.id.nav_calendar, eventList[position])
    }
  }

  inner class RecentAlertViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val itemLayout: View = itemView.findViewById(R.id.item_search_layout)
    val itemTitle: TextView = itemView.findViewById(R.id.item_search_title)
    val itemDate: TextView = itemView.findViewById(R.id.item_search_date)
  }

  fun onItemClickListener(listener: (Int, ScheduleModel) -> Unit) {
    onItemClickListener = listener
  }
}