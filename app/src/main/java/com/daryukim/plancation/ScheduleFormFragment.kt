package com.daryukim.plancation

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.daryukim.plancation.databinding.FragmentScheduleFormBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ScheduleFormFragment: BottomSheetDialogFragment() {
  private val googleGeocodingApi: GoogleGeocodingApi
  private var _binding: FragmentScheduleFormBinding? = null
  private val binding get() = _binding!!

  private var isModify: Boolean = false
  private var data: ScheduleModel = ScheduleModel()

  init {
    val retrofit = Retrofit.Builder()
      .baseUrl("https://maps.googleapis.com")
      .addConverterFactory(GsonConverterFactory.create())
      .build()

    googleGeocodingApi = retrofit.create(GoogleGeocodingApi::class.java)
  }

  // 프래그먼트 생성 시 뷰 설정
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    arguments?.let {
      isModify = it.getBoolean("isModify")
      data = it.getParcelable("data")!!
    }

    // 바인딩 객체를 생성하고 화면 레이아웃을 동적으로 연결합니다.
    _binding = FragmentScheduleFormBinding.inflate(inflater, container, false)
    val view = binding.root

    if (isModify) {
      binding.scheduleFormBarTitle.text = "이벤트 수정"
      binding.scheduleFormBarSubmitButton.text = "완료"

      binding.scheduleFormContentTitle.text = SpannableStringBuilder(data.eventTitle)
    } else {
      binding.scheduleFormBarTitle.text = "새로운 이벤트"
      binding.scheduleFormBarSubmitButton.text = "등록"
    }

    binding.scheduleFormBarCancelButton.setOnClickListener {
      dismiss()
    }

    binding.scheduleFormBarSubmitButton.setOnClickListener {
      if (isModify) {
        // 이벤트 수정
      } else {
        // 새로운 이벤트
      }
      dismiss()
    }

    binding.scheduleFormContentLocationButton.setOnClickListener {
      fetchCoordinatesFromAddress(binding.scheduleFormContentLocationEdittext.text.toString())
    }

    return view
  }

  private fun fetchCoordinatesFromAddress(address: String) {
    val apiKey = "AIzaSyDbUGBM3TEQKIR_Nah01_d8cxUq9eFdS3I"
    val language = "ko"
    Log.d("Geocoding", address)

    googleGeocodingApi.getLatLngFromAddress(address, apiKey, language).enqueue(object: Callback<GeocodingResponse> {
      override fun onResponse(call: Call<GeocodingResponse>, response: Response<GeocodingResponse>) {
        if (response.isSuccessful) {
          val geocodingResponse = response.body()
          val result = geocodingResponse?.results?.firstOrNull()

          if (result != null) {
            val latLng = result.geometry.location
            Log.d("Geocoding", "Latitude: ${latLng.lat}, Longitude: ${latLng.lng}")
          } else {
            Log.d("Geocoding", "No coordinates found for the address.")
          }
        } else {
          Log.d("Geocoding", "Error occurred: ${response.errorBody()?.string()}")
        }
      }

      override fun onFailure(call: Call<GeocodingResponse>, t: Throwable) {
        Log.d("Geocoding", "Failed to get geocoding result: $t")
      }
    })
  }

  companion object {
    // 팩토리 함수를 사용하여 프로퍼티값을 지정할 수 있는 프래그먼트 인스턴스를 생성합니다.
    fun newInstance(isModify: Boolean, data: ScheduleModel) =
      ScheduleFormFragment().apply {
        arguments = Bundle().apply {
          putBoolean("isModify", isModify)
          putParcelable("data", data)
        }
      }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
  }

  // 프래그먼트가 파괴될 때 바인딩을 해제합니다.
  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  override fun getTheme(): Int {
    return R.style.BottomSheetDialogStyle
  }
}
