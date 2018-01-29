package com.developerhaoz.androidnetwork;

import android.app.Application;
import android.content.Context;

/**
 * @author Haoz
 * @date 2018/1/29.
 */
public class MyApplication extends Application {

    private static MyApplication mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    public static Context getContext(){
        return mContext;
    }

}
