package com.daryukim.plancation

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.daryukim.plancation.databinding.ActivityRecentAlertBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

class RecentAlertActivity : AppCompatActivity() {
  private lateinit var binding: ActivityRecentAlertBinding
  private var eventList: ArrayList<ScheduleModel> = ArrayList()
  private lateinit var recentAlertAdapter: RecentAlertAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityRecentAlertBinding.inflate(layoutInflater)
    setContentView(binding.root)

    setupEventItems()

    binding.appBarCancel.setOnClickListener {
      finish()
    }

    binding.recentAlertRefresh.setOnRefreshListener {
      setupEventItems()
    }
  }

  private fun setupEventItems() {
    eventList.clear()
    fetchEventsDataFromFirestore { result ->
      eventList.addAll(result)
      setUpEventView()
      binding.recentAlertRefresh.isRefreshing = false
    }
  }

  private fun setUpEventView() {
    recentAlertAdapter = RecentAlertAdapter(eventList)
    recentAlertAdapter.onItemClickListener { fragment, model ->
      val resultIntent = Intent().apply {
        putExtra("resultFragment", fragment)
        putExtra("resultModel", model)
      }
      setResult(Activity.RESULT_OK, resultIntent)
      finish()
    }
    binding.recentAlertRv.apply {
      layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
      adapter = recentAlertAdapter
    }
    checkEmptyList()
  }

  private fun checkEmptyList() {
    if (recentAlertAdapter.itemCount == 0) { // 항목이 없는 경우
      binding.recentAlertEmptyLayout.visibility = View.VISIBLE
    } else {
      binding.recentAlertEmptyLayout.visibility = View.GONE
    }
  }

  private fun fetchEventsDataFromFirestore(onComplete: (List<ScheduleModel>) -> Unit) {
    val date = LocalDate.now().plusDays(7)
    val startDateTime: LocalDateTime = date.atStartOfDay()
    val startDate: Date = Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant())
    val dataList = mutableListOf<ScheduleModel>()

    Application.db.collection("Calendars")
      .document(Application.prefs.getString("currentCalendar", Application.auth.currentUser!!.uid))
      .collection("Events")
      .whereLessThan("eventTime", Timestamp(startDate))
      .limit(15)
      .orderBy("eventTime", Query.Direction.DESCENDING)
      .get()
      .addOnSuccessListener {documents ->
        if (documents != null) {
          for (document in documents) {
            dataList.add(ScheduleModel.fromDocument(document.data))
          }
          onComplete(dataList)
        } else {
          Application.prefs.setString("currentCalendar", Application.auth.currentUser!!.uid)
          setupEventItems()
        }
      }
      .addOnFailureListener {
        onComplete(listOf())
      }
  }
}
