package com.example.project_helper.features.fragments

import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.project_helper.R
import com.example.project_helper.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        binding.buttonEnterLogIn.setOnClickListener {
            loginUser()
        }

        binding.registerEnterLogIn.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        if (auth.currentUser != null) {
            findNavController().navigate(R.id.action_login_to_profile)
        }
    }

    private fun loginUser() {
        val email = binding.emailLogIn.text.toString().trim()
        val password = binding.passwordLogIn.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            showToast("Пожалуйста, заполните все поля")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    findNavController().navigate(R.id.action_login_to_profile)
                } else {
                    handleLoginError(task.exception)
                }
            }
    }

    private fun handleLoginError(exception: Exception?) {
        when ((exception as? FirebaseAuthException)?.errorCode) {
            "ERROR_INVALID_EMAIL" -> showToast("Неверный формат email")
            "ERROR_WRONG_PASSWORD" -> showToast("Неверный пароль")
            "ERROR_USER_NOT_FOUND" -> showToast("Пользователь не найден")
            else -> showToast("Ошибка входа: ${exception?.message}")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}