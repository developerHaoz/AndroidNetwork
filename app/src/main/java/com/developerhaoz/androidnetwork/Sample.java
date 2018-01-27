package com.developerhaoz.androidnetwork;

import android.content.Context;

import java.lang.ref.WeakReference;

/**
 * @author Haoz
 * @date 2018/1/27.
 */
public class Sample {

    private WeakReference<Context> mWeakReference;

    public Sample(Context context){
        this.mWeakReference = new WeakReference<>(context);
    }

    public Context getContext() {
        if(mWeakReference.get() != null){
            return mWeakReference.get();
        }
        return null;
    }
}
