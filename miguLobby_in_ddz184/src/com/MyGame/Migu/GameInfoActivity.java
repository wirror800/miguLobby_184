package com.MyGame.Migu;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.MyGame.Midlet.service.AppVersion;
import com.MyGame.Midlet.service.GameInfoItem;
import com.MyGame.Midlet.service.MykjService.DownloadThread;
import com.MyGame.Midlet.service.MykjService.MykjServiceBinder;
import com.MyGame.Midlet.util.AppConfig;
import com.MyGame.Midlet.util.Configs;
import com.MyGame.Migu.R;


public class GameInfoActivity extends Activity{
	private static final String TAG="GameInfoActivity";


	private Context mContext;
	private boolean mBinded;

	private int mPosition;
	private int mGameId;

	private MykjServiceBinder mService;
	private List<AppVersion>  mGamesList= new ArrayList<AppVersion>();

	private TextView tvBack;
	private TextView tvTitle;

	private Gallery mGameInfoGallery;  
	private GameInfoAdapter imageAdapter;

	private ImageView imgGameIcon;
	private TextView tvGameName;
	private TextView tvSizeNum;
	private TextView tvOnlineNum;
	private TextView tvGameDesc;
	private ProgressBar progress_bar;
	private TextView btnGameDownLoad;



	public Handler mGameInfoHandler = new Handler() {
		public void handleMessage(Message msg) {
			final int position=msg.arg1;
			switch (msg.what) {
			//获取服务器游戏详情信息成功
			case 1: {
				if (mGamesList != null && position <= mGamesList.size() - 1) {
					AppVersion gameItem = (AppVersion) mGamesList.get(position); // 游戏类结果

					// String onlineurl=gameItem.getOnLineUrl();
					// GetOnlineTask task = new GetOnlineTask();
					// task.execute(onlineurl); //获取在线人数

					tvOnlineNum.setText(gameItem.getOnLineNum());
					String gameinfo_url = AppConfig.GAMEINFO_URL + "gameid="
							+ mGameId;
					mService.serviceHttpGetGameInfo(gameinfo_url,
							downloadHandler);// 获取游戏详情图片 , //暂未提供URL

					if (gameItem.isAppInstalled()) {
						btnGameDownLoad.setText("运行");
						btnGameDownLoad
								.setBackgroundResource(R.drawable.yellow_selector);
						// 运行
					} else if (gameItem.isUpdateComplete()) {
						btnGameDownLoad.setText("安装");
						btnGameDownLoad
								.setBackgroundResource(R.drawable.yellow_selector);
						// 安装
					} else {
						btnGameDownLoad.setTextColor(Color.WHITE);
						btnGameDownLoad.setText("下载");
						btnGameDownLoad
								.setBackgroundResource(R.drawable.green_selector);
					}
					btnGameDownLoad
							.setOnClickListener(new btnOnClickListener());
					String gametitle = gameItem.getGameName();
					Drawable gameicon = gameItem.getGameIcon();
					String gamename = gameItem.getGameName();
					String gamesize = gameItem.getApkSize();

					long size = Long.parseLong(gamesize);
					float ft = ((float) size) / (1024 * 1024);
					int scale = 2;// 设置位数
					int roundingMode = 4;// 表示四舍五入，可以选择其他舍值方式，例如去尾，等等.
					BigDecimal bd = new BigDecimal((double) ft);
					bd = bd.setScale(scale, roundingMode);
					ft = bd.floatValue();

					tvTitle.setText(gametitle);
					imgGameIcon.setImageDrawable(gameicon);
					tvGameName.setText(gamename);
					tvSizeNum.setText(ft + "M");
				}

			}
			break;
			}

		}
	};




