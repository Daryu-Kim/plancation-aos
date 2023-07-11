package com.daryukim.plancation

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.daryukim.plancation.databinding.ActivityAiResultBinding
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okio.IOException
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*
import java.util.concurrent.TimeUnit

data class GptRequest(
  val prompt: String,
  val max_tokens: Int,
  val model: String
)

data class GptResponse(
  val choices: List<Choice>
)

data class Choice(
  val text: String
)

interface GptApi {
  @Headers(
    "Content-Type: application/json",
    "Authorization: Bearer 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"
  )
  @POST("/v1/completions")
  fun getCompletion(
    @Body requestBody: GptRequest
  ): Call<GptResponse>
}

class AIResultActivity : AppCompatActivity() {
  private lateinit var binding: ActivityAiResultBinding
  private var resultList: ArrayList<AIResultModel> = arrayListOf()
  private var isLoading: Boolean = false

  val client = OkHttpClient.Builder()
    .connectTimeout(120, TimeUnit.SECONDS)
    .readTimeout(120, TimeUnit.SECONDS)
    .writeTimeout(120, TimeUnit.SECONDS)
    .build()

  val retrofit = Retrofit.Builder()
    .baseUrl("https://api.openai.com")
    .client(client)
    .addConverterFactory(GsonConverterFactory.create())
    .build()

  val api = retrofit.create(GptApi::class.java)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityAiResultBinding.inflate(layoutInflater)
    val view = binding.root
    setContentView(view)

    val promptList = intent.getStringArrayListExtra("prompt")

    if (promptList != null) {
      for (prompt in promptList) {
        sendRequest(prompt)
      }
    }

    binding.aiResultBtnCancel.setOnClickListener {
      finish()
    }

    binding.appBarPrev.setOnClickListener {
      finish()
    }

    binding.aiResultErrorPrev.setOnClickListener {
      finish()
    }
  }

  fun sendRequest(question: String) {
    val gson = Gson()
    val request = GptRequest(
      prompt = question,
      max_tokens = 1023,
      model = "text-davinci-003"
    )

    CoroutineScope(Dispatchers.IO).launch {
      isLoading = true
      val call = api.getCompletion(request)
      val response = call.execute()
      isLoading = false

      if (response.isSuccessful) {
        val choice = response.body()?.choices?.get(0)
        CoroutineScope(Dispatchers.Main).launch {
          choice?.text?.let {
            Log.d("GPT", it)
            try {
              val jsonMap: List<Map<String, Any>>? = gson.fromJson(it, object : TypeToken<List<Map<String, Any>>>() {}.type)
              if (jsonMap != null) {
                for (map in jsonMap) {
                  val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd") // 날짜 형식 지정
                  val timeFormatter = DateTimeFormatter.ofPattern("HH:mm") // 날짜 형식 지정
                  var model = AIResultModel()

                  model = model.copy(
                    date = LocalDate.parse(map["date"].toString(), dateFormatter),
                    startTime = LocalTime.parse(map["startTime"].toString(), timeFormatter),
                    endTime = LocalTime.parse(map["endTime"].toString(), timeFormatter),
                    title = map["title"].toString()
                  )
                  resultList.add(model)
                }
              }
                setupResult()
            } catch (e: JsonSyntaxException) {
              setupResultError()
            } catch (e: DateTimeParseException) {
              setupResultError()
            }
          }
        }
      } else {
        CoroutineScope(Dispatchers.Main).launch {
          Log.d("GPT", "Error: ${response.code()} - ${response.message()}")
        }
      }
    }
  }

  private fun setupResult() {
    binding.aiResultLayout.visibility = View.VISIBLE
    binding.aiResultProgress.visibility = View.GONE
    binding.aiResultError.visibility = View.GONE

    binding.aiResultRv.apply {
      layoutManager = LinearLayoutManager(this@AIResultActivity)
      adapter = AIResultItemAdapter(resultList)
    }
  }

  private fun setupResultError() {
    binding.aiResultLayout.visibility = View.GONE
    binding.aiResultProgress.visibility = View.GONE
    binding.aiResultError.visibility = View.VISIBLE
  }
}
