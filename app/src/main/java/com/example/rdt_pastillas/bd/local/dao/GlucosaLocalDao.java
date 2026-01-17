package com.example.rdt_pastillas.bd.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.rdt_pastillas.Modelo.ModeloBD.entity.ControlBD.glucosa_entity.GlucosaEntity;

import java.util.List;

@Dao
public interface GlucosaLocalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertGlucosa(GlucosaEntity glucosa);

    // En lugar de traer todo, ahora filtra por un patrón de fecha (ej: "2025/11%")
    @Query("SELECT * FROM GlucosaEntity WHERE id_usuario = :idUsuario AND fecha_hora_creacion LIKE :filtroFecha || '%' ORDER BY fecha_hora_creacion DESC")
    LiveData<List<GlucosaEntity>> getGlucosaFiltradaPorMes(long idUsuario, String filtroFecha);

    @Query("UPDATE GlucosaEntity SET estado = 1 WHERE id_glucosa = :id")
    void actualizarEstado(long id);

    @Update
    void editGlucosa(GlucosaEntity glucosa);


    /** 1. Cuenta todos los registros en la tabla. */
    @Query("SELECT COUNT(*) FROM GlucosaEntity")
    int countGlucosa();

    /** 2. Inserta una lista completa de registros. Usado para poblar desde el TXT. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<GlucosaEntity> glucosas);

    /** 3. Obtiene todos los registros que NO han sido sincronizados con el servidor. */
    @Query("SELECT * FROM GlucosaEntity WHERE estado = 0")
    List<GlucosaEntity> getRegistrosNoSincronizados();

    @Query("SELECT * FROM (SELECT * FROM GlucosaEntity WHERE id_usuario = :idUsuario ORDER BY fecha_hora_creacion DESC LIMIT :limit OFFSET :offset) ORDER BY fecha_hora_creacion ASC")
    List<GlucosaEntity> getGlucosaPaginadaGraficos(long idUsuario,int limit,int offset);
    @Query("SELECT COUNT(*) FROM GlucosaEntity WHERE id_usuario = :idUsuario")
    int getTotalRegistros(long idUsuario);


}
