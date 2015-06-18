package com.mykj.game;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Vector;

import org.cocos2dx.util.GameUtilJni;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.login.view.AccountManager;
import com.login.view.LoginViewCallBack;
import com.mingyou.accountInfo.AccountItem;
import com.mingyou.accountInfo.LoginInfoManager;
import com.mingyou.distributor.NetDefaultListener;
import com.mingyou.distributor.NetErrorListener;
import com.mingyou.distributor.NetPrivateListener;
import com.mingyou.distributor.NetSocketPak;
import com.mingyou.login.LoginSocket;
import com.mingyou.login.RecoverForDisconnect;
import com.mingyou.login.SocketLoginListener;
import com.mingyou.login.TcpShareder;
import com.mykj.andr.headsys.HeadManager;
import com.mykj.andr.model.DateDetailInfo;
import com.mykj.andr.model.HallDataManager;
import com.mykj.andr.model.NodeData;
import com.mykj.andr.model.RoomData;
import com.mykj.andr.model.SavedMessage;
import com.mykj.andr.model.UserInfo;
import com.mykj.andr.net.NetSocketManager;
import com.mykj.andr.provider.BackPackItemProvider;
import com.mykj.andr.provider.NewCardZoneProvider;
import com.mykj.andr.provider.NoticePersonProvider;
import com.mykj.andr.provider.UserCenterProvider;
import com.mykj.andr.task.GameTask;
import com.mykj.andr.ui.CustomActivity;
import com.mykj.andr.ui.fragment.AmusementFragment;
import com.mykj.andr.ui.fragment.CardZoneFragment;
import com.mykj.andr.ui.fragment.ChallengeFragment;
import com.mykj.andr.ui.fragment.Cocos2dxFragment;
import com.mykj.andr.ui.fragment.FragmentModel;
import com.mykj.andr.ui.fragment.FreeRoomInfoFragment;
import com.mykj.andr.ui.fragment.LoadingFragment;
import com.mykj.andr.ui.fragment.LoginViewFragment;
import com.mykj.andr.ui.fragment.LogonViewFragment;
import com.mykj.andr.ui.fragment.NodifyPasswordFragment;
import com.mykj.andr.ui.widget.CardZoneDataListener;
import com.mykj.andr.ui.widget.LoginAssociatedWidget;
import com.mykj.andr.ui.widget.Interface.InvokeViewCallBack;
import com.mykj.comm.io.TDataOutputStream;
import com.MyGame.Migu.R;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.CrossGeneralizeHelper;
import com.mykj.game.utils.Log;
import com.mykj.game.utils.Util;
import com.mykj.game.utils.UtilHelper;

public class FiexedViewHelper extends GlobalFiexParamer {

	private static final String TAG = "FiexedViewHelper";

	private static FiexedViewHelper instance = null;
	private Vector<NetSocketPak> proData = new Vector<NetSocketPak>(); // 游戏协议消息缓存

	private Handler reLinkHandler = new Handler(); // 游戏重连handler

	private static final int SOCKET_EXCEPTION = 0;

	public static final String BEAN = "bean";

	private Resources mResource;

	private boolean isInSkip = false; // 是否在界面跳转
	private long vilidClickTime = -1; // 上次有效点击界面跳转时间
	// --------------------handler
	// what----------------------------------------------------
	public static final int HANDLER_SWITCH = 1; // 游戏返回分区，提供jni调用接口

	public static final int HANDLER_LOGIN_VIEW = 2; // 游戏中返回到登录

	public static final int HANDLER_MATCH_VIEW = 3; // 游戏中返回到登录

	public static final int HANDLER_CHARGE_SUCCESS = 4; // 充值成功

	private static final int HANDLER_SOCKET_CLOSE = 10; // 网络断开

	private static final int HANDLER_LOGIN_FAIL = 11; // 登录失败

	private static final int HANDLER_LOGIN_ACTION = 12; // 登录动作

	private static final int HANDLER_EXIT_APPLICATION = 13; // 退出应用(杀进程)

	public static final int GAME_TYPE_UNKNOW = -1; // 未设定玩法
	public static final int GAME_TYPE_NORMAL = 0; // 普通玩法
	public static final int GAME_TYPE_XIAOBING = 1; // 小兵玩法
	public static final int GAME_TYPE_LAIZI = 2; // 癞子玩法
	public static final int GAME_TYPE_MMVIDEO = 3; // 美女玩法
	
	
	private int curGameType = GAME_TYPE_UNKNOW; // 当前玩法

	/**
	 * 获得游戏玩法 GAME_TYPE_UNKNOW 斗地主有以下 GAME_TYPE_NORMAL GAME_TYPE_LAIZI
	 * GAME_TYPE_XIAOBING 中的一种
	 * 
	 * @return 当前游戏玩法
	 */
	public int getGameType() {
		if (curGameType == GAME_TYPE_UNKNOW) { //只有未知玩法时候才会进入
			int usrId = FiexedViewHelper.getInstance().getUserId();
			int type = Util.getIntSharedPreferences(mAct, "gameType" + usrId,
					GAME_TYPE_UNKNOW);
			if (type != GAME_TYPE_UNKNOW) {
				setGameType(type);
			}
		}
		return curGameType;
	}

