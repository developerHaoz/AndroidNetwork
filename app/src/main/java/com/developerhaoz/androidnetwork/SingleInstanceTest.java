package com.developerhaoz.androidnetwork;

import android.content.Context;

/**
 * @author Haoz
 * @date 2018/1/27.
 */
public class SingleInstanceTest {

    private static SingleInstanceTest sInstance;
    private Context mContext;

    private SingleInstanceTest(Context context){
        this.mContext = context.getApplicationContext();
    }

    public static SingleInstanceTest newInstance(Context context){
        if(sInstance == null){
            sInstance = new SingleInstanceTest(context);
        }
        return sInstance;
    }
}
