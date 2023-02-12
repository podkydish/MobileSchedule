package com.example.kalonkotlin.ui.professor

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

import androidx.fragment.app.Fragment
import com.example.kalonkotlin.R
import com.example.kalonkotlin.client.*

import com.example.kalonkotlin.client.entities.Professor
import com.example.kalonkotlin.client.entities.Schedule
import com.example.kalonkotlin.databinding.FragmentProfessorBinding
import org.apache.commons.text.similarity.LevenshteinDistance
import java.time.LocalDate
import java.time.ZoneId
import java.util.LinkedList
import java.util.Locale
import java.util.UUID



import java.util.stream.Collectors
import kotlin.Comparator
import kotlin.collections.ArrayList

class ProfessorFragment : Fragment() {
    private val weekdays = arrayOf("", "ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС")
    private val allProfessors: List<Professor> =
        Professor.professorRequest("SELECT*FROM public.professors")
    private var clientProfessor: Professor? = null
    private var outputProfessors: List<Professor> = LinkedList()
    private lateinit var profInfoText: TextView
    private lateinit var profSpinner: Spinner
    private lateinit var searchNameBtn: Button
    private lateinit var scheduleText: TextView
    private lateinit var nextDay: Button
    private lateinit var prevDay: Button
    private lateinit var todayBtn: Button
    private lateinit var onWeekBtn: Button
    private lateinit var profName: TextView
    private lateinit var allSchedule: List<Schedule>
    private var date = LocalDate.now()
    private lateinit var backTextBtn: TextView
    private lateinit var sPref: SharedPreferences
    private var _binding: FragmentProfessorBinding? = null

    private val binding get() = _binding!!

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfessorBinding.inflate(inflater, container, false)
        val root: View = binding.root

        if (!Network.checkConnectivity(this.requireContext())) {
            Toast.makeText(this.requireContext(), "Проверьте подключение к интернету", Toast.LENGTH_LONG).show()
        }
        scheduleText = root.findViewById(R.id.scheduleText_prof)
        nextDay = root.findViewById(R.id.prof_next_btn)
        prevDay = root.findViewById(R.id.back_prof)
        todayBtn = root.findViewById(R.id.now_prof)
        onWeekBtn = root.findViewById(R.id.onWeek_prof)

        backTextBtn = root.findViewById(R.id.backTextBtn)

        profName = root.findViewById(R.id.prof_name_status)
        profInfoText = root.findViewById(R.id.text_professor)
        profInfoText.text = "Введите ваше ФИО"
        profSpinner = root.findViewById(R.id.chooseProfSpin)
        searchNameBtn = root.findViewById(R.id.profChooseBtn)
        backTextBtn.visibility = View.INVISIBLE
        val input = root.findViewById<EditText>(R.id.editText2)
        input.hint = "Вводить сюда..."

        val searchBtn = root.findViewById<Button>(R.id.profSearchBtn)

        if (loadPref() != "") {
            outputProfessors = LinkedList(allProfessors)
            (outputProfessors as LinkedList<Professor>).removeIf { prof: Professor ->
                prof.siteId == UUID.fromString("00000000-0000-0000-0000-000000000000")
            }
            for (professor in outputProfessors) {
                if (professor.getFullName() == loadPref()) {
                    clientProfessor = professor
                    break
                }
            }
        }
        backTextBtn.visibility = View.VISIBLE
        backTextBtn.isClickable = true
        searchBtn.visibility = View.INVISIBLE
        input.visibility = View.INVISIBLE
        searchBtn.visibility = View.GONE
        searchBtn.isClickable = false
        if (clientProfessor != null) {
            searchByProf()
        }

