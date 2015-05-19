package com.mykj.andr.ui.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.login.utils.DensityConst;
import com.mingyou.distributor.NetPrivateListener;
import com.mingyou.distributor.NetSocketPak;
import com.mingyou.login.struc.DownLoadListener;
import com.mingyou.login.struc.NotifiyDownLoad;
import com.mingyou.login.struc.VersionInfo;
import com.mykj.andr.headsys.HeadManager;
import com.mykj.andr.logingift.TheGiftDialog;
import com.mykj.andr.model.ActionInfo;
import com.mykj.andr.model.AllNodeData;
import com.mykj.andr.model.GoodsItem;
import com.mykj.andr.model.HallDataManager;
import com.mykj.andr.model.MixtureInfo;
import com.mykj.andr.model.NewUIDataStruct;
import com.mykj.andr.model.NodeData;
import com.mykj.andr.model.UserCenterData;
import com.mykj.andr.model.UserInfo;
import com.mykj.andr.net.NetSocketManager;
import com.mykj.andr.pay.PayManager;
import com.mykj.andr.provider.ActionInfoProvider;
import com.mykj.andr.provider.GoodsItemProvider;
import com.mykj.andr.provider.MixInfoProvider;
import com.mykj.andr.provider.NewCardZoneProvider;
import com.mykj.andr.provider.ScrollDataProvider;
import com.mykj.andr.provider.UserCenterProvider;
import com.mykj.andr.ui.ActionActivity;
import com.mykj.andr.ui.BackPackActivity;
import com.mykj.andr.ui.CustomActivity;
import com.mykj.andr.ui.CustomDialog;
import com.mykj.andr.ui.FeedbackInfoActivity;
import com.mykj.andr.ui.GetTicketActivity;
import com.mykj.andr.ui.MMVideoBuyDialog;
import com.mykj.andr.ui.MarketActivity;
import com.mykj.andr.ui.MessageBoxActivity;
import com.mykj.andr.ui.MixGridActivity;
import com.mykj.andr.ui.PlaytypeSelectDialog;
import com.mykj.andr.ui.ServerCenterActivity;
import com.mykj.andr.ui.ServerDialog;
import com.mykj.andr.ui.ShareActivity;
import com.mykj.andr.ui.UpdateDialog;
import com.mykj.andr.ui.UserCenterActivity;
import com.mykj.andr.ui.adapter.CardZoneDropListAdapter;
import com.mykj.andr.ui.adapter.CardZoneViewPagerAdapter;
import com.mykj.andr.ui.adapter.NewCardZoneGridViewAdapter;
import com.mykj.andr.ui.fragment.LoadingFragment.NodeDataType;
import com.mykj.andr.ui.widget.ActionInfoWidget;
import com.mykj.andr.ui.widget.CardZoneDataListener;
import com.mykj.andr.ui.widget.CardZoneProtocolListener;
import com.mykj.andr.ui.widget.GameRoomAssociatedWidget;
import com.mykj.andr.ui.widget.HallAssociatedWidget;
import com.mykj.andr.ui.widget.LoginAssociatedWidget;
import com.mykj.andr.ui.widget.Interface.ActionInfoInterface;
import com.mykj.andr.ui.widget.Interface.GameRoomAssociatedInterface;
import com.mykj.andr.ui.widget.Interface.HallAssociatedInterface;
import com.mykj.andr.ui.widget.Interface.LoginAssociatedInterface;
import com.mykj.andr.ui.widget.Interface.OnArticleSelectedListener;
import com.mykj.comm.io.TDataInputStream;
import com.mykj.comm.io.TDataOutputStream;
import com.mykj.game.FiexedViewHelper;
import com.MyGame.Midlet.R;
import com.mykj.game.ddz.api.AnalyticsUtils;
import com.mykj.game.ddz.api.UC;
import com.mykj.game.moregame.MoregameActivity;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.CenterUrlHelper;
import com.mykj.game.utils.ChannelDataMgr;
import com.mykj.game.utils.Log;
import com.mykj.game.utils.Toast;
import com.mykj.game.utils.Util;
import com.mykj.game.utils.UtilHelper;

public class CardZoneFragment extends FragmentModel implements OnClickListener {
	public final static String TAG = "CardZoneFragment";

	private Activity mAct;

	private Resources mResource;

	// ---------------------------UI控件--------------------------------------------------
	private PopupWindow mPopupWindow;

	private View mPopView;
	/** 合成 */
	private ImageButton ibMix;
	/** 排行榜 */
	private ImageButton ibRank;
	/** 意见反馈 */
	private ImageButton ibFeedback;
	/** 帮助 */
	private ImageButton ibHelp;
	/** 版本信息 */
	private ImageButton ibShare;
	/** 点数专区 */
	private ImageButton ibPoint;
	/** 切换账号 */
	private ImageButton ibSwitch;
	/** 版本升级 */
	private ImageButton ibUpdate;
	/** 版本升级检查 */
	private ImageView imgUpdate;

	private ImageView imgNew;
	/** 消息盒子 **/
	private ImageButton imgBtnNotice;
	/** 活动 **/
	private Button btnAction;
	/** 活动动画 */
	private ImageView btnActAnim;
	/** 头像 **/
	public ImageView ivFace;
	/** 登录送 **/
	private Button btnLogin;
	/*** 玩法切换按钮 **/
	private Button btnPlayType;
	/** 兑换卷按钮 */
	private Button btnGetTicket;
	/** 小钱包 */
	private Button btnSmallMoneyPkg;
	/** 当前版本号 */
	private TextView tvVersion; 

	private ImageView btnServer;

	private ImageView imgPacketTag;
	private ImageView imgMsgTag;

	private ImageView imgQGameAnim;


	/** wanghj **/
	/** 开关界面 **/
	private Button btnSwitch;
	private ListView lvUnder;
	private View mDropView;
	private ViewPager mCardZonePager; // 分区页面pager
	private CardZoneDropListAdapter mAdapter; // 下拉抽屉
	private LinearLayout point_linear = null; // 页面圆点标示

	private CardZoneViewPagerAdapter adapter;

	// ----------------------------类成员变量区----------------------------------------
	private int sumOfDrawble = 0; // 页面圆点数量

	private static long mills = 0; // 按键响应计时

	/** 用户信息 */
	private UserInfo userInfo;

	private String uid;

	private String login_info;

	/** 昵称 **/
	private TextView tvuser_name = null;

	/** 乐豆 **/
	private TextView tvuser_bean = null;

	/** 兑换卷 **/
	private TextView tvuser_ticket = null;

	private TheGiftDialog theGiftDialog;

	/** 玩法切换对话框 */
	private PlaytypeSelectDialog playtypeDialog = null;
	/**
	 * fragment 返回view
	 */
	private View mView;

	/** 合成列表 */
	private MixtureInfo[] mixInfos;

	/** 当前数 **/
	private int currReturnMixtureNum = 0;

	/** 合成是否读取完毕 */
	private boolean recordMixtureType = false;

	/** 第一页是否卡片分区 */
	private boolean isFirstCard = false;

	/** 登录相关：接口 **/
	public LoginAssociatedInterface loginAssociated = null;

	/** 大厅UI相关：接口 **/
	public HallAssociatedInterface hallAssociated = null;

	/*** 与房间相关：接口 **/
	public GameRoomAssociatedInterface gameRoomAssociated = null;

	/** 活动专区相关：接口 ***/
	public ActionInfoInterface actionInfoInterface = null;

	/** 大厅版本类型(cocos2d-x中为3) **/
	public static final byte LOBBYTYPE = 3;

	// ------------------------------handler
	// what---------------------------------------

	private static final int HANDLER_MIX_QUERY_SUCCESS = 0;

	public static final int HANDLER_SIT_DOWN_FAIL = 1;
	public static final int HANDLER_USER_STATUS_SIT_DOWN_SUCCESS = 2;

	private static final int HANDLER_UPDATE_HEAD = 3;

	public static final int HANDLER_CHECK_UPDATE_COMPLETE = 4;

	private static final int HANDLER_UPDATE_FAIL = 5;
	public static final int HANDLER_GAME_PLAYER = 6; // 游戏玩家按钮显示处理
	private static final int HANDLER_GET_TICKET_SUCCESS = 7;

	public static final int HANDLER_SMALL_MONEYPKG = 8;

	// -----------------------------协议定义-----------------------------------------------
	/** 道具主协议 */
	private static final short LS_MDM_PROP = 17;

	/** 合成列表请求 */
	private static final short LSUB_CMD_COM_PRO_LIST_REQ = 802;

	/** 合成列表返回 */
	private static final short LSUB_CMD_COM_PRO_LIST_RESP = 803;

	public OnArticleSelectedListener mListener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.mAct = activity;
		this.mResource = mAct.getResources();
		try {
			mListener = (OnArticleSelectedListener) activity;
			mListener.onArticleSelected(cardZoneHandler);
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnArticleSelectedListener");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.cardzone_view, container, false);
		init(mView);
		return mView;
	}

	private boolean hasShowedCardZone = false; // 已经显示过分区界面

