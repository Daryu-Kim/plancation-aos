package com.daryukim.plancation

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.daryukim.plancation.databinding.ActivityDeepLinkBinding
import com.daryukim.plancation.databinding.ActivityDiaryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class DeepLinkActivity : AppCompatActivity() {
  private lateinit var binding: ActivityDeepLinkBinding
  private var calendarID: String = ""
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityDeepLinkBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    binding.deepLinkCancel.setOnClickListener {
      finish()
    }

    binding.deepLinkSubmit.setOnClickListener {
      Application.db.collection("Calendars")
        .document(calendarID)
        .update("calendarUsers", FieldValue.arrayUnion(Application.auth.currentUser!!.uid))
        .addOnSuccessListener {
          Toast.makeText(this, "초대에 응해주셔서 감사합니다!", Toast.LENGTH_SHORT).show()
          val intent = Intent(this, MainActivity::class.java)
          intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
          startActivity(intent)
        }
    }

    Firebase.dynamicLinks
      .getDynamicLink(intent)
      .addOnSuccessListener(this) { pendingDynamicLinkData ->
        if (pendingDynamicLinkData != null) {
          if (Application.auth.currentUser != null) {
            val deepLink = pendingDynamicLinkData.link
            handleDeepLink(deepLink)
          } else {
            Toast.makeText(this, "로그인을 먼저 진행하신 후 초대 링크에 응해주세요!", Toast.LENGTH_SHORT).show()
            finish()
          }

        } else {
          // 앱 링크가 없습니다.
        }
      }
      .addOnFailureListener(this) {
        // 동적 링크를 처리하는 동안 오류 발생
      }
  }

  @SuppressLint("SetTextI18n")
  private fun handleDeepLink(link: Uri?) {
    link?.getQueryParameter("referrerId")?.let { referrerId ->
      calendarID = referrerId
      binding.deepLinkName.text = "안녕하세요 ${Application.auth.currentUser!!.displayName}님!"

      Application.db.collection("Calendars")
        .document(referrerId)
        .get()
        .addOnSuccessListener { calendar ->
          if (calendar != null) {
            binding.deepLinkTitle.text = "${calendar.data!!["calendarTitle"]} 캘린더에서\n당신을 초대합니다!"
            binding.deepLinkCalendar.text = "계획 세우기에 어려움을 겪고 있을 당신을 위해 ${calendar.data!!["calendarTitle"]} 캘린더에 초대하고자 이 메시지를 전달 드립니다."
            Application.db.collection("Users")
              .document(calendar.data!!["calendarAuthorID"].toString())
              .get()
              .addOnSuccessListener { author ->
                if (author != null) {
                  binding.deepLinkAuthor.text = "캘린더 관리자 ${author.data!!["userName"]} 드림"
                }
              }
          }
        }
      // referrerId와 함께 필요한 작업을 수행합니다.
    }
  }
}