package com.example.kalonkotlin.client.entities


import java.sql.DriverManager
import java.sql.ResultSet
import com.example.kalonkotlin.client.connection
import com.example.kalonkotlin.client.status
import com.example.kalonkotlin.client.url
import com.example.kalonkotlin.client.user
import com.example.kalonkotlin.client.pass
import java.util.TreeSet


data class Group(
    var id: Int,
    var name: String,
    var course: Int,
    var faculty: Int,
    var headman: Int,
    var lessons: Set<Lesson> = TreeSet()
) {

    fun addLesson(lsn: Lesson) {
        if (!this.lessons.contains(lsn)) {
            this.lessons += lsn
            lsn.addGroup(this)
        }
    }

    override fun toString(): String {
        return name
    }

    companion object {
        fun groupRequest(query: String): List<Group> {
            var groups: List<Group> = ArrayList()
            val thread = Thread {
                try {
                    Class.forName("org.postgresql.Driver")
                    connection = DriverManager.getConnection(url, user, pass)
                    val resultSet: ResultSet = connection.createStatement().executeQuery(query)
                    while (resultSet.next()) {
                        val newGroup = Group(
                            resultSet.getInt("group_id"),
                            resultSet.getString("group_name"),
                            resultSet.getInt("group_course"),
                            resultSet.getInt("group_faculty"),
                            resultSet.getInt("headman_student_id")
                        )
                        newGroup.id = resultSet.getInt("group_id")
                        newGroup.course = resultSet.getInt("group_course")
                        newGroup.faculty = resultSet.getInt("group_faculty")
                        newGroup.name = resultSet.getString("group_name")
                        groups = groups + newGroup
                    }
                    status = true
                    println(groups.size)
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
            return groups
        }
    }
}
