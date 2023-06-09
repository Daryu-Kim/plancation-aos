package com.daryukim.plancation

import androidx.lifecycle.MutableLiveData
import java.time.LocalDate
import kotlin.properties.Delegates

data class TodoUtil(
  val selectedDate: MutableLiveData<LocalDate> = MutableLiveData(),
  val checkedUserList: MutableLiveData<ArrayList<String>> = MutableLiveData()
) {
  companion object {
    val selectedDate: MutableLiveData<LocalDate> = MutableLiveData()
    val checkedUserList: MutableLiveData<ArrayList<String>> = MutableLiveData()
  }
}