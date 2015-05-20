package com.mykj.andr.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.cocos2dx.lib.Cocos2dxActivity;
import org.cocos2dx.util.GameUtilJni;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.Toast;

import com.login.utils.DensityConst;
import com.mingyou.distributor.NetPrivateListener;
import com.mingyou.distributor.NetSocketPak;
import com.mingyou.login.LoginSocket;
import com.mingyou.login.SocketLoginListener;
import com.minyou.android.net.HttpConnector;
import com.minyou.android.net.IRequest;
import com.minyou.android.net.NetService;
import com.mykj.andr.headsys.HeadManager;
import com.mykj.andr.model.GoodsItem;
import com.mykj.andr.model.HallDataManager;
import com.mykj.andr.model.RoomConfigData;
import com.mykj.andr.net.NetSocketManager;
import com.mykj.andr.pay.PayManager;
import com.mykj.andr.provider.GoodsItemProvider;
import com.mykj.andr.provider.VipXmlParser;
import com.mykj.andr.ui.fragment.CardZoneFragment;
import com.mykj.andr.ui.fragment.Cocos2dxFragment;
import com.mykj.andr.ui.widget.Interface.OnArticleSelectedListener;
import com.mykj.comm.io.TDataInputStream;
import com.mykj.comm.io.TDataOutputStream;
import com.mykj.comm.log.MLog;
import com.mykj.game.FiexedViewHelper;
import com.mykj.game.MainApplication;
import com.MyGame.Midlet.R;
import com.MyGame.Midlet.wxapi.WXUtil;
import com.mykj.game.ddz.api.AnalyticsUtils;
import com.mykj.game.ddz.api.PushManager;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.ChannelDataMgr;
import com.mykj.game.utils.Log;
import com.mykj.game.utils.MobileHttpApiMgr;
import com.mykj.game.utils.PreloadCocos2dRes;
import com.mykj.game.utils.URLUtils;
import com.mykj.game.utils.Util;
import com.mykj.game.utils.UtilHelper;
import com.umeng.analytics.MobclickAgent;

