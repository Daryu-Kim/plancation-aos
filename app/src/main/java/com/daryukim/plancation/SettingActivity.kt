// 패키지 선언
package com.daryukim.plancation
// 필요한 라이브러리 임포트
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.daryukim.plancation.databinding.ActivitySettingBinding
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class SettingActivity : AppCompatActivity() {

  private lateinit var binding: ActivitySettingBinding
  private var isOpenedAlertTimePicker = false
  private var isOpenedDeleteCalendarPicker = false

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivitySettingBinding.inflate(layoutInflater)
    setContentView(binding.root)

    setupAlertTimePicker()
    setupDeleteCalendar()

    with(binding) {
      appBarPrev.setOnClickListener { finish() }
      settingAlertBtn.setOnClickListener { toggleAlertTimePicker() }
      settingAlertTimePickerCancel.setOnClickListener { toggleAlertTimePicker() }
      settingAlertTimePickerSubmit.setOnClickListener { saveAlertTimePicker() }
      settingChangePwBtn.setOnClickListener { startActivity(Intent(this@SettingActivity, ChangePWActivity::class.java)) }
      settingDeleteCalendarBtn.setOnClickListener { toggleDeleteCalendarPicker() }
      settingDeleteAccountBtn.setOnClickListener {
        val deleteAccountBottomSheet = DeleteAccountBottomSheet()
        deleteAccountBottomSheet.show(supportFragmentManager, "deleteAccount")
      }
    }
  }

  private fun toggleView(isViewOpened: Boolean, view: View, drawable: ImageView) {
    view.visibility = if (isViewOpened) View.VISIBLE else View.GONE
    drawable.background = ContextCompat.getDrawable(this, if (isViewOpened) R.drawable.ic_down else R.drawable.ic_right)
  }

  private fun toggleAlertTimePicker() {
    isOpenedAlertTimePicker = !isOpenedAlertTimePicker
    toggleView(isOpenedAlertTimePicker, binding.settingAlertTimePicker, binding.settingAlertTimeDropdown)
  }

  private fun toggleDeleteCalendarPicker() {
    isOpenedDeleteCalendarPicker = !isOpenedDeleteCalendarPicker
    toggleView(isOpenedDeleteCalendarPicker, binding.settingDeleteCalendarPicker, binding.settingDeleteCalendarDropdown)
  }

  private fun setupAlertTimePicker() {
    val alertTimeString = Application.prefs.getString("alertTime", "09:00")
    val alertTime = LocalTime.parse(alertTimeString)
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    binding.settingAlertTime.text = timeFormatter.format(alertTime)
    binding.settingAlertTimePickerWidget.apply {
      hour = alertTime.hour
      minute = alertTime.minute
    }
  }

  private fun setupDeleteCalendar() {

  }

  private fun getAuthorCalendarData() {
    Application.db.collection("Calendars")
  }

  private fun saveAlertTimePicker() {
    val time = LocalTime.of(binding.settingAlertTimePickerWidget.hour, binding.settingAlertTimePickerWidget.minute)
    val timeString = DateTimeFormatter.ofPattern("HH:mm").format(time)

    Application.prefs.setString("alertTime", timeString)
    binding.settingAlertTime.text = timeString
    toggleAlertTimePicker()
  }
}