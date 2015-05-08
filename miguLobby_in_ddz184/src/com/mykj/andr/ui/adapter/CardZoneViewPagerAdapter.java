package com.mykj.andr.ui.adapter;

import java.util.List;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

public class CardZoneViewPagerAdapter extends PagerAdapter{
	private List<View> mListViews;

	/**
	 * 构造方法，参数是我们的页卡，
	 * @param mListViews
	 */
	public CardZoneViewPagerAdapter(List<View> listViews) {
		this.mListViews = listViews;
	}

	/**
	 * 重设数据
	 * @param listViews
	 */
	public void setViews(List<View> listViews){
		this.mListViews.clear();
		this.mListViews.addAll(listViews);
		this.notifyDataSetChanged();
	}
	
	/**
	 * 删除指定页卡
	 */
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) 	{	
		//container.removeView(mListViews.get(position));
		((ViewGroup) container).removeView((View) object);
	}




	/**
	 * 这个方法用来实例化页卡	
	 */
	@Override
	public Object instantiateItem(ViewGroup container, int position){
		container.addView(mListViews.get(position), 0);//添加页卡
		return mListViews.get(position);
	}



	/**
	 * 返回页卡的数量
	 */
	@Override
	public int getCount() {			
		return  mListViews.size();
	}



	/**
	 * 官方提示这样写
	 */
	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {			
		return arg0==arg1;
	}




}
