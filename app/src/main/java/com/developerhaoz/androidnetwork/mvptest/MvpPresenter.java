package com.developerhaoz.androidnetwork.mvptest;

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

    }

    @Override
    public void detachView() {

    }
}
