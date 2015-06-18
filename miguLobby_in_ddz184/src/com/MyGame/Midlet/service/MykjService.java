package com.MyGame.Midlet.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.text.format.Time;
import android.util.Log;
import android.widget.RemoteViews;

import com.MyGame.Migu.MyGameMidlet;
import com.MyGame.Migu.R;
import com.MyGame.Midlet.util.AppConfig;
import com.MyGame.Midlet.util.Configs;
import com.login.view.AccountManager;
import com.mingyou.accountInfo.LoginInfoManager;


public class MykjService extends Service{  

	private static final String TAG="MykjService";
	//private static final long WATCHDOG_DELAY=60*60*1000; //1小时;

	public static final String NOTIFIYPATH=Configs.getSdcardPath()+AppConfig.SERVICE_DOWNLOAD_PATH +"/notifiy.xml";
	public static final String LOBBYPATH=Configs.getSdcardPath()+AppConfig.SERVICE_DOWNLOAD_PATH +"/lobby.xml";
	public static final String APPVERSIONPATH=Configs.getSdcardPath()+AppConfig.SERVICE_DOWNLOAD_PATH +"/version.xml";
	public static final String QUICKLOBBYPATH=Configs.getSdcardPath()+AppConfig.SERVICE_DOWNLOAD_PATH +"/quicklobby.xml";
	public static final String CUSTOMINFO=Configs.getSdcardPath()+AppConfig.SERVICE_DOWNLOAD_PATH +"/custom.xml";
	public static final String TESTIPLIST=Configs.getSdcardPath()+AppConfig.SERVICE_DOWNLOAD_PATH +"/__IP_PORT_CONFIG__.txt";
	public static final String ADVPATH=Configs.getSdcardPath()+AppConfig.SERVICE_DOWNLOAD_PATH +"/adv.xml";
	public static final String GAMEINFOPATH=Configs.getSdcardPath()+AppConfig.SERVICE_DOWNLOAD_PATH +"/gameinfo/";

	public static final int GET_NOTIFIY_XML_SUCCESS=1;
	public static final int GET_NOTIFIY_XML_FAIL=2;
	public static final int GET_LOBBY_XML_SUCCESS=3;
	public static final int GET_LOBBY_XML_FAIL=4;
	public static final int GET_GAMEVERSION_XML_SUCCESS=5;
	public static final int GET_GAMEVERSION_XML_FAIL=6;
	public static final int SERVICE_DOWNLOAD_FILE_SUCCESS=7;
	public static final int SERVICE_DOWNLOAD_FILE_FAIL=8;
	public static final int SERVICE_DOWNLOAD_NET_CONNECT_FAIL=9;
	public static final int GET_QUICK_LOBBY_XML_SUCCESS=11;
	public static final int GET_QUICK_LOBBY_XML_FAIL=12;
	public static final int GET_CUSTOM_INFO_XML_SUCCESS=13;
	public static final int GET_CUSTOM_INFO_XML_FAIL=14;
	public static final int SERVICE_NOTIFIY_MSG=21;


	private NotificationManager mNotificationManager;  
	private AlarmManager mAlarmManager;
	private Context mContext;

	/**WIFI默认下载更新*/
	public boolean isBGUpdate;
	/**notifity提示音开关*/
	public boolean isNotifiySound;
	/**notifity提示震动开关*/
	public boolean isNotifiyVibrate;



	private String mTspan ;  //连接服务器频率
	private String mMax  ;  //每天最大消息弹出次数
	private String mDft;    //消息弹出免打扰开始时间
	private String mDet;    //消息弹出免打扰结束时间

	private String mAdvPath;
	private String mGameInfoPath;
	private String mFolder;
	private String mGameInfoDes;


	private List<NotifiyMsg> NotifiyMsgList = new ArrayList<NotifiyMsg>();
	private List<AppVersion> AppConfigList  = new ArrayList<AppVersion>();
	private List<AdvItem> adList = new ArrayList<AdvItem>();
	private List<GameInfoItem> gameInfoList = new ArrayList<GameInfoItem>();
	private LobbyVersion  mlobbyVersion;

	public HashMap<Integer,DownloadThread> mDownloadMap=new HashMap<Integer,DownloadThread>();



	/**
	 * service handler
	 */
	private Handler mServiceHandler = new Handler() {  
		@Override
		public void handleMessage(Message msg) {  
			Log.v(TAG,"mServiceHandler ,msg.what="+msg.what);
			switch (msg.what) { 
			// 网络获取消息成功
			case GET_NOTIFIY_XML_SUCCESS:  
				beginNotifiyMsg();
				break;
				// 网络获取消息失败，从本地文件获取
			case GET_NOTIFIY_XML_FAIL:  
				Log.e(TAG, "GET_NOTIFIY_XML_FAIL");
				break;	

			case GET_LOBBY_XML_SUCCESS:
				getLobbyVersionFromXml();
				setReqAppVerDate();    //当天已经检查版本了
				break;

			case GET_LOBBY_XML_FAIL:

				break;
				
				// 网络获取游戏版本信息成功	
			case GET_GAMEVERSION_XML_SUCCESS:  
				downLoadAPKBackGround();
				setReqAppVerDate();    //当天已经检查版本了
				break;	

				// 网络获取游戏版本信息成功,从本地文件获取	
			case GET_GAMEVERSION_XML_FAIL:  
				Log.e(TAG, "GET_GAMEVERSION_XML_FAIL");
				break;	

			case SERVICE_DOWNLOAD_FILE_SUCCESS:  
				//文件下载成功
				break;	
			case SERVICE_DOWNLOAD_FILE_FAIL:  
				//文件下载出现错误
				break;	
			case SERVICE_DOWNLOAD_NET_CONNECT_FAIL:  
				//文件下载网络连接错误
				break;
			case GET_CUSTOM_INFO_XML_SUCCESS:
				break;
			case GET_CUSTOM_INFO_XML_FAIL:
				break;
			case SERVICE_NOTIFIY_MSG:
				notifiyQueueMsg();//notifiy活动
				break;
			}
		}
	};	



	/**
	 * Activity绑定后回调
	 * */
	@Override
	public IBinder onBind(Intent intent)
	{
		Log.v(TAG, "service onBind...");
		IBinder mBinder = new MykjServiceBinder();
		return mBinder;
	} 




	/**
	 * activity和service通信接口
	 * */
	public class  MykjServiceBinder extends Binder{
		/**获取大厅中游戏配置信息*/
		public  List<AppVersion>  getGamesConfig(){
			return AppConfigList;
		}

		/**获取大厅广告配置信息*/
		public  List<AdvItem>  getADVConfig(){
			return adList;
		}

		/**获取游戏详情配置信息*/
		public  List<GameInfoItem>  getGameInfoConfig(){
			return gameInfoList;
		}

		/**
		 * 获取游戏详情
		 * @return
		 */
		public String getGameInfoDes(){
			return mGameInfoDes;
		}