        searchBtn.setOnClickListener {
            if (input.text.toString().split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray().size < 5) { // Считывание ФИО преподавателя
                backTextBtn.visibility = View.VISIBLE
                backTextBtn.isClickable = true
                profSpinner.visibility = View.VISIBLE
                searchNameBtn.visibility = View.VISIBLE
                searchBtn.visibility = View.INVISIBLE
                input.visibility = View.INVISIBLE
                professorSearch(input.text.toString().lowercase(Locale.getDefault()))
            } else {
                input.hint = "Проверьте написание и повторите попытку"
            }
        }
        searchNameBtn.setOnClickListener {
            backTextBtn.visibility = View.VISIBLE
            backTextBtn.isClickable = true
            profName.visibility = View.VISIBLE
            clientProfessor = outputProfessors[profSpinner.selectedItemPosition]
            searchByProf()
        }
        backTextBtn.setOnClickListener {
            profName.visibility = View.INVISIBLE
            profInfoText.visibility = View.VISIBLE
            searchNameBtn.visibility = View.VISIBLE
            onWeekBtn.visibility = View.INVISIBLE
            todayBtn.visibility = View.INVISIBLE
            prevDay.visibility = View.INVISIBLE
            nextDay.visibility = View.INVISIBLE
            scheduleText.visibility = View.INVISIBLE
            profSpinner.visibility = View.INVISIBLE
            searchNameBtn.visibility = View.INVISIBLE
            searchBtn.visibility = View.VISIBLE
            input.visibility = View.VISIBLE
            input.visibility = View.VISIBLE
            searchBtn.visibility = View.VISIBLE
            input.hint = "Вводить сюда..."
            backTextBtn.visibility = View.INVISIBLE
            backTextBtn.isClickable = false
        }
        nextDay.setOnClickListener {
            date = date.plusDays(1)
            searchByDate(allSchedule, date)
        }
        prevDay.setOnClickListener {
            date = date.minusDays(1)
            searchByDate(allSchedule, date)
        }
        todayBtn.setOnClickListener {
            date = LocalDate.now()
            searchByDate(allSchedule, date)
        }
        onWeekBtn.setOnClickListener {
            scheduleText.movementMethod = ScrollingMovementMethod()
            scheduleText.text = ""
            val firstDayOfWeek =
                date.minusDays((date.dayOfWeek.value - 1).toLong())
            var localText = ""
            for (i in 0..6) {
                val currentDay = firstDayOfWeek.plusDays(i.toLong())
                if (currentDay !== firstDayOfWeek) {
                    localText = scheduleText.text.toString()
                }
                searchByDate(allSchedule, currentDay)
                val answer = scheduleText.text.toString()
                scheduleText.text = localText + answer
            }
        }


