package com.daryukim.plancation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.daryukim.plancation.databinding.SheetCreateCalendarBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.GeoPoint
import java.util.*
import kotlin.collections.HashMap

class CreateCalendarBottomSheet : BottomSheetDialogFragment() {
  private var _binding: SheetCreateCalendarBinding? = null
  private val binding get() = _binding!!
  private lateinit var onFormSubmittedListener: (Boolean) -> Unit

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
    _binding = SheetCreateCalendarBinding.inflate(inflater, container, false)
    return binding.root
  }

  private fun setupClickListeners() {
    binding.createCalendarBarCancelButton.setOnClickListener { onCancelButtonClick() }
    binding.createCalendarBarSubmitButton.setOnClickListener { onSubmitButtonClick() }
  }

  private fun onCancelButtonClick() {
    dismiss()
  }

  private fun onSubmitButtonClick() {
    if (binding.createCalendarContentTitle.text.isNotEmpty()) {
      addCalendarToFirestore()
    } else {
      Toast.makeText(requireContext(), "할 일 제목을 입력해주세요!", Toast.LENGTH_SHORT).show()
    }
  }

  private fun addCalendarToFirestore() {
    val calendarID = UUID.randomUUID().toString()
    val calendarData = createCalendarData(calendarID)

    Application.db.collection("Calendars")
      .document(calendarID)
      .set(calendarData)
      .addOnSuccessListener {
        Toast.makeText(requireContext(), "캘린더를 성공적으로 생성했습니다!", Toast.LENGTH_SHORT).show()
        onFormSubmittedListener(true)
        dismiss()
      }
      .addOnFailureListener {
        Toast.makeText(requireContext(), "캘린더를 생성하지 못했습니다!", Toast.LENGTH_SHORT).show()
      }
  }

  private fun createCalendarData(calendarID: String): HashMap<String, Any?> {
    return hashMapOf(
      "calendarID" to calendarID,
      "calendarTitle" to binding.createCalendarContentTitle.text.toString(),
      "calendarUsers" to arrayListOf<String>(Application.auth.currentUser!!.uid),
      "calendarAuthorID" to Application.auth.currentUser!!.uid,
    )
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

  fun setOnFormSubmittedListener(listener: (Boolean) -> Unit) {
    onFormSubmittedListener = listener
  }
}
