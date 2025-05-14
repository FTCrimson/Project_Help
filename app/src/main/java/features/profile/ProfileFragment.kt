package com.example.project_helper.features.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project_helper.R
import com.example.project_helper.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import data.auth.UserData
import data.auth.formatToString


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val currentUser: FirebaseUser? = firebaseAuth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid
            Log.d("ProfileFragment", "Текущий пользователь UID: $userId")

            loadUserProfile(userId)

            binding.tvEmail.text = currentUser.email ?: "Email недоступен"
            binding.tvPassword.setText("********")
            binding.tvPassword.isEnabled = false

            binding.btnLogout.setOnClickListener {
                firebaseAuth.signOut()
                Log.d("ProfileFragment", "Пользователь вышел из аккаунта")
                Toast.makeText(requireContext(), "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.loginFragment)
            }

        } else {
            Log.d("ProfileFragment", "Пользователь не вошел.")
            Toast.makeText(requireContext(), "Пожалуйста, войдите в аккаунт для просмотра профиля", Toast.LENGTH_LONG).show()

            binding.tvUsername.text = ""
            binding.tvEmail.text = ""
            binding.tvRegistrationDate.text = ""
            binding.tvPassword.setText("")
            binding.btnLogout.isEnabled = false
        }
    }

    private fun loadUserProfile(userId: String) {
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val user = documentSnapshot.toObject(UserData::class.java)
                    if (user != null) {
                        Log.d("ProfileFragment", "Данные пользователя загружены: $user")

                        binding.tvUsername.text = user.username ?: "Имя пользователя не указано"

                        binding.tvRegistrationDate.text = user.registrationDate.formatToString()

                    } else {
                        Log.e("ProfileFragment", "Не удалось преобразовать данные документа Firestore в UserData")
                        Toast.makeText(requireContext(), "Ошибка загрузки данных профиля.", Toast.LENGTH_SHORT).show()
                        binding.tvUsername.text = "Ошибка данных"
                        binding.tvRegistrationDate.text = "Ошибка данных"
                    }
                } else {
                    Log.d("ProfileFragment", "Документ пользователя с UID $userId не найден в Firestore.")
                    Toast.makeText(requireContext(), "Данные профиля не найдены.", Toast.LENGTH_SHORT).show()
                    binding.tvUsername.text = "Данные не найдены"
                    binding.tvRegistrationDate.text = "Данные не найдены"
                }
            }
            .addOnFailureListener { exception ->
                Log.e("ProfileFragment", "Ошибка загрузки данных пользователя из Firestore: ${exception.message}", exception)
                Toast.makeText(requireContext(), "Ошибка загрузки профиля: ${exception.message}", Toast.LENGTH_LONG).show()

                binding.tvUsername.text = "Ошибка загрузки"
                binding.tvRegistrationDate.text = "Ошибка загрузки"
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}