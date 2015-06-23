package com.mykj.game.moregame;

import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.MyGame.Midlet.R;
import com.mykj.game.ddz.api.AnalyticsUtils;
import com.mykj.game.moregame.MoreGameManager.DownloadThread;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.Log;
import com.mykj.game.utils.Util;


public class MoreGameListAdapter extends BaseAdapter
{
	
	
	
	private static final String TAG="MoreGameListAdapter";
	private Context     mContext;
	private Resources mResource;
	private List<AppVersion> mAdapterList;
	private Handler mHand;
	public MoreGameListAdapter(Context context, Handler handler, List<AppVersion> list)
	{
		mAdapterList=list;
		mContext = context;
		mHand = handler;
		mResource = context.getResources();
		Log.i(TAG, "init list size is " + list.size());
	}


	// 获取图片的个数
	@Override  
	public int getCount()
	{
		if (mAdapterList == null) {  
			return 0;  
		}else{  
			return mAdapterList.size();  
		}  
	}


	@Override  
	public Object getItem(int position)
	{
		return mAdapterList.get(position);
	}

	public AppVersion getItemById(int id){
		if(mAdapterList != null && mAdapterList.size() > 0){
			for(AppVersion item : mAdapterList){
				if(item.getGameId() == id){
					return item;
				}
			}
		}
		return null;
	}


	@Override  
	public long getItemId(int position)
	{
		return position;
	}

	/**
	 * 游戏菜单Item控件组成
	 *
	 */
    class ViewHolder {
		ImageView imgGameIcon; 
		TextView tvGameName;
		TextView tvSize;
		LinearLayout downloadProgress;		
		ProgressBar progress_bar;
		TextView tvDetail;
		TextView tvDiscripton;
		TextView tvReward;
		Button btnOper;
	}



	@Override  
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		final AppVersion gameItem=mAdapterList.get(position);
		boolean isWap = gameItem.isWap();
		final int gameid=gameItem.getGameId();
		DownloadThread downloading=((MoregameActivity)mContext).myManager.getDownloadThread(gameid);

		ViewHolder holder = null; 
		if (convertView == null) {
		holder = new ViewHolder(); 					
		convertView = LayoutInflater.from(mContext).inflate(R.layout.more_game_listitem,null); 
		}else{
			holder =  (ViewHolder)convertView.getTag();
		}
		
		//icon
		holder.imgGameIcon = (ImageView) convertView.findViewById(R.id.iv_more_game_list_icon); 
		holder.imgGameIcon.setImageDrawable(gameItem.getGameIcon());
		
		//name
		holder.tvGameName = (TextView) convertView.findViewById(R.id.tv_more_game_list_appname); 
		holder.tvGameName.setText(gameItem.getGameName());
		
		//size
		holder.tvSize = (TextView)convertView.findViewById(R.id.tv_more_game_list_appsize);
		holder.tvSize.setText(gameItem.getApkSize());
		//progress
		holder.downloadProgress = (LinearLayout)convertView.findViewById(R.id.ll_more_game_progress);
		holder.progress_bar = (ProgressBar) convertView.findViewById(R.id.pb_more_game_progressbar); 
		//detail
		holder.tvDetail = (TextView)convertView.findViewById(R.id.tv_more_game_list_progressdetail);
		if(isWap){
			holder.tvSize.setVisibility(View.INVISIBLE);
			holder.downloadProgress.setVisibility(View.GONE);
		}else{
			if(downloading != null){
				//下载时不显示size，显示进度条和进度文字
				holder.tvSize.setVisibility(View.GONE);
				holder.downloadProgress.setVisibility(View.VISIBLE);
				holder.progress_bar.setProgress(gameItem.mProgress);
				holder.tvDetail.setText(mResource.getString(R.string.more_game_downloaded)+":" + gameItem.mProgress + "%");
			}else{
				//不下载时显示size，不显示进度条和进度文字
				holder.tvSize.setVisibility(View.VISIBLE);
				holder.downloadProgress.setVisibility(View.GONE);
			}
		}
		
		//描述
		holder.tvDiscripton = (TextView) convertView.findViewById(R.id.tv_more_game_list_discription);
		holder.tvDiscripton.setText(gameItem.getGameDesc());
		
