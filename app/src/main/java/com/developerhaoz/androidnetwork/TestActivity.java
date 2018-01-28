package com.developerhaoz.androidnetwork;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class TestActivity extends AppCompatActivity {

    private static final String TAG = "TestActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        int capacity = (int)(Runtime.getRuntime().totalMemory() / 1024);
        Log.d(TAG, "onCreate: " + capacity / 8);

    }
}
