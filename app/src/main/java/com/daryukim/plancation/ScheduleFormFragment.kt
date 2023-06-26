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
import android.widget.Button
import android.widget.DatePicker
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.daryukim.plancation.databinding.FragmentScheduleFormBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.ktx.Firebase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit.DAYS
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.max

class ScheduleFormFragment : BottomSheetDialogFragment() {
  private var _binding: FragmentScheduleFormBinding? = null
  private val binding get() = _binding!!

  private val googleGeocodingApi: GoogleGeocodingApi
  private val googleReverseGeocodingApi: GoogleReverseGeocodingApi

  private val db: FirebaseFirestore
  private val auth: FirebaseAuth

  private var data: ScheduleModel = ScheduleModel()

  private var isModify: Boolean = false
  private var isAllDay: Boolean = true

  private var isRangeStartClicked = false
  private var isRangeEndClicked = false

  private var selectedStartDate: LocalDate = CalendarUtil.selectedDate.value!!
  private var selectedEndDate: LocalDate = CalendarUtil.selectedDate.value!!

  private var dataColor: Map<String, Int>
  private var dataLocation: Map<String, Double>

  init {
    val retrofit = Retrofit.Builder()
      .baseUrl("https://maps.googleapis.com")
      .addConverterFactory(GsonConverterFactory.create())
      .build()

    googleGeocodingApi = retrofit.create(GoogleGeocodingApi::class.java)
    googleReverseGeocodingApi = retrofit.create(GoogleReverseGeocodingApi::class.java)
    db = FirebaseFirestore.getInstance()
    auth = Firebase.auth
    dataColor = data.eventBackgroundColor.toMutableMap()
    dataLocation = mapOf(
      "latitude" to 0.0,
      "longitude" to 0.0
    )
  }

  // 프래그먼트 생성 시 뷰 설정
  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    val view = inflateBinding(inflater, container)

    setupArguments()
    setupForm()
    setupDates()
    setupSpinners()
    setupClickListeners()

