package com.daryukim.plancation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.daryukim.plancation.databinding.SheetDeleteAccountBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FieldValue

class DeleteAccountBottomSheet: BottomSheetDialogFragment() {
  private var _binding: SheetDeleteAccountBinding? = null
  private val binding get() = _binding!!

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    _binding = SheetDeleteAccountBinding.inflate(inflater, container, false)
    val view = binding.root

    binding.sheetCancelBtn.setOnClickListener {
      dismiss()
    }

    binding.sheetDeleteBtn.setOnClickListener {
      deleteAccount()
    }

    return view
  }

  private fun deleteAccount() {
    // 소유중인 캘린더 삭제 작업
    Application.db.collection("Calendars")
      .whereEqualTo("calendarAuthorID", Application.auth.currentUser!!.uid)
      .get()
      .addOnSuccessListener { calendars ->
        calendars.forEach { calendar ->
          Application.db.collection("Calendars")
            .document(calendar.data["calendarID"].toString())
            .collection("Events")
            .get()
            .addOnSuccessListener { events ->
              events.forEach { event ->
                event.reference.delete()
              }
              Application.db.collection("Calendars")
                .document(calendar.data["calendarID"].toString())
                .collection("Posts")
                .get()
                .addOnSuccessListener { posts ->
                  posts.forEach { post ->
                    post.reference.delete()
                  }
                  calendar.reference.delete()
                }
            }
        }
        // 참여중인 캘린더 Events 제외 및 게시 항목 삭제
        Application.db.collection("Calendars")
          .whereArrayContains("calendarUsers", Application.auth.currentUser!!.uid)
          .get()
          .addOnSuccessListener { otherCalendars ->
            otherCalendars.forEach { otherCalendar ->
              // Posts 삭제
              otherCalendar.reference
                .collection("Posts")
                .whereEqualTo("postAuthorID", Application.auth.currentUser!!.uid)
                .get()
                .addOnSuccessListener { posts ->
                  posts.forEach { post ->
                    post.reference.delete()
                  }
                  otherCalendar.reference
                    .collection("Events")
                    .whereEqualTo("eventAuthorID", Application.auth.currentUser!!.uid)
                    .get()
                    .addOnSuccessListener { authorEvents ->
                      authorEvents.forEach { authorEvent ->
                        authorEvent.reference.delete()
                      }
                      otherCalendar.reference
                        .collection("Events")
                        .whereArrayContains("eventUsers", Application.auth.currentUser!!.uid)
                        .get()
                        .addOnSuccessListener { userEvents ->
                          userEvents.forEach { userEvent ->
                            userEvent.reference.update("eventUsers", FieldValue.arrayRemove(Application.auth.currentUser!!.uid))
                          }
                          Application.db.collection("Users")
                            .document(Application.auth.currentUser!!.uid)
                            .delete()
                            .addOnSuccessListener {
                              Application.auth.currentUser!!.delete()
                                .addOnCompleteListener { task ->
                                  if (task.isSuccessful) {
                                    val intent = Intent(requireContext(), LoginMainActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    Toast.makeText(requireContext(), "계정 탈퇴가 완료되었습니다, 이용해주셔서 감사합니다!", Toast.LENGTH_SHORT).show()
                                    startActivity(intent)
                                  }
                                }
                            }
                        }
                    }
                }
            }
          }
      }
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