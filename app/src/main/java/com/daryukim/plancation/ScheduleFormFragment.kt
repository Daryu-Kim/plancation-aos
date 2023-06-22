package com.daryukim.plancation

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.DatePicker
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.daryukim.plancation.databinding.FragmentScheduleFormBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

class ScheduleFormFragment : BottomSheetDialogFragment() {
  private val googleGeocodingApi: GoogleGeocodingApi

  private var _binding: FragmentScheduleFormBinding? = null
  private val binding get() = _binding!!

  private var isModify: Boolean = false
  private var isAllDay: Boolean = true
  private var data: ScheduleModel = ScheduleModel()
  private var isRangeStartClicked = false
  private var isRangeEndClicked = false

  private var selectedStartDate: LocalDate = CalendarUtil.selectedDate.value!!
  private var selectedEndDate: LocalDate = CalendarUtil.selectedDate.value!!

  init {
    val retrofit = Retrofit.Builder()
      .baseUrl("https://maps.googleapis.com")
      .addConverterFactory(GsonConverterFactory.create())
      .build()

    googleGeocodingApi = retrofit.create(GoogleGeocodingApi::class.java)
  }

  // 프래그먼트 생성 시 뷰 설정
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    val repeatItems = resources.getStringArray(R.array.repeat_array)
    val alertItems = resources.getStringArray(R.array.alert_array)
    val repeatAdapter = CustomSpinnerAdapter(requireContext(), repeatItems)
    val alertAdapter = CustomSpinnerAdapter(requireContext(), alertItems)
    arguments?.let {
      isModify = it.getBoolean("isModify")
      data = it.getParcelable("data")!!
    }
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

    // 바인딩 객체를 생성하고 화면 레이아웃을 동적으로 연결합니다.
    _binding = FragmentScheduleFormBinding.inflate(inflater, container, false)
    val view = binding.root

    if (isModify) {
      binding.scheduleFormBarTitle.text = "이벤트 수정"
      binding.scheduleFormBarSubmitButton.text = "완료"
      binding.scheduleFormContentRepeatSpinner.isEnabled = false
      binding.scheduleFormContentAlertSpinner.isEnabled = false

      binding.scheduleFormContentTitle.text = SpannableStringBuilder(data.eventTitle)
      onClickedDayButton()
      binding.scheduleDayButton.isEnabled = false
      binding.scheduleRangeButton.isEnabled = false
    } else {
      binding.scheduleFormBarTitle.text = "새로운 이벤트"
      binding.scheduleFormBarSubmitButton.text = "등록"
      onClickedDayButton()
    }

    binding.scheduleRangeDatePickerLayout.init(year, month, dayOfMonth,
      DatePicker.OnDateChangedListener { _, y, m, d ->
        if (isRangeStartClicked) {
          selectedStartDate = LocalDate.of(y,m + 1,d)
          if (selectedStartDate > selectedEndDate) {
            selectedEndDate = selectedStartDate
          }
          changeDateTextView()
        } else if (isRangeEndClicked) {
          selectedEndDate = LocalDate.of(y,m + 1,d)
          if (selectedStartDate > selectedEndDate) {
            selectedStartDate = selectedEndDate
          }
          changeDateTextView()
        }
      })
    changeDateTextView()


