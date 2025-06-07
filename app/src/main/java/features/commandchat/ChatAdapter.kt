package com.example.project_helper.features.commandchat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
// import android.widget.ImageButton // Этот импорт, возможно, больше не нужен, если нигде не используется ImageButton
import android.widget.TextView
import android.widget.Button // Добавляем импорт для Button
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import data.auth.commandchat.Chat
import com.example.project_helper.R
// Если хотите быть более точным, можете использовать MaterialButton:
// import com.google.android.material.button.MaterialButton

class ChatAdapter(
    private val onChatClick: (Chat) -> Unit,
    private val onAddMemberClick: (Chat) -> Unit
) : ListAdapter<Chat, ChatAdapter.ChatViewHolder>(ChatDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val chatName: TextView = itemView.findViewById(R.id.chatName)
        // ИСПРАВЛЕНО: Изменяем тип с ImageButton на Button (или MaterialButton)
        // Вариант с Button:
        private val addMemberButton: Button = itemView.findViewById(R.id.addMemberButton)
        // Вариант с MaterialButton (если вы добавили импорт):
        // private val addMemberButton: MaterialButton = itemView.findViewById(R.id.addMemberButton)


        fun bind(chat: Chat) {
            // Убедитесь, что у объекта Chat есть поле 'name'
            chatName.text = chat.name ?: "Название чата неизвестно" // Добавим проверку на null для надежности

            itemView.setOnClickListener { onChatClick(chat) }
            addMemberButton.setOnClickListener { onAddMemberClick(chat) }
        }
    }

    class ChatDiffCallback : DiffUtil.ItemCallback<Chat>() {
        // Убедитесь, что у объекта Chat есть поле 'id'
        override fun areItemsTheSame(oldItem: Chat, newItem: Chat) = oldItem.id == newItem.id
        // Убедитесь, что Chat является data class или имеет правильную реализацию equals/hashCode
        override fun areContentsTheSame(oldItem: Chat, newItem: Chat) = oldItem == newItem
    }
}
