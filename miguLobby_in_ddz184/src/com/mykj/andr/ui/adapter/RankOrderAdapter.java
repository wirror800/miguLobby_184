package com.mykj.andr.ui.adapter;

import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.mykj.andr.model.RankOrderInfo;
import com.MyGame.Migu.R;


/**
 * 
 * @ClassName: RankOrderAdapter
 * @Description: 比赛排名信息
 * @author Administrator
 * @date 2013-4-23 下午06:19:49
 *
 */
public class RankOrderAdapter extends BaseAdapter {
	private List<RankOrderInfo> mList;
	private Context mContext;
	private Resources mResource;
	public RankOrderAdapter(Context context,List<RankOrderInfo> list) {
		mContext=context;
		this.mResource = mContext.getResources();
		mList=list;
	}
	public void update(List<RankOrderInfo> list){
		mList = list;
		notifyDataSetChanged();
	}
	
	ViewHolder holder;
	

	@Override
	public int getCount() {
		return mList.size();
	}


	@Override
	public Object getItem(int position) {
		return mList.get(position);
	}


	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			// 获得界面解析器
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.user_rank_order_item,null);
			holder = new ViewHolder();
			 
			holder.tvRank=(TextView)convertView.findViewById(R.id.tvRank);
			holder.tvscore=(TextView)convertView.findViewById(R.id.tvscore);
			holder.tvnickName=(TextView)convertView.findViewById(R.id.tvnickName);
			
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		RankOrderInfo info=mList.get(position);
		if(position%2==1){
			convertView.setBackgroundResource(R.drawable.rank_item_bg);
		}else{
			convertView.setBackgroundColor(0xff016192);
		}
		String orders="";
		if(info.cbPromotionFlag==0){
			orders=mResource.getString(R.string.match_eliminate);
		}else if(info.cbPromotionFlag==1){
			orders=mResource.getString(R.string.match_di)+info.order+mResource.getString(R.string.match_ming);
		}
		holder.tvRank.setText(orders);
		holder.tvscore.setText(info.score+"");
		holder.tvnickName.setText(info.nickName);
		
		
		return convertView;
	}

	
	public static class ViewHolder {
		public TextView tvRank;
		public TextView tvscore;
		public TextView tvnickName; 
	}

}
