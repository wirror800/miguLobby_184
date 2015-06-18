package com.mykj.andr.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.MyGame.Migu.R;

/**
 * 
 * @ClassName: VipMineAdapter
 * @Description: 我的VIP信息
 * @author  
 * @date 2013-6-9 下午02:50:42
 *
 */
public class VipMineAdapter extends ArrayListAdapter<String> {

	
	private Activity act; 
	
	public VipMineAdapter(Context context) {
		super(context);
		this.act =(Activity)context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ViewHolder holder;
		if (row == null) {
			LayoutInflater inflater = act.getLayoutInflater();
			row = inflater.inflate(R.layout.vip_mine_item, null);
			holder = new ViewHolder();

			holder.tvDetail = (TextView) row.findViewById(R.id.tvDetail);
			holder.btnGet = (TextView) row.findViewById(R.id.btnGet);
			holder.btnGet.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					
					
				}
			});
			row.setTag(holder);
		} else {
			holder = (ViewHolder) row.getTag();
		}

		String info = mList.get(position);
		holder.tvDetail.setText(info);
		holder.btnGet.setTag(info);
		
		return row;
	}
	private class ViewHolder {
		TextView tvDetail;
		TextView btnGet;
	}
}
