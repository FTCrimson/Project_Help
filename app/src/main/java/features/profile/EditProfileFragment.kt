// EditProfileFragment.kt
package com.example.project_helper.features.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.project_helper.R
import com.example.project_helper.databinding.FragmentEditProfileBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import data.auth.UserData
import java.util.UUID

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var selectedImageUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            Glide.with(this)
                .load(it)
                .into(binding.ivProfile)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        loadUserData()
        setupListeners()
    }

    private fun loadUserData() {
        val user = auth.currentUser
        user?.let {
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    val userData = document.toObject(UserData::class.java)
                    userData?.let {
                        binding.etUsername.setText(it.username ?: "")
                        binding.etEmail.setText(it.email ?: "")
                        binding.etPhone.setText(it.phone ?: "+7********")

                        Glide.with(this)
                            .load(it.avatarUrl)
                            .placeholder(R.drawable.ic_default_avatar)
                            .into(binding.ivProfile)
                    }
                }
        }
    }

    private fun setupListeners() {
        binding.btnChangeAvatar.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.btnSave.setOnClickListener {
            if (validateInputs()) {
                updateProfile()
            }
        }
    }

    private fun validateInputs(): Boolean {
        val username = binding.etUsername.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val oldPassword = binding.etOldPassword.text.toString().trim()
        val newPassword = binding.etNewPassword.text.toString().trim()

        if (!validateUsername(username)) return false
        if (!validateEmail(email)) return false
        if (!validatePhone(phone)) return false
        if (newPassword.isNotEmpty() && !validatePassword(newPassword)) return false
        if (newPassword.isNotEmpty() && oldPassword.isEmpty()) {
            binding.etOldPassword.error = "Введите старый пароль"
            return false
        }

        return true
    }

    private fun validateUsername(username: String): Boolean {
        return when {
            username.isEmpty() -> {
                binding.etUsername.error = "Введите имя пользователя"
                false
            }
            username.length < 3 -> {
                binding.etUsername.error = "Минимум 3 символа"
                false
            }
            !username.matches(Regex("^[a-zA-Z][a-zA-Z0-9_]*$")) -> {
                binding.etUsername.error = "Только буквы, цифры и _"
                false
            }
            else -> true
        }
    }

    private fun validateEmail(email: String): Boolean {
        val emailPattern = Regex("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", RegexOption.IGNORE_CASE)
        return when {
            email.isEmpty() -> {
                binding.etEmail.error = "Введите email"
                false
            }
            !emailPattern.matches(email) -> {
                binding.etEmail.error = "Некорректный email"
                false
            }
            else -> true
        }
    }

    private fun validatePhone(phone: String): Boolean {
        return if (phone.isEmpty() || !phone.matches(Regex("^\\+7\\d{10}$"))) {
            binding.etPhone.error = "Формат: +7XXXXXXXXXX"
            false
        } else true
    }

    private fun validatePassword(password: String): Boolean {
        return when {
            password.isEmpty() -> false
            password.length < 6 -> {
                binding.etNewPassword.error = "Минимум 6 символов"
                false
            }
            !password.any { it.isUpperCase() } -> {
                binding.etNewPassword.error = "Нужна хотя бы 1 заглавная буква"
                false
            }
            !password.any { it.isDigit() } -> {
                binding.etNewPassword.error = "Нужна хотя бы 1 цифра"
                false
            }
            password.contains(Regex("[,.!?]")) -> {
                binding.etNewPassword.error = "Запрещены ,.!? Разрешены @#_"
                false
            }
            else -> true
        }
    }

    private fun updateProfile() {
        val user = auth.currentUser ?: return
        val userId = user.uid

        val username = binding.etUsername.text.toString()
        val email = binding.etEmail.text.toString()
        val phone = binding.etPhone.text.toString()
        val oldPassword = binding.etOldPassword.text.toString()
        val newPassword = binding.etNewPassword.text.toString()

        // Обновление аватарки
        selectedImageUri?.let { uri ->
            val storageRef = storage.reference
            val imageRef = storageRef.child("avatars/${userId}/${UUID.randomUUID()}")

            imageRef.putFile(uri)
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let { throw it }
                    }
                    imageRef.downloadUrl
                }
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        updateUserData(userId, username, email, phone, downloadUri.toString())
                    } else {
                        Toast.makeText(context, "Ошибка загрузки аватарки", Toast.LENGTH_SHORT).show()
                    }
                }
        } ?: run {
            updateUserData(userId, username, email, phone, null)
        }

        // Обновление email
        if (email != user.email) {
            user.updateEmail(email)
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Ошибка обновления email: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Обновление пароля
        if (newPassword.isNotEmpty()) {
            val credential = EmailAuthProvider.getCredential(user.email!!, oldPassword)
            user.reauthenticate(credential)
                .addOnSuccessListener {
                    user.updatePassword(newPassword)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Пароль обновлен", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Неверный старый пароль", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateUserData(userId: String, username: String, email: String, phone: String, avatarUrl: String?) {
        val updates = hashMapOf<String, Any>(
            "username" to username,
            "email" to email,
            "phone" to phone
        )

        avatarUrl?.let {
            updates["avatarUrl"] = it
        }

        db.collection("users").document(userId)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(context, "Профиль обновлен", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Ошибка обновления: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}