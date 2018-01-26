package com.developerhaoz.androidnetwork;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class MainActivity extends AppCompatActivity {

    private Button mBtnTest;
    private TextView mTvShowInfo;

    private static final String URL = "https://www.baidu.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final RequestQueue requestQueue = Volley.newRequestQueue(this);

        mTvShowInfo = (TextView) findViewById(R.id.main_tv_show_info);

        StringRequest stringRequest = new StringRequest(URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                mTvShowInfo.setText(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mTvShowInfo.setText("it doesn't work");
            }
        });
        requestQueue.add(stringRequest);
    }

    public class Test{
        int a1 = 0;
        Test mTest1 = new Test();

        public void fun(){
            int a2 = 0;
            Test test2 = new Test();
        }
    }

    Test mTest3 = new Test();













}
