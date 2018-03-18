package com.developerhaoz.androidnetwork.windowtest;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.developerhaoz.androidnetwork.R;

public class WindowActivity extends AppCompatActivity {

    private Button mBtnAddWindow;
    private Button mBtnFloat;
    private WindowManager.LayoutParams mLayoutParams;
    private WindowManager mWindowManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_window);
        mBtnAddWindow = (Button) findViewById(R.id.window_btn_add_window);
        mWindowManager = getWindowManager();
        mBtnAddWindow.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int x = (int) event.getRawX();
                int y = (int) event.getRawY();
                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        0, 0,
                        PixelFormat.TRANSPARENT
                );
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        layoutParams.x = x;
                        layoutParams.y = y;
                        mWindowManager.updateViewLayout(mBtnAddWindow, layoutParams);
                        break;
                    default:break;

                }
                return false;
            }
        });
//        mBtnAddWindow.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mBtnFloat = new Button(WindowActivity.this.getApplicationContext());
//                mBtnAddWindow.setText("button");
//                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
//                        WindowManager.LayoutParams.WRAP_CONTENT,
//                        WindowManager.LayoutParams.WRAP_CONTENT,
//                        0, 0,
//                        PixelFormat.TRANSPARENT
//                );
//                // flag 设置 Window 属性
//                layoutParams.flags= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
//                // type 设置 Window 类别（层级）
//                layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
//                layoutParams.gravity = Gravity.TOP;
//                mWindowManager.addView(mBtnFloat, layoutParams);
//            }
//        });
    }
}
