package com.developerhaoz.androidnetwork.mvptest;

/**
 * @author Haoz
 * @date 2018/1/28.
 */
public interface BasePresenter<V extends BaseView> {

    public void attachView(V view);

    public void detachView();

}
