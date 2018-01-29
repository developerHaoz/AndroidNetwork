package com.developerhaoz.androidnetwork.mvptest;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.developerhaoz.androidnetwork.MyApplication;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Haoz
 * @date 2018/1/29.
 */
public class MvpPresenter implements BasePresenter<MvpView> {

    private MvpView mView;
    private List<MeiziBean> mMeiziBeanList;

    private static final String URL = "http://gank.io/api/data/%E7%A6%8F%E5%88%A9/20/20";

    public MvpPresenter(){
        mMeiziBeanList = new ArrayList<>();
    }

    @Override
    public void attachView(MvpView view) {
        this.mView = view;
    }

    public void startLoadMeizi(){
        RequestQueue requestQueue = Volley.newRequestQueue(MyApplication.getContext());
        StringRequest stringRequest = new StringRequest(URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    String json = new JSONObject(response).getString("results");
                    Gson gson = new Gson();
                    Type type = new TypeToken<List<MeiziBean>>(){}.getType();
                    List<MeiziBean> meiziList = gson.fromJson(json, type);
                    mView.showMeizi(meiziList);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        requestQueue.add(stringRequest);
    }

    @Override
    public void detachView() {

    }
}
