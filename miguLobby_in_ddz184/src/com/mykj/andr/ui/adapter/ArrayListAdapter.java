package com.mykj.andr.ui.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

/*********************
 * 抽象适配器，作为抽象基类使用，封装BaseAdapter基本操作
 * 
 * @author zhanghuadong 2012-6-18
 * @param <T>
 */
public abstract class ArrayListAdapter<T> extends BaseAdapter {

	protected ArrayList<T> mList;
	protected Context mContext;
	protected ListView mListView;

	public ArrayListAdapter(Context context) {
		this.mContext = context;
	}

	@Override
	public int getCount() {
		if (mList != null)
			return mList.size();
		else
			return 0;
	}

	@Override
	public Object getItem(int position) {
		return mList == null ? null : mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public abstract View getView(int position, View convertView,
			ViewGroup parent);

	public void setList(ArrayList<T> list) {
		this.mList = list;
		notifyDataSetChanged();
	}

	public ArrayList<T> getList() {
		return mList;
	}

	public void setList(T[] list) {
		ArrayList<T> arrayList = new ArrayList<T>(list.length);
		for (T t : list) {
			arrayList.add(t);
		}
		setList(arrayList);
	}

	public ListView getListView() {
		return mListView;
	}

	public void setListView(ListView listView) {
		mListView = listView;
	}

}
