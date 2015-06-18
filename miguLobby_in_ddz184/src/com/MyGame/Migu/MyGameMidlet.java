package com.MyGame.Migu;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import cn.cmgame.billing.api.BillingResult;
import cn.cmgame.billing.api.GameInterface;
import cn.cmgame.billing.api.LoginResult;

import com.MyGame.Midlet.service.AdvItem;
import com.MyGame.Midlet.service.AppVersion;
import com.MyGame.Midlet.service.GamePlayerUtil;
import com.MyGame.Midlet.service.GamePlayerUtil.ResultObject;
import com.MyGame.Midlet.service.MykjReceiver;
import com.MyGame.Midlet.service.MykjService.DownloadThread;
import com.MyGame.Midlet.service.MykjService.MykjServiceBinder;
import com.MyGame.Midlet.util.AppConfig;
import com.MyGame.Midlet.util.Configs;
import com.MyGame.Midlet.util.Util;
import com.MyGame.Migu.R;
import com.MyGame.Migu.GirdViewGameAdapter.ViewHolder;
import com.login.utils.DensityConst;
import com.login.utils.UtilDrawableStateList;
import com.login.view.AccountManager;
import com.login.view.LoginViewCallBack;
import com.login.view.LogonView;
import com.mingyou.accountInfo.LoginInfoManager;
import com.mingyou.community.Community;
import com.mingyou.community.MUserInfo;
import com.mingyou.login.LoginSocket;
import com.mingyou.login.SocketLoginListener;
import com.mingyou.login.struc.DownLoadListener;
import com.mingyou.login.struc.VersionInfo;
import com.mykj.andr.thirdlogin.ThirdLoginStart;
import com.mykj.comm.log.MLog;
import com.mykj.game.utils.UtilHelper;

public class MyGameMidlet extends Activity implements OnClickListener {
	private static final String TAG = "MyGameMidlet";

	public static final int REGEDIT_OK = 1;

	public static final int GET_GAME_VERSION_SUCCESS = 1;
	public static final int GET_GAME_VERSION_FAIL = 2;
	public static final int GET_LOBBY_ADV_SUCCESS = 3;
	public static final int GET_LOBBY_ADV_FAIL = 4;
	public static final int GET_GAMEINFO_SUCCESS = 5;
	public static final int GET_GAMEINFO_FAIL = 6;
	public static final int GET_CUSTOMINFO_SUCCESS = 7;
	public static final int GET_CUSTOMINFO_FAIL = 8;
	public static final int GET_GAME_VERSION_UPDATE = 11;
	public static final int GET_ADV_VERSION_UPDATE = 12;
	public static final int GET_GAMEINFO_VERSION_UPDATE = 13;
	public static final int GET_CUSTOMINFO_VERSION_UPDATE = 14;
	public static final int CHECK_UPDATE_COMPLETE = 15;

	public static Context mContext;
	public MykjServiceBinder myService = null;
	public static final int MSG_UPDATE = 1;

	private int sumOfDrawble = 0;

	private TextView btnRegedit;
	private TextView btnLogin;
	// private TextView btnAdvice;
	private TextView btnShareFriends;
	private TextView btnGamePlayer;
	private TextView btnQuickLogin;
	private ImageButton btnSetting;
	private ImageButton btnHelp;
	private LinearLayout llLogined;
	private LinearLayout llNoLogined;
	private LinearLayout llLogining;
	private boolean binded;
	private GridView gridview;
	private GirdViewGameAdapter girdViewGameAdapter;
	private ViewPager mAdvViewPager;
	private PagerAdapter advAdapter;

	private int cur_view_id;
	private ProgressBar progress_gird;
	private ProgressBar progress_gal;
	private View mainView; // 登陆，注册view
	// 订购玩家流程是否执行完成
	private boolean isFinish = true;
	private LinearLayout pointLinear; // 广告标示小圆点

