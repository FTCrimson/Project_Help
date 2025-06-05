package com.example.project_helper.features.auth.fragments

import com.example.project_helper.features.neurochat.MessageAdapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope // Используем lifecycleScope для корутин
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_helper.data.api.deepseek.DeepSeekApiClient // Импортируем клиент API
import com.example.project_helper.databinding.FragmentNeuroChatBinding // <-- ИСПРАВЛЕН ИМПОРТ BINDING КЛАССА
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log // Для логирования
import android.widget.Toast // Для коротких сообщений пользователю
import com.example.project_helper.data.auth.api.deepseek.ChatCompletionRequest
import com.example.project_helper.data.auth.api.deepseek.Message

class NeuroChatFragment : Fragment() {

    private var _binding: FragmentNeuroChatBinding? = null
    // Это свойство доступно только между onCreateView и onDestroyView
    private val binding get() = _binding!!

    private lateinit var messageAdapter: MessageAdapter
    // Список для хранения истории сообщений. Важно для поддержания контекста беседы.
    // API DeepSeek (и OpenAI-совместимые) требуют отправлять всю историю сообщений.
    private val messages = mutableListOf<Message>()

    // Модель DeepSeek, которую мы хотим использовать. "deepseek-reasoner" соответствует R1.
    private val DEEPSEEK_MODEL = "deepseek/deepseek-chat-v3-0324:free"

    // Тег для логирования
    private val TAG = "NeuroChatFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Надуваем разметку с помощью View Binding, используя сгенерированный класс FragmentNeuroChatBinding
        _binding = FragmentNeuroChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Настраиваем RecyclerView
        messageAdapter = MessageAdapter()
        binding.chatRecyclerView.apply {
            // Используем LinearLayoutManager для вертикального списка сообщений
            layoutManager = LinearLayoutManager(context).apply {
                // Опционально: если хочешь, чтобы новые сообщения добавлялись снизу
                // stackFromEnd = true
            }
            adapter = messageAdapter
        }

        // Отправка приветственного сообщения от бота при первом запуске (опционально)
        if (messages.isEmpty()) {
            // Добавляем системное сообщение для установки контекста (опционально)
            // messages.add(Message(role = "system", content = "Ты — дружелюбный ассистент, готовый помочь с вопросами по программированию."))
            // Затем добавляем приветственное сообщение от бота для отображения
            addBotMessage("Привет! Я нейросеть, чем могу помочь?")
        }


        // Устанавливаем слушатель на кнопку отправки
        binding.sendButton.setOnClickListener {
            sendMessage()
        }

