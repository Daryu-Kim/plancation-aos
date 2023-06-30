package com.daryukim.plancation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.daryukim.plancation.databinding.SheetUserSelectorBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore

class UserSelectorBottomSheet(eventUsers: ArrayList<String>) : BottomSheetDialogFragment() {
  private var _binding: SheetUserSelectorBinding? = null
  private val binding get() = _binding!!
  private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
  private var calendarUserList: ArrayList<String> = arrayListOf()
  private var checkedUserList: ArrayList<String> = arrayListOf()
  private lateinit var eventUsers: ArrayList<String>
  private lateinit var onUserSelectedListener: (ArrayList<String>) -> Unit

  init {
    this.eventUsers = eventUsers
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    _binding = SheetUserSelectorBinding.inflate(inflater, container, false)
    val view = binding.root

    db.collection("Calendars")
      .document("A9PHFsmDLUWbaYDdy2XX")
      .get()
      .addOnSuccessListener { calendar ->
        if (calendar != null) {
          calendarUserList = (calendar.data!!["users"] as ArrayList<String>)
          setupUserSelector()
        }
      }

    binding.userSelectorFormBarCancelButton.setOnClickListener {
      dismiss()
    }

    binding.userSelectorFormBarSubmitButton.setOnClickListener {
      onUserSelectedListener(checkedUserList)
      dismiss()
    }

    return view
  }

  private fun setupUserSelector() {
    val userSelectorItemAdapter = UserSelectorItemAdapter(calendarUserList, eventUsers)
    binding.userSelectorFormContentRv.apply {
      layoutManager = LinearLayoutManager(requireContext())
      adapter = userSelectorItemAdapter
    }

    userSelectorItemAdapter.setOnUserSelectedListener { selectedUsers ->
      checkedUserList = selectedUsers
    }
  }

  fun setOnUserSelectedListener(listener: (ArrayList<String>) -> Unit) {
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