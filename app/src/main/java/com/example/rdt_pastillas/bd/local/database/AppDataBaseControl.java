package com.example.rdt_pastillas.bd.local.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.rdt_pastillas.Modelo.ModeloBD.entity.ControlBD.glucosa_entity.GlucosaEntity;
import com.example.rdt_pastillas.Modelo.ModeloBD.entity.ControlBD.usuario_entity.UsuarioEntity;
import com.example.rdt_pastillas.bd.local.dao.GlucosaLocalDao;
import com.example.rdt_pastillas.bd.local.dao.UsuarioLocalDao;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {GlucosaEntity.class, UsuarioEntity.class}, version = 2, exportSchema = false)
public abstract class AppDataBaseControl extends RoomDatabase {
    public abstract GlucosaLocalDao glucosa_interfaz();
    public abstract UsuarioLocalDao usuario_interfaz();

    private static volatile AppDataBaseControl INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;

    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDataBaseControl getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDataBaseControl.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDataBaseControl.class, "ControlBD")
                            // Si no hay migraciones y la versión cambia, recrea la BD
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
