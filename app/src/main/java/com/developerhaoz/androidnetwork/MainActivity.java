package com.developerhaoz.androidnetwork;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;

import com.developerhaoz.androidnetwork.volley.Header;

public class MainActivity extends AppCompatActivity {

    private Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Header header;
        // ① 匿名线程持有 Activity 的引用，进行耗时操作
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // ② 使用匿名 Handler 发送耗时消息
        Message message = Message.obtain();
        mHandler.sendMessageDelayed(message, 6000);
    }

    static class MyAscnyTask extends AsyncTask<Void, Integer, String>{
        @Override
        protected String doInBackground(Void... params) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "";
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
