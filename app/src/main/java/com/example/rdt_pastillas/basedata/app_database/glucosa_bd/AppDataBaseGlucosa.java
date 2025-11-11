package com.example.rdt_pastillas.basedata.app_database.glucosa_bd;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.rdt_pastillas.basedata.entity.glucosa_bd.glucosa_entity.GlucosaEntity;
import com.example.rdt_pastillas.basedata.interfaz.glucosa_bd.GlucosaInterfaz;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {GlucosaEntity.class}, version = 1, exportSchema = false)
public abstract class AppDataBaseGlucosa extends RoomDatabase {
    public abstract GlucosaInterfaz glucosa_interfaz();

    private static volatile AppDataBaseGlucosa INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;

    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDataBaseGlucosa getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDataBaseGlucosa.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDataBaseGlucosa.class, "Glucosabd")
                            // Si no hay migraciones y la versión cambia, recrea la BD
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}