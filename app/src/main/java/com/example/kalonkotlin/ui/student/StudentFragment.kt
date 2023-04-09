package com.example.kalonkotlin.ui.student

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast

import androidx.fragment.app.Fragment
import com.example.kalonkotlin.MainActivity
import com.example.kalonkotlin.R
import com.example.kalonkotlin.client.APP_PREFERENCES
import com.example.kalonkotlin.client.ARRAY_REGEX
import com.example.kalonkotlin.client.BOOKS_SMILE
import com.example.kalonkotlin.client.CALENDAR_SMILE
import com.example.kalonkotlin.client.DATE_FORMATTER
import com.example.kalonkotlin.client.Database
import com.example.kalonkotlin.client.Logging
import com.example.kalonkotlin.client.NO_PAIRS
import com.example.kalonkotlin.client.Network
import com.example.kalonkotlin.client.PROFESSOR_SMILE
import com.example.kalonkotlin.client.STUDENT_SMILE
import com.example.kalonkotlin.client.TIME_SMILE
import com.example.kalonkotlin.client.UNIVERSITY_SMILE

import com.example.kalonkotlin.client.entities.Group
import com.example.kalonkotlin.client.entities.Schedule
import com.example.kalonkotlin.databinding.FragmentStudentBinding
import java.time.LocalDate
import java.time.ZoneId
import java.util.Collections
import java.util.LinkedList
import java.util.Objects
import java.util.stream.Collectors


class StudentFragment : Fragment(), View.OnClickListener {
    private var _binding: FragmentStudentBinding? = null


