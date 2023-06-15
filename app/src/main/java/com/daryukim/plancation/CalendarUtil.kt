package com.daryukim.plancation

import androidx.lifecycle.MutableLiveData
import java.time.LocalDate
import kotlin.properties.Delegates

data class CalendarUtil(
  val selectedDate: MutableLiveData<LocalDate> = MutableLiveData(),
  var isDateClicked: Boolean
) {
  companion object {
    val selectedDate: MutableLiveData<LocalDate> = MutableLiveData()
  }
}