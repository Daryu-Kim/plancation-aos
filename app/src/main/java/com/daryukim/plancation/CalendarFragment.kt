package com.daryukim.plancation

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.daryukim.plancation.databinding.FragmentCalendarBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class CalendarFragment: Fragment() {
  private var _binding: FragmentCalendarBinding? = null
  private val binding get() = _binding!!
  private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
  private val auth: FirebaseAuth = FirebaseAuth.getInstance()
  private var scheduleList: ArrayList<ScheduleModel> = ArrayList()
  private lateinit var scheduleAdapter: ScheduleAdapter
  private var selectedSchedule: Int = 0
  private var selectedScheduleData: ScheduleModel = ScheduleModel()

  // 프래그먼트 생성 시 뷰 설정
  @SuppressLint("ClickableViewAccessibility")
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    // 바인딩 객체를 생성하고 화면 레이아웃을 동적으로 연결합니다.
    _binding = FragmentCalendarBinding.inflate(inflater, container, false)
    val view = binding.root

    val data: ScheduleModel? = arguments?.getParcelable("data")

    if (data != null) {
      CalendarUtil.selectedDate.value = data.eventTime.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    } else {
      CalendarUtil.selectedDate.value = LocalDate.now()
    }

    // 월별 캘린더 뷰를 설정합니다.
    setMonthView()

    binding.calendarDatePrev.setOnClickListener {
      CalendarUtil.selectedDate.value = CalendarUtil.selectedDate.value!!.minusMonths(1)
      setMonthView()
    }

    binding.calendarDateNext.setOnClickListener {
      CalendarUtil.selectedDate.value = CalendarUtil.selectedDate.value!!.plusMonths(1)
      setMonthView()
    }

    // 선택된 날짜에 변화를 관찰하고, 변경 시 스케줄 날짜를 갱신합니다.
    CalendarUtil.selectedDate.observe(viewLifecycleOwner, Observer { value ->
      binding.scheduleDate.text = yearMonthFromDate(value)
      setupScheduleItems()
    })

    binding.scheduleAddButton.setOnClickListener {_ ->
      val scheduleFormFragment = ScheduleFormFragment.newInstance(
        isModify = false,
        data = ScheduleModel()
      )
      scheduleFormFragment.setOnFormSubmittedListener { _ ->
        setupScheduleItems()
      }
      scheduleFormFragment.show(childFragmentManager, scheduleFormFragment.tag)
    }

    binding.calendarScheduleLayout.setOnRefreshListener {
      setupScheduleItems()
    }

    return view
  }

  private fun setupScheduleItems() {
    scheduleList.clear()
    fetchEventsDataFromFirestore { result ->
      scheduleList.addAll(result)
      setUpScheduleView()
      binding.calendarScheduleLayout.isRefreshing = false
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
  }

  // 프래그먼트가 파괴될 때 바인딩을 해제합니다.
  override fun onDestroyView() {
    super.onDestroyView()
    CalendarUtil.selectedDate.removeObservers(viewLifecycleOwner)
    _binding = null
  }

  // 월의 일수를 배열 형태로 반환합니다.
  private fun daysInMonthArray(date: LocalDate): ArrayList<LocalDate> {
    // 월별 일 수를 담을 ArrayList를 생성합니다.
    val dayList: ArrayList<LocalDate> = ArrayList()
    val yearMonth: YearMonth = YearMonth.from(date)
    val lastDay = yearMonth.lengthOfMonth()
    val firstDay = CalendarUtil.selectedDate.value!!.withDayOfMonth(1)
    val dayOfWeek = firstDay.dayOfWeek.value

    // 각 일을 로컬날짜 배열에 채웁니다.
    for (i: Int in 1..42) {
      when {
        i <= dayOfWeek -> {
          dayList.add(LocalDate.of(1,1,1))
        }
        i > lastDay + dayOfWeek -> { }
        else -> {
          val selectedCellValue = i - dayOfWeek
          dayList.add(LocalDate.of(CalendarUtil.selectedDate.value!!.year, CalendarUtil.selectedDate.value!!.month, selectedCellValue))
        }
      }
    }

    // 필요없는 행을 제거합니다.
    if (dayList[0] == LocalDate.of(1,1,1) && dayList[6] == LocalDate.of(1,1,1)) {
      dayList.removeAll(dayList.subList(0, 7).toSet())
    }

    return dayList
  }

  // 월별 캘린더 뷰를 설정합니다.
  private fun setMonthView() {
    // 월별 일 수 목록을 얻어옵니다.
    val dayList: ArrayList<LocalDate> = daysInMonthArray(CalendarUtil.selectedDate.value!!)
    val adapter = CalendarAdapter(dayList)
    val manager: RecyclerView.LayoutManager = NoScrollGridLayoutManager(requireContext(), 7)

    // 리사이클러 뷰의 레이아웃 관리자를 설정합니다.
    binding.calendarDateView.layoutManager = manager
    // 리사이클러 뷰의 어댑터를 설정합니다.
    binding.calendarDateView.adapter = adapter

    // 스케줄 날짜 텍스트에 선택된 날짜를 표시합니다.
    binding.scheduleDate.text = yearMonthFromDate(CalendarUtil.selectedDate.value!!)
    binding.calendarDateTitle.text = CalendarUtil.selectedDate.value!!.format(DateTimeFormatter.ofPattern("yyyy년 MM월"))
  }

  private fun fetchEventsDataFromFirestore(onComplete: (List<ScheduleModel>) -> Unit) {
    val date = CalendarUtil.selectedDate.value!!
    val startDateTime: LocalDateTime = date.atStartOfDay()
    val startDate: Date = Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant())
    val endLocalDate: LocalDate = date.plusDays(1)
    val endDateTime: LocalDateTime = endLocalDate.atStartOfDay()
    val endDate: Date = Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant())
    val dataList = mutableListOf<ScheduleModel>()

    db.collection("Calendars")
      .document(Application.prefs.getString("currentCalendar", auth.currentUser!!.uid))
      .collection("Events")
      .whereEqualTo("eventIsTodo", false)
      .whereGreaterThanOrEqualTo("eventTime", Timestamp(startDate))
      .whereLessThan("eventTime", Timestamp(endDate))
      .get()
      .addOnSuccessListener {documents ->
        if (documents != null) {
          for (document in documents) {
            dataList.add(ScheduleModel.fromDocument(document.data))
          }
          onComplete(dataList)
        } else {
          Application.prefs.setString("currentCalendar", Application.auth.currentUser!!.uid)
          setupScheduleItems()
        }
      }
      .addOnFailureListener {
        onComplete(listOf())
      }
  }

  @SuppressLint("ClickableViewAccessibility")
  private fun setUpScheduleView() {
    scheduleAdapter = ScheduleAdapter(scheduleList)
    binding.calendarScheduleView.apply {
      layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
      adapter = scheduleAdapter
    }
    checkEmptyList()
  }

  // 날짜로부터 년/월을 반환합니다.
  private fun yearMonthFromDate(date: LocalDate): String {
    // DateTimeFormatter를 이용하여 날짜를 문자열로 변환합니다.
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("M월 d일 EEEE")
    return date.format(formatter)
  }

  private fun checkEmptyList() {
    if (scheduleAdapter.itemCount == 0) { // 항목이 없는 경우
      binding.scheduleEmptyLayout.visibility = View.VISIBLE
    } else {
      binding.scheduleEmptyLayout.visibility = View.GONE
    }
  }

  @SuppressLint("NotifyDataSetChanged")
  private fun updateData(data: List<Any>) { // List<Any>를 사용 중인 데이터 타입으로 변경해주세요.
    // 여기서 데이터를 업데이트하고, 어댑터에 알립니다.
    scheduleAdapter.notifyDataSetChanged()

    // 이후에 데이터 상태를 확인하여 TextView의 가시성을 다시 설정합니다.
    checkEmptyList()
  }

  override fun onContextItemSelected(item: MenuItem): Boolean {
    val position = item.groupId
    when (item.itemId) {
      0 -> {
        if (auth.currentUser?.uid == scheduleList[position].eventAuthorID) {
          selectedScheduleData = scheduleList[position]
          selectedSchedule = position
          val scheduleFormFragment = ScheduleFormFragment.newInstance(
            isModify = true,
            data = scheduleList[position]
          )
          scheduleFormFragment.show(childFragmentManager, scheduleFormFragment.tag)
        } else {
          Toast.makeText(requireContext(), "작성자만 수정할 수 있습니다!", Toast.LENGTH_SHORT).show()
        }
        return true
      }
      1 -> {
        if (auth.currentUser?.uid == scheduleList[position].eventAuthorID) {
          db.collection("Calendars")
            .document(Application.prefs.getString("currentCalendar", auth.currentUser!!.uid))
            .collection("Events")
            .whereEqualTo("eventLinkID", scheduleList[position].eventLinkID)
            .get()
            .addOnSuccessListener { querySnapshot ->
              val batch = db.batch()

              querySnapshot.documents.forEach { documentSnapshot ->
                batch.delete(documentSnapshot.reference)
              }

              batch.commit()
                .addOnSuccessListener {
                  Toast.makeText(requireContext(), "일정을 삭제했습니다!", Toast.LENGTH_SHORT).show()
                  setupScheduleItems()
                }
                .addOnFailureListener { e ->
                  Toast.makeText(requireContext(), "일정을 삭제하지 못했습니다!", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
              Toast.makeText(requireContext(), "일정을 삭제하지 못했습니다!", Toast.LENGTH_SHORT).show()
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