package com.example.project_helper.domain.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.navigation.fragment.findNavController
import com.example.project_helper.R

class InfoFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val aiChatButton = view.findViewById<Button>(R.id.btn_ai_help)
        val commandChatButton = view.findViewById<Button>(R.id.btn_command_chat)
        aiChatButton.setOnClickListener {
            findNavController().navigate(R.id.action_InfoFragment_to_NeuroChatFragment)
        }
        commandChatButton.setOnClickListener {
            findNavController().navigate(R.id.action_InfoFragment_to_CommandChatFragment)
        }
    }
}