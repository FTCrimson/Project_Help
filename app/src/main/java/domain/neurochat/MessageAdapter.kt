package com.example.project_helper.domain.neurochat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.project_helper.data.api.deepseek.Message
import com.example.project_helper.databinding.ItemMessageBinding
import io.noties.markwon.Markwon
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessageAdapter(context: Context, private val markwon: Markwon) : ListAdapter<Message, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    private val VIEW_TYPE_USER = 1
    private val VIEW_TYPE_BOT = 2
    private val VIEW_TYPE_SYSTEM = 3

    class VisibleMessageViewHolder(private val binding: ItemMessageBinding, private val markwon: Markwon) : RecyclerView.ViewHolder(binding.root) {
        private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        fun bind(message: Message) {
            binding.userMessageContainer.visibility = View.GONE
            binding.botMessageContainer.visibility = View.GONE

            when {
                message.isUser -> {
                    binding.userMessageContainer.visibility = View.VISIBLE
                    binding.botMessageContainer.visibility = View.GONE
                    binding.userMessageText.text = message.content
                    binding.userMessageTime.text = timeFormat.format(Date(message.timestamp))
                }
                message.isBot -> {
                    binding.userMessageContainer.visibility = View.GONE
                    binding.botMessageContainer.visibility = View.VISIBLE
                    markwon.setMarkdown(binding.botMessageText, message.content)
                    binding.botMessageTime.text = timeFormat.format(Date(message.timestamp))
                }
            }
        }
    }

    class SystemMessageViewHolder(binding: ItemMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.visibility = View.GONE
            binding.root.layoutParams = RecyclerView.LayoutParams(0, 0)
        }
        fun bind(message: Message) {
        }
    }


    override fun getItemViewType(position: Int): Int {
        return when {
            getItem(position).isUser -> VIEW_TYPE_USER
            getItem(position).isBot -> VIEW_TYPE_BOT
            getItem(position).isSystem -> VIEW_TYPE_SYSTEM
            else -> throw IllegalArgumentException("Unknown message type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return when (viewType) {
            VIEW_TYPE_USER, VIEW_TYPE_BOT -> VisibleMessageViewHolder(binding, markwon)
            VIEW_TYPE_SYSTEM -> SystemMessageViewHolder(binding)
            else -> throw IllegalArgumentException("Unknown view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        when (holder) {
            is VisibleMessageViewHolder -> holder.bind(message)
            is SystemMessageViewHolder -> holder.bind(message)
        }
    }

    class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.timestamp == newItem.timestamp
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }
}
