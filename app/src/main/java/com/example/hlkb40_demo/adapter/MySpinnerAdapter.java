package com.example.hlkb40_demo.adapter;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.example.hlkb40_demo.R;
import com.example.hlkb40_demo.UUIDInfo;

import java.util.ArrayList;

public class MySpinnerAdapter implements SpinnerAdapter {

    private ArrayList<UUIDInfo> list;
    private Context context;
    private boolean isServer;

    public MySpinnerAdapter(ArrayList<UUIDInfo> list, Context context,boolean isServer) {
        this.list = list;
        this.context = context;
        this.isServer = isServer;
    }

    public void updateList(ArrayList<UUIDInfo> list) {
        this.list = list;
        notifyAll();
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView== null){
            holder= new ViewHolder();
            convertView= LayoutInflater.from(context).inflate(R.layout.spinner_item,null);
            holder.tvUUID= convertView.findViewById(R.id.tvUUID);
            holder.tvChara= convertView.findViewById(R.id.tvChara);
            convertView.setTag(holder);
        }else {
            holder= (ViewHolder) convertView.getTag();
        }

        holder.tvUUID.setText(list.get(position).getUUIDString());
        holder.tvChara.setText(list.get(position).getStrCharactInfo());
//        if (isServer) {
//            holder.tvChara.setVisibility(View.GONE);
//        }
        return convertView;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView== null){
            holder= new ViewHolder();
            convertView= LayoutInflater.from(context).inflate(R.layout.spinner_item,null);
            holder.tvUUID= convertView.findViewById(R.id.tvUUID);
            holder.tvChara= convertView.findViewById(R.id.tvChara);
            convertView.setTag(holder);
        }else {
            holder= (ViewHolder) convertView.getTag();
        }

        holder.tvUUID.setText(list.get(position).getUUIDString());
        holder.tvChara.setText(list.get(position).getStrCharactInfo());
        if (isServer) {
            holder.tvChara.setVisibility(View.GONE);
        }
        return convertView;
    }

    private class ViewHolder{
        TextView tvUUID;
        TextView tvChara;
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

}
