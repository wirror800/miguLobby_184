package com.MyGame.Midlet;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.MyGame.Midlet.service.AppVersion;
import com.MyGame.Midlet.service.MykjService.DownloadThread;


public class GirdViewGameAdapter extends BaseAdapter

{
	private static final String TAG="GirdViewGameAdapter";
	private Context     mContext;
	private List<AppVersion> mAdapterList;

	public GirdViewGameAdapter(Context context,List<AppVersion> list)

	{
		mAdapterList=list;
		mContext = context;
		
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
		ProgressBar progress_bar;
		ImageView imgDownload;
		ImageView imgStatus;
		TextView tvOnlineNum;
	}



	@Override  
	public View getView(int position, View convertView, ViewGroup parent)
	{
		final AppVersion gameItem=mAdapterList.get(position);			
		final int gameid=gameItem.getGameId();
		DownloadThread downloading=((MyGameMidlet)mContext).myService.getDownloadThread(gameid);

		int status=gameItem.getGameStatus();
		ViewHolder holder = null; 
		//if (convertView == null) { //去掉缓存数据
		holder = new ViewHolder(); 					
		convertView = LayoutInflater.from(mContext).inflate(R.layout.game_item,null); 

		holder.imgGameIcon = (ImageView) convertView.findViewById(R.id.imgGameIcon); 
		holder.imgGameIcon.setImageDrawable(gameItem.getGameIcon());
		holder.tvGameName = (TextView) convertView.findViewById(R.id.tvGameName); 
		holder.tvGameName.setText(gameItem.getGameName());
		holder.progress_bar = (ProgressBar) convertView.findViewById(R.id.progress_bar); 
		holder.imgDownload=(ImageView) convertView.findViewById(R.id.imgDownload); 
		holder.imgStatus=(ImageView) convertView.findViewById(R.id.imgGameStatus); 
		holder.tvOnlineNum = (TextView) convertView.findViewById(R.id.tvOnlineNum); 
		holder.tvOnlineNum.setText(gameItem.getOnLineNum());
		if(status==0){
			holder.imgStatus.setVisibility(View.INVISIBLE);
		}else if(status==1){
			holder.imgStatus.setVisibility(View.VISIBLE);
			holder.imgStatus.setImageResource(R.drawable.hot);
		}else if(status==2){
			holder.imgStatus.setVisibility(View.VISIBLE);
			holder.imgStatus.setImageResource(R.drawable.new1);
		}else{
			holder.imgStatus.setVisibility(View.INVISIBLE);
		}
		if(downloading!=null && !gameItem.isUpdateComplete()){
			holder.progress_bar.setVisibility(View.VISIBLE);
			holder.progress_bar.setProgress(gameItem.mProgress);
			holder.imgDownload.setVisibility(View.VISIBLE);			
			if(downloading.isCancelled()){
				holder.imgDownload.setImageResource(R.drawable.download_star);

			}else{
				holder.imgDownload.setImageResource(R.drawable.download_pause);
			}

			if(holder.progress_bar.getProgress()==0){		
				holder.progress_bar.setVisibility(View.INVISIBLE);
			}

		}else{
			holder.progress_bar.setVisibility(View.INVISIBLE);
			holder.imgDownload.setVisibility(View.INVISIBLE);		
		}

		convertView.setTag(holder); 
		//} else { 
		//	holder = (ViewHolder) convertView.getTag(); 
		//} 
		return convertView; 

	}

    /**
     * 更新进度条
     * @param postion
     * @param progess
     */
    public void chargeProgress(int postion,int progess) {
    	mAdapterList.get(postion).mProgress=progess;
        notifyDataSetChanged();
    }



}
