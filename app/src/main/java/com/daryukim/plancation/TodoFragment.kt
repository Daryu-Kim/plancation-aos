package com.daryukim.plancation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.daryukim.plancation.databinding.FragmentTodoBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class TodoFragment: Fragment() {
  private var _binding: FragmentTodoBinding? = null
  private val binding get() = _binding!!
  private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
  private val auth: FirebaseAuth = FirebaseAuth.getInstance()
  private var todoItemList: ArrayList<ScheduleModel> = ArrayList()

  // 프래그먼트 생성 시 뷰 설정
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    // 바인딩 객체를 생성하고 화면 레이아웃을 동적으로 연결합니다.
    _binding = FragmentTodoBinding.inflate(inflater, container, false)
    val view = binding.root
    val data: ScheduleModel? = arguments?.getParcelable("data")

    if (data != null) {
      TodoUtil.selectedDate.value = data.eventTime.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    } else {
      TodoUtil.selectedDate.value = LocalDate.now()
    }

    TodoUtil.selectedDate.observe(viewLifecycleOwner, Observer { value ->
      binding.todoDateTitle.text = TodoUtil.selectedDate.value!!.format(DateTimeFormatter.ofPattern("yyyy년 MM월"))
      setupTodoItems()
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

    binding.todoAddButton.setOnClickListener {
      val todoFormBottomSheet = TodoFormFragment()
      todoFormBottomSheet.setOnFormSubmittedListener { _ ->
        setupTodoItems()
      }
      todoFormBottomSheet.show(parentFragmentManager, "todoForm")
    }

    binding.todoListLayout.setOnRefreshListener {
      setupTodoItems()
    }

    return view
  }

  private fun setupTodoItems() {
    todoItemList.clear()
    fetchEventsDataFromFirestore { result ->
      todoItemList.addAll(result)
      setUpTodoListView()
      binding.todoListLayout.isRefreshing = false
    }
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
      .document(Application.prefs.getString("currentCalendar", auth.currentUser!!.uid))
      .collection("Events")
      .whereEqualTo("eventIsTodo", true)
      .whereGreaterThanOrEqualTo("eventTime", Timestamp(startDate))
      .whereLessThan("eventTime", Timestamp(endDate))
      .get()
      .addOnSuccessListener { documents ->
        if (documents != null) {
          for (document in documents) {
            dataList.add(ScheduleModel.fromDocument(document.data))
          }
          onComplete(dataList)
        } else {
          Application.prefs.setString("currentCalendar", Application.auth.currentUser!!.uid)
          setupTodoItems()
        }
      }
      .addOnFailureListener {
        onComplete(listOf())
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
        db.collection("Calendars")
          .document(Application.prefs.getString("currentCalendar", auth.currentUser!!.uid))
          .collection("Events")
          .document(todoItemList[position].eventID)
          .get()
          .addOnSuccessListener {document ->
            if (document != null) {
              val todoUserListFragment = TodoUserListBottomSheet.newInstance(
                data = ScheduleModel.fromDocument(document.data!!)
              )
              todoUserListFragment.show(childFragmentManager, todoUserListFragment.tag)
            }
          }
          .addOnFailureListener {  }

        return true
      }
      1 -> {
        if (auth.currentUser?.uid == todoItemList[position].eventAuthorID) {
          db.collection("Calendars")
            .document(Application.prefs.getString("currentCalendar", auth.currentUser!!.uid))
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
                  setupTodoItems()
                }
                .addOnFailureListener { e ->
                  Toast.makeText(requireContext(), "할 일을 삭제하지 못했습니다!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
              Toast.makeText(requireContext(), "할 일을 삭제하지 못했습니다!", Toast.LENGTH_SHORT).show()
            }
        } else {
          Toast.makeText(requireContext(), "작성자만 삭제할 수 있습니다!", Toast.LENGTH_SHORT).show()
        }
        return true
      }
      else -> return super.onContextItemSelected(item)
    }
  }
}