public class CustomActivity extends Cocos2dxActivity implements
		OnArticleSelectedListener {
	private static final String TAG = "CustomActivity";
	private CustomActivity mAct;

	private boolean isBinded=false;
	private Boolean isEntryRoomNoBind=false;
	
	/**
	 * 加载游戏so库
	 */
	static {
		// 下面是加载库文件
		System.loadLibrary("cocosdenshion");
		System.loadLibrary("ddz");
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setPackageName(getPackageName());
		AppConfig.UNIT = this.getResources().getString(R.string.lucky_ledou_2);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		MainApplication.sharedApplication().addActivity(this);
		mAct = this;
		AppConfig.mContext = this; // 提供游戏全局使用context,如果当前界面没有传入context
		// jni mContext
		GameUtilJni.init(this);
		// 初始化MLog
		try {
			MLog.init(MLog.ADNROID_PRINT, null, true);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (!AppConfig.debug) {
			MLog.setCurPrintLevel(MLog.CUSTOMLEVEL1);
		}

		//移动支付初始化
//		if(AppConfig.isOpenPayByCmccSdk()){
//		GameInterface.initializeApp(this);
//		}



//		 AppConfig.setIsOuterNet(false);
//		 AppConfig.setConnectIP("192.168.1.186");
//		 AppConfig.setConnectPort(7000);
		 
		/** 获取启动传入的bundle */
		initializeBundle(this);

		/** 下面是创建setContentView界面 */
		FiexedViewHelper.getInstance().init(this);
		HeadManager.getInstance(); // 需要在此初始化
		FiexedViewHelper.getInstance().loadCreateView(); //登录行为在此方法中..
		

		initConfig();
	}

	@Override
	protected void onResume() {
		super.onResume();
		PayManager.getInstance(mAct); // 重设PayManger
										// mContext,防止商城购买后，切换到分区mContext变化，弹框挂掉
		FiexedViewHelper.getInstance().setFragmentActivity(true);

		mUIHandler.removeMessages(HANDLERTHREAD_KILLSELF);

		AnalyticsUtils.onResume(this);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		AnalyticsUtils.onPause(this);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		FiexedViewHelper.getInstance().setFragmentActivity(false);

		initHandler();
		mUIHandler.sendEmptyMessageDelayed(HANDLERTHREAD_KILLSELF,
				1000 * 60 * 15); // 15分钟 后台自动自杀

		super.onStop();
	}

	@Override
	protected void onDestroy() {
		MainApplication.sharedApplication().finishActivity(this);
		GameUtilJni.exitApplication();
		super.onDestroy();
	}

	


	
	/**
	 * 分线程加载
	 */
	private void initConfig() {
		initHandler();
		mProcessHandler.sendEmptyMessage(HANDLERTHREAD_INIT_CONFIG_START);
		mProcessHandler.sendEmptyMessage(HANDLERTHREAD_PRELOAD_SOUND);
		mProcessHandler.sendEmptyMessage(HANDLERTHREAD_CHECK_NET);

		if (AppConfig.imgUrl == null) {
			mProcessHandler.sendEmptyMessage(HANDLERTHREAD_GET_IMG_SERVER);
		}
	}

	private void initUmeng() {
		MobclickAgent.setDebugMode(AppConfig.debug);
		// 禁止默认的页面统计方式，这样将不会再自动统计Activity
		MobclickAgent.openActivityDurationTrack(false);
		MobclickAgent.updateOnlineConfig(this);
		MobclickAgent.onResume(mAct, AnalyticsUtils.umeng_appkey,
				AppConfig.channelId);
		// 开启推送服务
		PushManager.getInstance(this).setDebug(AppConfig.debug);
		PushManager.getInstance(this).onAppStart();
		PushManager.getInstance(this).open();

	}

	
	/**
	 * 移动SDK
	 */
	public void cmccExitGame() {
//		if(AppConfig.isOpenPayByCmccSdk()){
//			GameInterface.exitApp();
//			
//		}
	}
	
	
	
	/**
	 * 请求美女视频购买商品列表
	 */
	public void reqMmVideoGoods() {
		initHandler();
		Message msg = mProcessHandler.obtainMessage();
		msg.what = HANDLERTHREAD_REQ_MMVIDEOGOODS;
		mProcessHandler.sendMessage(msg);
	}

	/**
	 * 请求推广码
	 */
	public void reqSpKey() {
		initHandler();
		Message msg = mProcessHandler.obtainMessage();
		msg.what = HANDLERTHREAD_REQ_SPKEY;
		mProcessHandler.sendMessage(msg);
	}

	/**
	 * 请求push标签
	 */
	public void reqPushTags() {
		initHandler();
		Message msg = mProcessHandler.obtainMessage();
		msg.what = HANDLERTHREAD_PUSH_TAGS;
		mProcessHandler.sendMessage(msg);
	}
	
	public void reqPingCooConfig(){
		initHandler();
		Message msg = mProcessHandler.obtainMessage();
		msg.what = HANDLERTHREAD_PINGCOO_CONFIG;
		mProcessHandler.sendMessage(msg);
	}

	/**
	 * 请求快捷购买
	 */
	public void reqPropId() {
		initHandler();
		Message msg = mProcessHandler.obtainMessage();
		msg.what = HANDLERTHREAD_REQ_PROPID;
		mProcessHandler.sendMessage(msg);
	}

	/**
	 * 请求vip数据
	 */
	public void reqVipData() {
		initHandler();
		Message msg = mProcessHandler.obtainMessage();
		msg.what = HANDLER_OTHER_VIP_DATA;
		mProcessHandler.sendMessage(msg);
	}

	/**
	 * 在线客服url地址请求
	 */
	public void reqOnlineServerUrl() {
		initHandler();
		Message msg = mProcessHandler.obtainMessage();
		msg.what = HANDLERTHREAD_ONLINE_SERVER;
		mProcessHandler.sendMessage(msg);
	}

	public void ininCloudPay() {
		initHandler();
		Message msg = mProcessHandler.obtainMessage();
		msg.what = HANDLERTHREAD_INITPAYSDK;
		mProcessHandler.sendMessage(msg);
	}

	/**
	 * 播放短促音效接口
	 * 
	 * @param soundPool
	 * @param soundId
	 */
	public void playAudio(SoundPool soundPool, int soundId) {
		initHandler();
		Message msg = mProcessHandler.obtainMessage();
		msg.what = HANDLER_PLAY_AUDIO;
		msg.arg1 = soundId;
		msg.obj = soundPool;
		mProcessHandler.sendMessage(msg);
	}

	/**
	 * 保存用户登录token
	 */
	public void reqSaveToken() {
		initHandler();
		Message msg = mProcessHandler.obtainMessage();
		msg.what = HANDLERTHREAD_SAVE_TOKEN;
		mProcessHandler.sendMessage(msg);
	}

	/**
	 * 播放音效
	 * 
	 * @param soundPool
	 * @param soundId
	 */
	private void playSound(SoundPool soundPool, int soundId) {
		synchronized (this) {
			soundPool.play(soundId, 1.0f, 1.0f, 0, 0, 1.0f);
		}
	}

	/**
	 * 请求德州VIP配置信息
	 */
	private void requestVipData() {
		final String VipUrl = URLUtils.getVipSettings(AppConfig.channelId,
				AppConfig.gameId, "0");
		String str = requestPost(null, VipUrl);
		VipXmlParser.ParserVipSettingsXml(str);
	}

	/**
	 * http Post请求的过程
	 * 
	 * @param postParameters
	 *            ：请求服务端接口需要的数据
	 * @param url
	 *            :请求接口的地址
	 * @return result
	 */
	public static String requestPost(List<NameValuePair> postParameters,
			String url) {
		String result = null;
		BufferedReader in = null;
		try {
			HttpClient client = new DefaultHttpClient();
			// Represents a collection of HTTP protocol and framework parameters
			HttpParams params = null;
			params = client.getParams();
			// set timeout
			HttpConnectionParams.setConnectionTimeout(params, 5000);
			HttpConnectionParams.setSoTimeout(params, 35000);
			// url为访问地址
			HttpPost request = new HttpPost(url);
			UrlEncodedFormEntity formEntity = null;
			// new UrlEncodedFormEntity(
			// postParameters,"utf-8");
			request.setEntity(formEntity);
			// 通过execute()执行httppost调用
			HttpResponse response = client.execute(request);

			// 读取返回结果
			in = new BufferedReader(new InputStreamReader(response.getEntity()
					.getContent()));
			StringBuffer sb = new StringBuffer("");
			String line = "";
			// 换行操作
			String NL = System.getProperty("line.separator");
			while ((line = in.readLine()) != null) {
				sb.append(line + NL);
			}
			in.close();
			result = sb.toString();
			Log.d("返回数据", result);
		} catch (Exception e) {
			result = null;
			Log.i("Error", "Exception" + e.getMessage());
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	
	
	
	public void doTalkingData(String str) {
		initHandler();
		Message msg = mProcessHandler.obtainMessage();
		msg.what = HANDLERTHREAD_TALKINGDATA;
		msg.obj = str;
		mProcessHandler.sendMessage(msg);
	}

	
	
	
	
	/**
	 * 线程初始化
	 */
	private void initHandler() {
		if (mUIHandler == null) {
			mUIHandler = new UIHandler();
		}
		if (mProcessHandler == null) {
			HandlerThread handlerThread = new HandlerThread(
					"handler looper Thread");
			handlerThread.start();
			mProcessHandler = new ProcessHandler(handlerThread.getLooper(),
					mUIHandler);
		}
	}

	private static final int UITHREAD_INIT_COMPLETE = 1;
	private static final int HANDLERTHREAD_INIT_CONFIG_START = 2;
	private static final int HANDLERTHREAD_PRELOAD_SOUND = 4;
	private static final int HANDLERTHREAD_GET_IMG_SERVER = 5;
	private static final int HANDLERTHREAD_REQ_SPKEY = 6; // 获取推广码
	private static final int HANDLERTHREAD_REQ_PROPID = 7; // 获取快捷购买ID

	private static final int HANDLER_OTHER_VIP_DATA = 8; // 请求德州VIP配置信息handler
	private static final int HANDLER_PLAY_AUDIO = 9;
	private static final int HANDLERTHREAD_TALKINGDATA = 10;// talking data

	private static final int HANDLERTHREAD_ONLINE_SERVER = 11;// 在线客服
	private static final int HANDLERTHREAD_INITPAYSDK = 12;// 初始化支付sdk
	private static final int HANDLERTHREAD_CHECK_NET = 13; // 检查网络
	private static final int HANDLERTHREAD_SAVE_TOKEN = 14; // 保存用户登录TOKEN

	private static final int HANDLERTHREAD_KILLSELF = 15; // 应用自杀

	private static final int HANDLERTHREAD_PUSH_TAGS = 16; // 请求push tags
	private static final int HANDLER_UI_RESP_PUSHTAGS = 17;// 接收到push标签，回传到ui
	private static final int HANDLERTHREAD_PINGCOO_CONFIG =18;//宾谷盒子配置
	
	private static final int HANDLERTHREAD_MMVIDEO_CONFIG =19;//视频SDK初始化
	
	private static final int HANDLERTHREAD_REQ_MMVIDEOGOODS =20;//请求美女视频商品列表
	
	private static final int MAIN_HANDLER_YUNVALIVE_INIT =21;//请求美女视频商品列表
	
	private UIHandler mUIHandler;
	private ProcessHandler mProcessHandler;

	/**
	 * 子线程handler,looper
	 * 
	 * @author Administrator
	 * 
	 */
	@SuppressLint("HandlerLeak")
	private class ProcessHandler extends Handler {
		private Handler mHandler;

		public ProcessHandler(Looper looper, Handler restoreHandler) {
			super(looper);
			this.mHandler = restoreHandler;
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLERTHREAD_INIT_CONFIG_START:
				DensityConst.initDensity(mAct);

				AppConfig.loadVersion(mAct);

				// 获取微信推广信息
				WXUtil.requestWXUrl();

				PayManager.getInstance(mAct).thirdPayInit();// 初始化第三方支付
				
				/** 初始化友盟 */
				initUmeng();
				
				mHandler.sendEmptyMessage(UITHREAD_INIT_COMPLETE); // 通知UI线程
				break;
			case HANDLERTHREAD_PRELOAD_SOUND:
				if (!Util.isOMS()) {
					// 加载声音
					PreloadCocos2dRes.getInstance(mAct).perloadSoundRes(
							soundPlayer, "sound/common");
					PreloadCocos2dRes.getInstance(mAct).perloadSoundRes(
							soundPlayer, "sound/man");
					PreloadCocos2dRes.getInstance(mAct).perloadSoundRes(
							soundPlayer, "sound/woman");
					// 加载背景音乐
					PreloadCocos2dRes.getInstance(mAct).perloadMusicRes(
							backgroundMusicPlayer, "sound/back_music.mp3");
				}
				break;
			case HANDLERTHREAD_CHECK_NET:
				if (isNetConnected()) {
					/** 获取bind url地址 */
					//httpAccountBindListener();

					/** 请求游戏的IP列表，移动用户伪码等配置数据 */
					MobileHttpApiMgr.getInstance().start(mAct,
							AppConfig.channelId); // 原CID改为channelId
				}
				break;
			case HANDLERTHREAD_SAVE_TOKEN:
				FiexedViewHelper.getInstance().saveToken();
				break;
			case HANDLERTHREAD_GET_IMG_SERVER:
				HttpConnector http = NetService.getInstance()
						.createHttpConnection(null);
				http.addEvent(new IRequest() {

					@Override
					public void handler(byte[] buf) {
						String xmlUrl = TDataInputStream.getUTF8String(buf);
						String imgUrl = UtilHelper.parseTagValueXml(xmlUrl,
								"serv", "url");
						AppConfig.imgUrl = imgUrl;
					}

					@Override
					public byte[] getData() {
						return super.getData();
					}

					@Override
					public String getParam() {
						StringBuffer buffer = new StringBuffer();
						buffer.append("cmd=").append("imgserver");
						return buffer.toString();
					}

					@Override
					public String getHttpUrl() {
						return AppConfig.updateUrl;
					}
				});
				http.connect();
				break;
			case HANDLERTHREAD_REQ_MMVIDEOGOODS:
				MMVideoBuyDialog.setData(Util.getConfigXmlByHttp(AppConfig.MMVIDEO_DIAMOND_URL));
				break;
			case HANDLERTHREAD_REQ_SPKEY:
				int userId = FiexedViewHelper.getInstance().getUserId();
				if (userId != -1) {
					FiexedViewHelper.getSPKey(AppConfig.gameId, userId);
				}
				break;
			case HANDLERTHREAD_REQ_PROPID:
				UtilHelper.invokePropIdThread();
				UtilHelper.reqCmccSdkConfig(mAct);
				break;
			case HANDLER_OTHER_VIP_DATA:
				requestVipData();
				break;
			case HANDLERTHREAD_TALKINGDATA:
				String url = (String) msg.obj;
				Util.getConfigXmlByHttp(url);
				break;
			case HANDLERTHREAD_ONLINE_SERVER:
				ServerDialog.reqOnlineServerUrl(mAct);
				break;
			case HANDLER_PLAY_AUDIO:
				SoundPool soundPool = (SoundPool) msg.obj;
				int soundId = msg.arg1;
				playSound(soundPool, soundId);
				break;
			case HANDLERTHREAD_INITPAYSDK:
				// // 支付SDK 初始化
				// MYLePay.getInstance().init(mAct, AppConfig.channelId,
				// AppConfig.childChannelId, AppConfig.gameId,
				// AppConfig.CID,
				// LoginInfoManager.getInstance().getToken(),
				// new MYPayCallBack() {
				// @Override
				// public void payResult(String status, String errorMsg) {
				// if (status.equals("000")) {
				// Log.e(TAG, errorMsg);
				// } else {
				// Log.e(TAG, errorMsg);
				// }
				// }
				// });
				break;
			case HANDLERTHREAD_PUSH_TAGS:
				FiexedViewHelper.getInstance().reqPushTags(mHandler,
						HANDLER_UI_RESP_PUSHTAGS);
				break;

			case HANDLERTHREAD_PINGCOO_CONFIG:
				break;
			case HANDLERTHREAD_MMVIDEO_CONFIG:
				mHandler.sendEmptyMessage(MAIN_HANDLER_YUNVALIVE_INIT); // 通知UI线程
				
				break;
			default:
				break;
			}

		}

	}


	
	
	
	
	
	
	
	/***
	 * 检查升级更新
	 * 
	 */
	public void checkUpgrade(final Context context, int gameId,
			final Handler handler) {
		SocketLoginListener slListener = new SocketLoginListener() {

			@Override
			public void onSuccessed(Message arg0) {
				Message msg = arg0;
				msg.what = CardZoneFragment.HANDLER_CHECK_UPDATE_COMPLETE;
				handler.sendMessage(msg);
			}

			@Override
			public void onFiled(Message arg0, int param) {
				MLog.e(TAG, "更新失败");
			}
		};
		// 升级操作
		LoginSocket.getInstance().reqVersionInfo(gameId, slListener);
	}

	/**
	 * 主线程处理handler
	 * 
	 * @author Administrator
	 * 
	 */
	@SuppressLint("HandlerLeak")
	private class UIHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case UITHREAD_INIT_COMPLETE:
				break;

			case HANDLERTHREAD_KILLSELF:
				//finish();
				MainApplication.sharedApplication().AppExit();
				break;
			case HANDLER_UI_RESP_PUSHTAGS:
				String str = msg.obj.toString();
				PushManager.getInstance(mAct).setUserTags(parseTagsXml(str));
				break;
			case MAIN_HANDLER_YUNVALIVE_INIT:

				break;
			default:
				break;
			}
		}
	}

	/**
	 * 解析xml数据，返回用户标签列表
	 * 
	 * @param xmlStr
	 * @return
	 */
	private String[] parseTagsXml(String xmlStr) {
		try {
			XmlPullParserFactory f = XmlPullParserFactory.newInstance();
			XmlPullParser parser = f.newPullParser();
			parser.setInput(new StringReader(xmlStr));
			int eventType = parser.getEventType();
			ArrayList<String> tagList = new ArrayList<String>();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					String tag = parser.getName();
					if ("l".equals(tag)) {
						tagList.add(parser.nextText());
					}
					break;
				case XmlPullParser.END_TAG:
					break;
				}
				eventType = parser.next();
			}

			return (String[]) tagList.toArray(new String[tagList.size()]);
		} catch (Exception e) {
			Log.i("chao", "pus标签数据解析出错了");
			return new String[0];
		}

	}

	private void debugLog() {
		String tags = getPackageName();
		try {
			Integer.parseInt(AppConfig.channelId);
		} catch (NumberFormatException e) {
			android.util.Log.e(tags, "主渠道：非法");
			return;
		}
		try {
			Integer.parseInt(AppConfig.childChannelId);
		} catch (NumberFormatException e) {
			android.util.Log.e(tags, "子渠道：非法");
			return;
		}
		try {
			android.util.Log.d(tags, "游戏ID：" + AppConfig.gameId);
			android.util.Log.d(tags, "主渠道：" + AppConfig.channelId);
			android.util.Log.d(tags, "子渠道：" + AppConfig.childChannelId);
			android.util.Log.d(tags, "支付方式：" + PayManager.getPlistString(false));
			int versionCode = getPackageManager().getPackageInfo(
					getPackageName(), 0).versionCode;

			android.util.Log.d(tags, "versionCode：" + versionCode);

			String versionName = Util.getVersionName(mAct);
			android.util.Log.d(tags, "程序版本号：" + versionName);
		} catch (NameNotFoundException e) {
			android.util.Log.e(tags, "versionCode可能发生了错误");
		}

	}

	/**
	 * 检查网络
	 * 
	 * @return true 网络已打开， false 网络已关闭
	 */
	private boolean isNetConnected() {
		boolean isNetConn = Util.isNetworkConnected(mAct);
		Resources resource = this.getResources();
		if (!isNetConn) {
			// 网络连接不上的提示操作
			AlertDialog.Builder builder = new AlertDialog.Builder(mAct)
					.setTitle(resource.getString(R.string.prompt))
					.setMessage(resource.getString(R.string.ddz_no_net))
					.setPositiveButton(
							resource.getString(R.string.ddz_set_net),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface arg0,
										int arg1) {
									mAct.startActivity(new Intent(
											android.provider.Settings.ACTION_WIRELESS_SETTINGS));
									android.os.Process
											.killProcess(android.os.Process
													.myPid());
								}
							})
					.setNegativeButton(
							resource.getString(R.string.ddz_empress_retry),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface arg0,
										int arg1) {
									android.os.Process
											.killProcess(android.os.Process
													.myPid());
								}
							});
			builder.setCancelable(false); // 屏蔽返回键
			builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
				public boolean onKey(DialogInterface dialog, int keyCode,
						KeyEvent event) {
					if (keyCode == KeyEvent.KEYCODE_SEARCH) { // 屏蔽搜索键
						return true;
					} else {
						return false; // 默认返回 false
					}
				}
			});
			builder.show();
		}
		return isNetConn;
	}

	/**
	 * 初始化渠道号 获取启动传入的bundle
	 */
	private void initializeBundle(Activity act) {

		// 渠道相关数据初始化--登录方式记录
		ChannelDataMgr.getInstance().initAllData(act);
		// 游戏调试配置文件加载
		AppConfig.readIpPortFormConfig();

		// 显示必要日志
		debugLog();
	}

	@Override
	public void onArticleSelected(Handler handler) {
		checkUpgrade(mAct, AppConfig.gameId, handler);
	}

	@Override
	public boolean isBindMMVideo(){
		return isBinded;
	}
	
	
	@Override	
	public void setEntryRoomNoBind(boolean b){
		isEntryRoomNoBind=b;
	}
	
	@Override
	public void onBackPressed() {
		FiexedViewHelper.getInstance().onBackPressed();
	}

	
	/**
	 * 视频SDK初始化
	 */
	public void yunvaLiveInit(){
		mProcessHandler.sendEmptyMessage(HANDLERTHREAD_MMVIDEO_CONFIG);
	}
	
	/**
	 * 获取白名单绑定地址监听
	 */
	/*private void httpAccountBindListener() {
		HttpConnector httpConntector = NetService.getInstance()
				.createHttpConnection(null);
		httpConntector.addEvent(new IRequest() {

			@Override
			public void handler(byte[] buf) {
				parseBindUrls(new String(buf));
			}

			@Override
			public String getHttpUrl() {
				return AppConfig.REQ_BIND_URL;
			}
		});
		httpConntector.connect();
	}*/

	/**
	 * 获取白名单绑定地址
	 */
	/*private void parseBindUrls(String data) {
		try {
			List<String> bindUrls = new ArrayList<String>();
			byte xmlData[] = data.getBytes();
			InputStream is = new ByteArrayInputStream(xmlData);
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(is);
			NodeList eleList = document.getElementsByTagName("login");
			for (int i = 0; i < eleList.getLength(); i++) {
				NamedNodeMap namedMap = eleList.item(i).getAttributes();
				String url = namedMap.getNamedItem("url").getNodeValue();
				bindUrls.add(url);
			}

			Random random = new Random();
			int index = random.nextInt(bindUrls.size());
			AppConfig.mBindUrl = bindUrls.get(index);
		} catch (Exception e) {
			Log.e(TAG, "parseBindUrls is error");
		}
	}*/


	/** 主协议 */
	public static final short MDM_PROP = 17;

	/** 子协议 */
	public static final short MSUB_CMD_SHOP_PROP_LIST_REQ_EX = 794;

	/** 返回 失败/成功：子协议 **/
	public static final short MSUB_CMD_SHOP_PROP_LIST_RESP = 753;

	/***
	 * @Title: requestMarketList
	 * @Description: TODO获取商城列表
	 * @version: 2012-7-23 下午02:51:10
	 */
	private int mCurGoodsNum;

	public void requestMarketList() {
		GoodsItemProvider.getInstance().goodsListClear();//清除商场道具
		// 创建发送的数据包
		TDataOutputStream tdos = new TDataOutputStream();
		short type = (short) UtilHelper.getMobileCardType(mAct);
		/** 1：中国移动,2：中国联通,3：中国电信,4：无卡 */
		if (type == 0) {
			type = 4;
		}
		tdos.writeShort(type, false);
		NetSocketPak pointBalance = new NetSocketPak(MDM_PROP,
				MSUB_CMD_SHOP_PROP_LIST_REQ_EX, tdos);
		// 定义接受数据的协议
		short[][] parseProtocol = { { MDM_PROP, MSUB_CMD_SHOP_PROP_LIST_RESP } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			public boolean doReceive(NetSocketPak netSocketPak) {
				// 解析接受到的网络数据
				TDataInputStream tdis = netSocketPak.getDataInputStream();
				tdis.setFront(false);

				int total = tdis.readShort(); // 商品总个数
				int num = tdis.readShort(); // 当次商品个数
				mCurGoodsNum += num;
				for (int i = 0; i < num; i++) {
					GoodsItem goodsItem = new GoodsItem(tdis);
					GoodsItemProvider.getInstance().add(goodsItem);
				}
				if (total == mCurGoodsNum) {
					GoodsItemProvider.getInstance().setFinish(true);
				}

				return true;
			}
		};
		nPListener.setOnlyRun(false);

		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		// 发送协议
		NetSocketManager.getInstance().sendData(pointBalance);
		// 清理协议对象
		pointBalance.free();
	}

	/**
	 * 添加请求用户乐豆和请求用户话费券
	 * 
	 * @return
	 */
	private String requestBeanXml() {
		StringBuffer sb = new StringBuffer();
		sb.append("<r>");
		sb.append("<p n=\"").append("bean").append("\"/>"); // 用户乐豆
		sb.append("<p n=\"").append("mobilevoucher").append("\"/>"); // 用户话费券
		sb.append("</r>");
		return sb.toString();
	}

	private static final short LS_TRANSIT_LOGON = 18;
	/** 请求子协议：获取用户信息（包括实名信息）请求(主要得到bean) **/
	private static final short MSUB_CMD_USERINFO_EX_REQ = 22;
	/**** 返回子协议：取用户信息（包括实名信息）请求(主要得到bean) ***/
	private static final short MSUB_CMD_USERINFO_EX_RESP = 23;

	/*** 用户乐豆获取处理handler ***/
	public static final int HANDLER_USER_BEAN = 2223;

	public void getUserBeanProtocol(final Handler handler) {

		// 创建发送的数据包
		TDataOutputStream tdos = new TDataOutputStream(false);
		tdos.writeUTF(requestBeanXml());

		NetSocketPak socketInfo = new NetSocketPak(LS_TRANSIT_LOGON,
				MSUB_CMD_USERINFO_EX_REQ, tdos);
		// 发送协议
		NetSocketManager.getInstance().sendData(socketInfo);

		// 定义接受数据的协议
		short[][] parseProtocol = { { LS_TRANSIT_LOGON,
				MSUB_CMD_USERINFO_EX_RESP } };
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			public boolean doReceive(NetSocketPak netSocketPak) {
				String data = "";
				try {
					TDataInputStream tdis = netSocketPak.getDataInputStream();
					tdis.setFront(false);
					byte[] dataByte = tdis.readBytes();
					data = new String(dataByte);
				} catch (Exception ex) {
					ex.printStackTrace();
					data = "";
				}
				Log.v(TAG, "乐豆文本：" + data);
				if (handler != null) {
					Message msg = handler.obtainMessage(HANDLER_USER_BEAN);
					msg.obj = data;
					handler.sendMessage(msg);
				}
				return true;
			}
		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		nPListener.setOnlyRun(false);

	}

	

}