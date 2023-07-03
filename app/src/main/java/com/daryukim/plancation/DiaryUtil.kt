package com.daryukim.plancation

import androidx.lifecycle.MutableLiveData
import java.time.LocalDate
import kotlin.properties.Delegates

data class DiaryUtil(
  val selectedDate: MutableLiveData<LocalDate> = MutableLiveData(),
) {
  companion object {
    val selectedDate: MutableLiveData<LocalDate> = MutableLiveData()
  }
}