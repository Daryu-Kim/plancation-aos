package com.daryukim.plancation

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.daryukim.plancation.databinding.ActivityLoginBinding
import com.daryukim.plancation.databinding.ActivityLoginmainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
  private lateinit var binding: ActivityLoginBinding
  private lateinit var auth: FirebaseAuth
  private lateinit var account: GoogleSignInAccount

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityLoginBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    auth = Firebase.auth

    binding.formLoginBtn.setOnClickListener {
      loginWithEmail()
    }

    binding.formFind.setOnClickListener {
      startActivity(Intent(this, FindPWActivity::class.java))
    }

    binding.socialJoinButton.setOnClickListener {
      startActivity(Intent(this, JoinActivity::class.java))
    }

    binding.socialButtonLayout.setOnClickListener {
      signInWithGoogle()
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    if (requestCode == RC_GOOGLE_SIGN_IN) {
      val task = GoogleSignIn.getSignedInAccountFromIntent(data)
      try {
        account = task.getResult(ApiException::class.java)
        firebaseAuthWithGoogle(account.idToken!!)
      } catch (e: ApiException) {
        Log.w("TAG", "Google Sign-In Failed", e)
      }
    }
  }

  private fun loginWithEmail() {
    if (binding.formId.text.toString().isEmpty() || binding.formPw.text.toString().isEmpty()) {
      Toast.makeText(this, "아이디와 비밀번호는 필수 입력사항입니다!", Toast.LENGTH_SHORT).show()
    } else {
      auth.signInWithEmailAndPassword(
        binding.formId.text.toString(),
        binding.formPw.text.toString()
      )
        .addOnCompleteListener(this) { task ->
          if (task.isSuccessful) {
            Application.prefs.setString("currentCalendar", auth.currentUser!!.uid)
            startActivity(Intent(this, MainActivity::class.java))
          } else {
            Toast.makeText(this, "로그인 실패: " + task.exception?.message, Toast.LENGTH_SHORT).show()
          }
        }
    }
  }

  private fun createGoogleSignInOptions(): GoogleSignInOptions {
    return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
      .requestIdToken(getString(R.string.default_web_client_id))
      .requestEmail()
      .build()
  }

  private fun signInWithGoogle() {
    val signInOptions = createGoogleSignInOptions()
    val signInClient = GoogleSignIn.getClient(this, signInOptions)
    val signInIntent = signInClient.signInIntent
    startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
  }

  private fun firebaseAuthWithGoogle(idToken: String) {
    val credential = GoogleAuthProvider.getCredential(idToken, null)
    auth.signInWithCredential(credential)
      .addOnCompleteListener(this) { task ->
        if (task.isSuccessful) {
          if (auth.currentUser != null) {
            Application.db.collection("Users")
              .document(Application.auth.currentUser!!.uid)
              .get()
              .addOnSuccessListener {
                if (it.exists()) {
                  Application.prefs.setString("currentCalendar", auth.currentUser!!.uid)
                  startActivity(Intent(this, MainActivity::class.java))
                } else {
                  Application.db.collection("Calendars")
                    .document(Application.auth.currentUser!!.uid)
                    .set(
                      mapOf(
                        "calendarUsers" to listOf(Application.auth.currentUser!!.uid),
                        "calendarAuthorID" to Application.auth.currentUser!!.uid,
                        "calendarTitle" to "내 캘린더",
                        "calendarID" to Application.auth.currentUser!!.uid
                      )
                    )
                    .addOnSuccessListener {
                      Application.db.collection("Users")
                        .document(Application.auth.currentUser!!.uid)
                        .set(
                          mapOf(
                            "userID" to auth.currentUser!!.uid,
                            "userImagePath" to null,
                            "userName" to account.displayName
                          )
                        )
                        .addOnSuccessListener {
                          Application.prefs.setString("currentCalendar", auth.currentUser!!.uid)
                          startActivity(Intent(this, MainActivity::class.java))
                        }

                    }
                }
              }
          }
        } else {
          Log.w("TAG", "signInWithCredential:failure", task.exception)
        }
      }
  }

  companion object {
    private const val RC_GOOGLE_SIGN_IN = 9001
  }
}