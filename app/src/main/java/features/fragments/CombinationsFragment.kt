package com.example.project_helper.features.fragments

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project_helper.R

class CombinationsFragment : Fragment() {

    private lateinit var word1TextView: TextView
    private lateinit var word2TextView: TextView
    private lateinit var wordsCard: CardView
    private val wordPairs = listOf(
        "Искусственный" to "интеллект",
        "Квантовый" to "компьютер",
        "Виртуальная" to "реальность",
        "Биометрическая" to "аутентификация",
        "Нейронные" to "сети",
        "Блокчейн" to "технологии",
        "Дополненная" to "реальность",
        "Беспилотный" to "автомобиль",
        "Умный" to "город",
        "Биотехнологии" to "медицина",
        "Космический" to "туризм",
        "Голографический" to "интерфейс"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_combinations, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        word1TextView = view.findViewById(R.id.word1TextView)
        word2TextView = view.findViewById(R.id.word2TextView)
        wordsCard = view.findViewById(R.id.wordsCard)
        val newWordsButton = view.findViewById<Button>(R.id.newWordsButton)
        val aiHelpButton = view.findViewById<Button>(R.id.aiHelpButton)

        // Начальная анимация появления
        wordsCard.alpha = 0f
        wordsCard.animate()
            .alpha(1f)
            .setDuration(800)
            .start()

        // Показать случайные слова
        showRandomWordsWithAnimation()

        // Обработчик кнопки новых слов
        newWordsButton.setOnClickListener {
            // Анимация нажатия
            it.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction {
                    it.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()
                    showRandomWordsWithAnimation()
                }
                .start()
        }

        // Обработчик кнопки AI Help
        aiHelpButton.setOnClickListener {
            // Эффект пульсации перед переходом
            val scale = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.05f)
            val alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 1f, 0.8f)
            ObjectAnimator.ofPropertyValuesHolder(it, scale, alpha).apply {
                duration = 200
                repeatCount = 2
                repeatMode = ObjectAnimator.REVERSE
                start()
            }

            // Задержка для завершения анимации перед переходом
            it.postDelayed({
                findNavController().navigate(R.id.action_CombinationsFragment_to_NeuroChatFragment)
            }, 500)
        }
    }

    private fun showRandomWordsWithAnimation() {
        // Анимация исчезновения
        wordsCard.animate()
            .alpha(0.2f)
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(300)
            .withEndAction {
                // Обновление слов
                val randomPair = wordPairs.random()
                word1TextView.text = randomPair.first
                word2TextView.text = randomPair.second

                // Анимация появления с эффектом "пружины"
                wordsCard.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(500)
                    .setInterpolator(OvershootInterpolator())
                    .start()

                // Анимация цвета текста
                animateTextColor(word1TextView, Color.parseColor("#00FFFF"))
                animateTextColor(word2TextView, Color.parseColor("#FF00FF"))
            }
            .start()
    }

    private fun animateTextColor(textView: TextView, targetColor: Int) {
        val startColor = textView.currentTextColor
        val animator = ValueAnimator.ofArgb(startColor, targetColor).apply {
            duration = 800
            addUpdateListener { animator ->
                textView.setTextColor(animator.animatedValue as Int)
            }
        }
        animator.start()
    }
}