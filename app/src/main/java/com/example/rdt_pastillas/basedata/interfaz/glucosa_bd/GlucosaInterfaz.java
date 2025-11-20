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

    // En lugar de traer todo, ahora filtra por un patrón de fecha (ej: "2025/11%")
    @Query("SELECT * FROM GlucosaEntity WHERE fecha_hora_creacion LIKE :filtroFecha || '%' ORDER BY fecha_hora_creacion ASC")
    LiveData<List<GlucosaEntity>> getGlucosaFiltradaPorMes(String filtroFecha);

    @Query("UPDATE GlucosaEntity SET estado = 1 WHERE id_glucosa = :id")
    void actualizarEstado(long id);

}
