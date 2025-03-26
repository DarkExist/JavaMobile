package com.example.readapplication.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.readapplication.data.Book;

import java.util.List;

@Dao
public interface BookDao {
    @Insert
    void insert(Book book);
    @Insert
    void insertAll(List<Book> books);

    @Update
    void update(Book book);

    @Delete
    void delete(Book book);

    @Query("SELECT * FROM books")
    List<Book> getAllBooks();

    @Query("SELECT * FROM books WHERE title LIKE '%' || :query || '%'")
    List<Book> searchBooks(String query);

    @Query("SELECT * FROM books WHERE isBookmarked = 1")
    List<Book> getBookmarkedBooks();

    @Query("SELECT * FROM books WHERE filename = :fileName")
    Book getBookByFileName(String fileName);

    @Query("SELECT * FROM books WHERE fb2Path = :path LIMIT 1")
    Book getBookByPath(String path);

    @Query("SELECT * FROM books WHERE isBookmarked = 1 AND (title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%')")
    List<Book> searchBookmarkedBooks(String query);
}