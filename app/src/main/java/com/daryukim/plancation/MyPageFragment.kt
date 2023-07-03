package com.daryukim.plancation

import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.daryukim.plancation.databinding.FragmentMypageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class MyPageFragment : Fragment() {
  // Binding 객체 선언
  private var _binding: FragmentMypageBinding? = null
  private val binding get() = _binding!!

  private val db = FirebaseFirestore.getInstance()

  // FirebaseAuth 인스턴스 생성
  private val auth = FirebaseAuth.getInstance()

  // 프로필 변경 전 이름 및 이메일 변수 선언
  private var beforeName = ""
  private var beforeEmail = ""

  private val pickImagesLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
    uri?.let { uploadImageToFirebaseStorage(it) }
  }

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
    setupUserProfileImage()
  }

  // 변경 사항이 있는지 확인하고 저장 버튼의 활성화를 제어하는 메서드
  private fun isFormChanged() {
    binding.mypageSubmit.isEnabled =
      binding.mypageName.text.toString() != beforeName || binding.mypageEmail.text.toString() != beforeEmail
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
    binding.mypageUserImgChange.setOnClickListener {
      pickImagesLauncher.launch("image/*")
    }

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
          db.collection("Users")
            .document(user.uid)
            .update("userName", binding.mypageName.text.toString())
            .addOnSuccessListener {
              binding.mypageUserName.text = binding.mypageName.text
              // 이메일 변경 요청
              user.updateEmail(binding.mypageEmail.text.toString())
                .addOnCompleteListener { updateEmailTask ->
                  if (updateEmailTask.isSuccessful) {
                    Toast.makeText(requireContext(), "프로필 변경에 성공했습니다!", Toast.LENGTH_SHORT).show()
                    binding.mypageSubmit.isEnabled = false
                  } else {
                    Toast.makeText(requireContext(), "이메일 변경에 실패했습니다!", Toast.LENGTH_SHORT).show()
                  }
                }
            }
        } else {
          Toast.makeText(requireContext(), "이름 변경에 실패했습니다", Toast.LENGTH_SHORT).show()
        }
        isFormChanged()
      }
    }
  }

  private fun setupUserProfileImage() {
    db.collection("Users")
      .document(auth.currentUser?.uid.toString())
      .get()
      .addOnSuccessListener { value ->
        if (value.get("userImagePath") == null) {
          binding.mypageUserImg.background = ContextCompat.getDrawable(requireContext(), R.drawable.ic_user_profile)
          binding.mypageUserName.text = value.get("userName") as String
          binding.mypageUserName.visibility = View.VISIBLE
        } else {
          Glide.with(requireContext())
            .asBitmap()
            .load(value.get("userImagePath") as String)
            .circleCrop()
            .into(object : CustomTarget<Bitmap>() {
              override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                val drawable: Drawable = BitmapDrawable(resources, resource)
                binding.mypageUserImg.background = drawable
              }

              override fun onLoadCleared(placeholder: Drawable?) {}
            })
          binding.mypageUserName.visibility = View.GONE
        }
      }
  }

  private fun uploadImageToFirebaseStorage(uri: Uri) {
    val storageReference = FirebaseStorage.getInstance().reference
    val imageRef = storageReference.child("Users/${auth.currentUser?.uid}/profile_image.png")

    // 이미지를 Firebase Storage에 업로드
    imageRef.putFile(uri)
      .addOnSuccessListener { uploadTask ->
        // 업로드 되었기 때문에, 이미지의 다운로드 URL을 얻어 Firestore에 저장
        imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
          val profileUpdates = UserProfileChangeRequest.Builder().setPhotoUri(downloadUri).build()
          auth.currentUser?.updateProfile(profileUpdates)?.addOnCompleteListener {
            if (it.isSuccessful) {
              updateFirestoreWithImageUrl(downloadUri.toString())
            }
          }
        }
      }
      .addOnFailureListener { exception ->
        // 업로드 실패시 처리
      }
  }

  private fun updateFirestoreWithImageUrl(imageUrl: String) {
    // "userImagePath" 필드에 이미지 URL을 저장한다.
    db.collection("Users")
      .document(auth.currentUser?.uid.toString())
      .update("userImagePath", imageUrl)
      .addOnSuccessListener {
        Toast.makeText(requireContext(), "프로필 사진이 업데이트 되었습니다!", Toast.LENGTH_SHORT).show()
        setupUserProfileImage()
      }
      .addOnFailureListener { e ->
        Toast.makeText(requireContext(), "프로필 사진이 업데이트되지 않았습니다!", Toast.LENGTH_SHORT).show()
      }
  }

  // 프래그먼트가 파괴될 때 바인딩 객체를 해제하는 메서드
  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}
