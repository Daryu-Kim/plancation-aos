package com.daryukim.plancation

import android.annotation.SuppressLint
import android.graphics.drawable.GradientDrawable.Orientation
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.daryukim.plancation.databinding.FragmentCalendarBinding
import com.daryukim.plancation.databinding.FragmentTodoBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class TodoFragment: Fragment() {
  private var _binding: FragmentTodoBinding? = null
  private val binding get() = _binding!!
  private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
  private var todoItemList: ArrayList<ScheduleModel> = ArrayList()

  // 프래그먼트 생성 시 뷰 설정
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    // 바인딩 객체를 생성하고 화면 레이아웃을 동적으로 연결합니다.
    _binding = FragmentTodoBinding.inflate(inflater, container, false)
    val view = binding.root
    TodoUtil.selectedDate.value = LocalDate.now()

    TodoUtil.selectedDate.observe(viewLifecycleOwner, Observer { value ->
      binding.todoDateTitle.text = TodoUtil.selectedDate.value!!.format(DateTimeFormatter.ofPattern("yyyy년 MM월"))
      todoItemList.clear()
      fetchEventsDataFromFirestore { result ->
        todoItemList.addAll(result)
        setUpTodoListView()
      }
    })

    setUpTodoDateView(setItems(TodoUtil.selectedDate.value!!))
    setUpTodoListView()

    binding.todoDatePrev.setOnClickListener {
      TodoUtil.selectedDate.value = TodoUtil.selectedDate.value!!.minusDays(7)
      setUpTodoDateView(setItems(TodoUtil.selectedDate.value!!))
    }
    binding.todoDateNext.setOnClickListener {
      TodoUtil.selectedDate.value = TodoUtil.selectedDate.value!!.plusDays(7)
      setUpTodoDateView(setItems(TodoUtil.selectedDate.value!!))
    }

    return view
  }

  private fun setItems(date: LocalDate): ArrayList<LocalDate> {
    return arrayListOf(
      date,
      date.plusDays(1),
      date.plusDays(2),
      date.plusDays(3),
      date.plusDays(4),
      date.plusDays(5),
      date.plusDays(6),
    )
  }

  private fun setUpTodoDateView(items: ArrayList<LocalDate>) {
    binding.todoDate.apply {
      layoutManager = NoScrollGridLayoutManager(context, 7)
      adapter = TodoDateAdapter(items)
    }
  }

  private fun setUpTodoListView() {
    binding.todoList.apply {
      layoutManager = LinearLayoutManager(requireContext())
      adapter = TodoItemAdapter(todoItemList)
    }
    checkEmptyList()
  }

  private fun fetchEventsDataFromFirestore(onComplete: (List<ScheduleModel>) -> Unit) {
    val date = TodoUtil.selectedDate.value!!
    val startDateTime: LocalDateTime = date.atStartOfDay()
    val startDate: Date = Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant())
    val endLocalDate: LocalDate = date.plusDays(1)
    val endDateTime: LocalDateTime = endLocalDate.atStartOfDay()
    val endDate: Date = Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant())
    val dataList = mutableListOf<ScheduleModel>()

    db.collection("Calendars")
      .document("A9PHFsmDLUWbaYDdy2XX")
      .collection("Events")
      .whereEqualTo("eventIsTodo", true)
      .whereGreaterThanOrEqualTo("eventTime", Timestamp(startDate))
      .whereLessThan("eventTime", Timestamp(endDate))
      .addSnapshotListener { snapshots, e ->
        if (e != null) {
          Toast.makeText(requireContext(), "할 일을 불러오지 못했습니다!", Toast.LENGTH_SHORT).show()
          return@addSnapshotListener
        }

        if (snapshots!!.documentChanges.size == 0) {
          onComplete(dataList)
        }

        for (dc in snapshots.documentChanges) {
          when (dc.type) {
            DocumentChange.Type.ADDED -> {
              dataList.add(ScheduleModel.fromDocument(dc.document.data))
//              setMonthView()
              onComplete(dataList)
            }

            DocumentChange.Type.MODIFIED -> {}

            DocumentChange.Type.REMOVED -> {
              dataList.remove(ScheduleModel.fromDocument(dc.document.data))
//              setMonthView()
              onComplete(dataList)
            }
          }
        }
      }
  }

  private fun checkEmptyList() {
    if (TodoItemAdapter(todoItemList).itemCount == 0) { // 항목이 없는 경우
      binding.todoItemEmptyLayout.visibility = View.VISIBLE
    } else {
      binding.todoItemEmptyLayout.visibility = View.GONE
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
  }

  // 프래그먼트가 파괴될 때 바인딩을 해제합니다.
  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  override fun onContextItemSelected(item: MenuItem): Boolean {
    val position = item.groupId
    when (item.itemId) {
      0 -> {
        return true
      }
      1 -> {
        // 삭제 버튼
        db.collection("Calendars")
          .document("A9PHFsmDLUWbaYDdy2XX")
          .collection("Events")
          .whereEqualTo("eventLinkID", todoItemList[position].eventLinkID)
          .get()
          .addOnSuccessListener { querySnapshot ->
            val batch = db.batch()

            querySnapshot.documents.forEach { documentSnapshot ->
              batch.delete(documentSnapshot.reference)
            }

            batch.commit()
              .addOnSuccessListener {
                Toast.makeText(requireContext(), "할 일을 삭제했습니다!", Toast.LENGTH_SHORT).show()
              }
              .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "할 일을 삭제하지 못했습니다!", Toast.LENGTH_SHORT).show()
              }
          }
          .addOnFailureListener { e ->
            Toast.makeText(requireContext(), "할 일을 삭제하지 못했습니다!", Toast.LENGTH_SHORT).show()
          }
        return true
      }
      else -> return super.onContextItemSelected(item)
    }
  }
}
