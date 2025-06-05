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

        // Инициализируем ViewModel. Используем requireActivity() для шаринга между фрагментами в одной Activity.
        viewModel = ViewModelProvider(requireActivity()).get(ProfileViewModel::class.java)

        // Получаем данные из ресурсов
        val studentRoles = resources.getStringArray(R.array.student_roles)
        val activityFields = resources.getStringArray(R.array.activity_fields)
        val expertFields = resources.getStringArray(R.array.expert_fields) // Список для экспертов

        // Настраиваем адаптеры для спиннеров
        val studentRoleAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, studentRoles)
        binding.studentRoleSpinner.adapter = studentRoleAdapter

        val activityFieldAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, activityFields)
        binding.activityFieldSpinner.adapter = activityFieldAdapter

        val expertFieldAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, expertFields)
        binding.expertFieldSpinner.adapter = expertFieldAdapter

        // Изначально скрываем все элементы, кроме выбора роли
        binding.studentRoleSpinner.visibility = View.GONE
        binding.activityFieldSpinner.visibility = View.GONE
        binding.expertFieldSpinner.visibility = View.GONE
        binding.expertNameEditText.visibility = View.GONE // Скрываем поле имени ученика по умолчанию

        // Устанавливаем слушатель для переключателя ролей
        binding.roleRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.studentRadioButton -> {
                    // Если выбран "Ученик"
                    binding.studentRoleSpinner.visibility = View.VISIBLE
                    binding.activityFieldSpinner.visibility = View.VISIBLE
                    binding.expertFieldSpinner.visibility = View.GONE
                    binding.expertNameEditText.visibility = View.GONE // Скрываем поле имени
                }
                R.id.expertRadioButton -> {
                    // Если выбран "Эксперт"
                    binding.studentRoleSpinner.visibility = View.GONE
                    binding.activityFieldSpinner.visibility = View.GONE
                    binding.expertFieldSpinner.visibility = View.VISIBLE // Показываем спиннер эксперта
                    binding.expertNameEditText.visibility = View.GONE // СКРЫВАЕМ поле имени
                }
                R.id.mentorRadioButton -> {
                    // Если выбран "Наставник"
                    binding.studentRoleSpinner.visibility = View.GONE
                    binding.activityFieldSpinner.visibility = View.GONE
                    binding.expertFieldSpinner.visibility = View.GONE // Скрываем спиннер эксперта
                    binding.expertNameEditText.visibility = View.VISIBLE // ПОКАЗЫВАЕМ поле имени
                }
            }
        }

        // Устанавливаем слушатель для кнопки "Далее"
        binding.nextButton.setOnClickListener {
            val selectedRoleId = binding.roleRadioGroup.checkedRadioButtonId

            val roleSelection = when (selectedRoleId) {
                R.id.studentRadioButton -> RoleSelection(
                    roleType = "student",
                    role = binding.studentRoleSpinner.selectedItem.toString(),
                    field = binding.activityFieldSpinner.selectedItem.toString()
                    // studentName останется пустым по умолчанию
                )
                R.id.expertRadioButton -> RoleSelection(
                    roleType = "expert",
                    role = binding.expertFieldSpinner.selectedItem.toString()
                    // studentName останется пустым по умолчанию, так как поле скрыто
                )
                R.id.mentorRadioButton -> RoleSelection(
                    roleType = "mentor",
                    // role останется пустым по умолчанию, если для наставника нет спиннера роли
                    studentName = binding.expertNameEditText.text.toString() // Получаем имя ученика из поля
                )
                else -> null // Если ничего не выбрано
            }

            roleSelection?.let {
                // Сохраняем выбранные данные через ViewModel
                viewModel.saveRoleSelection(it)

                // TODO: Здесь можно добавить логику перехода на следующий экран
                // Например, если вы хотите вернуться назад после сохранения:
                findNavController().popBackStack()
                // Или перейти на другой фрагмент:
                // findNavController().navigate(R.id.action_RoleSelectionFragment_to_NextFragment) // Замените на актуальный ID
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}