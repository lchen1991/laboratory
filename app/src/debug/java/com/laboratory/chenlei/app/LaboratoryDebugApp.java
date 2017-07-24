package com.laboratory.chenlei.app;

import com.facebook.stetho.Stetho;

/**
 * Created by chenlei on 17/7/25.
 */

public class LaboratoryDebugApp extends LaboratoryApp{

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}
