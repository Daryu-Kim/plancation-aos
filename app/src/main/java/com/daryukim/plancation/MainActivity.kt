package com.daryukim.plancation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.daryukim.plancation.databinding.ActivityJoinBinding
import com.daryukim.plancation.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.bottomNavigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        loadFragment(CalendarFragment())
    }

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        val selectedFragment: Fragment = when (item.itemId) {
            R.id.nav_calendar -> CalendarFragment()
            R.id.nav_todo -> TodoFragment()
            R.id.nav_my -> MyPageFragment()
            else -> CalendarFragment()
        }
        binding.appBarTitle.text = when (item.itemId) {
            R.id.nav_calendar -> "캘린더"
            R.id.nav_todo -> "할 일 목록"
//            CalendarFragment() -> "캘린더"
//            CalendarFragment() -> "캘린더"
            R.id.nav_my -> "내 계정"
            else -> "캘린더"
        }
        loadFragment(selectedFragment)
        true
    }

    private fun loadFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_layout, fragment).commit()

    }
}