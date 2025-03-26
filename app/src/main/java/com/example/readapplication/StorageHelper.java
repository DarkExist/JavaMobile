package com.example.readapplication;

import android.content.Context;

import java.io.File;

public class StorageHelper {
    public static File getBookDir(Context context) {
        return new File(context.getApplicationInfo().dataDir, "books/fb2");
    }

    public static File getImageDir(Context context) {
        return new File(context.getApplicationInfo().dataDir, "books/images");
    }

    public static File getDatabaseDir(Context context) {
        return new File(context.getApplicationInfo().dataDir, "database");
    }
}
