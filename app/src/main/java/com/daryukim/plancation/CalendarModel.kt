package com.daryukim.plancation

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class CalendarModel(
  val calendarID: String = "",
  val calendarTitle: String = "",
  val calendarAuthorID: String = "",
  val calendarUsers: ArrayList<String> = ArrayList(),
): Parcelable {
  constructor(parcel: Parcel) : this(
    parcel.readString() ?: "",
    parcel.readString() ?: "",
    parcel.readString() ?: "",
    parcel.createStringArrayList() ?: arrayListOf()
  )

  override fun writeToParcel(parcel: Parcel, flags: Int) {
    parcel.writeString(calendarID)
    parcel.writeString(calendarTitle)
    parcel.writeString(calendarAuthorID)
    parcel.writeStringList(calendarUsers)
  }

  override fun describeContents(): Int {
    return 0
  }

  companion object CREATOR : Parcelable.Creator<CalendarModel> {
    override fun createFromParcel(parcel: Parcel): CalendarModel {
      return CalendarModel(parcel)
    }

    override fun newArray(size: Int): Array<CalendarModel?> {
      return arrayOfNulls(size)
    }

    fun fromDocument(document: MutableMap<String, Any>): CalendarModel {
      return CalendarModel(
        calendarID = document["calendarID"] as String,
        calendarTitle = document["calendarTitle"] as String,
        calendarAuthorID = document["calendarAuthorID"] as String,
        calendarUsers = document["calendarUsers"] as ArrayList<String>,
      )
    }
  }
}