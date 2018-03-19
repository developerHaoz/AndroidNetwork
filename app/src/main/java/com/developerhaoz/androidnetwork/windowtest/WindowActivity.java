package com.developerhaoz.androidnetwork.windowtest;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.developerhaoz.androidnetwork.R;

public class WindowActivity extends AppCompatActivity implements View.OnTouchListener{

    private Button mBtnAddWindow;
    private Button mBtnFloat;
    private WindowManager.LayoutParams layoutParams;
    private WindowManager mWindowManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_window);
        mBtnAddWindow = (Button) findViewById(R.id.window_btn_add_window);
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        mBtnAddWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBtnFloat = new Button(WindowActivity.this);
                mBtnAddWindow.setText("button");
                layoutParams = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, 0, 0,
                        PixelFormat.TRANSPARENT);
                // flag 设置 Window 属性
                layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
                // type 设置 Window 类别（层级）
                layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
                layoutParams.gravity = Gravity.TOP;
                mBtnFloat.setOnTouchListener(WindowActivity.this);
                mWindowManager.addView(mBtnFloat, layoutParams);
            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                layoutParams.x = x;
                layoutParams.y = y;
                mWindowManager.updateViewLayout(mBtnFloat, layoutParams);
                break;
            default:break;

        }
        return true;
    }
}