        // Устанавливаем слушатель на нажатие "Done" на клавиатуре в поле ввода
        // EditorInfo.IME_ACTION_SEND также может подойти, если клавиатура его поддерживает
        binding.messageInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true // Обработано
            } else {
                false // Не обработано
            }
        }
    }

    // Логика отправки сообщения пользователя и получения ответа нейросети
    private fun sendMessage() {
        val userText = binding.messageInput.text.toString().trim()

        if (userText.isEmpty()) {
            // Не отправляем пустое сообщение
            Toast.makeText(requireContext(), "Введите ваше сообщение", Toast.LENGTH_SHORT).show()
            return
        }

        // 1. Добавляем сообщение пользователя в список и обновляем UI
        addUserMessage(userText)
        binding.messageInput.text.clear() // Очищаем поле ввода

        // 2. Показываем индикатор печати
        showTypingIndicator(true)

        // 3. Формируем запрос к API
        // Отправляем всю текущую историю сообщений для поддержания контекста
        val request = ChatCompletionRequest(
            model = DEEPSEEK_MODEL,
            messages = messages.toList(),
            stream = false // Не используем стриминг в этом примере
        )

        // 4. Выполняем асинхронный вызов API с использованием корутин
        // lifecycleScope связан с жизненным циклом фрагмента, launch создает новую корутину
        // Dispatchers.IO подходит для сетевых операций
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Выполняем сетевой запрос. suspend функция ждет ответа, не блокируя поток.
                val response = DeepSeekApiClient.apiService.createChatCompletion(request)
                Log.d(TAG, "API Response received: $response")

                // 5. Обрабатываем ответ на главном потоке (для обновления UI)
                withContext(Dispatchers.Main) {
                    showTypingIndicator(false) // Скрываем индикатор печати

                    // Проверяем, есть ли ответ от модели и извлекаем текст
                    val botMessageContent = response.choices.firstOrNull()?.message?.content
                    if (!botMessageContent.isNullOrBlank()) {
                        // Добавляем сообщение нейросети в список и обновляем UI
                        addBotMessage(botMessageContent)
                    } else {
                        // Если ответ пустой или отсутствует
                        Toast.makeText(requireContext(), "Не удалось получить ответ от нейросети", Toast.LENGTH_SHORT).show()
                        Log.e(TAG, "Received empty or null content in API response choices")
                    }
                }

            } catch (e: Exception) {
                // 6. Обрабатываем ошибки (сеть, парсинг и т.д.)
                // Ошибки обрабатываем также на главном потоке, чтобы безопасно показать Toast
                Log.e(TAG, "Error sending message to DeepSeek API", e)
                withContext(Dispatchers.Main) {
                    showTypingIndicator(false) // Скрываем индикатор печати
                    // Выводим более информативное сообщение об ошибке, если возможно
                    val errorMessage = "Ошибка связи с нейросетью: ${e.localizedMessage ?: e.message ?: "Неизвестная ошибка"}"
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()

                    // Опционально: можно удалить последнее сообщение пользователя из истории,
                    // если не удалось получить ответ, чтобы не сбивать контекст для будущих запросов.
                    // messages.removeLastOrNull()
                    // messageAdapter.submitList(messages.toList()) {
                    //     binding.chatRecyclerView.scrollToPosition(messages.size - 1)
                    // }
                }
            }
        }
    }

    // Вспомогательная функция для добавления сообщения пользователя в список и обновления UI
    private fun addUserMessage(text: String) {
        val userMessage = Message(role = "user", content = text)
        messages.add(userMessage)
        // submitList отправляет новый список в адаптер, который эффективно обновляет RecyclerView
        messageAdapter.submitList(messages.toList()) {
            // Прокручиваем до последнего сообщения после того, как список будет обновлен
            binding.chatRecyclerView.scrollToPosition(messages.size - 1)
        }
    }

    // Вспомогательная функция для добавления сообщения бота в список и обновления UI
    private fun addBotMessage(text: String) {
        val botMessage = Message(role = "assistant", content = text)
        messages.add(botMessage)
        // submitList отправляет новый список в адаптер, который эффективно обновляет RecyclerView
        messageAdapter.submitList(messages.toList()) {
            // Прокручиваем до последнего сообщения после того, как список будет обновлен
            binding.chatRecyclerView.scrollToPosition(messages.size - 1)
        }
    }

    // Вспомогательная функция для управления видимостью индикатора печати
    private fun showTypingIndicator(isVisible: Boolean) {
        binding.typingIndicator.visibility = if (isVisible) View.VISIBLE else View.GONE
        // Если индикатор появляется/исчезает, это может повлиять на расположение элементов.
        // ConstraintLayout обычно справляется, но при необходимости можно добавить
        // логику изменения LayoutParams или обновления LayoutManager.
    }


    override fun onDestroyView() {
        super.onDestroyView()
        // Очищаем привязку при уничтожении представления фрагмента
        _binding = null
        Log.d(TAG, "NeuroChatFragment view destroyed.")
    }

    // Опционально: Если нужно сохранить историю чата при уничтожении/пересоздании фрагмента
    // например, при повороте экрана, можно использовать ViewModel с LiveData или SavedStateHandle.
    // override fun onSaveInstanceState(outState: Bundle) {
    //     super.onSaveInstanceState(outState)
    //     // Здесь можно сохранить список messages
    // }
    // override fun onViewStateRestored(savedInstanceState: Bundle?) {
    //     super.onViewStateRestored(savedInstanceState)
    //     // Здесь можно восстановить список messages
    // }
}