	/**
	 * 设置游戏玩法
	 * 
	 * @param type
	 *            新的游戏玩法
	 * 
	 */
	public void setGameType(int type) {
		if(type==GAME_TYPE_MMVIDEO){//去除美女玩法
			return;
		}
		if (curGameType != type) {
			curGameType = type;
			int usrId = FiexedViewHelper.getInstance().getUserId();
			Util.setIntSharedPreferences(mAct, "gameType" + usrId, curGameType);
			if (cardZoneFragment != null) {
				cardZoneFragment.setPlaytypeBackground((byte) type);
				cardZoneFragment.switchPlayMethod((byte) type);
			}

		}
	}

	/**
	 * 登录回调函数
	 * */
	private LoginViewCallBack mLoginViewCallBack = new LoginViewCallBack() {
		@Override
		public void loginsuccessed(Message msg) {
			invokeLoginSuccess();
		}

		@Override
		public void loginFailed(Message msg) {
			Log.e(TAG, "loginFailed");
			Message message = sHandler.obtainMessage();
			if (msg != null) {
				message.obj = msg.obj;
			}
			message.what = HANDLER_LOGIN_FAIL;
			sHandler.sendMessageDelayed(message, 500); // 延时0.5S
			// 以防界面切回不了logonview
		}

		@Override
		public void btnBackClick() {
			sHandler.sendEmptyMessage(HANDLER_EXIT_APPLICATION);
		}

		@Override
		public void btnCancel() {
			sHandler.sendEmptyMessage(HANDLER_LOGIN_FAIL);
		}

		@Override
		public void logout() {
		}

		@Override
		public void loginAction() {
			sHandler.sendEmptyMessage(HANDLER_LOGIN_ACTION);
		}

		@Override
		public void nativeLoginInfo(boolean isHasNativeLoginInfo) {

		}
	};

	public LoginViewCallBack getLoginViewCallBack() {
		return mLoginViewCallBack;
	}

	/**
	 * 私有构造函数
	 * 
	 * @param act
	 */
	private FiexedViewHelper() {

	}

	/**
	 * fragment调度器初始化
	 * 
	 * @param act
	 */
	public void init(FragmentActivity act) {
		mAct = act;
		mResource = mAct.getResources();
	}

	/**
	 * 单例类
	 * 
	 * @param act
	 * @return
	 */
	public static FiexedViewHelper getInstance() {
		if (instance == null) {
			instance = new FiexedViewHelper();
		}
		return instance;
	}

	/** 创建分区主入口 */
	public void loadCreateView() {
		/** 注册网络监听 */
		registerListener();
		/** 初始化UI */
		initializeView();
	}

	/**
	 * @Title: Sendsub_As_User_Exit
	 * @Description: 发送退出登录 发送201----106
	 * @version: 2011-11-23 上午11:31:12
	 */
	public void sendSUB_AS_USER_EXIT(int userID) {
		// 构造数据包
		TDataOutputStream tdous = new TDataOutputStream(false);
		tdous.writeInt(userID);
		tdous.writeInt(AppConfig.gameId);
		NetSocketPak mConsumptionSock = new NetSocketPak(MDM_LOTTERY,
				LSUB_CMD_USER_LOGON_REQ, tdous);
		// 发送协议
		NetSocketManager.getInstance().sendData(mConsumptionSock);
		// 清理协议对象
		mConsumptionSock.free();
	}

	/**
	 * 
	 * 网络协议监听
	 */
	private void registerListener() {
		// 一进来进监听
		receiveDefaultSocket();

		registerOtherAddressLogin();
		registerSystemPopMsg(sHandler);

		AppConfig.isReceiveLoginGiftSwitch = false;
		registerLoginGiftSwitch();
		// 2013-01-30登陆送数据
		loginGiftPack();
		// 抽奖送数据
		luckyDrawPack();
		// 注册登录成功后回调
		CardZoneDataListener.getInstance(mAct).setInvokeViewCallBack(
				new InvokeViewCallBack() {
					@Override
					public void skipToCocods2dView() {

					}

					@Override
					public void skipToCardZoneView() {
						skipToFragment(CARDZONE_VIEW);
					}

					@Override
					public void skipToLoginView() {

					}

					@Override
					public void hallComeInSuccess() {
						// 登录成功操作：切换游戏-->请求分区数据
						querySwitchGame(HallDataManager.getInstance()
								.getUserMe().userID, AppConfig.gameId, sHandler);

					}
				});
	}

	/**
	 * 初始化UI界面
	 */
	private void initializeView() {
		mAct.setContentView(R.layout.cardzone_activity_main);

		fragmentManager = ((FragmentActivity) mAct).getSupportFragmentManager();// fragment管理

		// 初始化登录模块
		final int lauchTag = AppConfig.getLaunchType();
		final String appVer = Util.getVersionName(mAct);
        
		byte playId=(byte)getGameType();
		if(playId==GAME_TYPE_UNKNOW){
			playId=GAME_TYPE_NORMAL;
		}
		
		AccountManager.getInstance().initialize(mAct,
				AccountManager.SOCKET_LOGIN_TAG, lauchTag, AppConfig.gameId,
				AppConfig.channelId, AppConfig.childChannelId, appVer,
				Util.getProtocolCode(AppConfig.ZONE_VER),
				playId,
				Util.getICCID(mAct));

		// 显示登录动画界面fragment
		if (AccountManager.getInstance().enableQuickEntrance()) {
			skipToFragment(LOGIN_VIEW);
			if (loginViewFragment != null) {
				loginViewFragment.setLoginText(mResource
						.getString(R.string.ddz_into_game));
				loginViewFragment.quickLogin();
			}
		} else {
			skipToFragment(LOGON_VIEW);
		}
	}

