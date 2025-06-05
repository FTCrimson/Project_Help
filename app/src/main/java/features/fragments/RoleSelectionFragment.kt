package com.example.project_helper.features.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.project_helper.R
import com.example.project_helper.data.auth.RoleSelection
import com.example.project_helper.databinding.FragmentRoleSelectionBinding
import features.fragments.ProfileViewModel

class RoleSelectionFragment : Fragment() {

    private var _binding: FragmentRoleSelectionBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRoleSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity()).get(ProfileViewModel::class.java)

        val studentRoles = resources.getStringArray(R.array.student_roles)
        val activityFields = resources.getStringArray(R.array.activity_fields)
        val expertFields = resources.getStringArray(R.array.expert_fields)

        val studentRoleAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, studentRoles)
        binding.studentRoleSpinner.adapter = studentRoleAdapter

        val activityFieldAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, activityFields)
        binding.activityFieldSpinner.adapter = activityFieldAdapter

        val expertFieldAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, expertFields)
        binding.expertFieldSpinner.adapter = expertFieldAdapter

        binding.studentRoleSpinner.visibility = View.GONE
        binding.activityFieldSpinner.visibility = View.GONE
        binding.expertFieldSpinner.visibility = View.GONE
        binding.expertNameEditText.visibility = View.GONE

        binding.roleRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.studentRadioButton -> {
                    binding.studentRoleSpinner.visibility = View.VISIBLE
                    binding.activityFieldSpinner.visibility = View.VISIBLE
                    binding.expertFieldSpinner.visibility = View.GONE
                    binding.expertNameEditText.visibility = View.GONE
                }
                R.id.expertRadioButton -> {
                    binding.studentRoleSpinner.visibility = View.GONE
                    binding.activityFieldSpinner.visibility = View.GONE
                    binding.expertFieldSpinner.visibility = View.VISIBLE
                    binding.expertNameEditText.visibility = View.GONE
                }
                R.id.mentorRadioButton -> {
                    binding.studentRoleSpinner.visibility = View.GONE
                    binding.activityFieldSpinner.visibility = View.GONE
                    binding.expertFieldSpinner.visibility = View.GONE
                    binding.expertNameEditText.visibility = View.VISIBLE
                }
            }
        }

        binding.nextButton.setOnClickListener {
            val selectedRoleId = binding.roleRadioGroup.checkedRadioButtonId

            val roleSelection = when (selectedRoleId) {
                R.id.studentRadioButton -> RoleSelection(
                    roleType = "student",
                    role = binding.studentRoleSpinner.selectedItem.toString(),
                    field = binding.activityFieldSpinner.selectedItem.toString()
                )
                R.id.expertRadioButton -> RoleSelection(
                    roleType = "expert",
                    role = binding.expertFieldSpinner.selectedItem.toString()
                )
                R.id.mentorRadioButton -> RoleSelection(
                    roleType = "mentor",
                    studentName = binding.expertNameEditText.text.toString()
                )
                else -> null
            }

            roleSelection?.let {
                viewModel.saveRoleSelection(it)
            }
            binding.nextButton.setOnClickListener {
                findNavController().navigate(R.id.action_RoleSelectionFragment_to_NeuroChatFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}