    private val binding get() = _binding!!
    private val savedText = "client_group"
    private var clientGroup: Group? = null
    private var groups: List<Group> = LinkedList()
    private var allGroups: List<Group> = LinkedList()
    private var allSchedule: List<Schedule> = LinkedList()
    private var date: LocalDate = LocalDate.now()
    private lateinit var sPref: SharedPreferences
    private lateinit var chooseBtn: Button
    private lateinit var chooseCourse: Spinner
    private lateinit var chooseFac: Spinner
    private lateinit var chooseGroup: Spinner
    private lateinit var mainText: TextView
    private lateinit var courseText: TextView
    private lateinit var facultyText: TextView
    private lateinit var groupText: TextView
    private lateinit var searchBtn: Button
    private lateinit var nextButton: Button
    private lateinit var prevButton: Button
    private lateinit var onWeekBtn: Button
    private lateinit var nowBtn: Button
    private lateinit var scheduleText: TextView
    private lateinit var weekdays: Array<String>
    private lateinit var db: Database

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.item1) {
            (activity as MainActivity?)
                ?.setActionBarTitle(getString(R.string.title_student))
            chooseCourse.visibility = View.VISIBLE //блок насилия над элементами
            chooseFac.visibility = View.VISIBLE
            courseText.visibility = View.VISIBLE
            facultyText.visibility = View.VISIBLE
            chooseBtn.visibility = View.INVISIBLE
            searchBtn.visibility = View.VISIBLE
            chooseGroup.visibility = View.INVISIBLE
            groupText.visibility = View.INVISIBLE
            searchBtn.isClickable = true
            chooseBtn.isClickable = false
            nextButton.visibility = View.INVISIBLE
            nextButton.isClickable = false

            prevButton.visibility = View.INVISIBLE
            prevButton.isClickable = false

            onWeekBtn.visibility = View.INVISIBLE
            onWeekBtn.isClickable = false

            nowBtn.visibility = View.INVISIBLE
            nowBtn.isClickable = false

            scheduleText.visibility = View.INVISIBLE

            return true
        }
        return super.onOptionsItemSelected(item)

    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.student, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentStudentBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val context = requireContext()
        Logging.logTo(context, "Come in student")
        setHasOptionsMenu(true)
        db = Database(context)
        weekdays = resources.getStringArray(R.array.weekdays)
        chooseCourse = binding.courseNumber
        chooseFac = binding.facultyNumber
        chooseGroup = binding.group
        mainText = root.findViewById(R.id.text_student)
        courseText = binding.courseText
        facultyText = binding.facultyText
        groupText = binding.groupText
        chooseBtn = binding.chooseBtn
        searchBtn = root.findViewById(R.id.stud_search_btn)
        nextButton = root.findViewById(R.id.next_day_button)
        prevButton = root.findViewById(R.id.back_prof)
        onWeekBtn = root.findViewById(R.id.on_week_button)
        nowBtn = root.findViewById(R.id.now_button)
        scheduleText = root.findViewById(R.id.scheduleText_prof)
        scheduleText.movementMethod = ScrollingMovementMethod()

        if (!Network.checkConnectivity(this.requireContext())) {
            Toast.makeText(this.requireContext(), getString(R.string.connection_error), Toast.LENGTH_LONG).show()
            chooseCourse.visibility = View.INVISIBLE //блок насилия над элементами
            chooseFac.visibility = View.INVISIBLE
            courseText.visibility = View.INVISIBLE
            facultyText.visibility = View.INVISIBLE
            chooseBtn.isClickable = false
            chooseBtn.visibility = View.INVISIBLE
            chooseGroup.visibility = View.INVISIBLE
            groupText.visibility = View.INVISIBLE
            mainText.visibility = View.INVISIBLE
            scheduleText.visibility = View.VISIBLE
            searchBtn.visibility = View.GONE
            scheduleText.movementMethod = ScrollingMovementMethod()
            nextButton.visibility = View.VISIBLE
            nextButton.isClickable = true
            prevButton.visibility = View.VISIBLE
            prevButton.isClickable = true
            onWeekBtn.visibility = View.VISIBLE
            onWeekBtn.isClickable = true
            nowBtn.visibility = View.VISIBLE
            nowBtn.isClickable = true
            setHasOptionsMenu(false)
            (activity as MainActivity?)
                ?.setActionBarTitle(loadPref())
            allSchedule = db.getStudentSchedule()
            searchByDate(allSchedule, date)
        } else {
            try {
                allGroups = Group.groupRequest("SELECT * FROM public.groups")
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, getString(R.string.database_error), Toast.LENGTH_SHORT).show()
            }

            mainText.text = getString(R.string.group_input)


            if (!Objects.equals(loadPref(), "")) {
                for (group in allGroups) {
                    if (group.name == loadPref()) {
                        clientGroup = group
                        break
                    }
                }
            }
            if (clientGroup != null) {
                chooseCourse.visibility = View.INVISIBLE //блок насилия над элементами
                chooseFac.visibility = View.INVISIBLE
                courseText.visibility = View.INVISIBLE
                facultyText.visibility = View.INVISIBLE
                searchBtn.visibility = View.GONE
                searchBtn.isClickable = false
                searchByGroup()
            }
            searchBtn.setOnClickListener(this)
            chooseBtn.setOnClickListener(this)
        }
        nextButton.setOnClickListener(this)
        prevButton.setOnClickListener(this)
        onWeekBtn.setOnClickListener(this)
        nowBtn.setOnClickListener(this)
        return root
    }


    private fun searchByGroup() {
        Logging.logTo(requireContext(), "professor $clientGroup")
        db.deleteStudentData()
        db.insertStudentData(clientGroup!!)
        //вывод в app bar выбранной группы
        (activity as MainActivity?)
            ?.setActionBarTitle(clientGroup!!.name)
        savePref()
        chooseBtn.isClickable = false
        chooseBtn.visibility = View.INVISIBLE
        chooseGroup.visibility = View.INVISIBLE
        groupText.visibility = View.INVISIBLE
        mainText.visibility = View.INVISIBLE
        nextButton.visibility = View.VISIBLE
        nextButton.isClickable = true
        prevButton.visibility = View.VISIBLE
        prevButton.isClickable = true
        onWeekBtn.visibility = View.VISIBLE
        onWeekBtn.isClickable = true
        nowBtn.visibility = View.VISIBLE
        nowBtn.isClickable = true
        scheduleText.visibility = View.VISIBLE
        try {
            allSchedule = Schedule.scheduleRequest("SELECT group_name, lesson_day, lesson_name,"
                    + " professor_lastname, professor_firstname, professor_secondname,"
                    + "lesson_number, lesson_type, rooms FROM public.groups AS g"
                    + " INNER JOIN public.lessons_groups AS lg ON lg.group_id=g.group_id"
                    + " INNER JOIN public.lessons AS l ON l.lesson_id = lg.lesson_id"
                    + " INNER JOIN public.lesson_rooms AS lr ON lr.room_id = l.lesson_id"
                    + " INNER JOIN public.lessons_professors AS lp ON lp.lesson_id = l.lesson_id"
                    + " INNER JOIN public.professors AS p ON p.professor_id = lp.professor_id"
                    + " WHERE g.group_name='" + clientGroup!!.name + "'"
                    + " ORDER BY lesson_day")
            searchByDate(allSchedule, date)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, getString(R.string.database_error), Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun searchByDate(allSchedule: List<Schedule>, date: LocalDate) { // поиск расписания по дате
        val answer: StringBuilder
        var lessons: List<Schedule> = ArrayList()
        for (sc in allSchedule) {
            if (sc.lesson_date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().equals(date)) {
                lessons = lessons + sc
            }
        }
        if (lessons.isNotEmpty()) {
            answer = StringBuilder()
            answer.append(getString(R.string.space)).append(CALENDAR_SMILE).append(date.format(DATE_FORMATTER)).append(" (").append(weekdays[date.dayOfWeek.value]).append(")").append("\n")
            val lessonsForDay: List<Schedule> = lessons.stream()
                .filter { lesson -> lesson.lesson_date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().equals(date) }
                .sorted(Comparator.comparing(Schedule::lesson_number)).collect(Collectors.toList())
            for (lsn in lessonsForDay) {
                scheduleMessage(answer, lsn)
            }
            scheduleAnswer(date, date, answer)
        } else {
            scheduleText.text = """${getString(R.string.space)}$CALENDAR_SMILE${date.format(DATE_FORMATTER)} (${weekdays[date.dayOfWeek.value]})
$NO_PAIRS"""
        }
    }

    private fun scheduleMessage(answer: java.lang.StringBuilder, lsn: Schedule) { // тело ответа для
        // расписания
        val localProfs = lsn.professor
        // if (lsn.getLesson_date().equals(firstDayOfWeek)) {
        // составление сообщения с данными о занятиях
        answer.append("""
    
    $TIME_SMILE
    """.trimIndent()).append(lsn.getNumberToTime()).append("\n")
            .append(BOOKS_SMILE).append(lsn.name).append(" (")
            .append(lsn.getLessonType()).append(")\n")
            .append(UNIVERSITY_SMILE).append(lsn.room.replace(ARRAY_REGEX.toRegex(), "")).append("\n")
            .append(PROFESSOR_SMILE)
            .append(localProfs /*.get(0).getFullName()*/)
        /* if (localProfs.size() > 1) {
            for (int i = 1; i < localProfs.size(); i++) {
                answer.append(", ").append(localProfs.get(i).getFullName());
            }
        }*/answer.append("\n").append(STUDENT_SMILE)
            .append(lsn.group_name.replace(ARRAY_REGEX.toRegex(), ""))
            .append("\n").append("\n")
        //  }
        scheduleText.text = answer.toString()
    }

    @SuppressLint("SetTextI18n")
    private fun scheduleAnswer(date: LocalDate,
                               firstDayOfWeek: LocalDate, answer: java.lang.StringBuilder) { // построение ответа для расписания
        scheduleText.text = answer.toString()
        if (scheduleText.text == "" || scheduleText.text == """${getString(R.string.space)}$CALENDAR_SMILE${firstDayOfWeek.format(DATE_FORMATTER)} (${weekdays[date.dayOfWeek.value]})
"""
        ) {
            scheduleText.text = """
            ${CALENDAR_SMILE + date.format(DATE_FORMATTER) + " (" + weekdays[date.dayOfWeek.value]})
            $NO_PAIRS
            """.trimIndent()
        }
    }

    private fun savePref() {
        sPref = requireActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
        val ed = sPref.edit()
        ed.putString(clientGroup.toString(), savedText)
        ed.apply()
    }

    private fun loadPref(): String? {
        sPref = requireActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
        return sPref.getString(clientGroup.toString(), "")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @SuppressLint("SetTextI18n")
    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.stud_search_btn -> {
                    val faculty = chooseFac.selectedItem.toString()
                    val course = chooseCourse.selectedItem.toString()
                    groups = Group.groupRequest("SELECT * FROM public.groups WHERE group_faculty = $faculty AND group_course = $course")
                    Collections.sort(groups, Comparator.comparing(Group::name))
                    val spinnerArray: MutableList<String> = ArrayList() //накидываем в список группы

                    if (groups.isNotEmpty()) {
                        chooseCourse.visibility = View.INVISIBLE //блок насилия над элементами
                        chooseFac.visibility = View.INVISIBLE
                        courseText.visibility = View.INVISIBLE
                        facultyText.visibility = View.INVISIBLE
                        chooseBtn.visibility = View.VISIBLE
                        searchBtn.visibility = View.GONE
                        chooseGroup.visibility = View.VISIBLE
                        groupText.visibility = View.VISIBLE
                        searchBtn.isClickable = false
                        chooseBtn.isClickable = true
                        for (group in groups) {
                            spinnerArray.add(group.name)
                        }
                    } else {
                        Toast.makeText(context, getString(R.string.input_warning), Toast.LENGTH_SHORT).show()
                    }
                    val adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, spinnerArray)
                    // Определяем разметку для использования при выборе элемента
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    // Применяем адаптер к элементу spinner
                    chooseGroup.adapter = adapter
                }
                R.id.chooseBtn -> {
                    scheduleText.text = ""
                    for (group in groups) {
                        if (group.name == chooseGroup.selectedItem.toString()) {
                            clientGroup = group
                            break
                        }
                    }
                    searchByGroup()
                }
                R.id.next_day_button -> {
                    scheduleText.text = ""
                    date = date.plusDays(1)
                    searchByDate(allSchedule, date)
                }
                R.id.back_prof -> {
                    scheduleText.text = ""
                    date = date.minusDays(1)
                    searchByDate(allSchedule, date)
                }
                R.id.now_button -> {
                    scheduleText.text = ""
                    date = LocalDate.now()
                    searchByDate(allSchedule, date)
                }
                R.id.on_week_button -> {
                    scheduleText.movementMethod = ScrollingMovementMethod()
                    scheduleText.text = ""
                    val firstDayOfWeek = date.minusDays((date.dayOfWeek.value - 1).toLong())
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

            }

        }

    }
}
