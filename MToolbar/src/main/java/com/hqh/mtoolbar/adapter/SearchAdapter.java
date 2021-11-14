package com.hqh.mtoolbar.adapter;

import android.content.Context;
import android.view.View;
import com.hqh.mtoolbar.R;
import com.hqh.mtoolbar.bean.Bean;
import com.hqh.mtoolbar.util.CommonAdapter;
import com.hqh.mtoolbar.util.ViewHolder;


import java.util.List;


public class SearchAdapter extends CommonAdapter<Bean> {

    public SearchAdapter(Context context, List<Bean> data, int layoutId) {
        super(context, data, layoutId);
    }

    @Override
    public void convert(ViewHolder holder, int position) {
        holder.setImageResource(R.id.item_search_iv_icon,mData.get(position).getIconId())
                .setText(R.id.item_search_tv_title,mData.get(position).getTitle())
                .setText(R.id.item_search_tv_content,mData.get(position).getContent())
                .setText(R.id.item_search_tv_comments,mData.get(position).getComments());
    }
}
