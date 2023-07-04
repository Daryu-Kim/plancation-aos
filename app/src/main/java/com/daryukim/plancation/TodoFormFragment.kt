package com.daryukim.plancation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.daryukim.plancation.databinding.FragmentTodoFormBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.ktx.Firebase
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap

class TodoFormFragment : BottomSheetDialogFragment() {
  private var _binding: FragmentTodoFormBinding? = null
  private val binding get() = _binding!!

  private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
  private val auth: FirebaseAuth = Firebase.auth
  private lateinit var onFormSubmittedListener: (Boolean) -> Unit

  private var data: ScheduleModel = ScheduleModel()
  private var isAllDay: Boolean = true

  private var isRangeStartClicked = false
  private var isRangeEndClicked = false

  private var selectedStartTime: LocalTime = LocalTime.now()
  private var selectedEndTime: LocalTime = LocalTime.now()

  init {
    TodoUtil.checkedUserList.value = arrayListOf()
  }

  // 프래그먼트 생성 시 뷰 설정
  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    val view = inflateBinding(inflater, container)

    setupForm()
    setupDates()
    setupSpinners()
    setupClickListeners()

    return view
  }

  private fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?): View {
    _binding = FragmentTodoFormBinding.inflate(inflater, container, false)
    return binding.root
  }

  private fun setupForm() {
    onDayButtonClick()
  }

  private fun setupDates() {

    binding.todoRangeTimePickerLayout.setOnTimeChangedListener { view, hourOfDay, minute ->
      if (isRangeStartClicked) {
        selectedStartTime = LocalTime.of(hourOfDay, minute)
        if (selectedStartTime > selectedEndTime) {
          selectedEndTime = selectedStartTime
        }
        changeDateTextView()
      } else if (isRangeEndClicked) {
        selectedEndTime = LocalTime.of(hourOfDay, minute)
        if (selectedStartTime > selectedEndTime) {
          selectedStartTime = selectedEndTime
        }
        changeDateTextView()
      }
      changeDateTextView()
    }
  }

  private fun setupSpinners() {
    val alertItems = resources.getStringArray(R.array.alert_array)
    val alertAdapter = CustomSpinnerAdapter(requireContext(), alertItems)

    binding.todoFormContentAlertSpinner.adapter = alertAdapter
    binding.todoFormContentAlertSpinner.setSelection(alertAdapter.count - 1)
    binding.todoFormContentAlertSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
      override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        alertAdapter.selectedPosition = position
        alertAdapter.notifyDataSetChanged()
      }

      override fun onNothingSelected(parent: AdapterView<*>?) {
      }
    }
  }

  private fun setupClickListeners() {
    binding.todoFormBarCancelButton.setOnClickListener { onCancelButtonClick() }
    binding.todoFormBarSubmitButton.setOnClickListener { onSubmitButtonClick() }
    binding.todoDayButton.setOnClickListener { onDayButtonClick() }
    binding.todoRangeButton.setOnClickListener { onRangeButtonClick() }
    binding.todoRangeStartButton.setOnClickListener { onRangeStartButtonClick() }
    binding.todoRangeEndButton.setOnClickListener { onRangeEndButtonClick() }
    binding.todoFormContentUserSelector.setOnClickListener { onUserSelectorButtonClick() }
  }

  private fun onCancelButtonClick() {
    dismiss()
  }

  private fun onSubmitButtonClick() {
    if (binding.todoFormContentTitle.text.isNotEmpty()) {
      try {
        addTodoToFirestore()
        Toast.makeText(requireContext(), "할 일을 성공적으로 생성했습니다!", Toast.LENGTH_SHORT).show()
        onFormSubmittedListener(true)
        dismiss()
      } catch (e: FirebaseFirestoreException) {
        Toast.makeText(requireContext(), "할 일을 생성하지 못했습니다!", Toast.LENGTH_SHORT).show()
      }
    } else {
      Toast.makeText(requireContext(), "할 일 제목을 입력해주세요!", Toast.LENGTH_SHORT).show()
      Toast.makeText(requireContext(), data.eventUsers.toString(), Toast.LENGTH_SHORT).show()
    }
  }

  private fun addTodoToFirestore() {
    val eventLinkID = UUID.randomUUID().toString()
    val eventID = UUID.randomUUID().toString()
    val eventData = createEventData(eventID, eventLinkID)

    db.collection("Calendars")
      .document(Application.prefs.getString("currentCalendar", auth.currentUser!!.uid))
      .collection("Events")
      .document(eventID)
      .set(eventData)
  }

  private fun createEventData(eventID: String, eventLinkID: String): HashMap<String, Any?> {
    val eventTime = Timestamp(Date.from(TodoUtil.selectedDate.value!!.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()))
    val eventStartTime = Timestamp(Date.from(TodoUtil.selectedDate.value!!.atTime(selectedStartTime.hour, selectedStartTime.minute).atZone(
      ZoneId.systemDefault()).toInstant()))
    val eventEndTime = Timestamp(Date.from(TodoUtil.selectedDate.value!!.atTime(selectedEndTime.hour, selectedEndTime.minute).atZone(
      ZoneId.systemDefault()).toInstant()))
    val selectedItemPosition = binding.todoFormContentAlertSpinner.selectedItemPosition

    if (data.eventUsers.isEmpty()) data.eventUsers.add(auth.currentUser?.uid.toString())

    return hashMapOf(
      "eventID" to eventID,
      "eventTitle" to binding.todoFormContentTitle.text.toString(),
      "eventTime" to eventTime,
      "eventUsers" to data.eventUsers,
      "eventAlerts" to mapOf(
        "isToday" to (selectedItemPosition == 0),
        "isDayAgo" to (selectedItemPosition == 1),
        "isWeekAgo" to (selectedItemPosition == 2),
        "isNone" to (selectedItemPosition == 3),
      ),
      "eventAuthorID" to auth.currentUser?.uid,
      "eventIsTodo" to true,
      "eventCheckUsers" to arrayListOf<String>(),
      "eventBackgroundColor" to mapOf<String, Int>(),
      "eventLocation" to GeoPoint(0.0, 0.0),
      "eventLinkID" to eventLinkID,
      "eventSTime" to eventStartTime,
      "eventETime" to eventEndTime
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


  private fun changeDateTextView() {
    val formatter = DateTimeFormatter.ofPattern("HH : mm")
    binding.todoRangeStartDate.text = selectedStartTime.format(formatter)
    binding.todoRangeEndDate.text = selectedEndTime.format(formatter)
  }

  private fun onDayButtonClick() {
    isAllDay = true
    updateButtonColors(
      selectedDayButton = binding.todoDayButton,
      unselectedDayButton = binding.todoRangeButton,
      hintColorResources = arrayOf(R.color.hint_text, R.color.hint_text, R.color.hint_text, R.color.hint_text),
    )
    binding.todoRangeTimePickerLayout.visibility = View.GONE
    selectedEndTime = selectedStartTime
    changeDateTextView()
  }

  private fun onRangeButtonClick() {
    isAllDay = false
    updateButtonColors(
      selectedDayButton = binding.todoRangeButton,
      unselectedDayButton = binding.todoDayButton,
      hintColorResources = arrayOf(R.color.text, R.color.text, R.color.text, R.color.text),
    )
    binding.todoRangeTimePickerLayout.visibility = View.GONE
  }

  private fun updateButtonColors(selectedDayButton: Button, unselectedDayButton: Button, hintColorResources: Array<Int>) {
    selectedDayButton.background = ContextCompat.getDrawable(requireContext(), if (selectedDayButton == binding.todoDayButton) R.drawable.form_left_button_shape else R.drawable.form_right_button_shape)
    selectedDayButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
    unselectedDayButton.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
    unselectedDayButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.text))

    binding.todoRangeStartTitle.setTextColor(ContextCompat.getColor(requireContext(), hintColorResources[0]))
    binding.todoRangeStartDate.setTextColor(ContextCompat.getColor(requireContext(), hintColorResources[1]))
    binding.todoRangeEndTitle.setTextColor(ContextCompat.getColor(requireContext(), hintColorResources[2]))
    binding.todoRangeEndDate.setTextColor(ContextCompat.getColor(requireContext(), hintColorResources[3]))
  }

  private fun onClickedRangeDateButton() {
    binding.todoRangeTimePickerLayout.visibility = View.GONE
    binding.todoRangeStartTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.text))
    binding.todoRangeStartDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.text))
    binding.todoRangeEndTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.text))
    binding.todoRangeEndDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.text))

    if (isRangeStartClicked || isRangeEndClicked) {
      binding.todoRangeTimePickerLayout.visibility = View.VISIBLE
    } else {
      return
    }

    if (isRangeStartClicked) {
      binding.todoRangeStartTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.accent))
      binding.todoRangeStartDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.accent))
    } else if (isRangeEndClicked) {
      binding.todoRangeEndTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.accent))
      binding.todoRangeEndDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.accent))
    }
  }


  private fun onUserSelectorButtonClick() {
    val userSelectorBottomSheet = UserSelectorBottomSheet(data.eventUsers)
    userSelectorBottomSheet.setOnUserSelectedListener { selectedUsers ->
        data = data.copy(eventUsers = selectedUsers)
    }
    userSelectorBottomSheet.show(parentFragmentManager, "userSelector")
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

  fun setOnFormSubmittedListener(listener: (Boolean) -> Unit) {
    onFormSubmittedListener = listener
  }
}
