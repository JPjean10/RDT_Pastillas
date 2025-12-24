package com.example.rdt_pastillas.bd.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.rdt_pastillas.Modelo.ModeloBD.entity.ControlBD.presion_entity.PresionEntity;

@Dao
public interface PresionLocalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertPresion(PresionEntity presion);

    @Query("UPDATE PresionEntity SET estado = 1 WHERE id_presion = :id")
    void actualizarEstado(long id);
}