	/***
	 * @Title: invokeLoginSuccess
	 * @Description: 登录成功后相关操作
	 * @version: 2013-1-7 上午10:31:47 登录成功后回调
	 */
	private void invokeLoginSuccess() {
		receiveErrorListener();

		setUserInfo();

		NewCardZoneProvider.getInstance().clearCardZoneProvider();
		// 从大厅进来调用，进而回调到请求分区数据
		CardZoneDataListener.getInstance(mAct).hallComeInSuccess();

		// 请求商城列表信息
		// if(!GoodsItemProvider.getInstance().getFinish()){
		((CustomActivity) mAct).requestMarketList(); // 商城道具跟用户ID无关，切换账号不用重复请求
		// }
		// 请求头像列表
		HeadManager.getInstance().clearHeadMarketInfo();
		HeadManager.getInstance().clearHeadPackInfo();
		HeadManager.getInstance().requestHeadMarketList(mAct);
		
		//((CustomActivity) mAct).yunvaLiveInit();
		
		((CustomActivity) mAct).ininCloudPay();
		// 请求用户推广码
		((CustomActivity) mAct).reqSpKey();

		// 请求快捷购买道具ID
		((CustomActivity) mAct).reqPropId();

		// 请求美女视频商品列表
		//((CustomActivity) mAct).reqMmVideoGoods();	
		
		// 请求VIP 数据
		((CustomActivity) mAct).reqVipData();
		((CustomActivity) mAct).reqOnlineServerUrl();

		((CustomActivity) mAct).reqSaveToken();

		((CustomActivity) mAct).reqPushTags();
		//((CustomActivity) mAct).reqPingCooConfig();  //去掉宾谷盒子
		// 请求我的物品信息(刚进来不请求)
		// ((CustomActivity)
		// mAct).requestBackPackList(Community.getSelftUserInfo().userId);

		// 初始化用户头像
		// HeadManager.getInstance().setHeadId(HallDataManager.getInstance().getUserMe().getFaceId());
	}

