package com.example.project_helper.domain.fragments

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
    private var lastWord1: String = ""
    private var lastWord2: String = ""
    private val wordPairs = listOf(
        "Автобус" to "якорь",
        "Банкир" to "электростанция",
        "Велосипед" to "цирк",
        "Гитара" to "шарф",
        "Дельфин" to "щука",
        "Ежевика" to "щепка",
        "Жонглер" to "эскимо",
        "Зонт" to "юла",
        "Ирис" to "ярмарка",
        "Карандаш" to "астра",
        "Лесник" to "барабан",
        "Микрофон" to "ваза",
        "Ноутбук" to "герань",
        "Огурец" to "дерево",
        "Пианино" to "елка",
        "Роза" to "жаворонок",
        "Солнце" to "заря",
        "Трамвай" to "игра",
        "Утюг" to "карета",
        "Флаг" to "лама",
        "Хлопушка" to "магнит",
        "Циркуль" to "нора",
        "Чайник" to "опера",
        "Шапка" to "парус",
        "Щетка" to "роща",
        "Эмульсия" to "слава",
        "Юбка" to "тень",
        "Яблоко" to "узел",
        "Абрикос" to "ферма",
        "Брюки" to "хвост",
        "Ветер" to "цепь",
        "Глобус" to "чайка",
        "Дверь" to "шар",
        "Ежедневник" to "щука",
        "Жимолость" to "щепка",
        "Зеркало" to "эхо",
        "Игла" to "юмор",
        "Капуста" to "яма",
        "Линейка" to "аист",
        "Морковь" to "бубен",
        "Носки" to "ведро",
        "Облако" to "гвоздь",
        "Пиджак" to "домбра",
        "Ракета" to "ерш",
        "Сова" to "жираф",
        "Торт" to "заяц",
        "Удочка" to "иволга",
        "Фартук" to "капля",
        "Хурма" to "лебедь",
        "Цветок" to "мангал",
        "Чемодан" to "норка",
        "Шарф" to "окунь",
        "Щенок" to "попугай",
        "Экран" to "ртуть",
        "Юрта" to "сахар",
        "Ящерица" to "тополь",
        "Актер" to "укроп",
        "Баклажан" to "факел",
        "Валенки" to "хмель",
        "Грабли" to "цапля",
        "Дыня" to "черника",
        "Ель" to "шмель",
        "Желе" to "щель",
        "Зефир" to "электрод",
        "Икра" to "ювелир",
        "Какао" to "ястреб",
        "Лимон" to "альбом",
        "Медуза" to "банан",
        "Нож" to "вишня",
        "Омар" to "герой",
        "Палец" to "диск",
        "Рюкзак" to "енот",
        "Сапог" to "жук",
        "Тигр" to "замок",
        "Улица" to "инжир",
        "Флейта" to "кувшин",
        "Халат" to "лодка",
        "Цифра" to "майка",
        "Часы" to "нарцисс",
        "Шея" to "обруч",
        "Щи" to "пила",
        "Эполет" to "ручка",
        "Яд" to "сова",
        "Альбом" to "телефон",
        "Барабанщик" to "улитка",
        "Виноград" to "флакон",
        "Гармонь" to "хомяк",
        "Джемпер" to "цыпленок",
        "Ежевичное" to "шарик",
        "Жалюзи" to "щупальце",
        "Забор" to "эскалатор",
        "Изюм" to "яичница",
        "Календарь" to "абсент",
        "Лопата" to "бочка",
        "Майонез" to "вафля",
        "Напиток" to "горох",
        "Очки" to "джем",
        "Палатка" to "ежевика",
        "Ремень" to "журавль",
        "Салют" to "злак",
        "Табурет" to "индюк",
        "Укроп" to "крем",
        "Фиалка" to "лак",
        "Холст" to "миндаль",
        "Цыпленок" to "натюрморт",
        "Черешня" to "олень",
        "Шиповник" to "пингвин",
        "Щит" to "рубин",
        "Электровоз" to "сапфир",
        "Юрист" to "тарелка",
        "Янтарь" to "уголь",
        "Арбуз" to "арфа",
        "Бокал" to "баранина",
        "Вагон" to "велодром",
        "Гвоздика" to "горка",
        "Дерево" to "домино",
        "Ежевика" to "егерь",
        "Жаба" to "жатка",
        "Звезда" to "зенит",
        "Иволга" to "изумруд",
        "Капуста" to "каток",
        "Лимонад" to "луч",
        "Морковь" to "маяк",
        "Носорог" to "навигатор",
        "Облако" to "овод",
        "Парусник" to "пила",
        "Ромашка" to "ручка",
        "Сахар" to "сироп",
        "Табурет" to "топор",
        "Укроп" to "улика",
        "Фазан" to "фужер",
        "Холодильник" to "халат",
        "Цапля" to "циркуль",
        "Чайник" to "чернила",
        "Шампунь" to "шхуна",
        "Щука" to "щупальца",
        "Экран" to "эхолот",
        "Юла" to "юрта",
        "Якорь" to "яшма",
        "Авторучка" to "акула",
        "Бумеранг" to "бусинка",
        "Вишня" to "вьюга",
        "Галстук" to "гусеница",
        "Джем" to "дятел",
        "Ежевика" to "ежедневник",
        "Жёлудь" to "журавль",
        "Зонтик" to "золото",
        "Инжир" to "икра",
        "Карусель" to "каштан",
        "Лейка" to "лещ",
        "Машина" to "муравей",
        "Носок" to "норка",
        "Огурец" to "одуванчик",
        "Пальма" to "пингвин",
        "Радуга" to "рюкзак",
        "Самокат" to "скворец",
        "Тапки" to "таракан",
        "Утка" to "уголь",
        "Фен" to "футляр",
        "Хлеб" to "холодильник",
        "Циркуль" to "цыплёнок",
        "Чайка" to "черепаха",
        "Шкаф" to "шорты",
        "Щётка" to "щука",
        "Электричка" to "эскимос",
        "Ювелир" to "юрка",
        "Яблоня" to "ящерица",
        "Абрикос" to "алмаз",
        "Баклажан" to "бинокль",
        "Воробей" to "вьюнок",
        "Гармошка" to "гриб",
        "Дождь" to "дымка",
        "Ёрш" to "ежевика",
        "Жемчуг" to "жук",
        "Заяц" to "зебра",
        "Игрушка" to "изюм",
        "Капля" to "карандаш",
        "Лев" to "лимон",
        "Майка" to "молоко",
        "Ножницы" to "небо",
        "Ослик" to "орех",
        "Пароход" to "пирог",
        "Ремень" to "рыба",
        "Свеча" to "сахар",
        "Тюльпан" to "топор",
        "Улитка" to "укроп",
        "Фиалка" to "флаг",
        "Хомяк" to "холодильник",
        "Цветы" to "цыплёнок",
        "Чемодан" to "чайник",
        "Шляпа" to "шишка",
        "Щетка" to "щука",
        "Электричество" to "эскимо",
        "Юмор" to "юбка",
        "Яхта" to "яблоко",
        "Арка" to "автобус",
        "Бусинка" to "бабочка",
        "Весло" to "василек",
        "Гвоздь" to "гранат",
        "Дерево" to "диван",
        "Ель" to "ежевика",
        "Журавль" to "жасмин",
        "Звезда" to "земляника",
        "Индюк" to "ирис",
        "Карандаш" to "кувшин",
        "Лопата" to "лилия",
        "Морковь" to "море",
        "Нож" to "носки",
        "Окно" to "обезьяна",
        "Пальто" to "паровоз",
        "Рак" to "ракета",
        "Сапог" to "солнце",
        "Тюлень" to "телефон",
        "Утюг" to "укроп",
        "Фен" to "фонарь",
        "Хлеб" to "холодильник",
        "Цветы" to "цыпленок",
        "Чайник" to "черепаха",
        "Шляпа" to "шкаф",
        "Щука" to "щетка",
        "Экран" to "электричка",
        "Юбка" to "юла",
        "Яблоко" to "янтарь",
        "Аист" to "арена",
        "Бурундук" to "барометр",
        "Велосипед" to "варенье",
        "Гармошка" to "гитара",
        "Дерево" to "дирижабль",
        "Ежевика" to "ершик",
        "Жёлудь" to "жасмин",
        "Зонт" to "зубило",
        "Инжир" to "иволга",
        "Карусель" to "календарь",
        "Лимон" to "ложка",
        "Машина" to "магнитофон",
        "Носорог" to "ножницы",
        "Облако" to "огурец",
        "Пальто" to "петух",
        "Радуга" to "рояль",
        "Самолет" to "сахарница",
        "Тарелка" to "тюльпан",
        "Утюг" to "удочка",
        "Филин" to "фартук",
        "Холодильник" to "хомяк",
        "Цветок" to "цыпленок",
        "Черепаха" to "чемодан",
        "Шляпа" to "шоколад",
        "Щетка" to "щука",
        "Электричка" to "эскимо",
        "Юла" to "ювелир",
        "Яблоко" to "ящерица",
        "Арбуз" to "якорь",
        "Баклажан" to "электрод",
        "Вишня" to "цикада",
        "Груша" to "шампунь",
        "Дыня" to "щука",
        "Ежевика" to "щепка",
        "Жасмин" to "эскимо",
        "Зонт" to "юбилей",
        "Ирис" to "яшма",
        "Капуста" to "астрагал",
        "Лимон" to "бублик",
        "Малина" to "вальс",
        "Нож" to "герань",
        "Облако" to "домра",
        "Палец" to "ель",
        "Радуга" to "жаворонок",
        "Свеча" to "закат",
        "Торт" to "изумруд",
        "Улитка" to "каракуль",
        "Факел" to "ладонь",
        "Холст" to "мох",
        "Цирк" to "навес",
        "Чайник" to "омар",
        "Шкаф" to "печь",
        "Щит" to "рюкзак",
        "Эхо" to "сирень",
        "Юмор" to "таз",
        "Яблоко" to "укроп",

        "Апельсин" to "абрикос",
        "Брюки" to "бокал",
        "Велосипед" to "вагон",
        "Гитара" to "гвоздика",
        "Дверь" to "диван",
        "Ежедневник" to "ёршик",
        "Жимолость" to "жемчуг",
        "Зеркало" to "зонт",
        "Игла" to "изумруд",
        "Какао" to "капуста",
        "Линейка" to "лёд",
        "Морковь" to "малина",
        "Носки" to "ножницы",
        "Облако" to "обезьяна",
        "Пиджак" to "паровоз",
        "Ракета" to "ракушка",
        "Сова" to "сапог",
        "Торт" to "тарелка",
        "Удочка" to "уголь",
        "Фартук" to "флейта",
        "Хурма" to "хлеб",
        "Цветок" to "циркуль",
        "Чемодан" to "чайник",
        "Шарф" to "шляпа",
        "Щенок" to "щетка",
        "Экран" to "электричество",
        "Юбка" to "юла",
        "Ящерица" to "яблоко",

        "Автомобиль" to "алмаз",
        "Береза" to "бумеранг",
        "Водопад" to "вьюга",
        "Гармошка" to "гриб",
        "Джемпер" to "дым",
        "Ель" to "ёрш",
        "Железо" to "жук",
        "Забор" to "золото",
        "Инжир" to "иволга",
        "Карусель" to "кость",
        "Лимонад" to "лампа",
        "Майонез" to "море",
        "Ноутбук" to "ножницы",
        "Омар" to "одуванчик",
        "Палатка" to "парус",
        "Рюкзак" to "радуга",
        "Салют" to "свекла",
        "Табурет" to "топор",
        "Укроп" to "уголь",
        "Флейта" to "фонарь",
        "Халат" to "хлеб",
        "Цыпленок" to "цикада",
        "Черепаха" to "чайник",
        "Шиповник" to "шляпа",
        "Щука" to "щетка",
        "Электровоз" to "электричество",
        "Юрист" to "юбка",
        "Янтарь" to "яблоко",

        "Арфа" to "ананас",
        "Бабочка" to "болото",
        "Весло" to "ваза",
        "Гранатомет" to "голубь",
        "Домкрат" to "дымка",
        "Енот" to "ель",
        "Жемчуг" to "жаворонок",
        "Заря" to "забор",
        "Икра" to "иволга",
        "Каток" to "ключ",
        "Лес" to "лимон",
        "Маяк" to "мышь",
        "Ножницы" to "небо",
        "Огурец" to "облако",
        "Пингвин" to "палец",
        "Ручка" to "радуга",
        "Сахар" to "свекла",
        "Тюльпан" to "тетрадь",
        "Улитка" to "уголь",
        "Фонарик" to "флаг",
        "Холод" to "хлеб",
        "Цыпленок" to "циркуль",
        "Чемодан" to "чай",
        "Шоколад" to "шляпа",
        "Щетка" to "щука",
        "Электричка" to "эскимо",
        "Ювелир" to "юла",
        "Яблоня" to "янтарь",

        "Абажур" to "ананас",
        "Бамбук" to "букет",
        "Веник" to "вальс",
        "Гладиолус" to "гриб",
        "Дерево" to "диск",
        "Ель" to "ерш",
        "Жимолость" to "жалюзи",
        "Замок" to "зонт",
        "Иволга" to "иголка",
        "Карандаш" to "кастрюля",
        "Лилия" to "лев",
        "Малина" to "молоко",
        "Ножницы" to "норка",
        "Огурец" to "облако",
        "Пистолет" to "парус",
        "Ракета" to "ручка",
        "Самолет" to "солнце",
        "Тюльпан" to "топор",
        "Утюг" to "уголь",
        "Фонарь" to "факел",
        "Холодильник" to "холодец",
        "Цветок" to "циркуль",
        "Черепаха" to "чайник",
        "Шкаф" to "шорты",
        "Щука" to "щепка",
        "Электричка" to "эскимо",
        "Ювелир" to "юбка",
        "Яблоко" to "яхта",

        "Автомат" to "апельсин",
        "Блины" to "бусы",
        "Вишня" to "весло",
        "Грабли" to "гусеница",
        "Дым" to "джорж",
        "Ежевика" to "ель",
        "Жимолость" to "жук",
        "Заяц" to "золото",
        "Изумруд" to "икра",
        "Карусель" to "кактус",
        "Лейка" to "лимон",
        "Малина" to "море",
        "Нож" to "носки",
        "Огурец" to "окунь",
        "Парикмахер" to "пенал",
        "Радуга" to "ремень",
        "Сапоги" to "сосна",
        "Тюльпан" to "тополь",
        "Удочка" to "улитка",
        "Фонарь" to "фартук",
        "Холодильник" to "халат",
        "Цветок" to "цифра",
        "Чемодан" to "черешня",
        "Шляпа" to "шорты",
        "Щука" to "щетка",
        "Электричка" to "эскимо",
        "Ювелир" to "юла",
        "Яблоня" to "ящик",

        "Арбуз" to "арка",
        "Бочка" to "браслет",
        "Веник" to "вагон",
        "Гора" to "груша",
        "Дом" to "диван",
        "Ель" to "ерш",
        "Жаба" to "жемчуг",
        "Звезда" to "забор",
        "Игла" to "инжир",
        "Карандаш" to "катушка",
        "Лампа" to "лимон",
        "Малина" to "медуза",
        "Носок" to "норка",
        "Огурец" to "облако",
        "Пирог" to "парус",
        "Радуга" to "ракета",
        "Свеча" to "самолет",
        "Тюльпан" to "топор",
        "Удочка" to "улитка",
        "Фартук" to "факел",
        "Холод" to "хлеб",
        "Цветок" to "цыпленок",
        "Чемодан" to "черепаха",
        "Шляпа" to "шорты",
        "Щука" to "щетка",
        "Электричка" to "эскимо",
        "Ювелир" to "юла",
        "Яблоня" to "ящерица",

        "Абрикос" to "астра",
        "Бусы" to "баня",
        "Ветер" to "ваза",
        "Голубь" to "гвоздь",
        "Дерево" to "диван",
        "Ель" to "ежевика",
        "Жук" to "жемчуг",
        "Зонт" to "закат",
        "Иволга" to "известь",
        "Кран" to "клубок",
        "Лимонад" to "луч",
        "Майка" to "мох",
        "Носорог" to "нож",
        "Окунь" to "облако",
        "Парус" to "пирог",
        "Ручка" to "радуга",
        "Сахар" to "самолет",
        "Тюльпан" to "трава",
        "Уголь" to "улица",
        "Фарфор" to "фен",
        "Хвост" to "холодильник",
        "Цыпленок" to "цирк",
        "Черепаха" to "чернила",
        "Шорты" to "шляпа",
        "Щука" to "щербет",
        "Электричка" to "эхо",
        "Юла" to "юмор",
        "Ящерица" to "яблоня",

        "Альбом" to "айва",
        "Бублик" to "болото",
        "Веник" to "василек",
        "Гвоздь" to "гриб",
        "Дымка" to "доска",
        "Ежевика" to "ель",
        "Жимолость" to "журавль",
        "Закат" to "звезда",
        "Изумруд" to "индюк",
        "Ковёр" to "кисть",
        "Ложка" to "луна",
        "Магнит" to "молоко",
        "Ножницы" to "норка",
        "Огурец" to "очки",
        "Паровоз" to "пуговица",
        "Радуга" to "рюкзак",
        "Самокат" to "сахар",
        "Тюльпан" to "тарелка",
        "Удочка" to "улитка",
        "Фартук" to "фильм",
        "Холодильник" to "холодец",
        "Цветок" to "циркуль",
        "Чайник" to "чемодан",
        "Шляпа" to "шорты",
        "Щетка" to "щука",
        "Эскимо" to "эхо",
        "Юбка" to "юла",
        "Яблоко" to "ягода"
    )

    private val firstWords = wordPairs.map { it.first }.distinct()
    private val secondWords = wordPairs.map { it.second }.distinct()

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
        val CommandChatButton = view.findViewById<Button>(R.id.CommandChatButton)

        wordsCard.alpha = 0f
        wordsCard.animate()
            .alpha(1f)
            .setDuration(800)
            .start()

        showRandomWordsWithAnimation()

        newWordsButton.setOnClickListener {
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

        CommandChatButton.setOnClickListener {
            findNavController().navigate(R.id.action_CombinationsFragment_to_CommandChatFragment)
        }

        aiHelpButton.setOnClickListener {
            val scale = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.05f)
            val alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 1f, 0.8f)
            ObjectAnimator.ofPropertyValuesHolder(it, scale, alpha).apply {
                duration = 200
                repeatCount = 2
                repeatMode = ObjectAnimator.REVERSE
                start()
            }

            it.postDelayed({
                val bundle = Bundle().apply {
                    putString("word1", lastWord1)
                    putString("word2", lastWord2)
                }
                findNavController().navigate(
                    R.id.action_CombinationsFragment_to_NeuroChatFragment,
                    bundle
                )
            }, 500)
        }
    }

    private fun showRandomWordsWithAnimation() {
        wordsCard.animate()
            .alpha(0.2f)
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(300)
            .withEndAction {
                val randomFirst = firstWords.random()
                val randomSecond = secondWords.random()

                lastWord1 = randomFirst
                lastWord2 = randomSecond

                word1TextView.text = randomFirst
                word2TextView.text = randomSecond

                wordsCard.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(500)
                    .setInterpolator(OvershootInterpolator())
                    .start()

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