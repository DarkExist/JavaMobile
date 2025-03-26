package com.example.readapplication.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity(tableName = "books")
public class Book {
    @PrimaryKey @NonNull
    private String id = UUID.randomUUID().toString();
    private String title;
    private String author;
    private String imagePath;// Путь относительно getImageDir()
    private String fb2Path;// Путь относительно getBookDir()
    private String filename;
    private boolean isBookmarked;


    public Book(String title, String author, String imagePath, String fb2Path, String filename, boolean isBookmarked) {
        this.title = title;
        this.author = author;
        this.imagePath = imagePath;
        this.fb2Path = fb2Path;
        this.filename = filename;
        this.isBookmarked = isBookmarked;
    }
    public String getId() {return id;}
    public void setId(String id) {this.id = id;}
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getImagePath() { return imagePath; }
    public String getFb2Path() { return fb2Path; }
    public String getFilename() { return filename; }

    public boolean isBookmarked() { return isBookmarked; }

    public void setBookmarked(boolean bookmarked) {
        isBookmarked = bookmarked;
    }

}