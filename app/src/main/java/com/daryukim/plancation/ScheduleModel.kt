package com.daryukim.plancation

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class ScheduleModel(
  val eventID: String = "",
  val eventTitle: String = "",
  val eventTime: Timestamp = Timestamp.now(),
  val eventUsers: ArrayList<String> = ArrayList(),
  val eventAlerts: Map<String, Any> = mapOf(),
  val eventAuthorID: String = "",
  val eventIsTodo: Boolean = false,
  val eventCheckUsers: ArrayList<String> = ArrayList(),
  val eventBackgroundColor: Map<String, Int> = mapOf(),
  val eventLocation: GeoPoint = GeoPoint(0.0, 0.0)
)