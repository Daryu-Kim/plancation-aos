package com.daryukim.plancation

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.daryukim.plancation.databinding.FragmentMypageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class MyPageFragment : Fragment() {
  // Binding 객체 선언
  private var _binding: FragmentMypageBinding? = null
  private val binding get() = _binding!!

  // FirebaseAuth 인스턴스 생성
  private val auth = FirebaseAuth.getInstance()

  // 프로필 변경 전 이름 및 이메일 변수 선언
  private var beforeName = ""
  private var beforeEmail = ""

  // 뷰 초기화 및 바인딩 설정
  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    _binding = FragmentMypageBinding.inflate(inflater, container, false)
    val view = binding.root

    setupForm()
    setupChangeListeners()
    setupClickListeners()

    return view
  }

  // 사용자 프로필 불러와서 폼에 설정하는 메서드
  private fun setupForm() {
    beforeName = auth.currentUser?.displayName.toString()
    beforeEmail = auth.currentUser?.email.toString()
    binding.mypageName.text = Editable.Factory.getInstance().newEditable(beforeName)
    binding.mypageEmail.text = Editable.Factory.getInstance().newEditable(beforeEmail)
    binding.mypageUserName.text = beforeName
  }

  // 변경 사항이 있는지 확인하고 저장 버튼의 활성화를 제어하는 메서드
  private fun isFormChanged() {
    binding.mypageSubmit.isEnabled = binding.mypageName.text.toString() != beforeName || binding.mypageEmail.text.toString() != beforeEmail
  }

  // 텍스트 변경 리스너를 설정하는 메서드
  private fun setupChangeListeners() {
    val textChangedWatcher = object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

      // 텍스트가 변경될 때 현재 폼 상태를 확인하여 활성화 상태를 변경함
      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        isFormChanged()
      }

      override fun afterTextChanged(s: Editable?) {}
    }

    // 텍스트 변경 리스너를 "이름"과 "이메일" 입력란에 추가
    binding.mypageName.addTextChangedListener(textChangedWatcher)
    binding.mypageEmail.addTextChangedListener(textChangedWatcher)
  }

  // 클릭 리스너를 설정하는 메서드
  private fun setupClickListeners() {
    // 로그아웃 버튼 클릭 시 로그아웃 폼을 띄우는 이벤트 처리
    binding.mypageLogout.setOnClickListener {
      val logoutFormFragment = LogoutFormFragment()
      logoutFormFragment.show(childFragmentManager, logoutFormFragment.tag)
    }

    // 프로필 저장 버튼 클릭 시 프로필 변경을 시도하는 이벤트 처리
    binding.mypageSubmit.setOnClickListener {
      val user = auth.currentUser
      val profileUpdates = UserProfileChangeRequest.Builder()
        .setDisplayName(binding.mypageName.text.toString())
        .build()

      // 이름 변경 요청
      user?.updateProfile(profileUpdates)?.addOnCompleteListener { updateNameTask ->
        if (updateNameTask.isSuccessful) {
          // 이름 변경 성공 시 화면에 표시된 이름 업데이트
          binding.mypageUserName.text = binding.mypageName.text
          // 이메일 변경 요청
          user.updateEmail(binding.mypageEmail.text.toString())
            .addOnCompleteListener { updateEmailTask ->
              if (updateEmailTask.isSuccessful) {
                Toast.makeText(requireContext(), "프로필 변경에 성공했습니다!", Toast.LENGTH_SHORT).show()
              } else {
                Toast.makeText(requireContext(), "이메일 변경에 실패했습니다!", Toast.LENGTH_SHORT).show()
              }
            }
        } else {
          Toast.makeText(requireContext(), "이름 변경에 실패했습니다", Toast.LENGTH_SHORT).show()
        }
        isFormChanged()
      }
    }
  }

  // 프래그먼트가 파괴될 때 바인딩 객체를 해제하는 메서드
  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}
