package com.example.kalonkotlin.client


import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.kalonkotlin.client.entities.Group
import com.example.kalonkotlin.client.entities.Professor
import com.example.kalonkotlin.client.entities.Schedule
import java.text.SimpleDateFormat
import java.util.LinkedList

class Database(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "my_database"
        private const val DATABASE_VERSION = 1
        private const val PROFESSOR_TABLE_NAME = "professor_lessons"
        private const val STUDENT_TABLE_NAME = "student_lessons"
        private const val COLUMN_ID = "id"
        const val COLUMN_GROUP_NAME = "group_name"
        const val COLUMN_DAY = "lesson_day"
        const val COLUMN_LESSON_NAME = "lesson_name"
        const val COLUMN_PROFESSOR = "professor"
        const val COLUMN_LESSON_NUMBER = "lesson_number"
        const val COLUMN_LESSON_TYPE = "lesson_type"
        const val COLUMN_ROOMS = "rooms"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = "CREATE TABLE IF NOT EXISTS $PROFESSOR_TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY, $COLUMN_GROUP_NAME TEXT, $COLUMN_DAY DATE, $COLUMN_LESSON_NAME TEXT, $COLUMN_PROFESSOR TEXT, $COLUMN_LESSON_NUMBER INTEGER, $COLUMN_LESSON_TYPE INTEGER, $COLUMN_ROOMS TEXT)"
        db.execSQL(createTable)
        val createStudentTable = "CREATE TABLE IF NOT EXISTS $STUDENT_TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY, $COLUMN_GROUP_NAME TEXT, $COLUMN_DAY DATE, $COLUMN_LESSON_NAME TEXT, $COLUMN_PROFESSOR TEXT, $COLUMN_LESSON_NUMBER INTEGER, $COLUMN_LESSON_TYPE INTEGER, $COLUMN_ROOMS TEXT)"
        db.execSQL(createStudentTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        val dropTable = "DROP TABLE IF EXISTS $PROFESSOR_TABLE_NAME"
        db.execSQL(dropTable)
        onCreate(db)
    }

    fun insertProfessorData(clientProfessor: Professor) {
        val allSchedule: List<Schedule> = Schedule.scheduleRequest(
            "SELECT group_name, lesson_day, lesson_name," +
                    "professor_lastname, professor_firstname, professor_secondname," +
                    " lesson_number, lesson_type, rooms FROM public.groups AS g" +
                    " INNER JOIN public.lessons_groups AS lg ON lg.group_id=g.group_id" +
                    " INNER JOIN public.lessons AS l ON l.lesson_id = lg.lesson_id" +
                    " INNER JOIN public.lesson_rooms AS lr ON lr.room_id = l.lesson_id" +
                    " INNER JOIN public.lessons_professors AS lp ON lp.lesson_id = l.lesson_id" +
                    " INNER JOIN public.professors AS p ON p.professor_id = lp.professor_id" +
                    " WHERE p.professor_lastname = '" + clientProfessor.lastname + "'" +
                    " ORDER BY lesson_day"
        )
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        for (sc in allSchedule) {
            // Insert data into the database
            val values = ContentValues()
            values.put(COLUMN_GROUP_NAME, sc.group_name)
            values.put(COLUMN_DAY, sdf.format(sc.lesson_date))
            values.put(COLUMN_LESSON_NAME, sc.name)
            values.put(COLUMN_PROFESSOR, sc.professor)
            values.put(COLUMN_LESSON_NUMBER, sc.lesson_number)
            values.put(COLUMN_LESSON_TYPE, sc.lesson_type)
            values.put(COLUMN_ROOMS, sc.room)
            writableDatabase.insert(PROFESSOR_TABLE_NAME, null, values)
        }

    }

    fun insertStudentData(clientGroup: Group) {
        val allSchedule: List<Schedule> = Schedule.scheduleRequest("SELECT group_name, lesson_day, lesson_name,"
                + " professor_lastname, professor_firstname, professor_secondname,"
                + "lesson_number, lesson_type, rooms FROM public.groups AS g"
                + " INNER JOIN public.lessons_groups AS lg ON lg.group_id=g.group_id"
                + " INNER JOIN public.lessons AS l ON l.lesson_id = lg.lesson_id"
                + " INNER JOIN public.lesson_rooms AS lr ON lr.room_id = l.lesson_id"
                + " INNER JOIN public.lessons_professors AS lp ON lp.lesson_id = l.lesson_id"
                + " INNER JOIN public.professors AS p ON p.professor_id = lp.professor_id"
                + " WHERE g.group_name='" + clientGroup.name + "'"
                + " ORDER BY lesson_day")
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        for (sc in allSchedule) {
            // Insert data into the database
            val values = ContentValues()
            values.put(COLUMN_GROUP_NAME, sc.group_name)
            values.put(COLUMN_DAY, sdf.format(sc.lesson_date))
            values.put(COLUMN_LESSON_NAME, sc.name)
            values.put(COLUMN_PROFESSOR, sc.professor)
            values.put(COLUMN_LESSON_NUMBER, sc.lesson_number)
            values.put(COLUMN_LESSON_TYPE, sc.lesson_type)
            values.put(COLUMN_ROOMS, sc.room)
            writableDatabase.insert(STUDENT_TABLE_NAME, null, values)
        }

    }

    @SuppressLint("Range", "Recycle")
    fun getProfessorSchedule(): List<Schedule> {
        val allSchedule: MutableList<Schedule> = LinkedList()
        val cursor = readableDatabase.rawQuery("SELECT * FROM $PROFESSOR_TABLE_NAME", null)
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        if (cursor.moveToFirst()) {
            do {
                val oneDay = Schedule(cursor.getString(cursor.getColumnIndex(COLUMN_GROUP_NAME)), sdf.parse(cursor.getString(cursor.getColumnIndex(COLUMN_DAY)))!!,
                    cursor.getString(cursor.getColumnIndex(COLUMN_LESSON_NAME)), cursor.getString(cursor.getColumnIndex(COLUMN_PROFESSOR)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_ROOMS)), Integer.valueOf(cursor.getString(cursor.getColumnIndex(COLUMN_LESSON_NUMBER))),
                    Integer.valueOf(cursor.getString(cursor.getColumnIndex(COLUMN_LESSON_TYPE))))
                allSchedule += oneDay
            } while (cursor.moveToNext())
        }
        return allSchedule
    }

    @SuppressLint("Range", "Recycle")
    fun getStudentSchedule(): List<Schedule> {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val allSchedule: MutableList<Schedule> = LinkedList()
        val cursor = readableDatabase.rawQuery("SELECT * FROM $STUDENT_TABLE_NAME", null)
        if (cursor.moveToFirst()) {
            do {
                val oneDay = Schedule(cursor.getString(cursor.getColumnIndex(COLUMN_GROUP_NAME)), sdf.parse(cursor.getString(cursor.getColumnIndex(COLUMN_DAY)))!!,
                    cursor.getString(cursor.getColumnIndex(COLUMN_LESSON_NAME)), cursor.getString(cursor.getColumnIndex(COLUMN_PROFESSOR)),
                    cursor.getString(cursor.getColumnIndex(COLUMN_ROOMS)), Integer.valueOf(cursor.getString(cursor.getColumnIndex(COLUMN_LESSON_NUMBER))),
                    Integer.valueOf(cursor.getString(cursor.getColumnIndex(COLUMN_LESSON_TYPE))))
                allSchedule += oneDay
            } while (cursor.moveToNext())
        }
        return allSchedule
    }

    fun deleteProfessorData() {
        //return writableDatabase.delete(PROFESSOR_TABLE_NAME, "$COLUMN_ID=?", arrayOf(id.toString()))
        return writableDatabase.execSQL("DELETE FROM $PROFESSOR_TABLE_NAME")
    }

    fun deleteStudentData() {
        //return writableDatabase.delete(STUDENT_TABLE_NAME, "$COLUMN_ID=?", arrayOf(id.toString()))
        return writableDatabase.execSQL("DELETE FROM $STUDENT_TABLE_NAME")
    }
}

//TODO Datetimeformatter