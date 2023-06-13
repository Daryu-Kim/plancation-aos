package com.daryukim.plancation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.daryukim.plancation.databinding.ActivityFindpwBinding
import com.daryukim.plancation.databinding.ActivityJoinBinding
import com.google.firebase.auth.FirebaseAuth

class FindPWActivity : AppCompatActivity() {
  private lateinit var binding: ActivityFindpwBinding
  private lateinit var auth: FirebaseAuth
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityFindpwBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    auth = FirebaseAuth.getInstance()

    binding.sendButton.setOnClickListener {
      sendPasswordResetEmail()
    }
  }

  private fun sendPasswordResetEmail() {
    if (binding.formId.text.toString().isNotEmpty()) {
      auth.sendPasswordResetEmail(binding.formId.text.toString())
        .addOnCompleteListener { task ->
          if (task.isSuccessful) {
            Toast.makeText(this, "비밀번호 재설정 이메일이 전송되었습니다!", Toast.LENGTH_SHORT).show()
          } else {
            Toast.makeText(this, "비밀번호 재설정 이메일 전송에 실패했습니다!", Toast.LENGTH_SHORT).show()
          }
        }
    } else {
      Toast.makeText(this, "이메일 주소를 입력해주세요!", Toast.LENGTH_SHORT).show()
    }
  }
}