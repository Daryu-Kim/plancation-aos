package com.daryukim.plancation

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.daryukim.plancation.databinding.ActivityJoinBinding
import com.daryukim.plancation.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest

class JoinActivity : AppCompatActivity() {
  private lateinit var binding: ActivityJoinBinding
  private lateinit var auth: FirebaseAuth
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityJoinBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    auth = FirebaseAuth.getInstance()

    binding.joinButton.setOnClickListener {
      createAccountWithEmail()
    }
  }

  private fun createAccountWithEmail() {
    if (
      binding.formId.text.toString().isEmpty() ||
      binding.formName.text.toString().isEmpty() ||
      binding.formPw.text.toString().isEmpty() ||
      binding.formPwRe.text.toString().isEmpty()
    ) {
      Toast.makeText(this, "모든 항목을 다 입력해주세요!", Toast.LENGTH_SHORT).show()
      return
    }

    if (binding.formPw.text.toString() != binding.formPwRe.text.toString()) {
      Toast.makeText(this, "비밀번호가 일치하지 않습니다!", Toast.LENGTH_SHORT).show()
      return
    }

    auth.createUserWithEmailAndPassword(
      binding.formId.text.toString(),
      binding.formPw.text.toString()
    )
      .addOnCompleteListener(this) { task ->
        if (task.isSuccessful) {
          changeUserName(auth.currentUser)
          createUserCalendar()
        } else {
          Toast.makeText(this, "회원가입 실패: " + task.exception?.message, Toast.LENGTH_SHORT).show()
        }
      }
  }

  private fun createUserCalendar() {
    Application.db.collection("Calendars")
      .document(Application.auth.currentUser!!.uid)
      .set(mapOf(
        "calendarUsers" to listOf(Application.auth.currentUser!!.uid),
        "calendarAuthorID" to Application.auth.currentUser!!.uid,
        "calendarTitle" to "내 캘린더",
        "calendarID" to Application.auth.currentUser!!.uid
      ))
      .addOnSuccessListener {
        Application.db.collection("Users")
          .document(Application.auth.currentUser!!.uid)
          .set(mapOf(
            "userID" to Application.auth.currentUser!!.uid,
            "userImagePath" to null,
            "userName" to binding.formName.text.toString()
          ))
          .addOnSuccessListener {
            Application.prefs.setString("currentCalendar", auth.currentUser!!.uid)
            startActivity(Intent(this, MainActivity::class.java))
          }
      }
  }

  private fun changeUserName(user: FirebaseUser?) {
    if (user != null) {
      val profileUpdates = UserProfileChangeRequest.Builder()
        .setDisplayName(binding.formName.text.toString())
        .build()

      user.updateProfile(profileUpdates)
        .addOnCompleteListener(this) { task ->
          if (task.isSuccessful) {
            Log.d("TAG", "User Name Updated!")
          } else {
            Log.e("Tag", "User Name Not Updated!")
          }
        }
    }
  }
}