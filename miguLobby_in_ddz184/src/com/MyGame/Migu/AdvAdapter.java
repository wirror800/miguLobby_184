package com.MyGame.Migu;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.MyGame.Midlet.service.AdvItem;

public class AdvAdapter extends PagerAdapter {
	private List<AdvItem> mImgList; // 图片bitmap
	private Context mContext;

	public AdvAdapter(List<AdvItem> list, Context context) {
		mImgList = list;
		mContext = context;

	}

	public List<AdvItem> getList() {
		return mImgList;
	}

	/**
	 * 删除指定页卡
	 */
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
		object = null;
	}

	/**
	 * 这个方法用来实例化页卡
	 */
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		ImageView image = mImgList.get(position).getAdvIcon();
		container.addView(image);// 添加页卡
		final int index = position;
		image.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				AdvItem adv = mImgList.get(index);
				String onclick = adv.getOnClick();
				if (onclick == null || onclick.trim().length() == 0) {
					return;
				}
				Intent in = new Intent();
				Uri uri = Uri.parse(onclick);
				in.setAction(Intent.ACTION_VIEW);
				in.setData(uri);
				mContext.startActivity(in);
			}

		});

		return image;
	}

	/**
	 * 返回页卡的数量
	 */
	@Override
	public int getCount() {
		return mImgList.size();
	}

	/**
	 * 官方提示这样写
	 */
	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

}