	public void saveToken() {
		String token = "";
		String str = LoginInfoManager.getInstance().getToken();
		if (!Util.isEmptyStr(str)) {
			try {
				token = URLEncoder.encode(str, "UTF-8");
				Util.setStringSharedPreferences(mAct, "token", token);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
	}

	/***
	 * @Title: quickMatcheRoom
	 * @Description: 速配
	 * @version: 2012-9-18 下午06:46:10
	 */
	public void quickMatcheRoom(NodeData node) {
		// 速配游戏请求
		if (cardZoneFragment != null) {
			cardZoneFragment.gameRoomAssociated.quickMatchNode(node);
		}
	}

	/**
	 * 切换账号，关闭网络，清除数据
	 */
	public void goToReLoginView() {
		DateDetailInfo.setDateDetailInfo(null);
		AppConfig.spKey = null;
		AppConfig.personInfoList.clear();

		UserCenterProvider.getInstance().setUserCenterData(null); // 用户中心数据至空

		AppConfig.isReceive = false;
		curGameType = GAME_TYPE_UNKNOW;

		UserInfo user = new UserInfo();
		HallDataManager.getInstance().setUserMe(user); // 重置用户信息

		if (amusementFragment != null) {
			amusementFragment.exitMatch();
			amusementFragment.exitLogin();
		}

		//((CustomActivity) mAct).yunvaLiveDestroy();
		
		NoticePersonProvider.getInstance().init();
		BackPackItemProvider.getInstance().init();

		TcpShareder.getInstance().setNetErrorListener(null); // 移除网络错误监听，防止自动断线重连
		LoginSocket.getInstance().closeNet();
		TcpShareder.getInstance().clearListener();
		AppConfig.isReceiveLoginGiftSwitch = false;
		AppConfig.LoginGiftSwitch = AppConfig.LOGIN_GIFT_SHOW_EVERYTIME;

		skipToFragment(LOGON_VIEW);
	}

	/**
	 * 显示登录游戏loading界面
	 */
	public void showAddCocos2dLoading() {
		// 移除游戏loading
		// skipToFragment(Cocos2dLoadingFragment.TAG);
	}

	/**
	 * 移除登录游戏loading界面
	 */
	public void removeCococs2dLoading() {
		// skipToFragment(Cocos2dxFragment.TAG);
		// Cocos2dxFragment.getInstance().clearCocos2dBG();
	}

	/**
	 * 游戏返回分区
	 */
	public void cocosReturnToCardZone() {
		// 任务清理
		GameTask.getInstance().clrearTask();
		requestUserBean();
		// -----
		skipToFragment(CARDZONE_VIEW);
		RoomData room = HallDataManager.getInstance().getCurrentRoomData(); // 获取速配成功进入房间保存的房间信息
		if (room != null) {
			// 请求离开房间
			LoginAssociatedWidget.getInstance().exitRoom(room.RoomID);
		}

		switch (CardZoneDataListener.NODE_DATA_PROTOCOL_VER) {
		case CardZoneDataListener.VERSION_1:// 列表协议第一版，每个节点单独请求
			break;
		case CardZoneDataListener.VERSION_2:// 列表协议第二版，支持所有列表数据一次请求完成，本地缓存列表数据
			break;
		case CardZoneDataListener.VERSION_3:// 列表协议第三版，支持斗地主3种玩法，列表数据zip压缩，本地缓存列表数据
			int playId = FiexedViewHelper.getInstance().getGameType();
			RoomData roomData = HallDataManager.getInstance()
					.getCurrentRoomData();
			int playGameId = roomData.playId;
			if (playGameId != playId && playId != GAME_TYPE_UNKNOW) { // 不为unknow才设置，否则表示用户没设置过，即可能是一键游戏进入的
				setGameType(playGameId);
			}
			break;
		default:
			break;
		}

	}

	public void cocosReturnToMatchView() {
		// 任务清理
		GameTask.getInstance().clrearTask();
		// FiexedViewHelper.getInstance().requestUserBean();
		RoomData room = HallDataManager.getInstance().getCurrentRoomData(); // 获取速配成功进入房间保存的房间信息
		if (room != null) {
			// 请求离开房间
			LoginAssociatedWidget.getInstance().exitRoom(room.RoomID);
		}
		FiexedViewHelper.getInstance().skipToFragment(
				FiexedViewHelper.AMUSEMENT_ROOM);
		NodeData node=HallDataManager.getInstance().getCurrentNodeData();
		if(FiexedViewHelper.getInstance().amusementFragment!=null
			&& node!=null){
			FiexedViewHelper.getInstance().amusementFragment.sendLoginDataId(node.dataID);
		}
	}

	/**
	 * 三个fragment显示控制 当一个显示时候，另外2个隐藏
	 * 
	 * @param tag
	 */
	public void fragmentDisplayControl(FragmentTransaction ft, int tag) {
		Fragment loadingfrag = fragmentManager
				.findFragmentById(R.id.loadingfragment);
		Fragment cardzonefrag = fragmentManager
				.findFragmentById(R.id.cardzonefragment);
		Fragment cocosfrag = fragmentManager
				.findFragmentById(R.id.cocos2dfragment);
		ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
		if (tag == CARDZONE_VIEW) { // 分区显示
			if (loadingfrag != null) {
				ft.remove(loadingfrag);
			}
			if (cocosfrag != null) {
				ft.remove(cocosfrag);
			}
			if (cardzonefrag != null && cardzonefrag.isHidden()) {
				ft.show(cardzonefrag);
			}
		} else if (tag == COCOS2DX_VIEW) { // 游戏
			if (loadingfrag != null) {
				ft.hide(loadingfrag);
			}
			if (cardzonefrag != null) {
				ft.hide(cardzonefrag);
			}
			if (cocosfrag != null && cocosfrag.isHidden()) {
				ft.show(cocosfrag);
			}

		} else if (tag == LOGIN_VIEW) { // loading for 登录
			if (cardzonefrag != null) {
				ft.remove(cardzonefrag);
			}
			if (cocosfrag != null) {
				ft.remove(cocosfrag);
			}
			if (loadingfrag != null && loadingfrag.isHidden()) {
				ft.show(loadingfrag);
			}

		} else if (tag == LOGON_VIEW) { // loading
			if (cardzonefrag != null) {
				ft.remove(cardzonefrag);
			}
			if (cocosfrag != null) {
				ft.remove(cocosfrag);
			}
			if (loadingfrag != null && loadingfrag.isHidden()) {
				ft.show(loadingfrag);
			}

		} else { // loading
			if (cardzonefrag != null) {
				ft.hide(cardzonefrag);
			}
			if (cocosfrag != null) {
				ft.remove(cocosfrag);
			}
			if (loadingfrag != null && loadingfrag.isHidden()) {
				ft.show(loadingfrag);
			}

		}
	}

	/**
	 * 
	 * 注：此方法仅在fragment的resume和hiddenChange的时候调用，以设置当前显示的fragment
	 * 
	 * @param tag
	 * 
	 */
	public void setFragment(int tag) {
		if (tag >= FRAGMENT_START && tag <= FRAGMENT_END) {
			curFragmentFlag = tag;
		} else {
			Log.e(TAG, "set wrong fragment TAG " + tag);
		}
		isInSkip = false;
	}

	/**
	 * 跳转到指定fragment 若当前不在跳转中则可进行跳转，若当前已在跳转中则新跳转调用不成功
	 * 
	 * @param tag
	 */
	public boolean skipToFragment(int tag) {
		long clickTime = System.currentTimeMillis();
		// 如果超过2秒，强制设为可以跳转，避免线程导致卡死在某个界面无法跳转
		if (clickTime - vilidClickTime > 2000) {
			isInSkip = false;
		}
		if (isInSkip) {
			return false;
		}
		if (tag != curFragmentFlag) {
			isInSkip = true;
			vilidClickTime = clickTime;
			final FragmentTransaction ft = fragmentManager.beginTransaction();
			if (tag == LOGON_VIEW) {
				// 还原断线控制变量
				isReContinueGameing = false;
				RecoverForDisconnect.isSendBeginReconnect = false;
				isParseNetError = true;
				// ----
				if (fragmentManager.findFragmentByTag(LogonViewFragment.TAG) == null) {
					logonViewFragment = new LogonViewFragment();
					logonViewFragment.setLoginCallBack(mLoginViewCallBack);
					ft.replace(R.id.loadingfragment, logonViewFragment,
							LogonViewFragment.TAG);
					ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
				}
				fragmentDisplayControl(ft, tag);
				ft.commitAllowingStateLoss();
				registerListener(); // 重新注册监听消息
				// curFragmentFlag = LOGON_VIEW;
			} else if (tag == LOGIN_VIEW) {
				// 还原断线控制变量
				isReContinueGameing = false;
				RecoverForDisconnect.isSendBeginReconnect = false;
				isParseNetError = true;
				// ----
				if (fragmentManager.findFragmentByTag(LoginViewFragment.TAG) == null) {
					loginViewFragment = new LoginViewFragment();
					loginViewFragment.setText(mAct.getResources().getString(
							R.string.ddz_into_game));
					loginViewFragment.setLoginText(mAct.getResources()
							.getString(R.string.ddz_into_game));
					loginViewFragment.setLoginCallBack(mLoginViewCallBack);
					ft.replace(R.id.loadingfragment, loginViewFragment,
							LoginViewFragment.TAG);
					ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
				}
				fragmentDisplayControl(ft, tag);
				ft.commitAllowingStateLoss();
				// curFragmentFlag = LOGIN_VIEW;
			} else if (tag == CARDZONE_VIEW) {
				if (fragmentManager.findFragmentByTag(CardZoneFragment.TAG) == null) {
					cardZoneFragment = new CardZoneFragment();
					ft.replace(R.id.cardzonefragment, cardZoneFragment,
							CardZoneFragment.TAG);
					ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				}
				fragmentDisplayControl(ft, tag);
				ft.commitAllowingStateLoss();
				requestUserBean();// 跳到分区主动请求用户乐豆 --20130304 colin--新添加
				// curFragmentFlag = CARDZONE_VIEW;
			} else if (tag == LOADING_VIEW) {
				if (fragmentManager.findFragmentByTag(LoadingFragment.TAG) == null) {
					loadingFragment = new LoadingFragment();
					ft.replace(R.id.loadingfragment, loadingFragment,
							LoadingFragment.TAG);
					ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

				}
				fragmentDisplayControl(ft, tag);
				ft.commitAllowingStateLoss();
				// curFragmentFlag = LOADING_VIEW;
			} else if (tag == COCOS2DX_VIEW) {
				if (fragmentManager.findFragmentByTag(Cocos2dxFragment.TAG) == null) {
					cocos2dxFragment = new Cocos2dxFragment();
					ft.replace(R.id.cocos2dfragment, cocos2dxFragment,
							Cocos2dxFragment.TAG);
					ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

				}

				// fragmentDisplayControl(ft, tag);
				ft.commitAllowingStateLoss();
				// curFragmentFlag = COCOS2DX_VIEW;

			} else if (tag == NODIFY_ACCOUNT_VIEW) {
				if (fragmentManager
						.findFragmentByTag(NodifyPasswordFragment.TAG) == null) {
					nodifyPasswordFragment = new NodifyPasswordFragment();
					ft.replace(R.id.loadingfragment, nodifyPasswordFragment);

				}
				fragmentDisplayControl(ft, tag);
				ft.commit();
				// curFragmentFlag = NODIFY_ACCOUNT_VIEW;
			} else if (tag == FREE_ROOM) {
				if (fragmentManager.findFragmentByTag(FreeRoomInfoFragment.TAG) == null) {
					freeRoomInfoFragment = new FreeRoomInfoFragment();
					ft.replace(R.id.loadingfragment, freeRoomInfoFragment);
				}
				fragmentDisplayControl(ft, tag);
				ft.commit();
				// curFragmentFlag = FREE_ROOM;
			} else if (tag == CHALLENGE_ROOM) {

				if (fragmentManager.findFragmentByTag(ChallengeFragment.TAG) == null) {
					challengeFragment = new ChallengeFragment();
					ft.replace(R.id.loadingfragment, challengeFragment);
				}
				fragmentDisplayControl(ft, tag);
				ft.commit();
				// curFragmentFlag = CHALLENGE_ROOM;

			} else if (tag == AMUSEMENT_ROOM) {

				if (fragmentManager.findFragmentByTag(AmusementFragment.TAG) == null) {
					amusementFragment = new AmusementFragment();
					ft.replace(R.id.loadingfragment, amusementFragment);
				}
				fragmentDisplayControl(ft, tag);
				ft.commit();
				// curFragmentFlag = AMUSEMENT_ROOM;

			}
		}
		return true;
	}

	public void onBackPressed() {
		FragmentModel frag = null;
		switch (curFragmentFlag) {
		case LOGIN_VIEW:
			frag = loginViewFragment;
			break;
		case CARDZONE_VIEW:
			frag = cardZoneFragment;
			break;
		case COCOS2DX_VIEW:
			frag = cocos2dxFragment;
			break;
		case NODIFY_ACCOUNT_VIEW:
			frag = nodifyPasswordFragment;
			break;
		case LOGON_VIEW:
			frag = logonViewFragment;
			break;
		case LOADING_VIEW:
			frag = loadingFragment;
			break;
		case FREE_ROOM:
			frag = freeRoomInfoFragment;
			break;
		case CHALLENGE_ROOM:
			frag = challengeFragment;
			break;
		case AMUSEMENT_ROOM:
			frag = amusementFragment;
			break;
		default:
			break;
		}

		if (frag != null) {
			frag.onBackPressed();
		}
	}

	/**
	 * 获取用户ID
	 * 
	 * @return -1 表示失败
	 */
	public int getUserId() {
		int userId = -1;
		UserInfo userInfo = HallDataManager.getInstance().getUserMe();

		if (userInfo != null) {
			userId = userInfo.userID;
		}
		return userId;

	}

	/**
	 * 获取用户Token
	 * 
	 * @return
	 */
	public String getUserToken() {
		String userToken = "";
		UserInfo userInfo = HallDataManager.getInstance().getUserMe();

		if (userInfo != null) {
			userToken = userInfo.Token;
		}
		return userToken;
	}

	/**
	 * 获取用户昵称
	 * 
	 * @return
	 */
	public String getUserNickName() {
		String nickName = "";
		UserInfo userInfo = HallDataManager.getInstance().getUserMe();

		if (userInfo != null) {
			nickName = userInfo.nickName;
		}
		return nickName;
	}

	/**
	 * 获取用户userStatusBit
	 * 
	 * @return
	 */
	public int getUserStatusBit() {
		int userStatusBit = -1;
		UserInfo userInfo = HallDataManager.getInstance().getUserMe();

		if (userInfo != null) {
			userStatusBit = userInfo.statusBit;
		}
		return userStatusBit;

	}

	
	/**第24位
	 * 比赛排名节目是否显示美女视频
	 * @return
	 */
	public  boolean isOpenMMVideo(){
		int statusBit =getUserStatusBit();
		int flagBit = 1<<23;//第24位
		if((statusBit & flagBit)!=0){
			return true;
		}
		return false;
	}
	
	
	
	/**第31位
	 * 分区页面是否弹出交叉推广框
	 * @return
	 */
	public  boolean isOpenCrossSpread(){
		int statusBit =getUserStatusBit();
		int flagBit = 1<<30;//第31位
		if((statusBit & flagBit)!=0){
			return true;
		}
		return false;
	}
	
	
	
	
	
	/**
	 * 获取当前fragment
	 */
	public int getCurFragment() {
		return curFragmentFlag;
	}

	/**
	 * 当前是否正在界面跳转
	 * 
	 * @return
	 */
	public boolean isSkipFragment() {
		return isInSkip;
	}

	/***
	 * @Title: requestUserBean
	 * @Description:请求更新用户乐豆
	 * @version: 2013-1-9 上午11:35:20
	 */
	public void requestUserBean(final Handler handler) {
		((CustomActivity) mAct).getUserBeanProtocol(handler);
	}

	/**
	 * 默认请求分区乐豆数量
	 */
	public void requestUserBean() {
		if (cardZoneFragment != null) {
			requestUserBean(cardZoneFragment.cardZoneHandler);
		}
	}

	/**
	 * 快速开始
	 */
	private long currentTime = 0;

	public void quickGame() {
		if (cardZoneFragment != null) {
			if (cardZoneFragment.hallAssociated != null) {
				if ((System.currentTimeMillis() - currentTime) > 2500) {
					currentTime = System.currentTimeMillis();
					cardZoneFragment.hallAssociated.quickGame();
				}
			}
		}
	}

	/**
	 * 默认网络消息接收 派发给游戏
	 */
	public void receiveDefaultSocket() {
		NetDefaultListener netdefListener = new NetDefaultListener() {
			@Override
			public boolean doReceive(NetSocketPak data) {
				try {
					Log.v(TAG, "发送到游戏，主：" + data.getMdm_gr());
					Log.v(TAG, "发送到游戏，子：" + data.getSub_gr());
					if (getCurFragment() == COCOS2DX_VIEW) {
						byte[] buffer = data.getBufferByte();
						GameUtilJni.parseGameNetData(buffer, buffer.length);
						return true;
					}
					proData.addElement(data);
				} catch (Exception e) {
					Log.v(TAG, "NetDefaultListener is error!");
				}
				return false;
			}

			@Override
			public boolean doError(Exception e) {
				return false;
			}
		};
		TcpShareder.getInstance().setTcpDefaultListener(netdefListener); // 默认监听器
	}

	/**
	 * 标记是否可以处理网络异常 谨慎使用 只能在断线重连弹出重连失败提示框后，出现的网络异常不能继续处理 默认为true(需要处理网络异常)
	 */
	private boolean isParseNetError = true;

	/*************************************
	 * @Title: receiveErrorListener
	 * @Description: 网络错误监听器，此监听器只能在登陆成功事件中注册
	 * @version: 2012-12-17 下午01:42:11
	 **************************************/
	public void receiveErrorListener() {
		NetErrorListener netErrorListener = new NetErrorListener() {
			public boolean doNetError(final Exception e) {
				if (e != null) {
					if (!isParseNetError) {
						Log.e(TAG, "收到网络异常，但已弹出断线重连失败提示框，不能继续网络异常");
						return true;// 不处理网络错误（在断线重连弹出重连失败提示框后，出现的网络异常不能继续处理）
					}

					switch (AppConfig.gameId) {
					case AppConfig.GAMEID_DDZ:
						if (RecoverForDisconnect.getInstance().isReConnect()) {
							isReContinueGameing = false;
							if (getCurFragment() == COCOS2DX_VIEW) {
								RoomData roomData=HallDataManager.getInstance().getCurrentRoomData();
								final int playId=roomData.playId;
								/*if(playId==3){
									GameUtilJni.parseNetError(SOCKET_EXCEPTION,
											mResource.getString(R.string.ddz_notice_game_net_error)); // 通知游戏
								}else*/{
									RecoverForDisconnect.isSendBeginReconnect = true; // 一定要设置为true
									GameUtilJni.onZoneEvent(GameUtilJni.EventZone_BeginReconnect); // 通知游戏中断线重回开始
								}
							} else if (getCurFragment() == CARDZONE_VIEW) {
								reCutLoginAgain(false); // 非游戏断线重回
							} else {
								sHandler.sendEmptyMessage(HANDLER_SOCKET_CLOSE);
							}
						} else {
							GameUtilJni.parseNetError(SOCKET_EXCEPTION,
										mResource.getString(R.string.ddz_notice_game_net_error)); // 通知游戏
						}
						break;

					default:
						sHandler.sendEmptyMessage(HANDLER_SOCKET_CLOSE);
						break;
					}

				}
				return true;
			}
		};
		TcpShareder.getInstance().setNetErrorListener(netErrorListener);
	}

	/** 防止连续多次的断线重连请求，必须等待上一次请求流程执行结束后 */
	private boolean isReContinueGameing = false;

	/**
	 * 发起断线重连操作
	 * 
	 * @param bool
	 *            true:游戏中断线重连，false:分区中断线重连
	 */
	public void reCutLoginAgain(final boolean bool) {
		if (isReContinueGameing) {
			Log.e(TAG, "UI 层 收到重连开始reCutLoginAgain--Error-已存在正在执行的断线重连流程");
			return;
		}
		isReContinueGameing = true;
		final SocketLoginListener listener = new SocketLoginListener() {

			public void onSuccessed(Message msg) {
				isReContinueGameing = false;
			}

			public void onFiled(Message msg, int arg) {
				isReContinueGameing = false;
				RecoverForDisconnect.isSendBeginReconnect = false;
				reLinkHandler.post(new Runnable() {
					public void run() {
						OnClickListener listener = new OnClickListener() {
							public void onClick(View v) {
								goToReLoginView();
								isParseNetError = true; // 继续处理网络异常
							}
						};
						isParseNetError = false; // 暂时不处理任何网络异常
						UtilHelper.showCustomDialog(mAct, mResource
								.getString(R.string.ddz_web_connetion_failed),
								listener);
					}
				});
			}
		};
		RoomData room = HallDataManager.getInstance().getCurrentRoomData();
		// -----开启断线重连-----
		RecoverForDisconnect.getInstance().start(mAct, listener, bool,
				room != null ? room.RoomID : 0);
	}

	/***
	 * @Title: registerOtherAddressLogin
	 * @Description: 注册异地登陆接受协议
	 * @version: 2013-1-11 下午06:33:15
	 */
	private void registerOtherAddressLogin() {
		// 定义接受数据的协议
		short[][] parseProtocol = { { MDM_LOGIN, MSUB_MULTI_LOGIN } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				sHandler.sendEmptyMessage(HANDLER_OTHER_ADDRESS_LOGIN);
				TcpShareder.getInstance().setNetErrorListener(null);
				return true;
			}
		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		nPListener.setOnlyRun(true);
	}

	/**
	 * 
	 */
	public void gotoGame(int mode) {
		CrossGeneralizeHelper.getInstance().markIntoGameTime();
		GameUtilJni.intoGame(HallDataManager.getInstance().getUserMe(), mode);
		RecoverForDisconnect.isSendBeginReconnect = false; // 重置断线提示界面控制
		while (!proData.isEmpty()) {
			NetSocketPak sendData = proData.remove(0);
			byte[] bufData = sendData.getBufferByte();
			GameUtilJni.parseGameNetData(bufData, bufData.length);
		}

	}

	public void exitGame() {
		// 后台绑定流程是这样的。
		// 1.修改密码成功后，退出游戏，同时发起后台绑定
		// 2.普通账号登陆成功后判断muid是否为0，如果是0，发起后台绑定并退出，否则直接退出
		UserInfo userInfo = HallDataManager.getInstance().getUserMe();
		stopSystemPopDialog();
		/*if (userInfo != null) {
			if (userInfo.loginType == AccountItem.ACC_TYPE_TEMP
					&& !LoginInfoManager.getInstance().isBind()) {
				skipToFragment(NODIFY_ACCOUNT_VIEW);
			} else {
				GameUtilJni.exitApplication();
			}
		} else {
			GameUtilJni.exitApplication();
		}*/
		GameUtilJni.exitApplication();
	}

	private static SavedMessage savedMsg;

	public static void setSavedMessage(SavedMessage msg) {
		savedMsg = msg;
	}

	public static SavedMessage getSavedMessage() {
		return savedMsg;
	}

	/**
	 * fragment 调度handler
	 */
	@SuppressLint("HandlerLeak")
	public Handler sHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLER_SWITCH: // 返回分区
				long bean = msg.getData().getLong(FiexedViewHelper.BEAN);
				cardZoneFragment.setUserBean(bean);
				HallDataManager.getInstance().getUserMe().setBean((int) bean);
				cocosReturnToCardZone();
				SavedMessage savedMsg = getSavedMessage();
				if (savedMsg != null) {
					boolean isQuickGame = savedMsg.mIsQuickGame;
					int propId = savedMsg.mPropId;
					String propMessage = savedMsg.mPropMessage;
					if (isQuickGame) {
						FiexedViewHelper.getInstance().quickGame();
					} else {
						FiexedViewHelper.getInstance().cardZoneFragment
								.showQuickBuyDialog(propId, propMessage,
										savedMsg.ensureBtnStr,
										savedMsg.cancelBtnStr);
					}
					FiexedViewHelper.setSavedMessage(null);
				} else {
					CrossGeneralizeHelper.getInstance().handleUserTiredWarm(
							mAct);
				}

				break;
			case HANDLER_LOGIN_VIEW: // 游戏返回到登陆界面
				goToReLoginView();
				break;
			case HANDLER_MATCH_VIEW:
				cocosReturnToMatchView(); // 游戏返回比赛报名界面
				break;
			case HANDLER_SOCKET_CLOSE: // socket连接关闭
				if (getCurFragment() == LOGON_VIEW) { // 如果是登录模块
					return;
				} else {
					// 如果存在屏蔽界面，则移除
					String message = mResource
							.getString(R.string.ddz_web_connetion_cross);
					UtilHelper.showSocketAlertDialog(message, mAct,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// 设置网络
									mAct.startActivity(new Intent(
											android.provider.Settings.ACTION_WIRELESS_SETTINGS));
									GameUtilJni.exitApplication();
								}
							}, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// 退出游戏
									GameUtilJni.exitApplication();
								}
							});
				}
				// 1:判断当前cocos2d-x默认加载界面是否呈现
				break;

			case HANDLER_LOGIN_FAIL:
				String errStr = (String) msg.obj;
				if (!Util.isEmptyStr(errStr)) {
					Toast.makeText(mAct, errStr, Toast.LENGTH_SHORT).show();
				}

				goToReLoginView();

				break;
			case HANDLER_LOGIN_ACTION:
				skipToFragment(LOGIN_VIEW);
				if (loginViewFragment != null) {
					// loginViewFragment.startLoadingAnimation();

				}
				break;
			case HANDLER_EXIT_APPLICATION:
				GameUtilJni.exitApplication();
				break;
			case HANDLER_OTHER_ADDRESS_LOGIN: // 异地登陆
				// 改成单按钮，返回切换账号，而不是退出应用
				UtilHelper.showCustomDialog(mAct, mResource
						.getString(R.string.ddz_account_load_other_way),
						new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								goToReLoginView();
							}
						});
				break;

			case HANDLER_REQUEST_CARD_ZONE_DATA: // 切换游戏成功后，请求分区数据
				if (loginViewFragment != null) {
					loginViewFragment
							.setBtnCancelOnclick(new OnClickListener() {
								@Override
								public void onClick(View v) {
									GameUtilJni.exitApplication();
								}
							});
					loginViewFragment.setText(mResource
							.getString(R.string.ddz_into_game));
					loginViewFragment.setLoginText(mResource
							.getString(R.string.ddz_into_game));
				}
				// 接口回调，请求分区数据
				CardZoneDataListener.getInstance(mAct)
						.invokeReveiveCardZoneData();
				break;
			case HANDLER_CHANGE_GAME_ERROR: // 切换游戏失败
				String errorMsg = "";
				if (msg.obj != null) {
					errorMsg = String.valueOf(msg.obj);
				}
				UtilHelper.showCustomDialog(mAct, errorMsg,
						new OnClickListener() {
							@Override
							public void onClick(View v) {
								GameUtilJni.exitApplication();
							}
						});

				break;
			case MSUB_SYSMSG_POP_MSG: // 显示系统弹出框
				showSystemPopDialog(mAct);
				break;
			case CLEAR_COCOS2D_BACKGOUND:
				FragmentTransaction ft = fragmentManager.beginTransaction();
				// ft.setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out);
				fragmentDisplayControl(ft, COCOS2DX_VIEW);
				ft.commit();
				break;
			case HANDLER_CHARGE_SUCCESS:
				startGame = true;
				requestUserBean();
				break;
			default:
				break;
			}
		}
	};

	private boolean isFragmentActivity = false;

	/**
	 * 设置是否是在承载fragment的Activity
	 * 
	 * @param isFrgAct
	 */
	public void setFragmentActivity(boolean isFrgAct) {
		isFragmentActivity = isFrgAct;
	}

	/**
	 * 是否是在承载fragment的Activity
	 * 
	 * @return
	 */
	public boolean isFragmentActivity() {
		return isFragmentActivity;
	}

	public boolean startGame = false;

}
