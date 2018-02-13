package com.developerhaoz.androidnetwork;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayDeque;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        ArrayDeque stack = new ArrayDeque();
    }
}
