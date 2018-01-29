package com.developerhaoz.androidnetwork.mvptest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.developerhaoz.androidnetwork.R;

import java.util.List;

public class MvpActivity extends AppCompatActivity implements MvpView{

    private RecyclerView mRv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mvp);
        mRv = (RecyclerView) findViewById(R.id.mvp_rv);
        MvpPresenter presenter = new MvpPresenter();
        presenter.attachView(this);
        presenter.startLoadMeizi();
    }

    @Override
    public void showMeizi(List<MeiziBean> meiziBeanList) {
        mRv.setLayoutManager(new LinearLayoutManager(MvpActivity.this));
        mRv.setAdapter(new MvpAdapter(meiziBeanList));
    }
}
