package com.daryukim.plancation

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class ScheduleModel(
  val eventID: String = "",
  val eventTitle: String = "",
  val eventTime: Timestamp = Timestamp.now(),
  var eventUsers: ArrayList<String> = ArrayList(),
  val eventAlerts: Map<String, Any> = mapOf(),
  val eventAuthorID: String = "",
  val eventIsTodo: Boolean = false,
  val eventCheckUsers: ArrayList<String> = ArrayList(),
  val eventBackgroundColor: Map<String, Int> = mapOf(
    "alphaColor" to 255,
    "redColor" to 115,
    "greenColor" to 91,
    "blueColor" to 242
  ),
  val eventLocation: ParcelableGeoPoint = ParcelableGeoPoint(GeoPoint(0.0, 0.0)),
  val eventLinkID: String = "",
  val eventSTime: Timestamp = Timestamp.now(),
  val eventETime: Timestamp = Timestamp.now()
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
    parcel.readParcelable(ParcelableGeoPoint::class.java.classLoader)!!,
    parcel.readString() ?: "",
    parcel.readParcelable(Timestamp::class.java.classLoader)!!,
    parcel.readParcelable(Timestamp::class.java.classLoader)!!
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
    parcel.writeString(eventLinkID)
    parcel.writeParcelable(eventSTime, flags)
    parcel.writeParcelable(eventETime, flags)
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

    fun fromDocument(document: MutableMap<String, Any>): ScheduleModel {
      return ScheduleModel(
        eventID = document["eventID"] as String,
        eventTitle = document["eventTitle"] as String,
        eventTime = document["eventTime"] as Timestamp,
        eventUsers = document["eventUsers"] as ArrayList<String>,
        eventAlerts = document["eventAlerts"] as Map<String, Any>,
        eventAuthorID = document["eventAuthorID"] as String,
        eventIsTodo = document["eventIsTodo"] as Boolean,
        eventCheckUsers = document["eventCheckUsers"] as ArrayList<String>,
        eventBackgroundColor = document["eventBackgroundColor"] as Map<String, Int>,
        eventLocation = ParcelableGeoPoint(document["eventLocation"] as GeoPoint),
        eventLinkID = document["eventLinkID"] as String,
        eventSTime = document["eventSTime"] as Timestamp,
        eventETime = document["eventETime"] as Timestamp
      )
    }
  }
}