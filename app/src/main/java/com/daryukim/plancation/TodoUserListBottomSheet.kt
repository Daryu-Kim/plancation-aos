package com.daryukim.plancation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.daryukim.plancation.databinding.FragmentTodoUserListBinding
import com.daryukim.plancation.databinding.SheetColorPickerBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.time.LocalDate

class TodoUserListBottomSheet: BottomSheetDialogFragment() {
  private var _binding: FragmentTodoUserListBinding? = null
  private val binding get() = _binding!!
  private var data: ScheduleModel = ScheduleModel()

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    _binding = FragmentTodoUserListBinding.inflate(inflater, container, false)
    val view = binding.root

    setupArguments()
    setUpTodoUserView(data.eventUsers, data.eventCheckUsers)

    return view
  }

  private fun setupArguments() {
    arguments?.let {
      data = it.getParcelable("data")!!
    }
  }

  private fun setUpTodoUserView(userList: ArrayList<String>, userCheckList: ArrayList<String>) {
    val gridItemWidth = resources.getDimension(R.dimen.todo_user_width) / resources.displayMetrics.density
    val deviceWidth = resources.displayMetrics.widthPixels / resources.displayMetrics.density
    val spanCount = (deviceWidth / gridItemWidth).toInt()

    binding.todoUserListView.apply {
      layoutManager = GridLayoutManager(context, spanCount)
      adapter = TodoUserAdapter(userList, userCheckList)
    }
  }

  companion object {
    // 팩토리 함수를 사용하여 프로퍼티값을 지정할 수 있는 프래그먼트 인스턴스를 생성합니다.
    fun newInstance(data: ScheduleModel) =
      TodoUserListBottomSheet().apply {
        arguments = Bundle().apply {
          putParcelable("data", data)
        }
      }
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