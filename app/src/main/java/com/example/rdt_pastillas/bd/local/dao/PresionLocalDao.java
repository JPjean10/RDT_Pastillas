package com.example.rdt_pastillas.bd.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.rdt_pastillas.Modelo.ModeloBD.entity.ControlBD.presion_entity.PresionEntity;

import java.util.List;

@Dao
public interface PresionLocalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertPresion(PresionEntity presion);

    @Query("UPDATE PresionEntity SET estado = 1 WHERE id_presion = :id")
    void actualizarEstado(long id);

    @Query("SELECT * FROM PresionEntity WHERE fecha_hora_creacion LIKE :filtroFecha || '%' ORDER BY fecha_hora_creacion DESC")
    LiveData<List<PresionEntity>> getPresionFiltradaPorMes(String filtroFecha);

    @Update
    void editPresion(PresionEntity presion);

    /** 3. Obtiene todos los registros que NO han sido sincronizados con el servidor. */
    @Query("SELECT * FROM PresionEntity WHERE estado = 0")
    List<PresionEntity> getRegistrosNoSincronizados();

    /** 1. Cuenta todos los registros en la tabla. */
    @Query("SELECT COUNT(*) FROM PresionEntity")
    int countPresion();

    /** 2. Inserta una lista completa de registros. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<PresionEntity> presiones);
}
