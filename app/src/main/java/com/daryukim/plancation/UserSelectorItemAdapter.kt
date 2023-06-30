package com.daryukim.plancation

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.firestore.FirebaseFirestore

class UserSelectorItemAdapter(
  private val userSelectorItemList: ArrayList<String>,
  private val checkedUserList: ArrayList<String>
) : RecyclerView.Adapter<UserSelectorItemAdapter.UserSelectorItemViewHolder>() {

  private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

  private lateinit var onUserSelectedListener: (ArrayList<String>) -> Unit

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserSelectorItemViewHolder {
    val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_user_selector, parent, false)
    return UserSelectorItemViewHolder(itemView)
  }

  override fun getItemCount(): Int = userSelectorItemList.size

  @SuppressLint("ResourceType", "SetTextI18n")
  override fun onBindViewHolder(holder: UserSelectorItemViewHolder, position: Int) {
    val userSelectorItem = userSelectorItemList[position]

    db.collection("Users")
      .document(userSelectorItem)
      .get()
      .addOnSuccessListener { document ->
        document?.data?.let { data ->
          setupViewHolder(holder, data)
        }
      }
  }

  private fun setupViewHolder(holder: UserSelectorItemViewHolder, data: Map<String, Any>) {
    val userImagePath = data["userImagePath"] as String?
    val userName = data["userName"].toString()
    val userId = data["userID"].toString()

    if (userImagePath == null) {
      holder.itemImg.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.ic_user_profile)
      holder.itemImgName.text = userName
      holder.itemImgName.visibility = View.VISIBLE
    } else {
      loadImageIntoView(holder, userImagePath)
      holder.itemImgName.visibility = View.GONE
    }
    holder.itemImgName.text = userName
    holder.itemName.text = userName

    holder.itemCheckBox.isChecked = userId in checkedUserList

    holder.itemCheckBox.setOnClickListener {
      toggleUserInCheckedList(holder, userId)
    }
  }

  private fun loadImageIntoView(holder: UserSelectorItemViewHolder, userImagePath: String) {
    Glide.with(holder.itemView.context)
      .asBitmap()
      .load(userImagePath)
      .circleCrop()
      .into(object : CustomTarget<Bitmap>() {
        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
          val drawable: Drawable = BitmapDrawable(holder.itemView.resources, resource)
          holder.itemImg.background = drawable
        }

        override fun onLoadCleared(placeholder: Drawable?) {}
      })
  }

  private fun toggleUserInCheckedList(holder: UserSelectorItemViewHolder, userId: String) {
    if (holder.itemCheckBox.isChecked) {
      checkedUserList.add(userId)
    } else {
      checkedUserList.remove(userId)
    }
    onUserSelectedListener(checkedUserList)
  }

  fun setOnUserSelectedListener(listener: (ArrayList<String>) -> Unit) {
    onUserSelectedListener = listener
  }

  inner class UserSelectorItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val itemImg: View = itemView.findViewById(R.id.user_selector_img)
    val itemImgName: TextView = itemView.findViewById(R.id.user_selector_img_name)
    val itemName: TextView = itemView.findViewById(R.id.user_selector_name)
    val itemCheckBox: CheckBox = itemView.findViewById(R.id.user_selector_item_checkbox)
  }
}
