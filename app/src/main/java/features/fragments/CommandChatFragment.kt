package com.example.project_helper.features.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project_helper.R
import com.example.project_helper.databinding.FragmentCommandChatBinding
import com.example.project_helper.features.commandchat.ChatAdapter
import com.example.project_helper.features.commandchat.CommandChatViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import data.auth.commandchat.Chat
import data.auth.commandchat.User

class CommandChatFragment : Fragment() {

    private var _binding: FragmentCommandChatBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CommandChatViewModel by viewModels()
    private lateinit var chatAdapter: ChatAdapter

    private lateinit var drawerContainer: ConstraintLayout
    private lateinit var drawerPanel: View
    private lateinit var drawerOverlay: View
    private var isDrawerOpen = false

    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommandChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDrawer()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()

        viewModel.loadUserChats(currentUserId)

        if (!isDrawerOpen) {
            drawerContainer.visibility = View.GONE
            val drawerPanelWidth = resources.getDimensionPixelSize(R.dimen.drawer_panel_width)
            drawerPanel.translationX = -drawerPanelWidth.toFloat()
        }
    }

    private fun setupDrawer() {
        drawerContainer = binding.root.findViewById(R.id.drawerContainer)
        drawerOverlay = binding.root.findViewById(R.id.drawerOverlay)
        drawerPanel = binding.root.findViewById(R.id.drawerPanel)

        binding.root.findViewById<ImageButton>(R.id.Menu).setOnClickListener {
            if (isDrawerOpen) closeDrawer() else openDrawer()
        }

        drawerOverlay.setOnClickListener { closeDrawer() }

        binding.root.findViewById<Button>(R.id.aiChatButton).setOnClickListener {
            closeDrawer()
            val currentDestinationId = findNavController().currentDestination?.id
            if (currentDestinationId != R.id.NeuroChatFragment) {
                findNavController().navigate(R.id.NeuroChatFragment)
            }
        }

        binding.root.findViewById<Button>(R.id.commandChatButton).setOnClickListener {
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
            .withEndAction {
                drawerContainer.visibility = View.GONE
            }
            .start()

        val drawerPanelWidth = resources.getDimensionPixelSize(R.dimen.drawer_panel_width)
        drawerPanel.animate()
            .translationX(-drawerPanelWidth.toFloat())
            .setDuration(300)
            .start()
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(
            onChatClick = { chat -> openChat(chat) },
            onAddMemberClick = { chat -> showAddMemberDialog(chat) }
        )

        binding.chatsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatAdapter
        }
    }

    private fun setupClickListeners() {
        binding.createChatButton.setOnClickListener { createNewChat() }
        binding.emptyState.findViewById<Button>(R.id.createFirstChatButton).setOnClickListener { createNewChat() }
    }

    private fun observeViewModel() {
        viewModel.chats.observe(viewLifecycleOwner, Observer { chats ->
            if (chats.isNullOrEmpty()) {
                binding.emptyState.visibility = View.VISIBLE
                binding.chatsRecyclerView.visibility = View.GONE
            } else {
                binding.emptyState.visibility = View.GONE
                binding.chatsRecyclerView.visibility = View.VISIBLE
                chatAdapter.submitList(chats)
            }
        })

        viewModel.error.observe(viewLifecycleOwner, Observer { message ->
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun createNewChat() {
        val userId = currentUserId
        if (userId.isEmpty()) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }

        val newChat = Chat(
            creatorId = userId,
            members = listOf(userId)
        )

        viewModel.createChat(newChat) { chatId ->
            chatId?.let {
                openChat(newChat.copy(id = chatId))
            }
        }
    }

    private fun openChat(chat: Chat) {
        val bundle = Bundle().apply {
            putString("chatId", chat.id)
        }
        findNavController().navigate(R.id.ChatFragment, bundle)
    }

    private fun showAddMemberDialog(chat: Chat) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_member, null)
        val usernameInput = dialogView.findViewById<TextInputEditText>(R.id.usernameInput)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Member")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val username = usernameInput.text.toString().trim()
                if (username.isNotEmpty()) {
                    addMemberToChat(chat, username)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addMemberToChat(chat: Chat, username: String) {
        FirebaseFirestore.getInstance().collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val user = documents.documents[0].toObject(User::class.java)
                user?.let { // <- Здесь 'it' это User объект, полученный из Firestore
                    if (chat.members.contains(it.id)) {
                        Toast.makeText(requireContext(), "${username} is already a member", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }
                    viewModel.addMemberToChat(chat.id, it.id)
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error finding user", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
