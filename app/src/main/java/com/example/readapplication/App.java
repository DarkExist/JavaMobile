package com.example.readapplication;

import android.app.Application;
import android.util.Log;

import com.yandex.mapkit.MapKitFactory;

import java.io.File;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MapKitFactory.setApiKey(BuildConfig.MAPKIT_API_KEY);
        MapKitFactory.initialize(this);


        createAppDirs();
    }

    private void createAppDirs() {
        File rootDir = new File(getApplicationInfo().dataDir);
        String[] dirs = {
                "database",
                "books/fb2",
                "books/images"
        };

        for (String dir : dirs) {
            File folder = new File(rootDir, dir);
            if (!folder.exists() && !folder.mkdirs()) {
                Log.e("APP", "Failed to create dir: " + folder.getAbsolutePath());
            }
        }
    }
}