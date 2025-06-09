package com.example.project_helper.domain.fragments

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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_helper.R
import com.example.project_helper.data.repository.ChatRepository
import com.example.project_helper.databinding.FragmentChatBinding
import com.example.project_helper.domain.commandchat.ChatMessageAdapter
import com.example.project_helper.presentation.viewmodel.commandchat.ChatViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return ChatViewModel(ChatRepository()) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
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

        viewModel.setChatId(chatId)
        setupRecyclerView()
        setupClickListeners()
        setupDrawerAnimations()
        observeViewModel()
    }

    private fun setupRecyclerView() {
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
        if (_binding == null) return
        val drawerWidth = 280f

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
        if (_binding == null) return

        binding.sendButton.setOnClickListener { sendMessage() }
        binding.addMemberButton.setOnClickListener { showAddMemberDialog() }
        binding.requestAccessButton.setOnClickListener { requestAccessToChat() }
        binding.menuButton.setOnClickListener { openDrawer() }

        binding.aiChatButton.setOnClickListener {
            findNavController().navigate(R.id.action_ChatFragment_to_NeuroChatFragment)
            closeDrawer()
        }

        binding.commandChatButton.setOnClickListener {
            findNavController().navigate(R.id.action_ChatFragment_to_CommandChatFragment)
            closeDrawer()
        }

        binding.ProfileButton.setOnClickListener {
            findNavController().navigate(R.id.action_ChatFragment_to_ProfileFragment)
            closeDrawer()
        }

        binding.Info2.setOnClickListener {
            viewModel.moveToStage3()
            closeDrawer()
            findNavController().navigate(R.id.action_ChatFragment_to_InfoFragment2)
            closeDrawer()
        }

        binding.btnEditChatName.setOnClickListener {
            showEditChatNameDialog()
            closeDrawer()
        }

        binding.drawerOverlay.setOnClickListener { closeDrawer() }

        binding.approveIdeaButton.setOnClickListener {
            viewModel.approveIdea()
            closeDrawer()
        }

        binding.stage2Button.setOnClickListener {
            viewModel.moveToStage2()
            closeDrawer()
            findNavController().navigate(R.id.action_ChatFragment_to_InfoFragment2)
        }

        binding.approveProblemButton.setOnClickListener {
            viewModel.approveProblem()
            closeDrawer()
        }

        binding.stage3Button.setOnClickListener {
            viewModel.moveToStage3()
            closeDrawer()
        }
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

        drawerSlideOut.addUpdateListener {
            if (_binding != null && it.animatedFraction == 1f) {
                binding.drawerContainer.visibility = View.GONE
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
                    viewModel.addMemberToChat(username)
                } else {
                    Toast.makeText(requireContext(), "Введите username", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }


    private fun showEditChatNameDialog() {
        if (_binding == null) return

        val currentName = binding.chatTitle.text.toString()
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_chat_name, null)
        val nameInput = dialogView.findViewById<TextInputEditText>(R.id.chatNameInput)
        nameInput.setText(currentName)

        AlertDialog.Builder(requireContext())
            .setTitle("Изменить название чата")
            .setView(dialogView)
            .setPositiveButton("Сохранить") { _, _ ->
                val newName = nameInput.text.toString().trim()
                if (newName.isNotEmpty()) {
                    viewModel.updateChatName(newName)
                } else {
                    Toast.makeText(requireContext(), "Введите название", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun requestAccessToChat() {
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
                binding.addMemberButton.visibility = if (viewModel.canEditChat(chat)) View.VISIBLE else View.GONE
                binding.btnEditChatName.visibility = if (viewModel.canEditChat(chat)) View.VISIBLE else View.GONE

                if (!viewModel.isCurrentUserMember(chat)) {
                    showAccessDeniedView("Нет доступа к чату")
                } else {
                    binding.accessDeniedView.visibility = View.GONE
                    binding.inputLayout.visibility = View.VISIBLE
                }
            } else {
                binding.chatTitle.text = "Чат не найден"
                showAccessDeniedView("Чат не найден")
            }
            // chatInfo обновляется из Firestore, что триггерит stage и approvals LiveData
            // их наблюдатели уже настроены ниже
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

        // Наблюдение за этапом для управления видимостью кнопок И навигации
        viewModel.stage.observe(viewLifecycleOwner) { stage ->
            if (_binding == null) return@observe

            when (stage) {
                1 -> {
                    binding.stage2Button.visibility = View.VISIBLE
                    binding.approveIdeaButton.visibility = View.VISIBLE
                    binding.stage3Button.visibility = View.GONE
                    binding.approveProblemButton.visibility = View.GONE
                    binding.Info2.visibility = View.GONE
                }
                2 -> {
                    binding.stage2Button.visibility = View.GONE
                    binding.approveIdeaButton.visibility = View.GONE
                    binding.stage3Button.visibility = View.VISIBLE
                    binding.approveProblemButton.visibility = View.VISIBLE
                    binding.Info2.visibility = View.VISIBLE
                    // Навигация на InfoFragment2 ПЕРЕМЕЩЕНА в обработчик нажатия stage2Button
                }
                3 -> {
                    binding.stage2Button.visibility = View.GONE
                    binding.approveIdeaButton.visibility = View.GONE
                    binding.stage3Button.visibility = View.VISIBLE
                    binding.approveProblemButton.visibility = View.VISIBLE
                    binding.Info2.visibility = View.VISIBLE
                    // TODO: Навигация на InfoFragment3 при переходе на Stage 3 (если нужно)
                    // if (findNavController().currentDestination?.id == R.id.ChatFragment) {
                    //     findNavController().navigate(R.id.action_ChatFragment_to_InfoFragment3)
                    // }
                }
                else -> {
                    binding.stage2Button.visibility = View.GONE
                    binding.approveIdeaButton.visibility = View.GONE
                    binding.stage3Button.visibility = View.GONE
                    binding.approveProblemButton.visibility = View.GONE
                    binding.Info2.visibility = View.GONE
                }
            }
        }

        // Наблюдение за подтверждениями этапа 1 для управления активностью кнопки Stage 2
        viewModel.approvalsStage1.observe(viewLifecycleOwner) { approvals ->
            if (_binding == null) return@observe

            val currentChat = viewModel.chatInfo.value // Используем chatInfo из ViewModel
            val currentStage = currentChat?.currentStage ?: 1

            if (currentStage == 1 && currentChat != null) {
                binding.stage2Button.isEnabled = currentChat.members.all { memberId ->
                    approvals[memberId] == true
                }

                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                if (currentUserId.isNotEmpty()) {
                    binding.approveIdeaButton.isEnabled = approvals[currentUserId] != true
                }
            }
        }

        // Наблюдение за подтверждениями этапа 2 для управления активностью кнопки Stage 3
        viewModel.approvalsStage2.observe(viewLifecycleOwner) { approvals ->
            if (_binding == null) return@observe

            val currentChat = viewModel.chatInfo.value // Используем chatInfo из ViewModel
            val currentStage = currentChat?.currentStage ?: 1

            if (currentStage == 2 && currentChat != null) {
                binding.stage3Button.isEnabled = currentChat.members.all { memberId ->
                    approvals[memberId] == true
                }

                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                if (currentUserId.isNotEmpty()) {
                    binding.approveProblemButton.isEnabled = approvals[currentUserId] != true
                }
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

        viewModel.sendMessage(text)
        binding.messageInput.text?.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::drawerSlideOut.isInitialized) drawerSlideOut.cancel()
        if (::overlayFadeOut.isInitialized) overlayFadeOut.cancel()
        _binding = null
    }
}
