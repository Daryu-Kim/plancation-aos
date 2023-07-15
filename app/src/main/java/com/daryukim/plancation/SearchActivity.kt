package com.daryukim.plancation

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.daryukim.plancation.databinding.ActivitySearchBinding
import com.google.firebase.firestore.Query

class SearchActivity : AppCompatActivity() {
  private lateinit var binding: ActivitySearchBinding
  private var searchList: ArrayList<ScheduleModel> = arrayListOf()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivitySearchBinding.inflate(layoutInflater)
    setContentView(binding.root)

    binding.appBarCancel.setOnClickListener { finish() }
    binding.searchTitleEdit.addTextChangedListener {
      if (binding.searchTitleEdit.text.isEmpty()) {
        binding.searchTitleRemove.visibility = View.GONE
      } else {
        binding.searchTitleRemove.visibility = View.VISIBLE
      }
    }
    binding.searchTitleRemove.setOnClickListener {
      binding.searchTitleEdit.text = Editable.Factory.getInstance().newEditable("")
    }
    binding.searchTitleEdit.setOnEditorActionListener { _, actionId, _ ->
      if (actionId == EditorInfo.IME_ACTION_SEARCH) {
        searchScheduleAndTodos(binding.searchTitleEdit.text.toString(), "eventTitle")
      }
      return@setOnEditorActionListener true
    }

    setupSearchResult()
  }

  private fun setupSearchResult() {
    val searchAdapter = SearchAdapter(searchList)
    searchAdapter.onItemClickListener { fragment, model ->
      val resultIntent = Intent().apply {
        putExtra("resultFragment", fragment)
        putExtra("resultModel", model)
      }
      setResult(Activity.RESULT_OK, resultIntent)
      finish()
    }
    binding.searchResultRv.apply {
      layoutManager = LinearLayoutManager(this@SearchActivity)
      adapter = searchAdapter
    }
  }

  private fun searchScheduleAndTodos(searchWord : String, option : String) {
    if (searchWord.isEmpty()) {
      Toast.makeText(this, "검색할 내용을 입력해주세요!", Toast.LENGTH_SHORT).show()
    } else {
      Application.db.collection("Calendars")
        .document(Application.prefs.getString("currentCalendar", Application.auth.currentUser!!.uid))
        .collection("Events")
        .whereEqualTo("eventIsTodo", false)
        .orderBy("eventTime", Query.Direction.DESCENDING)
        .get()
        .addOnSuccessListener{ schedules ->
          searchList.clear()

          for (schedule in schedules.documents) {
            if (schedule.getString(option)!!.contains(searchWord)) {
              schedule.data?.let { ScheduleModel.fromDocument(it) }?.let { searchList.add(it) }
            }
          }

          if (searchList.isEmpty()) {
            binding.searchResultEmpty.visibility = View.VISIBLE
            binding.searchResultRv.visibility = View.GONE
            binding.searchResultSumLayout.visibility = View.GONE
          } else {
            binding.searchResultText.text = "'${binding.searchTitleEdit.text}'에 대한 검색 결과 ${searchList.size}건"
            binding.searchResultEmpty.visibility = View.GONE
            binding.searchResultRv.visibility = View.VISIBLE
            binding.searchResultSumLayout.visibility = View.VISIBLE
          }

          setupSearchResult()
        }
    }
  }
}
