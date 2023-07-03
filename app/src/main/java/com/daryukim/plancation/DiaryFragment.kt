package com.daryukim.plancation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.daryukim.plancation.databinding.FragmentDiaryBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class DiaryFragment: Fragment() {
  private var _binding: FragmentDiaryBinding? = null
  private val binding get() = _binding!!
  private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
  private val auth: FirebaseAuth = FirebaseAuth.getInstance()
  private var diaryItemList: ArrayList<DiaryModel> = ArrayList()

  // 프래그먼트 생성 시 뷰 설정
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    // 바인딩 객체를 생성하고 화면 레이아웃을 동적으로 연결합니다.
    _binding = FragmentDiaryBinding.inflate(inflater, container, false)
    val view = binding.root
    DiaryUtil.selectedDate.value = LocalDate.of(LocalDate.now().year, LocalDate.now().month, 1)

    binding.diaryDateButton.setOnClickListener {
      val diaryDateBottomSheet = DiaryDateBottomSheet()
      diaryDateBottomSheet.setOnDateSelectedListener { selectedDate ->
        DiaryUtil.selectedDate.value = selectedDate
        Toast.makeText(requireContext(), DiaryUtil.selectedDate.value.toString(), Toast.LENGTH_SHORT).show()
        setupDiaryItems()
      }
      diaryDateBottomSheet.show(parentFragmentManager, "diaryDate")
    }

    binding.diaryAddButton.setOnClickListener {
      startActivity(Intent(requireContext(), DiaryFormActivity::class.java))
    }

    binding.diaryPostLayout.setOnRefreshListener {
      setupDiaryItems()
    }

    return view
  }

  private fun setupDiaryItems() {
    binding.diaryDateView.text = DateTimeFormatter.ofPattern("yyyy년 MM월").format(DiaryUtil.selectedDate.value)
    diaryItemList.clear()
    fetchPostsDataFromFirestore { result ->
      diaryItemList.addAll(result)
      setUpDiaryListView()
      binding.diaryPostLayout.isRefreshing = false
    }
  }

  private fun setUpDiaryListView() {
    val diaryItemAdapter = DiaryItemAdapter(diaryItemList)
    diaryItemAdapter.setOnClickListener { diaryModel ->
      val intent = Intent(requireContext(), DiaryActivity::class.java)
      intent.putExtra("diaryData", diaryModel)
      startActivity(intent)
    }
    binding.diaryPostView.apply {
      layoutManager = LinearLayoutManager(requireContext())
      adapter = diaryItemAdapter
    }
    checkEmptyList()
  }

  private fun fetchPostsDataFromFirestore(onComplete: (List<DiaryModel>) -> Unit) {
    val date = DiaryUtil.selectedDate.value!!
    val startDateTime: LocalDateTime = date.atStartOfDay()
    val startDate: Date = Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant())
    val endLocalDate: LocalDate = date.plusMonths(1)
    val endDateTime: LocalDateTime = endLocalDate.atStartOfDay()
    val endDate: Date = Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant())
    val dataList = mutableListOf<DiaryModel>()

    db.collection("Calendars")
      .document("A9PHFsmDLUWbaYDdy2XX")
      .collection("Posts")
      .whereGreaterThanOrEqualTo("postTime", Timestamp(startDate))
      .whereLessThan("postTime", Timestamp(endDate))
      .orderBy("postTime", Query.Direction.DESCENDING)
      .get()
      .addOnSuccessListener { documents ->
        for (document in documents) {
          dataList.add(DiaryModel.fromDocument(document.data))
        }
        onComplete(dataList)

      }
      .addOnFailureListener {
        onComplete(listOf())
      }
  }

  private fun checkEmptyList() {
    if (DiaryItemAdapter(diaryItemList).itemCount == 0) { // 항목이 없는 경우
      binding.diaryItemEmptyLayout.visibility = View.VISIBLE
    } else {
      binding.diaryItemEmptyLayout.visibility = View.GONE
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
  }

  override fun onResume() {
    super.onResume()
    setupDiaryItems()
  }

  // 프래그먼트가 파괴될 때 바인딩을 해제합니다.
  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}
