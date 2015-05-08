package com.mykj.andr.ui.fragment;

import java.util.Timer;
import java.util.TimerTask;

import org.cocos2dx.util.GameUtilJni;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.mingyou.distributor.NetPrivateListener;
import com.mingyou.distributor.NetSocketPak;
import com.mykj.andr.model.AllNodeData;
import com.mykj.andr.model.Conditions;
import com.mykj.andr.model.HallDataManager;
import com.mykj.andr.model.NodeData;
import com.mykj.andr.model.RoomData;
import com.mykj.andr.model.UserInfo;
import com.mykj.andr.net.NetSocketManager;
import com.mykj.andr.ui.fragment.LoadingFragment.NodeDataType;
import com.mykj.andr.ui.widget.TaskTimer;
import com.mykj.comm.io.TDataInputStream;
import com.mykj.comm.io.TDataOutputStream;
import com.mykj.game.FiexedViewHelper;
import com.MyGame.Midlet.R;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.Log;
import com.mykj.game.utils.Toast;
import com.mykj.game.utils.Util;
import com.mykj.game.utils.UtilHelper;

public class AmusementFragment extends FragmentModel {
	public static final String TAG = "AmusementFragment";

	private Activity mAct;

	private Resources mResource;

	// -----------------------------------------handler what-----------------------------------------

	private static final int HANDLER_USER_LOGIN_SUCCESS = 1; // 登录报名服务器成功
	private static final int HANDLER_USER_LOGIN_FAIL = 2; // 登录报名服务器失败
	private static final int HANDLER_CONNECT_STATE_FAIL_CUT = 3; // 消息：连接状态失败或断开
	private static final int HANDLER_MATCH_INFO = 4; // 报名基本信息1:显示报名条件
	private static final int HANDLER_NEXT_START_TIME = 5; // 定点赛开赛时间(服务下发)
	private static final int HANDLER_ONLINE_SUCCESS = 6; // 显示已报名与正在比赛人数
	private static final int HANDLER_EXIT_MATCH_SUCCESS = 7; // 退赛成功，修改为：我要报名，以及其他操作
	private static final int HANDLER_EXIT_MATCH_FAIL = 8; // 娱乐赛退赛失败
	private static final int HANDLER_HAMUSEMENT_TIMER_COUNT = 9; // 倒计时
	private static final int HANDLER_HAMUSEMENT_TIMER_COUNT_END = 10; // 倒计时结束,可能情况，进入了游戏，游戏人数不够，进入不了
	private static final int HANDLER_ONLINE_TIMER = 11; // 定时向服务端发送已报名和正在比赛人数请求
	private static final int HANDLER_APPLY_MATCH_SUCCESS = 12; // 报名成功
	private static final int HANDLER_APPLY_MATCH_NOT_OPTION = 13; // 不符合参赛条件
	private static final int HANDLER_APPLY_MATCH_ED = 14; // 已报名
	private static final int HANDLER_APPLY_MATCH_ALREADY_OTHER_ROOM = 15; // 已经在其他乐豆房间(上次强退游戏未结束)
	private static final int HANDLER_APPLY_MATCH_NOT_BEAN_AND_NOT_COST = 16; // 报名费用不足
	private static final int HANDLER_MATCH_OTHER = 17; // 比如：关闭加载界面，重新请求人数等
	private static final int HANDLER_REWARD_CONTENT = 18; // 拼凑奖励内容
	private static final int HANDLER_USER_RECORD = 19; // 拼凑用户战绩内容
	private static final int HANDLER_MATCH_BEFORE_NOTICE = 20; // 定时赛(开赛前)通知 401
	private static final int HANDLER_MATCH_STARTED = 21; // 定时赛(开赛已经开始,只有退出报名服务器后才会收到)，拉入比赛通知 402

	private static final int HANDLER_START_GAME = 22; // 比赛开始通知
	//private static final int HANDLER_START_GAME_UPDATA_LIST_ITEM = 23; // 比赛开赛通知，只更新ListViewItem状态

	// -------------------------------------------显示UI控件-----------------------------------------------
	private Button btnAttendOrExit;// 娱乐赛报名、退赛

	private Button btnMatchDetail;// 比赛详细

	private RadioGroup rdoGroupHAmusement;

	private TextView tvAttendOption;

	private TextView tvAttendPerson; // 当前参数人数

	private TextView tvCompetitionTime; // 比赛时间

	private TextView tvPersonLimit; // 人数限制

	private LinearLayout layoutCurrentPerson;// 激战人数(报名人数)

	private TextView tvMatchName; // 比赛名称

	private TextView tvAttendFee; // 报名费用

	private TextView tvRewardContent; // 奖励内容

	private TextView tvCountdownTime; // 开赛倒计时

	private LinearLayout lyState;// 报名状态容器

	private TextView tvBack; // 赚话费区返回按钮

	private LinearLayout linearMainContent;

	private LinearLayout linearProgress;

	// -------------------------------------------协议和UI数据----------------------------------------------------
	private TaskTimer taskTimer; // 时间记时器

	private boolean isTimerMath; // 是否定是定时赛

	private int dwStartMaxCount; // 开赛的最大人数

	private int dwStartMinCount;// 开赛的最小人数

	private String matchName; // 比赛名称

	private Conditions applys[]; // 当前报名费

	private int dwStartTime; // 开赛时间 12-11-07-11-10

	private int dwDisStartTime;// 距离开赛时间（单位秒）

	private short wMRuleUrlId = 0; // 主规则ID
	private short wSRuleUrlId = 0; // 子规则ID

	/** 定时发送人数请求 **/
	private Timer applytimer = null; // 定时发送人数请求

	private boolean isWillAttending = true; // True 未报名。false已报名

	private static long currentDataId = 0; // 当前报名节点dataID(赛事房间)

	private boolean isLogined = false; // 是否登录比赛报名服务器
	// --------------------------网络协议命令码------------------------------------

	// ------------------------手机比赛报名主协议201---------------------------------
	/**  **/
	private static final short MDM_MATCH_APPLY = 201;

	// ------------------------手机比赛报名子协议------------------------------------

	/** 用户登录报名服务器 101 **/
	private static final short SUB_AS_USER_LOGON = 101;

	/** 登录结果返回（服务器下发）102 **/
	private static final short SUB_AS_USER_LOGON_RESULT = 102;

	/** 比赛基本信息（服务器下发）103 **/
	private static final short SUB_AS_MATCH_INFO = 103;

	/** 比赛详细介绍查询 */
	// private static final short SUB_AS_MATCH_DESC_REQ = 104;

	/** 比赛奖励详细介绍（服务器下发）105 **/
	private static final short SUB_AS_MATCH_DESC = 105;

	/** 用户退出登录 **/
	private static final short SUB_AS_USER_EXIT = 106;

	/** 比赛报名 201 **/
	private static final short SUB_AS_MATCH_APPLY = 201;

	/** 报名结果 202 **/
	private static final short SUB_AS_APPLY_RESULT = 202;

	/** 取消报名（退赛） 203 **/
	private static final short SUB_AS_CANCEL_APPLY = 203;

	/** 退赛结果 204 **/
	private static final short SUB_AS_CANCEL_APPLY_RESULT = 204;

	/** 比赛开始通知（服务器下发） 205 **/
	private static final short SUB_AS_MATCH_START = 205;

	/** 已报名和正在比赛人数等信息请求 301 **/
	private static final short SUB_AS_ONLINE_INFO_REQ = 301;

	/** 已报名和正在比赛人数等信息返回（服务器下发）302 **/
	private static final short SUB_AS_ONLINE_INFO_RESP = 302;

	/** 用户信息返回 304 **/
	private static final short SUB_AS_USER_INFO_RESP = 304;

	/** 系统消息（服务器下发）305 **/
	//private static final short SUB_AS_SYS_MESSAGE = 305;