		/**
		 * 清空游戏类数据
		 */
		public void clearAppConfigList(){
			AppConfigList.clear();
		}

		

		
		/**
		 * 获取大厅中游戏的版本配置,广告配置
		 */
		public void serviceHttpGetLobbyData(final Handler handler){
			//获取大厅中游戏的版本配置
			new Thread(){
				@Override
				public void run(){
					AppConfigList.clear();
					Message msg = handler.obtainMessage();  
					//游戏版本XML
					String local=Configs.readFromFile(APPVERSIONPATH);//配置文件读取配置，判读服务器是否有更新
					String localmd5=Configs.md5(local);
					boolean isInit = false;
					if(!Configs.isEmptyStr(local))
					{
						Log.v(TAG, "game version config xml read from local file");
						if(parseNotifiyXml(local)){
							msg.what = MyGameMidlet.GET_GAME_VERSION_SUCCESS;  						
							handler.sendMessage(msg);
						}else{
							isInit = true;
							Configs.deleteDir(new File(APPVERSIONPATH));
							Log.e(TAG, "parseNotifiyXml game version config xml fail");
						}

						String url = Configs.getConfigXmlUrl(AppConfig.CONFIG_CMD_GAMES);
						String wapstr=Configs.getConfigXmlByHttp(url);
						
				
						 //重连
						if (wapstr == null) {
							String before = "update.139game.com";
							String after = "update1.139game.com";
							if (url.indexOf(before) >= 0 ) {
								url = url.replace(before, after);
								 wapstr=Configs.getConfigXmlByHttp(url);
							}	
						}
						
						
						if(wapstr!=null){												  						
							String wapmd5=Configs.md5(wapstr);
							if(!localmd5.equals(wapmd5)){
								if(parseNotifiyXml(wapstr)){
									Configs.saveToFile(APPVERSIONPATH,wapstr);//将内容保存文件
									Message msg1 = handler .obtainMessage();
									if(!isInit){
										msg1.what = MyGameMidlet.GET_GAME_VERSION_UPDATE;  
										handler.sendMessage(msg1);
									}else{
										msg1.what = MyGameMidlet.GET_GAME_VERSION_SUCCESS;
										handler.sendMessage(msg1);
									}
									Log.e(TAG, "game config xml is need update");
								}else{ // 文件下载错误
									Message msg1 = handler .obtainMessage();
									msg1.what = MyGameMidlet.GET_GAME_VERSION_FAIL;  
									handler.sendMessage(msg1);
								}

							}
						}
						
					}
					//本地文件不存在，直接从网络获取
					else
					{
						Log.v(TAG, "game version config xml read from http");
						String url = Configs.getConfigXmlUrl(AppConfig.CONFIG_CMD_GAMES);
						String wapstr=Configs.getConfigXmlByHttp(url);
						
						 //重连
						if (wapstr == null) {
							String before = "update.139game.com";
							String after = "update1.139game.com";
							if (url.indexOf(before) >= 0 ) {
								url = url.replace(before, after);
								wapstr=Configs.getConfigXmlByHttp(url);
							}	
						}
						
						if(wapstr!=null){		
							if(parseNotifiyXml(wapstr)){
								Configs.saveToFile(APPVERSIONPATH,wapstr);//将内容保存文件
								msg.what = MyGameMidlet.GET_GAME_VERSION_SUCCESS;  
							}else{
								msg.what = MyGameMidlet.GET_GAME_VERSION_FAIL;  
							}
							handler.sendMessage(msg);
						}
						else{
							msg.what = MyGameMidlet.GET_GAME_VERSION_FAIL; 
							handler.sendMessage(msg);
						}

					}

					Log.v(TAG, "serviceHttpGetReqThread appversion xml is finished");
				}
			}.start();

			//获取大厅中游戏的广告配置
			new Thread(){
				@Override
				public void run(){
					adList.clear();
					Message msg = handler.obtainMessage();  
					//广告版本XML
					String local=Configs.readFromFile(ADVPATH);//配置文件读取配置，判读服务器是否有更新
					String localmd5=Configs.md5(local);
					boolean isInit = false;
					if(!Configs.isEmptyStr(local))
					{
						Log.v(TAG, "adv config xml read from local file");
						if(parseNotifiyXml(local)){
							msg.what = MyGameMidlet.GET_LOBBY_ADV_SUCCESS;  						
							handler.sendMessage(msg);
						}else{
							Configs.deleteDir(new File(ADVPATH));
							isInit = true;
							Log.e(TAG, "parseNotifiyXml adv config xml fail");
						}

						String url = Configs.getConfigXmlUrl(AppConfig.CONFIG_CMD_ADV);
						String wapstr=Configs.getConfigXmlByHttp(url);
						if(wapstr!=null){												  						
							String wapmd5=Configs.md5(wapstr);
							if(!localmd5.equals(wapmd5)){
								if(parseNotifiyXml(wapstr)){
									Configs.saveToFile(ADVPATH,wapstr);//将内容保存文件	
									Message msg1 = handler .obtainMessage();
									if(!isInit){
										msg1.what = MyGameMidlet.GET_ADV_VERSION_UPDATE;  
										handler.sendMessage(msg1);
									}else{
										msg1.what = MyGameMidlet.GET_LOBBY_ADV_SUCCESS;
										handler.sendMessage(msg1);
									}
									
									Log.v(TAG, " adv config xml is need update");
								}else{ // 文件下载错误
									Message msg1 = handler .obtainMessage();
									msg1.what = MyGameMidlet.GET_GAME_VERSION_FAIL;  
									Log.v(TAG, " adv config xml is failed");
									handler.sendMessage(msg1);
								}
							}
						}

					}
					//本地文件不存在，直接从网络获取
					else
					{
						Log.v(TAG, "adv config xml read from http");
						String url = Configs.getConfigXmlUrl(AppConfig.CONFIG_CMD_ADV);
						String wapstr=Configs.getConfigXmlByHttp(url);
						if(wapstr!=null){		
							if(parseNotifiyXml(wapstr)){
								Configs.saveToFile(ADVPATH,wapstr);//将内容保存文件
								msg.what = MyGameMidlet.GET_LOBBY_ADV_SUCCESS;  
								
							}else{
								msg.what = MyGameMidlet.GET_LOBBY_ADV_FAIL;  
							}
							handler.sendMessage(msg);
						}		

					}

					Log.v(TAG, "serviceHttpGetReqThread adv xml is finished");
				}
			}.start();

		}

