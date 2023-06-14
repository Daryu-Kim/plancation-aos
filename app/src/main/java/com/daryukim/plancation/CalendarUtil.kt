package com.daryukim.plancation

import java.time.LocalDate

data class CalendarUtil(
  var selectedDate: LocalDate,
  var isDateClicked: Boolean
) {
  companion object {
    lateinit var selectedDate: LocalDate
    var isDateClicked: Boolean = false
  }
}