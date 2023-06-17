package com.daryukim.plancation
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleGeocodingApi {
  @GET("maps/api/geocode/json")
  fun getLatLngFromAddress(
    @Query("address") address: String,
    @Query("key") apiKey: String,
    @Query("language") language: String
  ): Call<GeocodingResponse>
}

data class GeocodingResponse(
  val results: List<GeocodingResult>,
  val status: String
)

data class GeocodingResult(
  val geometry: Geometry,
  val formatted_address: String
)

data class Geometry(
  val location: LatLng
)

data class LatLng(
  val lat: Double,
  val lng: Double
)

