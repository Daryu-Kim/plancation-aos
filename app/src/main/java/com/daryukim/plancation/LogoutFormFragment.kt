package com.daryukim.plancation

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.DatePicker
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.daryukim.plancation.databinding.FragmentLogoutFormBinding
import com.daryukim.plancation.databinding.FragmentScheduleFormBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.ktx.Firebase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit.DAYS
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.max

class LogoutFormFragment : BottomSheetDialogFragment() {
  private var _binding: FragmentLogoutFormBinding? = null
  private val binding get() = _binding!!
  private val auth: FirebaseAuth

  init {
    auth = Firebase.auth
  }

  // 프래그먼트 생성 시 뷰 설정
  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    val view = inflateBinding(inflater, container)
    setupClickListeners()
    return view
  }

  private fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): View {
    _binding = FragmentLogoutFormBinding.inflate(inflater, container, false)
    return binding.root
  }

  private fun setupClickListeners() {
    binding.logoutFormCancel.setOnClickListener { onCancelButtonClick() }
    binding.logoutFormSubmit.setOnClickListener { onSubmitButtonClick() }
  }

  private fun onCancelButtonClick() {
    dismiss()
  }

  private fun onSubmitButtonClick() {
    auth.signOut()
    dismiss()
    startActivity(Intent(requireContext(), LoginMainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
    Toast.makeText(requireContext(), "성공적으로 로그아웃 하였습니다!", Toast.LENGTH_SHORT).show()
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
  }

  // 프래그먼트가 파괴될 때 바인딩을 해제합니다.
  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  override fun getTheme(): Int {
    return R.style.BottomSheetDialogStyle
  }
}
