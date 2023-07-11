package com.daryukim.plancation

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalTime

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
  private val SPLASH_TIME_OUT: Long = 2000

  private fun isUserLoggedIn(): Boolean {
    val firebaseAuth = FirebaseAuth.getInstance()
    val currentUser = firebaseAuth.currentUser
    return currentUser != null
  }
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_splash)

    Handler(Looper.getMainLooper()).postDelayed({
      if (Application.prefs.getBoolean("isFirstRunKey", true)) {
        val time: LocalTime = LocalTime.of(9,0)
        Application.prefs.setBoolean("isFirstRunKey", false)
        Application.prefs.setString("alertTime", time.toString())
      }

      if (isUserLoggedIn()) {
        startActivity(Intent(this, MainActivity::class.java))
      } else {
        startActivity(Intent(this, LoginMainActivity::class.java))
      }
      finish()
    }, SPLASH_TIME_OUT)
  }
}