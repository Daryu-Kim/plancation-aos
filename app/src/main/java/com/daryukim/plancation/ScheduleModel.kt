package com.daryukim.plancation

import java.time.LocalDate

class ScheduleModel(
  val eventID: String,
  val eventSTime: LocalDate,
  val eventETime: LocalDate,
  val eventUsers: ArrayList<String>,
  val eventAlerts: ArrayList<ScheduleAlertModel>,
  val eventAuthorID: String,
  val eventIsTodo: Boolean,
  val eventCheckUsers: ArrayList<String>,
  val eventBackgroundColor: ScheduleColorModel
) {
}