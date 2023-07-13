package com.daryukim.plancation

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import okhttp3.internal.wait
import java.util.*

class DeleteCalendarAdapter(
  private val calendarItemList: List<CalendarModel>,
  private val context: Context
) :
  RecyclerView.Adapter<DeleteCalendarAdapter.DeleteCalendarViewHolder>() {
    private lateinit var calendarItem: CalendarModel
    private lateinit var onClickListener: (CalendarModel) -> Unit
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeleteCalendarViewHolder {
    val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_delete_calendar, parent, false)
    return DeleteCalendarViewHolder(itemView)
  }

  override fun getItemCount(): Int {
    return calendarItemList.size
  }

  @SuppressLint("ResourceType", "SetTextI18n")
  override fun onBindViewHolder(holder: DeleteCalendarViewHolder, @SuppressLint("RecyclerView") position: Int) {
    calendarItem = calendarItemList[position]

    holder.itemName.text = calendarItemList[position].calendarTitle
    holder.itemType.background =
      if (calendarItemList[position].calendarUsers.size == 1)
        ContextCompat.getDrawable(holder.itemView.context, R.drawable.ic_person)
      else
        ContextCompat.getDrawable(holder.itemView.context, R.drawable.ic_group)

    if (calendarItemList[position].calendarID == Application.auth.currentUser!!.uid || calendarItemList[position].calendarAuthorID != Application.auth.currentUser!!.uid) {
      holder.itemDelDisabled.visibility = VISIBLE
    } else {
      holder.itemDelBtn.visibility = VISIBLE
    }

    holder.itemDelBtn.setOnClickListener {
      if (context is AppCompatActivity) {
        val deleteCalendarBottomSheet = DeleteCalendarBottomSheet()
        deleteCalendarBottomSheet.setOnClickListener {
          onClickListener(calendarItemList[position])
        }
        deleteCalendarBottomSheet.show(context.supportFragmentManager, "todoForm")
      }

    }
  }

  fun setOnClickListener(listener: (CalendarModel) -> Unit) {
    onClickListener = listener
  }

  inner class DeleteCalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val itemName: TextView = itemView.findViewById(R.id.item_calendar_name)
    val itemType: ImageView = itemView.findViewById(R.id.item_calendar_type)
    val itemDelBtn: Button = itemView.findViewById(R.id.item_calendar_delete_btn)
    val itemDelDisabled: TextView = itemView.findViewById(R.id.item_calendar_delete_disabled)
  }
}