package com.daryukim.plancation

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

class AIResultItemAdapter(
  private val resultItemList: List<AIResultModel>,
) :
  RecyclerView.Adapter<AIResultItemAdapter.ResultItemViewHolder>() {
    private lateinit var resultItem: AIResultModel
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultItemViewHolder {
    val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_ai_result, parent, false)
    return ResultItemViewHolder(itemView)
  }

  override fun getItemCount(): Int {
    return resultItemList.size
  }

  @SuppressLint("ResourceType", "SetTextI18n")
  override fun onBindViewHolder(holder: ResultItemViewHolder, @SuppressLint("RecyclerView") position: Int) {
    resultItem = resultItemList[position]

    if (position == 0 || resultItemList[position].date > resultItemList[position - 1].date) {
      holder.itemDateLayout.visibility = VISIBLE
      holder.itemDateText.text = DateTimeFormatter.ofPattern("yyyy-MM-dd EEE", Locale.US).format(resultItemList[position].date)
    }

    val startTime = DateTimeFormatter.ofPattern("a hh:mm", Locale.US).format(resultItemList[position].startTime).uppercase()
    val endTime = DateTimeFormatter.ofPattern("a hh:mm", Locale.US).format(resultItemList[position].endTime).uppercase()

    holder.itemDateButton.text = "${startTime} ~\n${endTime}"
    holder.itemTitleText.text = resultItemList[position].title
  }

  inner class ResultItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val itemDateLayout: View = itemView.findViewById(R.id.ai_result_date_layout)
    val itemDateText: TextView = itemView.findViewById(R.id.ai_result_date_text)
    val itemLayout: View = itemView.findViewById(R.id.ai_result_item_layout)
    val itemDateButton: TextView = itemView.findViewById(R.id.ai_result_item_date)
    val itemTitleText: TextView = itemView.findViewById(R.id.ai_result_item_title)
  }
}