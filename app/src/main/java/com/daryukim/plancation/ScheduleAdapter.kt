package com.daryukim.plancation

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.location.Geocoder
import android.util.Log
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnCreateContextMenuListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit.DAYS
import java.util.Locale

class ScheduleAdapter(
  private val scheduleList: List<ScheduleModel>,
) :
  RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {
  private val googleReverseGeocodingApi: GoogleReverseGeocodingApi

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
    val itemView = LayoutInflater.from(parent.context).inflate(R.layout.schedule_item, parent, false)
    return ScheduleViewHolder(itemView)
  }

  init {
    val retrofit = Retrofit.Builder()
      .baseUrl("https://maps.googleapis.com")
      .addConverterFactory(GsonConverterFactory.create())
      .build()

    googleReverseGeocodingApi = retrofit.create(GoogleReverseGeocodingApi::class.java)
  }

  override fun getItemCount(): Int {
    return scheduleList.size
  }

  @SuppressLint("ResourceType", "SetTextI18n")
  override fun onBindViewHolder(holder: ScheduleViewHolder, @SuppressLint("RecyclerView") position: Int) {
    val schedule = scheduleList[position]
    val scheduleDate = schedule.eventTime.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    val scheduleDDay = DAYS.between(LocalDate.now(), scheduleDate)
    val scheduleGeoPoint = schedule.eventLocation.geoPoint

    holder.itemLayout.setCustomBackgroundTint(
      schedule.eventBackgroundColor,
      schedule.eventBackgroundColor["redColor"]!! +
              schedule.eventBackgroundColor["greenColor"]!! +
              schedule.eventBackgroundColor["blueColor"]!!,
      true)
    holder.itemRing.setCustomBackgroundTint(
      schedule.eventBackgroundColor,
      schedule.eventBackgroundColor["redColor"]!! +
              schedule.eventBackgroundColor["greenColor"]!! +
              schedule.eventBackgroundColor["blueColor"]!!,
      false)
    holder.itemTitle.text = schedule.eventTitle
    holder.itemTitle.setTextColor(
      if ((schedule.eventBackgroundColor["redColor"]!! +
        schedule.eventBackgroundColor["greenColor"]!! +
        schedule.eventBackgroundColor["blueColor"]!!) > 420)
      Color.BLACK
      else
      Color.WHITE
    )
    holder.itemLocation.setTextColor(
      if ((schedule.eventBackgroundColor["redColor"]!! +
        schedule.eventBackgroundColor["greenColor"]!! +
        schedule.eventBackgroundColor["blueColor"]!!) > 420)
      Color.DKGRAY
      else
      Color.LTGRAY
    )
    holder.itemDate.setTextColor(
      if ((schedule.eventBackgroundColor["redColor"]!! +
        schedule.eventBackgroundColor["greenColor"]!! +
        schedule.eventBackgroundColor["blueColor"]!!) > 420)
      Color.DKGRAY
      else
      Color.LTGRAY
    )
    when {
      scheduleDDay == 0L -> holder.itemDate.text = "D-Day"
      scheduleDDay > 0 -> holder.itemDate.text = "D-$scheduleDDay"
      else -> holder.itemDate.text = "이미 지난 날짜입니다!"
    }
    fetchAddressFromCoordinates(holder, scheduleGeoPoint.latitude, scheduleGeoPoint.longitude)
    holder.itemView.setOnClickListener(View.OnClickListener {
      Toast.makeText(holder.itemView.context, schedule.eventTitle, Toast.LENGTH_SHORT).show()
    })
  }

  inner class ScheduleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), OnCreateContextMenuListener {
    val itemLayout: View = itemView.findViewById(R.id.schedule_item_layout)
    val itemTitle: TextView = itemView.findViewById(R.id.schedule_item_title)
    val itemDate: TextView = itemView.findViewById(R.id.schedule_item_date)
    val itemLocation: TextView = itemView.findViewById(R.id.schedule_item_location)
    val itemRing: ImageView = itemView.findViewById(R.id.schedule_item_ring)

    init {
      itemLayout.setOnCreateContextMenuListener(this)
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
      menu?.add(adapterPosition, 0, 0 ,"수정")
      menu?.add(adapterPosition, 1, 1 ,"삭제")
    }
  }

  private fun View.setCustomBackgroundTint(
    color: Map<String, Int>,
    colorSum: Int,
    isLayout: Boolean
  ) {
    val tempColor: Int
    if (!isLayout) {
      tempColor = if (colorSum > 420) {
        Color.BLACK
      } else {
        Color.WHITE
      }
    } else {
      tempColor = Color.argb(
        color["alphaColor"]!!,
        color["redColor"]!!,
        color["greenColor"]!!,
        color["blueColor"]!!)
    }

    val tempColorStateList = ColorStateList.valueOf(tempColor)
    return ViewCompat.setBackgroundTintList(this, tempColorStateList)
  }

  private fun fetchAddressFromCoordinates(holder: ScheduleAdapter.ScheduleViewHolder, latitude: Double, longitude: Double) {
    val apiKey = "AIzaSyDbUGBM3TEQKIR_Nah01_d8cxUq9eFdS3I"
    val language = "ko"

    googleReverseGeocodingApi.getGeocode("$latitude,$longitude", apiKey, language).enqueue(object: Callback<ReverseGeocodingResponse> {
      override fun onResponse(call: Call<ReverseGeocodingResponse>, response: Response<ReverseGeocodingResponse>) {
        if (response.isSuccessful) {
          val geocodingResponse = response.body()
          val result = geocodingResponse?.results?.firstOrNull()

          if (result != null) {
            val address = result.formatted_address
            Log.d("Geocoding", "Full Address: $address")
            holder.itemLocation.text = "장소: $address"
          } else {
            Log.d("Geocoding", "No address found for the coordinates.")
            holder.itemLocation.text = "장소를 지정하지 않았습니다!"
          }
        } else {
          Log.d("Geocoding", "Error occurred: ${response.errorBody()?.string()}")
          holder.itemLocation.text = "장소를 지정하지 않았습니다!"
        }
      }

      override fun onFailure(call: Call<ReverseGeocodingResponse>, t: Throwable) {
        Log.d("Geocoding", "Failed to get geocoding result: $t")
        holder.itemLocation.text = "장소를 지정하지 않았습니다!"
      }
    })
  }

}