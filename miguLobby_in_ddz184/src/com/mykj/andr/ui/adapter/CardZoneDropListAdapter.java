package com.mykj.andr.ui.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mykj.andr.model.NewUIDataStruct;
import com.MyGame.Midlet.R;

/**
 * @author wanghj
 * 下拉列表适配器
 *
 */
public class CardZoneDropListAdapter extends BaseAdapter{

	/*用来显示的菜单名字*/
	private List<NewUIDataStruct> items;
	
	/*高亮索引*/
	private int hilight = -1;
	private Context context;
	public CardZoneDropListAdapter(List<NewUIDataStruct> items, Context context){
		this.items = items;
		this.context = context;
	}
	public String getString(int index){
		if(index >= 0 && index < items.size()){
			return items.get(index).Name;
		}
		return null;
	}
	public void setHilight(int hilight){
		this.hilight = hilight;
	}
	public int getHilight(){
		return hilight;
	}
	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){
			LayoutInflater mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = mLayoutInflater.inflate(R.layout.card_zone_drop_listitem, null);
		}
		TextView tv = (TextView)convertView.findViewById(R.id.card_zone_under_listitem_text);
		tv.setText(items.get(position).Name);
		
		
		return convertView;
	}
}