	public Handler downloadHandler = new Handler() {
		public void handleMessage(Message msg) {
			Bundle bundle=msg.getData();			
			int rate=bundle.getInt("rate");
			String apkPath=bundle.getString("filepath");
			AppVersion gameItem=(AppVersion) mGamesList.get(mPosition);
			
			switch (msg.what) {
			case MyGameMidlet.UPDATE_DOWNLOAD_PROGRESS: 

				progress_bar.setVisibility(View.VISIBLE);
				progress_bar.setProgress(rate);
				btnGameDownLoad.setText("暂停");


				break;
			case MyGameMidlet.SERVICE_DOWNLOAD_SUCCESS: 
				progress_bar.setVisibility(View.INVISIBLE);
				btnGameDownLoad.setText("安装");
				Configs.installApk(mContext,apkPath);


				break;
			case MyGameMidlet.SERVICE_DOWNLOAD_FAIL: 
				btnGameDownLoad.setText("继续");
				progress_bar.setVisibility(View.INVISIBLE);
				Toast.makeText(mContext, gameItem.getGameName()+"下载失败!", Toast.LENGTH_SHORT).show();		

				break;
			case MyGameMidlet.GET_GAMEINFO_SUCCESS:
				List<GameInfoItem> gameList=mService.getGameInfoConfig();				
				GameInfoGalleryInit(gameList);
				String gamedes=mService.getGameInfoDes(); //获取游戏详情描述
				tvGameDesc.setText("        "+gamedes);

				break;
			case MyGameMidlet.GET_GAMEINFO_FAIL:

				break;

			case MyGameMidlet.GET_GAMEINFO_VERSION_UPDATE:
				if(imageAdapter!=null){
					imageAdapter.notifyDataSetChanged();
				}
				break;
			}

		}
	};




	@Override  
	public void onCreate(Bundle savedInstanceState) {  
		super.onCreate(savedInstanceState);  
		setContentView(R.layout.activity_gameinfo); 
		mContext=this;
		Intent intent = getIntent();	
		Bundle bundle=intent.getExtras();
		mPosition=bundle.getInt("position");           //intent.getIntExtra("position", 0);
		mGameId=bundle.getInt("gameid");

		tvBack=(TextView)findViewById(R.id.tvBack);
		tvBack.setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View v) {
				finish();			
			}
		});

		tvTitle=(TextView)findViewById(R.id.tvTitle);
		imgGameIcon=(ImageView)findViewById(R.id.imgGameIcon);
		tvGameName=(TextView)findViewById(R.id.tvGameName);
		tvSizeNum=(TextView)findViewById(R.id.tvSizeNum);
		tvOnlineNum=(TextView)findViewById(R.id.tvOnlineNum);
		tvGameDesc=(TextView)findViewById(R.id.tvGameInfo);
		progress_bar=(ProgressBar)findViewById(R.id.progress_bar);
		btnGameDownLoad=(TextView)findViewById(R.id.btnGameDownLoad);

		Intent in = new Intent();
		in.setAction("mykj.service.BOOT_SERVICE"); 
		bindService(in, serviceGameInfoConn, Context.BIND_AUTO_CREATE);

	}



	/**
	 * bind service callback
	 */
	private ServiceConnection serviceGameInfoConn = new ServiceConnection(){   
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService =(MykjServiceBinder)service; 
			mBinded = true; 
			mGamesList=mService.getGamesConfig();
			Log.v(TAG, "Service Connected...");
			Message msg=mGameInfoHandler.obtainMessage();
			msg.what=1;
			msg.arg1=mPosition;																	
			mGameInfoHandler.handleMessage(msg);

		}
		// 连接服务失败后，该方法被调用
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = null;
			mBinded = false; 
			Log.e(TAG, "Service Failed...");
		}
	}; 



	private class btnOnClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			AppVersion gameItem=(AppVersion) mGamesList.get(mPosition);
			final String url=gameItem.getDownloadUrl();
			final int gameid=gameItem.getGameId();
			final String savePath=AppVersion.getDownloadPath();
			final String md5=gameItem.getDownFileConfigMD5();

			DownloadThread downloading=mService.getDownloadThread(gameid);			
			Log.v(TAG,"download id="+gameid);
			btnGameDownLoad.setTextColor(Color.BLACK);
			btnGameDownLoad.setBackgroundResource(R.drawable.yellow_selector);
			if(gameItem.isAppInstalled()){
				if(MyGameMidlet.getIsLoginStatus()==2){
					Toast.makeText(mContext, "正在为您登录，请稍后...", Toast.LENGTH_SHORT).show();
				}else{
					Log.v(TAG,"game will be start!");
					//运行游戏				
					gameItem.startGame(mContext);
					finish();
				}
			}else if(gameItem.isUpdateComplete()){
				Log.v(TAG,"game will be setup!");
				//安装游戏
				Configs.installApk(mContext,gameItem.getAPKFilePath());
			}else if(downloading==null){
				Log.v(TAG,"game will be download!");
				//下载游戏
				btnGameDownLoad.setText("暂停");
				mService.startDownloadFile(gameid,url,savePath,md5,downloadHandler,mPosition);
				progress_bar.setVisibility(View.VISIBLE);
			}else if(downloading!=null){
				if(downloading.isCancelled()){
					Log.v(TAG,"game download will be restart!");
					btnGameDownLoad.setText("暂停");
					mService.clearDownloadThread(gameid);
					mService.startDownloadFile(gameid,url,savePath,md5,downloadHandler,mPosition);	

				}else{
					Log.v(TAG,"game download will be pause!");
					//显示下载暂停图片
					downloading.cancel();
					btnGameDownLoad.setText("继续");
				}
			}

		}

	}

	@Override
	public void onStop(){
		super.onStop();
		if(mBinded&&mGamesList!=null&&mGamesList.size()>0){
			AppVersion gameItem=(AppVersion) mGamesList.get(mPosition);
			final int gameid=gameItem.getGameId();
			DownloadThread downloading=mService.getDownloadThread(gameid);	
			if(downloading!=null){
				downloading.cancel();
				mService.clearDownloadThread(gameid);
				btnGameDownLoad.setText("继续");
				((MyGameMidlet) MyGameMidlet.mContext).refreshGameIconStatus(mPosition);
				
			}
		}
	}

	@Override
	public void onResume(){
		super.onResume();
		if(mGamesList!=null&&mGamesList.size()>0){
			AppVersion gameItem=(AppVersion) mGamesList.get(mPosition);

			if(gameItem.isAppInstalled()){
				btnGameDownLoad.setText("运行");		
			}
		}
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		unbindService(serviceGameInfoConn);
	}

