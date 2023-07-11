package com.daryukim.plancation

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class AIResultModel(
  val date: LocalDate = LocalDate.now(),
  val startTime: LocalTime = LocalTime.now(),
  val endTime: LocalTime = LocalTime.now(),
  val title: String = "",
): Parcelable {
  constructor(parcel: Parcel) : this(
    LocalDate.ofEpochDay(parcel.readLong()),
    LocalTime.ofSecondOfDay(parcel.readLong() % 86400),
    LocalTime.ofSecondOfDay(parcel.readLong() % 86400),
    parcel.readString() ?: "",
  )

  override fun writeToParcel(parcel: Parcel, flags: Int) {
    parcel.writeLong(date.toEpochDay())
    parcel.writeLong(startTime.toSecondOfDay().toLong())
    parcel.writeLong(endTime.toSecondOfDay().toLong())
    parcel.writeString(title)
  }

  override fun describeContents(): Int {
    return 0
  }

  companion object CREATOR : Parcelable.Creator<AIResultModel> {
    override fun createFromParcel(parcel: Parcel): AIResultModel {
      return AIResultModel(parcel)
    }

    override fun newArray(size: Int): Array<AIResultModel?> {
      return arrayOfNulls(size)
    }

    fun fromDocument(document: MutableMap<String, Any>): AIResultModel {
      val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
      val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
      return AIResultModel(
        date = LocalDate.parse(document["date"].toString(), dateFormatter),
        startTime = LocalTime.parse(document["startTime"].toString(), timeFormatter),
        endTime = LocalTime.parse(document["endTime"].toString(), timeFormatter),
        title = document["title"] as String,
      )
    }
  }
}