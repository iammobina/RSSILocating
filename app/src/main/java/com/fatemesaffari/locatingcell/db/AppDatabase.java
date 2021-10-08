package com.fatemesaffari.locatingcell.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.fatemesaffari.locatingcell.model.Parameters;

import java.lang.reflect.Parameter;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Parameters.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ApplicationDao globalDao();

    private static volatile AppDatabase parameterInstance;
    private static final Integer NUMBER_SIZE_THREAD = 3;
    public static ExecutorService databaseExcuter= Executors.newFixedThreadPool(NUMBER_SIZE_THREAD);


    public static AppDatabase getDatabase(final Context context) {
        if (parameterInstance == null) {
            synchronized ((AppDatabase.class)) {
                if (parameterInstance == null) {
                    parameterInstance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "parameter_database")
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return parameterInstance;
    }
}