    return view
  }

  private fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): View {
    _binding = FragmentScheduleFormBinding.inflate(inflater, container, false)
    return binding.root
  }

  private fun setupArguments() {
    arguments?.let {
      isModify = it.getBoolean("isModify")
      data = it.getParcelable("data")!!
      dataColor = data.eventBackgroundColor.toMutableMap()
    }
  }

  private fun setupForm() {
    if (isModify) {
      binding.scheduleFormBarTitle.text = "이벤트 수정"
      binding.scheduleFormBarSubmitButton.text = "완료"
      binding.scheduleFormContentTitle.isEnabled = false
      binding.scheduleFormContentRepeatSpinner.isEnabled = false
      binding.scheduleFormContentAlertSpinner.isEnabled = false

      binding.scheduleFormContentTitle.text = SpannableStringBuilder(data.eventTitle)
      onDayButtonClick()
      fetchAddressFromCoordinates(data.eventLocation.geoPoint.latitude, data.eventLocation.geoPoint.longitude)
      binding.scheduleFormContentColorButton.backgroundTintList = ColorStateList.valueOf(
        Color.argb(
          data.eventBackgroundColor["alphaColor"]!!,
          data.eventBackgroundColor["redColor"]!!,
          data.eventBackgroundColor["greenColor"]!!,
          data.eventBackgroundColor["blueColor"]!!
        )
      )
      binding.scheduleDayButton.isEnabled = false
      binding.scheduleRangeButton.isEnabled = false
    } else {
      binding.scheduleFormBarTitle.text = "새로운 이벤트"
      binding.scheduleFormBarSubmitButton.text = "등록"
      onDayButtonClick()
    }
  }

  private fun setupDates() {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

    binding.scheduleRangeDatePickerLayout.init(year, month, dayOfMonth,
      DatePicker.OnDateChangedListener { _, y, m, d ->
        if (isRangeStartClicked) {
          selectedStartDate = LocalDate.of(y, m + 1, d)
          if (selectedStartDate > selectedEndDate) {
            selectedEndDate = selectedStartDate
          }
          changeDateTextView()
        } else if (isRangeEndClicked) {
          selectedEndDate = LocalDate.of(y, m + 1, d)
          if (selectedStartDate > selectedEndDate) {
            selectedStartDate = selectedEndDate
          }
          changeDateTextView()
        }
      })
    changeDateTextView()
  }

  private fun setupSpinners() {
    val repeatItems = resources.getStringArray(R.array.repeat_array)
    val alertItems = resources.getStringArray(R.array.alert_array)
    val repeatAdapter = CustomSpinnerAdapter(requireContext(), repeatItems)
    val alertAdapter = CustomSpinnerAdapter(requireContext(), alertItems)

    binding.scheduleFormContentRepeatEditLayout.visibility = View.GONE
    binding.scheduleFormContentRepeatSpinner.adapter = repeatAdapter
    binding.scheduleFormContentRepeatSpinner.setSelection(repeatAdapter.count - 1)
    binding.scheduleFormContentRepeatSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
    binding.scheduleFormContentAlertSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
      override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        alertAdapter.selectedPosition = position
        alertAdapter.notifyDataSetChanged()
      }

      override fun onNothingSelected(parent: AdapterView<*>?) {
      }
    }
  }

  private fun setupClickListeners() {
    binding.scheduleFormBarCancelButton.setOnClickListener { onCancelButtonClick() }
    binding.scheduleFormBarSubmitButton.setOnClickListener { onSubmitButtonClick() }
    binding.scheduleDayButton.setOnClickListener { onDayButtonClick() }
    binding.scheduleRangeButton.setOnClickListener { onRangeButtonClick() }
    binding.scheduleRangeStartButton.setOnClickListener { onRangeStartButtonClick() }
    binding.scheduleRangeEndButton.setOnClickListener { onRangeEndButtonClick() }
    binding.scheduleFormContentLocationButton.setOnClickListener { onLocationButtonClick() }
    binding.scheduleFormContentColorButton.setOnClickListener { onShowColorPicker() }
  }

  private fun onCancelButtonClick() {
    dismiss()
  }

  private fun onSubmitButtonClick() {
    if (isModify) {
      try {
        updateScheduleToFirestore()
        Toast.makeText(requireContext(), "일정을 성공적으로 수정했습니다!", Toast.LENGTH_SHORT).show()
        dismiss()
      } catch (e: FirebaseFirestoreException) {
        Toast.makeText(requireContext(), "일정을 수정하지 못했습니다!", Toast.LENGTH_SHORT).show()
      }
    } else {
      try {
        addScheduleToFirestore()
        Toast.makeText(requireContext(), "일정을 성공적으로 생성했습니다!", Toast.LENGTH_SHORT).show()
        dismiss()
      } catch (e: FirebaseFirestoreException) {
        Toast.makeText(requireContext(), "일정을 생성하지 못했습니다!", Toast.LENGTH_SHORT).show()
      }
    }
  }

  private fun updateScheduleToFirestore() {
    db.collection("Calendars")
      .document("A9PHFsmDLUWbaYDdy2XX")
      .collection("Events")
      .whereEqualTo("eventLinkID", data.eventLinkID)
      .get()
      .addOnSuccessListener { querySnapshot ->
        querySnapshot.documents.forEach { documentSnapshot ->
           documentSnapshot.reference.update(modifyEventData())
        }
      }
      .addOnFailureListener { e ->
        Toast.makeText(requireContext(), "일정을 수정하지 못했습니다!", Toast.LENGTH_SHORT).show()
      }
  }

  private fun addScheduleToFirestore() {
    val eventLinkID = UUID.randomUUID().toString()
    val dateCount = DAYS.between(selectedStartDate.atStartOfDay(), selectedEndDate.atStartOfDay()).toInt()

    for (i in 0..dateCount) {
      val eventID = UUID.randomUUID().toString()
      val eventData = createEventData(eventID, i, eventLinkID, dateCount > 0)

      db.collection("Calendars")
        .document("A9PHFsmDLUWbaYDdy2XX")
        .collection("Events")
        .document(eventID)
        .set(eventData)
    }
  }

  private fun modifyEventData(): HashMap<String, Any?> {
    return hashMapOf(
      "eventBackgroundColor" to dataColor,
      "eventLocation" to GeoPoint(
        dataLocation["latitude"]!!,
        dataLocation["longitude"]!!
      ),
    )
  }

  private fun createEventData(eventID: String, day: Int, eventLinkID: String, isRange: Boolean): HashMap<String, Any?> {
    val eventTitle = if (!isRange) binding.scheduleFormContentTitle.text.toString() else "${binding.scheduleFormContentTitle.text} ${day + 1}일차"
    val eventTime = Timestamp(Date.from(selectedStartDate.atStartOfDay().plusDays(day.toLong()).atZone(ZoneId.systemDefault()).toInstant()))
    val selectedItemPosition = binding.scheduleFormContentAlertSpinner.selectedItemPosition

    return hashMapOf(
      "eventID" to eventID,
      "eventTitle" to eventTitle,
      "eventTime" to eventTime,
      "eventUsers" to arrayListOf(auth.currentUser?.uid.toString()),
      "eventAlerts" to mapOf(
        "isToday" to (selectedItemPosition == 0),
        "isDayAgo" to (selectedItemPosition == 1),
        "isWeekAgo" to (selectedItemPosition == 2),
        "isNone" to (selectedItemPosition == 3),
      ),
      "eventAuthorID" to auth.currentUser?.uid,
      "eventIsTodo" to false,
      "eventCheckUsers" to arrayListOf<String>(),
      "eventBackgroundColor" to dataColor,
      "eventLocation" to GeoPoint(
        dataLocation["latitude"]!!,
        dataLocation["longitude"]!!
      ),
      "eventLinkID" to eventLinkID
    )
  }

  private fun onRangeStartButtonClick() {
    if (!isAllDay) {
      isRangeStartClicked = !isRangeStartClicked
      isRangeEndClicked = false
      onClickedRangeDateButton()
    }
  }

  private fun onRangeEndButtonClick() {
    if (!isAllDay) {
      isRangeStartClicked = false
      isRangeEndClicked = !isRangeEndClicked
      onClickedRangeDateButton()
    }
  }

  private fun onLocationButtonClick() {
    fetchCoordinatesFromAddress(binding.scheduleFormContentLocationEdittext.text.toString())
  }


  private fun changeDateTextView() {
    val formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일 EEEE")
    binding.scheduleRangeStartDate.text = selectedStartDate.format(formatter)
    binding.scheduleRangeEndDate.text = selectedEndDate.format(formatter)
  }

  private fun onDayButtonClick() {
    isAllDay = true
    updateButtonColors(
      selectedDayButton = binding.scheduleDayButton,
      unselectedDayButton = binding.scheduleRangeButton,
      hintColorResources = arrayOf(R.color.hint_text, R.color.hint_text, R.color.hint_text, R.color.hint_text),
    )
    binding.scheduleRangeDatePickerLayout.visibility = View.GONE
    selectedEndDate = selectedStartDate
    changeDateTextView()
  }

  private fun onRangeButtonClick() {
    isAllDay = false
    updateButtonColors(
      selectedDayButton = binding.scheduleRangeButton,
      unselectedDayButton = binding.scheduleDayButton,
      hintColorResources = arrayOf(R.color.text, R.color.text, R.color.text, R.color.text),
    )
    binding.scheduleRangeDatePickerLayout.visibility = View.GONE
  }

  private fun updateButtonColors(selectedDayButton: Button, unselectedDayButton: Button, hintColorResources: Array<Int>) {
    selectedDayButton.background = ContextCompat.getDrawable(requireContext(), if (selectedDayButton == binding.scheduleDayButton) R.drawable.form_left_button_shape else R.drawable.form_right_button_shape)
    selectedDayButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
    unselectedDayButton.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
    unselectedDayButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.text))

    binding.scheduleRangeStartTitle.setTextColor(ContextCompat.getColor(requireContext(), hintColorResources[0]))
    binding.scheduleRangeStartDate.setTextColor(ContextCompat.getColor(requireContext(), hintColorResources[1]))
    binding.scheduleRangeEndTitle.setTextColor(ContextCompat.getColor(requireContext(), hintColorResources[2]))
    binding.scheduleRangeEndDate.setTextColor(ContextCompat.getColor(requireContext(), hintColorResources[3]))
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
    }
  }

  private fun fetchAddressFromCoordinates(latitude: Double, longitude: Double) {
    val apiKey = getString(R.string.google_map_api_key)
    val language = "ko"

    googleReverseGeocodingApi.getGeocode("$latitude,$longitude", apiKey, language).enqueue(object: Callback<ReverseGeocodingResponse> {
      override fun onResponse(call: Call<ReverseGeocodingResponse>, response: Response<ReverseGeocodingResponse>) {
        if (response.isSuccessful) {
          val geocodingResponse = response.body()
          val result = geocodingResponse?.results?.firstOrNull()

          if (result != null) {
            val address = result.formatted_address
            binding.scheduleFormContentLocationEdittext.text = Editable.Factory.getInstance().newEditable(address)
            Log.d("Geocoding", "Full Address: $address")
          } else {
            Log.d("Geocoding", "No address found for the coordinates.")
          }
        }
      }

      override fun onFailure(call: Call<ReverseGeocodingResponse>, t: Throwable) {
        Log.d("Geocoding", "Failed to get geocoding result: $t")
      }
    })
  }


  private fun fetchCoordinatesFromAddress(address: String) {
    val apiKey = getString(R.string.google_map_api_key)
    val language = "ko"

    googleGeocodingApi.getLatLngFromAddress(address, apiKey, language).enqueue(object : Callback<GeocodingResponse> {
      override fun onResponse(call: Call<GeocodingResponse>, response: Response<GeocodingResponse>) {
        response.body()?.let { geocodingResponse ->
          geocodingResponse.results.firstOrNull()?.run {
            val latLng = geometry.location

            binding.scheduleFormContentLocationEdittext.text = Editable.Factory.getInstance().newEditable(formatted_address)
            dataLocation = mapOf(
              "latitude" to latLng.lat,
              "longitude" to latLng.lng
            )
          } ?: run {
            Toast.makeText(requireContext(), getString(R.string.address_not_found), Toast.LENGTH_SHORT).show()
            Log.d("Geocoding", "No coordinates found for the address.")
          }
        } ?: run {
          Log.d("Geocoding", "Error occurred: ${response.errorBody()?.string()}")
        }
      }

      override fun onFailure(call: Call<GeocodingResponse>, t: Throwable) {
        Log.d("Geocoding", "Failed to get geocoding result: $t")
      }
    })
  }


  private fun onShowColorPicker() {
    val colorPickerBottomSheet = ColorPickerBottomSheet()
    colorPickerBottomSheet.setOnColorSelectedListener { selectedColor ->
        dataColor = mapOf(
          "alphaColor" to Color.alpha(selectedColor),
          "redColor" to Color.red(selectedColor),
          "greenColor" to Color.green(selectedColor),
          "blueColor" to Color.blue(selectedColor)
        )
        data = data.copy(eventBackgroundColor = dataColor)
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