        //
        return root
    }


    @SuppressLint("SetTextI18n")
    private fun searchByProf() {
        profInfoText.visibility = View.INVISIBLE
        profSpinner.visibility = View.INVISIBLE
        searchNameBtn.visibility = View.INVISIBLE
        onWeekBtn.visibility = View.VISIBLE
        todayBtn.visibility = View.VISIBLE
        prevDay.visibility = View.VISIBLE
        nextDay.visibility = View.VISIBLE
        scheduleText.visibility = View.VISIBLE
        profName.text = clientProfessor!!.firstname + " " + clientProfessor!!.secondname
        allSchedule = Schedule.scheduleRequest(
            "SELECT group_name, lesson_day, lesson_name," +
                    "professor_lastname, professor_firstname, professor_secondname," +
                    " lesson_number, lesson_type, rooms FROM public.groups AS g" +
                    " INNER JOIN public.lessons_groups AS lg ON lg.group_id=g.group_id" +
                    " INNER JOIN public.lessons AS l ON l.lesson_id = lg.lesson_id" +
                    " INNER JOIN public.lesson_rooms AS lr ON lr.room_id = l.lesson_id" +
                    " INNER JOIN public.lessons_professors AS lp ON lp.lesson_id = l.lesson_id" +
                    " INNER JOIN public.professors AS p ON p.professor_id = lp.professor_id" +
                    " WHERE p.professor_lastname = '" + clientProfessor!!.lastname + "'" +
                    " ORDER BY lesson_day"
        )
        savePref(clientProfessor!!.getFullName())
        searchByDate(allSchedule, date)
    }

    private fun professorSearch(userInputName: String) {
        outputProfessors = LinkedList(allProfessors)
        (outputProfessors as LinkedList<Professor>).removeIf { prof: Professor ->
            prof.siteId == UUID.fromString("00000000-0000-0000-0000-000000000000")
        }
        (outputProfessors as LinkedList<Professor>).sortWith { o1: Professor, o2: Professor ->
            val tmpToO1: Int = calculateDistanceBetweenStrings(o1.getFullName(), userInputName)
            val tmpToO2: Int = calculateDistanceBetweenStrings(o2.getFullName(), userInputName)
            tmpToO1.compareTo(tmpToO2)
        }
        profInfoText.text = "Выберите из списка нужного преподавателя"
        val spinnerArray: MutableList<String> = ArrayList() //накидываем в список группы
        for (i in 0..4) {
            spinnerArray.add(outputProfessors[i].getFullName())
        }
        val adapter =
            ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, spinnerArray)
        // Определяем разметку для использования при выборе элемента
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Применяем адаптер к элементу spinner
        profSpinner.adapter = adapter
        profSpinner.setSelection(0)
    }

    private fun calculateDistanceBetweenStrings(
        str1: String,
        str2: String
    ): Int { //расчёт разницы для преподавателей
        val spl1 =
            str1.lowercase(Locale.getDefault()).split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
        val spl2 =
            str2.lowercase(Locale.getDefault()).split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
        var dist = 0
        val distance: LevenshteinDistance = LevenshteinDistance.getDefaultInstance()
        if (spl1.size != spl2.size) {
            //Log.warn("arrays length isn't equal");
        }
        for (i in 0 until spl1.size.coerceAtMost(spl2.size)) {
            dist += distance.apply(spl1[i], spl2[i])
        }
        return dist
    }

    @SuppressLint("SetTextI18n")
    fun searchByDate(allSchedule: List<Schedule>, date: LocalDate) { // поиск расписания по дате
        println("поиск и вывод сообщения")
        val answer: StringBuilder
        val lessons: MutableList<Schedule> = ArrayList()
        for (sc in allSchedule) {
            if (sc.lesson_date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                    .equals(date)
            ) {
                lessons.add(sc)
            }
        }
        if (lessons.size != 0) {
            answer = StringBuilder()
            answer.append("ㅤㅤㅤㅤㅤ").append(CALENDAR_SMILE)
                .append(date.format(DATE_FORMATTER)).append(" (")
                .append(weekdays[date.dayOfWeek.value]).append(")")
                .append("\n")
            val lessonsForDay: List<Schedule> = lessons.stream()
                .filter { lesson: Schedule ->
                    lesson.lesson_date.toInstant().atZone(ZoneId.systemDefault())
                        .toLocalDate().equals(date)
                }
                .sorted(Comparator.comparing(Schedule::lesson_number))
                .collect(Collectors.toList()) as List<Schedule>
            for (lsn in lessonsForDay) {
                scheduleMessage(answer, lsn)
            }
            scheduleAnswer(date, date, answer)
        } else {
            scheduleText.text =
                """ㅤㅤㅤㅤㅤ${CALENDAR_SMILE}${date.format(DATE_FORMATTER)} (${
                    weekdays[date.dayOfWeek.value]
                })
$NO_PAIRS
"""
        }
    }

    private fun scheduleMessage(answer: java.lang.StringBuilder, lsn: Schedule) { // тело ответа для
        // расписания
        val localProfs: String = lsn.professor
        // if (lsn.getLesson_date().equals(firstDayOfWeek)) {
        // составление сообщения с данными о занятиях
        answer.append(
            """
            
            $TIME_SMILE
            """.trimIndent()
        ).append(lsn.getNumberToTime()).append("\n")
            .append(BOOKS_SMILE).append(lsn.name).append(" (")
            .append(lsn.getLessonType()).append(")\n")
            .append(UNIVERSITY_SMILE)
            .append(lsn.room.replace(ARRAY_REGEX, "")).append("\n")
            .append(PROFESSOR_SMILE)
            .append(localProfs /*.get(0).getFullName()*/)
        /* if (localProfs.size() > 1) {
                for (int i = 1; i < localProfs.size(); i++) {
                    answer.append(", ").append(localProfs.get(i).getFullName());
                }
            }*/answer.append("\n").append(STUDENT_SMILE)
            .append(lsn.group_name.replace(ARRAY_REGEX, ""))
            .append("\n").append("\n")
        //  }
        scheduleText.text = answer.toString()
    }

    @SuppressLint("SetTextI18n")
    private fun scheduleAnswer(
        date: LocalDate,
        firstDayOfWeek: LocalDate, answer: java.lang.StringBuilder
    ) { // построение ответа для расписания
        scheduleText.text = answer.toString()
        if (scheduleText.text == "" || scheduleText.text == """ㅤㅤㅤㅤㅤ$CALENDAR_SMILE${
                firstDayOfWeek.format(
                    DATE_FORMATTER
                )
            } (${weekdays[date.dayOfWeek.value]})
"""
        ) {
            scheduleText.text =
                """
            ${CALENDAR_SMILE + date.format(DATE_FORMATTER) + " (" + weekdays[date.dayOfWeek.value]})
            $NO_PAIRS
            """.trimIndent()
        }
    }

    private fun savePref(prefValue: String?) {
        sPref = requireActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
        val ed = sPref.edit()
        ed.putString(SAVED_TEXT, prefValue)
        ed.apply()
    }

    private fun loadPref(): String? {
        sPref = requireActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
        return sPref.getString(SAVED_TEXT, "")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val SAVED_TEXT = "client_professor"
    }
}


