package com.example.project_helper.domain.profile

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
import com.google.firebase.firestore.ListenerRegistration
import data.auth.UserData
import data.auth.formatToString

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private var snapshotListener: ListenerRegistration? = null

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

        binding.btnEdit.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }

        binding.btnProject.setOnClickListener {
            findNavController().navigate(R.id.action_ProfileFragment_to_RoleSelectionFragment)
        }

        binding.btnLogout.setOnClickListener {
            firebaseAuth.signOut()
            Toast.makeText(requireContext(), "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }
    }

    override fun onStart() {
        super.onStart()
        setupUserListener()
    }

    override fun onStop() {
        super.onStop()
        snapshotListener?.remove()
    }

    private fun setupUserListener() {
        val currentUser: FirebaseUser? = firebaseAuth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid

            snapshotListener?.remove()

            snapshotListener = firestore.collection("users").document(userId)
                .addSnapshotListener { documentSnapshot, error ->
                    if (error != null) {
                        Toast.makeText(
                            requireContext(),
                            "Ошибка загрузки профиля: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                        return@addSnapshotListener
                    }

                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        val user = documentSnapshot.toObject(UserData::class.java)
                        user?.let { updateUI(it) }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Данные пользователя не найдены",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        } else {
            Toast.makeText(requireContext(), "Пожалуйста, войдите в аккаунт", Toast.LENGTH_LONG).show()
            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }
    }

    private fun updateUI(userData: UserData) {
        binding.tvUsername.text = userData.username ?: "Имя пользователя не указано"
        binding.tvEmail.text = userData.email ?: "Email недоступен"
        binding.tvPhone.text = userData.phone ?: "+7********"
        binding.tvRegistrationDate.text = userData.registrationDate?.formatToString() ?: "Дата регистрации не указана"

        Glide.with(this)
            .load(userData.avatarUrl)
            .circleCrop()
            .placeholder(R.drawable.ic_default_avatar)
            .into(binding.ivProfile)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        snapshotListener?.remove()
        snapshotListener = null
        _binding = null
    }
}