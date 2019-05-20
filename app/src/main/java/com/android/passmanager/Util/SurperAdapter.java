package com.android.passmanager.Util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.passmanager.Account;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public abstract class SurperAdapter<T> extends RecyclerView.Adapter<SurperAdapter.VH>{

    private ArrayList<T> mDatas;
    private Context mContext;

    public SurperAdapter(ArrayList <T> mDatas, Context context){
        this.mDatas=mDatas;
        this.mContext=context;
    }

    public abstract int getLayoutId(int viewType);

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup viewGroup , int i) {
        return VH.get(viewGroup,getLayoutId(i));
    }

    @Override
    public void onBindViewHolder(@NonNull SurperAdapter.VH vh , int i) {
        convert(vh, mDatas.get(i), i,mContext);
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public abstract void convert(VH holder, T data, int position,Context mContext);

    public static class VH extends RecyclerView.ViewHolder{
        private SparseArray<View> mViews;
        private View mConvertView;

        private VH(View v){
            super(v);
            mConvertView = v;
            mViews = new SparseArray <>();
        }

        public static VH get(ViewGroup parent, int layoutId){
            View convertView = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
            return new VH(convertView);
        }

        public <T extends View> T getView(int id){
            View v = mViews.get(id);
            if(v == null){
                v = mConvertView.findViewById(id);
                mViews.put(id, v);
            }
            return (T)v;
        }

        public void setText(int id, String value){
            TextView view = getView(id);
            view.setText(value);
        }
        public void gone(int id){
            TextView view = getView(id);
            view.setVisibility( View.GONE);
        }

    }

    public void add(T account,int position){
        mDatas.add(position,account );
        notifyItemInserted(position);
        if (position != mDatas.size()) {
            notifyItemRangeChanged(position, mDatas.size() - position);
        }
    }

    public void updata(T account,int position){
        mDatas.remove( position );
        mDatas.add(position,account );
        notifyItemChanged( position );
        if (position != mDatas.size()) {
            notifyItemRangeChanged(position, mDatas.size() - position);
        }
    }

    public void remove(int position){
        mDatas.remove( position );
        notifyItemRemoved( position );
        if (position != mDatas.size()) {
            notifyItemRangeChanged(position, mDatas.size() - position);
        }
    }


    public void setmDatas(ArrayList <T> mDatas) {
        this.mDatas = mDatas;
    }

    public ArrayList <T> getmDatas() {
        return mDatas;
    }
}
