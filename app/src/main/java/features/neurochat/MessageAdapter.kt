package com.example.project_helper.features.neurochat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.project_helper.databinding.ItemMessageBinding
import io.noties.markwon.Markwon
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class MessageAdapter(
    private val markwon: Markwon
) : ListAdapter<MessageAdapter.DisplayMessage, MessageAdapter.MessageViewHolder>(MessageDiffCallback()) {

    // Класс для отображения с уникальным ID
    data class DisplayMessage(
        val id: String = UUID.randomUUID().toString(),
        val content: String,
        val isUser: Boolean,
        val timestamp: Long = System.currentTimeMillis()
    )

    inner class MessageViewHolder(private val binding: ItemMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        fun bind(message: DisplayMessage) {
            binding.apply {
                // Скрываем оба контейнера
                userMessageContainer.visibility = View.GONE
                botMessageContainer.visibility = View.GONE

                // Форматируем время
                val timeText = timeFormat.format(Date(message.timestamp))

                if (message.isUser) {
                    userMessageContainer.visibility = View.VISIBLE
                    userMessageText.text = message.content
                    userMessageTime.text = timeText
                } else {
                    botMessageContainer.visibility = View.VISIBLE
                    markwon.setMarkdown(botMessageText, message.content) // Применяем Markdown
                    botMessageTime.text = timeText

                    // Добавляем обработчик для раскрытия/скрытия длинных сообщений
                    botMessageText.setOnClickListener {
                        if (botMessageText.maxLines == 10) {
                            botMessageText.maxLines = Integer.MAX_VALUE
                        } else {
                            botMessageText.maxLines = 10
                        }
                    }
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).isUser) VIEW_TYPE_USER else VIEW_TYPE_BOT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MessageDiffCallback : DiffUtil.ItemCallback<DisplayMessage>() {
        override fun areItemsTheSame(oldItem: DisplayMessage, newItem: DisplayMessage): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DisplayMessage, newItem: DisplayMessage): Boolean {
            return oldItem == newItem
        }
    }

    companion object {
        private const val VIEW_TYPE_USER = 1
        private const val VIEW_TYPE_BOT = 2
    }
}