	/** 连接状态 子协议 307 **/
	private static final short SUB_AS_CONNECT_STATE = 307;

	/** 下一开赛时间点（用于定时赛，服务器主动下发） */
	private final short SUB_AS_NEXT_START_TIME = 308;

	/** 整点赛开赛前通知 全局子协议 401 **/
	private static final short SUB_AS_MATCHBEFORE_NOTICE = 401;
	/** 整点赛开赛拉入  全局子协议 402 **/
	private static final short SUB_AS_MATCHBEFORE_START = 402;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		registerReceiveAgreement();
		this.mAct = activity;
		mResource = mAct.getResources();
	}

	/**
	 * When creating, retrieve this instance's number from its arguments.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	/**
	 * 比赛场view
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.room_detail_view, null);
		((TextView)view.findViewById(R.id.tvTitle)).setText(R.string.match);

		linearMainContent=((LinearLayout) view.findViewById(R.id.linearMainContent));
		linearProgress=((LinearLayout) view.findViewById(R.id.linearProgress));


		tvBack = ((TextView) view.findViewById(R.id.tvBack));
		tvBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				matchRoomGoBack();
			}
		});

		// 比赛名称
		tvMatchName = ((TextView) view.findViewById(R.id.tvMatchName));
		// 乐豆单选组
		rdoGroupHAmusement = ((RadioGroup) view
				.findViewById(R.id.rdoGroupHAmusement));
		// 报名条件
		tvAttendOption = ((TextView) view.findViewById(R.id.tvAttendOption));
		// 报名费用
		tvAttendFee = ((TextView) view.findViewById(R.id.tvAttendFee));
		// 奖励内容
		tvRewardContent = ((TextView) view.findViewById(R.id.tvRewardContent));
		// 激战人数
		layoutCurrentPerson = ((LinearLayout) view.findViewById(R.id.tvCurrentPerson));
		// 人数限制
		tvPersonLimit = ((TextView) view.findViewById(R.id.tvPersonLimit));
		// 比赛时间
		tvCompetitionTime = ((TextView) view.findViewById(R.id.tvCompetitionTime));

		// 开赛倒计时
		tvCountdownTime = ((TextView) view.findViewById(R.id.tvCountdownTime));

		// 当前报名人数
		tvAttendPerson = ((TextView) view.findViewById(R.id.tvAttendPerson));

		// 报名状态容器
		lyState = ((LinearLayout) view.findViewById(R.id.lyState));

		// 娱乐赛报名、退赛
		btnAttendOrExit = ((Button) view.findViewById(R.id.btnAttendOrExit));
		btnAttendOrExit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isLogined) {
					// 人满赛，定时赛：报名/退赛
					attendExitHAmusement();
				} else {
					Toast.makeText(mAct, mResource.getString(R.string.ddz_donot_loaded),
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		// 比赛详细
		btnMatchDetail = ((Button) view.findViewById(R.id.btnMatchDetail));
		btnMatchDetail.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String url =getMatchInfoPath();
				UtilHelper.onWeb(mAct, url);
			}
		});

		sendLoginMatch();   //登录报名服务器

		return view;
	}



	@Override
	public void onDestroy(){
		super.onDestroy();
		ApplyOnlineAndMatchPersonTimer(false);
		stopTaskTimer();
	}


	public int getFragmentTag(){
		return FiexedViewHelper.AMUSEMENT_ROOM;
	}

	@Override
	public void onBackPressed() {
		matchRoomGoBack();
	}

	// -----------------------------------------public方法区域----------------------------------------
	/**
	 * 比赛场物理返回键和软返回键操作
	 * 人满赛发送退赛协议
	 * 定点赛不操作，返回分区
	 */
	public void matchRoomGoBack() {

		ApplyOnlineAndMatchPersonTimer(false);

		if(!getAttendFlag()){   //已经报名
			if(isTimerMath){//定时赛
				exitLogin();
			}else{  //人满赛
				exitMatch();  //退赛
				exitLogin();  //退报名服务器
			}
		}
		FiexedViewHelper.getInstance().requestUserBean();//请求用户乐豆
		// 关闭比赛赚话费房间UI
		FiexedViewHelper.getInstance().skipToFragment(FiexedViewHelper.CARDZONE_VIEW);

	}



	/**
	 * 设置登录节点
	 * @param dataId
	 */
	public void sendLoginDataId(long dataId) {
		// 记录当前报名节点dataID
		currentDataId = dataId;
	}



	/**
	 * @Title: sendSUB_AS_USER_LOGON
	 * @Description: 发送用户登录报名服务器 发送201---101，接受201---102
	 * @version: 2011-11-10 下午02:48:19
	 */
	private void sendLoginMatch() {
		TDataOutputStream tdous = new TDataOutputStream(false);
		tdous.writeLong(currentDataId);
		NetSocketPak mConsumptionSock = new NetSocketPak(MDM_MATCH_APPLY,
				SUB_AS_USER_LOGON, tdous);

		// 定义接受数据的协议
		short[][] parseProtocol = { { MDM_MATCH_APPLY, SUB_AS_CONNECT_STATE } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				// 解析接受到的网络数据
				TDataInputStream tdis = netSocketPak.getDataInputStream();
				byte result = tdis.readByte(); // 连接状态  0：连接成功      1：连接失败   2：连接断开
				String err = mResource.getString(R.string.ddz_net_error);
				if (result == 1) { // 1：连接失败   2：连接断开
					Message msg = mMatchHandler.obtainMessage();
					msg.what = HANDLER_USER_LOGIN_FAIL;
					msg.obj = err;
					mMatchHandler.sendMessage(msg);
				}else if (result == 2) { //2：连接断开
					if(FiexedViewHelper.getInstance().getCurFragment()!=FiexedViewHelper.COCOS2DX_VIEW){
						Message msg = mMatchHandler.obtainMessage();
						msg.what = HANDLER_CONNECT_STATE_FAIL_CUT;
						msg.obj = err;
						mMatchHandler.sendMessage(msg);
					}
				}
				return true;
			}
		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		nPListener.setOnlyRun(false);

		NetSocketManager.getInstance().sendData(mConsumptionSock);
	}


	/***
	 * @Title: startTimer
	 * @Description: 启动关闭定时器(每次请求报名人数)
	 * @version: 2012-9-27 下午05:21:48
	 */
	public void ApplyOnlineAndMatchPersonTimer(boolean runFlag) {
		if (runFlag) {
			if(applytimer != null){
				applytimer.cancel();
				applytimer=null;
			}
			applytimer = new Timer(true);
			applytimer.schedule(new TimerTask() {
				@Override
				public void run() {
					mMatchHandler.sendEmptyMessage(HANDLER_ONLINE_TIMER);
				}
			}, 0, 3000); // 每隔1秒触发一次
		} else {
			stopApplyTimer();
		}
	}


	/**
	 * @Title: Sendsub_As_User_Exit
	 * @Description: 发送退出登录服务器请求  发送201----106
	 * @version: 2011-11-23 上午11:31:12
	 */
	public void exitLogin() {
		int userid = getUserID();
		if (userid != -1) {
			// 构造数据包
			TDataOutputStream tdous = new TDataOutputStream(false);
			tdous.writeInt(userid);
			NetSocketPak mConsumptionSock = new NetSocketPak(MDM_MATCH_APPLY,
					SUB_AS_USER_EXIT, tdous);
			// 发送协议
			NetSocketManager.getInstance().sendData(mConsumptionSock);
			// 清理协议对象
			mConsumptionSock.free();
		}

	}


	/**
	 * 退赛
	 */
	public void exitMatch() {
		int userID = getUserID();
		if (userID != -1) {
			TDataOutputStream tdous = new TDataOutputStream(false);
			tdous.writeInt(userID);
			NetSocketPak mConsumptionSock = new NetSocketPak(MDM_MATCH_APPLY,SUB_AS_CANCEL_APPLY, tdous);
			// 发送协议
			NetSocketManager.getInstance().sendData(mConsumptionSock);
		}
	}

	// -----------------------------------------handler--------------------------------------------------------------------

	@SuppressLint("HandlerLeak")
	private Handler mMatchHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLER_USER_LOGIN_SUCCESS:
				/** 登录报名服务器成功 **/
				isLogined = true; // 登录报名服务器成功
				ApplyOnlineAndMatchPersonTimer(true);// 开启定时器，每3秒请求
				break;
			case HANDLER_USER_LOGIN_FAIL:
				/** 登录报名服务器失败 **/
				FiexedViewHelper.getInstance().skipToFragment(FiexedViewHelper.CARDZONE_VIEW);
				isLogined = false; // 登录报名服务器失败
				String err1 = (String) msg.obj;
				if (!Util.isEmptyStr(err1)) {
					UtilHelper.showCustomDialog(mAct, err1);
				}
				break;
			case HANDLER_CONNECT_STATE_FAIL_CUT:
				/** 消息：连接状态失败或断开 **/
				FiexedViewHelper.getInstance().skipToFragment(FiexedViewHelper.CARDZONE_VIEW);
				String err2 = (String) msg.obj;
				if (!Util.isEmptyStr(err2)) {
					UtilHelper.showCustomDialog(mAct, err2);
				}
				break;
			case HANDLER_MATCH_INFO:
				linearMainContent.setVisibility(View.VISIBLE);
				linearProgress.setVisibility(View.GONE);
				/** 报名基本信息1:显示报名条件 **/
				setHAmumentUiContent(setStartMatchOption(), matchName, applys,(String) msg.obj);
				if (isTimerMath) { // 定时赛
					lyState.setVisibility(View.GONE);
				} else {
					// 报名状态容器可见,激战人数不可见
					lyState.setVisibility(View.VISIBLE);
				}

				break;

			case HANDLER_NEXT_START_TIME:
				setHAmusementStartTime(getStartTime());
				break;

			case HANDLER_ONLINE_SUCCESS: // 显示已报名与正在比赛人数
				/** 请求报名和正在比赛人数 **/
				//int allPeoples = msg.arg1;// 参赛总人数（这里不需要总人数显示） 未使用
				int currPeople = msg.arg2;// 已报名人数
				if (isTimerMath) {
					// 设置当前报名人数(定时赛)可见
					setCurrentPersons(currPeople);
				} else {
					// 设置当前报名人数(非定时赛)不可见
					layoutCurrentPerson.setVisibility(View.GONE);
					// 更新报名状态
					setHAmusementAttendState(dwStartMaxCount, currPeople);
				}
				break;
			case HANDLER_EXIT_MATCH_SUCCESS: // 退赛成功，修改为：我要报名，以及其他操作
				// 我要报名;//退赛成功后显示文字
				btnAttendOrExit.setText(R.string.match_baomin);
				// 更新显示UI为：我要报名
				ToastExitMatch(); // 退赛成功显示图片
				setAttendFlag(true);
				break;
			case HANDLER_EXIT_MATCH_FAIL: // 娱乐赛退赛失败
				String msgObj = (String) msg.obj;
				Toast.makeText(mAct, msgObj, Toast.LENGTH_LONG).show();// 退赛结果消息
				break;

			case HANDLER_HAMUSEMENT_TIMER_COUNT:
				/** 倒计时 **/
				// 显示剩余时间
				String time = UtilHelper.getTimeSecond(msg.arg1 * 1000);
				setHAmusementCountdownTime(time, isTimerMath);

				break;

			case HANDLER_HAMUSEMENT_TIMER_COUNT_END:
				/** 倒计时结束,可能情况，进入了游戏，游戏人数不够，进入不了 **/
				if (msg.arg1 <= 0 && taskTimer != null) {
					taskTimer.cancel();
					taskTimer = null;
				}
				break;
			case HANDLER_ONLINE_TIMER: // 定时向服务端发送已报名和正在比赛人数请求
				// 请求当前比赛人数
				applyOnlineAndMatchPerson();
				break;

			case HANDLER_APPLY_MATCH_SUCCESS: // 报名成功
				ToastShowMatchSuccess(); // 报名成功显示图片
				setAttendFlag(false);
				// 报名成功后显示文字 “退赛"");
				btnAttendOrExit.setText(R.string.match_exit);
				break;

			case HANDLER_APPLY_MATCH_NOT_OPTION: // 不符合参赛条件
				// 返回分区
				FiexedViewHelper.getInstance().skipToFragment(FiexedViewHelper.CARDZONE_VIEW);
				String popMessage = (String) msg.obj;
				if(!Util.isEmptyStr(popMessage)){
					UtilHelper.showCustomDialog(mAct,popMessage);
				}

				break;
			case HANDLER_APPLY_MATCH_ED: // 已报名
				// 改变按钮状态
				btnAttendOrExit.setText(R.string.match_exit);
				setAttendFlag(false);
				UtilHelper.showCustomDialog(mAct, mResource.getString(R.string.ddz_signuped));
				break;
			case HANDLER_APPLY_MATCH_ALREADY_OTHER_ROOM: // 已经在其他乐豆房间(上次强退游戏未结束)
				String popmsg=(String) msg.obj;
				// 返回分区
				FiexedViewHelper.getInstance().skipToFragment(FiexedViewHelper.CARDZONE_VIEW);
				if(!Util.isEmptyStr(popmsg)){
					UtilHelper.showCustomDialog(mAct,popmsg);
				}

				break;
			case HANDLER_APPLY_MATCH_NOT_BEAN_AND_NOT_COST: // 报名费用不足

				FiexedViewHelper.getInstance().skipToFragment(FiexedViewHelper.CARDZONE_VIEW);
				String info = (String) msg.obj;
				if(Util.isEmptyStr(info)){
					info = mResource.getString(R.string.ddz_cost_not_enough);
				}
				UtilHelper.showCustomDialog(mAct, info);

				break;

			case HANDLER_MATCH_OTHER: // 比如：关闭加载界面，重新请求人数等
				FiexedViewHelper.getInstance().skipToFragment(FiexedViewHelper.CARDZONE_VIEW);
				String err = (String) msg.obj;
				if (!Util.isEmptyStr(err)) // 有信息
				{
					UtilHelper.showCustomDialog(mAct, err);
				}
				// 屏蔽退出登录服务器
				ApplyOnlineAndMatchPersonTimer(false);

				break;

			case HANDLER_REWARD_CONTENT: // 拼凑奖励内容
				String awardStr=(String) msg.obj;
				tvRewardContent.setText(awardStr);
				break;
			case HANDLER_USER_RECORD: // 拼凑用户战绩内容
				// tvRecordContent.setText(recordContent); 
				int matchState=msg.arg1;
				int roomId=msg.arg2;
				InvokeUILogic(matchState,roomId);

				break;
			case HANDLER_MATCH_BEFORE_NOTICE:
				final Bundle bundle = msg.getData();
				if (bundle != null) {
					final String text=bundle.getString("text");
					final long dataId=bundle.getLong("dataID");
					UtilHelper.showCustomDialog(mAct,text,
							new OnClickListener() {
						@Override
						public void onClick(View v) {
							NodeData nodeData=AllNodeData.getInstance(mAct).getNodeDataByDataId(dataId);
							if(nodeData!=null){
								HallDataManager.getInstance().setCurrentNodeData(nodeData);
								if (FiexedViewHelper.getInstance().getCurFragment() == FiexedViewHelper.COCOS2DX_VIEW) {
									// 强退 ,跳转会分区
									sendLoginDataId(dataId);
									GameUtilJni.onZoneEvent(GameUtilJni.EventZone_BackToMatchView);

								} else if(FiexedViewHelper.getInstance().getCurFragment() == FiexedViewHelper.AMUSEMENT_ROOM){
									//do nothing
								}else{
									sendLoginMatchFromGame(dataId);
								}
							}

						}
					},true,30); //30秒后自动消失
				}

				break;
			case HANDLER_MATCH_STARTED:
				final Bundle b = msg.getData();
				if (b != null) {
					final String text=b.getString("text");
					final long dataId=b.getLong("dataID");

					NodeData nodeData=AllNodeData.getInstance(mAct).getNodeDataByDataId(dataId);
					if(nodeData!=null){
						HallDataManager.getInstance().setCurrentNodeData(nodeData);

						if (FiexedViewHelper.getInstance().getCurFragment() == FiexedViewHelper.COCOS2DX_VIEW) {
							UtilHelper.showCustomDialog(mAct,text,
									new OnClickListener() {
								@Override
								public void onClick(View v) {
									// 强退 ,跳转会分区
									sendLoginDataId(dataId);
									GameUtilJni.onZoneEvent(GameUtilJni.EventZone_BackToMatchView);
									//GameUtilJni.onZoneEvent(GameUtilJni.EventZone_BackToCardZone);
								}

							},true);
						}else {
							sendLoginMatchFromGame(dataId);
						}
					}
				}
				break;
			case HANDLER_START_GAME: // 比赛开始通知
				int currentRoomId = msg.arg1;
				// 删除滚动
				//removeTimerTask(currentDataId);
				// 请求进入房间
				if (FiexedViewHelper.getInstance().cardZoneFragment != null) {
					if (FiexedViewHelper.getInstance().cardZoneFragment.gameRoomAssociated != null) {
						FiexedViewHelper.getInstance().cardZoneFragment.gameRoomAssociated.enterRoom(currentRoomId);
					}
				}
				// 2013-1-18添加加载屏蔽UI
				FiexedViewHelper.getInstance().skipToFragment(FiexedViewHelper.LOADING_VIEW);
				if (FiexedViewHelper.getInstance().loadingFragment != null) {
					FiexedViewHelper.getInstance().loadingFragment.setLoadingType(mResource.getString(R.string.ddz_loading_match),
							NodeDataType.NODE_TYPE_NOT_DO);
				}

				break;
			default:
				break;
			}
		}
	};


	// ----------------------private协议接收方法区域------------------------------------

	/**
	 * 比赛详情http地址
	 * @return
	 */
	private String getMatchInfoPath(){
		StringBuilder sb=new StringBuilder();
		sb.append(AppConfig.MATCHINFO);
		sb.append('?');
		sb.append("urlid=").append(wMRuleUrlId);
		sb.append('&');
		sb.append("subid=").append(wSRuleUrlId);
		sb.append('&');
		sb.append("cid=").append(AppConfig.CID);

		return sb.toString();
	}

	/**
	 * 根据报名状态，设置btn字符
	 * @param isApply
	 */
	private void setApplyStatus(boolean isApply){
		if(btnAttendOrExit!=null){
			if(isApply){
				btnAttendOrExit.setText(R.string.match_exit);	
			}else{
				btnAttendOrExit.setText(R.string.match_baomin);
			}
		}


	}

	/****
	 * @Title: registerReceiveAgreement
	 * @Description: 注册接受协议
	 * @version: 2012-11-7 上午10:49:15
	 */
	private void registerReceiveAgreement() {
		doReceiveMatchInfo(); // 报名基本信息返回 接受：201---103

		matchLoginResult(); // 登录结果返回 收：201---102(登录接受放在一起)

		doReceiveMatchStartTime(); // 定点赛开赛时间 接受:201----308

		startGameReceive(); // 比赛开始通知（服务器下发） 201---205

		doReceiveRewardContent(); // 比赛奖励内容（服务器下发） 201---105

		doReceiveUserRecord(); // 用户战绩内容以及比赛状态（服务器下发） 201---304

		//doReceiveSUB_AS_SYS_MESSAGE(); // 系统消息下发201---305  未使用

		doReceiveMatchStartNotice(); // 定时赛开赛前通知（服务器下发） 201---401

		receiveExitGame(); // 接受退赛 201-204
	}

	/**
	 * @Description: 登录结果返回 收：201---102
	 * @param command
	 * @version: 2011-11-10 下午02:56:51
	 */
	private void matchLoginResult() {
		// 定义接受数据的协议
		short[][] parseProtocol = { { MDM_MATCH_APPLY, SUB_AS_USER_LOGON_RESULT } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				// 解析接受到的网络数据
				TDataInputStream tdis = netSocketPak.getDataInputStream();
				tdis.setFront(false);
				byte byResult = tdis.readByte(); // 登录结果
				String msgStr = tdis.readUTFByte(); // 返回消息
				if (byResult != 0) { // 登录失败显示消息
					Message msg = mMatchHandler.obtainMessage();
					msg.what=HANDLER_USER_LOGIN_FAIL;
					msg.obj=msgStr;
					mMatchHandler.sendMessage(msg);
				} else {  // 0 --登录成功
					mMatchHandler.sendEmptyMessage(HANDLER_USER_LOGIN_SUCCESS);
				}
				return true;
			}
		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		nPListener.setOnlyRun(false); // 表示一直监听
	}

	/**
	 * @Title: doReceiveMatchInfo
	 * @Description: 报名基本信息返回 接受：201---103
	 * @param command
	 * @version: 2011-11-10 下午03:16:34
	 */
	private void doReceiveMatchInfo() {
		// 定义接受数据的协议
		short[][] parseProtocol = { { MDM_MATCH_APPLY, SUB_AS_MATCH_INFO } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				TDataInputStream tdis = netSocketPak.getDataInputStream();
				tdis.setFront(false);

				byte byMatchNameLen = tdis.readByte();// 比赛名称长度
				byte byAwardLen = tdis.readByte(); // 奖励信息长度
				wMRuleUrlId = tdis.readShort(); // 比赛规则主UrlID
				wSRuleUrlId = tdis.readShort(); // 比赛规则子UrlID
				tdis.readShort(); // 比赛图片ID

				// 下面部分也是(X人赛多少人的限制)
				dwStartMaxCount = tdis.readInt(); // 开赛的最大人数
				dwStartMinCount = tdis.readInt();// 开赛的最小人数

				final int startTime = tdis.readInt(); // 开赛时间(1：定时赛；0：满人赛)
				// 是否定时赛
				isTimerMath = (startTime != 0);

				tdis.readShort(); // 比赛大约时长

				final byte byConditionsCount = tdis.readByte();// 参赛条件数量
				final byte byCostCount = tdis.readByte();// 报名费用种类数量
				matchName = tdis.readUTF(byMatchNameLen);// 比赛名称
				tdis.readUTF(byAwardLen);// String 奖励信息 未使用

				// 比赛条件(报名条件)
				Conditions conds[] = null;
				if (byConditionsCount >= 0) {
					conds = new Conditions[byConditionsCount];
					for (int i = 0; i < byConditionsCount; i++) {
						conds[i] = new Conditions(tdis);
					}
				}
				// 比赛费用(报名费用)
				if (byCostCount >= 0) {
					applys = new Conditions[byCostCount];
					for (int i = 0; i < byCostCount; i++) {
						applys[i] = new Conditions(tdis);
					}
				}
				String amuText = mResource.getString(R.string.match_condition) + getConditionText(conds, 1);
				Message msg=mMatchHandler.obtainMessage();
				msg.what=HANDLER_MATCH_INFO;
				msg.obj=amuText;
				mMatchHandler.sendMessage(msg);
				return true;
			}
		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		nPListener.setOnlyRun(false); // 表示一直运行
	}

	/**
	 * @Description: 定点赛开赛时间 接受:201----308
	 * @param command
	 * @return
	 * @version: 2012-4-25 下午04:12:15
	 */
	private void doReceiveMatchStartTime() {
		// 定义接受数据的协议
		short[][] parseProtocol = { { MDM_MATCH_APPLY, SUB_AS_NEXT_START_TIME } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				// 解析接受到的网络数据
				TDataInputStream tdis = netSocketPak.getDataInputStream();
				tdis.setFront(false);
				dwStartTime = tdis.readInt(); // 开赛时间 12-11-07-11-10
				dwDisStartTime = tdis.readInt();// 距离开赛时间（单位秒）

				mMatchHandler.sendEmptyMessage(HANDLER_NEXT_START_TIME);
				return true;
			}
		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		nPListener.setOnlyRun(false); 
	}

	/**
	 * @Title: startGameReceive
	 * @Description: 比赛开始通知（服务器下发） 201---205 （备注：下发比赛开通知的时候需要移除定时器以及滚动项）
	 * @version: 2012-9-5 下午04:11:37
	 */
	private void startGameReceive() {
		// 定义接受数据的协议
		short[][] parseProtocol = { { MDM_MATCH_APPLY, SUB_AS_MATCH_START } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				// 当前UI为非游戏中(分区UI，在提示时间内不作跳转，在分区中才跳转)
				if (FiexedViewHelper.getInstance().getCurFragment() != FiexedViewHelper.COCOS2DX_VIEW) {
					// 解析接受到的网络数据
					TDataInputStream tdis = netSocketPak.getDataInputStream();
					// 保存读取信息
					tdis.setFront(false);

					int currentRoomId = tdis.readInt(); // 房间ID
					tdis.readInt(); // ip地址 ,未使用
					tdis.readInt(); // prot端口,未使用
					RoomData room = new RoomData();
					room.RoomID = currentRoomId;
					room.GameType = RoomData.GAME_GENRE_DONGGUAN_MATCH;
					// 设置当前比赛的房间
					HallDataManager.getInstance().setCurrentRoomData(room);
					Message msg=mMatchHandler.obtainMessage();
					msg.what=HANDLER_START_GAME;
					msg.arg1=currentRoomId;
					mMatchHandler.sendMessage(msg);
				} 
				return true;
			}
		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		nPListener.setOnlyRun(false); 
	}

	/***
	 * @Title: receiveRewardContent
	 * @Description: 接受比赛奖励内容信息
	 * @version: 2012-11-12 上午10:42:08
	 */
	private void doReceiveRewardContent() {
		// 定义接受数据的协议
		short[][] parseProtocol = { { MDM_MATCH_APPLY, SUB_AS_MATCH_DESC } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				TDataInputStream tdis = netSocketPak.getDataInputStream();
				tdis.setFront(false);
				String awardStr = tdis.readUTFShort();
				awardStr = rewardReplace(awardStr, '#', '\n');
				Message msg=mMatchHandler.obtainMessage();
				msg.what=HANDLER_REWARD_CONTENT;
				msg.obj=awardStr;
				mMatchHandler.sendMessage(msg);
				return true;
			}
		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		nPListener.setOnlyRun(false); 
	}

	/**
	 * @Description: 用户战绩返回（服务器下发） 接受：201----304
	 * @param command
	 * @version: 2011-11-10 下午06:27:05
	 */
	private void doReceiveUserRecord() {
		// 定义接受数据的协议
		short[][] parseProtocol = { { MDM_MATCH_APPLY, SUB_AS_USER_INFO_RESP } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				// 解析接受到的网络数据
				TDataInputStream tdis = netSocketPak.getDataInputStream();
				tdis.setFront(false);
				int roomId=-1;

				int dwBestStandings = tdis.readInt(); // 最佳战绩 0:为未参赛过或未获得名次
				int dwFinalCount = tdis.readInt(); // 决赛次数
				int dwCompetitionCount = tdis.readInt(); // 参赛次数

				/**********************************
				 * 比赛状态 0：未知状态 1：未报名 2：已报名 3：等待进入比赛 4：等待开赛 5：正在比赛
				 **********************************/
				final byte matchState = tdis.readByte();
				final byte byConditionsCount = tdis.readByte(); // 条件数
				final byte byCostCount = tdis.readByte(); // 费用数

				// 条件
				tdis.skip(byConditionsCount * 2); // 跳过所有条件结构体，此处为条件的符合状态结构体固定2个字节
				tdis.skip(byCostCount * 2);

				if (matchState == 3 || matchState == 4 || matchState == 5) {
					roomId = tdis.readInt();
					tdis.readInt();  //ip 未使用
					tdis.readInt(); //port 未使用
				}

				//比赛内容信息，未使用
				StringBuilder sb=new StringBuilder();
				sb.append( mResource.getString(R.string.ddz_best_rank));
				sb.append(dwBestStandings);
				sb.append("\n");
				sb.append(mResource.getString(R.string.ddz_match_time));
				sb.append(dwCompetitionCount);
				sb.append("\n");
				sb.append(mResource.getString(R.string.ddz_finals_match_time));
				sb.append(dwFinalCount);
				//String recordStr=sb.toString();  //比赛内容信息，未使用

				Message msg = mMatchHandler.obtainMessage();
				msg.what=HANDLER_USER_RECORD;
				msg.arg1=matchState;
				msg.arg2=roomId;
				mMatchHandler.sendMessage(msg);
				return true;
			}
		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		nPListener.setOnlyRun(false); 
	}

	/***
	 * @Title: doReceiveSUB_AS_SYS_MESSAGE
	 * @Description: 系统消息（服务下发）201--305
	 * @version: 2012-11-20 上午10:22:44
	 */
	//	private void doReceiveSUB_AS_SYS_MESSAGE() {
	//		// 定义接受数据的协议
	//		short[][] parseProtocol = { { MDM_MATCH_APPLY, SUB_AS_SYS_MESSAGE } };
	//		// 创建协议解析器
	//		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
	//			@Override
	//			public boolean doReceive(NetSocketPak netSocketPak) {
	//				// 解析接受到的网络数据
	//				TDataInputStream tdis = netSocketPak.getDataInputStream();
	//				if (tdis == null) {
	//					return true;
	//				}
	//				// 保存读取信息
	//				tdis.setFront(false);
	//				tdis.readByte();
	//				String msg = tdis.readUTFShort();
	//
	//				return true;
	//			}
	//		};
	//		// 注册协议解析器到网络数据分发器中
	//		NetSocketManager.getInstance().addPrivateListener(nPListener);
	//		nPListener.setOnlyRun(false); 
	//	}

	/***
	 * @Description:定时赛(开赛前)通知 401
	 * @version: 2012-11-20 上午09:45:03
	 */
	private void doReceiveMatchStartNotice() {
		// 定义接受数据的协议
		short[][] parseProtocol = { { MDM_MATCH_APPLY,SUB_AS_MATCHBEFORE_NOTICE } ,
				{ MDM_MATCH_APPLY,SUB_AS_MATCHBEFORE_START }};
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				if (netSocketPak.getSub_gr() == SUB_AS_MATCHBEFORE_NOTICE) {
					TDataInputStream tdis = netSocketPak.getDataInputStream();
					// 保存读取信息
					tdis.setFront(false);
					final long dataID = tdis.readLong(); // 报名服务器网络标识
					final String text = tdis.readUTFShort(); // 消息内容

					Bundle data = new Bundle();
					data.putLong("dataID", dataID);
					data.putString("text", text);

					Message msg = mMatchHandler.obtainMessage();
					msg.what=HANDLER_MATCH_BEFORE_NOTICE;
					msg.setData(data);
					mMatchHandler.sendMessage(msg);
				}else if(netSocketPak.getSub_gr() == SUB_AS_MATCHBEFORE_START){
					TDataInputStream tdis = netSocketPak.getDataInputStream();
					// 保存读取信息
					tdis.setFront(false);
					final long dataID = tdis.readLong(); // 报名服务器网络标识
					final String text = tdis.readUTFShort(); // 消息内容

					Bundle data = new Bundle();
					data.putLong("dataID", dataID);
					data.putString("text", text);

					Message msg = mMatchHandler.obtainMessage();
					msg.what=HANDLER_MATCH_STARTED;
					msg.setData(data);
					mMatchHandler.sendMessage(msg);
				}
				// 解析接受到的网络数据

				return true;
			}
		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		nPListener.setOnlyRun(false); 
	}




	/****
	 * @Title: receiveExitGame
	 * @Description: 接受退赛 201-204
	 * @version: 2013-1-27 下午02:20:47
	 */
	private void receiveExitGame() {
		// 定义接受数据的协议
		short[][] parseProtocol = { { MDM_MATCH_APPLY,
			SUB_AS_CANCEL_APPLY_RESULT } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				// 解析接受到的网络数据
				TDataInputStream tdis = netSocketPak.getDataInputStream();
				tdis.setFront(false);

				byte byResult = tdis.readByte();
				// 退赛失败
				String msgStr = tdis.readUTFByte();
				if (byResult == 0) {
					// 退赛成功,发送通知修改UI：我要报名/我要退赛
					mMatchHandler.sendEmptyMessage(HANDLER_EXIT_MATCH_SUCCESS);
				} else {
					Message msg = mMatchHandler.obtainMessage();
					msg.what=HANDLER_EXIT_MATCH_FAIL;
					msg.obj=msgStr;
					mMatchHandler.sendMessage(msg);
				}
				return true;
			}
		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		nPListener.setOnlyRun(false);
	}

	/**
	 * @Title: applyMatch
	 * @Description: 娱乐赛报名
	 * @param userID
	 *            用户ID
	 * @param costid
	 *            费用byCostID
	 * @version: 2012-9-5 上午11:39:47
	 */
	private void applyMatch(int userID, byte costid) {
		// 构造数据包
		TDataOutputStream tdous = new TDataOutputStream(false);
		tdous.writeInt(userID);
		tdous.writeByte(costid); // 费用类型ID
		NetSocketPak mConsumptionSock = new NetSocketPak(MDM_MATCH_APPLY,
				SUB_AS_MATCH_APPLY, tdous);
		// 定义接受数据的协议
		short[][] parseProtocol = { { MDM_MATCH_APPLY, SUB_AS_APPLY_RESULT } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				// 解析接受到的网络数据
				TDataInputStream tdis = netSocketPak.getDataInputStream();
				// 保存读取信息
				tdis.setFront(false);
				byte byResult = tdis.readByte();
				String msgStr = tdis.readUTFByte();

				Message msg=mMatchHandler.obtainMessage();

				switch (byResult) {
				case 0: // 成功
					msg.what=HANDLER_APPLY_MATCH_SUCCESS;
					break;
				case 2: // 已报名
					msg.what=HANDLER_APPLY_MATCH_ED;
					break;
				case 3: // 不符合参赛条件(如：乐豆大于本房间要求上限，请进入其他房间)
					msg.what=HANDLER_APPLY_MATCH_NOT_OPTION;
					msg.obj=msgStr;
					break;
				case 4: // 费用不足(系统会下发消息，弹出客户端快捷购买)
					msg.what=HANDLER_APPLY_MATCH_NOT_BEAN_AND_NOT_COST;
					msg.obj = msgStr;
					break;
				case 5: // 已经在其他乐豆房间
					msg.what=HANDLER_APPLY_MATCH_ALREADY_OTHER_ROOM;
					msg.obj=msgStr;
					break;
				default:
					msg.what=HANDLER_MATCH_OTHER;
					msg.obj=msgStr;
					break;

				}
				mMatchHandler.sendMessage(msg);
				return true;
			}
		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		// 发送协议
		NetSocketManager.getInstance().sendData(mConsumptionSock);

		// 清理协议对象
		mConsumptionSock.free();

		nPListener.setOnlyRun(false); // 一直运行

	}


	/**
	 * @Title: sendSUB_AS_ONLINE_INFO_REQ
	 * @Description: 已报名和正在比赛人数请求(2种赛事)
	 * @version: 2011-11-10 下午06:19:17
	 */
	private void applyOnlineAndMatchPerson() {
		// 构造数据包
		TDataOutputStream tdous = new TDataOutputStream();
		NetSocketPak mConsumptionSock = new NetSocketPak(MDM_MATCH_APPLY,
				SUB_AS_ONLINE_INFO_REQ, tdous);
		// 定义接受数据的协议
		short[][] parseProtocol = { { MDM_MATCH_APPLY, SUB_AS_ONLINE_INFO_RESP } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				// 解析接受到的网络数据
				TDataInputStream tdis = netSocketPak.getDataInputStream();
				// 保存读取信息
				tdis.setFront(false);
				int allPeoples = tdis.readInt();// 参赛总人数
				int currPeople = tdis.readInt();// 已报名人数
				// 发送通知更新UI
				Message msg=mMatchHandler.obtainMessage();
				msg.what=HANDLER_ONLINE_SUCCESS;
				msg.arg1=allPeoples;
				msg.arg2=currPeople;
				mMatchHandler.sendMessage(msg);
				return true;
			}
		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		// 发送协议
		NetSocketManager.getInstance().sendData(mConsumptionSock);
		// 清理协议对象
		mConsumptionSock.free();
	}

	// --------------------------------private方法-------------------------------------------
	/****
	 * @Title: attendHAmusement
	 * @Description: (满人赛，定时赛) 报名/退赛 操作逻辑封装
	 * @version: 2012-10-25 下午05:49:54
	 */
	private void attendExitHAmusement() {
		if (getAttendFlag()) { // 报名
			// 获得选中RadionButton
			int isChecked=0; 
			for (int i = 0; i < rdoGroupHAmusement.getChildCount(); i++) {
				RadioButton rdo = (RadioButton) rdoGroupHAmusement.getChildAt(i);
				if (rdo.isChecked()) {
					isChecked=i;
					break;
				}
			}
			final Conditions applys[] = (Conditions[]) rdoGroupHAmusement.getTag();
			if (applys == null) {
				return;
			}
			if (applys.length == 0) {
				// 表示此赛场报名免费，直接发送报名协议
				sendMatchApply(0);
			}else{
				if(applys.length >isChecked){
					sendMatchApply(applys[isChecked].byCostID);
				}
			}

		} else {// 退赛
			exitMatch();
		}
	}









	/**
	 * @Title: sendMatchApply
	 * @Description: 发送报名
	 * @param cost
	 *            报名条件类中byCostID
	 * @version: 2011-12-21 下午05:23:14
	 */
	private void sendMatchApply(int cost) {
		int userid = getUserID();
		if (userid != -1) {
			applyMatch(userid, (byte) cost);
		}
	}

	private int getUserID() {
		UserInfo user = HallDataManager.getInstance().getUserMe();
		return user == null ? -1 : user.userID;
	}


	/**
	 * 设置报名状态
	 * 获取报名状态 True 未报名。false已报名
	 */
	private void setAttendFlag(boolean isAttend) {
		this.isWillAttending = isAttend;
	}

	/**
	 * 获取报名状态 True 未报名。false已报名
	 */
	private boolean getAttendFlag() {
		return isWillAttending;
	}



	/**
	 * @Title: setHAmusementStartTime
	 * @Description: 设置赚话费面板信息(定时赛比赛开赛时间)xx-xx xx:xx
	 * @param mstrStartTime
	 *            开赛时间字符串
	 * @version: 2012-10-9 上午11:07:51
	 */
	public void setHAmusementStartTime(String mstrStartTime) {
		if (tvCompetitionTime != null) {
			if (mstrStartTime.length() <= 0) {
				tvCompetitionTime.setText("");
				tvCompetitionTime.setVisibility(View.GONE);
			} else {
				tvCompetitionTime.setText(mstrStartTime);
				tvCompetitionTime.setVisibility(View.VISIBLE);
			}
		}

	}

	/**
	 * @Title: setCurrentPersons
	 * @Description: 设置激战人数UI信息
	 * @param mCurrentPersons
	 *            激战人数字符串
	 * @version: 2012-10-9 上午11:07:51
	 */
	private void setCurrentPersons(int curPersons) {
		if (layoutCurrentPerson != null) {
			if (curPersons <= 0) {
				layoutCurrentPerson.removeAllViews();
				layoutCurrentPerson.setVisibility(View.GONE);
			} else {
				layoutCurrentPerson.removeAllViews();
				String curPersonsStr=String.valueOf(curPersons);
				char[] persionChar = curPersonsStr.toCharArray();
				for (int i = 0; i < persionChar.length; i++) {
					char digital = persionChar[i];
					int integer = Integer.valueOf(String.valueOf(digital));
					ImageView iv = new ImageView(mAct);
					ViewGroup.MarginLayoutParams mlp = new ViewGroup.MarginLayoutParams(
							layoutCurrentPerson.getLayoutParams());
					mlp.leftMargin = -2;
					iv.setLayoutParams(mlp);
					switch (integer) {
					case 0:
						iv.setImageResource(R.drawable.match_0);
						break;
					case 1:
						iv.setImageResource(R.drawable.match_1);
						break;
					case 2:
						iv.setImageResource(R.drawable.match_2);
						break;
					case 3:
						iv.setImageResource(R.drawable.match_3);
						break;
					case 4:
						iv.setImageResource(R.drawable.match_4);
						break;
					case 5:
						iv.setImageResource(R.drawable.match_5);
						break;
					case 6:
						iv.setImageResource(R.drawable.match_6);
						break;
					case 7:
						iv.setImageResource(R.drawable.match_7);
						break;
					case 8:
						iv.setImageResource(R.drawable.match_8);
						break;
					case 9:
						iv.setImageResource(R.drawable.match_9);
						break;
					}
					layoutCurrentPerson.addView(iv);
				}
				layoutCurrentPerson.setVisibility(View.VISIBLE);
			}
		}
	}

	/**
	 * @Title: setHAmusementAttendState
	 * @Description: 参赛人数状态
	 * @param allPeoples
	 *            参赛总人数
	 * @param currPeople
	 *            已报名人数
	 * @version: 2012-10-9 下午03:00:01
	 */
	private void setHAmusementAttendState(int limit, int currPeople) {
		lyState.setVisibility(View.VISIBLE);
		if (tvAttendPerson != null)
			tvAttendPerson.setText(currPeople + "/" + limit);
	}


	/**
	 * @Title: setHAmusementAttendInfo
	 * @Description: 设置报名条件
	 * @param mAttendOption
	 * @version: 2012-10-9 下午04:17:52
	 */
	private void setHAmusementAttendInfo(String mAttendOption) {
		if (tvAttendOption != null)
			tvAttendOption.setText(mAttendOption);
	}

	/***
	 * @Title: setHAmusementAttendInfoFee
	 * @Description: 设置报名费用
	 * @param mAttendFee
	 * @version: 2012-10-9 下午04:41:58
	 */
	private void setHAmusementAttendInfoFee(Conditions applys[]) {
		String matchFeiYongStr = mResource.getString(R.string.ddz_match_feiyong); // 报名费用
		String freeStr = mResource.getString(R.string.ddz_match_free); // 免费 
		if (tvAttendFee != null)
			tvAttendFee.setText(matchFeiYongStr);
		if (applys == null || applys.length == 0) {
			if (tvAttendFee != null)
				tvAttendFee.setText(matchFeiYongStr + freeStr);
		}
		if (rdoGroupHAmusement.getChildCount() > 0)
			rdoGroupHAmusement.removeAllViews();
		int len = 0;
		// 下面是添加单选按钮组
		if (applys != null) {
			len = applys.length;
			for (int i = 0; i < len; i++) {
				Conditions info = applys[i]; // 报名条件
				rdoGroupHAmusement.addView(createRadio(info.getDesConditions(),
						info, false));
			}
			// 设置按钮组的Tag为:条件组
			rdoGroupHAmusement.setTag(applys);
			// 默认选中第一个
			RadioButton rdo = (RadioButton) rdoGroupHAmusement.getChildAt(0);
			if (rdo != null)
				rdo.setChecked(true);
		}
	}

	/****
	 * @Title: createRadio
	 * @Description: 构造RadioButton
	 * @param text
	 *            显示文本
	 * @param tag
	 *            Tag标记(条件对象)Conditions
	 * @param id
	 *            RadioButton id,
	 * @param checked
	 *            是否选中
	 * @return
	 * @version: 2012-10-10 下午02:55:48
	 */
	private RadioButton createRadio(CharSequence text, Object tag,
			boolean checked) {
		RadioButton rdo = new RadioButton(mAct);
		rdo.setButtonDrawable(R.drawable.radio_selector);
		rdo.setPadding(30, 0, 0, 0);
		ViewGroup.MarginLayoutParams mlp = new ViewGroup.MarginLayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		mlp.setMargins(0, 0, 10, 0);
		rdo.setLayoutParams(mlp);
		rdo.setText(text);
		rdo.setTag(tag);
		rdo.setChecked(checked);
		return rdo;
	}

	
	/****
	 * @Title: sendLoginMatchFromGame
	 * @Description: 登录报名服务器，用于在游戏中弹出开赛提前通知，确定操作
	 * @param dataId
	 * @version: 2013-2-5 下午02:23:02
	 */
	private void sendLoginMatchFromGame(long dataId) {
		FiexedViewHelper.getInstance().skipToFragment(FiexedViewHelper.AMUSEMENT_ROOM);
		sendLoginDataId(dataId);
	}

	/**
	 * @Title: ToastShowMatchSuccess
	 * @Description: Toast显示报名成功(图片形式)
	 * @version: 2012-10-8 上午11:38:05
	 */
	private void ToastShowMatchSuccess() {
		ToastShowImage(R.drawable.match_sign_match_success);
	}

	/**
	 * @Title: ToastExitMatch
	 * @Description: Toast退赛成功(图片形式)
	 * @version: 2012-10-8 上午11:38:41
	 */
	private void ToastExitMatch() {
		ToastShowImage(R.drawable.match_exit_match_success);
	}

	private void ToastShowImage(int drawableId) {
		android.widget.Toast toast = new android.widget.Toast(mAct);
		toast.setGravity(Gravity.CENTER, 0, 0);
		FrameLayout toastView = new FrameLayout(mAct);
		toastView.setBackgroundResource(drawableId);
		toast.setView(toastView);
		toast.show();
	}

	/***
	 * @Title: InvokeUILogic
	 * @Description: 根据下发的赛事状态处理UI逻辑等
	 * @param matchState
	 * @version: 2012-11-20 下午09:19:21
	 */
	private void InvokeUILogic(int matchState,int roomId ) {
		/**********************************
		 * 比赛状态 0：未知状态 1：未报名 2：已报名 3：等待进入比赛 4：等待开赛 5：正在比赛
		 **********************************/
		switch (matchState) {
		case 0:
		case 1: // 未报名
			setAttendFlag(true); //设置未报名
			setApplyStatus(false);
			break;
		case 2: // 已报名
			setAttendFlag(false); //设置已经报名
			setApplyStatus(true);
			break;
		case 3: // 等待进入比赛
		case 4: // 等待开赛
		case 5: // 正在比赛
			if (FiexedViewHelper.getInstance().cardZoneFragment != null) {
				if (FiexedViewHelper.getInstance().cardZoneFragment.gameRoomAssociated != null) {
					Log.v(TAG, "比赛开始...");
					RoomData room = new RoomData();
					room.RoomID = roomId;
					room.GameType = RoomData.GAME_GENRE_DONGGUAN_MATCH;
					// 设置当前比赛的房间
					HallDataManager.getInstance().setCurrentRoomData(room);

					FiexedViewHelper.getInstance().skipToFragment(FiexedViewHelper.LOADING_VIEW);
					if (FiexedViewHelper.getInstance().loadingFragment != null) {
						FiexedViewHelper.getInstance().loadingFragment.setLoadingType(mResource.getString(R.string.ddz_match_into_game), NodeDataType.NODE_TYPE_NOT_DO);
					}
					FiexedViewHelper.getInstance().cardZoneFragment.gameRoomAssociated.enterRoom(room.RoomID);
				}
			}

			break;
		default:
			break;
		}
	}

	/**
	 * @Title: setStartTime
	 * @Description: 设置开赛时间
	 * @param startTime
	 *            开赛时间
	 * @param disStartTime
	 *            距离开赛倒计时
	 * @param matchName
	 *            比赛时间
	 * @param dataId
	 *            当前节点Id
	 * @return
	 * @version: 2012-11-21 下午05:03:00
	 */
	private String getStartTime() {
		String strStartTime=mResource.getString(R.string.ddz_match_end);
		StringBuilder sb=new StringBuilder();
		String tmpStr = String.valueOf(dwStartTime);
		if (tmpStr.length() < 10  || dwStartTime == 0){
			return strStartTime;
		}else{
			int lengh = tmpStr.length();
			sb.append(mResource.getString(R.string.ddz_match_begin_time));
			sb.append(tmpStr.substring(2, lengh - 6));
			sb.append("-");
			sb.append(tmpStr.substring(4, lengh - 4));
			sb.append(" ");
			sb.append(tmpStr.substring(6, lengh - 2));
			sb.append(":");
			sb.append(tmpStr.substring(8, lengh));
		}
		// 启动定时器,设置倒计时
		startTimer();

		return sb.toString();
	}

	/***
	 * @Title: startTimer
	 * @Description: 启动计时器
	 * @version: 2012-9-27 下午05:21:48
	 */
	private void startTimer() {
		if (taskTimer != null) {
			taskTimer.cancel();
			taskTimer = null;
		}
		taskTimer = new TaskTimer(dwDisStartTime, matchName, currentDataId);
		taskTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				Message msg=mMatchHandler.obtainMessage();
				msg.arg1=taskTimer.mdisTime;

				if (taskTimer.mdisTime > 0){
					taskTimer.mdisTime -= 1; // 因为出来的是秒非毫秒
					msg.what=HANDLER_HAMUSEMENT_TIMER_COUNT;
				} else {
					msg.what=HANDLER_HAMUSEMENT_TIMER_COUNT_END;
				}
				mMatchHandler.sendMessage(msg);
			}
		}, 0, 1000); // 每隔1秒触发一次
	}

	/***
	 * @Title: setHAmumentUiContent
	 * @Description: 报名基本信息1:显示报名条件
	 * @param startMatchOption
	 *            开赛条件
	 * @param pMatchName
	 *            比赛名称
	 * @param applysInfo
	 *            报名费用
	 * @param serverSendOption
	 *            服务器下发报名条件
	 * @version: 2012-11-21 下午04:32:21
	 */
	private void setHAmumentUiContent(String startMatchOption,
			String pMatchName, Conditions applysInfo[], String serverSendOption) {
		setHAmusementPersonLimit(startMatchOption, pMatchName); // 人数限制
		setHAmusementAttendInfo(serverSendOption); // 报名条件
		setHAmusementAttendInfoFee(applysInfo);
		setHAmusementCountdownTime("", false);
	}

	/***
	 * @Title: setHAmusementCountdownTime
	 * @Description: 开赛时间倒计时
	 * @param mCountdownTime
	 * @param isTimerMath
	 *            是否定时赛(定时赛不显示倒计时)
	 * @version: 2012-10-9 下午07:01:00
	 */
	private void setHAmusementCountdownTime(String mCountdownTime,
			boolean isTimerMath) {
		if (tvCountdownTime != null) {
			if (mCountdownTime.trim().length() <= 0) {
				tvCountdownTime.setText("");
				tvCountdownTime.setVisibility(View.GONE);
			} else {
				if (isTimerMath) {
					tvCountdownTime.setVisibility(View.VISIBLE);
					tvCountdownTime.setText(mResource.getString(R.string.ddz_match_count_down) + mCountdownTime);
				} else {
					tvCountdownTime.setVisibility(View.GONE);
				}
			}
		}

	}

	/**
	 * @Title: setHAmusementPersonLimit
	 * @Description: 设置赚话费面板UI信息
	 * @param mstrStartMatchOption
	 *            限制人数（比赛人数）字符串
	 * @version: 2012-10-9 上午11:07:51
	 */
	private void setHAmusementPersonLimit(String mstrStartMatchOption,
			String mMatchName) {
		if (tvPersonLimit != null) {
			if (mstrStartMatchOption.length() <= 0) {
				tvPersonLimit.setText("");
				tvPersonLimit.setVisibility(View.GONE);
			} else {
				tvPersonLimit.setText(mstrStartMatchOption);
				tvPersonLimit.setVisibility(View.VISIBLE);
			}
		}

		if (tvMatchName != null) {
			if (mMatchName.length() <= 0) {
				tvMatchName.setText("");
				tvMatchName.setVisibility(View.GONE);
			} else {
				tvMatchName.setText(mMatchName);
				tvMatchName.setVisibility(View.VISIBLE);
			}
		}

	}

	/**
	 * @Title: stopTimer
	 * @Description: 关闭定时器
	 * @version: 2013-1-27 下午07:22:45
	 */
	private void stopApplyTimer() {
		if (applytimer != null) {
			applytimer.cancel();
			applytimer = null;
		}
	}

	/***
	 * @Title: stopTaskTimer
	 * @Description: 停止任务计时器
	 * @version: 2013-1-27 下午07:28:16
	 */
	private void stopTaskTimer() {
		if (taskTimer != null) {
			taskTimer.cancel();
			taskTimer = null;
		}
	}

	/**
	 * @Title: setStartMatchOption
	 * @Description: 设置开赛条件（限制人数）
	 * @param startMinCount
	 * @param startMaxCount
	 * @return
	 * @version: 2012-10-9 上午10:30:33
	 */
	//setStartMatchOption(dwStartMinCount, dwStartMaxCount);
	private String setStartMatchOption() {
		String limitingStr = mResource.getString(R.string.ddz_match_limiting); // 限制人数
		String personStr = mResource.getString(R.string.ddz_person); // 人
		StringBuilder sb=new StringBuilder();
		sb.append(limitingStr);
		sb.append(dwStartMinCount);
		if (dwStartMinCount != dwStartMaxCount){
			sb.append("-");
			sb.append(dwStartMaxCount);
		}
		sb.append(personStr);
		return sb.toString();
	}

	/**
	 * @Title: getConditionText
	 * @Description:组装报名费的描述信息，1表示比赛条件，2表示比赛费用
	 * @param applys
	 * @param type
	 * @return
	 * @version: 2012-3-26 下午03:28:51
	 */
	private String getConditionText(Conditions applys[], final int type) {
		if (applys == null || applys.length <= 0) {
			if (type == 1) {
				return mResource.getString(R.string.ddz_match_none);
			} else {
				return mResource.getString(R.string.ddz_match_free);
			}
		}
		int len = applys.length;
		final String strCh = type == 1 ? "、" : "";
		String str = (type == 1 || len == 1) ? "" : "\n    ";
		String text = (type == 1 && len != 1) ? "    " : "";

		for (int i = 0; i < len;) {
			final String index = (type == 1 || len == 1) ? "" : mResource.getString(R.string.ddz_match_choose) + (i + 1)
					+ "：";
			text += (applys[i] == null ? "" : str + index)
					+ applys[i].getDesConditions();
			if (++i < len) {
				text += strCh;
			}
		}
		return text;
	}



	private String rewardReplace(String content, char oldChar, char newChar) {
		return content.replace(oldChar, newChar);
	}


}
