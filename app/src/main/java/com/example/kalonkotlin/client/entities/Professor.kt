package com.example.kalonkotlin.client.entities

import java.sql.DriverManager
import com.example.kalonkotlin.client.connection
import com.example.kalonkotlin.client.status
import com.example.kalonkotlin.client.url
import com.example.kalonkotlin.client.user
import com.example.kalonkotlin.client.pass
import java.sql.ResultSet
import java.util.*

data class Professor(
    var id: Int,
    var firstname: String,
    var lastname: String,
    var secondname: String,
    var siteId: UUID,
    var lessons: Set<Lesson> = TreeSet()) {
    fun getFullName(): String {
        return "$lastname $firstname $secondname"
    }

    fun addLesson(lsn: Lesson) {
        if (!this.lessons.contains(lsn)) {
            this.lessons += lsn
            lsn.addProfessor(this)
        }
    }

    companion object {
        fun professorRequest(query: String?): List<Professor> {
            val professors: MutableList<Professor> = ArrayList()
            val thread = Thread {
                try {
                    Class.forName("org.postgresql.Driver")
                    connection = DriverManager.getConnection(
                        url,
                        user,
                        pass
                    )
                    val resultSet: ResultSet =
                        connection.createStatement().executeQuery(query)
                    while (resultSet.next()) {
                        val newProfessor = Professor(
                            resultSet.getInt("professor_id"),
                            resultSet.getString("professor_firstname"),
                            resultSet.getString("professor_lastname"),
                            resultSet.getString("professor_secondname"),
                            UUID.fromString(resultSet.getString("professor_siteid"))
                        )
                        newProfessor.id = resultSet.getInt("professor_id")
                        newProfessor.firstname = resultSet.getString("professor_firstname")
                        newProfessor.lastname = resultSet.getString("professor_lastname")
                        newProfessor.secondname = resultSet.getString("professor_secondname")
                        newProfessor.siteId = UUID.fromString(resultSet.getString("professor_siteid"))
                        professors.add(newProfessor)
                    }
                    status = true
                    println(professors.size)
                    println("connection status:$status")
                } catch (e: Exception) {
                    e.printStackTrace()
                    status = false
                }
            }
            thread.start()
            try {
                println("вошли в препода")
                thread.join()
            } catch (e: InterruptedException) {
                e.printStackTrace()
                status = false
            }
            return professors
        }


    }
}
