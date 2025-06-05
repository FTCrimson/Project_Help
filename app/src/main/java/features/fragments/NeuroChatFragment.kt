package com.example.project_helper.features.auth.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_helper.data.api.deepseek.DeepSeekApiClient
import com.example.project_helper.databinding.FragmentNeuroChatBinding
import io.noties.markwon.Markwon
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log
import android.widget.Toast
import com.example.project_helper.data.auth.api.deepseek.ChatCompletionRequest
import com.example.project_helper.data.auth.api.deepseek.Message as ApiMessage
import com.example.project_helper.features.neurochat.MessageAdapter
import com.example.project_helper.features.neurochat.MessageAdapter.DisplayMessage

class NeuroChatFragment : Fragment() {

    private var _binding: FragmentNeuroChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var messageAdapter: MessageAdapter
    private val messages = mutableListOf<DisplayMessage>()
    private lateinit var markwon: Markwon

    private val DEEPSEEK_MODEL = "deepseek/deepseek-chat-v3-0324:free"
    private val TAG = "NeuroChatFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNeuroChatBinding.inflate(inflater, container, false)

        // Инициализация Markwon
        markwon = Markwon.builder(requireContext())
            .usePlugin(HtmlPlugin.create())
            .usePlugin(LinkifyPlugin.create())
            .build()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализация адаптера с Markwon
        messageAdapter = MessageAdapter(markwon)
        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true
            }
            adapter = messageAdapter
        }

        // Приветственное сообщение
        if (messages.isEmpty()) {
            addBotMessage("Привет! Я нейросеть, чем могу помочь?")
        }

        binding.sendButton.setOnClickListener { sendMessage() }
        binding.messageInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else {
                false
            }
        }
    }

    private fun sendMessage() {
        val userText = binding.messageInput.text.toString().trim()
        if (userText.isEmpty()) {
            Toast.makeText(requireContext(), "Введите ваше сообщение", Toast.LENGTH_SHORT).show()
            return
        }

        addUserMessage(userText)
        binding.messageInput.text.clear()

        showTypingIndicator(true)

        // Формируем запрос с учетом истории сообщений
        val apiMessages = messages.map {
            ApiMessage(
                role = if (it.isUser) "user" else "assistant",
                content = it.content
            )
        }

        val request = ChatCompletionRequest(
            model = DEEPSEEK_MODEL,
            messages = apiMessages,
            stream = false
        )

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = DeepSeekApiClient.apiService.createChatCompletion(request)
                Log.d(TAG, "API Response: $response")

                withContext(Dispatchers.Main) {
                    showTypingIndicator(false)
                    response.choices.firstOrNull()?.message?.content?.let { content ->
                        addBotMessage(content)
                    } ?: run {
                        Toast.makeText(requireContext(), "Пустой ответ от нейросети", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "API Error", e)
                withContext(Dispatchers.Main) {
                    showTypingIndicator(false)
                    Toast.makeText(
                        requireContext(),
                        "Ошибка: ${e.message ?: "Неизвестная ошибка"}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun addUserMessage(text: String) {
        val userMessage = DisplayMessage(content = text, isUser = true)
        messages.add(userMessage)
        updateMessageList()
    }

    private fun addBotMessage(text: String) {
        val botMessage = DisplayMessage(content = text, isUser = false)
        messages.add(botMessage)
        updateMessageList()
    }

    private fun updateMessageList() {
        messageAdapter.submitList(messages.toList()) {
            binding.chatRecyclerView.post {
                binding.chatRecyclerView.smoothScrollToPosition(messages.size - 1)
            }
        }
    }

    private fun showTypingIndicator(isVisible: Boolean) {
        binding.typingIndicator.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}