		/**
		 * 获取游戏详情图片
		 */		
		public void serviceHttpGetGameInfo(final String url,final Handler handler){
			new Thread(){
				@Override
				public void run(){
					String filename=GAMEINFOPATH+Configs.getParmFormUrl(url);
					Message msg = handler.obtainMessage();  
					//游戏详情版本XML
					String local=Configs.readFromFile(filename);//配置文件读取配置，判读服务器是否有更新
					String localmd5=Configs.md5(local);
					if(!Configs.isEmptyStr(local))
					{
						Log.v(TAG, "gameinfo config xml read from local file");
						if(parseNotifiyXml(local)){
							msg.what = MyGameMidlet.GET_GAMEINFO_SUCCESS;  						
						}else{
							msg.what = MyGameMidlet.GET_GAMEINFO_FAIL; 
							Log.e(TAG, "parseNotifiyXml gameinfo config xml fail");
						}
						handler.sendMessage(msg);

						String wapstr=Configs.getConfigXmlByHttp(url);
						if(!Configs.isEmptyStr(wapstr)){												  						
							String wapmd5=Configs.md5(wapstr);
							if(!localmd5.equals(wapmd5)){
								Configs.saveToFile(filename,wapstr);//将内容保存文件	
								if(parseNotifiyXml(wapstr)){
									Message msg1 = handler .obtainMessage();
									msg1.what = MyGameMidlet.GET_GAMEINFO_VERSION_UPDATE;  
									handler.sendMessageDelayed(msg1, 10000);
									Log.e(TAG, "game config xml is need update");
								}

							}
						}

					}
					//本地文件不存在，直接从网络获取
					else
					{
						Log.v(TAG, "gameinfo config xml read from http");
						String wapstr=Configs.getConfigXmlByHttp(url);
						if(!Configs.isEmptyStr(wapstr)){		
							Configs.saveToFile(filename,wapstr);//将内容保存文件	
							if(parseNotifiyXml(wapstr)){
								msg.what = MyGameMidlet.GET_GAMEINFO_SUCCESS;  
							}else{
								msg.what = MyGameMidlet.GET_GAMEINFO_FAIL;  
							}
							handler.sendMessage(msg);
						}		

					}
					Log.v(TAG, "serviceHttpGetReqThread gameinfo xml is finished");
				}
			}.start();
		}


		/**
		 * http获取客服信息	
		 * @param handler
		 */
		public void serviceHttpGetCustomInfo(final Handler handler){
			//获取客服信息
			new Thread(){
				@Override
				public void run(){
					Message msg = handler.obtainMessage();  
					//游戏版本XML
					String local=Configs.readFromFile(CUSTOMINFO);//配置文件读取配置，判读服务器是否有更新
					String localmd5=Configs.md5(local);
					if(!Configs.isEmptyStr(local))
					{
						Log.v(TAG, "custom info config xml read from local file");
						if(parseNotifiyXml(local)){
							msg.what = MyGameMidlet.GET_CUSTOMINFO_SUCCESS;  						
						}else{
							msg.what = MyGameMidlet.GET_CUSTOMINFO_FAIL; 
							Log.e(TAG, "parseNotifiyXml custom info config xml fail");
						}
						handler.sendMessage(msg);
						
						String url = getNotifiyConfigXmlUrl(AppConfig.CONFIG_CUSTOM);
						String wapstr=Configs.getConfigXmlByHttp(url);
						if(wapstr!=null){												  						
							String wapmd5=Configs.md5(wapstr);
							if(!localmd5.equals(wapmd5)){
								Configs.saveToFile(CUSTOMINFO,wapstr);//将内容保存文件	
								if(parseNotifiyXml(wapstr)){
									Message msg1 = handler .obtainMessage();
									msg1.what = MyGameMidlet.GET_CUSTOMINFO_VERSION_UPDATE;  
									handler.sendMessageDelayed(msg1, 10000);
									Log.e(TAG, "custom info config xml is need update");
								}

							}
						}
						
					}
					//本地文件不存在，直接从网络获取
					else
					{
						Log.v(TAG, "custom info config xml read from http");	
						
						String url = getNotifiyConfigXmlUrl(AppConfig.CONFIG_CUSTOM);
						String wapstr=Configs.getConfigXmlByHttp(url);
						if(wapstr!=null){		
							Configs.saveToFile(CUSTOMINFO,wapstr);//将内容保存文件
							if(parseNotifiyXml(wapstr)){
								msg.what = MyGameMidlet.GET_CUSTOMINFO_SUCCESS;  
							}else{
								msg.what = MyGameMidlet.GET_CUSTOMINFO_FAIL;  
							}
						}else{
							msg.what = MyGameMidlet.GET_CUSTOMINFO_FAIL;
						}
						handler.sendMessage(msg);
					}

					Log.v(TAG, "serviceHttpGetReqThread custom info xml is finished");
				}
			}.start();
			
		}

		/**设置WIFI默认下载开关*/
		public void setBGUpdateState(boolean value){
			isBGUpdate=value;
			setBGUpdateValue(value);
		}

		/**获取WIFI默认下载开关*/
		public boolean getBGUpdateState(){
			isBGUpdate=getBGUpdateValue();		
			return isBGUpdate;

		}

		/**设置notifiy声音开关*/
		public void setNotifiySoundState(boolean value){
			isNotifiySound=value;
			setNotifiySoundValue(value);
		}

		/**获取notifiy声音开关*/
		public boolean getNotifiySoundState(){
			isNotifiySound=getNotifiySoundValue();
			return isNotifiySound;
		}


		/**设置notifiy震动开关*/
		public void setNotifiyVibrateState(boolean value){
			isNotifiyVibrate=value;
			setNotifiyVibrateValue(value);
		}

		/**获取notifiy震动开关*/
		public boolean getNotifiyVibrateState(){
			isNotifiyVibrate=getNotifiyVibrateValue();
			return isNotifiyVibrate;
		}


		/**
		 * HTTP Service下载接口
		 * @param savePath 下载文件保存路径
		 * @param url 下载文件URL
		 * @paramcls 调用service的activity类名
		 */

		public void startDownloadFile(int id,String url,String savePath,String md5,Handler handler,int what) {  	
			DownloadThread t=new DownloadThread(url,savePath,md5,handler,what);
			t.start();
			mDownloadMap.put(id, t);	

		}  

		public DownloadThread getDownloadThread(int id){
			return mDownloadMap.get(id);
		}

		public void clearDownloadThread(int id){
			mDownloadMap.remove(id);
		}


	}


