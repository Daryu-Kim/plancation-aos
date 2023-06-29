package com.daryukim.plancation

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TodoUserAdapter(
  private val todoUserList: ArrayList<String>,
  private val todoCheckUserList: ArrayList<String>,
) :
  RecyclerView.Adapter<TodoUserAdapter.TodoUserViewHolder>() {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoUserViewHolder {
    val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_todo_user, parent, false)
    return TodoUserViewHolder(itemView)
  }

  override fun getItemCount(): Int {
    return todoUserList.size
  }

  @SuppressLint("ResourceType", "SetTextI18n")
  override fun onBindViewHolder(holder: TodoUserViewHolder, @SuppressLint("RecyclerView") position: Int) {
    db.collection("Users")
      .document(todoUserList[position])
      .get()
      .addOnSuccessListener { value ->
        if (value.get("userImagePath") == null) {
          holder.itemImg.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.ic_user_profile)
          holder.itemImgName.text = value.get("userName") as String
          holder.itemImgName.visibility = VISIBLE
        } else {
          Glide.with(holder.itemView.context)
            .asBitmap()
            .load(value.get("userImagePath") as String)
            .circleCrop()
            .into(object : CustomTarget<Bitmap>() {
              override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                val drawable: Drawable = BitmapDrawable(holder.itemView.resources, resource)
                holder.itemImg.background = drawable
              }

              override fun onLoadCleared(placeholder: Drawable?) {}
            })
          holder.itemImgName.visibility = GONE
        }
        if (todoCheckUserList.contains(todoUserList[position])) {
          holder.itemChecked.visibility = VISIBLE
          holder.itemUnChecked.visibility = GONE
        } else {
          holder.itemChecked.visibility = GONE
          holder.itemUnChecked.visibility = VISIBLE
        }
        holder.itemName.text = value.get("userName") as String
      }
  }

  private fun getCheckData(holder: TodoUserViewHolder) {
  }

  inner class TodoUserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val itemImg: View = itemView.findViewById(R.id.todo_user_img)
    val itemImgName: TextView = itemView.findViewById(R.id.todo_user_img_name)
    val itemName: TextView = itemView.findViewById(R.id.todo_user_name)
    val itemChecked: ImageView = itemView.findViewById(R.id.todo_user_checked)
    val itemUnChecked: ImageView = itemView.findViewById(R.id.todo_user_unchecked)
  }
}