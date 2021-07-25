package com.walkfure.chat;

import android.app.Application;
import android.util.Log;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;

/**
 * @Description: java类作用描述
 * @CreateDate: 2020/7/15 10:54
 * @Author: Fiora
 */
public class MyApplication extends Application {
    private static MyApplication app;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        try {
            SDKInitializer.initialize(this);
            SDKInitializer.setCoordType(CoordType.GCJ02);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
        }
    }

    public static MyApplication getInstance() {
        return app;
    }
}
