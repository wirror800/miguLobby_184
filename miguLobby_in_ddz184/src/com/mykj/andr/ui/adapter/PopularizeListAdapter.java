package com.mykj.andr.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mykj.andr.model.PopularizeFriendList.PopularizeFriend;
import com.MyGame.Midlet.R;

public class PopularizeListAdapter extends ArrayListAdapter<PopularizeFriend> {

	private Context ctx;
	private static int currPos;

	static class ViewHolder {
		ImageView avatar;
		TextView nickName;

	}
	
	public PopularizeListAdapter(Context context) {
		super(context);
		ctx = context;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		ViewHolder holder = null;
		if (view == null) {
			LayoutInflater inflater = ((Activity) ctx).getLayoutInflater();
			view = inflater.inflate(R.layout.poplarize_list_item, null);
			holder = new ViewHolder();
            view.setTag(holder);
            holder.nickName = (TextView) view.findViewById(R.id.tv_poplarize_nickname);
            holder.avatar = (ImageView) view.findViewById(R.id.iv_poplarize_avatar);
            view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		if (currPos == position && null != view) {
			view.setBackgroundResource(R.drawable.list_item_selected_bg);
		} else {
			view.setBackgroundResource(R.drawable.list_item_bg);
		}
        PopularizeFriend friend = mList.get(position);
        if (friend != null) {
        	holder.nickName.setText(friend.nickName);
        	if (friend.sex == 1) {
        		holder.avatar.setImageResource(R.drawable.ic_male_face);
			} else {
				holder.avatar.setImageResource(R.drawable.ic_female_face);
			}
		}
		
		return view;
	}

	public static void setCurrPos(int pos) {
		// TODO Auto-generated method stub
		currPos = pos;
	}
	public static int getCurrPos() {
		// TODO Auto-generated method stub
		return currPos;
	}

	
}
