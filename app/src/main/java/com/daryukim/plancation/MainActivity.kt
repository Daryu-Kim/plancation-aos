package com.daryukim.plancation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.daryukim.plancation.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var selectedItem = 0
    private var isOpenedCurrentCalendar = false
    private var calendarList: ArrayList<CalendarModel> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setUpCurrentCalendarListView()

        binding.appBarSidebarBtn.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.sideCurrentCalendarLayout.setOnClickListener {
            openCurrentCalendarList(isOpenedCurrentCalendar)
        }

        binding.bottomNavigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        loadFragment(CalendarFragment())
    }

    private fun openCurrentCalendarList(isOpened: Boolean) {
        isOpenedCurrentCalendar = !isOpened
        if (isOpened) {
            binding.sideCurrentCalendarList.visibility = View.VISIBLE
            binding.sideCurrentCalendarArrow.background = ContextCompat.getDrawable(this, R.drawable.ic_down)
        } else {
            binding.sideCurrentCalendarList.visibility = View.GONE
            binding.sideCurrentCalendarArrow.background = ContextCompat.getDrawable(this, R.drawable.ic_right)
        }
    }

    private fun setUpCurrentCalendarListView() {
        val currentCalendarAdapter = CurrentCalendarAdapter(calendarList)
        currentCalendarAdapter.setOnClickListener { calendarModel ->
            Application.prefs.setString("currentCalendar", calendarModel.calendarID)
        }
        binding.sideCurrentCalendarList.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = currentCalendarAdapter
        }
    }

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        if (item.itemId != selectedItem) {
            selectedItem = item.itemId
            val selectedFragment: Fragment = when (selectedItem) {
                R.id.nav_calendar -> CalendarFragment()
                R.id.nav_todo -> TodoFragment()
                R.id.nav_diary -> DiaryFragment()
                R.id.nav_my -> MyPageFragment()
                else -> CalendarFragment()
            }
            binding.appBarTitle.text = when (selectedItem) {
                R.id.nav_calendar -> "캘린더"
                R.id.nav_todo -> "할 일 목록"
                R.id.nav_diary -> "기록"
                R.id.nav_my -> "내 계정"
                else -> "캘린더"
            }
            loadFragment(selectedFragment)
        }
        true
    }

    private fun loadFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_layout, fragment).commit()
        fragmentTransaction.addToBackStack(null)
    }
}