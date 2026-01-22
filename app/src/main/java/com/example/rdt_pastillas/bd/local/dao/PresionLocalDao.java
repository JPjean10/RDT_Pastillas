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

    @Query("SELECT * FROM PresionEntity WHERE id_usuario = :idUsuario AND fecha_hora_creacion LIKE :filtroFecha || '%' ORDER BY fecha_hora_creacion DESC")
    LiveData<List<PresionEntity>> getPresionFiltradaPorMes(String filtroFecha, long idUsuario);

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

    // 1. Obtener el total de registros para calcular la paginación
    @Query("SELECT COUNT(*) FROM PresionEntity WHERE id_usuario = :idUsuario")
    int getTotalRegistros(long idUsuario);
    @Query("SELECT * FROM (SELECT * FROM PresionEntity WHERE id_usuario = :idUsuario ORDER BY fecha_hora_creacion DESC LIMIT :limit OFFSET :offset) ORDER BY fecha_hora_creacion ASC")
    List<PresionEntity> getPresionPaginadaGraficosBase(long idUsuario, int limit, int offset);

    @Query("SELECT MIN(fecha_hora_creacion) FROM PresionEntity WHERE id_usuario = :userId")
    String getFechaMinima(long userId);

    @Query("SELECT MAX(fecha_hora_creacion) FROM PresionEntity WHERE id_usuario = :userId")
    String getFechaMaxima(long userId);

    @Query("SELECT * FROM PresionEntity WHERE id_usuario = :userId " +
            "AND fecha_hora_creacion >= :inicio " +
            "AND fecha_hora_creacion <= :fin " +
            "ORDER BY fecha_hora_creacion ASC")
    List<PresionEntity> obtenerPorRango(long userId, String inicio, String fin);
}