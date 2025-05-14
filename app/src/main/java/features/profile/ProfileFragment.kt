// ProfileFragment.kt
package com.example.project_helper.features.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
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
            loadUserProfile(userId)

            binding.btnEdit.setOnClickListener {
                findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
            }

            binding.btnLogout.setOnClickListener {
                firebaseAuth.signOut()
                Toast.makeText(requireContext(), "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
            }
        } else {
            Toast.makeText(requireContext(), "Пожалуйста, войдите в аккаунт", Toast.LENGTH_LONG).show()
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }
    }

    private fun loadUserProfile(userId: String) {
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val user = documentSnapshot.toObject(UserData::class.java)
                    user?.let {
                        binding.tvUsername.text = it.username ?: "Имя пользователя не указано"
                        binding.tvEmail.text = it.email ?: "Email недоступен"
                        binding.tvRegistrationDate.text = it.registrationDate.formatToString()
                        binding.tvPhone.text = it.phone ?: "+7********"

                        // Загрузка аватарки
                        Glide.with(this)
                            .load(it.avatarUrl)
                            .placeholder(R.drawable.ic_default_avatar)
                            .into(binding.ivProfile)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    requireContext(),
                    "Ошибка загрузки профиля: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}