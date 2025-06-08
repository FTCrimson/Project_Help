package com.example.project_helper.domain.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_helper.R
import com.example.project_helper.data.api.deepseek.ChatCompletionRequest
import com.example.project_helper.data.api.deepseek.DeepSeekApiClient
import com.example.project_helper.data.api.deepseek.Message
import com.example.project_helper.data.auth.RoleSelection
import com.example.project_helper.databinding.FragmentNeuroChatBinding
import com.example.project_helper.domain.neurochat.MessageAdapter
import presentation.viewmodel.ProfileViewModel
import io.noties.markwon.Markwon
import io.noties.markwon.html.HtmlPlugin
import io.noties.markwon.linkify.LinkifyPlugin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class NeuroChatFragment : Fragment() {

    private var _binding: FragmentNeuroChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var messageAdapter: MessageAdapter
    private val messages = mutableListOf<Message>()
    private val chatHistory = mutableListOf<Message>()

    private lateinit var markwon: Markwon
    private lateinit var profileViewModel: ProfileViewModel

    private val DEEPSEEK_MODEL = "deepseek/deepseek-chat-v3-0324:free"

    private val TAG = "NeuroChatFragment"

    private var isDrawerOpen = false

    private lateinit var drawerContainer: ConstraintLayout
    private lateinit var drawerPanel: View
    private lateinit var drawerOverlay: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNeuroChatBinding.inflate(inflater, container, false)

        markwon = Markwon.builder(requireContext())
            .usePlugin(HtmlPlugin.create())
            .usePlugin(LinkifyPlugin.create())
            .build()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileViewModel = ViewModelProvider(requireActivity()).get(ProfileViewModel::class.java)

        messageAdapter = MessageAdapter(requireContext(), markwon)
        binding.chatRecyclerView.apply {
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true
            }
            adapter = messageAdapter
        }

        if (chatHistory.isEmpty()) {
            val roleSelection = profileViewModel.getRoleSelection()
            val systemPrompt = buildSystemPrompt(roleSelection)
            chatHistory.add(Message(role = "system", content = systemPrompt, timestamp = Date().time))

            sendInitialPrompt()
        } else {
            messages.addAll(chatHistory.filter { !it.isSystem })
            updateMessageList()
        }

        binding.sendButton.setOnClickListener {
            sendMessage()
        }

        binding.messageInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else {
                false
            }
        }

        initDrawer()
    }

    private fun initDrawer() {
        val menuButton: ImageButton = binding.root.findViewById(R.id.Menu)
        drawerContainer = binding.root.findViewById(R.id.drawerContainer)
        drawerOverlay = binding.root.findViewById(R.id.drawerOverlay)
        drawerPanel = binding.root.findViewById(R.id.drawerPanel)
        val aiChatButton = binding.root.findViewById<Button>(R.id.aiChatButton)
        val commandChatButton = binding.root.findViewById<Button>(R.id.commandChatButton)

        menuButton.setOnClickListener {
            if (isDrawerOpen) {
                closeDrawer()
            } else {
                openDrawer()
            }
        }

        drawerOverlay.setOnClickListener {
            closeDrawer()
        }

        binding.aiChatButton.setOnClickListener {
            closeDrawer()
        }

        binding.commandChatButton.setOnClickListener {
            closeDrawer()
            findNavController().navigate(R.id.action_NeuroChatFragment_to_CommandChatFragment)
        }

        binding.ProfileButton.setOnClickListener {
            findNavController().navigate(R.id.action_NeuroChatFragment_to_ProfileFragment)
            closeDrawer()
        }
    }

    private fun openDrawer() {
        if (isDrawerOpen) return

        isDrawerOpen = true
        drawerContainer.visibility = View.VISIBLE

        drawerOverlay.animate()
            .alpha(1f)
            .setDuration(300)
            .start()

        drawerPanel.animate()
            .translationX(0f)
            .setDuration(300)
            .start()
    }

    private fun closeDrawer() {
        if (!isDrawerOpen) return

        isDrawerOpen = false

        drawerOverlay.animate()
            .alpha(0f)
            .setDuration(300)
            .start()

        drawerPanel.animate()
            .translationX(-280f)
            .setDuration(300)
            .withEndAction {
                drawerContainer.visibility = View.GONE
            }
            .start()
    }

    private fun sendInitialPrompt() {
        val request = ChatCompletionRequest(
            model = DEEPSEEK_MODEL,
            messages = chatHistory.toList(),
            stream = false
        )

        showTypingIndicator(true)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Попытка инициализации клиента перед использованием
                DeepSeekApiClient.initialize()
                val apiService = DeepSeekApiClient.apiService

                if (apiService == null) {
                    withContext(Dispatchers.Main) {
                        showTypingIndicator(false)
                        Toast.makeText(requireContext(), "Не удалось инициализировать API клиент.", Toast.LENGTH_LONG).show()
                    }
                    Log.e(TAG, "API client not initialized.")
                    return@launch // Выходим из корутины
                }

                val response = apiService.createChatCompletion(request)
                Log.d(TAG, "Initial API Response received: $response")

                withContext(Dispatchers.Main) {
                    showTypingIndicator(false)

                    val botMessageContent = response.choices.firstOrNull()?.message?.content
                    val botMessageRole = response.choices.firstOrNull()?.message?.role

                    if (botMessageRole == "assistant" && !botMessageContent.isNullOrBlank()) {
                        val botMessage = Message(role = "assistant", content = botMessageContent, timestamp = Date().time)
                        messages.add(botMessage)
                        chatHistory.add(botMessage)
                        updateMessageList()
                    } else {
                        val errorDetail = response.choices.firstOrNull()?.finishReason ?: "No content or invalid response"
                        Toast.makeText(requireContext(), "Не удалось получить первое сообщение от нейросети: $errorDetail", Toast.LENGTH_LONG).show()
                        Log.e(TAG, "Initial API response had incorrect role or empty content. Role: $botMessageRole, Content: $botMessageContent, Error Detail: $errorDetail")
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error sending initial prompt to API", e)
                withContext(Dispatchers.Main) {
                    showTypingIndicator(false)
                    val errorMessage = "Ошибка при отправке начального промпта: ${e.localizedMessage ?: e.message ?: "Неизвестная ошибка"}"
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun buildSystemPrompt(roleSelection: RoleSelection?): String {
        val userInfo = when (roleSelection?.roleType) {
            "student" -> "Ученик (${roleSelection.role}) в сфере ${roleSelection.field}"
            "expert" -> "Эксперт в области ${roleSelection.role}"
            "mentor" -> "Наставник ученика ${roleSelection.studentName}"
            else -> "Пользователь"
        }

        return """
            Ты — дружелюбный и креативный помощник в проектной деятельности и Тебя зовут Project Helper.
            Твоя основная задача — помочь пользователю, который является $userInfo, создать или развить его проект.
            Поприветствуй пользователя, представься как помощник по проектной деятельности и вежливо попроси его рассказать о проекте, над которым он работает или который хочет создать.
            Когда пользователь опишет свой проект, внимательно проанализируй предоставленную информацию, учитывая его роль и сферу деятельности, которую он выбрал ранее.
            На основе этой информации предложи пользователю конкретные и полезные идеи по развитию или созданию проекта.
            Идеи должны быть релевантны проекту и учитывать контекст пользователя (его роль).
            Будь поддерживающим и поощряй дальнейшее обсуждение.
            Общайся на русском языке.
            Твой первый ответ должен быть приветствием и вопросом о проекте.
        """.trimIndent()
    }

    private fun sendMessage() {
        val userText = binding.messageInput.text.toString().trim()

        if (userText.isEmpty()) {
            Toast.makeText(requireContext(), "Введите ваше сообщение", Toast.LENGTH_SHORT).show()
            return
        }

        val userMessage = Message(role = "user", content = userText, timestamp = Date().time)

        messages.add(userMessage)
        chatHistory.add(userMessage)

        binding.messageInput.text.clear()

        updateMessageList()

        showTypingIndicator(true)

        val request = ChatCompletionRequest(
            model = DEEPSEEK_MODEL,
            messages = chatHistory.toList(),
            stream = false
        )

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                DeepSeekApiClient.initialize()
                val apiService = DeepSeekApiClient.apiService

                if (apiService == null) {
                    withContext(Dispatchers.Main) {
                        showTypingIndicator(false)
                        Toast.makeText(requireContext(), "Не удалось инициализировать API клиент.", Toast.LENGTH_LONG).show()
                    }
                    Log.e(TAG, "API client not initialized.")
                    return@launch
                }

                val response = apiService.createChatCompletion(request)
                Log.d(TAG, "API Response received for user message: $response")

                withContext(Dispatchers.Main) {
                    showTypingIndicator(false)

                    val botMessageContent = response.choices.firstOrNull()?.message?.content
                    val botMessageRole = response.choices.firstOrNull()?.message?.role

                    if (botMessageRole == "assistant" && !botMessageContent.isNullOrBlank()) {
                        val botMessage = Message(role = "assistant", content = botMessageContent, timestamp = Date().time)
                        messages.add(botMessage)
                        chatHistory.add(botMessage)
                        updateMessageList()
                    } else {
                        val errorDetail = response.choices.firstOrNull()?.finishReason ?: "No content or invalid response"
                        Toast.makeText(requireContext(), "Не удалось получить ответ от нейросети: $errorDetail", Toast.LENGTH_LONG).show()
                        Log.e(TAG, "API response for user message had incorrect role or empty content. Role: $botMessageRole, Content: $botMessageContent, Error Detail: $errorDetail")
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error sending user message to API", e)
                withContext(Dispatchers.Main) {
                    showTypingIndicator(false)
                    val errorMessage = "Ошибка при отправке сообщения: ${e.localizedMessage ?: e.message ?: "Неизвестная ошибка"}"
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                }
            }
        }
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
        Log.d(TAG, "NeuroChatFragment view destroyed.")
    }
}