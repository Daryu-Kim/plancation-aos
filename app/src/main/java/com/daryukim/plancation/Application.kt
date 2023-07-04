package com.daryukim.plancation

import android.annotation.SuppressLint
import android.app.Application
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

open class Application: Application() {
  companion object {
    lateinit var prefs: PreferenceUtil
    @SuppressLint("StaticFieldLeak")
    lateinit var db: FirebaseFirestore
    lateinit var auth: FirebaseAuth
  }

  override fun onCreate() {
    prefs = PreferenceUtil(applicationContext)
    db = FirebaseFirestore.getInstance()
    auth = FirebaseAuth.getInstance()
    super.onCreate()
  }
}