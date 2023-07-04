package com.daryukim.plancation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.daryukim.plancation.databinding.SheetDiaryManageBinding
import com.daryukim.plancation.databinding.SheetUserSelectorBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore

class DiaryManageBottomSheet() : BottomSheetDialogFragment() {
  private var _binding: SheetDiaryManageBinding? = null
  private val binding get() = _binding!!
  private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
  private lateinit var onUserSelectedListener: (Int) -> Unit

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    _binding = SheetDiaryManageBinding.inflate(inflater, container, false)
    val view = binding.root

    binding.diaryManageMenuEdit.setOnClickListener {
      onUserSelectedListener(0)
      dismiss()
    }

    binding.diaryManageMenuDelete.setOnClickListener {
      db.collection("Calendars")
        .document(Application.prefs.getString("currentCalendar", Application.auth.currentUser!!.uid))
        .collection("Posts")
      onUserSelectedListener(1)
      dismiss()
    }

    return view
  }

  fun setOnUserSelectedListener(listener: (Int) -> Unit) {
    onUserSelectedListener = listener
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