	private void firstShowCardZone() {
		// 请求用户乐豆
		FiexedViewHelper.getInstance().requestUserBean(cardZoneHandler);

		mCardZonePager.setCurrentItem(mCurPointViewIndex, false);
		UserInfo user = HallDataManager.getInstance().getUserMe();
		if (user.gender >= 0 && user.nickName != null) {
			setCardZoneUserInfo(user.gender, user.nickName);
		}

		showNewTag(); // 设置new_tag显示属性

		switch (CardZoneDataListener.NODE_DATA_PROTOCOL_VER) {
		case CardZoneDataListener.VERSION_1:// 列表协议第一版，每个节点单独请求
			NewCardZoneProvider.getInstance().resetCardZoneData();
			break;
		case CardZoneDataListener.VERSION_2:// 列表协议第二版，支持所有列表数据一次请求完成，本地缓存列表数据
			//resetQuickEntryCard();  //新增美女视频玩法去除
			break;
		case CardZoneDataListener.VERSION_3:// 列表协议第三版，支持斗地主3种玩法，列表数据zip压缩，本地缓存列表数据
			// 若还没有确定玩法，则要求选择一种玩法
			int playtype = FiexedViewHelper.getInstance().getGameType();
			if (playtype == FiexedViewHelper.GAME_TYPE_UNKNOW) {
				boolean playSwitch = AllNodeData.getInstance(mAct)
						.getPlayedSwitch();
				FiexedViewHelper.getInstance().setGameType(
						AllNodeData.getInstance(mAct).getPlayId());
				if (playSwitch) { // 第一次玩法切换快关 预留配置接口
					if (playtypeDialog == null) {
						playtypeDialog = new PlaytypeSelectDialog(mAct);
					}
					playtypeDialog.show();
				}

			} else {
				setPlaytypeBackground((byte) playtype);
				//resetQuickEntryCard((byte) playtype); //新增美女视频玩法去除
			}
			break;
		default:
			break;
		}
		hasShowedCardZone = true;
	}

	public void hideSmallMoneyPkgView() {
		btnSmallMoneyPkg.setVisibility(View.GONE);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!GoodsItemProvider.getInstance().hasSmallMoneyPkg(mAct)) {
			btnSmallMoneyPkg.setVisibility(View.GONE);
		} else {
			btnSmallMoneyPkg.setVisibility(View.VISIBLE);
		}

		/*
		 * 1.6.3新加1健游戏
		 */
		int statusBit = HallDataManager.getInstance().getUserMe().statusBit;
		int flagBit = 1 << 25; // 一键游戏标识位第26位
		if ((statusBit & flagBit) != 0) { // 需要一键游戏
			HallDataManager.getInstance().getUserMe().statusBit = (statusBit & (~flagBit)); // 清除标识
			switch (CardZoneDataListener.NODE_DATA_PROTOCOL_VER) {
			case CardZoneDataListener.VERSION_1:// 列表协议第一版，每个节点单独请求
				break;
			case CardZoneDataListener.VERSION_2:
				break;
			case CardZoneDataListener.VERSION_3:
				NewCardZoneProvider.getInstance().initCardZoneProvider(
						AllNodeData.getInstance(mAct).getPlayId());
				break;
			}
			FiexedViewHelper.getInstance().quickGame();
			return;
		} else {
			firstShowCardZone();
		}

		ivFace.setImageDrawable(HeadManager.getInstance().getZoneHead(mAct));
		HeadManager.getInstance().setUpdateHanler(cardZoneHandler,
				HANDLER_UPDATE_HEAD);

