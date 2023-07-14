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

class RecentAlertAdapter(
  private val eventList: List<ScheduleModel>,
) :
  RecyclerView.Adapter<RecentAlertAdapter.RecentAlertViewHolder>() {
    private lateinit var onItemClickListener: (Int, ScheduleModel) -> Unit
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentAlertViewHolder {
    val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_recent_alert, parent, false)
    return RecentAlertViewHolder(itemView)
  }

  override fun getItemCount(): Int {
    return eventList.size
  }

  @SuppressLint("ResourceType", "SetTextI18n")
  override fun onBindViewHolder(holder: RecentAlertViewHolder, @SuppressLint("RecyclerView") position: Int) {
    val schedule = eventList[position]
    val date = eventList[position].eventTime.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    val betweenDates = DAYS.between(LocalDate.now(), date)

    if (eventList[position].eventIsTodo) {
      holder.itemIcon.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.ic_todo)
    } else {
      holder.itemIcon.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.ic_calendar)
    }

    holder.itemTitle.text = eventList[position].eventTitle
    holder.itemDate.text = DateTimeFormatter.ofPattern("M월 d일").format(date)
    when {
      betweenDates < 0 -> {
        if (eventList[position].eventIsTodo) {
          if (eventList[position].eventCheckUsers.contains(Application.auth.currentUser!!.uid)) {
            holder.itemContent.text = "'${eventList[position].eventTitle}' 할 일이 ${abs(betweenDates)}일 지났습니다.\n할 일을 잘 마치셨군요!"
          } else {
            holder.itemContent.text = "'${eventList[position].eventTitle}' 할 일이 ${abs(betweenDates)}일 지났습니다.\n할 일을 마치셨나요?"
          }

          if (!eventList[position].eventUsers.contains(Application.auth.currentUser!!.uid)) {
            holder.itemContent.text = "'${eventList[position].eventTitle}' 할 일이 ${abs(betweenDates)}일 지났습니다.\n참여자에 포함되어 있지 않으셨군요!"
          }
        } else {
          holder.itemContent.text = "'${eventList[position].eventTitle}' 일정이 ${abs(betweenDates)}일 지났습니다."
        }
      }
      betweenDates.toInt() == 0 -> {
        if (eventList[position].eventIsTodo) {
          holder.itemContent.text = "오늘은 '${eventList[position].eventTitle}' 할 일이 있습니다."
        } else {
          holder.itemContent.text = "오늘은 '${eventList[position].eventTitle}' 일정이 있습니다."
        }
      }
      else -> {
        holder.itemContent.text = "'${eventList[position].eventTitle}'까지 ${betweenDates}일 남았습니다."
      }
    }

    holder.itemLayout.setOnClickListener {
      if (eventList[position].eventIsTodo) {
        onItemClickListener(R.id.nav_todo, eventList[position])
      } else {
        onItemClickListener(R.id.nav_calendar, eventList[position])
      }
    }
  }

  inner class RecentAlertViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val itemLayout: View = itemView.findViewById(R.id.item_recent_alert_layout)
    val itemIcon: ImageView = itemView.findViewById(R.id.item_recent_alert_ic)
    val itemTitle: TextView = itemView.findViewById(R.id.item_recent_alert_title)
    val itemDate: TextView = itemView.findViewById(R.id.item_recent_alert_date)
    val itemContent: TextView = itemView.findViewById(R.id.item_recent_alert_content)
  }

  fun onItemClickListener(listener: (Int, ScheduleModel) -> Unit) {
    onItemClickListener = listener
  }
}