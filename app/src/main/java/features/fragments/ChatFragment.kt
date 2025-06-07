package com.example.project_helper.features.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_helper.R
import com.example.project_helper.databinding.FragmentChatBinding
import com.example.project_helper.features.commandchat.ChatMessageAdapter
import com.example.project_helper.features.commandchat.ChatViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatViewModel by viewModels()
    private lateinit var messageAdapter: ChatMessageAdapter

    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

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

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()

        viewModel.loadChatMessages(chatId)
        viewModel.loadChatInfo(chatId)
    }

    private fun setupRecyclerView() {
        messageAdapter = ChatMessageAdapter(currentUserId)

        binding.messagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true
            }
            adapter = messageAdapter
        }
    }

    private fun setupClickListeners() {
        binding.sendButton.setOnClickListener {
            sendMessage()
        }

        binding.addMemberButton.setOnClickListener {
            showAddMemberDialog()
        }

        binding.requestAccessButton.setOnClickListener {
            requestAccessToChat()
        }
    }

    private fun showAddMemberDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_member, null)
        val usernameInput = dialogView.findViewById<TextInputEditText>(R.id.usernameInput)

        AlertDialog.Builder(requireContext())
            .setTitle("Добавить участника")
            .setView(dialogView)
            .setPositiveButton("Добавить") { _, _ ->
                val username = usernameInput.text.toString().trim()
                if (username.isNotEmpty()) {
                    val chatId = arguments?.getString("chatId") ?: return@setPositiveButton
                    viewModel.addMemberToChat(chatId, username)
                } else {
                    Toast.makeText(requireContext(), "Введите username", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun requestAccessToChat() {
        val chatId = arguments?.getString("chatId") ?: return
        Toast.makeText(requireContext(), "Запрос доступа отправлен создателю чата", Toast.LENGTH_SHORT).show()
        // Здесь можно добавить логику отправки запроса создателю чата
    }

    private fun observeViewModel() {
        viewModel.messages.observe(viewLifecycleOwner, Observer { messages ->
            if (messages.isEmpty()) {
                binding.emptyStateTextView.visibility = View.VISIBLE
                binding.messagesRecyclerView.visibility = View.GONE
            } else {
                binding.emptyStateTextView.visibility = View.GONE
                binding.messagesRecyclerView.visibility = View.VISIBLE
                messageAdapter.submitList(messages) {
                    binding.messagesRecyclerView.scrollToPosition(messages.size - 1)
                }
            }
        })

        viewModel.error.observe(viewLifecycleOwner, Observer { message ->
            if (message != null) {
                if (message.contains("доступ", ignoreCase = true)) {
                        showAccessDeniedView(message)
                    } else {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                }
            }
        })

        viewModel.chatInfo.observe(viewLifecycleOwner) { chat ->
            binding.chatTitle.text = chat.name
        }

        viewModel.addMemberResult.observe(viewLifecycleOwner) { result ->
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
    }

    private fun showAccessDeniedView(message: String) {
        binding.apply {
            messagesRecyclerView.visibility = View.GONE
            inputLayout.visibility = View.GONE
            emptyStateTextView.visibility = View.GONE
            accessDeniedView.visibility = View.VISIBLE
            accessDeniedTextView.text = message
        }
    }

    private fun sendMessage() {
        val chatId = arguments?.getString("chatId") ?: return
        val text = binding.messageInput.text.toString().trim()

        if (text.isEmpty()) {
            Toast.makeText(requireContext(), "Введите сообщение", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.sendMessage(chatId, currentUserId, text)
        binding.messageInput.text?.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}