    binding.scheduleFormContentRepeatEditLayout.visibility = View.GONE
    binding.scheduleFormContentRepeatSpinner.adapter = repeatAdapter
    binding.scheduleFormContentRepeatSpinner.setSelection(repeatAdapter.count - 1)
    binding.scheduleFormContentRepeatSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
      override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        repeatAdapter.selectedPosition = position
        repeatAdapter.notifyDataSetChanged()
        binding.scheduleFormContentRepeatEditLayout.visibility = if (position != 4) View.VISIBLE else View.GONE
      }

      override fun onNothingSelected(parent: AdapterView<*>?) {

      }
    }

    binding.scheduleFormContentAlertSpinner.adapter = alertAdapter
    binding.scheduleFormContentAlertSpinner.setSelection(alertAdapter.count - 1)
    binding.scheduleFormContentAlertSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
      override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        alertAdapter.selectedPosition = position
        alertAdapter.notifyDataSetChanged()
      }

      override fun onNothingSelected(parent: AdapterView<*>?) {

      }
    }

    binding.scheduleFormBarCancelButton.setOnClickListener { dismiss() }

    binding.scheduleFormBarSubmitButton.setOnClickListener {
      if (isModify) {
        // 이벤트 수정
      } else {
        // 새로운 이벤트
      }
      dismiss()
    }

    binding.scheduleDayButton.setOnClickListener { onClickedDayButton() }
    binding.scheduleRangeButton.setOnClickListener { onClickedRangeButton() }
    binding.scheduleRangeStartButton.setOnClickListener {
      if (!isAllDay) {
        isRangeStartClicked = !isRangeStartClicked
        isRangeEndClicked = false
        onClickedRangeDateButton()
      }
    }
    binding.scheduleRangeEndButton.setOnClickListener {
      if (!isAllDay) {
        isRangeStartClicked = false
        isRangeEndClicked = !isRangeEndClicked
        onClickedRangeDateButton()
      }
    }
    binding.scheduleFormContentLocationButton.setOnClickListener { fetchCoordinatesFromAddress(binding.scheduleFormContentLocationEdittext.text.toString()) }
    binding.scheduleFormContentColorButton.setOnClickListener { onShowColorPicker() }

    return view
  }

  private fun changeDateTextView() {
    val formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일 EEEE")
    binding.scheduleRangeStartDate.text = selectedStartDate.format(formatter)
    binding.scheduleRangeEndDate.text = selectedEndDate.format(formatter)
  }

  private fun onClickedDayButton() {
    isAllDay = true
    binding.scheduleDayButton.background = ContextCompat.getDrawable(requireContext(), R.drawable.form_left_button_shape)
    binding.scheduleDayButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
    binding.scheduleRangeButton.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
    binding.scheduleRangeButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.text))
    binding.scheduleRangeStartTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.hint_text))
    binding.scheduleRangeStartDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.hint_text))
    binding.scheduleRangeEndTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.hint_text))
    binding.scheduleRangeEndDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.hint_text))

    binding.scheduleRangeDatePickerLayout.visibility = View.GONE

    selectedEndDate = selectedStartDate
    changeDateTextView()
  }

  private fun onClickedRangeButton() {
    isAllDay = false
    binding.scheduleRangeButton.background = ContextCompat.getDrawable(requireContext(), R.drawable.form_right_button_shape)
    binding.scheduleDayButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.text))
    binding.scheduleDayButton.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
    binding.scheduleRangeButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
    binding.scheduleRangeStartTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.text))
    binding.scheduleRangeStartDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.text))
    binding.scheduleRangeEndTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.text))
    binding.scheduleRangeEndDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.text))
    binding.scheduleRangeDatePickerLayout.visibility = View.GONE
  }

  private fun onClickedRangeDateButton() {
    binding.scheduleRangeDatePickerLayout.visibility = View.GONE
    binding.scheduleRangeStartTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.text))
    binding.scheduleRangeStartDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.text))
    binding.scheduleRangeEndTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.text))
    binding.scheduleRangeEndDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.text))

    if (isRangeStartClicked || isRangeEndClicked) {
      binding.scheduleRangeDatePickerLayout.visibility = View.VISIBLE
    } else {
      return
    }

    if (isRangeStartClicked) {
      binding.scheduleRangeStartTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.accent))
      binding.scheduleRangeStartDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.accent))
    } else if (isRangeEndClicked) {
      binding.scheduleRangeEndTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.accent))
      binding.scheduleRangeEndDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.accent))
    } else {

    }
  }

  private fun fetchCoordinatesFromAddress(address: String) {
    val apiKey = "AIzaSyDbUGBM3TEQKIR_Nah01_d8cxUq9eFdS3I"
    val language = "ko"
    Log.d("Geocoding", address)

    googleGeocodingApi.getLatLngFromAddress(address, apiKey, language).enqueue(object : Callback<GeocodingResponse> {
      override fun onResponse(call: Call<GeocodingResponse>, response: Response<GeocodingResponse>) {
        if (response.isSuccessful) {
          val geocodingResponse = response.body()
          val result = geocodingResponse?.results?.firstOrNull()

          if (result != null) {
            val latLng = result.geometry.location
            Log.d("Geocoding", "Latitude: ${latLng.lat}, Longitude: ${latLng.lng}")
            binding.scheduleFormContentLocationEdittext.text = Editable.Factory.getInstance().newEditable(result.formatted_address)
          } else {
            Toast.makeText(requireContext(), "주소를 찾을 수 없습니다!", Toast.LENGTH_SHORT).show()
            Log.d("Geocoding", "No coordinates found for the address.")
          }
        } else {
          Log.d("Geocoding", "Error occurred: ${response.errorBody()?.string()}")
        }
      }

      override fun onFailure(call: Call<GeocodingResponse>, t: Throwable) {
        Log.d("Geocoding", "Failed to get geocoding result: $t")
      }
    })
  }

  private fun onShowColorPicker() {
    val dataColor = data.eventBackgroundColor.toMutableMap()
    val colorPickerBottomSheet = ColorPickerBottomSheet()
    colorPickerBottomSheet.setOnColorSelectedListener { selectedColor ->
        dataColor["alphaColor"] = Color.alpha(selectedColor)
        dataColor["redColor"] = Color.red(selectedColor)
        dataColor["greenColor"] = Color.green(selectedColor)
        dataColor["blueColor"] = Color.blue(selectedColor)
        data = data.copy(eventBackgroundColor = dataColor)
        Log.d("Color", dataColor.toString())
        Log.d("Color", data.eventBackgroundColor.toString())
        binding.scheduleFormContentColorButton.backgroundTintList = ColorStateList.valueOf(
          Color.argb(
            dataColor["alphaColor"]!!,
            dataColor["redColor"]!!,
            dataColor["greenColor"]!!,
            dataColor["blueColor"]!!
          )
        )
    }
    colorPickerBottomSheet.show(parentFragmentManager, "colorPicker")
  }

  companion object {
    // 팩토리 함수를 사용하여 프로퍼티값을 지정할 수 있는 프래그먼트 인스턴스를 생성합니다.
    fun newInstance(isModify: Boolean, data: ScheduleModel) =
      ScheduleFormFragment().apply {
        arguments = Bundle().apply {
          putBoolean("isModify", isModify)
          putParcelable("data", data)
        }
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

  override fun getTheme(): Int {
    return R.style.BottomSheetDialogStyle
  }
}
