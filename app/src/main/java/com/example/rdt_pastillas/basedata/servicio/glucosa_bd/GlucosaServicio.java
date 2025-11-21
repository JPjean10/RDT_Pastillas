package com.example.rdt_pastillas.basedata.servicio.glucosa_bd;

import android.app.Application;

import com.example.rdt_pastillas.basedata.dao.glucosa_bd.GlucosaDao;
import com.example.rdt_pastillas.basedata.entity.glucosa_bd.glucosa_entity.GlucosaEntity;

public class GlucosaServicio {

    private GlucosaDao mRepository;

    public GlucosaServicio(Application application) {
        mRepository = new GlucosaDao(application);
    }

    public void insert(int nivel_glucosa) {
        mRepository.insert(nivel_glucosa);
    }

    public void edit(GlucosaEntity glucosa) {
        mRepository.edit(glucosa);
    }

}
