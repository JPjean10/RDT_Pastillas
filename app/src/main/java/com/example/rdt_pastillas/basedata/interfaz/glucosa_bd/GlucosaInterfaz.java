package com.example.rdt_pastillas.basedata.interfaz.glucosa_bd;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;

import com.example.rdt_pastillas.basedata.entity.glucosa_bd.glucosa_entity.GlucosaEntity;

@Dao
public interface GlucosaInterfaz {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertGlucosa(GlucosaEntity glucosa);


}


/*

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertGasto(GlucosaEntity glucosa);

    @Query("SELECT * FROM GlucosaEntity ORDER BY id ASC")
    LiveData<List<GlucosaEntity>> getAllGlucosa();

    @Update
    void actulizarGasto(GlucosaEntity glucosa);

*/
