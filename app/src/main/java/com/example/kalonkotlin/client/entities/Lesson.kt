package com.example.kalonkotlin.client.entities

import com.example.kalonkotlin.client.enums.LessonType
import java.time.LocalDate


data class Lesson(var id: Int,
                  var name: String,
                  var type: LessonType,
                  var day: LocalDate,
                  var number: Short,
                  var professors: List<Professor>,
                  var groups: List<Group>,
                  var rooms: List<String>) {
    fun addGroup(group: Group) {
        if (!groups.contains(group)) {
            groups += group
            group.addLesson(this)
        }
    }

    fun addProfessor(professor: Professor) {
        if (!professors.contains(professor)) {
            professors += professor
            professor.addLesson(this)
        }
    }

    fun getNumberToTime(): String {
        return when (number) {
            0.toShort() -> "9:00 - 10:30"
            1.toShort() -> "10:45 - 12:15"
            2.toShort() -> "13:00 - 14:30"
            3.toShort() -> "14:45 - 16:15"
            4.toShort() -> "16:30 - 18:00"
            5.toShort() -> "18:15 - 19:45"
            6.toShort() -> "20:00 - 21:30"
            7.toShort() -> "21:45 - 23:15"
            else -> "не найдено"
        }
    }

    override fun toString(): String {
        return "Lesson{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type.getName() +
                ", day=" + day +
                ", time=" + getNumberToTime() +
                ", rooms=" + rooms +
                '}';
    }
}