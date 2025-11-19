package com.example.rdt_pastillas.basedata.interfaz.glucosa_bd;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.rdt_pastillas.basedata.entity.glucosa_bd.glucosa_entity.GlucosaEntity;

import java.util.List;

@Dao
public interface GlucosaInterfaz {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertGlucosa(GlucosaEntity glucosa);

    @Query("SELECT * FROM GlucosaEntity ORDER BY fecha_hora_creacion DESC")
    LiveData<List<GlucosaEntity>> getAllGlucosaOrderByDate();

    @Query("UPDATE GlucosaEntity SET estado = 1 WHERE id_glucosa = :id")
    void actualizarEstado(long id);

}
