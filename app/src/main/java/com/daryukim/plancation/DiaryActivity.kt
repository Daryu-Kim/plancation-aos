package com.daryukim.plancation

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.daryukim.plancation.databinding.ActivityDiaryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class DiaryActivity : AppCompatActivity() {
  private lateinit var binding: ActivityDiaryBinding
  private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
  private val auth: FirebaseAuth = FirebaseAuth.getInstance()
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityDiaryBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    val data = intent.getParcelableExtra<DiaryModel>("diaryData")
    binding.appBarPrev.setOnClickListener { finish() }
    if (data != null) {
      binding.appBarBtn.visibility = if (data.postAuthorID == auth.currentUser?.uid) View.VISIBLE else View.GONE
      binding.appBarTitle.text =
        SimpleDateFormat("yyyy-MM-dd (EEE)", Locale.US).format(data.postTime.toDate()).uppercase()
      binding.diaryPostTitle.text = data.postTitle
      binding.diaryPostContent.text = data.postContent
      if (data.postImage.isEmpty()) {
        binding.diaryPostImageLayout.visibility = View.GONE
      } else {
        binding.diaryPostImageLayout.visibility = View.VISIBLE
        Glide.with(this)
          .load(data.postImage)
          .into(binding.diaryPostImage)
      }

      db.collection("Users")
        .document(data.postAuthorID)
        .get()
        .addOnSuccessListener { value ->
          if (value.data != null) {

          }
          binding.diaryUserName.text = value.get("userName") as String
          if (value.get("userImagePath") == null) {
            binding.diaryUserImage.background = ContextCompat.getDrawable(this, R.drawable.ic_user_profile)
            binding.diaryUserImageName.text = value.get("userName") as String
            binding.diaryUserImageName.visibility = View.VISIBLE
          } else {
            Glide.with(this)
              .asBitmap()
              .load(value.get("userImagePath") as String)
              .circleCrop()
              .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                  val drawable: Drawable = BitmapDrawable(resources, resource)
                  binding.diaryUserImage.background = drawable
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
              })
            binding.diaryUserImageName.visibility = View.GONE
          }
        }

      binding.appBarBtn.setOnClickListener {
        val diaryManageBottomSheet = DiaryManageBottomSheet()
        diaryManageBottomSheet.setOnUserSelectedListener { selectedPosition ->
          when (selectedPosition) {
            0 -> {
              val intent = Intent(this, DiaryFormActivity::class.java)
              intent.putExtra("diaryData", data)
              startActivity(intent)
              finish()
            }
            1 -> {
              db.collection("Calendars")
                .document(Application.prefs.getString("currentCalendar", auth.currentUser!!.uid))
                .collection("Posts")
                .document(data.postID)
                .delete()
                .addOnSuccessListener {
                  Toast.makeText(this, "기록이 삭제되었습니다!", Toast.LENGTH_SHORT).show()
                  finish()
                }
            }
          }
        }
        diaryManageBottomSheet.show(supportFragmentManager, "diaryManage")
      }
    }
  }
}