	@Override 
	public void onCreate() {  
		super.onCreate();  
		Log.v(TAG, "GameLobbyService is onCreate");

		mContext=this;
		Configs.mContext=this;
		
		mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);  
		mAlarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		Configs.loadGameLobbyProperties(mContext);  //读取渠道号
		Configs.loadGameLobbyUrlConfig(); //初始化URL配置，从文件
		Configs.loadGamePlayerProperties(mContext); // 初始化游戏玩家参数
		//3 为启动类型
		AccountManager.getInstance().initialize(this,
				AccountManager.HTTP_LOGIN_TAG, LoginInfoManager.HALL_TAG, 0,
				AppConfig.channelId, AppConfig.childChannelId,
				Configs.getVersionName(mContext), 0, (byte) 0,
				Configs.getICCID(this));
	}  

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		Log.v(TAG, "MykjService is onStartCommand");
		String action="";
		if(intent!=null){
			action=intent.getAction();
		}		
		Log.v(TAG,"onStartCommand action="+action);
		/*if(action.equals("mykj.service.NOTIFIY_MSG")){	
			Log.v(TAG, "mykj.service.NOTIFIY_MSG");
			String notifiy=Configs.readFromFile(NOTIFIYPATH);//从本地文件获取
			NotifiyMsgList.clear();
			if(parseNotifiyXml(notifiy)){
				beginNotifiyMsg();
			}

		}*/
		if(action.equals("mykj.service.JUST_BOOT_SERVICE")){
			setWatchdog(mAlarmManager);
		}else if(action.equals("mykj.service.BOOT_SERVICE")){
			setWatchdog(mAlarmManager);

			if(Configs.isConnect(mContext)){  // 网络连接OK
				serviceHttpGetReqThread();
			}
			setNotifyWatchdog(5000);
		}
		return START_NOT_STICKY;
	}

	
	
	
	/**
	 * 定时创建服务
	 * */
	private void setNotifyWatchdog(long mill) {

		AlarmManager mAlarmManager =(AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent();
		intent.setAction("mykj.game.intent.action.ALARM_BOOT_BROADCAST");
		PendingIntent pi=PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		long timeNow = SystemClock.elapsedRealtime();
		long nextCheckTime = timeNow + mill; //下次启动的时间		
		mAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, nextCheckTime, pi);
		//setReqBroadcastDate();
		Log.v(TAG, "注册广播发送PendingIntent");
	}
	
	
	/**
	 * 定时创建服务
	 * */
	private void setWatchdog(AlarmManager alarmMgr) {
		Intent intent = new Intent();
		intent.setClass(mContext, MykjReceiver.class);
		intent.setAction("mykj.intent.action.ALARM_BOOT_BROADCAST");
		PendingIntent pi=PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		long timeNow = SystemClock.elapsedRealtime();
		long nextCheckTime = timeNow +getConnectFrequency(); //下次启动的时间		
		alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, nextCheckTime, pi);
		Log.v(TAG, "定时注册广播发送PendingIntent");
	}

	@Override
	public void onDestroy(){
		Log.v(TAG, "GameLobbyService is onDestroy");
		AppConfigList.clear();
		super.onDestroy();
	}






	/**
	 * 创建新线程，从服务器上获取配置文件
	 */
	private void serviceHttpGetReqThread(){

		//getNotifyMsgConfigXml();	
		if(isAllowReqAppVersionXml()&&isWifiBGUpdate()){
			getGameVersionForWifiUpdate();
			getLobbyVersionForWifiUpdate();
		}
	}



	/**
	 * 获取通知消息配置文件
	 */
	private void getNotifyMsgConfigXml(){
		new Thread(){
			@Override
			public void run(){
				Message msg = mServiceHandler.obtainMessage();  

				String parsestr="";					
				//游戏版本XML
				String local=Configs.readFromFile(NOTIFIYPATH);//配置文件读取配置，判读服务器是否有更新
				String localmd5=Configs.md5(local);

				String url = getNotifiyConfigXmlUrl(AppConfig.CONFIG_MSG);  			
				Log.v(TAG, "getNotifiyConfigXmlUrl="+url);
				
				String wapstr=Configs.getConfigXmlByHttp(url);
				String wapmd5=Configs.md5(wapstr);

				if(!Configs.isEmptyStr(wapstr)){	//网络数据非空											  						
					if(!localmd5.equals(wapmd5)){
						Configs.saveToFile(NOTIFIYPATH,wapstr);//将内容保存文件				
					}
					parsestr=wapstr;
				}else if(!Configs.isEmptyStr(local)){//本地数据非空	
					parsestr=local;
				}else{
					Log.v(TAG, "notify wap & local xml is null");
					return ;
				}

				if(parseNotifiyXml(parsestr)){
					msg.what = GET_NOTIFIY_XML_SUCCESS;  						
				}else{
					msg.what = GET_NOTIFIY_XML_FAIL; 
					Log.e(TAG, "parseNotifiyXml game version xml fail");
				}					

				mServiceHandler.sendMessage(msg);
				Log.v(TAG, "getNotifyMsgConfigXml  xml is finished");
			}
		}.start();

	}

	/**
	 * 获取大厅中游戏的版本配置
	 */
	private void getGameVersionForWifiUpdate(){
		//获取大厅中游戏的版本配置
		new Thread(){
			@Override
			public void run(){
				Message msg = mServiceHandler.obtainMessage();  
				if(AppConfigList.isEmpty()){    //AppConfigList 非空 直接使用
					String parsestr="";					
					//游戏版本XML
					String local=Configs.readFromFile(APPVERSIONPATH);//配置文件读取配置，判读服务器是否有更新
					String localmd5=Configs.md5(local);

					String url = Configs.getConfigXmlUrl(AppConfig.CONFIG_CMD_GAMES);
					String wapstr=Configs.getConfigXmlByHttp(url);
					String wapmd5=Configs.md5(wapstr);

					if(!Configs.isEmptyStr(wapstr)){	//网络数据非空											  						
						if(!localmd5.equals(wapmd5)){
							Configs.saveToFile(APPVERSIONPATH,wapstr);//将内容保存文件						
						}
						parsestr=wapstr;
					}else if(!Configs.isEmptyStr(local)){//本地数据非空	
						parsestr=local;
					}else{
						Log.v(TAG, "wap & local xml is null");
						return ;
					}

					if(parseNotifiyXml(parsestr)){
						msg.what = GET_GAMEVERSION_XML_SUCCESS;  						
					}else{
						msg.what = GET_GAMEVERSION_XML_FAIL; 
						Log.e(TAG, "parseNotifiyXml game version xml fail");
					}					
				}else{
					msg.what = GET_GAMEVERSION_XML_SUCCESS; 
				}
				mServiceHandler.sendMessage(msg);
				Log.v(TAG, "getGameVersionForWifiUpdate  xml is finished");
			}
		}.start();
	}


	
	/**
	 * 获取大厅版本配置
	 */
	private void getLobbyVersionForWifiUpdate(){
		//获取大厅中游戏的版本配置
		new Thread(){
			@Override
			public void run(){
				Message msg = mServiceHandler.obtainMessage();  	
				String parsestr="";					
				//游戏版本XML
				String local=Configs.readFromFile(LOBBYPATH);//配置文件读取配置，判读服务器是否有更新
				String localmd5=Configs.md5(local);

				String url = Configs.getConfigXmlUrl(AppConfig.CONFIG_CMD_LOBBY);
				String wapstr=Configs.getConfigXmlByHttp(url);
				String wapmd5=Configs.md5(wapstr);

				if(!Configs.isEmptyStr(wapstr)){	//网络数据非空											  						
					if(!localmd5.equals(wapmd5)){
						Configs.saveToFile(LOBBYPATH,wapstr);//将内容保存文件						
					}
					parsestr=wapstr;
				}else if(!Configs.isEmptyStr(local)){//本地数据非空	
					parsestr=local;
				}else{
					Log.v(TAG, "wap & local xml is null");
					return ;
				}

				if(parseNotifiyXml(parsestr)){
					msg.what = GET_LOBBY_XML_SUCCESS;  						
				}else{
					msg.what = GET_LOBBY_XML_FAIL; 
					Log.e(TAG, "parseNotifiyXml lobby version xml fail");
				}					

				mServiceHandler.sendMessage(msg);
				Log.v(TAG, "getLobbyVersionForWifiUpdate  xml is finished");
			}
		}.start();
	}
	


	

	/**
	 * 从服务器获取系统通知信息
	 * */
	private void beginNotifiyMsg()
	{
		if (isConnectTime())// 成功解析从http取到的xml
		{
			//消息发送次数逻辑判断移到后面notify时候
//			int sendTimes=getNotifiyTimesEveryday();  //一天实际发送的次数
//			int allowTimes=getXmlNotifiyTimes();  //一天允许发送的次数
//			if(sendTimes>=allowTimes){
//				Log.v(TAG, "当天消息发送条数达到上限!!!!");
//				return;   //超过发送次数 返回
//			}

			Message msg = mServiceHandler.obtainMessage();  		
			msg.what = SERVICE_NOTIFIY_MSG; 
			mServiceHandler.sendMessage(msg);
			//notifiyQueueMsg();//notifiy活动

		}

	}


	/**
	 * 后台更新移动棋牌大厅版本
	 * @param xmlStr
	 */
	private void getLobbyVersionFromXml(){
		if (isWifiBGUpdate()&&mlobbyVersion!=null)// 成功解析从http取到的xml
		{
			if(mlobbyVersion.isNeedUpdate()){
				String url=mlobbyVersion.getDownloadUrl();
				String downloadPath=AppVersion.getDownloadPath();
				Intent intent=mlobbyVersion.getActionType();
				String md5=mlobbyVersion.getDownFileConfigMD5();
				String title="移动棋牌";
				String context="移动棋牌wifi自动更新版本完成";				
				DownloadThread t=new DownloadThread(url,downloadPath,md5,intent,true,title,context);
				t.start();
			}
		}
	}



	/**
	 * 后台下载更新,从APP队列中分析可更新和后台默认下载游戏
	 * */

	private void downLoadAPKBackGround(){
		//保存游戏ID
		StringBuilder sb=new StringBuilder();
		for(AppVersion entry:AppConfigList){
			if(entry.isAppInstalled()){
				int id=entry.getGameId();
				sb.append(id);
				sb.append('_');
			}

			//没有安装 ，后台下载
			if(!entry.isAppInstalled()){
				if(entry.isUpdateComplete()){
					Log.v(TAG, "game id="+entry.getGameId()+"已经下载成功了不用再下载");
					continue;
				}
				Log.v(TAG, "game id="+entry.getGameId()+"开始下载");
				String url=entry.getDownloadUrl();
				String downloadPath=AppVersion.getDownloadPath();
				Intent intent=entry.getActionType();
				String md5=entry.getDownFileConfigMD5();

				String title=entry.getUpdateTitle();
				String context=entry.getUpdateContext();

				int gameid=entry.getGameId();  
				DownloadThread downloading=mDownloadMap.get(gameid);
				if(downloading==null){  //判断前台没有下载
					DownloadThread t=new DownloadThread(url,downloadPath,md5,intent,false,title,context);
					t.start();
				}
			}
			//已安装 ，后台更新
			else if(entry.isAppInstalled()&&entry.isNeedUpdate()){
				//已经下载过了
				if(entry.isUpdateComplete()){
					Log.v(TAG, "game id="+entry.getGameId()+"已经下载成功了不用再更新");
					continue;
				}
				Log.v(TAG, "game id="+entry.getGameId()+"开始更新");
				//后台下载
				String url=entry.getDownloadUrl();
				String downloadPath=AppVersion.getDownloadPath();
				Intent intent=entry.getActionType();;
				String md5=entry.getDownFileConfigMD5();
				String title=entry.getUpdateTitle();
				String context=entry.getUpdateContext();

				int gameid=entry.getGameId();  
				DownloadThread downloading=mDownloadMap.get(gameid);
				if(downloading==null){  //判断前台没有下载
					DownloadThread t=new DownloadThread(url,downloadPath,md5,intent,true,title,context);
					t.start();
				}
			}
		}

		//保存安装游戏ID
		String gamesId=sb.toString().trim();
		if(!Configs.isEmptyStr(gamesId)){
			byte[] ids=gamesId.getBytes();
			if(ids.length>1){
				String str=new String(ids,0,ids.length-1);
				Configs.setStringSharedPreferences(mContext,"gameid",str);
			}
		}

	}


	/**
	 * 从消息队列发送可以弹出的消息
	 * */
	private void notifiyQueueMsg() {
		Log.v(TAG, "notifiyQueueMsg...");
		for(NotifiyMsg msg:NotifiyMsgList){
			if(msg.getNotifiyTime()&& msg.isAllowNotifiySendTime()){				
				int NotifiyId=msg.getID();
				Log.v(TAG, "notifiy msgID="+NotifiyId);

				if(!isNeedNotifiyMsg()){
					Log.v(TAG, "当天不允许发送消息!!!!");
					return;   //超过发送次数 返回
				}

				mNotificationManager.notify(NotifiyId, msg.getMsgNotification());
				setNotifiyTimesEveryday();   //保存一天的发送次数
				msg.setMsgNotifiySendTime(); //保存消息发送时间
			}
		}

		NotifiyMsgList.clear();
	}



	/**
	 * 解析xml数据
	 */
	public boolean parseNotifiyXml(String strXml)
	{
		boolean isParseSuccess = false;
		if(strXml==null){
			return isParseSuccess;
		}
		try
		{
			// 定义工厂
			XmlPullParserFactory f = XmlPullParserFactory.newInstance();
			// 定义解析器
			XmlPullParser p = f.newPullParser();
			// 获取xml输入数据
			p.setInput(new StringReader(strXml));
			// 解析事件
			int eventType = p.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT)
			{
				switch (eventType)
				{
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					String tagName = p.getName();
					if(tagName.equals("ctrl"))
					{
						mTspan = p.getAttributeValue(null, "tspan");  //连接服务器频率
						mMax = p.getAttributeValue(null, "max");
						mDft=p.getAttributeValue(null, "dft");
						mDet=p.getAttributeValue(null, "det");
						writeConnectRule(); //保存服务器连接规则
					}
					else if (tagName.equals("msg"))
					{						
						NotifiyMsg gc = new NotifiyMsg(mContext, p);
						NotifiyMsgList.add(gc);	
						isParseSuccess =true;
					}
					else if (tagName.equals("lobby"))
					{						
						mlobbyVersion=new LobbyVersion(mContext, p);
						isParseSuccess =true;

					}
					else if (tagName.equals("game"))
					{	
						boolean replease=false;					
						AppVersion gc = new AppVersion(mContext, p);
						if(AppConfigList.isEmpty()){
							AppConfigList.add(gc);	
						}else{
							for(int i=0,j=AppConfigList.size();i<j;i++){
								int id=AppConfigList.get(i).getGameId();
								if(gc.getGameId()==id){
									AppConfigList.set(i, gc);
									replease=true;
									break;
								}

							}	
							if(!replease){
								AppConfigList.add(gc);		
							}
						}

						isParseSuccess =true;
					}			
					else if(tagName.equals("custom"))
					{
						String phoneNum=p.getAttributeValue(null, "phoneNum");
						String qqNum = p.getAttributeValue(null, "qqNum");  
						String email=p.getAttributeValue(null, "email");
						String qqGroup=p.getAttributeValue(null, "qqGroup");
						HelpInfo.getInstance().init(phoneNum, qqNum, email, qqGroup);
						
						Configs.setStringSharedPreferences(mContext,"phoneNum",phoneNum);
						Configs.setStringSharedPreferences(mContext,"qqNum",qqNum);
						Configs.setStringSharedPreferences(mContext,"email",email);
						Configs.setStringSharedPreferences(mContext,"qqGroup",qqGroup);
						
						isParseSuccess =true;
					}
					else if(tagName.equals("ad"))
					{
						String serverAdVer =p.getAttributeValue(null, "ver");
						if(serverAdVer!=null&&Configs.isMediaMounted()){
							mAdvPath=Configs.getSdcardPath() + AppConfig.ADS_PATH+serverAdVer;
						}
						adList.clear();

					}
					else if(tagName.equals("adImg"))
					{
						int adSTime = 2000;
						try
						{
							adSTime = Integer.parseInt(p.getAttributeValue(null, "sTime"));
						}
						catch(Exception e)
						{

						}
						String url = p.getAttributeValue(null, "url");
						String onClick = p.getAttributeValue(null, "onClick");
						AdvItem adv = new AdvItem(mContext,adSTime, url, onClick);
						adList.add(adv);						
						isParseSuccess =true;
					}

					else if(tagName.equals("GameInfo"))
					{
						mFolder=p.getAttributeValue(null, "name");					
						mGameInfoDes=p.getAttributeValue(null, "des");
						String ver=p.getAttributeValue(null, "ver");

						if(mFolder!=null&&ver!=null&&Configs.isMediaMounted()){
							StringBuilder sb=new StringBuilder();
							sb.append(Configs.getSdcardPath());
							sb.append(AppConfig.GAMEINFO_PATH);
							sb.append("/");
							sb.append(mFolder);
							sb.append(ver);
							mGameInfoPath=sb.toString().trim();
						}
						gameInfoList.clear();


					}
					else if(tagName.equals("GameInfoImg"))
					{
						String url = p.getAttributeValue(null, "url");
						GameInfoItem item = new GameInfoItem(mContext,url);
						gameInfoList.add(item);
						isParseSuccess =true;
					}
					else{
						isParseSuccess = false;
					}

					break;
				case XmlPullParser.END_TAG:
					break;
				case XmlPullParser.END_DOCUMENT:
					break;
				default:
					break;
				}
				// 用next方法处理下一个事件，否则会造成死循环。
				eventType = p.next();
			}
		}
		catch (Exception e)
		{
			Log.v(TAG, "parse xml error");
			isParseSuccess = false;
		}
		return isParseSuccess;
	}



	/**
	 * 
	 * @return 获取广告图片保存目录
	 */
	public String getAdvPath(){		
		return mAdvPath;
	}	


	public String getFolder(){
		return mFolder;
	}

	/**
	 * 
	 * @return 获取游戏详细图片保存目录
	 */
	public String getGameInfoPath(){
		return mGameInfoPath;
	}




	/**
	 * 当前是否可以检测系统版本，每天只检测一次
	 * true 可以检测
	 * */
	private boolean isAllowReqAppVersionXml(){
		Time t = new Time();
		t.setToNow(); //取得系统时间
		int date = t.monthDay;

		if((date-getReqAppVerDate())>0){
			Log.v(TAG, "http can req version ");
			return true;
		}else{
			Log.v(TAG, "http can't req version ");
			return false;
		}

	}


	/**
	 * 获取上次检测游戏版本的日期时间，1-31号
	 * */
	private int getReqAppVerDate(){
		return Configs.getIntSharedPreferences(mContext,"appverDate",0);
	}

	/**
	 * 保存本次检测游戏版本的日期时间，1-31号
	 * */
	private void setReqAppVerDate(){
		Time t = new Time();
		t.setToNow(); //取得系统时间
		int date = t.monthDay;
		Configs.setIntSharedPreferences(mContext,"appverDate",date);

	}






	/**
	 * 保存连接服务器规则
	 * */
	private void writeConnectRule(){
		if(mTspan!=null){
			Configs.setStringSharedPreferences(mContext,"tspan",mTspan);
		}

		if(mMax!=null){
			Configs.setStringSharedPreferences(mContext,"max",mMax);
		}

		if(mDft!=null){
			Configs.setStringSharedPreferences(mContext,"dft",mDft);
		}

		if(mDet!=null){
			Configs.setStringSharedPreferences(mContext,"det",mDet);
		}
	}



	/**
	 * 获取连接服务器频率
	 * */
	private  long getConnectFrequency(){
		long res=AppConfig.WATCHDOG_DELAY;
		//Random rand = new Random();
		//int i = rand.nextInt(10)-5; //生成(-5,+5)以内的随机数
		if(res==0){
			String timeStr=Configs.getStringSharedPreferences(mContext,"tspan","0");
			long time=Long.parseLong(timeStr);//请求时间间隔单位 小时   (1000*60);
            long min=24*60;   //请求最小时间间隔为一天  （转换为分钟）
            long max=7*24*60; //请求最大时间间隔为七天 （转换为分钟）
			if(time<min){
				time=min;            
			}else if(time>max){
				time=max;           
			}

			res=time*60*1000;
		}else{
			res*=60*1000;     //配置文件 时间间隔为分钟
		}
		if(!Configs.isConnect(mContext)){
			res=AppConfig.DEFAULT_FQCY;
		}
		return res;
	}


	private boolean isNeedNotifiyMsg(){
		Time t = new Time();
		t.setToNow(); //取得系统时间	
		int month=t.month;
		int date = t.monthDay;  //获取当天日期

		int sendTimes=getNotifiyTimesEveryday();  //一天实际发送的次数
		int allowTimes=getXmlNotifiyTimes();  //一天允许发送的次数

		int senddate=month*100+date;  //生成唯一的日期标示	
		int getDate=Configs.getIntSharedPreferences(mContext,"monthDay",0); //获取保存的最近发送日期
		if((senddate-getDate)!=0){
			Log.v(TAG, "消息已经不在当天了");
			return true;
		}

		if(sendTimes>=allowTimes){
			Log.v(TAG, "当天消息发送条数达到上限!!!!");
			return false;
		}
		return true;
	}



	/**
	 * 保存消息弹出的当前次数
	 * */
	private void setNotifiyTimesEveryday(){
		Time t = new Time();
		t.setToNow(); //取得系统时间	
		int month=t.month;
		int date = t.monthDay;  //获取当天日期

		int senddate=month*100+date;  //生成唯一的日期标示

		int getDate=Configs.getIntSharedPreferences(mContext,"monthDay",0); //获取保存的最近发送日期


		if(getDate==0){//第一次保存发送的日期
			Configs.setIntSharedPreferences(mContext,"monthDay",senddate);
			Configs.setIntSharedPreferences(mContext,"notifiytimes",1);//保存当天发生次数		
		}else if((senddate-getDate)==0){//消息在当天

			int maxStr=Configs.getIntSharedPreferences(mContext,"notifiytimes",0);
			int atTimes=maxStr+1;
			Configs.setIntSharedPreferences(mContext,"notifiytimes",atTimes);

		}else if((date-getDate)!=0){//已经超过了一天
			Configs.setIntSharedPreferences(mContext,"monthDay",senddate); //保存发送的日期
			Configs.setIntSharedPreferences(mContext,"notifiytimes",1);//保存当天发生次数			
		}
	}


	/**
	 * 获取消息已经弹出的次数
	 * */
	private int getNotifiyTimesEveryday(){

		return Configs.getIntSharedPreferences(mContext,"notifiytimes",0);

	}

	/**
	 * 获取配置文件消息弹出次数
	 * */
	private int getXmlNotifiyTimes(){
		if(mMax!=null){
			int max=Integer.parseInt(mMax);
			return max;

		}else{
			String maxStr=Configs.getStringSharedPreferences(mContext,"max","0");
			int atTimes=Integer.parseInt(maxStr);
			return atTimes;
		}


	}



	/**
	 * 获取连接服务器时间是否允许
	 * */
	private boolean isConnectTime(){
		String beginStr;
		String endStr;
		if(mDft==null&&mDet==null){
			beginStr=Configs.getStringSharedPreferences(mContext,"dft","");
			endStr=Configs.getStringSharedPreferences(mContext,"det","");
		}else{
			beginStr=mDft;
			endStr=mDet;
		}
		Log.v(TAG,"isConnectTime beginStr="+beginStr);
		Log.v(TAG,"isConnectTime endStr="+endStr);

		if(beginStr==""||endStr==""){
			Log.v(TAG, "Connect time is allowed");
			return true;
		}


		Date date=new Date();
		SimpleDateFormat df=new SimpleDateFormat("HH:mm");			
		String time=df.format(date);

		Log.v(TAG,"isConnectTime now time="+time);

		if(beginStr.compareTo(endStr)<0){
			Log.v(TAG,"beginStr 小于 endStr");
			if((time.compareTo(beginStr)>0)&&(time.compareTo(endStr)<0)){
				Log.v(TAG, "Connect time is refused");
				return false;
			}else{
				Log.v(TAG, "Connect time is allowed");
				return true;
			}
		}else{
			Log.v(TAG,"beginStr 大于 endStr");
			if((time.compareTo(beginStr)>0)||(time.compareTo(endStr)<0)){
				Log.v(TAG, "Connect time is refused");
				return false;
			}else{
				Log.v(TAG, "Connect time is allowed");
				return true;
			}
		}

	}


	/**
	 * 获取notifiy URL地址
	 * */
	private  String getNotifiyConfigXmlUrl(String cmd)
	{
		StringBuffer sb = new StringBuffer();

		sb.append(AppConfig.MSG_URL);

		sb.append(cmd);
		sb.append("&");

		sb.append("Token");
		sb.append("=");
		sb.append(getToken());
		sb.append("&");

		sb.append("IMSI");
		sb.append("=");
		sb.append(Configs.getImsiDirectly(mContext));
		sb.append("&");

		sb.append("PID");
		sb.append("=");
		sb.append(AppConfig.channelId);
		sb.append("&");

		sb.append("fid");
		sb.append("=");
		sb.append(AppConfig.fid);
		sb.append("&");

		sb.append("m");
		sb.append("=");
		sb.append(Configs.getDevice());
		sb.append("&");

		sb.append("v");
		sb.append("=");
		sb.append(Configs.getOSVerion());
		sb.append("&");

		sb.append("game");
		sb.append("=");
		sb.append(getReportGameId()); 
		sb.append("&");

		sb.append("opid");
		sb.append("=");
		sb.append(Configs.getOPID(mContext)); 

		return sb.toString();
	}

	/**
	 * 获取游戏是否安装
	 * */
	private String getReportGameId(){
		String str= Configs.getStringSharedPreferences(mContext,"gameid","");
		return str;
	}



	/**获取登陆token*/
	private String getToken(){
		 String token="";
		 LoginInfoManager.initManager(mContext, LoginInfoManager.HALL_TAG);
		 LoginInfoManager.getInstance().loadNativeLoginInfo();
		 String str=LoginInfoManager.getInstance().getToken();
		 if(!Configs.isEmptyStr(str)){
			 token=str;
		 }
		 return URLEncoder.encode(token);
	}

	

	/**
	 * wifi和wifi默认更新 是否打开
	 * */
	private boolean isWifiBGUpdate(){
		boolean res=false;
		if(getBGUpdateValue() && Configs.isWiFiActive(mContext)){
			res=true;
		}
		return res;
	}

	
	/********************************/
	//设置状态保存

	/**
	 * 保存后台wifi自动更新设置状态
	 * @param b
	 */
	private void setBGUpdateValue(boolean b){
		Configs.setBooleanSharedPreferences(mContext,"wifiupdate",b);
	}

	/**
	 * 获取后台wifi自动更新设置状态
	 * @return true 打开，false 关闭
	 */
	public boolean getBGUpdateValue(){
		return Configs.getBooleanSharedPreferences(mContext,"wifiupdate",true);
	}


	/**
	 * 保存notifiy 声音状态
	 * @param b
	 */
	public void setNotifiySoundValue(boolean b){
		Configs.setBooleanSharedPreferences(mContext,"notifiysound",b);
	}


	/**
	 * 设置notifiy 声音状态
	 * @param b
	 */
	public boolean getNotifiySoundValue(){
		return Configs.getBooleanSharedPreferences(mContext,"notifiysound",false);
	}

	/**
	 * 保存notifiy 声音状态
	 * @param b
	 */
	public void setNotifiyVibrateValue(boolean b){
		Configs.setBooleanSharedPreferences(mContext,"notifiyvibrate",b);
	}


	/**
	 * 设置notifiy 声音状态
	 * @param b
	 */
	public boolean getNotifiyVibrateValue(){
		return Configs.getBooleanSharedPreferences(mContext,"notifiyvibrate",false);
	}

	

	/**
	 * 下载线程内部类
	 * */
	public class DownloadThread extends Thread{
		private String mUrl;
		private String mDownLoadPath;
		private String mMd5;
		private Intent mIntent;
		private String mDownLoadFileName;
		private boolean isNotifiy=false;
		public int param;  
		private int NOTIFY_ID; 
		private boolean cancelled=false;
		private Handler mHandler=null;
		private Notification mThreadNotification;
		private String notifyTitle;
		private String notifyContext;
		/**
		 * 下载线程构造函数
		 * @param url 下载URL
		 * @param downloadpath 下载文件保存路径
		 * @param cls 下载完成后，notifiy提示，点击进入的class
		 * @param nm
		 */
		public DownloadThread(String url,String downloadpath,String md5,Intent intent,boolean isNotifiy,String title,String context){	
			mUrl=url;
			mDownLoadPath=downloadpath;									
			mDownLoadFileName =Configs.getFileNameFromUrl(url);//从URL中获取文件名
			mMd5=md5;
			mIntent=intent;
			this.isNotifiy=isNotifiy;
			notifyTitle=title;
			notifyContext=context;
			NOTIFY_ID=url.hashCode();
			initDownloadNotifity();
		}

		/**
		 * 
		 * @param url 下载文件url
		 * @param downloadpath 下载文件保存目录
		 * @param md5 下载文件校验md5
		 * @param handler 通知UI线程Handler
		 * @param param 参数
		 */
		public DownloadThread(String url,String downloadpath,String md5,Handler handler,int param){	
			mUrl=url;
			mDownLoadPath=downloadpath;									
			mDownLoadFileName =Configs.getFileNameFromUrl(url);//从URL中获取文件名
			mMd5=md5;
			mHandler=handler;
			this.param=param;
		}

		@Override  
		public void run() { 			
			startDownload(mUrl,mDownLoadPath);  
		};  

		/** 
		 * 下载模块 
		 */  

		private void startDownload(String url,String downloadpath) {  
			int res=-1;
			cancelled = false;  
			Log.v(TAG, "startDownload...");
			File path=new File(downloadpath);
			if(!path.exists()){
				path.mkdirs();
			}

			File downloadFile=new File(downloadpath+"/"+mDownLoadFileName);
			if(downloadFile.exists()){
				//已经存在 不用下载
				Log.v(TAG, "file is exist,don't download");				
				return;
			}

			String downLoadFileTmpName=mDownLoadFileName+".tmp";   //设置下载的临时文件名
			String downLoadFileTmpPath=downloadpath+"/"+downLoadFileTmpName;

			File tmpFile=new File(downLoadFileTmpPath);

			try {
				HttpParams httpParams = new BasicHttpParams();
				HttpConnectionParams.setSoTimeout(httpParams, 30000);

				HttpGet httpGet = new HttpGet(url);

				long startPosition=tmpFile.length();  //已下载的文件长度
				String start = "bytes=" + startPosition + "-";			
				httpGet.addHeader("Range",start);

				HttpClient httpClient = new DefaultHttpClient(httpParams);
				HttpResponse httpResponse = httpClient.execute(httpGet);
				if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK
						||httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_PARTIAL_CONTENT)
				{
					HttpEntity entity = httpResponse.getEntity();
					long length = entity.getContentLength();//请求文件长度


					InputStream inputStream = entity.getContent();
					byte[] b = new byte[1024];
					int readedLength = -1;

					OutputStream outputStream = new FileOutputStream(tmpFile,true);

					long percentile=(length+startPosition)/20;   //文件每下载5%的长度
					int rate=0;
					int count=0;


					while(((readedLength = inputStream.read(b)) != -1)){
						if(cancelled){
							inputStream.close();
							outputStream.close();
							if(mHandler!=null){
								Message msg=mHandler.obtainMessage();								
								msg.what=MyGameMidlet.SERVICE_DOWNLOAD_CANCLE;														
								Bundle bundle=new Bundle();																
								bundle.putInt("position", param);
								bundle.putInt("rate", rate);
								msg.setData(bundle);
								mHandler.sendMessage(msg);							
							}
							return;
						}

						outputStream.write(b, 0, readedLength);
						startPosition += readedLength;

						if (startPosition>=percentile)  //每下载5%，计算进度条
						{
							count=(int)(startPosition/percentile)*5;	
							Log.v(TAG, "count="+count);
							rate+=count;

							startPosition=0;
							count=0;

							if(mHandler!=null){
								Message msg=mHandler.obtainMessage();
								msg.what=MyGameMidlet.UPDATE_DOWNLOAD_PROGRESS;								
								Bundle bundle=new Bundle();																
								bundle.putInt("position", param);
								bundle.putInt("rate", rate);
								msg.setData(bundle);
								mHandler.sendMessage(msg);
							}

							Log.v(TAG, "文件已下载"+rate+"%");

						}
					}
					//设置进度条100%
					if(mHandler!=null){
						Message msg=mHandler.obtainMessage();
						msg.what=MyGameMidlet.UPDATE_DOWNLOAD_PROGRESS;		
						Bundle bundle=new Bundle();																
						bundle.putInt("position", param);
						bundle.putInt("rate", 100);
						msg.setData(bundle);
						mHandler.sendMessage(msg);
					}

					inputStream.close();
					outputStream.close();

					//下载文件MD5检测
					if(Configs.downloadFileMD5Check(tmpFile, mMd5)){
						Log.v(TAG, "download file md5 check success");
						tmpFile.renameTo(downloadFile);
						res=0;
						if(isNotifiy){
							mThreadNotification.flags = Notification.FLAG_AUTO_CANCEL;   
							PendingIntent contentIntent = PendingIntent.getActivity(mContext, NOTIFY_ID, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);  
							mThreadNotification.setLatestEventInfo(mContext,getString(R.string.app_name), notifyContext, contentIntent);  

							mNotificationManager.notify(NOTIFY_ID, mThreadNotification);

						}												
					}else{
						Log.e(TAG, "download file md5 check fail");
						tmpFile.delete();
						res=1;
					}

				}
			} catch (ClientProtocolException e) {		
				res=2;
				Log.e(TAG, "file download fail");
			} catch (IOException e) {
				res=3;
				Log.e(TAG, "file download fail");
			}
			if(mHandler!=null){
				Message msg=mHandler.obtainMessage();
				String str=downloadFile.getPath();
				Bundle bundle=new Bundle();
				bundle.putInt("position", param);
				bundle.putString("filepath", str);
				msg.setData(bundle);
				if(res==0){
					msg.what=MyGameMidlet.SERVICE_DOWNLOAD_SUCCESS;
				}else{
					msg.what=MyGameMidlet.SERVICE_DOWNLOAD_FAIL;
					
				}
				cancelled = true; 
				mHandler.sendMessage(msg);
			}
		}  


		/** 
		 * 创建正在下载的notifiy通知 
		 */  
		@SuppressWarnings("deprecation")
		private void initDownloadNotifity() {  
			int icon =R.drawable.app_launcher;  
			String tickerText=getString(R.string.app_name);	  
			long when = System.currentTimeMillis();  

			mThreadNotification=new Notification(icon,tickerText,when);
			mThreadNotification.flags = Notification.FLAG_ONGOING_EVENT;  


			RemoteViews contentView = new RemoteViews(mContext.getPackageName(), R.layout.download_notifiy);  
			contentView.setTextViewText(R.id.fileName, notifyContext);  
			// 指定个性化视图  
			mThreadNotification.contentView = contentView; 

			// 指定内容意图  
			PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);  
			mThreadNotification.contentIntent = contentIntent;  

			Log.v(TAG, "init createDownloadingNotifity msg");
		}  

		/** 
		 * 取消下载 
		 */  
		public void cancel() {  

			cancelled = true;  
		}  

		/** 
		 * 是否已被取消 
		 *  
		 * @return 
		 */  
		public boolean isCancelled() {  
			return cancelled;  
		}  


	}


}
