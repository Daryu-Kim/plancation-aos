package com.daryukim.plancation

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.util.*

class CurrentCalendarUsersAdapter(
  private val calendarUserList: List<String>,
) :
  RecyclerView.Adapter<CurrentCalendarUsersAdapter.CurrentCalendarUsersViewHolder>() {
    private lateinit var calendarUser: String
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrentCalendarUsersViewHolder {
    val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_current_calendar_user, parent, false)
    return CurrentCalendarUsersViewHolder(itemView)
  }

  override fun getItemCount(): Int {
    return calendarUserList.size
  }

  @SuppressLint("ResourceType", "SetTextI18n")
  override fun onBindViewHolder(holder: CurrentCalendarUsersViewHolder, @SuppressLint("RecyclerView") position: Int) {
    calendarUser = calendarUserList[position]

    Application.db.collection("Users")
      .document(calendarUserList[position])
      .get()
      .addOnSuccessListener { user ->
        if (user.get("userImagePath") == null) {
          holder.itemImg.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.ic_user_profile)
          holder.itemImgName.text = user.get("userName") as String
          holder.itemImgName.visibility = View.VISIBLE
        } else {
          Glide.with(holder.itemView.context)
            .asBitmap()
            .load(user.get("userImagePath") as String)
            .circleCrop()
            .into(object : CustomTarget<Bitmap>() {
              override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                val drawable: Drawable = BitmapDrawable(holder.itemView.resources, resource)
                holder.itemImg.background = drawable
              }

              override fun onLoadCleared(placeholder: Drawable?) {}
            })
          holder.itemImgName.visibility = View.GONE
        }
        holder.itemName.text = user["userName"].toString()
      }
  }

  inner class CurrentCalendarUsersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val itemImg: RelativeLayout = itemView.findViewById(R.id.side_item_current_calendar_user_img)
    val itemImgName: TextView = itemView.findViewById(R.id.side_item_current_calendar_user_img_name)
    val itemName: TextView = itemView.findViewById(R.id.side_item_current_calendar_user_name)
  }
}