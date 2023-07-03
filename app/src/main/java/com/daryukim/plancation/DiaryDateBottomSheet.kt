package com.daryukim.plancation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.daryukim.plancation.databinding.SheetDiaryDateBinding
import com.daryukim.plancation.databinding.SheetUserSelectorBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

class DiaryDateBottomSheet() : BottomSheetDialogFragment() {
  private var _binding: SheetDiaryDateBinding? = null
  private val binding get() = _binding!!
  private lateinit var onDateSelectedListener: (LocalDate) -> Unit

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    _binding = SheetDiaryDateBinding.inflate(inflater, container, false)
    val view = binding.root

    binding.userSelectorFormBarCancelButton.setOnClickListener {
      dismiss()
    }

    binding.diaryDateFormBarSubmitButton.setOnClickListener {
      val selectedDate = LocalDate.of(binding.diaryDatePickerYear.value, binding.diaryDatePickerMonth.value, 1)
      onDateSelectedListener(selectedDate)
      dismiss()
    }

    setupDatePicker()

    return view
  }

  private fun setupDatePicker() {
    // Set up month picker
    val currentYear = DiaryUtil.selectedDate.value!!.year
    val currentMonth = DiaryUtil.selectedDate.value!!.monthValue

    val monthFormatter = SimpleDateFormat("MMM", Locale.getDefault())
    val months = (0..11).map { month -> monthFormatter.format(GregorianCalendar(currentYear, month - 1, 1).time) }

    binding.diaryDatePickerMonth.minValue = 0
    binding.diaryDatePickerMonth.maxValue = 11
    binding.diaryDatePickerMonth.displayedValues = months.toTypedArray()
    binding.diaryDatePickerMonth.wrapSelectorWheel = true

    // Set up year picker
    binding.diaryDatePickerYear.minValue = currentYear - 100 // 100 years ago
    binding.diaryDatePickerYear.maxValue = currentYear // current year
    binding.diaryDatePickerYear.wrapSelectorWheel = false

    // Set defaults (optional)
    binding.diaryDatePickerMonth.value = currentMonth
    binding.diaryDatePickerYear.value = currentYear
  }

  fun setOnDateSelectedListener(listener: (LocalDate) -> Unit) {
    onDateSelectedListener = listener
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