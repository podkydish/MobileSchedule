package com.example.kalonkotlin.client.entities


import com.example.kalonkotlin.client.connection
import com.example.kalonkotlin.client.pass
import com.example.kalonkotlin.client.status
import com.example.kalonkotlin.client.url
import com.example.kalonkotlin.client.user
import java.sql.DriverManager
import java.sql.ResultSet
import java.util.Date


data class Schedule(
    var group_name: String,
    var lesson_date: Date,
    var name: String,
    var professor: String,
    var room: String,
    var lesson_number: Int,
    var lesson_type: Int
) {
    companion object {
        fun scheduleRequest(query: String): List<Schedule> {
            var allSchedule: List<Schedule> = ArrayList()
            val thread = Thread {
                try {
                    Class.forName("org.postgresql.Driver")
                    connection = DriverManager.getConnection(url, user, pass)
                    val resultSet: ResultSet = connection.createStatement().executeQuery(query)
                    while (resultSet.next()) {
                        val newSchedule = Schedule(
                            resultSet.getString("group_name"),
                            resultSet.getDate("lesson_day"),
                            resultSet.getString("lesson_name"),
                            resultSet.getString("professor_lastname") + " " +
                                    resultSet.getString("professor_firstname") + " " +
                                    resultSet.getString("professor_secondname"),
                            resultSet.getString("rooms"),
                            resultSet.getInt("lesson_number"),
                            resultSet.getInt("lesson_type")
                        )
                        newSchedule.group_name = resultSet.getString("group_name")
                        newSchedule.lesson_date = resultSet.getDate("lesson_day")
                        newSchedule.name = resultSet.getString("lesson_name")
                        newSchedule.professor =
                            resultSet.getString("professor_lastname") + " " +
                                    resultSet.getString("professor_firstname") + " " +
                                    resultSet.getString("professor_secondname")

                        newSchedule.lesson_number = resultSet.getInt("lesson_number")
                        newSchedule.lesson_type = resultSet.getInt("lesson_type")
                        newSchedule.room = resultSet.getString("rooms")
                        allSchedule = allSchedule + newSchedule
                    }
                    status = true
                    println(allSchedule.size)
                    System.out.println("connection status:$status")
                } catch (e: Exception) {
                    e.printStackTrace()
                    status = false
                }
            }
            thread.start()
            try {
                thread.join()
            } catch (e: InterruptedException) {
                e.printStackTrace()
                status = false
            }
            return allSchedule
        }
    }

    fun getNumberToTime(): String {
        return when (this.lesson_number) {
            0 -> "9:00 - 10:30"
            1 -> "10:45 - 12:15"
            2 -> "13:00 - 14:30"
            3 -> "14:45 - 16:15"
            4 -> "16:30 - 18:00"
            5 -> "18:15 - 19:45"
            6 -> "20:00 - 21:30"
            7 -> "21:45 - 23:15"
            else -> "не найдено"
        }
    }

    fun getLessonType(): String {
        return when (this.lesson_type) {
            1 -> "Лекция"
            2 -> "Практика"
            3 -> "Лабораторная"
            else -> ""
        }
    }

}