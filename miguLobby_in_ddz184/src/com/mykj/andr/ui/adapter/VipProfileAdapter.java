package com.mykj.andr.ui.adapter; 

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * 
 * @ClassName: VipProfileAdapter
 * @Description: Vip Profile adapter
 * @author  
 * @date 2013-6-8 上午11:06:43
 *
 */
public class VipProfileAdapter extends ArrayListAdapter<String> {

	private Activity act; 
	public VipProfileAdapter(Context context) {
		super(context);
		this.act =(Activity)context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ViewHolder holder;
		if (row == null) {
			LayoutInflater inflater = act.getLayoutInflater();
			row = inflater.inflate(android.R.layout.simple_list_item_1, null);
			holder = new ViewHolder();

			holder.tvProfileDetail = (TextView) row.findViewById(android.R.id.text1); 
			row.setTag(holder);
		} else {
			holder = (ViewHolder) row.getTag();
		}
		String info = mList.get(position);
		holder.tvProfileDetail.setText((position+1)+info);
		return row;
	}

	private class ViewHolder {
		TextView tvProfileDetail;
	}
	
}
