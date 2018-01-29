package com.developerhaoz.androidnetwork.mvptest;

import java.util.List;

/**
 * @author Haoz
 * @date 2018/1/29.
 */
public interface MvpView extends BaseView {

    void showMeizi(List<MeiziBean> meiziBeanList);
}
