package com.example.kalonkotlin.client.enums

enum class LessonType(val value: String) {
    DEFAULT(""),
    LECTURE("Лекция"),
    PRACTICE("Практическое занятие"),
    LABORATORY("Лабораторная работа"),
    CONSULTATION("Консультация"),
    EXAM("Экзамен"),
    CREDIT("Зачет");


    fun getName(): String {
        return name;
    }
}