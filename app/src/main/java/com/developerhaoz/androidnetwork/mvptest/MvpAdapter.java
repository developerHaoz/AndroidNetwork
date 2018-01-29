package com.developerhaoz.androidnetwork.mvptest;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.developerhaoz.androidnetwork.R;

import java.util.List;

/**
 * @author Haoz
 * @date 2018/1/28.
 */
public class MvpAdapter extends RecyclerView.Adapter<MvpAdapter.MvpViewHolder> {

    private List<MeiziBean> mMeiziBeanList;

    public MvpAdapter(List<MeiziBean> meiziBeanList){
        this.mMeiziBeanList = meiziBeanList;
    }

    @Override
    public MvpViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rv_mvp, parent,false);
        return new MvpViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MvpViewHolder holder, int position) {
        holder.mTvMvp.setText(mMeiziBeanList.get(position).getImageUrl());
    }

    @Override
    public int getItemCount() {
        if(mMeiziBeanList != null){
            return mMeiziBeanList.size();
        }

        return 0;
    }

    static class MvpViewHolder extends RecyclerView.ViewHolder {

        private TextView mTvMvp;

        public MvpViewHolder(View itemView) {
            super(itemView);
            mTvMvp = (TextView) itemView.findViewById(R.id.item_tv_info);
        }
    }
}
