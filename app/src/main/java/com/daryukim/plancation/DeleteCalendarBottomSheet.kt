package com.daryukim.plancation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.daryukim.plancation.databinding.SheetDeleteAccountBinding
import com.daryukim.plancation.databinding.SheetDeleteCalendarBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DeleteCalendarBottomSheet: BottomSheetDialogFragment() {
  private var _binding: SheetDeleteCalendarBinding? = null
  private val binding get() = _binding!!

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    _binding = SheetDeleteCalendarBinding.inflate(inflater, container, false)
    val view = binding.root

    binding.sheetCancelBtn.setOnClickListener {
      dismiss()
    }

    binding.sheetDeleteBtn.setOnClickListener {
      dismiss()
    }

    return view
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