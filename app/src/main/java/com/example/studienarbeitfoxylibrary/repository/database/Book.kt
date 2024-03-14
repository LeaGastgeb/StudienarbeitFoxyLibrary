package com.example.studienarbeitfoxylibrary.repository.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Book(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val isbn: String,
    val title: String,
    val author: String,
    val publisher: String,
    val publicationDate: String,
    val pageCount: String,
    val price: String,
    val genre: String,
    val language: String,
    val signature: String,
    val borrowed: String,
    val borrowedTo: String,
    val borrowedOn: String,
    val borrowedUntil: String,
    val rating: String,
    val comment: String,
    val cover: String
)
