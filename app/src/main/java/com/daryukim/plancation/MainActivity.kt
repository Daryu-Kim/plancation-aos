package com.daryukim.plancation

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.daryukim.plancation.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.dynamiclinks.ktx.*
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var selectedItem = 0
    private var isOpenedCurrentCalendar = false
    private var isOpenedCurrentCalendarUsers = false
    private var calendarList: ArrayList<CalendarModel> = ArrayList()
    private var backPressedTime : Long = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setUpCurrentCalendarListView()
        setUpCurrentCalendarUsersListView()

        binding.appBarSidebarBtn.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
            openCurrentCalendarList(false)
            openCurrentCalendarUsersList(false)
        }

        binding.sideSettingBtn.setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
        }

        binding.sideCurrentCalendarLayout.setOnClickListener {
            openCurrentCalendarList(isOpenedCurrentCalendar)
        }

        binding.sideCreateCalendarLayout.setOnClickListener {
            val createCalendarBottomSheet = CreateCalendarBottomSheet()
            createCalendarBottomSheet.setOnFormSubmittedListener { _ ->
                setUpCurrentCalendarListView()
                setUpCurrentCalendarUsersListView()
            }
            createCalendarBottomSheet.show(supportFragmentManager, "createCalendar")
        }

        binding.sideInviteCalendarLayout.setOnClickListener {
            val uniqueId = Application.prefs.getString("currentCalendar", Application.auth.currentUser!!.uid) // 이 곳에 고유한 식별자가 포함되면 된다.
            var domainUriPrefix = "https://plancation.page.link" // Firebase Console에서 생성된 접두사
            var link = "https://plancation.web.app/invite?referrerId=$uniqueId"
            val dynamicLink = Firebase.dynamicLinks.dynamicLink {
                this.link = Uri.parse(link)
                this.domainUriPrefix = domainUriPrefix
                androidParameters("com.daryukim.plancation") {
                    fallbackUrl = Uri.parse(getString(R.string.android_app_link))
                }
                iosParameters("com.daryukim.plancation") {
                    setFallbackUrl(Uri.parse(getString(R.string.ios_app_link)))
                }
            }

            val inviteLink = dynamicLink.uri.toString()

            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "App & Calendar invite")
            sharingIntent.putExtra(Intent.EXTRA_TEXT, "당신을 Plancation의 '${binding.appBarTitle.text}' 캘린더로 초대합니다! 링크를 클릭하여 초대를 승인해주세요!: $inviteLink")

            startActivity(Intent.createChooser(sharingIntent, "App & Calendar Invite"))
        }

        binding.sideCurrentCalendarUsersLayout.setOnClickListener {
            openCurrentCalendarUsersList(isOpenedCurrentCalendarUsers)
        }

        binding.bottomNavigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        loadFragment(CalendarFragment())
        setCalendarTitle()
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

    private fun openCurrentCalendarUsersList(isOpened: Boolean) {
        isOpenedCurrentCalendarUsers = !isOpened
        if (isOpened) {
            binding.sideCurrentCalendarUserList.visibility = View.VISIBLE
            binding.sideCurrentCalendarUsersArrow.background = ContextCompat.getDrawable(this, R.drawable.ic_down)
        } else {
            binding.sideCurrentCalendarUserList.visibility = View.GONE
            binding.sideCurrentCalendarUsersArrow.background = ContextCompat.getDrawable(this, R.drawable.ic_right)
        }
    }

    private fun setUpCurrentCalendarListView() {
        calendarList.clear()
        Application.db.collection("Calendars")
            .whereArrayContains("calendarUsers", Application.auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener { calendars ->
                for (calendar in calendars) {
                    calendarList.add(CalendarModel.fromDocument(calendar.data))
                    if (calendar.data.get("calendarID").toString() == Application.prefs.getString("currentCalendar", Application.auth.currentUser!!.uid)) {
                        binding.sideCurrentCalendarName.text = calendar.data.get("calendarTitle").toString()
                    }
                }
                val currentCalendarAdapter = CurrentCalendarAdapter(calendarList)
                currentCalendarAdapter.setOnClickListener { calendarModel ->
                    Application.prefs.setString("currentCalendar", calendarModel.calendarID)
                    Toast.makeText(this, "${calendarModel.calendarTitle} 캘린더로 변경되었습니다!", Toast.LENGTH_SHORT).show()
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    binding.bottomNavigation.selectedItemId = R.id.nav_calendar
                    binding.appBarTitle.text = calendarModel.calendarTitle
                    binding.sideCurrentCalendarName.text = calendarModel.calendarTitle
                }
                binding.sideCurrentCalendarList.apply {
                    layoutManager = LinearLayoutManager(this@MainActivity)
                    adapter = currentCalendarAdapter
                }
            }
    }

    private fun setUpCurrentCalendarUsersListView() {
        Application.db.collection("Calendars")
            .document(Application.prefs.getString("currentCalendar", Application.auth.currentUser!!.uid))
            .get()
            .addOnSuccessListener { calendar ->
                val userList = calendar.data!!["calendarUsers"] as ArrayList<String>
                Toast.makeText(this, userList.toString(), Toast.LENGTH_SHORT).show()
                val currentCalendarUsersAdapter = CurrentCalendarUsersAdapter(userList)
                binding.sideCurrentCalendarUserList.apply {
                    layoutManager = LinearLayoutManager(this@MainActivity)
                    adapter = currentCalendarUsersAdapter
                }
            }
    }

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        if (item.itemId != selectedItem) {
            selectedItem = item.itemId
            val selectedFragment: Fragment = when (selectedItem) {
                R.id.nav_calendar -> CalendarFragment()
                R.id.nav_todo -> TodoFragment()
                R.id.nav_ai -> AIFragment()
                R.id.nav_diary -> DiaryFragment()
                R.id.nav_my -> MyPageFragment()
                else -> CalendarFragment()
            }
            binding.appBarTitle.text = when (selectedItem) {
                R.id.nav_calendar -> "캘린더"
                R.id.nav_todo -> "할 일 목록"
                R.id.nav_ai -> "AI 스케줄링"
                R.id.nav_diary -> "기록"
                R.id.nav_my -> "내 계정"
                else -> "캘린더"
            }
            if (selectedItem == R.id.nav_calendar) {
                setCalendarTitle()
            }
            loadFragment(selectedFragment)
        } else {
            val selectedFragment: Fragment = when (selectedItem) {
                R.id.nav_calendar -> CalendarFragment()
                R.id.nav_todo -> TodoFragment()
                R.id.nav_diary -> DiaryFragment()
                R.id.nav_my -> MyPageFragment()
                else -> CalendarFragment()
            }
            refreshFragment(selectedFragment)
        }
        true
    }

    private fun setCalendarTitle() {
        Application.db.collection("Calendars")
            .document(Application.prefs.getString("currentCalendar", Application.auth.currentUser!!.uid))
            .get()
            .addOnSuccessListener {
                if (it.data != null) {
                    binding.appBarTitle.text = it.data!!["calendarTitle"].toString()
                }
            }
    }

    private fun loadFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_layout, fragment).commit()
        fragmentTransaction.addToBackStack(null)
        selectedItem = 0
    }

    private fun refreshFragment(selectedFragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .detach(selectedFragment)
            .attach(selectedFragment)
            .commit()
        selectedItem = 0
    }
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (System.currentTimeMillis() - backPressedTime < 2000) {
            finish()
            return
        }

        Toast.makeText(this, "한번 더 뒤로가기 하시면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show()
        backPressedTime = System.currentTimeMillis()
    }
}