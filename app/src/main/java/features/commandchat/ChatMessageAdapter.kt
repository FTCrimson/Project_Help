package com.example.project_helper.features.commandchat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.project_helper.R
import data.auth.commandchat.ChatMessage

class ChatMessageAdapter(private val currentUserId: String) :
    ListAdapter<ChatMessage, ChatMessageAdapter.MessageViewHolder>(MessageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mesage, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val sentContainer: ConstraintLayout = itemView.findViewById(R.id.sentContainer)
        private val receivedContainer: ConstraintLayout = itemView.findViewById(R.id.receivedContainer)
        private val sentMessageText: TextView = itemView.findViewById(R.id.sentMessageText)
        private val receivedMessageText: TextView = itemView.findViewById(R.id.receivedMessageText)

        init {
            // Проверка инициализации (для отладки)
            if (sentContainer == null) throw IllegalStateException("sentContainer not found")
            if (receivedContainer == null) throw IllegalStateException("receivedContainer not found")
            if (sentMessageText == null) throw IllegalStateException("sentMessageText not found")
            if (receivedMessageText == null) throw IllegalStateException("receivedMessageText not found")
        }

        fun bind(message: ChatMessage) {
            if (message.senderId == currentUserId) {
                sentContainer.visibility = View.VISIBLE
                receivedContainer.visibility = View.GONE
                sentMessageText.text = message.text
            } else {
                sentContainer.visibility = View.GONE
                receivedContainer.visibility = View.VISIBLE
                receivedMessageText.text = message.text
            }
        }
    }

    class MessageDiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage) =
            oldItem == newItem
    }
}