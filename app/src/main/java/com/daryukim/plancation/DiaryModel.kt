package com.daryukim.plancation

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class DiaryModel(
  val postID: String = "",
  val postTitle: String = "",
  val postTime: Timestamp = Timestamp.now(),
  val postImage: String = "",
  val postContent: String = "",
  val postAuthorID: String = "",
): Parcelable {
  constructor(parcel: Parcel) : this(
    parcel.readString() ?: "",
    parcel.readString() ?: "",
    parcel.readParcelable(Timestamp::class.java.classLoader)!!,
    parcel.readString() ?: "",
    parcel.readString() ?: "",
    parcel.readString() ?: "",
  )

  override fun writeToParcel(parcel: Parcel, flags: Int) {
    parcel.writeString(postID)
    parcel.writeString(postTitle)
    parcel.writeParcelable(postTime, flags)
    parcel.writeString(postImage)
    parcel.writeString(postContent)
    parcel.writeString(postAuthorID)
  }

  override fun describeContents(): Int {
    return 0
  }

  companion object CREATOR : Parcelable.Creator<DiaryModel> {
    override fun createFromParcel(parcel: Parcel): DiaryModel {
      return DiaryModel(parcel)
    }

    override fun newArray(size: Int): Array<DiaryModel?> {
      return arrayOfNulls(size)
    }

    fun fromDocument(document: MutableMap<String, Any>): DiaryModel {
      return DiaryModel(
        postID = document["postID"] as String,
        postTitle = document["postTitle"] as String,
        postTime = document["postTime"] as Timestamp,
        postImage = document["postImage"] as String,
        postContent = document["postContent"] as String,
        postAuthorID = document["postAuthorID"] as String
      )
    }
  }
}