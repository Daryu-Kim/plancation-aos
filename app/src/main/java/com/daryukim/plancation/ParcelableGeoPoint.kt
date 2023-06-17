package com.daryukim.plancation
import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.GeoPoint

data class ParcelableGeoPoint(val geoPoint: GeoPoint) : Parcelable {
  constructor(parcel: Parcel) : this(
    GeoPoint(parcel.readDouble(), parcel.readDouble())
  )

  override fun writeToParcel(parcel: Parcel, flags: Int) {
    parcel.writeDouble(geoPoint.latitude)
    parcel.writeDouble(geoPoint.longitude)
  }

  override fun describeContents(): Int {
    return 0
  }

  companion object CREATOR : Parcelable.Creator<ParcelableGeoPoint> {
    override fun createFromParcel(parcel: Parcel): ParcelableGeoPoint {
      return ParcelableGeoPoint(parcel)
    }

    override fun newArray(size: Int): Array<ParcelableGeoPoint?> {
      return arrayOfNulls(size)
    }
  }
}