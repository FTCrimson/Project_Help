package com.example.project_helper.features.fragments

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_helper.R
import com.example.project_helper.data.repository.ChatRepository // Import ChatRepository
import com.example.project_helper.databinding.FragmentChatBinding
import com.example.project_helper.features.commandchat.ChatMessageAdapter
import com.example.project_helper.features.commandchat.ChatViewModel
import com.example.project_helper.features.commandchat.ChatViewModelFactory // Import ChatViewModelFactory
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    // Используем ViewModelFactory для создания ViewModel с ChatRepository
    private val viewModel: ChatViewModel by viewModels {
        ChatViewModelFactory(ChatRepository())
    }
    private lateinit var messageAdapter: ChatMessageAdapter

    private lateinit var drawerSlideIn: ObjectAnimator
    private lateinit var drawerSlideOut: ObjectAnimator
    private lateinit var overlayFadeIn: ObjectAnimator
    private lateinit var overlayFadeOut: ObjectAnimator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val chatId = arguments?.getString("chatId") ?: ""
        if (chatId.isEmpty()) {
            Toast.makeText(requireContext(), "Не указан ID чата", Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressed()
            return
        }

        // Передаем ID чата во ViewModel
        viewModel.setChatId(chatId)

        setupRecyclerView()
        setupClickListeners()
        setupDrawerAnimations()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        // Получаем текущего пользователя из FirebaseAuth
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        messageAdapter = ChatMessageAdapter(currentUserId)

        binding.messagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true
            }
            adapter = messageAdapter
        }
    }

    private fun setupDrawerAnimations() {
        // Добавляем проверку на _binding на случай, если метод вызывается после onDestroyView
        if (_binding == null) return

        // Ширина панели (используйте dimension resource, если есть)
        val drawerWidth = 280f // или resources.getDimension(R.dimen.drawer_width)

        drawerSlideIn = ObjectAnimator.ofFloat(
            binding.drawerPanel,
            "translationX",
            -drawerWidth,
            0f
        ).apply {
            duration = 300
        }

        drawerSlideOut = ObjectAnimator.ofFloat(
            binding.drawerPanel,
            "translationX",
            0f,
            -drawerWidth
        ).apply {
            duration = 300
        }

        overlayFadeIn = ObjectAnimator.ofFloat(
            binding.drawerOverlay,
            "alpha",
            0f,
            1f
        ).apply {
            duration = 300
        }

        overlayFadeOut = ObjectAnimator.ofFloat(
            binding.drawerOverlay,
            "alpha",
            1f,
            0f
        ).apply {
            duration = 300
        }
    }

    private fun setupClickListeners() {
        // Добавляем проверку на _binding
        if (_binding == null) return

        binding.sendButton.setOnClickListener {
            sendMessage()
        }

        binding.addMemberButton.setOnClickListener {
            showAddMemberDialog()
        }

        binding.requestAccessButton.setOnClickListener {
            requestAccessToChat()
        }

        binding.menuButton.setOnClickListener {
            openDrawer()
        }

        binding.aiChatButton.setOnClickListener {
            // Проверяем на null перед использованием findNavController
            findNavController().navigate(R.id.action_ChatFragment_to_NeuroChatFragment)
            closeDrawer()
        }

        binding.commandChatButton.setOnClickListener {
            // Проверяем на null перед использованием findNavController
            findNavController().navigate(R.id.action_ChatFragment_to_CommandChatFragment)
            closeDrawer()
        }

        // Новая кнопка изменения названия в выдвижной панели
        binding.btnEditChatName.setOnClickListener {
            showEditChatNameDialog()
            closeDrawer() // Закрываем панель после выбора действия
        }

        binding.drawerOverlay.setOnClickListener {
            closeDrawer()
        }

        // Опционально: клик по заголовку тоже может открывать диалог изменения названия
        // binding.chatTitle.setOnClickListener {
        //     showEditChatNameDialog()
        // }
    }

    private fun openDrawer() {
        if (_binding == null) return
        binding.drawerContainer.visibility = View.VISIBLE
        drawerSlideIn.start()
        overlayFadeIn.start()
    }

    private fun closeDrawer() {
        if (_binding == null) return
        drawerSlideOut.start()
        overlayFadeOut.start()

        // Добавляем проверку на _binding внутри слушателя
        drawerSlideOut.addUpdateListener {
            if (_binding != null && it.animatedFraction == 1f) {
                binding.drawerContainer.visibility = View.GONE
            }
        }
        overlayFadeOut.addUpdateListener {
            if (_binding != null && it.animatedFraction == 1f) {
                // Действия после завершения анимации затемнения, если нужны
            }
        }
    }

    private fun showAddMemberDialog() {
        if (_binding == null) return

        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_member, null)
        val usernameInput = dialogView.findViewById<TextInputEditText>(R.id.usernameInput)

        AlertDialog.Builder(requireContext())
            .setTitle("Добавить участника")
            .setView(dialogView)
            .setPositiveButton("Добавить") { _, _ ->
                val username = usernameInput.text.toString().trim()
                if (username.isNotEmpty()) {
                    // Вызываем метод ViewModel, который уже знает chatId
                    viewModel.addMemberToChat(username)
                } else {
                    Toast.makeText(requireContext(), "Введите username", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    // Диалог для изменения названия чата
    private fun showEditChatNameDialog() {
        if (_binding == null) return

        val currentName = binding.chatTitle.text.toString()
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_chat_name, null) // Используем правильный layout
        val nameInput = dialogView.findViewById<TextInputEditText>(R.id.chatNameInput) // Используем правильный ID
        nameInput.setText(currentName)

        AlertDialog.Builder(requireContext())
            .setTitle("Изменить название чата")
            .setView(dialogView)
            .setPositiveButton("Сохранить") { _, _ ->
                val newName = nameInput.text.toString().trim()
                if (newName.isNotEmpty()) {
                    // Вызываем метод ViewModel, который уже знает chatId
                    viewModel.updateChatName(newName)
                } else {
                    Toast.makeText(requireContext(), "Введите название", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun requestAccessToChat() {
        // Логика запроса доступа. Возможно, нужно будет вызвать метод в ViewModel
        Toast.makeText(requireContext(), "Запрос доступа отправлен создателю чата", Toast.LENGTH_SHORT).show()
    }

    private fun observeViewModel() {
        viewModel.messages.observe(viewLifecycleOwner, Observer { messages ->
            if (_binding == null) return@Observer

            if (messages.isEmpty()) {
                binding.emptyStateTextView.visibility = View.VISIBLE
                binding.messagesRecyclerView.visibility = View.GONE
            } else {
                binding.emptyStateTextView.visibility = View.GONE
                binding.messagesRecyclerView.visibility = View.VISIBLE
                messageAdapter.submitList(messages) {
                    if (_binding != null) {
                        binding.messagesRecyclerView.scrollToPosition(messages.size - 1)
                    }
                }
            }
        })

        viewModel.error.observe(viewLifecycleOwner, Observer { message ->
            if (_binding == null) return@Observer

            if (message != null) {
                if (message.contains("доступ", ignoreCase = true)) {
                    showAccessDeniedView(message)
                } else {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
            }
        })

        viewModel.chatInfo.observe(viewLifecycleOwner) { chat ->
            if (_binding == null) return@observe

            if (chat != null) {
                binding.chatTitle.text = chat.name
                // Обновляем видимость кнопок в зависимости от прав пользователя
                binding.addMemberButton.visibility = if (viewModel.canEditChat(chat)) View.VISIBLE else View.GONE
                binding.btnEditChatName.visibility = if (viewModel.canEditChat(chat)) View.VISIBLE else View.GONE

                // Если пользователь не участник, показываем экран "Нет доступа"
                if (!viewModel.isCurrentUserMember(chat)) {
                    showAccessDeniedView("Нет доступа к чату")
                } else {
                    // Если пользователь участник, убеждаемся, что контент чата виден
                    binding.accessDeniedView.visibility = View.GONE
                    binding.inputLayout.visibility = View.VISIBLE
                    // Состояние emptyStateTextView и messagesRecyclerView управляется messages Observer
                }

            } else {
                // Если чат == null (не найден), показываем ошибку или пустой экран
                binding.chatTitle.text = "Чат не найден"
                showAccessDeniedView("Чат не найден")
            }
        }

        viewModel.addMemberResult.observe(viewLifecycleOwner) { result ->
            if (_binding == null) return@observe
            result?.let {
                if (it.isSuccess) {
                    Toast.makeText(
                        requireContext(),
                        "Участник успешно добавлен",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Ошибка: ${it.exceptionOrNull()?.message ?: "Неизвестная ошибка"}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                viewModel.resetAddMemberResult()
            }
        }

        viewModel.updateChatNameResult.observe(viewLifecycleOwner) { result ->
            if (_binding == null) return@observe
            result?.let {
                if (it.isSuccess) {
                    Toast.makeText(requireContext(), "Название чата обновлено", Toast.LENGTH_SHORT).show()
                    // Название в chatTitle обновится автоматически через observer chatInfo
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Ошибка при обновлении названия: ${it.exceptionOrNull()?.message ?: "Неизвестная ошибка"}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                viewModel.resetUpdateChatNameResult()
            }
        }
    }

    private fun showAccessDeniedView(message: String) {
        if (_binding == null) return
        binding.apply {
            messagesRecyclerView.visibility = View.GONE
            inputLayout.visibility = View.GONE
            emptyStateTextView.visibility = View.GONE
            accessDeniedView.visibility = View.VISIBLE
            accessDeniedTextView.text = message
        }
    }

    private fun sendMessage() {
        if (_binding == null) return

        val text = binding.messageInput.text.toString().trim()

        if (text.isEmpty()) {
            Toast.makeText(requireContext(), "Введите сообщение", Toast.LENGTH_SHORT).show()
            return
        }

        // Вызываем метод ViewModel, который отправит сообщение
        viewModel.sendMessage(text)
        binding.messageInput.text?.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Отменяем анимации, чтобы их слушатели не срабатывали на null-binding
        if (::drawerSlideOut.isInitialized) drawerSlideOut.cancel()
        if (::overlayFadeOut.isInitialized) overlayFadeOut.cancel()
        _binding = null
    }
}