	private SharedPreferences userInfo;
	private String isGamePlayerKey;
	/**
	 * service 通知Main UI handler
	 */
	@SuppressLint("HandlerLeak")
	public Handler mGameVersionHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Log.v(TAG, "mServiceHandler ,msg.what=" + msg.what);
			switch (msg.what) {
			// http获取游戏版本信息成功
			case GET_GAME_VERSION_SUCCESS:
				List<AppVersion> gamesList = myService.getGamesConfig();
				girdViewGameInit(gamesList);
				break;

			case GET_GAME_VERSION_FAIL:
				// showNetErrorDialog();
				break;
			// http获取广告版本信息成功
			case GET_LOBBY_ADV_SUCCESS:
				List<AdvItem> AdvList = myService.getADVConfig();
				imgGalleryInit(AdvList);

				break;
			case GET_LOBBY_ADV_FAIL:
				break;
			case GET_GAME_VERSION_UPDATE:
				if (girdViewGameAdapter != null) {
					girdViewGameAdapter.notifyDataSetChanged();
				}
				break;
			case GET_ADV_VERSION_UPDATE:
				if (advAdapter != null) {
					advAdapter.notifyDataSetChanged();
				}
				break;
			case CHECK_UPDATE_COMPLETE:
				final VersionInfo vi = (VersionInfo) msg.obj;
				byte versionTag = vi._upgrade;
				String upDesc = null;
				if (vi._upDesc != null) {
					upDesc = vi._upDesc.replace("#", "\n");
				}
				// 当状态为需要升级的时候，事件所做的操作
				if (versionTag == VersionInfo.UPGRADE_NEED) {
					final UpdateDialog dialog = new UpdateDialog(mContext);
					if (upDesc != null) {
						dialog.setUpgradeDesc(upDesc);
						dialog.show();
					}
					dialog.setOnCancelUpgradeListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							dialog.dismiss();
						}
					});
					dialog.setOnEnsureUpgradeListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							vi.gotoUpgrade(mDownLoadListener);
							dialog.dismiss();
						}
					});
				} else if (versionTag == VersionInfo.UPGRADE_MUST) {
					final UpdateDialog dialog = new UpdateDialog(mContext);
					if (upDesc != null) {
						dialog.setUpgradeDesc(upDesc);
						dialog.show();
						dialog.setCancelable(false);
						dialog.setCanceledOnTouchOutside(false);
					}
					dialog.setOnCancelUpgradeListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							android.os.Process.killProcess(android.os.Process
									.myPid());
						}
					});
					dialog.setOnEnsureUpgradeListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							vi.gotoUpgrade(mDownLoadListener);
							dialog.dismiss();
						}
					});

				} else {
					// do nothing
				}
				break;
			default:
				break;
			}
		}
	};

	// 快速登录的回调逻辑
	LoginViewCallBack mLoginCallBack = new LoginViewCallBack() {

		@Override
		public void loginsuccessed(Message msg) {
			Message m = adapterHandler.obtainMessage(LOGIN_SUCESS);
			m.sendToTarget();
		}

		@Override
		public void loginFailed(Message msg) {
			Message m = adapterHandler.obtainMessage(LOGIN_FAIL);
			m.sendToTarget();
		}

		@Override
		public void loginAction() {
			updataUIByLoginState(STATE_LOGIN_ING);
		}

		@Override
		public void nativeLoginInfo(boolean isHasNativeLoginInfo) {
			if (isHasNativeLoginInfo) {
				updataUIByLoginState(STATE_LOGIN_ING);
			} else {
				updataUIByLoginState(STATE_NOLOGIN);
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG, "MyGameMidlet is onCreate...");
		mContext = this;
		Configs.mContext = this;
		DensityConst.initDensity(this);

		gamePlayerUtil = new GamePlayerUtil(mContext);

		userInfo = getSharedPreferences("quan_user_info", 0);

		isGamePlayerKey = gamePlayerUtil.getIMSI(mContext) + "_isPlayer";
		Configs.loadGameLobbyUrlConfig(); // 初始化URL配置，从文件
		Configs.loadGameLobbyProperties(mContext); // 读取渠道号
		Configs.loadGamePlayerProperties(mContext); // 初始化游戏玩家参数

		setContentView(R.layout.activity_main);

		pointLinear = (LinearLayout) findViewById(R.id.gallery_point_linear);
		mainView = findViewById(R.id.llmainview);

		AccountManager.getInstance().initialize(this,
				AccountManager.HTTP_LOGIN_TAG, LoginInfoManager.HALL_TAG, 0,
				AppConfig.channelId, AppConfig.childChannelId,
				Configs.getVersionName(mContext), 0, (byte) 0,
				Configs.getICCID(this));

		Intent intent = new Intent();
		intent.setAction("mykj.service.BOOT_SERVICE");
		bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

		initViews();
		
		/*if (getMIGUChecked()) {
			showMIGUDialog();
		}*/
		
		checkUpgrade(this, 0, mGameVersionHandler);
		//AccountManager.getInstance().quickEntrance(mLoginCallBack); 取消自动登录
		showProgressBar();
		setBootWatchdog(9000); // 9秒后启动后台server定时器

		GameInterface.initializeApp(this, "", null, null, null,
				new SdkCallBack());
		
		//fot test begin
		/*String token = Configs.getStringSharedPreferences(mContext,
				ThirdLoginStart.token_key, "");
		if (Configs.isEmptyStr(token)) {
			ThirdLoginStart.getInstance(mContext).init(mLoginCallBack);
			ThirdLoginStart.getInstance(mContext).getTokenUrl("01979015");
		}*/
		//fot test end
	}

	private class SdkCallBack implements GameInterface.ILoginCallback{

		@Override
		public void onResult(int resultCode, String userId,Object ogj) {

			// 用户登录成功的游戏业务逻辑代码
			if (LoginResult.SUCCESS_EXPLICIT == resultCode
					|| LoginResult.SUCCESS_IMPLICIT == resultCode) {

				String token = Configs.getStringSharedPreferences(mContext,
						ThirdLoginStart.token_key, "");
				if (Configs.isEmptyStr(token)) {
					ThirdLoginStart.getInstance(mContext).init(mLoginCallBack);
					ThirdLoginStart.getInstance(mContext).getTokenUrl(userId);
				}

				// 用户登录失败的游戏业务逻辑代码
			} else if (LoginResult.FAILED_EXPLICIT == resultCode) {
				// 用户取消登录的游戏业务逻辑代码
			}  else {
				
			}
	
		}
		
	}
	
	/**
	 * 定时创建服务
	 * */
	private void setBootWatchdog(long mill) {

		AlarmManager mAlarmManager = (AlarmManager) mContext
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent();
		intent.setClass(mContext, MykjReceiver.class);
		intent.setAction("mykj.intent.action.JUST_START_SERVER");
		PendingIntent pi = PendingIntent.getBroadcast(mContext, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		long timeNow = SystemClock.elapsedRealtime();
		long nextCheckTime = timeNow + mill; // 下次启动的时间
		mAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, nextCheckTime,
				pi);
		// setReqBroadcastDate();
		Log.v(TAG, "注册广播发送PendingIntent");
	}

	/**
	 * 单例启动游戏
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		String topActivity = Configs.getGameTopActicity(mContext);
		if (binded && !Configs.isEmptyStr(topActivity)) {
			List<AppVersion> gamesList = myService.getGamesConfig();
			for (AppVersion gameitem : gamesList) {
				if (gameitem.isAppInstalled()
						&& topActivity.startsWith(gameitem.getPackageName())) {
					gameitem.startGame(mContext);
				}
			}

		}
	};

	/**
	 * 显示加载进度条
	 */
	private void showProgressBar() {
		mAdvViewPager = (ViewPager) findViewById(R.id.adv_pager);
		mAdvViewPager.setPageMargin(15);
		mAdvViewPager.setVisibility(View.INVISIBLE);

		gridview = (GridView) findViewById(R.id.gridview);
		gridview.setVisibility(View.INVISIBLE);

		progress_gird = (ProgressBar) findViewById(R.id.progress_gird);
		progress_gird.setVisibility(View.VISIBLE);
		progress_gird.setIndeterminate(true);

		progress_gal = (ProgressBar) findViewById(R.id.progress_gal);
		progress_gal.setVisibility(View.VISIBLE);
		progress_gal.setIndeterminate(true);
	}

	/**
	 * 显示游戏GirdView
	 */
	private void dismissGirdProgressBar() {
		gridview.setVisibility(View.VISIBLE);
		progress_gird.setVisibility(View.GONE);
	}

	/**
	 * 显示广告gallary
	 */
	private void dismissGalProgressBar() {
		mAdvViewPager.setVisibility(View.VISIBLE);
		progress_gal.setVisibility(View.GONE);
	}

	public void setContentView(int id) {
		cur_view_id = id;
		super.setContentView(id);
	}

	public void setContentView(View view) {
		if (view == mainView) {
			cur_view_id = R.layout.activity_main;
		} else {
			cur_view_id = view.getId();
		}
		super.setContentView(view);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			switch (cur_view_id) {
			case R.layout.activity_main:
				// AccountManager.showExitView(this);
				// showExitDialog();
				exitGame();
				return true;
			default:
				if (mainView != null) {
					setContentView(mainView);
				}
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 退出游戏对话框
	 */
	public void showExitDialog() {
		Builder builder = new AlertDialog.Builder(this)
				.setTitle("提示")
				.setMessage("确认退出游戏?")
				.setPositiveButton("确认", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						android.os.Process.killProcess(android.os.Process
								.myPid());
						System.exit(0);
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
					}
				});
		builder.show();
	}

	/**
	 * 退出获取游戏信息提示对话框
	 */
	public void showNetErrorDialog() {

		// 网络连接不上的提示操作
		Builder builder = new AlertDialog.Builder(this)
				.setTitle("提示")
				.setMessage("您的网络不给力啊！")
				.setPositiveButton("设置网络",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface arg0, int arg1) {
								startActivity(new Intent(
										android.provider.Settings.ACTION_WIRELESS_SETTINGS));
								android.os.Process
										.killProcess(android.os.Process.myPid());
							}
						})
				.setNegativeButton("稍后重试",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface arg0, int arg1) {
								android.os.Process
										.killProcess(android.os.Process.myPid());
								System.exit(0);
							}
						});
		builder.show();
	}

	/**
	 * 初始化按键
	 */
	private void initViews() {
		btnRegedit = (TextView) findViewById(R.id.btnRegedit);
		btnLogin = (TextView) findViewById(R.id.btnLogin);
		btnQuickLogin = (TextView) findViewById(R.id.btnQuickLogin);
		// btnAdvice=(TextView)findViewById(R.id.btnAdvice);
		btnShareFriends = (TextView) findViewById(R.id.btnShareFriends);
		btnGamePlayer = (TextView) findViewById(R.id.btnGamePlayer);
		btnSetting = (ImageButton) findViewById(R.id.btnSetting);
		btnHelp = (ImageButton) findViewById(R.id.btnHelp);
		llLogined = (LinearLayout) findViewById(R.id.linearLogined);
		llNoLogined = (LinearLayout) findViewById(R.id.linearNoLogin);
		llLogining = (LinearLayout) findViewById(R.id.linearLogining);

		btnRegedit.setOnClickListener(this);
		btnLogin.setOnClickListener(this);
		btnQuickLogin.setOnClickListener(this);
		// btnAdvice.setOnClickListener(this);
		btnShareFriends.setOnClickListener(this);
		btnGamePlayer.setOnClickListener(this);
		btnSetting.setOnClickListener(this);
		btnHelp.setOnClickListener(this);
		if (AppConfig.isGamePlayerEnable) {
			btnHelp.setVisibility(View.GONE);
			btnGamePlayer.setVisibility(View.VISIBLE);
		} else {
			btnHelp.setVisibility(View.VISIBLE);
			btnGamePlayer.setVisibility(View.GONE);
		}

	}

	/**
	 * 进入账号登陆界面
	 */
	public void entryAccoutLoginView() {
		LogonView loginView = getLogonView(this);
		this.setContentView(loginView);
	}

	private LogonView getLogonView(Activity act) {
		final LogonView v = new LogonView(act);
		v.setBackgroundRes(R.drawable.new_bg); // 设置主view背景
		v.setImgLogo(R.drawable.normal_logo);// 设置view logo
		v.setBackgroudInput(R.drawable.bg_content); // 设置登录view 背景
		v.setBackgroudLinearInput(R.drawable.ll_account_psw);
		v.setBackgroudBtnChoose(R.drawable.accout_select_bg);
		v.setBackgroundBtnGetPassWord(R.drawable.btn_login_modify_unuse);// 忘记密码
		v.setBtnGetPassWordOnClickCallBack(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Toast.makeText(mContext, "此功能暂未开发,敬请期待", Toast.LENGTH_SHORT)
						.show();
			}
		});
		Drawable drawable = UtilDrawableStateList.newSelector(act,
				R.drawable.btn_login_login_normal,
				R.drawable.btn_login_login_press);
		v.setBackgroundBtnLogin(drawable); // 登录
		v.setBtnLoginOnClickCallBack(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				v.hideInputKeyBoard();
				final String account = v.getAccoutInput();
				final String password = v.getPassWordInput();
				if (Util.isEmptyStr(account)) {
					Toast.makeText(mContext, "账号不能为空", Toast.LENGTH_SHORT)
							.show();
				}
				AccountManager.getInstance().accountLogin(mContext, account,
						password, mLoginCallBack);
				setContentView(mainView);
			}
		});
		v.setAccountListBackGround(R.drawable.accout_select_bg);// popup
																// listview控件背景
		v.setAccountListDivider(R.drawable.pw_account_divider);// popup listview
																// 分隔线

		v.setPopupWindwPush(mContext.getResources().getDrawable(
				R.drawable.popup_push)); // popup 按键弹出指示
		v.setPopupWindwPull(mContext.getResources().getDrawable(
				R.drawable.popup_pull)); // popup 按键缩回指示

		return v;
	}

	private static GamePlayerUtil gamePlayerUtil;
	GamePlayerHandler gamePlayerHandler;
	HandlerThread ht = new HandlerThread("gamePlayer");
	@SuppressLint("HandlerLeak")
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			int what = msg.what;
			ResultObject resultCode = null;
			switch (what) {
			case 1:
				if (!pd.isShowing()) {
					pd.show();
				}
				break;
			case 2:
				pd.dismiss();
				resultCode = (ResultObject) msg.obj;
				if (resultCode != null
						&& GamePlayerUtil.RS_OK.equals(resultCode
								.getResultCode())
						&& !GamePlayerUtil.IS_GAME_PLAYER.equals(resultCode
								.getIsGamePlayer())) {
					// 提示订购游戏玩家
					gamePlayerUtil.dialog(
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									pd.show();
									// 点击确定后开始订购游戏玩家
									gamePlayerHandler
											.sendMessage(gamePlayerHandler
													.obtainMessage(3));
									dialog.dismiss();
								}
							}, new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									isFinish = true;
									dialog.dismiss();
								}
							}, resultCode.getMsg());
					break;
				}
				isFinish = true;
				Toast.makeText(mContext, resultCode.getMsg(), Toast.LENGTH_LONG)
						.show();
				break;
			// 提示订购结果
			case 3:
				pd.dismiss();
				resultCode = (ResultObject) msg.obj;
				if (GamePlayerUtil.RS_OK.equals(resultCode.getResultCode())) {
					// 将订购成功信息保存到本地
					userInfo.edit().putString(isGamePlayerKey, "yes").commit();
				}
				isFinish = true;
				Toast.makeText(mContext, resultCode.getResultMsg(),
						Toast.LENGTH_LONG).show();
				break;
			}

		};
	};

	@SuppressLint("HandlerLeak")
	class GamePlayerHandler extends Handler {
		public GamePlayerHandler() {

		}

		public GamePlayerHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			int what = msg.what;
			ResultObject resultCode = null;
			switch (what) {
			// 判断是否已经是游戏玩家
			case 1:
				// 先从本地查询是否是游戏玩家
				String isGamePlayer = userInfo.getString(isGamePlayerKey, null);

				handler.sendMessage(handler.obtainMessage(1));

				String isPlayer = "";
				if (null != isGamePlayer
						&& GamePlayerUtil.IS_GAME_PLAYER.equals(isGamePlayer)) {
					isPlayer += "yes";
				}
				resultCode = gamePlayerUtil.isGamePlayer(isPlayer);
				this.sendMessage(this.obtainMessage(2, resultCode));
				break;
			// 提示订购游戏玩家
			case 2:
				resultCode = (ResultObject) msg.obj;
				handler.sendMessage(handler.obtainMessage(2, resultCode));
				break;
			// 订购游戏玩家
			case 3:
				resultCode = gamePlayerUtil.getPlayerOrder();
				handler.sendMessage(handler.obtainMessage(3, resultCode));
				break;
			}

		}
	}

	ProgressDialog pd = null;

	@Override
	public void onClick(View v) {
		Intent in = new Intent();
		switch (v.getId()) {
		case R.id.btnRegedit:
			Intent intent = new Intent();
			intent.setClass(mContext, RegeditActivity.class);
			startActivityForResult(intent, REGEDIT_OK);
			break;
		case R.id.btnLogin:
			entryAccoutLoginView();
			break;
		case R.id.btnShareFriends:
			String sms = mContext.getString(R.string.smscontent);
			showPhoneBook(sms);
			break;
		case R.id.btnSetting:
			in.setClass(mContext, SettingActivity.class);
			mContext.startActivity(in);
			break;
		case R.id.btnHelp:
			startActivity(new Intent(mContext, HelpActivity.class));
			break;
		case R.id.btnGamePlayer:

			MUserInfo userInfo = Community.getSelftUserInfo();

			if (userInfo.userId == 0) {
				Toast.makeText(mContext, "您尚未登陆，请登陆后重试！", Toast.LENGTH_LONG)
						.show();
				break;
			}

			boolean isMobile = isMobile();
			if (!isFinish) {
				Toast.makeText(mContext, "亲，游戏玩家体验版订购中，请稍后！", Toast.LENGTH_LONG)
						.show();
				break;
			}
			if (!isMobile) {
				Toast.makeText(mContext, "亲，移动用户才能享受游戏玩家体验版哦，建议您换移动卡尝试体验！",
						Toast.LENGTH_LONG).show();
				break;
			}
			isFinish = false;
			if (!ht.isAlive()) {
				ht.start();
			}
			if (pd == null) {
				pd = gamePlayerUtil.progressDialog();
			}
			gamePlayerHandler = new GamePlayerHandler(ht.getLooper());
			// 判断是否已经是游戏玩家
			gamePlayerHandler.sendMessage(gamePlayerHandler.obtainMessage(1));
			break;
		case R.id.btnQuickLogin:
			AccountManager.getInstance().quickEntrance(mLoginCallBack);
			break;

		case R.id.userimg:
			if (Community.getSelftUserInfo().isValid()) {
				in.setClass(mContext, PersonalActivity.class);
				mContext.startActivity(in);
			}
			break;

		}

	}

	/**
	 * bind service callback
	 */
	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			myService = (MykjServiceBinder) service;
			myService.serviceHttpGetLobbyData(mGameVersionHandler); // http请求服务器游戏列表配置，mGameVersionHandler回调

			binded = true;
			Log.v(TAG, "Service Connected...");

		}

		// 连接服务失败后，该方法被调用
		@Override
		public void onServiceDisconnected(ComponentName name) {
			myService.clearAppConfigList();
			myService = null;
			binded = false;
			Log.e(TAG, "Service Failed...");
		}
	};

	@SuppressLint("HandlerLeak")
	final Handler autoGalleryHandler = new Handler() {
		public void handleMessage(Message message) {
			super.handleMessage(message);
			switch (message.what) {
			case MSG_UPDATE:
				int index = message.arg1;
				mAdvViewPager.setCurrentItem(index);
				changePointView(index);
				break;
			}
		}
	};

	private void imgGalleryInit(List<AdvItem> list) {

		if (mAdvViewPager == null) {
			return;
		}
		advAdapter = new AdvAdapter(list, this);
		mAdvViewPager.setAdapter(advAdapter);
		dismissGalProgressBar();

		sumOfDrawble = list.size();
		for (int i = 0; i < list.size(); i++) {
			ImageView pointView = new ImageView(this);
			if (i == 0) {
				pointView.setBackgroundResource(R.drawable.feature_point_cur);
			} else {
				pointView.setBackgroundResource(R.drawable.feature_point);
			}
			pointLinear.addView(pointView);
		}

		mAdvViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				// TODO Auto-generated method stub
				changePointView(arg0 % sumOfDrawble);
				autoGalleryHandler.removeMessages(MSG_UPDATE);

				int sum = advAdapter.getCount();
				int cur_index = (arg0 + 1) % sum;
				Message msgLooper = autoGalleryHandler.obtainMessage(
						MSG_UPDATE, cur_index, 0);
				autoGalleryHandler.sendMessageDelayed(msgLooper, 5000);

			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
			}
		});
		int sum = advAdapter.getCount();
		int index = 1 % sum;
		Message msg = autoGalleryHandler.obtainMessage(MSG_UPDATE, index, 0);
		autoGalleryHandler.sendMessageDelayed(msg, 5000);
	}


	@Override
	protected void onResume() {
		super.onResume();
		// 判断是否已经有账号登陆
		boolean isHasNewAccInfo = AccountManager.isAddNewAccount();
		if (isHasNewAccInfo) {
			AccountManager.getInstance().quickEntrance(mLoginCallBack);
		} else if (!AccountManager.reIsHasAccountInfo()) {// 帐号全部删除注销界面
			updataUIByLoginState(STATE_NOLOGIN);
		}

		Message msg = adapterHandler.obtainMessage();
		msg.what = UPDATA_USERSHOW;
		adapterHandler.sendMessage(msg);
	}

	/**
	 * 是否为移动卡
	 * 
	 * @return
	 */
	public boolean isMobile() {
		boolean isMobile = false;
		String imsi = gamePlayerUtil.getIMSI(this);
		if (imsi.startsWith("46000")) {
			isMobile = true;
		}
		if (imsi.startsWith("46002")) {
			isMobile = true;
		}
		if (imsi.startsWith("46007")) {
			isMobile = true;
		}
		return isMobile;
	}

	@Override
	public void onStop() {
		super.onStop();
		if (binded) {
			List<AppVersion> gamesList = myService.getGamesConfig();

			for (int i = 0, j = gamesList.size(); i < j; i++) {
				int gameid = gamesList.get(i).getGameId();
				int rate = gamesList.get(i).mProgress;
				DownloadThread downloading = myService
						.getDownloadThread(gameid);
				if (downloading != null) {
					if (!downloading.isCancelled()) {
						Log.v(TAG, "game download will be pause!");
						// 显示下载暂停图片
						downloading.cancel();
						girdViewGameAdapter.chargeProgress(i, rate);
						// myService.clearDownloadThread(gameid);
					}
				}
			}

		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) { // resultCode为回传的标记，我在B中回传的是RESULT_OK
		case REGEDIT_OK:
			Bundle b = data.getExtras(); // data为B中回传的Intent
			String account = b.getString("account");
			String password = b.getString("password");
			AccountManager.getInstance().accountLogin(mContext, account,
					password, mLoginCallBack);
			break;
		default:
			break;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		autoGalleryHandler.removeMessages(MSG_UPDATE);
		unbindService(serviceConnection);

	}

	private void exitGame() {
		// 移动退出接口，含确认退出UI
		// 如果外放渠道（非移动自有渠道）限制不允许包含移动退出UI，可用exitApp接口（无UI退出）
		GameInterface.exit(this, new GameInterface.GameExitCallback() {
			@Override
			public void onConfirmExit() {
				finish();
				System.exit(0);
			}

			@Override
			public void onCancelExit() {
				//Toast.makeText(mContext, "取消退出", Toast.LENGTH_SHORT).show();
			}
		});
	}

	/**
	 * 刷新游戏下载状态图片
	 * 
	 * @param positon
	 */
	public void refreshGameIconStatus(int positon) {
		if (binded) {
			List<AppVersion> gamesList = myService.getGamesConfig();
			int rate = gamesList.get(positon).mProgress;
			girdViewGameAdapter.chargeProgress(positon, rate);
		}
	}

	/**
	 * 刷新广告表示点点
	 * 
	 * @param cur
	 */
	private void changePointView(int cur) {
		if (pointLinear != null) {
			for (int i = 0; i < sumOfDrawble; i++) {
				// View view = pointLinear.getChildAt(i);
				ImageView pointView = (ImageView) pointLinear.getChildAt(i);
				if (pointView != null) {
					if (i == cur) {
						pointView
								.setBackgroundResource(R.drawable.feature_point_cur);
					} else {
						pointView
								.setBackgroundResource(R.drawable.feature_point);
					}
				}

			}
		}

	}

	/**
	 * 初始化GirdView
	 * 
	 * @param list
	 */
	private void girdViewGameInit(List<AppVersion> list) {

		girdViewGameAdapter = new GirdViewGameAdapter(this, list);

		gridview.setAdapter(girdViewGameAdapter);

		dismissGirdProgressBar();

		gridview.setOnItemLongClickListener(new GridViewOnItemLongClickListener());
		// 事件监听
		gridview.setOnItemClickListener(new GridViewOnItemClickListener());

		for (AppVersion gameItem : list) {
			if (Configs.isEmptyStr(gameItem.getOnLineNum())) {
				Message msg = mGameVersionHandler.obtainMessage();
				msg.what = MyGameMidlet.GET_GAME_VERSION_UPDATE;
				mGameVersionHandler.sendMessageDelayed(msg, 8000);
				break;
			}
		}
	}

	private class GridViewOnItemClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position,
				long id) {
			final AppVersion gameItem = (AppVersion) girdViewGameAdapter
					.getItem(position);
			final int gameid = gameItem.getGameId();
			final String url = gameItem.getDownloadUrl();
			final String savePath = AppVersion.getDownloadPath();
			final String md5 = gameItem.getDownFileConfigMD5();
			final int pos = position;
			final String gamename = gameItem.getGameName();
			DownloadThread downloading = myService.getDownloadThread(gameid);
			Log.v(TAG, "download id=" + gameid);
			Log.v(TAG, "position=" + position);
			if (gameid < 0) {
				Toast.makeText(mContext, "此款游戏即将上线，敬请期待^_^", Toast.LENGTH_SHORT)
						.show();
				return;
			}

			if (gameItem.isMustUpdate()) {
				Log.v(TAG, "game must be update!");
				ViewHolder holder = (ViewHolder) v.getTag();
				final ImageView imgDownload = (ImageView) holder.imgDownload;
				if (downloading == null) {
					Builder builder = new AlertDialog.Builder(mContext);
					builder.setTitle(gamename + "版本过低,必须升级才能体验游戏");
					builder.setMessage(gameItem.getUpdateContext());
					builder.setPositiveButton("现在升级",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface arg0,
										int arg1) {
									if (gameItem.isUpdateComplete()) {
										Log.v(TAG, "game will be setup!");
										// 安装游戏
										Configs.installApk(mContext,
												gameItem.getAPKFilePath());
									} else {
										Log.v(TAG, "game will be download!");
										// 下载游戏
										imgDownload
												.setImageResource(R.drawable.download_pause);
										myService.startDownloadFile(gameid,
												url, savePath, md5,
												adapterHandler, pos);
									}
								}
							});
					builder.setNegativeButton("以后再说",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface arg0,
										int arg1) {
									// finish();
								}
							});
					builder.show();
				} else if (downloading != null) {
					if (downloading.isCancelled()) {
						Log.v(TAG, "game download will be restart!");

						imgDownload.setImageResource(R.drawable.download_pause);
						myService.clearDownloadThread(gameid);
						myService.startDownloadFile(gameid, url, savePath, md5,
								adapterHandler, position);
					} else {
						Log.v(TAG, "game download will be pause!");
						// 显示下载暂停图片
						imgDownload.setImageResource(R.drawable.download_star);
						downloading.cancel();
					}
				}
			}
			// 游戏是否需要升级
			else if (gameItem.isNeedUpdate()) {
				Log.v(TAG, "game will be update!");
				ViewHolder holder = (ViewHolder) v.getTag();
				final ImageView imgDownload = (ImageView) holder.imgDownload;
				if (downloading == null) {
					Builder builder = new AlertDialog.Builder(mContext);
					builder.setTitle(gamename + "版本更新");
					builder.setMessage(gameItem.getUpdateContext());
					builder.setPositiveButton("现在升级",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface arg0,
										int arg1) {
									if (gameItem.isUpdateComplete()) {
										Log.v(TAG, "game will be setup!");
										// 安装游戏
										Configs.installApk(mContext,
												gameItem.getAPKFilePath());
									} else {
										Log.v(TAG, "game will be download!");
										// 下载游戏
										imgDownload
												.setImageResource(R.drawable.download_pause);
										myService.startDownloadFile(gameid,
												url, savePath, md5,
												adapterHandler, pos);
									}
								}
							});
					builder.setNegativeButton("直接进入",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface arg0,
										int arg1) {
									gameItem.startGame(mContext);
								}
							});
					builder.show();

				} else if (downloading != null) {
					if (downloading.isCancelled()) {
						Log.v(TAG, "game download will be restart!");

						imgDownload.setImageResource(R.drawable.download_pause);
						myService.clearDownloadThread(gameid);
						myService.startDownloadFile(gameid, url, savePath, md5,
								adapterHandler, position);
					} else {
						Log.v(TAG, "game download will be pause!");
						// 显示下载暂停图片
						imgDownload.setImageResource(R.drawable.download_star);
						downloading.cancel();
					}
				}
			} else if (gameItem.isAppInstalled()) {
				if (getIsLoginStatus() == 2) {
					Toast.makeText(mContext, "正在为您登录，请稍后...",
							Toast.LENGTH_SHORT).show();
					return;
				} else {
					Log.v(TAG, "game will be start!");
					// 运行游戏
					gameItem.startGame(mContext);
				}
			} else if (gameItem.isUpdateComplete()) {
				Log.v(TAG, "game will be setup!");
				// 安装游戏
				Configs.installApk(mContext, gameItem.getAPKFilePath());
			} else {
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putInt("position", position);
				bundle.putInt("gameid", gameid);
				intent.putExtras(bundle);
				intent.setClass(mContext, GameInfoActivity.class);
				startActivity(intent);
			}

		}

	}

	private class GridViewOnItemLongClickListener implements
			OnItemLongClickListener {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View v,
				int position, long id) {
			final AppVersion gameItem = (AppVersion) girdViewGameAdapter
					.getItem(position);
			final int gameid = gameItem.getGameId();
			final String url = gameItem.getDownloadUrl();
			final String savePath = AppVersion.getDownloadPath();
			final String md5 = gameItem.getDownFileConfigMD5();
			final int pos = position;
			final String gamename = gameItem.getGameName();
			DownloadThread downloading = myService.getDownloadThread(gameid);

			Log.v(TAG, "download id=" + gameid);
			if (gameid < 0) {
				Toast.makeText(mContext, "此款游戏即将上线，敬请期待^_^", Toast.LENGTH_SHORT)
						.show();
			} else if (gameItem.isMustUpdate()) {
				Log.v(TAG, "game must be update!");
				ViewHolder holder = (ViewHolder) v.getTag();
				final ImageView imgDownload = (ImageView) holder.imgDownload;
				if (downloading == null) {
					Builder builder = new AlertDialog.Builder(mContext);
					builder.setTitle(gamename + "版本过低,必须升级才能体验游戏");
					builder.setMessage(gameItem.getUpdateContext());
					builder.setPositiveButton("现在升级",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface arg0,
										int arg1) {
									if (gameItem.isUpdateComplete()) {
										Log.v(TAG, "game will be setup!");
										// 安装游戏
										Configs.installApk(mContext,
												gameItem.getAPKFilePath());
									} else {
										Log.v(TAG, "game will be download!");
										// 下载游戏
										imgDownload
												.setImageResource(R.drawable.download_pause);
										myService.startDownloadFile(gameid,
												url, savePath, md5,
												adapterHandler, pos);
									}
								}
							});
					builder.setNegativeButton("以后再说",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface arg0,
										int arg1) {
									// finish();
								}
							});
					builder.show();
				} else if (downloading != null) {
					if (downloading.isCancelled()) {
						Log.v(TAG, "game download will be restart!");

						imgDownload.setImageResource(R.drawable.download_pause);
						myService.clearDownloadThread(gameid);
						myService.startDownloadFile(gameid, url, savePath, md5,
								adapterHandler, position);
					} else {
						Log.v(TAG, "game download will be pause!");
						// 显示下载暂停图片
						imgDownload.setImageResource(R.drawable.download_star);
						downloading.cancel();
					}
				}
			} else if (gameItem.isNeedUpdate() && downloading != null) {
				// do nothing
			} else {
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putInt("position", position);
				bundle.putInt("gameid", gameid);
				intent.putExtras(bundle);

				intent.setClass(mContext, GameInfoActivity.class);
				startActivity(intent);
			}
			return true;
		}

	}

	public static final int UPDATE_DOWNLOAD_PROGRESS = 0;

	public static final int SERVICE_DOWNLOAD_SUCCESS = 1;

	public static final int SERVICE_DOWNLOAD_FAIL = 2;

	public static final int SERVICE_DOWNLOAD_CANCLE = 3;

	public static final int LOGIN_SUCESS = 4;

	public static final int LOGIN_FAIL = 5;

	public static final int REGISTER_SUCESS = 6;

	public static final int UPDATA_USERSHOW = 7;

	@SuppressLint("HandlerLeak")
	public Handler adapterHandler = new Handler() {
		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();
			int position = bundle.getInt("position");
			int rate = bundle.getInt("rate");
			Log.v(TAG, "position=" + position);
			Log.v(TAG, "rate=" + rate);
			String apkPath = bundle.getString("filepath");

			switch (msg.what) {
			case UPDATE_DOWNLOAD_PROGRESS: {
				girdViewGameAdapter.chargeProgress(position, rate);
			}
				break;
			case SERVICE_DOWNLOAD_SUCCESS: {
				final AppVersion gameItem = (AppVersion) girdViewGameAdapter
						.getItem(position);
				final int gameid = gameItem.getGameId();
				DownloadThread downloading = myService
						.getDownloadThread(gameid);
				if (downloading != null && downloading.isCancelled()) {
					myService.clearDownloadThread(gameid);
				}
				girdViewGameAdapter.chargeProgress(position, rate);
				Configs.installApk(mContext, apkPath);
			}
				break;
			case SERVICE_DOWNLOAD_FAIL: {
				final AppVersion gameItem = (AppVersion) girdViewGameAdapter
						.getItem(position);
				girdViewGameAdapter.chargeProgress(position, rate);
				Toast.makeText(mContext, gameItem.getGameName() + "下载失败!",
						Toast.LENGTH_SHORT).show();
			}
				break;
			case SERVICE_DOWNLOAD_CANCLE: {
				girdViewGameAdapter.chargeProgress(position, rate);
			}
				break;
			case LOGIN_SUCESS: {
				MyGameMidlet.this.setContentView(mainView);
				updataUIByLoginState(STATE_LOGINED);
			}
				break;
			case LOGIN_FAIL: {
				updataUIByLoginState(STATE_NOLOGIN);
			}
				break;
			case UPDATA_USERSHOW: {
				ImageView userImg = (ImageView) llLogined
						.findViewById(R.id.userimg);
				TextView nickNameText = (TextView) llLogined
						.findViewById(R.id.nickname);
				if (userImg == null || nickNameText == null) {
					return;
				}
				MUserInfo userInfo = Community.getSelftUserInfo();
				if (userInfo.isValid()) {
					if (userInfo.cbGender == 1) {
						userImg.setImageResource(R.drawable.user_man);
					} else {
						userImg.setImageResource(R.drawable.user_femal);
					}
					nickNameText.setText(userInfo.nickName);
				} else {
					userImg.setImageResource(R.drawable.user_no);
					nickNameText.setText("");
				}
			}
				break;

			default:
				break;
			}

		}
	};

	/** 未登录状态 */
	private final int STATE_NOLOGIN = 1;

	/** 登录中状态 */
	private final int STATE_LOGIN_ING = 2;

	/** 已登录状态 */
	private final int STATE_LOGINED = 3;

	private static int isLoginStatus = -1;

	/**
	 * 获取登陆状态 1 未登录 2 登录中 3 已登陆
	 * 
	 * @return
	 */
	public static int getIsLoginStatus() {
		return isLoginStatus;
	}

	private void updataUIByLoginState(int loginState) {
		isLoginStatus = loginState;
		switch (loginState) {
		case STATE_NOLOGIN: {
			if (llNoLogined == null) {
				break;
			}
			llNoLogined.setVisibility(View.VISIBLE);
			if (llLogined != null) {
				llLogined.setVisibility(View.GONE);
				btnShareFriends.setVisibility(View.GONE);
			}
			if (llLogining != null) {
				llLogining.setVisibility(View.GONE);
			}
		}
			break;
		case STATE_LOGIN_ING: {
			if (llLogined == null) {
				break;
			}

			if (llLogining != null) {
				llLogining.setVisibility(View.VISIBLE);
				btnShareFriends.setVisibility(View.GONE);
			}
			if (llNoLogined != null) {
				llNoLogined.setVisibility(View.GONE);
			}
			if (llLogined != null) {
				llLogined.setVisibility(View.GONE);
			}

		}
			break;
		case STATE_LOGINED: {
			if (llLogined == null) {
				break;
			}
			llLogined.setVisibility(View.VISIBLE);
			if (llNoLogined != null) {
				llNoLogined.setVisibility(View.GONE);
			}
			if (llLogining != null) {
				llLogining.setVisibility(View.GONE);
			}
			ImageView userImg = (ImageView) llLogined
					.findViewById(R.id.userimg);
			TextView nickNameText = (TextView) llLogined
					.findViewById(R.id.nickname);
			btnShareFriends.setVisibility(View.VISIBLE);
			if (userImg == null || nickNameText == null) {
				return;
			}
			MUserInfo userInfo = Community.getSelftUserInfo();
			if (userInfo.isValid()) {
				if (userInfo.cbGender == 1) {
					userImg.setImageResource(R.drawable.user_man);
				} else {
					userImg.setImageResource(R.drawable.user_femal);
				}
				userImg.setOnClickListener(this);
				nickNameText.setText(userInfo.nickName);
			} else {
				userImg.setImageResource(R.drawable.user_no);
				nickNameText.setText("");
			}
		}
			break;
		default:
			break;
		}
	}

	/**
	 * 邀请好友，调用短信发送界面
	 * 
	 * @param smsBody
	 */
	public void showPhoneBook(String smsBody) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_SENDTO);
		intent.setData(Uri.parse("smsto:"));
		if (smsBody != null)
			intent.putExtra("sms_body", smsBody);
		else
			intent.putExtra("sms_body", "");
		mContext.startActivity(intent);
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
				msg.what = CHECK_UPDATE_COMPLETE;
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

	private NotificationManager mNM = null;

	/**
	 * 获取Notification对象
	 * 
	 * @return
	 */
	@SuppressLint("NewApi")
	public Notification getUpdateNotification(int rate) {
		String appName = mContext.getString(R.string.app_name)
				+ mContext.getString(R.string.login_downloaded);
		Notification.Builder bob = new Notification.Builder(mContext);
		bob.setContentTitle(mContext.getString(R.string.app_name));
		RemoteViews views = new RemoteViews(mContext.getPackageName(),
				R.layout.download_notifiy);
		views.setProgressBar(R.id.rate, 100, 0, false);
		views.setTextViewText(R.id.fileName, appName + rate + "%");
		bob.setContent(views);
		bob.setSmallIcon(R.drawable.app_launcher);

		Intent intent = getPackageManager().getLaunchIntentForPackage(
				mContext.getPackageName());
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		bob.setContentIntent(contentIntent);
		bob.setAutoCancel(true);
		bob.setOngoing(true); // 放置在"正在运行"栏目中

		return bob.getNotification();
	}

	/**
	 * 游戏和大厅启动升级监听接口
	 */
	private DownLoadListener mDownLoadListener = new DownLoadListener() {
		@Override
		public void onProgress(int rate, String arg1) {
			if (mNM == null) {
				mNM = (NotificationManager) mContext
						.getSystemService(Context.NOTIFICATION_SERVICE);
			}
			mNM.notify(0, getUpdateNotification(rate));
			if (rate == 100) {
				rate = 0;
				mNM.cancel(0);
			}
		}

		@Override
		public void downloadFail(String arg0) {
			// TODO Auto-generated method stub

		}

	};

	private static final String MIGU_KEY = "MIGU_KEY";

	private void setMIGUChecked(boolean b) {
		Configs.setBooleanSharedPreferences(mContext, MIGU_KEY, b);
	}

	private boolean getMIGUChecked() {
		return Configs.getBooleanSharedPreferences(mContext, MIGU_KEY, true);
	}

	private void showMIGUDialog() {
		Dialog dialog = new AlertDialog.Builder(mContext)
				.setTitle("系统提示")
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setMessage(getDialogContext())
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								android.os.Process
										.killProcess(android.os.Process.myPid());
							}
						})
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}

						}).create();
		dialog.setCancelable(false);
		addCheckBox(dialog);
		dialog.show();
	}

	private void addCheckBox(Dialog dialog) {
		RelativeLayout container = new RelativeLayout(mContext);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
		CheckBox makePrimaryCheckBox = new CheckBox(mContext);
		makePrimaryCheckBox.setText("不再提醒我");
		makePrimaryCheckBox
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						setMIGUChecked(!isChecked);
					}
				});
		container.addView(makePrimaryCheckBox, lp);
		((AlertDialog) dialog).setView(container);

	}

	private String getDialogContext() {
		StringBuilder sb = new StringBuilder();
		sb.append(mContext.getString(R.string.popinfo));
		return sb.toString();
	}

}