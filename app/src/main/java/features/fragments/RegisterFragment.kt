package com.example.project_helper.features.auth.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project_helper.R
import com.example.project_helper.databinding.FragmentRegisterBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.regex.Pattern

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        setupValidationListeners()

        binding.buttonEnterReg.setOnClickListener {
            if (validateAllInputs()) {
                registerUser()
            }
        }

        binding.registerEnterReg.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }

    private fun setupValidationListeners() {
        binding.nickReg.addTextChangedListener(validationWatcher)
        binding.emailReg.addTextChangedListener(validationWatcher)
        binding.etPhone.addTextChangedListener(validationWatcher)
        binding.passwordReg.addTextChangedListener(validationWatcher)
    }

    private val validationWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            validateAllInputs()
        }
    }

    private fun validateAllInputs(): Boolean {
        val username = binding.nickReg.text.toString().trim()
        val email = binding.emailReg.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val password = binding.passwordReg.text.toString().trim()

        val isUsernameValid = validateUsername(username)
        val isEmailValid = validateEmail(email)
        val isPhoneValid = validatePhone(phone)
        val isPasswordValid = validatePassword(password)

        return isUsernameValid && isEmailValid && isPhoneValid && isPasswordValid
    }

    private fun validateUsername(username: String): Boolean {
        return when {
            username.isEmpty() -> {
                binding.nickReg.error = "Введите имя пользователя"
                false
            }
            username.length < 3 -> {
                binding.nickReg.error = "Минимум 3 символа"
                false
            }
            !username.matches(Regex("^[a-zA-Z][a-zA-Z0-9_]*$")) -> {
                binding.nickReg.error = "Только буквы, цифры и _"
                false
            }
            else -> true
        }
    }

    private fun validateEmail(email: String): Boolean {
        val emailPattern = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
            Pattern.CASE_INSENSITIVE
        )

        return when {
            email.isEmpty() -> {
                binding.emailReg.error = "Введите email"
                false
            }
            !emailPattern.matcher(email).matches() -> {
                binding.emailReg.error = "Некорректный email"
                false
            }
            email.endsWith("@почта") -> {
                binding.emailReg.error = "Используйте международный домен"
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
            password.isEmpty() -> {
                binding.passwordReg.error = "Введите пароль"
                false
            }
            password.length < 6 -> {
                binding.passwordReg.error = "Минимум 6 символов"
                false
            }
            !password.any { it.isUpperCase() } -> {
                binding.passwordReg.error = "Нужна хотя бы 1 заглавная буква"
                false
            }
            !password.any { it.isDigit() } -> {
                binding.passwordReg.error = "Нужна хотя бы 1 цифра"
                false
            }
            password.contains(Regex("[,.!?]")) -> {
                binding.passwordReg.error = "Запрещены ,.!? Разрешены @#_"
                false
            }
            else -> true
        }
    }

    private fun showPasswordRequirements() {
        Snackbar.make(binding.root, """
            Требования к паролю:
            • 6+ символов
            • 1 заглавная буква
            • 1 цифра
            • Разрешены: @#_
            • Запрещены: ,.!?
        """.trimIndent(), Snackbar.LENGTH_LONG).show()
    }

    private fun registerUser() {
        val email = binding.emailReg.text.toString().trim()
        val password = binding.passwordReg.text.toString().trim()
        val username = binding.nickReg.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()

        binding.progressBar.visibility = View.VISIBLE

        createUserAccount(email, password, username, phone)
    }

    private fun createUserAccount(email: String, password: String, username: String, phone: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                binding.progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    saveUserToFirestore(username, email, phone)
                } else {
                    handleRegistrationError(task.exception)
                }
            }
    }

    private fun saveUserToFirestore(username: String, email: String, phone: String) {
        val user = hashMapOf(
            "username" to username,
            "email" to email,
            "phone" to phone,
            "registration_date" to FieldValue.serverTimestamp(),
            "last_login" to FieldValue.serverTimestamp()
        )

        auth.currentUser?.uid?.let { uid ->
            db.collection("users").document(uid)
                .set(user)
                .addOnSuccessListener {
                    findNavController().navigate(R.id.action_registerFragment_to_profileFragment)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        requireContext(),
                        "Ошибка сохранения данных: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    auth.currentUser?.delete()
                }
        }
    }

    private fun handleRegistrationError(exception: Exception?) {
        when (exception?.message) {
            "The email address is already in use by another account." -> {
                binding.emailReg.error = "Email уже используется"
            }
            "The password is invalid or the user does not have a password." -> {
                binding.passwordReg.error = "Некорректный пароль"
            }
            else -> {
                Toast.makeText(
                    requireContext(),
                    "Ошибка регистрации: ${exception?.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}