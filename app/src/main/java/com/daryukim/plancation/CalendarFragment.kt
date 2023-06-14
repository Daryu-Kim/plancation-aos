package com.daryukim.plancation

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.daryukim.plancation.databinding.FragmentCalendarBinding
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class CalendarFragment: Fragment() {
  private var _binding: FragmentCalendarBinding? = null
  private val binding get() = _binding!!

  @SuppressLint("ClickableViewAccessibility")
  @RequiresApi(Build.VERSION_CODES.O)
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    _binding = FragmentCalendarBinding.inflate(inflater, container, false)
    val view = binding.root

    CalendarUtil.selectedDate = LocalDate.now()
    Log.d("Date", "Current Date is " + CalendarUtil.selectedDate)
    setMonthView()
    Toast.makeText(context, CalendarUtil.selectedDate.toString(), Toast.LENGTH_SHORT).show()

    binding.calendarLayout.setOnTouchListener(object: OnSwipeTouchListener(requireContext()) {
      override fun onSwipeRight() {
        CalendarUtil.selectedDate = CalendarUtil.selectedDate.minusMonths(1)
        setMonthView()
        CalendarUtil.selectedDate = CalendarUtil.selectedDate.plusMonths(1)
      }

      override fun onSwipeLeft() {
        CalendarUtil.selectedDate = CalendarUtil.selectedDate.plusMonths(1)
        setMonthView()
        CalendarUtil.selectedDate = CalendarUtil.selectedDate.minusMonths(1)
      }
    })

    return view

  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun daysInMonthArray(date: LocalDate): ArrayList<LocalDate> {
    var dayList: ArrayList<LocalDate> = ArrayList()
    var yearMonth: YearMonth = YearMonth.from(date)
    var lastDay = yearMonth.lengthOfMonth()
    var firstDay = CalendarUtil.selectedDate.withDayOfMonth(1)
    var dayOfWeek = firstDay.dayOfWeek.value

    for (i: Int in 1..42) {
      if (i <= dayOfWeek) {
        dayList.add(LocalDate.of(1,1,1))
      } else if (i > lastDay + dayOfWeek) {}
      else {
        dayList.add(LocalDate.of(CalendarUtil.selectedDate.year, CalendarUtil.selectedDate.month, i - dayOfWeek))
      }
    }

    return dayList
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun setMonthView() {
    var dayList: ArrayList<LocalDate> = daysInMonthArray(CalendarUtil.selectedDate)
    var adapter: CalendarAdapter = CalendarAdapter(dayList)
    var manager: RecyclerView.LayoutManager = NoScrollGridLayoutManager(requireContext(), 7)

    binding.calendarDateView.layoutManager = manager
    binding.calendarDateView.adapter = adapter

    binding.scheduleDate.text = yearMonthFromDate(CalendarUtil.selectedDate)
  }

  @RequiresApi(Build.VERSION_CODES.O)
  private fun yearMonthFromDate(date: LocalDate): String {
    var formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("M월 d일 EEEE")
    return date.format(formatter)
  }
}