/*
	//加载图片的异步任务  
	class GetOnlineTask extends AsyncTask<String,Void,String>{  
		//从网上下载图片  
		@Override  
		protected String doInBackground(String... params) {  
			Log.v(TAG, "params[0]"+params[0]);
			String userdata=Configs.getConfigXmlByHttp(params[0]);
			Log.v(TAG, "userdata"+userdata);
			String num=null;

			try
			{
				String[] q = userdata.split("#");
				num=q[2];  //在线人数
			}
			catch(NullPointerException e)
			{

			}

			return num;  
		}
		@Override
		protected void onPostExecute(String result) {
			if(!Configs.isEmptyStr(result)){
				String num=result+"人";
				Log.v(TAG, "num="+num);
				tvOnlineNum.setText(num);
			}			
		}
	}  

*/

	private void GameInfoGalleryInit(List<GameInfoItem> list){  

		mGameInfoGallery = (Gallery)findViewById(R.id.galGameInfo); 
		imageAdapter = new GameInfoAdapter(list,this);  
		mGameInfoGallery.setAdapter(imageAdapter);  
		mGameInfoGallery.setSpacing(30);
		/*LinearLayout pointLinear = (LinearLayout) findViewById(R.id.gallery_point_linear);
		sumOfDrawble=list.size();
		for (int i = 0; i < list.size(); i++) {
			ImageView pointView = new ImageView(this);
			if(i==0){
				pointView.setBackgroundResource(R.drawable.feature_point_cur);
			}else
				pointView.setBackgroundResource(R.drawable.feature_point);
			pointLinear.addView(pointView);
		}*/

	}  




}
