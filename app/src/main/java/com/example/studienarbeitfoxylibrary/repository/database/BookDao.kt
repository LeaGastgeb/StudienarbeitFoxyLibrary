package com.example.studienarbeitfoxylibrary.repository.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface BookDao {
    @Insert
    fun insert(book: Book)
    @Delete
    fun delete(book: Book)
    @Update
    fun update(book: Book)
    @Query("SELECT * FROM Book WHERE id = :bookId")
    fun getBookById(bookId: Long): Book
    @Query("SELECT * FROM Book")
    fun getBookList(): List<Book>
    @Query("SELECT * FROM Book")
    fun getLiveDataBookList(): LiveData<List<Book>>

    @Query("SELECT * FROM Book WHERE isbn = :isbn")
    fun getBookByIsbn(isbn: String): Book

    @Query("SELECT * FROM Book WHERE title = :title")
    fun getBookByTitle(title: String): List<Book>

    @Query("SELECT * FROM Book WHERE author = :author")
    fun getBookByAuthor(author: String): List<Book>

    @Query("SELECT * FROM Book WHERE publisher = :publisher")
    fun getBookByPublisher(publisher: String): List<Book>




}