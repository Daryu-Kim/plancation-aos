package com.daryukim.plancation

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.daryukim.plancation.databinding.FragmentCalendarBinding
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class CalendarFragment: Fragment() {
  private var _binding: FragmentCalendarBinding? = null
  private val binding get() = _binding!!

  // 프래그먼트 생성 시 뷰 설정
  @SuppressLint("ClickableViewAccessibility")
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    // 바인딩 객체를 생성하고 화면 레이아웃을 동적으로 연결합니다.
    _binding = FragmentCalendarBinding.inflate(inflater, container, false)
    val view = binding.root

    // 오늘의 날짜를 설정합니다.
    CalendarUtil.selectedDate.value = LocalDate.now()

    // 로그에 오늘 날짜를 출력합니다.
    Log.d("Date", "Current Date is " + CalendarUtil.selectedDate)

    // 월별 캘린더 뷰를 설정합니다.
    setMonthView()

    // 토스트 메시지로 선택된 날짜를 표시합니다.
    Toast.makeText(context, CalendarUtil.selectedDate.toString(), Toast.LENGTH_SHORT).show()

    // 날짜 터치에 따라 스와이프 방향을 계산하고 이전 달 또는 다음 달로 이동합니다.
    binding.calendarLayout.setOnTouchListener(object: OnSwipeTouchListener(requireContext()) {
      override fun onSwipeRight() {
        CalendarUtil.selectedDate.value = CalendarUtil.selectedDate.value!!.minusMonths(1)
        setMonthView()
      }

      override fun onSwipeLeft() {
        CalendarUtil.selectedDate.value = CalendarUtil.selectedDate.value!!.plusMonths(1)
        setMonthView()
      }
    })

    // 선택된 날짜에 변화를 관찰하고, 변경 시 스케줄 날짜를 갱신합니다.
    CalendarUtil.selectedDate.observe(viewLifecycleOwner, Observer { value ->
      binding.scheduleDate.text = yearMonthFromDate(value)
    })

    return view
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
  }

  // 날짜로부터 년/월을 반환합니다.
  private fun yearMonthFromDate(date: LocalDate): String {
    // DateTimeFormatter를 이용하여 날짜를 문자열로 변환합니다.
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("M월 d일 EEEE")
    return date.format(formatter)
  }
}
