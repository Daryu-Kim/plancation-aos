// 패키지 선언
package com.daryukim.plancation
// 필요한 라이브러리 임포트
import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.DatePicker
import androidx.core.content.ContextCompat
import com.daryukim.plancation.databinding.ActivityAiFormBinding
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.time.temporal.ChronoUnit.DAYS
// AIFormActivity 클래스 선언
class AIFormActivity : AppCompatActivity() {
  // 변수 선언
  private lateinit var binding: ActivityAiFormBinding
  private var isStartDateClicked: Boolean = false
  private var isEndDateClicked: Boolean = false
  private var isStartTimeClicked: Boolean = false
  private var isEndTimeClicked: Boolean = false
  private var startLocalDateValue: LocalDate = LocalDate.now()
  private var endLocalDateValue: LocalDate = LocalDate.now()
  private var startLocalTimeValue: LocalTime = LocalTime.now()
  private var endLocalTimeValue: LocalTime = LocalTime.now()
  // 액티비티 생성 시 호출되는 함수
  @SuppressLint("SetTextI18n")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityAiFormBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)
    // 날짜와 시간 선택자를 설정하는 함수 호출
    setupDatePicker()
    setupTimePicker()
    // 이전 버튼 클릭 시 액티비티 종료
    binding.appBarPrev.setOnClickListener { finish() }
    // 시작 날짜 버튼 클릭 시 동작 설정
    binding.aiFormDateStart.setOnClickListener {
      resetClickState()
      isStartDateClicked = true
      updateDatePickerAndTimePickerVisibility()
    }
    // 종료 날짜 버튼 클릭 시 동작 설정
    binding.aiFormDateEnd.setOnClickListener {
      resetClickState()
      isEndDateClicked = true
      updateDatePickerAndTimePickerVisibility()
    }
    // 시작 시간 버튼 클릭 시 동작 설정
    binding.aiFormTimeStart.setOnClickListener {
      resetClickState()
      isStartTimeClicked = true
      updateDatePickerAndTimePickerVisibility()
    }
    // 종료 시간 버튼 클릭 시 동작 설정
    binding.aiFormTimeEnd.setOnClickListener {
      resetClickState()
      isEndTimeClicked = true
      updateDatePickerAndTimePickerVisibility()
    }
    // 제출 버튼 클릭 시 동작 설정
    binding.aiFormSubmit.setOnClickListener {
      submitForm()
    }
  }
  // 모든 클릭 상태를 초기화하는 함수
  private fun resetClickState() {
    isStartDateClicked = false
    isEndDateClicked = false
    isStartTimeClicked = false
    isEndTimeClicked = false
  }
  // 날짜 선택자를 설정하는 함수
  private fun setupDatePicker() {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
    binding.aiFormDatePicker.init(year, month, dayOfMonth, DatePicker.OnDateChangedListener { _, y, m, d ->
      handleDateChange(y, m, d)
    })
    updateDateTextView()
  }
  // 시간 선택자를 설정하는 함수
  private fun setupTimePicker() {
    binding.aiFormTimePicker.setOnTimeChangedListener { view, hourOfDay, minute ->
      handleTimeChange(hourOfDay, minute)
    }
    updateTimeTextView()
  }
  // 날짜 변경 시 동작을 처리하는 함수
  private fun handleDateChange(y: Int, m: Int, d: Int) {
    if (isStartDateClicked) {
      startLocalDateValue = LocalDate.of(y, m + 1, d)
      if (startLocalDateValue > endLocalDateValue) {
        endLocalDateValue = startLocalDateValue
      }
    } else if (isEndDateClicked) {
      endLocalDateValue = LocalDate.of(y, m + 1, d)
      if (startLocalDateValue > endLocalDateValue) {
        startLocalDateValue = endLocalDateValue
      }
    }
    updateDateTextView()
  }
  // 시간 변경 시 동작을 처리하는 함수
  private fun handleTimeChange(hourOfDay: Int, minute: Int) {
    if (isStartTimeClicked) {
      startLocalTimeValue = LocalTime.of(hourOfDay, minute)
      if (startLocalTimeValue > endLocalTimeValue) {
        endLocalTimeValue = startLocalTimeValue
      }
    } else if (isEndTimeClicked) {
      endLocalTimeValue = LocalTime.of(hourOfDay, minute)
      if (startLocalTimeValue > endLocalTimeValue) {
        startLocalTimeValue = endLocalTimeValue
      }
    }
    updateTimeTextView()
  }
  // 날짜 텍스트뷰를 업데이트하는 함수
  private fun updateDateTextView() {
    val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    binding.aiFormDateStartText.text = startLocalDateValue.format(formatter)
    binding.aiFormDateEndText.text = endLocalDateValue.format(formatter)
  }
  // 시간 텍스트뷰를 업데이트하는 함수
  private fun updateTimeTextView() {
    val formatter = DateTimeFormatter.ofPattern("a hh:mm")
    binding.aiFormTimeStartText.text = startLocalTimeValue.format(formatter)
    binding.aiFormTimeEndText.text = endLocalTimeValue.format(formatter)
  }
  // 날짜와 시간 선택자의 가시성을 업데이트하는 함수
  private fun updateDatePickerAndTimePickerVisibility() {
    updateDatePickerVisibility()
    updateTimePickerVisibility()
  }
  // 날짜 선택자의 가시성을 업데이트하는 함수
  private fun updateDatePickerVisibility() {
    if (isStartDateClicked || isEndDateClicked) {
      binding.aiFormDatePicker.visibility = View.VISIBLE
    } else {
      binding.aiFormDatePicker.visibility = View.GONE
    }
    updateDateTextColor()
  }
  // 시간 선택자의 가시성을 업데이트하는 함수
  private fun updateTimePickerVisibility() {
    if (isStartTimeClicked || isEndTimeClicked) {
      binding.aiFormTimePicker.visibility = View.VISIBLE
    } else {
      binding.aiFormTimePicker.visibility = View.GONE
    }
    updateTimeTextColor()
  }
  // 날짜 텍스트의 색상을 업데이트하는 함수
  private fun updateDateTextColor() {
    if (isStartDateClicked) {
      binding.aiFormDateStartText.setTextColor(ContextCompat.getColor(this, R.color.accent))
      binding.aiFormDateEndText.setTextColor(ContextCompat.getColor(this, R.color.hint_text))
    } else if (isEndDateClicked) {
      binding.aiFormDateStartText.setTextColor(ContextCompat.getColor(this, R.color.hint_text))
      binding.aiFormDateEndText.setTextColor(ContextCompat.getColor(this, R.color.accent))
    } else {
      binding.aiFormDateStartText.setTextColor(ContextCompat.getColor(this, R.color.hint_text))
      binding.aiFormDateEndText.setTextColor(ContextCompat.getColor(this, R.color.hint_text))
    }
  }
  // 시간 텍스트의 색상을 업데이트하는 함수
  private fun updateTimeTextColor() {
    if (isStartTimeClicked) {
      binding.aiFormTimeStartText.setTextColor(ContextCompat.getColor(this, R.color.accent))
      binding.aiFormTimeEndText.setTextColor(ContextCompat.getColor(this, R.color.hint_text))
    } else if (isEndTimeClicked) {
      binding.aiFormTimeStartText.setTextColor(ContextCompat.getColor(this, R.color.hint_text))
      binding.aiFormTimeEndText.setTextColor(ContextCompat.getColor(this, R.color.accent))
    } else {
      binding.aiFormTimeStartText.setTextColor(ContextCompat.getColor(this, R.color.hint_text))
      binding.aiFormTimeEndText.setTextColor(ContextCompat.getColor(this, R.color.hint_text))
    }
  }
  // 폼 제출 함수
  private fun submitForm() {
    if (binding.aiFormPrompt.text.isNotEmpty()) {
      val datePattern = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")
      val timePattern = DateTimeFormatter.ofPattern("HH시 mm분")
      val currentDate = startLocalDateValue
      val promptList = arrayListOf<String>()
      val dateCount = DAYS.between(startLocalDateValue, endLocalDateValue) + 1
      Log.d("GPT", "${dateCount}일 간의 일정")
      for (i in 1..dateCount) {
        val prompt =
          "${startLocalDateValue.format(datePattern)}부터 ${endLocalDateValue.format(datePattern)}까지 ${binding.aiFormPrompt.text} 일정을 짤거야. 활동 시간은 ${startLocalTimeValue.format(timePattern)}부터 ${endLocalTimeValue.format(timePattern)}까지고, 상황에 따라서 최소 30분에서 최대 120분까지 유동적으로 계획을 짜줬으면 좋겠어. 계획을 최대한 상세하게 짜서 날짜는 'date', 시작 시간은 'startTime', 종료 시간은 'endTime', 제목은 'title'이라는 필드로만 표현해줘. 그리고 'date' 필드는 'yyyy-MM-dd' 형식으로, 'startTime'이랑 'endTime' 필드는 'HH:mm' 형식에 맞춰서 JSON 방식으로 출력해줘."
        currentDate.plusDays(1)
        promptList.add(prompt)
      }
      val intent = Intent(this, AIResultActivity::class.java)
      intent.putExtra("prompt", promptList)
      startActivity(intent)
    } else {
      binding.aiFormPromptText.text = "계획 주제를 입력해주세요!"
      binding.aiFormPromptText.setTextColor(ContextCompat.getColor(this, R.color.error))
    }
  }

  override fun onPause() {
    super.onPause()
    binding.aiFormPrompt.text = Editable.Factory.getInstance().newEditable("")
    isStartDateClicked = false
    isEndDateClicked = false
    isStartTimeClicked = false
    isEndTimeClicked = false
    startLocalDateValue = LocalDate.now()
    endLocalDateValue = LocalDate.now()
    startLocalTimeValue = LocalTime.now()
    endLocalTimeValue = LocalTime.now()
  }
}