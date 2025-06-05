package com.example.project_helper.features.neurochat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.project_helper.data.auth.api.deepseek.Message
import com.example.project_helper.databinding.ItemMessageBinding // Используем View Binding для item_message.xml
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Адаптер для списка сообщений в чате
class MessageAdapter : ListAdapter<Message, MessageAdapter.MessageViewHolder>(MessageDiffCallback()) {

    // Константы для типов представлений (ViewType)
    private val VIEW_TYPE_USER = 1
    private val VIEW_TYPE_BOT = 2

    // ViewHolder для элементов списка
    class MessageViewHolder(private val binding: ItemMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        // Форматтер времени
        private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        fun bind(message: Message) {
            // Скрываем оба контейнера по умолчанию
            binding.userMessageContainer.visibility = View.GONE
            binding.botMessageContainer.visibility = View.GONE

            // Определяем, кто отправил сообщение, и показываем нужный контейнер
            if (message.isUser) {
                // Это сообщение пользователя
                binding.userMessageContainer.visibility = View.VISIBLE
                binding.userMessageText.text = message.content
                binding.userMessageTime.text = timeFormat.format(Date()) // Или используй время из API/локальное время создания
            } else {
                // Это сообщение нейросети
                binding.botMessageContainer.visibility = View.VISIBLE
                binding.botMessageText.text = message.content
                binding.botMessageTime.text = timeFormat.format(Date()) // Или используй время из API/локальное время создания
            }
        }
    }

    // Определяет тип представления для элемента в заданной позиции
    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).isUser) VIEW_TYPE_USER else VIEW_TYPE_BOT
    }

    // Создает новые ViewHolders (вызывается LayoutManager'ом, когда нужен новый ViewHolder)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        // Надуваем разметку элемента списка
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    // Заменяет содержимое представления (View) элемента списка (вызывается LayoutManager'ом)
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = getItem(position) // Получаем сообщение по позиции
        holder.bind(message) // Привязываем данные к ViewHolder'у
    }

    // DiffUtil.ItemCallback для эффективного обновления списка
    class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
        // Проверяет, является ли один и тот же элемент
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            // В простом случае, можем считать элементы одинаковыми, если у них одинаковое содержимое
            // В реальном приложении лучше использовать уникальный ID сообщения
            return oldItem.content == newItem.content && oldItem.role == newItem.role // Упрощенная проверка
        }

        // Проверяет, совпадает ли содержимое элементов
        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem // Data class автоматически генерирует equals/hashCode на основе всех свойств
        }
    }
}