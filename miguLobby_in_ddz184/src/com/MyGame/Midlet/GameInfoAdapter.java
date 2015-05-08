package com.MyGame.Midlet;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.MyGame.Midlet.service.GameInfoItem;

public class GameInfoAdapter extends BaseAdapter{
	private List<GameInfoItem> mImgList;       //图片bitmap   
	private Context mContext;  
	
	
	public GameInfoAdapter(List<GameInfoItem> list, Context context) {  
		mImgList = list;  
		mContext = context; 

	}
	
	
	@Override
	public int getCount() {
		return mImgList.size();
	}

	@Override
	public Object getItem(int position) {
		return mImgList.get(position);
	}

	@Override
	public long getItemId(int position) {

		return position;  
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) { 
		final GameInfoItem gameInfo=mImgList.get(position);
		ImageView gallery_image;  
		if(convertView==null){  
			convertView = LayoutInflater.from(mContext).inflate(R.layout.gameinfo_item,null); //实例化convertView  
			gallery_image=(ImageView)convertView.findViewById(R.id.gallery_image);		
			gallery_image.setImageDrawable(gameInfo.getGameInfoIcon());		
			convertView.setTag(gallery_image);  
		}  
		else{  
			gallery_image = (ImageView) convertView.getTag();  
		}  

		return convertView;  

	}

}
