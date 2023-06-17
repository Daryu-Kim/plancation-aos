package com.daryukim.plancation

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
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
  val eventLocation: ParcelableGeoPoint = ParcelableGeoPoint(GeoPoint(0.0, 0.0))
): Parcelable {
  constructor(parcel: Parcel) : this(
    parcel.readString() ?: "",
    parcel.readString() ?: "",
    parcel.readParcelable(Timestamp::class.java.classLoader)!!,
    parcel.createStringArrayList() ?: arrayListOf(),
    parcel.readHashMap(null) as Map<String, Any>,
    parcel.readString() ?: "",
    parcel.readByte() != 0.toByte(),
    parcel.createStringArrayList() ?: arrayListOf(),
    parcel.readHashMap(null) as Map<String, Int>,
    parcel.readParcelable(ParcelableGeoPoint::class.java.classLoader)!!
  )

  override fun writeToParcel(parcel: Parcel, flags: Int) {
    parcel.writeString(eventID)
    parcel.writeString(eventTitle)
    parcel.writeParcelable(eventTime, flags)
    parcel.writeStringList(eventUsers)
    parcel.writeMap(eventAlerts)
    parcel.writeString(eventAuthorID)
    parcel.writeByte(if (eventIsTodo) 1 else 0)
    parcel.writeStringList(eventCheckUsers)
    parcel.writeMap(eventBackgroundColor)
    parcel.writeParcelable(eventLocation, flags)
  }

  override fun describeContents(): Int {
    return 0
  }

  companion object CREATOR : Parcelable.Creator<ScheduleModel> {
    override fun createFromParcel(parcel: Parcel): ScheduleModel {
      return ScheduleModel(parcel)
    }

    override fun newArray(size: Int): Array<ScheduleModel?> {
      return arrayOfNulls(size)
    }

    fun fromDocument(document: DocumentSnapshot): ScheduleModel {
      return ScheduleModel(
        eventID = document.getString("eventID") ?: "",
        eventTitle = document.getString("eventTitle") ?: "",
        eventTime = document.getTimestamp("eventTime")!!,
        eventUsers = document.get("eventUsers") as ArrayList<String>,
        eventAlerts = document.get("eventAlerts") as Map<String, Any>,
        eventAuthorID = document.getString("eventAuthorID") ?: "",
        eventIsTodo = document.getBoolean("eventIsTodo") ?: false,
        eventCheckUsers = document.get("eventCheckUsers") as ArrayList<String>,
        eventBackgroundColor = document.get("eventBackgroundColor") as Map<String, Int>,
        eventLocation = ParcelableGeoPoint(document.getGeoPoint("eventLocation")!!)
      )
    }
  }
}