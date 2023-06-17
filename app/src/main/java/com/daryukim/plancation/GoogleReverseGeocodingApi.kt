package com.daryukim.plancation
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleReverseGeocodingApi {
  @GET("maps/api/geocode/json")
  fun getGeocode(
    @Query("latlng") latlng: String,
    @Query("key") apiKey: String,
    @Query("language") language: String
  ): Call<ReverseGeocodingResponse>
}

data class ReverseGeocodingResponse(
  val results: List<ReverseGeocodingResult>,
  val status: String
)

data class ReverseGeocodingResult(
  val address_components: List<ReverseAddressComponent>,
  val formatted_address: String
)

data class ReverseAddressComponent(
  val long_name: String,
  val short_name: String,
  val types: List<String>
)
