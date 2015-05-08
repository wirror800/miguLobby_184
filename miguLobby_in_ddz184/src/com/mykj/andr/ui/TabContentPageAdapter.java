package com.mykj.andr.ui;

import java.util.List;

import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

/*****************************
 * 承载Page页面,用于切换TabContent
 * 
 * @author zhanghusdong
 */

public class TabContentPageAdapter extends PagerAdapter {

	private List<View> mViews;

	public TabContentPageAdapter(List<View> mViews) {
		this.mViews = mViews;
	}

	@Override
	public void destroyItem(View arg0, int arg1, Object arg2) {
		((ViewPager) arg0).removeView(mViews.get(arg1));
	}

	@Override
	public void finishUpdate(View arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getCount() {

		return mViews.size();
	}

	@Override
	public Object instantiateItem(View arg0, int arg1) {
		((ViewPager) arg0).addView(mViews.get(arg1), 0);
		return mViews.get(arg1);
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == (arg1);
	}

	@Override
	public void restoreState(Parcelable arg0, ClassLoader arg1) {

	}

	@Override
	public Parcelable saveState() {

		return null;
	}

	@Override
	public void startUpdate(View arg0) {

	}
}
