package com.daryukim.plancation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.daryukim.plancation.databinding.ActivityChangePwBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ChangePWActivity : AppCompatActivity() {
  private lateinit var binding: ActivityChangePwBinding
  private lateinit var auth: FirebaseAuth

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityChangePwBinding.inflate(layoutInflater)
    setContentView(binding.root)

    auth = FirebaseAuth.getInstance()
    binding.sendButton.setOnClickListener { changePassword() }
  }

  private fun changePassword() {
    val currentPassword = binding.formPwCurrent.text.toString()
    if (currentPassword.isEmpty()) {
      showError(binding.formPwCurrentError, "현재 비밀번호를 입력해주세요!")
      return
    }

    val newPwd = binding.formPw.text.toString()
    if (newPwd.isEmpty()) {
      showError(binding.formPwError, "새로운 비밀번호를 입력해주세요!")
      return
    }

    val confirmationPwd = binding.formPwc.text.toString()
    if (newPwd != confirmationPwd) {
      showError(binding.formPwcError, "비밀번호가 일치하지 않습니다. 다시 확인해주세요!")
      return
    }

    val emailCredential = EmailAuthProvider.getCredential(auth.currentUser!!.email!!, currentPassword)
    auth.currentUser!!.reauthenticate(emailCredential).addOnCompleteListener { task ->
      if (task.isSuccessful) {
        hideError(binding.formPwCurrentError)
        auth.currentUser!!.updatePassword(newPwd).addOnCompleteListener { passwordTask ->
          if (passwordTask.isSuccessful) {
            Toast.makeText(this, "비밀번호를 성공적으로 변경했습니다!", Toast.LENGTH_SHORT).show()
            finish()
          } else {
            Toast.makeText(this, "비밀번호 변경에 실패했습니다!", Toast.LENGTH_SHORT).show()
          }
        }
      } else {
        showError(binding.formPwCurrentError, "비밀번호가 일치하지 않습니다. 다시 확인해주세요!")
      }
    }
  }

  private fun showError(view: View, message: String) {
    if (view is TextView) {
      view.text = message
    }
    view.visibility = View.VISIBLE
  }

  private fun hideError(view: View) {
    view.visibility = View.GONE
  }
}