		//奖励信息
		holder.tvReward = (TextView) convertView.findViewById(R.id.tv_more_game_reward);
		holder.tvReward.setVisibility(View.VISIBLE);
		String rewardDesc = gameItem.getRewardProp();   //优先rewardProp
		if(Util.isEmptyStr(rewardDesc)){    //若没有rewardProp就用reward
			rewardDesc = gameItem.getReward() + AppConfig.UNIT;
		}
		if(gameItem.getReward() == 0 && (Util.isEmptyStr(gameItem.getRewardProp()))){
			holder.tvReward.setVisibility(View.INVISIBLE);
		}
		else if(gameItem.isReward()){
			holder.tvReward.setTextColor(0xffffffff);
			holder.tvReward.setText(mResource.getString(R.string.more_game_gifted) +"：" + rewardDesc);
		}else{
			holder.tvReward.setTextColor(0xffffdd57);
			holder.tvReward.setText(mResource.getString(R.string.more_game_gift)+"：" + rewardDesc);
		}
		//操作按钮
		holder.btnOper = (Button) convertView.findViewById(R.id.btn_more_game_oper);
		holder.btnOper.setBackgroundResource(R.drawable.tab_btn_blue_selector);
		holder.btnOper.setText(mResource.getString(R.string.more_game_download));
		holder.btnOper.setEnabled(true);
		holder.btnOper.setTextColor(convertView.getResources().getColor(R.color.blue_btn_text_color));
		//初始为下载按钮
		final int clickType ;
		
		if(isWap){
			
			if(!gameItem.isReward() && gameItem.isNotifyed()){
				holder.btnOper.setText(mResource.getString(R.string.more_game_obtain));
				holder.btnOper.setBackgroundResource(R.drawable.btn_orange);
				clickType = MoregameActivity.CLICK_TYPE_REWARD;
			}
			else{
				holder.btnOper.setText(mResource.getString(R.string.more_game_open));
				clickType = MoregameActivity.CLICK_TYPE_WEB;
			}
		}
		else{
			if(downloading != null){ //下载中
				if(downloading.isCancelled()){
					holder.btnOper.setText(mResource.getString(R.string.more_game_continue));
					clickType = MoregameActivity.CLICK_TYPE_CONTINUE;
				}else{
					holder.btnOper.setText(mResource.getString(R.string.more_game_pause));
					clickType = MoregameActivity.CLICK_TYPE_PAUSE;
				}
			}else{
				if(gameItem.isDownloaded()){
					if(!gameItem.isAppInstalled()){//安装
						holder.btnOper.setText(mResource.getString(R.string.more_game_install));
						holder.btnOper.setBackgroundResource(R.drawable.btn_orange);
						clickType = MoregameActivity.CLICK_TYPE_INSTALL;
					}else if(!gameItem.isReward()){//领奖
						holder.btnOper.setText(mResource.getString(R.string.more_game_obtain));
						holder.btnOper.setBackgroundResource(R.drawable.btn_orange);
						clickType = MoregameActivity.CLICK_TYPE_REWARD;
					}else{//打开
						holder.btnOper.setText(mResource.getString(R.string.more_game_open));
						clickType = MoregameActivity.CLICK_TYPE_OPEN;
					}
				}else{
					clickType = MoregameActivity.CLICK_TYPE_DOWNLOAD;
				}
			}
		}
		holder.btnOper.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Message msg = Message.obtain();
				msg.arg1 = gameid;
				msg.arg2 = clickType;
				msg.what = MoregameActivity.LIST_BUTTON_CLICK;
				mHand.sendMessage(msg);
				
				int eventId ;
				//友盟统计 事项实现逻辑,
				switch (clickType) {
				case MoregameActivity.CLICK_TYPE_DOWNLOAD:
					eventId = 102+position*3;
					AnalyticsUtils.onClickEvent(mContext, String.valueOf(eventId));
					break;
				case MoregameActivity.CLICK_TYPE_INSTALL:
					eventId = 103+position*3;
					AnalyticsUtils.onClickEvent(mContext, String.valueOf(eventId));
					break;
				case MoregameActivity.CLICK_TYPE_OPEN:
					eventId = 104+position*3;
					AnalyticsUtils.onClickEvent(mContext, String.valueOf(eventId));
					break;
				default:
					break;
				}
				
				
			}
		});
		
		convertView.setTag(holder);
		
		return convertView; 

	}

    /**
     * 更新进度条
     * @param postion
     * @param progess
     */
    public void chargeProgress(int id,int progess) {
    	for(AppVersion data : mAdapterList){
    		if(data.getGameId() == id){
    			data.mProgress = progess;
    			break;
    		}
    	}
        notifyDataSetChanged();
    }
}