		VersionInfo vi = AppConfig.getVersionInfo();
		if (vi != null) {
			byte versionTag = vi._upgrade;
			if (imgNew != null) {
				if (versionTag == VersionInfo.UPGRADE_NEED) {
					imgNew.setVisibility(View.VISIBLE);
				} else {
					imgNew.setVisibility(View.INVISIBLE);
				}
			}

			if (imgUpdate != null) {
				if (versionTag == VersionInfo.UPGRADE_NEED) {
					imgUpdate.setVisibility(View.VISIBLE);
				} else {
					imgUpdate.setVisibility(View.INVISIBLE);
				}
			}
		} else {
			if (imgNew != null) {
				imgNew.setVisibility(View.INVISIBLE);
			}

			if (imgUpdate != null) {
				imgUpdate.setVisibility(View.INVISIBLE);
			}
		}
	}

	public void onHiddenChanged(boolean hidden) {
		// TODO Auto-generated method stub
		super.onHiddenChanged(hidden);
		if (!hasShowedCardZone && !hidden) {
			firstShowCardZone();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mCardZonePager = null;
		mDropView = null;
		mView = null;
	}

	@Override
	public int getFragmentTag() {
		return FiexedViewHelper.CARDZONE_VIEW;
	}

	@Override
	public void onBackPressed() {
		// 主分区主界面
		if(backToCardZoneFirst()){ //直接退出，不再返回分区
			return;
		}
		
		UtilHelper.showExitDialog(mAct, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// 可能有快捷购买，更多游戏
				if (v.getId() == R.id.img_quick_buy) {
					Log.e(TAG, "img_quick_buy");
					// 快捷购买
					// UtilHelper.showBuyDialog(mAct,AppConfig.propId,false,true);

					if (theGiftDialog != null && theGiftDialog.isShowing()) {
						return;
					}
					if (playtypeDialog == null) {
						playtypeDialog = new PlaytypeSelectDialog(mAct);
					}
					playtypeDialog.show();

				} else if (v.getId() == R.id.img_more_games) {
					// 交叉推广，记录渠道号
					ChannelDataMgr.getInstance().writeChannelToSDCard();

					// wanghj 2013-04-16 跳转到更多游戏界面，不是wap网页
					Intent intent = new Intent(mAct, MoregameActivity.class);
					startActivity(intent);
				}
			}
		}, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 若没有弹出退出强弹框，则退出游戏，否则由强弹框处理
				if (!FiexedViewHelper.getInstance().showExitSystemPopDialog()) {
					FiexedViewHelper.getInstance().exitGame();
				}

			}
		}, null);
	}

	private void showNewTag() {
		UserInfo user = HallDataManager.getInstance().getUserMe();
		String key_tag = user.nickName;
		String tag = Util.getStringSharedPreferences(mAct, key_tag,
				AppConfig.DEFAULT_TAG);
		String[] strs = tag.split("&");
		if (strs != null && strs.length == 3) {
			int act = Integer.parseInt(strs[0]);
			if (0 == act) {
				btnActAnim.setVisibility(View.VISIBLE);
				Animation animation = AnimationUtils.loadAnimation(mAct,
						R.anim.act_animation);
				btnActAnim.startAnimation(animation);
			} else if (1 == act) {
				btnActAnim.clearAnimation();
				btnActAnim.setVisibility(View.INVISIBLE);
			}

			int back = Integer.parseInt(strs[1]);
			if (0 == back) {
				imgPacketTag.setVisibility(View.VISIBLE);
			} else if (1 == back) {
				imgPacketTag.setVisibility(View.INVISIBLE);
			}

			int msg = Integer.parseInt(strs[2]);
			if (0 == msg) {
				imgMsgTag.setVisibility(View.VISIBLE);
			} else if (1 == msg) {
				imgMsgTag.setVisibility(View.INVISIBLE);
			}
		}
	}

	/****
	 * @Title: initView
	 * @Description: 初始化界面以及监听器
	 * @version: 2012-12-31 下午04:16:06
	 */
	private void init(View view) {
		loginAssociated = LoginAssociatedWidget.getInstance();
		hallAssociated = HallAssociatedWidget.getInstance();

		gameRoomAssociated = GameRoomAssociatedWidget.getInstance(mAct);
		gameRoomAssociated.receiveUserStatus();
		gameRoomAssociated.receiveRoomConfigData();

		actionInfoInterface = ActionInfoWidget.getInstance();

		CardZoneProtocolListener.getInstance(mAct).protocolInit(); // 添加监听协议

		AppConfig.loadGamePlayerProperties(mAct);

		// 请求活动中心数据,//暂时屏蔽2012-12-10
		actionInfoInterface.requestActionInfoList();

		initView(view); // 初始化分区主UI

		initPopWindow(); // 初始化抽屉菜单效果

		// MobilePayProtocal.requestConsumeCode(MOBILEPLATID,
		// COSUME_CODE_CMNET); //解析获取道具消费代码对照表（购买道具时需要）

		requestMixtureInfoList();

		if (loginAssociated != null) {
			// 每次登陆成功时候都请求断线信息(非每次启动CardZoneActivity时请求)
			loginAssociated.breakLine(LOBBYTYPE);
		}
		Log.v(TAG, "CardZoneFragment is OnCreateView");
	}

	/**
	 * 初始化界面UI
	 * 
	 * @param view
	 */
	private void initView(View view) {

		mCardZonePager = (ViewPager) view.findViewById(R.id.viewpager_card); // 初始化界面卡片
		byte playId=AllNodeData.getInstance(mAct).getPlayId();
		NewCardZoneProvider.getInstance().initCardZoneProvider(playId);
		
		List<View> views = getCardZoneViews();
		PagerAdapter adapter = new CardZoneViewPagerAdapter(views);
		mCardZonePager.setAdapter(adapter);
		mCardZonePager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int arg0) {

			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				if (arg0 == ViewPager.SCROLL_STATE_IDLE) {
					int cur = mCardZonePager.getCurrentItem();
					changePointView(cur);
					onSelectChanged(cur, false);
				}

			}
		});

		// 初始化卡片界面圆点
		point_linear = (LinearLayout) view.findViewById(R.id.point_linear);
		sumOfDrawble = views.size();
		mCurPointViewIndex = 0;
		for (int i = 0; i < sumOfDrawble; i++) {
			ImageView pointView = new ImageView(mAct);
			if (i == mCurPointViewIndex) {
				pointView.setBackgroundResource(R.drawable.feature_point_cur);
			} else {
				pointView.setBackgroundResource(R.drawable.feature_point);
			}
			point_linear.addView(pointView);
		}

		userInfo = HallDataManager.getInstance().getUserMe();
		uid = String.valueOf(userInfo.userID);
		/******************************
		 * 系统启动只请求一次消息盒子数据
		 *****************************/
		if (hallAssociated != null) {
			// TcpClient有变动，暂时屏蔽2012-12-10
			hallAssociated.requestSystemMessage(userInfo.userID);
		}

		// 喜报消息提示
		// ScrollDataProvider.getInstance(mAct).initialize(view.findViewById(R.id.linearBroad));
		// //喜报消息
		ScrollDataProvider.getInstance(mAct).initialize(
				view.findViewById(R.id.tvBroadcast));
		// 昵称，乐豆
		tvuser_name = (TextView) view.findViewById(R.id.tvuser_name);
		tvuser_name.setText(userInfo.nickName);
		tvuser_bean = (TextView) view.findViewById(R.id.tvuser_bean);
		tvuser_bean.setText(userInfo.bean + "");
		tvuser_ticket = (TextView) view.findViewById(R.id.tvuser_ticket);

		imgBtnNotice = (ImageButton) view.findViewById(R.id.new_ic_msg);
		// 活动
		btnAction = (Button) view.findViewById(R.id.btnAct);
		btnActAnim = (ImageView) view.findViewById(R.id.btnActAnim);

		imgPacketTag = (ImageView) view.findViewById(R.id.btnPacket_newTag); // 背包new_tag标签
		imgMsgTag = (ImageView) view.findViewById(R.id.new_ic_msg_newTag); // 系统消息new_tag标签

		btnLogin = (Button) view.findViewById(R.id.login_gift);
		btnPlayType = (Button) view.findViewById(R.id.playtype_select);
		btnGetTicket = (Button) view.findViewById(R.id.btnGettick);
		btnGetTicket.setOnClickListener(this);
		btnSmallMoneyPkg = (Button) view.findViewById(R.id.smallMoneyPkg);
		btnSmallMoneyPkg.setOnClickListener(this);

		tvVersion = (TextView) mView.findViewById(R.id.cardzone_main_version);

		btnServer= (ImageView) view.findViewById(R.id.btnServer);
		btnServer.setOnClickListener(this);

		ScaleAnimation sa = new ScaleAnimation(1f, 1.05f, 1f, 1.05f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		sa.setDuration(300);
		sa.setRepeatMode(Animation.REVERSE);
		sa.setRepeatCount(Animation.INFINITE);
		btnSmallMoneyPkg.setAnimation(sa);
		sa.start();

		imgQGameAnim= (ImageView) view.findViewById(R.id.imgQGameAnim);
		AnimationDrawable ad_bg = (AnimationDrawable) imgQGameAnim.getBackground();
		ad_bg.start();

		AnimationDrawable ad_fg = (AnimationDrawable) imgQGameAnim.getDrawable();
		ad_fg.start();

		switch (CardZoneDataListener.NODE_DATA_PROTOCOL_VER) {
		case CardZoneDataListener.VERSION_1:// 列表协议第一版，每个节点单独请求
		case CardZoneDataListener.VERSION_2:// 列表协议第二版，支持所有列表数据一次请求完成，本地缓存列表数据
			btnPlayType.setVisibility(View.INVISIBLE);
			break;
		case CardZoneDataListener.VERSION_3:// 列表协议第三版，支持斗地主3种玩法，列表数据zip压缩，本地缓存列表数据
			// btnPlayType.setVisibility(View.VISIBLE); //默认显示
			break;
		default:
			break;
		}

		imgNew = (ImageView) view.findViewById(R.id.imgNew);

		ivFace = (ImageView) view.findViewById(R.id.user_face);
		ivFace.setImageDrawable(HeadManager.getInstance()
				.getZoneHead(mAct));
		ivFace.setOnClickListener(this);

		// wanghj 开关层效果
		btnSwitch = (Button) view.findViewById(R.id.new_btn_switch);
		btnSwitch.setOnClickListener(this);
		mAdapter = new CardZoneDropListAdapter(getDropDodeDatas(), mAct);
		btnSwitch.setText(mAdapter.getString(0));

		// if (userInfo.gender == 0) {
		// ivFace.setImageResource(R.drawable.ic_female_face);
		// } else {
		// ivFace.setImageResource(R.drawable.ic_male_face);
		// }

		imgBtnNotice.setOnClickListener(this);
		btnAction.setOnClickListener(this);
		btnLogin.setOnClickListener(this);
		btnPlayType.setOnClickListener(this);
		view.findViewById(R.id.btnMore).setOnClickListener(this); // 更多
		view.findViewById(R.id.btnPacket).setOnClickListener(this); // 背包
		view.findViewById(R.id.btnMarket).setOnClickListener(this); // 商城

		ImageView imgQGame=(ImageView) view.findViewById(R.id.imgQGame);
		imgQGame.setOnClickListener(this); // 快速开始

		AnimationDrawable qgame_bg = (AnimationDrawable) imgQGame.getDrawable();
		qgame_bg.start();

		// 2013-4-18推荐快捷购买
		view.findViewById(R.id.ivQuickBuyTop).setOnClickListener(this); // 推荐快捷购买
		AnimationDrawable adrawable = (AnimationDrawable) view.findViewById(
				R.id.ivQuickBuyTop).getBackground();
		adrawable.start();
	}

	/**
	 * 
	 * 返回到第一个卡片分区
	 * 
	 * @return 若第一个是卡片分区且当前不在第一个分区则返回true，否则返回false
	 */
	public boolean backToCardZoneFirst() {
		if (isFirstCard && mCardZonePager.getCurrentItem() != 0) {
			onSelectChanged(0, true);
			return true;
		}
		return false;
	}

	/**
	 * 更新页面标示圆点
	 * 
	 * @param cur
	 */
	private int mCurPointViewIndex = 0;

	public void changePointView(int cur) {
		point_linear.getChildAt(mCurPointViewIndex).setBackgroundResource(
				R.drawable.feature_point);
		point_linear.getChildAt(cur).setBackgroundResource(
				R.drawable.feature_point_cur);
		mCurPointViewIndex = cur;
	}

	/**
	 * 设置用户乐豆数
	 * 
	 * @param bean
	 */
	public void setUserBean(long bean) {
		if (tvuser_bean != null) {
			tvuser_bean.setText("" + bean);
		}
	}

	/**
	 * 设置用户话费券
	 * 
	 * @param ticket
	 */
	public void setUserTicket(String ticket) {
		if (tvuser_ticket != null && !Util.isEmptyStr(ticket)) {
			tvuser_ticket.setText(ticket);
		}
	}

	/**
	 * 设置用户性别昵称
	 * 
	 * @param gender
	 * @param nickName
	 */
	private void setCardZoneUserInfo(byte gender, String nickName) {
		// if(ivFace!=null){
		// if (gender == 0) {
		// ivFace.setImageResource(R.drawable.ic_female_face);
		// } else {
		// ivFace.setImageResource(R.drawable.ic_male_face);
		// }
		// }

		if (tvuser_name != null) {
			tvuser_name.setText(nickName);
		}

	}

	/**
	 * 返回卡片分区所有的view
	 * 
	 * @return
	 */
	private List<View> getCardZoneViews() {
		List<View> listViews = new ArrayList<View>();
		LayoutInflater inflater = mAct.getLayoutInflater();
		List<NewUIDataStruct> dataList = NewCardZoneProvider.getInstance()
				.getNewUIDataList();
		for (final NewUIDataStruct item : dataList) {
			List<NodeData> subList = item.mSubNodeDataList;
			if(subList!=null
					&& subList.size()>0){
				View v = null;
				if (item.showCard) {
					isFirstCard = true;
					v = inflater.inflate(R.layout.cardzonefirst, null);
					initCardViews(subList, v);
				} else {
					NewCardZoneGridViewAdapter adapter = new NewCardZoneGridViewAdapter(
							mAct, subList);
					adapter.setCallBack(CardZoneProtocolListener.getInstance(mAct));
					v = (View) inflater.inflate(R.layout.card_zone_grid_view, null);
					GridView gridView = (GridView) v.findViewWithTag("gridview");
					gridView.setAdapter(adapter);

				}
				listViews.add(v);
			}
			
		}
		return listViews;
	}

	/** 第一个开片控件 */
	private ImageView imgCardZoneName1;
	private TextView tvTotalUsers1;
	private TextView tvExplain1;

	/** 第二个开片控件 */
	private ImageView imgCardZoneName2;
	private TextView tvTotalUsers2;
	private TextView tvExplain2;

	/** 第三个开片控件 */
	private ImageView imgCardZoneName3;
	private TextView tvTotalUsers3;
	private TextView tvExplain3;

	/**
	 * 初始化3个推荐卡片
	 * 
	 * @param item
	 * @param v
	 */
	private void initCardViews(final List<NodeData> list, View v) {

		String onlineCountStr = mResource.getString(R.string.ddz_online_count);
		NodeData node0 = null;
		NodeData node1 = null;
		NodeData node2 = null;
		if(list.size()==3){
			node0 = list.get(0);
			node1 = list.get(1);
			node2 = list.get(2);
		}else if(list.size()==2){
			node0 = list.get(0);
			node1 = null;
			node2 = list.get(1);
		}

		// ------------------------第一个卡片---------------------------------------
		ImageView ivCardItem1 = (ImageView) v.findViewById(R.id.ivCardItem1); // 卡片1
		imgCardZoneName1 = (ImageView) v.findViewById(R.id.imgCardZoneName1); // 房间名称
		tvExplain1 = (TextView) v.findViewById(R.id.tvExplain1); 
		setContent(node0,imgCardZoneName1,tvExplain1);

		tvTotalUsers1 = (TextView) v.findViewById(R.id.tvTotalUsers1); // 在线人数
		tvTotalUsers1.setText(onlineCountStr + node0.onLineUser);

		int num0 = node0.onLineUser;

		if (num0 == 0) { // 如果收到的人数为0，则客服端本地计算
			int parentID = node0.ID;
			List<NewUIDataStruct> dataList = NewCardZoneProvider.getInstance()
					.getNewUIDataList();
			for (final NewUIDataStruct its : dataList) {
				if (its.ID == parentID) {
					for (NodeData subs : its.mSubNodeDataList) {
						num0 += subs.onLineUser;
					}
					break;
				}
			}
		}

		tvTotalUsers1.setText(onlineCountStr + num0);



		// -------------------------第二个卡片--------------------------------------
		RelativeLayout rel2=(RelativeLayout) v.findViewById(R.id.fyCardZoneItem_id2); // 卡片2
		ImageView ivCardItem2 = (ImageView) v.findViewById(R.id.ivCardItem2); // 卡片2
		if(node1==null){
			rel2.setVisibility(View.GONE);
		}else{
			imgCardZoneName2 = (ImageView) v.findViewById(R.id.imgCardZoneName2); // 房间名称
			tvExplain2 = (TextView) v.findViewById(R.id.tvExplain2); // 准入条件
			setContent(node1,imgCardZoneName2,tvExplain2);

			tvTotalUsers2 = (TextView) v.findViewById(R.id.tvTotalUsers2); // 在线人数
			tvTotalUsers2.setText(onlineCountStr + node1.onLineUser);

			int num1 = node1.onLineUser;

			if (num1 == 0) { // 如果收到的人数为0，则客服端本地计算
				int parentID = node1.ID;
				List<NewUIDataStruct> dataList = NewCardZoneProvider.getInstance()
						.getNewUIDataList();
				for (final NewUIDataStruct its : dataList) {
					if (its.ID == parentID) {
						for (NodeData subs : its.mSubNodeDataList) {
							num1 += subs.onLineUser;
						}
						break;
					}
				}
			}

			tvTotalUsers2.setText(onlineCountStr + num1);
		}
		
		// ---------------------------第三个卡片------------------------------------

		ImageView ivCardItem3 = (ImageView) v.findViewById(R.id.ivCardItem3); // 卡片3
		imgCardZoneName3 = (ImageView) v.findViewById(R.id.imgCardZoneName3); // 房间名称
		tvExplain3 = (TextView) v.findViewById(R.id.tvExplain3); // 准入条件
		setContent(node2,imgCardZoneName3,tvExplain3);

		tvTotalUsers3 = (TextView) v.findViewById(R.id.tvTotalUsers3); // 房间人数

		int num2 = node2.onLineUser;

		tvTotalUsers3.setText(onlineCountStr + num2);
		// ------------------------第一个卡片按键监听---------------------------------------
		final NodeData nodeNata0=node0;
		ivCardItem1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cardOnClick(nodeNata0);
				AnalyticsUtils.onClickEvent(mAct, "008");
			}
		});

		// ------------------------第二个卡片按键监听---------------------------------------
		final NodeData nodeNata1=node1;
		ivCardItem2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				cardOnClick(nodeNata1);
				AnalyticsUtils.onClickEvent(mAct, "009");
			}
		});

		// ------------------------第三个卡片按键监听---------------------------------------
		final NodeData nodeNata2=node2;
		ivCardItem3.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				cardOnClick(nodeNata2);
				AnalyticsUtils.onClickEvent(mAct, "010");
			}
		});

	}




	@SuppressWarnings("deprecation")
	private void setContent(NodeData node,ImageView title,TextView sub) {
		switch (node.Type) {
		case NodeData.NODE_CHALLENGE: // 约战节点-----111
			break;
		case NodeData.NODE_NORMAL: // 普通节点(自由对战、智运会) 101
			title.setImageResource(R.drawable.ddz_free);
			String name=node.Name;
			if(name.contains("自由")){
				sub.setBackgroundResource(R.drawable.ddz_free_sub);
			}else if(name.contains("癞子")){
				sub.setBackgroundResource(R.drawable.ddz_laizi_sub);
			}else if(name.contains("小兵")){
				sub.setBackgroundResource(R.drawable.ddz_xiaobing_sub);
			}else{
				sub.setBackgroundDrawable(null);
				sub.setText(name);
			}
			
			break;
		case NodeData.NODE_ROOM: // 房间节点102
			break;
		case NodeData.NODE_ENROLL: // 报名节点,娱乐比赛场专用 109 
			title.setImageResource(R.drawable.ddz_match);
			sub.setBackgroundResource(R.drawable.ddz_match_sub);
			break;
		case NodeData.NODE_MM_VIDEO: // 报名节点,娱乐比赛场专用 109
			title.setImageResource(R.drawable.ddz_mmvideo);
			sub.setBackgroundResource(R.drawable.ddz_mmvideo_sub);
			break;
		default:
			break;
		}

	}














	/**
	 * 卡片点击事件
	 * 
	 * @param item
	 * @param v
	 */
	private void cardOnClick(NodeData node) {
		long curtime = System.currentTimeMillis();
		if ((curtime - mills) < 2000) { // 短于2秒钟的连续按键，不响应
			return;
		}

		List<NewUIDataStruct> dataList = NewCardZoneProvider.getInstance()
				.getNewUIDataList();
		for (int i = 0; i < dataList.size(); i++) {
			if (node.ID == dataList.get(i).ID) {
				mCardZonePager.setCurrentItem(i);
				return;
			}
		}

		CardZoneProtocolListener.getInstance(mAct)
		.invokeCardZoneListViewItem(node);
	}

	public void setPlaytypeBackground(byte type) {
		if (btnPlayType != null) {
			if (type == FiexedViewHelper.GAME_TYPE_NORMAL) {
				btnPlayType
				.setBackgroundResource(R.drawable.btn_playtype_jingdian);
			} else if (type == FiexedViewHelper.GAME_TYPE_LAIZI) {
				btnPlayType
				.setBackgroundResource(R.drawable.btn_playtype_laizi);
			} else if (type == FiexedViewHelper.GAME_TYPE_XIAOBING) {
				btnPlayType
				.setBackgroundResource(R.drawable.btn_playtype_xiaobing);
			}

		}
	}



	/**
	 * 获取抽屉房间数据
	 * 
	 * @return
	 */
	private List<NewUIDataStruct> getDropDodeDatas() {
		List<NewUIDataStruct> dataList = NewCardZoneProvider.getInstance()
				.getNewUIDataList();
		return dataList;
	}

	public boolean showNotice() {
		// 不在分区界面
		if (FiexedViewHelper.getInstance().getCurFragment() != FiexedViewHelper.CARDZONE_VIEW) {
			return false;
		}
		// 在显示玩法切换界面的时候不显示此界面
		if (playtypeDialog != null && playtypeDialog.isShowing()) {
			return false;
		}
		if (theGiftDialog == null) {
			theGiftDialog = new TheGiftDialog(mAct);
		}
		if (theGiftDialog.isShowing()) {
			return false;
		} else {
			theGiftDialog.show();
			return theGiftDialog.showNotice();
		}
	}

	private void initLoginGift() {
		if (FiexedViewHelper.getInstance().getCurFragment() != FiexedViewHelper.CARDZONE_VIEW) {
			cardZoneHandler.sendEmptyMessageDelayed(
					ActionInfoInterface.HANDLER_ACT_QUERY_SUCCESS, 1000);
			return;
		}
		// 初始化登录送对话框
		uid = String.valueOf(HallDataManager.getInstance().getUserMe().userID);
		AppConfig.giftPack = Util.getStringSharedPreferences(mAct, uid, "");

		// 是否已经显示过登陆送对话框，防止重复显示，0-未显示；1-已显示
		// int isShowGift = Util.getIntSharedPreferences(mAct, uid +
		// "_isshowgift", 0);

		if (!AppConfig.giftPack.equals("")/* && isShowGift == 0 */
				|| ActionInfoProvider.getInstance().getNoticeItem() != null) {
			login_info = AppConfig.giftPack;

			/***********************************
			 * 为防止登陆送对话框在新手引导界面之上，造成先后顺序不一致bug
			 ***********************************/
			if (theGiftDialog == null) {
				theGiftDialog = new TheGiftDialog(mAct);
			}
			Calendar c = Calendar.getInstance();
			int days = c.get(Calendar.DAY_OF_MONTH); // 获得日期

			/** 有强弹消息，一定弹出 */
			if (ActionInfoProvider.getInstance().getNoticeItem() != null) {
				theGiftDialog.show();
				theGiftDialog.showNotice();
				Util.setIntSharedPreferences(mAct, uid + "_isshowgift", days);
			} else {
				// 没有强弹时，若配置成次次弹，或者记录的弹出日期与现在日期不一样，则弹
				if (AppConfig.LoginGiftSwitch == AppConfig.LOGIN_GIFT_SHOW_EVERYTIME
						|| Util.getIntSharedPreferences(mAct, uid
								+ "_isshowgift", 0) != days) {
					theGiftDialog.show();
					theGiftDialog.showReward();
					Util.setIntSharedPreferences(mAct, uid + "_isshowgift",
							days);
				}
			}
			// theGiftDialog.show();
			// if(ActionInfoProvider.getInstance().getNoticeItem() == null){
			// theGiftDialog.showReward();
			// }else{
			// theGiftDialog.showNotice();
			// }
			/** 开始保存入SharedPreferences **/
			// Util.setStringSharedPreferences(mAct, uid, AppConfig.giftPack);
			// Util.setIntSharedPreferences(mAct, uid + "_isshowgift", 1);
			btnLogin.setVisibility(View.VISIBLE);
		} else {
			/** 拿到SharedPreferences中保存的数值 第二个参数为如果SharedPreferences中没有保存就赋一个默认值 **/
			login_info = Util.getStringSharedPreferences(mAct, uid, "");
			if (null != login_info && (!login_info.equals(""))) {
				btnLogin.setVisibility(View.VISIBLE);
			}
		}
	}

	private final int POP_MORE = 1;
	private final int POP_DROP = 2;

	// 初始化更多弹出框
	private void initPopWindow() {
		LayoutInflater mLayoutInflater = (LayoutInflater) mAct
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mPopView = mLayoutInflater.inflate(R.layout.pop_list, null);
		ibMix = (ImageButton) mPopView.findViewById(R.id.ib_more_mix);
		ibFeedback = (ImageButton) mPopView.findViewById(R.id.ib_more_feedback);
		ibShare = (ImageButton) mPopView.findViewById(R.id.ib_more_share);
		ibHelp = (ImageButton) mPopView.findViewById(R.id.ib_more_help);
		ibPoint = (ImageButton) mPopView.findViewById(R.id.ib_more_points);
		ibPoint.setVisibility(View.GONE); // 去掉点数专区
		mPopView.findViewById(R.id.iv_div_6).setVisibility(View.GONE);

		ibRank = (ImageButton) mPopView.findViewById(R.id.ib_more_ranklist);
		// GBMJ屏蔽排行榜
		/*
		 * if (AppConfig.gameId == AppConfig.GAMEID_GBMJ) {
		 * ibRank.setVisibility(View.GONE);
		 * mPopView.findViewById(R.id.iv_div_3).setVisibility(View.GONE); }
		 */

		ibSwitch = (ImageButton) mPopView.findViewById(R.id.ib_more_switch);
		ibUpdate = (ImageButton) mPopView.findViewById(R.id.ib_more_update);
		imgUpdate = (ImageView) mPopView.findViewById(R.id.img_update_tag);

		ibUpdate.setOnClickListener(this);
		ibHelp.setOnClickListener(this);
		ibPoint.setOnClickListener(this);
		ibRank.setOnClickListener(this);
		ibSwitch.setOnClickListener(this);
		ibUpdate.setOnClickListener(this);
		ibMix.setOnClickListener(this);
		ibFeedback.setOnClickListener(this);
		ibShare.setOnClickListener(this);
		// init drop menu
		mDropView = mLayoutInflater.inflate(R.layout.card_zone_drop_menu, null);
		lvUnder = (ListView) mDropView.findViewById(R.id.card_zone_under_list);
		lvUnder.setAdapter(mAdapter);
		lvUnder.setOnItemClickListener(new ListOnItemClickListener());
	}

	/**
	 * 各类点击事件
	 */
	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.btnPacket) {
			mView.findViewById(R.id.btnPacket_newTag).setVisibility(
					View.INVISIBLE); // 设置背包new标记为不可见
			mAct.startActivity(new Intent(mAct, BackPackActivity.class));
			AnalyticsUtils.onClickEvent(mAct, "003");
		} else if (id == R.id.imgQGame) { // 快速开始响应事件

			FiexedViewHelper.getInstance().quickGame();
			AnalyticsUtils.onClickEvent(mAct, "007");
		} else if (id == R.id.btnMore) { // 更多
			if (null != mPopupWindow && mPopupWindow.isShowing()) {
				mPopupWindow.dismiss();
			} else {
				popUpWindow(v, POP_MORE);
			}
		}

		else if (id == R.id.ivQuickBuyTop) { // 推荐使用的快捷购买
			// 快捷购买
			UtilHelper.showBuyDialog(mAct, AppConfig.propId, false,
					AppConfig.isConfirmon, AppConfig.ACTION_ZONE);  //2代表  分区【充】
			
			/** 点击快捷购买 */
			AnalyticsUtils.onClickEvent(mAct, "002");
		} else if (id == R.id.btnMarket) {
			mAct.startActivity(new Intent(mAct, MarketActivity.class));
			/** 统计点击商城事件 */
			AnalyticsUtils.onClickEvent(mAct, "004");
		} else if (id == R.id.btnAct) {
			mAct.startActivity(new Intent(mAct, ActionActivity.class));
			/** 点击 主界面-点击活动事件 */
			AnalyticsUtils.onClickEvent(mAct, "006");
		} else if (id == R.id.login_gift) { // 登录送乐豆
			if (theGiftDialog == null) {
				theGiftDialog = new TheGiftDialog(mAct);
				theGiftDialog.show();
				if (ActionInfoProvider.getInstance().getNoticeItem() == null) {
					theGiftDialog.showReward();
				} else {
					theGiftDialog.showNotice();
				}
			} else {
				theGiftDialog.show();
			}
			
			AnalyticsUtils.onClickEvent(mAct, UC.EC_212);

		} else if (id == R.id.user_face) {
			mAct.startActivity(new Intent(mAct, UserCenterActivity.class));
			/** 点击 主界面-个人头像事件 */
			AnalyticsUtils.onClickEvent(mAct, "001");
		} else if (id == R.id.ib_more_help) {
			// 跳转到服务中心
			mPopupWindow.dismiss();
			mAct.startActivity(new Intent(mAct, ServerCenterActivity.class));
			AnalyticsUtils.onClickEvent(mAct, UC.EC_223);
		} else if (id == R.id.ib_more_ranklist) {
			// 跳转到排行榜
			mPopupWindow.dismiss();
			UtilHelper.showRankWebView((short) 20002, userInfo.userID, mAct);
			/** 点击 主界面-点击排行榜事件 */
			AnalyticsUtils.onClickEvent(mAct, UC.EC_222);
		} else if (id == R.id.ib_more_points) {
			// 跳转到点数专区
			mPopupWindow.dismiss();
		} else if (id == R.id.ib_more_switch) {
			// 切换账号
			mPopupWindow.dismiss();
			FiexedViewHelper.getInstance().goToReLoginView();
			AnalyticsUtils.onClickEvent(mAct, UC.EC_219);
		} else if (id == R.id.ib_more_feedback) {
			// 跳转到意见反馈
			mPopupWindow.dismiss();
			mAct.startActivity(new Intent(mAct, FeedbackInfoActivity.class));
			AnalyticsUtils.onClickEvent(mAct, UC.EC_217);
		} else if (id == R.id.ib_more_mix) {
			// 合成
			mPopupWindow.dismiss();
			Intent intent = new Intent(mAct, MixGridActivity.class);
			intent.putExtra("entry", "menu");
			startActivity(intent);
			AnalyticsUtils.onClickEvent(mAct, UC.EC_221);
		} else if (id == R.id.ib_more_share) {
			// 分享
			mPopupWindow.dismiss();
			mAct.startActivity(new Intent(mAct, ShareActivity.class));
			AnalyticsUtils.onClickEvent(mAct, UC.EC_220);
		} else if (id == R.id.new_ic_msg) {
			imgBtnNotice.setImageDrawable(null);
			mView.findViewById(R.id.new_ic_msg_newTag).setVisibility(
					View.INVISIBLE);
			// 跳转到消息盒子
			mAct.startActivity(new Intent(mAct, MessageBoxActivity.class));

			/** 点击 主界面-点击消息盒子事件 */
			AnalyticsUtils.onClickEvent(mAct, "015");
		}
		/** wanghj 添加平划开关效果 **/
		else if (id == R.id.new_btn_switch) {
			if (null != mPopupWindow && mPopupWindow.isShowing()) {
				mPopupWindow.dismiss();
			} else {
				popUpWindow(v, POP_DROP);
				/** 点击 主界面-点击菜单事件 */
				AnalyticsUtils.onClickEvent(mAct, "016");
			}

		} else if (id == R.id.playtype_select) {
			if (theGiftDialog != null && theGiftDialog.isShowing()) {
				return;
			}
			if (playtypeDialog == null) {
				playtypeDialog = new PlaytypeSelectDialog(mAct);
			}
			playtypeDialog.show();
			AnalyticsUtils.onClickEvent(mAct, UC.EC_213);
		} else if (id == R.id.btnGettick) {
			/*Intent getTicket = new Intent(mAct, GetTicketActivity.class);
			startActivityForResult(getTicket, 0);
			AnalyticsUtils.onClickEvent(mAct, "005");*/
			String url=AppConfig.NEW_GETTICKET_URL;
			String userToken=FiexedViewHelper.getInstance().getUserToken();
			int userId=FiexedViewHelper.getInstance().getUserId();
			url += "&at=" + userToken + "&";
			String finalUrl = CenterUrlHelper.getUrl(url, userId);
			UtilHelper.onWeb(mAct, finalUrl);
			AnalyticsUtils.onClickEvent(mAct, UC.EC_211);
			
		} else if (id == R.id.ib_more_update) {
			mPopupWindow.dismiss();
			final VersionInfo vi = AppConfig.getVersionInfo();
			if (vi != null
					&& vi._upUrl != null
					&& (vi._upgrade == VersionInfo.UPGRADE_NEED || vi._upgrade == VersionInfo.UPGRADE_MUST)) {
				if(vi.isDownLoading()){
					// 下载完成后
					UtilHelper.showCustomDialog(mAct, "最新版本正在下载中，请稍候！",
							null, false);
				}else{
					doUpdate(vi);
				}
			} else {
				Toast.makeText(mAct, "您已经是最新的版本了~", Toast.LENGTH_SHORT).show();
			}
			AnalyticsUtils.onClickEvent(mAct, UC.EC_218);
		} else if (id == R.id.smallMoneyPkg) {
			// 小钱包按钮点击事件,购买小钱包
			UtilHelper.showBuyDialog(mAct, AppConfig.smallMoneyPkgPropId,
					false, true);
		} else if (id == R.id.btnServer) {
			ServerDialog dialog = new ServerDialog(mAct);
			dialog.show();
			/** 点击 主界面-点击客服事件 */
			AnalyticsUtils.onClickEvent(mAct, "014");
		}




	}

	// ---------------------------------获取个人中心部分数据--------------------------------------------
	@SuppressWarnings("deprecation")
	private void popUpWindow(View v, int id) {
		if (id == POP_MORE
				&& (mPopupWindow == null || !mPopupWindow.getContentView()
				.equals(mPopView))) {
			mPopupWindow = new PopupWindow(mPopView, LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
		} else if (id == POP_DROP
				&& (mPopupWindow == null || !mPopupWindow.getContentView()
				.equals(mDropView))) {
			mPopupWindow = new PopupWindow(mDropView,
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		}

		mPopupWindow.setBackgroundDrawable(new BitmapDrawable());

		mPopupWindow.setFocusable(true);

		if (POP_MORE == id) {
			int screenWidth = DensityConst.getWidthPixels();
			mPopupWindow.showAtLocation(v, Gravity.LEFT | Gravity.BOTTOM, 10,
					(int) (100 * screenWidth / 800.0));
		} else if (POP_DROP == id) {
			btnSwitch.setCompoundDrawablesWithIntrinsicBounds(0, 0,
					R.drawable.new_btn_switch_close, 0);
			mPopupWindow.showAsDropDown(v, 0, 10);
			mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

				@Override
				public void onDismiss() {
					btnSwitch.setCompoundDrawablesWithIntrinsicBounds(
							0, 0, R.drawable.new_btn_switch_drop, 0);
				}
			});
		}
	}

	/***
	 * @Title: requestMixtureInfoList
	 * @Description: TODO获取合成列表
	 * @version: 2012-11-09 下午03:50:10
	 */
	private void requestMixtureInfoList() {

		// 创建发送的数据包
		TDataOutputStream tdos = new TDataOutputStream();
		NetSocketPak pointBalance = new NetSocketPak(LS_MDM_PROP,
				LSUB_CMD_COM_PRO_LIST_REQ, tdos);
		// 定义接受数据的协议
		short[][] parseProtocol = { { LS_MDM_PROP, LSUB_CMD_COM_PRO_LIST_RESP } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			public boolean doReceive(NetSocketPak netSocketPak) {
				// 解析接受到的网络数据
				try {
					TDataInputStream tdis = netSocketPak.getDataInputStream();
					tdis.setFront(false);
					int total = tdis.readShort(); // 商品总个数
					int num = tdis.readShort(); // 当次商品个数

					if (num > 0) {
						if (mixInfos == null && currReturnMixtureNum == 0) { // 尚未有数组对象情况下
							mixInfos = new MixtureInfo[total];
						}
						// 累计接受到数据到数组中
						for (int i = 0; i < num; i++) {
							if (currReturnMixtureNum + i < mixInfos.length) {
								mixInfos[currReturnMixtureNum + i] = new MixtureInfo(
										tdis);
							}
						}
						currReturnMixtureNum += num; // 积累保存到全局变量，记录当前返回累计数目
						if (currReturnMixtureNum >= total) {
							// 读取完毕，交与主线程显示（同时恢复变量）
							Log.e(TAG, currReturnMixtureNum + "mix完成");
							// NetSocketManager.getInstance().removePrivateListener(this);
							currReturnMixtureNum = 0;
							recordMixtureType = true;
							cardZoneHandler.sendMessage(cardZoneHandler
									.obtainMessage(HANDLER_MIX_QUERY_SUCCESS));
						}

					}

				} catch (Exception e) {
					e.printStackTrace();
				}
				// 数据处理完成，终止继续解析
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

	private int waitTime = 0; // 等待次数，若登录送数据已接收登录弹框数据未接收则等一次
	// 速配Handler处理
	@SuppressLint("HandlerLeak")
	public Handler cardZoneHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ActionInfoInterface.HANDLER_ACT_QUERY_SUCCESS:
				if (msg.obj != null) {
					ActionInfoProvider.getInstance().setActionInfoProvider(
							(ActionInfo[]) msg.obj);
				}
				/*** 当前正在显示玩法选择对话框 **/
				if (playtypeDialog != null && playtypeDialog.isShowing()) {
					sendEmptyMessageDelayed(
							ActionInfoInterface.HANDLER_ACT_QUERY_SUCCESS, 1000);
				} else if (!AppConfig.isReceiveLoginGiftSwitch) { // 接收到是否显示开关
					sendEmptyMessageDelayed(
							ActionInfoInterface.HANDLER_ACT_QUERY_SUCCESS, 1000);
					AppConfig.isReceiveLoginGiftSwitch = true;
				} else if (ActionInfoProvider.getInstance().getNoticeItem() == null
						&& waitTime == 0) {
					waitTime = 1;
					sendEmptyMessageDelayed(
							ActionInfoInterface.HANDLER_ACT_QUERY_SUCCESS, 1000);
				} else {
					waitTime = 0;
					initLoginGift(); // 初始化登录送
					Log.v(TAG, "活动设置成功");
				}
				break;

			case CustomActivity.HANDLER_USER_BEAN: // 获取用户乐豆
			{
				// 先判断状态码，状态码为0表示成功，否则重试
				String statusStr = UtilHelper.parseStatusXml(
						String.valueOf(msg.obj), "status");
				int status = UtilHelper.stringToInt(statusStr, -1); // 默认为-1
				String beanStr = UtilHelper.parseStatusXml(
						String.valueOf(msg.obj), "bean");
				int bean = UtilHelper.stringToInt(beanStr, -1); // 默认为-1

				String ticketStr = UtilHelper.parseStatusXml(
						String.valueOf(msg.obj), "mobilevoucher");

				if (status == 0 && bean >= 0) {// 成功
					// 设置乐豆
					HallDataManager.getInstance().getUserMe().setBean(bean);
					UserCenterData userData = UserCenterProvider.getInstance()
							.getUserCenterData();
					if (userData != null) {
						userData.setLeDou(bean);
					}

					setUserBean(bean);
					setUserTicket(ticketStr);
					if (FiexedViewHelper.getInstance().startGame) {
						FiexedViewHelper.getInstance().startGame = false;
						FiexedViewHelper.getInstance().quickGame();
					}
				}
			}
			break;
			case HallAssociatedInterface.HANDLER_BANKRUPTCY: // 破产赠送乐豆
				String beanContent = String.valueOf(msg.obj);
				if (beanContent != null && beanContent.length() > 0) {
					String status = UtilHelper.getTagStr(beanContent, "status");
					if (Integer.parseInt(status) == 0) {
						int mbean = UtilHelper.stringToInt(
								UtilHelper.getTagStr(beanContent, "bean"), 0);
						if (mbean > 0) {
							// 设置乐豆
							int b = HallDataManager.getInstance().getUserMe()
									.getBean()
									+ mbean;

							HallDataManager.getInstance().getUserMe()
							.setBean(b);
							if (tvuser_bean != null) {
								tvuser_bean.setText("" + b);
							}

							String str=UtilHelper.getTagStr(beanContent, "msg");
							CustomDialog dialog = new CustomDialog(mAct, str,getString(R.string.continue_game));

							dialog.setConfirmCallBack(new OnClickListener() {
								@Override
								public void onClick(View v) {
									// TODO Auto-generated method stub
									FiexedViewHelper.getInstance().quickGame();
									AnalyticsUtils.onClickEvent(mAct, UC.EC_226);
								}
							});
							dialog.show();
						}
					}
				}
				break;

			case LoginAssociatedInterface.HANDLER_CUT_LINK_HAVE_DATA: // 有断线重连数据
				FiexedViewHelper.getInstance().skipToFragment(
						FiexedViewHelper.LOADING_VIEW);
				if (FiexedViewHelper.getInstance().loadingFragment != null) {
					FiexedViewHelper.getInstance().loadingFragment
					.setLoadingType(mResource
							.getString(R.string.ddz_return_game),
							NodeDataType.NODE_TYPE_NOT_DO);
				}
				int roomID = msg.arg1;
				if (gameRoomAssociated != null) {
					gameRoomAssociated.enterRoom(roomID);
				}
				break;
			case HANDLER_MIX_QUERY_SUCCESS:
				if (recordMixtureType) {
					MixInfoProvider.getInstance().setMixtureInfoProvider(
							mixInfos);
					Log.v(TAG, "合成设置成功");
				}
				break;
			case HANDLER_SIT_DOWN_FAIL: // 用户坐下失败
				String errorMsg = (String) msg.obj;
				FiexedViewHelper.getInstance().skipToFragment(
						FiexedViewHelper.CARDZONE_VIEW);
				UtilHelper.showCustomDialog(mAct, errorMsg);
				break;
			case HANDLER_USER_STATUS_SIT_DOWN_SUCCESS: // 坐下成功，下发用户信息后处理(跳转到游戏界面)
				/*********************************
				 * 坐下成功后切换到cocos2d-x游戏
				 ********************************/
				FiexedViewHelper.getInstance().skipToFragment(
						FiexedViewHelper.COCOS2DX_VIEW);
				break;
			case HANDLER_UPDATE_HEAD: {
				ivFace.setImageDrawable(HeadManager.getInstance().getZoneHead(
						mAct));
			}
			break;
			case HANDLER_CHECK_UPDATE_COMPLETE:
				final VersionInfo vi = (VersionInfo) msg.obj;
				AppConfig.setVersionInfo(vi);
				String versionName = null;
				try {
					versionName = Util.getVersionName(mAct);
					if(vi._upgrade == VersionInfo.UPGRADE_NONE){
						tvVersion.setText(versionName);
					}else if(isNewestVersionExist(vi)){
						tvVersion.setText(versionName);
						spark(tvVersion);
						tvVersion.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								CardZoneFragment frag=FiexedViewHelper.getInstance().cardZoneFragment;
								if(frag!=null){
									frag.doUpdate(vi);
								}
							}
						});
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				doUpdate(vi);
				break;
			case HANDLER_UPDATE_FAIL: {
				// String err=obj.toString();
				String err = mAct.getString(R.string.download_error);
				UtilHelper.showCustomDialog(mAct, err);
				
				// 强制升级失败，关闭客户端
				if(msg.arg1 == 1){
					android.os.Handler hander = new android.os.Handler();
					hander.postDelayed(new Runnable() {
						@Override
						public void run() {
							android.os.Process.killProcess(android.os.Process.myPid());
						}
					}, 2000);
				}
			}
			break;
			case HANDLER_GAME_PLAYER:
				//游戏玩家功能去除
				break;
			case HANDLER_GET_TICKET_SUCCESS:
				String ticket = msg.obj.toString();
				if (tvuser_ticket != null) {
					tvuser_ticket.setText(ticket);
				}
				break;
			default:
				break;

			}
		}
	};

	int clo = 0;
	/**
	 * 文字闪烁
	 * @param touchScreen
	 */
	public void spark(final TextView touchScreen) {  
		Timer timer = new Timer();  
		TimerTask taskcc = new TimerTask() {  
			public void run() {  
				mAct.runOnUiThread(new Runnable() {  
					public void run() {  

						if (clo == 0) {  
							clo = 1;  
							touchScreen.setTextColor(0x64ffdb60);
						} else{ 
							clo = 0; 
							touchScreen.setTextColor(0xffffdb60);
						} 
					}  
				});  
			}
		};  
		timer.schedule(taskcc, 1, 300);  
	}  

	public void setVersionName(){
		try {
			final String versionName = mAct.getPackageManager().getPackageInfo(
					mAct.getPackageName(), 0).versionName;
			final VersionInfo versionInfo = AppConfig.getVersionInfo();
			tvVersion.setText(versionName);
			if(versionInfo != null && versionName.compareTo(versionInfo._version) < 0 && isNewestVersionExist(versionInfo)){
				spark(tvVersion);
				tvVersion.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						CardZoneFragment frag=FiexedViewHelper.getInstance().cardZoneFragment;
						if(frag!=null){
							frag.doUpdate(versionInfo);
						}
					}
				});
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 版本升级操作
	 * 
	 * @param vi
	 */
	private void doUpdate(final VersionInfo vi) {
		if (Util.getAPNType(mAct) == 1) {
			// 如果下载文件已经存在
			if(isNewestVersionExist(vi)){
				showUpdateDialog(vi);
			}else{
				checkDownLoadVersion(vi, null);
			}
			return;
		}
		showUpdateDialog(vi);
	}

	/**
	 * 最近版本文件是否下载完成
	 * @param vi
	 * @return
	 */
	private boolean isNewestVersionExist(VersionInfo vi){
		String downpath = NotifiyDownLoad.getSdcardPath() + NotifiyDownLoad.APKS_PATH; // 最新版本下载目录
		String fileName = NotifiyDownLoad.getFileNameFromUrl(vi._upUrl); // 最新版本文件名
		if(fileName == null){
			return false;
		}
		File file = new File(downpath, fileName);
		return file.exists();
	}

	private void showUpdateDialog(final VersionInfo vi) {
		byte versionTag = vi._upgrade;
		String upDesc = null;
		if (vi._upDesc != null) {
			upDesc = vi._upDesc.replace("#", "\n");
		}
		// 当状态为需要升级的时候，事件所做的操作
		if (versionTag == VersionInfo.UPGRADE_NEED) {

			if (imgNew != null) {
				imgNew.setVisibility(View.VISIBLE);
			}
			if (imgUpdate != null) {
				imgUpdate.setVisibility(View.VISIBLE);
			}

			final UpdateDialog dialog = new UpdateDialog(mAct, vi);
			if (upDesc != null) {
				dialog.show();
				dialog.setUpgradeDesc(upDesc);
				dialog.setApkSize(vi._apkSize);
				dialog.setVersion(vi._version);
				dialog.setGifContent();

			}
			dialog.setOnCancelUpgradeListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// 取消更新统计
					AnalyticsUtils.onClickEvent(mAct, "025");
					dialog.dismiss();
				}
			});
			dialog.setOnEnsureUpgradeListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					if(v instanceof Button){
						Button btn = (Button) v;
						String text = btn.getText().toString();
						if(text.equals(mAct.getString(R.string.update_Confir))){
							// 立即更新统计
							AnalyticsUtils.onClickEvent(mAct, "024");
							checkDownLoadVersion(vi, dialog);
						}else{
							String downpath = NotifiyDownLoad.getSdcardPath() + NotifiyDownLoad.APKS_PATH; // 最新版本下载目录
							String fileName = NotifiyDownLoad.getFileNameFromUrl(vi._upUrl); // 最新版本文件名
							NotifiyDownLoad.installApk(mAct, downpath + "/" + fileName);
						}
					}else{
						// 立即更新统计
						AnalyticsUtils.onClickEvent(mAct, "024");
						checkDownLoadVersion(vi, dialog);
					}
					//dialog.dismiss();
				}
			});
		} else if (versionTag == VersionInfo.UPGRADE_MUST) {

			final UpdateDialog dialog = new UpdateDialog(mAct, vi);
			if (upDesc != null) {
				dialog.show();
				dialog.setUpgradeDesc(upDesc);
				dialog.setApkSize(vi._apkSize);
				dialog.setVersion(vi._version);
				dialog.setGifContent();
				dialog.setCancelable(false);
				dialog.setCanceledOnTouchOutside(false);
			}
			dialog.setOnCancelUpgradeListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					// 取消更新统计
					AnalyticsUtils.onClickEvent(mAct, "025");
					android.os.Process.killProcess(android.os.Process.myPid());
				}
			});
			dialog.setOnEnsureUpgradeListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(v instanceof Button){
						Button btn = (Button) v;
						String text = btn.getText().toString();
						if(text.equals(mAct.getString(R.string.update_Confir))){
							// 立即更新统计
							AnalyticsUtils.onClickEvent(mAct, "024");
							checkDownLoadVersion(vi, dialog);
						}else{
							String downpath = NotifiyDownLoad.getSdcardPath() + NotifiyDownLoad.APKS_PATH; // 最新版本下载目录
							String fileName = NotifiyDownLoad.getFileNameFromUrl(vi._upUrl); // 最新版本文件名
							NotifiyDownLoad.installApk(mAct, downpath + "/" + fileName);
						}
					}else{
						// 立即更新统计
						AnalyticsUtils.onClickEvent(mAct, "024");
						checkDownLoadVersion(vi, dialog);
					}
				}
			});

		} else {
			// do nothing
		}
	}

	/**
	 * 升级下载时候如果是非wifi网络，给予用户提示
	 * 
	 * @param vi
	 */
	/*private void checkDownLoadVersion(final VersionInfo vi) {
		if (Util.getAPNType(mAct) == 1) {
			vi.gotoUpgrade(getDownLoadListener());
		} else {

			UtilHelper.showCustomDialog(mAct, mAct.getString(R.string.no_wifi),
					new OnClickListener() {

						@Override
						public void onClick(View v) {
							vi.gotoUpgrade(getDownLoadListener());
						}
					}, true);

		}
	}*/

	/**
	 * 升级下载时候如果是非wifi网络，给予用户提示
	 * 
	 * @param vi
	 */
	private void checkDownLoadVersion(final VersionInfo vi, final UpdateDialog updateDialog) {
		if (Util.getAPNType(mAct) == 1) {
			if(!vi.isDownLoading()){
				if(vi._upgrade != VersionInfo.UPGRADE_MUST){
					vi.gotoUpgrade(getDownLoadListener());
					vi.setDownLoading(true);
				}else{
					CustomDialog customDialog = UtilHelper.showCustomDialog(mAct, "系统正在下载更新文件，请稍候...", new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							// 取消更新统计
							android.os.Process.killProcess(android.os.Process.myPid());
						}
					}, false, false);
					customDialog.setCancelable(false);
					customDialog.setCanceledOnTouchOutside(false);
					vi.gotoUpgrade(getDownLoadListener(customDialog));
					vi.setDownLoading(true);
				}
			}
		} else {

			UtilHelper.showCustomDialog(mAct, mAct.getString(R.string.no_wifi),
					new OnClickListener() {

				@Override
				public void onClick(View v) {
					if(!vi.isDownLoading()){
						vi.gotoUpgrade(getDownLoadListener(updateDialog));
						vi.setDownLoading(true);
					}
				}
			}, true);

		}
	}

	/***
	 * @Title: showQuickBuyDialog
	 * @Description: 弹出快捷购买对话框，
	 * @version: 2012-12-28 下午09:41:10
	 */
	public void showQuickBuyDialog(int propID, String proMessage,
			String ensureBtnStr, String cancelBtnStr) {
		if (propID != -1 && !Util.isEmptyStr(proMessage)) {
			final GoodsItem goodItem = UtilHelper.getGoodsItem(propID);
			if (goodItem != null) {

				UtilHelper.showCommonFastBuyDialog(mAct, new OnClickListener() {
					@Override
					public void onClick(View v) {
						PayManager.getInstance(mAct).requestBuyPropPlist(goodItem,
								AppConfig.isConfirmon, -1);
					}
				}, new OnClickListener() {
					@Override
					public void onClick(View v) {
						PayManager.IS_BUY_CANCLE = true;//此处为取消购买，则不需要再次请求订单
						PayManager.getInstance(mAct).requestBuyPropPlist(goodItem,
								AppConfig.isConfirmon, PayManager.BANKRUPTCY);
					}
				}, goodItem, proMessage, ensureBtnStr, cancelBtnStr);
			}
		}
	}

	public  void requestBankruptcy(){
		// 关闭后调用破产赠送
		if (hallAssociated != null) {
			NodeData node = HallDataManager.getInstance()
					.getCurrentNodeData();
			if (node != null
					&& node.Type == NodeData.NODE_NORMAL) {
				hallAssociated.givingBankruptcy();
			}
		}
	}

	public void showQuickBuyDialog(int propID, String proMessage) {
		showQuickBuyDialog(propID, proMessage,
				mResource.getString(R.string.Ensure),
				mResource.getString(R.string.Cancel));
	}

	/**
	 * 玩法切换
	 * 
	 * @param playId
	 */
	public void switchPlayMethod(byte playId) {
		NewCardZoneProvider.getInstance().initCardZoneProvider(playId);
		//此处在切换账号时非常容易引起nullpointException，所以不在cardZone页面时，不调用此函数也不影响，
		//		切换帐号还会出现奔溃的现象，暂时不能解决
		if(mCardZonePager == null){
			return;
		}
		List<View> views = getCardZoneViews();
		
		if(views.size()==0){  //列表数据出现了异常，弹出玩法选择框，让用户主动选择
			if (playtypeDialog == null) {
				playtypeDialog = new PlaytypeSelectDialog(mAct);
			}
			playtypeDialog.show();
		}
		
		
		if (adapter == null) {
			adapter = new CardZoneViewPagerAdapter(views);
		} else {
			adapter.setViews(views);
		}
		mCardZonePager.setAdapter(adapter);

		// 圆点标示
		if (point_linear != null) {
			point_linear.removeAllViews();
		}
		// 初始化卡片界面圆点
		sumOfDrawble = views.size();
		mCurPointViewIndex = 0;
		for (int i = 0; i < sumOfDrawble; i++) {
			ImageView pointView = new ImageView(mAct);
			if (i == mCurPointViewIndex) {
				pointView.setBackgroundResource(R.drawable.feature_point_cur);
			} else {
				pointView.setBackgroundResource(R.drawable.feature_point);
			}
			point_linear.addView(pointView);
		}

		// 抽屉标示
		mAdapter.notifyDataSetChanged();
		btnSwitch.setText(mAdapter.getString(0));

	}

	/**
	 * wanghj 当菜单改变时应调用，必须要做的事情是改变当前高亮项，改变开关按钮字符串，若是由点击下层按钮产生的改变，还需要切换房间
	 * 
	 * @param newSelect
	 *            新选择项
	 * @param changeByClick
	 *            是否由点击下层按钮改变
	 */
	public void onSelectChanged(int newSelect, boolean changeByClick) {
		if (newSelect == mAdapter.getHilight()) {
			return;
		}

		// 高亮选项
		mAdapter.setHilight(newSelect);
		btnSwitch.setText(mAdapter.getString(newSelect));
		mAdapter.notifyDataSetChanged();

		// 列表翻页，注：高亮选项不是listView的selection，而是自定义的，因为listView的selection会自动排在可视列表的第一个，所以不用
		int lastV = lvUnder.getLastVisiblePosition();
		int firstV = lvUnder.getFirstVisiblePosition();
		int range = lastV - firstV;
		if (range > 0 && (newSelect < firstV || newSelect >= lastV)) {
			int newFirst = newSelect - newSelect % range;
			lvUnder.setSelection(newFirst);
		}

		// 如果是点击列表，则还需要改变左边房间信息
		if (changeByClick) {
			if (newSelect >= 0) {
				mCardZonePager.setCurrentItem(newSelect);
			}
		}
	}

	private DownLoadListener getDownLoadListener() {

		DownLoadListener lis = new DownLoadListener() {
			@Override
			public void onProgress(int rate, String strRate) {
				if (rate == 100) {
					showUpdateDialog(AppConfig.getVersionInfo());
					setVersionName();
					AppConfig.getVersionInfo().setDownLoading(false);
				}
			}

			@Override
			public void downloadFail(String err) {
				/*Message msg = cardZoneHandler.obtainMessage();
				msg.what = HANDLER_UPDATE_FAIL;
				msg.obj = err;
				cardZoneHandler.sendMessage(msg);*/
				AppConfig.getVersionInfo().setDownLoading(false);
			}
		};
		return lis;
	}
	
	private DownLoadListener getDownLoadListener(final CustomDialog customDialog) {
		DownLoadListener lis = new DownLoadListener() {
			@Override
			public void onProgress(int rate, String strRate) {
				if (rate == 100) {
					showUpdateDialog(AppConfig.getVersionInfo());
					setVersionName();
					AppConfig.getVersionInfo().setDownLoading(false);
					if (customDialog.isShowing()) {
						customDialog.dismiss();
					}
				}
			}

			@Override
			public void downloadFail(String err) {
				Message msg = cardZoneHandler.obtainMessage();
				msg.what = HANDLER_UPDATE_FAIL;
				msg.arg1 = 1;
				msg.obj = err;
				cardZoneHandler.sendMessage(msg);
				AppConfig.getVersionInfo().setDownLoading(false);
			}
		};
		return lis;
	}

	private DownLoadListener getDownLoadListener(final UpdateDialog dialog) {
		DownLoadListener lis = new DownLoadListener() {
			@Override
			public void onProgress(int rate, String strRate) {
				dialog.setProgressBar(rate);
				dialog.setRateText(rate, strRate);
				if (rate == 100) {
					if (dialog.isShowing()) {
						// dialog.dismiss();
						dialog.showInstall();
					}
					setVersionName();
					AppConfig.getVersionInfo().setDownLoading(false);
				}
			}

			@Override
			public void downloadFail(String err) {
				dialog.dismiss();
				Message msg = cardZoneHandler.obtainMessage();
				msg.what = HANDLER_UPDATE_FAIL;
				msg.obj = err;
				cardZoneHandler.sendMessage(msg);
				AppConfig.getVersionInfo().setDownLoading(false);
			}
		};
		return lis;
	}

	/**
	 * @author wanghj 下层列表子项被点击
	 * 
	 */
	class ListOnItemClickListener implements
	android.widget.AdapterView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			onSelectChanged(arg2, true);
			mPopupWindow.dismiss();

		}
	}
}
