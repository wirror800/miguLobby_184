package com.mykj.andr.ui;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mykj.andr.model.HallDataManager;
import com.mykj.andr.model.RankOrderInfo;
import com.mykj.andr.ui.adapter.RankOrderAdapter;
import com.MyGame.Migu.R;
import com.mykj.game.utils.AppConfig;

public class RankOrderDialog extends AlertDialog  implements View.OnClickListener{
	
	private  RankOrderAdapter adapter=null;
	private  TextView tvOwerRank;
	private  ImageView ivCancel;
	private  Context ctx;
	private  List<RankOrderInfo>  mList;
	private static boolean isShow = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Resources resource = AppConfig.mContext.getResources();
		setContentView(R.layout.rank_order_list_view);

		tvOwerRank=(TextView)findViewById(R.id.tvOwerRank);
		tvOwerRank.setText(resource.getString(R.string.ddz_match_your_rank)+getOwerOrder(mList)+resource.getString(R.string.ddz_match_your_rank_ming));
		ivCancel=(ImageView)findViewById(R.id.ivCancel);
		ivCancel.setOnClickListener(this);
		
		adapter=new RankOrderAdapter(ctx,mList);
		ListView rankListView=(ListView)findViewById(R.id.rank_list_view);
		rankListView.setAdapter(adapter);
	}
	
	
	
	public RankOrderDialog(Context context,List<RankOrderInfo>  rankList) {
		super(context);
		this.ctx = context;
		this.mList = rankList;
	}

	public void updateList(){
		if(adapter != null){
			adapter.notifyDataSetChanged();
		}
	}
	
	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.ivCancel) {
			dismiss();
		} 
	}
	
	
	/**
	 * 获取自己的排名信息
	 * @param list
	 * @return
	 */
	private int getOwerOrder(List<RankOrderInfo> list){
		int userID=HallDataManager.getInstance().getUserMe().userID;
		int order = 0;
		if(list.size()>0){
			for(RankOrderInfo info:list){
				if(info.userID==userID){
					order=info.order;
					break;
				} 
			}
		}
		return order;
	}
	
	@Override
	public void show() {
		// TODO Auto-generated method stub
		if(!isShow){
			super.show();
			isShow = true;
		}
	}
	
	@Override
	public void dismiss() {
		// TODO Auto-generated method stub
		super.dismiss();
		isShow = false;
	}
	
	public interface DataChangeListener{
		public void onDataChanged();
	}
}
