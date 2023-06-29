package com.daryukim.plancation

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.time.DayOfWeek
import java.time.LocalDate

class TodoDateAdapter(todoDateList: ArrayList<LocalDate>) : RecyclerView.Adapter<TodoDateAdapter.TodoDateViewHolder>() {

  private var todoDateList: ArrayList<LocalDate> = ArrayList()
  private var lastSelectedPosition = -1
  private var lastSelectedDate: LocalDate = LocalDate.of(1,1,1)

  init {
    this.todoDateList = todoDateList
    lastSelectedPosition = todoDateList.indexOf(TodoUtil.selectedDate.value)
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoDateViewHolder {
    val itemView = LayoutInflater.from(parent.context).inflate(R.layout.todo_cell, parent, false)
    return TodoDateViewHolder(itemView)
  }

  override fun getItemCount(): Int {
    return todoDateList.size
  }

  @SuppressLint("ResourceAsColor", "NotifyDataSetChanged")
  override fun onBindViewHolder(holder: TodoDateViewHolder, @SuppressLint("RecyclerView") position: Int) {
    val date: LocalDate = todoDateList[position]

    if (TodoUtil.selectedDate.value == date) {
      holder.todoDateLayout.setBackgroundResource(R.drawable.item_selected_todo_date)
    } else {
      holder.todoDateLayout.setBackgroundResource(R.drawable.item_todo_date)
    }

    holder.todoDateDay.text = date.dayOfWeek.getDisplayNameInKorean()
    holder.todoDateDate.text = date.dayOfMonth.toString()

    holder.itemView.setOnClickListener(View.OnClickListener {
      if (date.year != 1) {
        if (lastSelectedPosition != -1) {
          notifyItemChanged(lastSelectedPosition)
        }
        TodoUtil.selectedDate.value = date

        holder.todoDateLayout.setBackgroundResource(R.drawable.item_selected_todo_date)
        lastSelectedPosition = position
        lastSelectedDate = date
      }
    })
  }

  private fun DayOfWeek.getDisplayNameInKorean(): String {
    return when (this) {
      DayOfWeek.MONDAY -> "월"
      DayOfWeek.TUESDAY -> "화"
      DayOfWeek.WEDNESDAY -> "수"
      DayOfWeek.THURSDAY -> "목"
      DayOfWeek.FRIDAY -> "금"
      DayOfWeek.SATURDAY -> "토"
      DayOfWeek.SUNDAY -> "일"
    }
  }

  inner class TodoDateViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val todoDateLayout: View = itemView.findViewById<TextView>(R.id.todo_date_layout)
    val todoDateDay: TextView = itemView.findViewById(R.id.todo_date_day)
    val todoDateDate: TextView = itemView.findViewById(R.id.todo_date_date)
  }
}