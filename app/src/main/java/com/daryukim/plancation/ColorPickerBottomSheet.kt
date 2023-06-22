package com.daryukim.plancation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.daryukim.plancation.databinding.SheetColorPickerBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ColorPickerBottomSheet: BottomSheetDialogFragment() {
  private var _binding: SheetColorPickerBinding? = null
  private val binding get() = _binding!!
  private lateinit var onColorSelectedListener: (Int) -> Unit

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    _binding = SheetColorPickerBinding.inflate(inflater, container, false)
    val view = binding.root

    binding.colorPickerFormBarCancelButton.setOnClickListener {
      dismiss()
    }

    binding.colorPickerFormBarSubmitButton.setOnClickListener {
      val color = binding.colorPickerFormContentPicker.selectedColor
      onColorSelectedListener(color)
      dismiss()
    }

    return view
  }

  fun setOnColorSelectedListener(listener: (Int) -> Unit) {
    onColorSelectedListener = listener
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