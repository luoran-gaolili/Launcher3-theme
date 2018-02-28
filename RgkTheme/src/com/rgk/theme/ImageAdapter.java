package com.rgk.theme;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class ImageAdapter extends BaseAdapter {

    private List<?> mSource;
    private SourceUtil mUtil;
    private Context mContext;
    private LayoutInflater mInflater;

    public interface SourceUtil{
        View getView(LayoutInflater inflater,int arg0, View arg1, ViewGroup arg2);
    }

    public ImageAdapter(Context context,List<?> source){
        mContext = context;
        mSource = source;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setSourceUtil(SourceUtil util){
        mUtil = util;
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mSource.size();
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return mSource.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return arg0;
    }

    @Override
    public View getView(int arg0, View arg1, ViewGroup arg2) {
        // TODO Auto-generated method stub
        View iv;
        //if(arg1==null){
            iv = mUtil.getView(mInflater,arg0, arg1, arg2);
        //}else{
        //    iv =  arg1;
        //}
        return iv;
    }

    public void destroy() {
        // TODO Auto-generated method stub
        mSource=null;
    }
    public List<?> getmSource() {
        return mSource;
    }

    public void setmSource(List<?> mSource) {
        this.mSource = mSource;
    }
}
