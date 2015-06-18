package com.mykj.andr.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mykj.andr.model.VipPrivilege;
import com.MyGame.Migu.R;

/**
 * 
 * @ClassName: VipPrivilegeAdapter
 * @Description: Vip Privilege adapter
 * @author  
 * @date 2013-6-8 上午11:09:53
 *
 */
public class VipPrivilegeAdapter extends ArrayListAdapter<VipPrivilege> {

	private Activity act;

	 
	public VipPrivilegeAdapter(Context context) {
		super(context);
		this.act =(Activity)context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		ViewHolder holder;
		if (row == null) {
			LayoutInflater inflater = act.getLayoutInflater();
			row = inflater.inflate(R.layout.vip_privilege_item, null);
			holder = new ViewHolder();

			holder.tvvipDegree = (TextView) row.findViewById(R.id.tvvipDegree);
			holder.tvexperienceAdd = (TextView) row.findViewById(R.id.tvexperienceAdd);
			holder.tvconposeRate = (TextView) row.findViewById(R.id.tvconposeRate);
			holder.tvweekAward = (TextView) row.findViewById(R.id.tvweekAward);
			holder.tvotherExponent = (TextView) row.findViewById(R.id.tvotherExponent);  
			 

			row.setTag(holder);
		} else {
			holder = (ViewHolder) row.getTag();
		}

		VipPrivilege info = mList.get(position);
		holder.tvvipDegree.setText(info.vipDegree+"");
		holder.tvexperienceAdd.setText(info.experienceAdd);
		holder.tvconposeRate.setText(info.conposeRate);
		holder.tvweekAward.setText(info.weekAward);
		holder.tvotherExponent.setText(info.otherExponent);
		 
		return row;
	}

	private class ViewHolder {
		TextView tvvipDegree;
		TextView tvexperienceAdd;
		TextView tvconposeRate;
		TextView tvweekAward;
		TextView tvotherExponent; 
	}
	
}
