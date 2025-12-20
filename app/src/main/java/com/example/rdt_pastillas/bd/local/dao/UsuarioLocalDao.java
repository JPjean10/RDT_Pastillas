package com.example.rdt_pastillas.bd.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.rdt_pastillas.Modelo.ModeloBD.entity.ControlBD.usuario_entity.UsuarioEntity;

@Dao
public interface UsuarioLocalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertUsuario(UsuarioEntity usuario);

    @Query("SELECT * FROM UsuarioEntity WHERE usuario = :usuario AND contrasena = :contrasena LIMIT 1")
    UsuarioEntity findUsuarioByCredentials(String usuario, String contrasena);

    @Query("SELECT COUNT(*) FROM UsuarioEntity")
    int countUsers();
}
