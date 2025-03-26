package com.example.readapplication.Databases;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.readapplication.DAO.BookDao;
import com.example.readapplication.StorageHelper;
import com.example.readapplication.data.Book;

import java.io.File;

@Database(entities = {Book.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract BookDao bookDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase create(Context context) {
        File dbFile = new File(StorageHelper.getDatabaseDir(context), "main.db");


        return Room.databaseBuilder(context, AppDatabase.class, dbFile.getAbsolutePath())
                .fallbackToDestructiveMigration()
                .build();
    }

    public static void destroyInstance() {
        if (INSTANCE != null && INSTANCE.isOpen()) {
            INSTANCE.close();
        }
        INSTANCE = null;
    }
}