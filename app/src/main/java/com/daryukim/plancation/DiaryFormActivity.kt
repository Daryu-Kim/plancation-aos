package com.daryukim.plancation

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.daryukim.plancation.databinding.ActivityDiaryBinding
import com.daryukim.plancation.databinding.ActivityDiaryFormBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class DiaryFormActivity : AppCompatActivity() {
  private lateinit var binding: ActivityDiaryFormBinding
  private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
  private val auth: FirebaseAuth = FirebaseAuth.getInstance()
  private var imageUri: Uri? = null
  private var data: DiaryModel? = null

  private val pickImagesLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
    uri?.let { imageUri = uri  }
  }
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityDiaryFormBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    data = intent.getParcelableExtra("diaryData")
    val isModify = data != null

    binding.diaryFormImage.setOnClickListener {
      pickImagesLauncher.launch("image/*")
    }
    binding.appBarCancel.setOnClickListener { finish() }
    binding.appBarSubmit.setOnClickListener {
      if (!isModify) data = data!!.copy(postID = UUID.randomUUID().toString(), postTime = Timestamp.now())
      data = data!!.copy(
        postTitle = binding.diaryFormTitle.text.toString(),
        postContent = binding.diaryFormContent.text.toString(),
        postAuthorID = auth.currentUser?.uid.toString(),
      )

      CoroutineScope(Dispatchers.Main).launch {
        uploadImageToFirebaseStorage(imageUri)
        setFirestoreData(isModify)
      }
    }

    if (data != null) {
      binding.appBarSubmit.text = "수정"
      binding.appBarTitle.text = "게시물 수정"

      binding.diaryFormTitle.setText(data!!.postTitle)
      binding.diaryFormContent.setText(data!!.postContent)
      Glide.with(this)
        .load(data!!.postImage)
        .into(binding.diaryFormImage)
    } else {
      binding.appBarSubmit.text = "등록"
      binding.appBarTitle.text = "게시물 등록"
      data = DiaryModel()
    }
  }

  private fun setFirestoreData(isModify: Boolean) {
    db.collection("Calendars")
      .document("A9PHFsmDLUWbaYDdy2XX")
      .collection("Posts")
      .document(data!!.postID)
      .set(data!!)
      .addOnSuccessListener {
        Toast.makeText(
          this,
          if (isModify) "게시물이 수정되었습니다!" else "게시물이 등록되었습니다!",
          Toast.LENGTH_SHORT
        ).show()
        finish()
      }
  }

  private fun uploadImageToFirebaseStorage(uri: Uri?) {
    if (uri != null) {
      val storageReference = FirebaseStorage.getInstance().reference
      val imageRef = storageReference.child("Posts/${data!!.postID}/post_image.png")

      // 이미지를 Firebase Storage에 업로드
      imageRef.putFile(uri)
        .addOnSuccessListener { uploadTask ->
          // 업로드 되었기 때문에, 이미지의 다운로드 URL을 얻어 Firestore에 저장
          imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
            updateFirestoreWithImageUrl(downloadUri.toString())
          }
        }
        .addOnFailureListener { exception ->
          // 업로드 실패시 처리
        }
    } else {
      return
    }

  }

  private fun updateFirestoreWithImageUrl(imageUrl: String) {
    // "userImagePath" 필드에 이미지 URL을 저장한다.
    db.collection("Users")
      .document(auth.currentUser?.uid.toString())
      .update("userImagePath", imageUrl)
      .addOnSuccessListener {
        data = data!!.copy(postImage = imageUrl)
      }
  }
}