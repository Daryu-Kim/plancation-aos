package com.daryukim.plancation

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class DiaryItemAdapter(
  private val diaryItemList: List<DiaryModel>,
) :
  RecyclerView.Adapter<DiaryItemAdapter.DiaryItemViewHolder>() {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var diaryItem: DiaryModel
  private lateinit var onClickListener: (DiaryModel) -> Unit
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryItemViewHolder {
    val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_diary, parent, false)
    return DiaryItemViewHolder(itemView)
  }

  override fun getItemCount(): Int {
    return diaryItemList.size
  }

  @SuppressLint("ResourceType", "SetTextI18n")
  override fun onBindViewHolder(holder: DiaryItemViewHolder, @SuppressLint("RecyclerView") position: Int) {
    diaryItem = diaryItemList[position]

    checkCalendarUsers(holder, position)
    holder.itemDay.text = SimpleDateFormat("EEE", Locale.US).format(diaryItem.postTime.toDate()).uppercase()
    holder.itemDate.text = SimpleDateFormat("d", Locale.getDefault()).format(diaryItem.postTime.toDate())
    holder.itemTitle.text = diaryItem.postTitle
    holder.itemContent.text = diaryItem.postContent
    if (diaryItem.postImage.isEmpty()) {
      holder.itemImage.visibility = GONE
    } else {
      holder.itemImage.visibility = VISIBLE
      Glide.with(holder.itemView.context)
        .load(diaryItem.postImage)
        .centerCrop()
        .into(holder.itemImage)
    }

    holder.itemLayout.setOnClickListener {
      onClickListener(diaryItemList[position])
    }
  }

  fun setOnClickListener(listener: (DiaryModel) -> Unit) {
    onClickListener = listener
  }

  private fun checkCalendarUsers(holder: DiaryItemViewHolder, position: Int) {
    db.collection("Calendars")
      .document("A9PHFsmDLUWbaYDdy2XX")
      .get()
      .addOnSuccessListener {
        if ((it.get("users") as ArrayList<*>).size == 1) {
          holder.itemUser.visibility = GONE
        } else {
          holder.itemUser.visibility = VISIBLE
          getUserName(holder, position)
        }
      }
  }

  private fun getUserName(holder: DiaryItemViewHolder, position: Int) {
    db.collection("Users")
      .document(diaryItemList[position].postAuthorID)
      .get()
      .addOnSuccessListener {
        holder.itemUser.text = it.get("userName").toString()
      }
  }

  inner class DiaryItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val itemLayout: View = itemView.findViewById(R.id.diary_item_layout)
    val itemDay: TextView = itemView.findViewById(R.id.diary_item_day)
    val itemDate: TextView = itemView.findViewById(R.id.diary_item_date)
    val itemUser: TextView = itemView.findViewById(R.id.diary_item_user)
    val itemTitle: TextView = itemView.findViewById(R.id.diary_item_title)
    val itemContent: TextView = itemView.findViewById(R.id.diary_item_content)
    val itemImage: ImageView = itemView.findViewById(R.id.diary_item_image)
  }
}