package com.example.project_helper.domain.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.project_helper.R

class BrainStormFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_brainstorm, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.button1).setOnClickListener {
            // Ничего не происходит
        }

        view.findViewById<View>(R.id.button2).setOnClickListener {
            findNavController().navigate(R.id.action_BrainStormFragment_to_InfoFragment)
        }

        view.findViewById<View>(R.id.buttonCombinations).setOnClickListener {
            // Переход на CombinationsFragment
            findNavController().navigate(R.id.action_BrainStormFragment_to_CombinationsFragment)
        }
    }
}