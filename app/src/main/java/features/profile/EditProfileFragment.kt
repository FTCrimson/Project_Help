package com.example.project_helper.features.profile

import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import data.auth.UserData
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var selectedImageUri: Uri? = null
    private var currentUsername: String? = null
    private var currentAvatarUrl: String? = null
    private var currentEmail: String? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            Glide.with(this)
                .load(it)
                .circleCrop()
                .placeholder(R.drawable.ic_default_avatar)
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
                        currentUsername = it.username
                        currentAvatarUrl = it.avatarUrl
                        currentEmail = it.email

                        binding.etUsername.setText(it.username ?: "")
                        binding.etEmail.setText(it.email ?: "")
                        binding.etPhone.setText(it.phone ?: "+7********")

                        it.avatarUrl?.let { url ->
                            if (url.isNotBlank()) {
                                Glide.with(this)
                                    .load(url)
                                    .circleCrop()
                                    .placeholder(R.drawable.ic_default_avatar)
                                    .into(binding.ivProfile)
                            } else {
                                binding.ivProfile.setImageResource(R.drawable.ic_default_avatar)
                            }
                        } ?: run {
                            binding.ivProfile.setImageResource(R.drawable.ic_default_avatar)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Ошибка загрузки данных: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.e("EditProfile", "Ошибка загрузки данных", e)
                }
        }
    }

    private fun setupListeners() {
        binding.btnChangeAvatar.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.btnSave.setOnClickListener {
            if (validateInputs()) {
                CoroutineScope(Dispatchers.Main).launch {
                    binding.progressBar.visibility = View.VISIBLE
                    try {
                        withContext(Dispatchers.IO) {
                            updateProfile()
                        }
                        Toast.makeText(context, "Профиль обновлен", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    } catch (e: Exception) {
                        handleUpdateError(e)
                    } finally {
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun handleUpdateError(e: Exception) {
        val errorMessage = when {
            e is FirebaseAuthInvalidCredentialsException -> "Неверный пароль"
            e is FirebaseAuthUserCollisionException -> "Email уже используется другим аккаунтом"
            e.message?.contains("username", ignoreCase = true) == true -> "Имя пользователя уже занято"
            e.message?.contains("requires recent authentication", ignoreCase = true) == true -> "Требуется повторный вход"
            e.message == "Требуется ввод пароля" -> "Для изменения email/пароля введите текущий пароль"
            e.message == "Неверный пароль" -> "Неверный текущий пароль"
            else -> "Ошибка: ${e.message ?: "Неизвестная ошибка"}"
        }
        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        Log.e("EditProfile", "Ошибка обновления профиля", e)
    }

    private fun validateInputs(): Boolean {
        val username = binding.etUsername.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val oldPassword = binding.etOldPassword.text.toString().trim()
        val newPassword = binding.etNewPassword.text.toString().trim()
        val user = auth.currentUser

        var isValid = true

        binding.etOldPassword.error = null

        if (email != user?.email) {
            if (oldPassword.isEmpty()) {
                binding.etOldPassword.error = "Введите пароль для изменения email"
                isValid = false
            }
        }

        if (newPassword.isNotEmpty() && oldPassword.isEmpty()) {
            binding.etOldPassword.error = "Введите текущий пароль"
            isValid = false
        }

        if (!validateUsername(username)) isValid = false
        if (!validateEmail(email)) isValid = false
        if (!validatePhone(phone)) isValid = false
        if (newPassword.isNotEmpty() && !validatePassword(newPassword)) isValid = false

        return isValid
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
            else -> true
        }
    }

    private suspend fun updateProfile() {
        val user = auth.currentUser ?: throw Exception("Пользователь не авторизован")
        val userId = user.uid

        val username = binding.etUsername.text.toString()
        val email = binding.etEmail.text.toString()
        val phone = binding.etPhone.text.toString()
        val oldPassword = binding.etOldPassword.text.toString()
        val newPassword = binding.etNewPassword.text.toString()

        if (username != currentUsername) {
            val isUnique = db.collection("usernames").document(username).get().await().exists().not()
            if (!isUnique) {
                throw Exception("Имя пользователя уже занято")
            }
        }

        // Переаутентификация при необходимости
        val needReauth = email != user.email || newPassword.isNotEmpty()
        if (needReauth) {
            if (oldPassword.isEmpty()) {
                throw Exception("Требуется ввод пароля")
            }
            try {
                val credential = EmailAuthProvider.getCredential(user.email!!, oldPassword)
                user.reauthenticate(credential).await()
            } catch (e: Exception) {
                throw Exception("Неверный пароль")
            }
        }

        if (email != user.email) {
            try {
                user.updateEmail(email).await()
                currentEmail = email
            } catch (e: FirebaseAuthUserCollisionException) {
            }
        }

        if (newPassword.isNotEmpty()) {
            user.updatePassword(newPassword).await()
        }

        val avatarUrl = selectedImageUri?.let { uri ->
            try {
                val storageRef = storage.reference
                val imageRef = storageRef.child("avatars/${userId}/${UUID.randomUUID()}")
                imageRef.putFile(uri).await()
                imageRef.downloadUrl.await().toString()
            } catch (e: Exception) {
                Log.e("EditProfile", "Ошибка загрузки аватара", e)
                currentAvatarUrl
            }
        } ?: currentAvatarUrl

        // Обновление Firestore
        val userUpdates = hashMapOf<String, Any>(
            "username" to username,
            "email" to email,
            "phone" to phone
        )

        avatarUrl?.takeIf { it.isNotBlank() }?.let {
            userUpdates["avatarUrl"] = it
        }

        db.collection("users").document(userId)
            .set(userUpdates, SetOptions.merge())
            .await()

        // Обновление displayName в FirebaseAuth
        user.updateProfile(
            UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build()
        ).await()

        // Очищаем поля паролей
        withContext(Dispatchers.Main) {
            binding.etOldPassword.text?.clear()
            binding.etNewPassword.text?.clear()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}