package com.example.kalonkotlin.client.entities

data class Student(
    var id: Int,
    var firstname: String,
    var lastname: String,
    var secondname: String,
    var group: Group,
    var isHeadmen: Boolean
) {
}