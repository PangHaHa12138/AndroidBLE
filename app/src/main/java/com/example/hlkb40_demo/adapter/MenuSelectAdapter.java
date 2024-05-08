package com.example.hlkb40_demo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.hlkb40_demo.R;

import java.util.List;

public class MenuSelectAdapter extends BaseAdapter {
	
	List<String> list ;
	String strName;
	Context con;
	ViewHolder holder;
	int number = 0;
	public MenuSelectAdapter(List<String> li, Context con) {
		this.list = li;
		this.con = con;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(con).inflate(R.layout.item_menu_select, null);
			holder = new ViewHolder();
			holder.m_tvName = (TextView) convertView.findViewById(R.id.tvName);
			convertView.setTag(holder);
		}
		else {
			holder = (ViewHolder) convertView.getTag();
		}
		strName = list.get(position);
		holder.m_tvName.setText(strName);
		
		return convertView;
	}
	
	
	
	private class ViewHolder {
		private TextView m_tvName;
		
	} 

}
