package com.mykj.andr.ui.fragment;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mingyou.distributor.NetPrivateListener;
import com.mingyou.distributor.NetSocketPak;
import com.mykj.andr.model.ChallengeInviteInfo;
import com.mykj.andr.model.HallDataManager;
import com.mykj.andr.model.RoomData;
import com.mykj.andr.model.UserInfo;
import com.mykj.andr.net.NetSocketManager;
import com.mykj.andr.ui.fragment.LoadingFragment.NodeDataType;
import com.mykj.comm.io.TDataInputStream;
import com.mykj.comm.io.TDataOutputStream;
import com.mykj.game.FiexedViewHelper;
import com.MyGame.Migu.R;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.Log;
import com.mykj.game.utils.Toast;
import com.mykj.game.utils.Util;
import com.mykj.game.utils.UtilHelper;

public class ChallengeFragment extends FragmentModel{
	public static final String TAG="ChallengeFragment";

	private Activity mAct;
	
	private Resources mResource;
	
    //-----------------------------------------handler what-----------------------------------------------------
	/***登录结果成功 1020**/
	private static final int HANDLER_LOGIN_SUCCESS=1;
	/***登录结果失败1021**/
	private static final int HANDLER_LOGIN_FAIL=2;	

	/**创建邀请码成功**/
	private static final int HANDLER_CREATE_CODE_SUCCESS=3;
	/**创建邀请码失败**/
	private static final int HANDLER_CREATE_CODE_ERROR=4;
	
	/**应邀约战请求(应邀请求结果)成功  **/
	private static final int HANDLER_ACCEPT_INVITE_SUCCESS=5;
	
	/**应邀约战请求(应邀请求结果)失败  **/
	private static final int HANDLER_ACCEPT_INVITE_ERROR=6;

	/**心跳检测Handler 121121收，发**/
	private static final int HANDLER_SUB_HEART_CHECK_RECEIVE_SEND=7;
	
	/**约战游戏开始消息**/
	private static final int HANLDER_START_GAME=8;
	
	/***进入约战区连接失败或者断开 12212***/
	private static final int HANDLER_CONNECT_FAIL_BREAK=9;
		
	/**约战时间倒计时**/
	private static final int HANDLER_CHALLENGE_TIMER_COUNT=10;
	
	/**约战区信息返回成功301----107***/
	private static final int HANLDER_CHALLENGE_INFO_SUCCESS=11;
	
	/**约战时间倒计时结束**/
	private static final int HANDLER_CHALLENGE_TIMER_COUNT_END=12;
		
	/**handler约战区状态  接受:301---108**/
	private static final int HANDLER_CHALLENGE_AREA_STATE=13;
	
	/**消息：邀请码过期通知  接受301----112**/
	private static final int HANDLER_CODE_TIME_OUT=14;
	
	/**邀请码房主**/
	private static final int JUDGE_ROOM_HOLDER=15;
	
	/**非房主**/
	private static final int JUDGE_ROOM_NOT_HOLDER=16;
	

	
    //-----------------------------------------网络协议命令码-----------------------------------------------------------
	/** 约战主命令 301 **/
	private static final short MDM_CHALLENGE_SYS = 301; // 主协议
		
	/** 用户登录 CS_UserLogon 101**/
	private static final short CS_SUB_UserLogon = 101; // 用户登录 CS_UserLogon
	
	/** 登录结果返回 CS_LogonResult 102**/
	private static final short CS_SUB_LogonResult = 102; // 登录结果返回 CS_LogonResult		
	
	/** 创建约战邀请码 CS_CreateCode 103 **/
	private static final short CS_SUB_CreateCode = 103; // 创建约战邀请码 CS_CreateCode
	
	/** 创建约战邀请码返回  104**/
	private static final short CS_SUB_CreateCodeResult = 104; // 创建约战邀请码返回
	
	/** 应邀请求 CS_AcceptInvite 105**/
	private static final short CS_SUB_AcceptInvite = 105; // 应邀请求 CS_AcceptInvite
	
	/** 应邀请求结果  106**/
	private static final short CS_SUB_AcceptInviteResult = 106; // 应邀请求结果
	
	/** 约战区信息 107 **/
	private static final short CS_SUB_ChallengeInfo = 107; // 约战区信息
	
	/** 约战区状态 108**/
	private static final short CS_SUB_ChallengeAreaState = 108; // 约战区状态
	
	/** 游戏开始消息 CS_GameStart 109 **/
	private static final short CS_SUB_GAME_START = 109; // 游戏开始消息 CS_GameStart
	
	/** 用户退出约战等待 CS_ExitWait 110 **/
	private final short CS_SUB_EXIT_WAIT = 110; // 用户退出约战等待 CS_ExitWait
	
	/** 用户退出登录 CS_UserExit 111 **/
	private static final short CS_SUB_USER_EXIT = 111; // 用户退出登录 CS_UserExit
	
	/** 邀请码过期通知 112 **/
	private static final short CS_SUB_Code_Time_Out = 112;// 邀请码过期通知 CS_CodeTimeOut	
	
	/** 短信邀请内容  113**/
	private static final short CS_SUB_InviteInfo = 113; // 邀请信息内容 CS_InviteInfo
	
	/** 心跳检测 121 **/
	private static final short CS_SUB_HEART_CHECK = 121; // 心跳检测
	
	/** 与约战服务器连接状态(用于手机网关返回给手机客户端) 122 **/
	private static final short CS_SUB_CONNECT_STATE = 122; // 与约战服务器连接状态(用于手机网关返回给手机客户端)
	
	//-------------------------------------------------------------------------------------------------------
	
	//-----------------------------------------协议变量---------------------------------------------------------	
	
	/** 服务器下发短信内容，不包括用户名 **/
	private String serverShortMsgText = null;	
	/** 最大人数 **/
	private short maxpers = 0;	
	/** 当前人数 **/
	private short currentper = 0;	
	/** 剩余时间 **/
	private int residueTime = 0;	
	/** 约战邀请码超时，计时器 **/
	private Timer timer = null;	
	/** 我的邀请码long型表示法 **/
	public long myChallengeCode = 0;	
	/** 当前邀请码邀请码字符串形式 **/
	public String currentChallengeCodeText = null;
	/** 当前邀请码 **/
	private long currentChallengeCode = 0;	
	/** 当前房主ID **/
	private int currentRoomUserID = 0;
	
	//-------------------------------------------------------------------------------------------------------
		
	//-----------------------------------------UI控件-----------------------------------------------------------	
	private TextView tvBack;
	
	private TextView tvResidueTime;
	private TextView tvcinvited_num; // 当前人数
	private TextView tvInvited_code; // 邀请码
	
	private Button create_challenge; // 创建、进入约战
	private Button answer_challenge; // 应邀约战按钮

	private LinearLayout challengcreate;
	private LinearLayout challengwaiting;
	
	private Button invited_friend;  //邀请好友	
	
	private boolean isLogined;
	//-------------------------------------------------------------------------------------------------------

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.mAct=activity;
		this.mResource = mAct.getResources();
	}



	/**
	 * When creating, retrieve this instance's number from its arguments.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		registerReceiveAgreement();
	}

	
	
	/**
	 * 约战界面UI
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view=inflater.inflate(R.layout.challenge,null);
		readUserRoomID();
		
		tvBack=(TextView) view.findViewById(R.id.tvBack);
		tvBack.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				exitChallengeRoom();
			
			}
		});
		((TextView)view.findViewById(R.id.tvTitle)).setText(R.string.challenge);
		challengcreate=(LinearLayout) view.findViewById(R.id.challengcreate);
		challengwaiting=(LinearLayout) view.findViewById(R.id.challengwaiting);

		tvInvited_code=(TextView) view.findViewById(R.id.invited_code);
		tvResidueTime=(TextView) view.findViewById(R.id.invited_time);
		tvcinvited_num=(TextView) view.findViewById(R.id.invited_num);

		create_challenge = (Button) view.findViewById(R.id.create_challenge);
		// 进入约战(创建约战)
		create_challenge.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!isLogined){
					Toast.makeText(mAct, mResource.getString(R.string.challenge_loading_server), Toast.LENGTH_SHORT).show();
					return;
				}
				/*******************************************
				 * 1：首先判断户主是否已经创建邀请码 
				 * 2：户主未创建邀请码时才发送 创建邀请码协议
				 ******************************************/
				if (isEnterChallenge()) { // 是否能进入
					/*****************************
					 * 进入约战等待： 发301---105,301--106，301-----107,301----108
					 *****************************/
					sendCS_SUB_AcceptInvite(myChallengeCode);
				} else {
					// 发送创建邀请码协议，成功后则跳转到第2个面板
					sendCS_SUB_CreateCode();
				}
			}
		});

		
		answer_challenge=(Button) view.findViewById(R.id.answer_challenge);
		answer_challenge.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				UtilHelper.showCodeAlertDialog(mAct, new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						EditText codeText = (EditText) v;
						String code=codeText.getText().toString().trim();
						if (!Util.isEmptyStr(code)) {
							sendCS_SUB_AcceptInvite(getChallengeCode(code));
						} else {
							Toast.makeText(mAct, mResource.getString(R.string.challenge_input_yaoqinma), Toast.LENGTH_SHORT).show();
						}
					}
				});

			}

		});
		
		invited_friend=(Button) view.findViewById(R.id.invited_friend);
		invited_friend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cbInviteFriend();
			}

		});
		return view;

	}
	
	
	@Override
	public void onDestroy(){
		super.onDestroy();
	}
	
	@Override
	public int getFragmentTag() {
		// TODO Auto-generated method stub
		return FiexedViewHelper.CHALLENGE_ROOM;
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		exitChallengeRoom();
	}
	
	//-------------------------------------------------------------------------------------------------------
	
	//-----------------------------------------public 方法区域---------------------------------------------------	
	
	/****
	 * @Title: exitChallengeRoom
	 * @Description: 离开分区物理返回键处理
	 * 如果在约战等待界面，退出等待到约战创建界面
	 * 如果在约战创建界面，返回卡片分区
	 */
	public void exitChallengeRoom() {
		if(challengwaiting.getVisibility()==View.VISIBLE){
			challengwaiting.setVisibility(View.GONE);
			challengcreate.setVisibility(View.VISIBLE);
			sendCS_SUB_EXIT_WAIT();
		}else{	
			leaveChallenge();
			FiexedViewHelper.getInstance().skipToFragment(FiexedViewHelper.CARDZONE_VIEW);
			saveUserRoomID();
		}
	}

	
	
	/**
	 * @Title: leaveChallenge
	 * @Description: 离开约战(约战区返回按钮)
	 * @version: 2012-5-10 上午10:30:55
	 */
	public void leaveChallenge(){
		sendUserExit();  //发送301---111
	}
	
	
	
	//-------------------------------------------------------------------------------------------------------
	
	//----------------------------------------private 方法区域--------------------------------------------------	

	/**
	 * 更新按键为 创建约战 还是 进入约战
	 * @param challengeCode
	 */
	private void showChallengeBtnText(long challengeCode){
		if(create_challenge!=null){
			if(challengeCode==0){
				create_challenge.setText(R.string.creat_challenge);
			}else{
				create_challenge.setText(R.string.entry_challenge);
			}
		}
	}
	
	
	/**
	 * 保存用户约战房间ID
	 */
	private void saveUserRoomID(){
		if(currentRoomUserID!=0){
			Util.setIntSharedPreferences(mAct, ChallengeFragment.TAG, currentRoomUserID);
		}
	}
	
	
	/**
	 * 获取用户保存的约战房间ID
	 */
	private void readUserRoomID(){
		int id=Util.getIntSharedPreferences(mAct, ChallengeFragment.TAG, 0);
		if(id!=0 && currentRoomUserID==0){
			currentRoomUserID=id;
		}
	}
	
	
	/**
	 * @Title: sendUserExit
	 * @Description: 退出登录  301---111
	 * @version: 2012-9-24 下午04:48:17
	 */
	private void sendUserExit(){
		/** DWORD dwUserID; //用户ID **/
		UserInfo user = HallDataManager.getInstance().getUserMe();
		if(user == null){
			return;
		}
		TDataOutputStream tdous = new TDataOutputStream(false);
		tdous.writeInt(user.userID);
		NetSocketPak mConsumptionSock = new NetSocketPak(MDM_CHALLENGE_SYS,CS_SUB_USER_EXIT, tdous);  
		// 发送协议
		NetSocketManager.getInstance().sendData(mConsumptionSock); 
	}
	
	
	
	/**
	 * 发送邀请码字符串
	 */
	private  void cbInviteFriend(){
		String smsBody = getShortMsgText(mResource.getString(R.string.challenge_me));
        //启动发短息
     	showPhoneBook(smsBody);
    }
	
	
	  /**
  	 * @Title: getShortMsgText
  	 * @Description: 组装邀请短信内容
  	 * @param pName
  	 * @return
  	 * @version: 2012-3-16 下午05:08:08
  	 */
  	private String getShortMsgText(String pName){
  		String str = getServerMsgText(serverShortMsgText); // 服务器下发字符串
  		if(str != null && str.length() > 0){
  			return pName + str;
  		}
  		return  pName+getLocalStr();
  	}
      
  	
  	/**
  	 * 发送短信接口
  	 * @param smsBody
  	 */
  	private void showPhoneBook(String smsBody) {
		Intent intent2 = new Intent();
		intent2.setAction(Intent.ACTION_SENDTO);
		intent2.setData(Uri.parse("smsto:"));
		if (smsBody != null)
			intent2.putExtra("sms_body", smsBody);
		else
			intent2.putExtra("sms_body", "");
		mAct.startActivity(intent2);
	}
  	
  	
  	
  	/**
  	 * 获取本地组成字符串
  	 * @return
  	 */
  	private  String getLocalStr(){
  		StringBuilder sb=new StringBuilder();
  		sb.append(mResource.getString(R.string.challenge_message_1));
  		sb.append(mAct.getString(R.string.app_name));
  		sb.append(mResource.getString(R.string.challenge_message_2));
  		sb.append(currentChallengeCodeText);
  		sb.append(';');
  		sb.append(residueTime / 60000);
  		sb.append(mResource.getString(R.string.challenge_message_3) + AppConfig.DOWNLOADPATH);
  		return  sb.toString();	 
  	}
	

    
	/**
	 * @Title: getServerMsgText
	 * @Description: 获得服务器下发短信内容拼装
	 * @version: 2012-3-29 下午03:54:45
	 */
	private String getServerMsgText(final String str){
		if(str != null && str.length() > 0){
			StringBuffer buffer = new StringBuffer(str);
			// 替换%s为邀请码
			final int index1 = buffer.toString().indexOf("%s");
			if(index1 != -1){
				buffer.delete(index1, index1 + 2);
				buffer.insert(index1, currentChallengeCodeText);
			}
			// 替换第二个%s为时间
			final int index2 = buffer.toString().indexOf("%s");
			if(index2 != -1){
				buffer.delete(index2, index2 + 2);
				buffer.insert(index2, residueTime / 60000);
			}
			return buffer.toString();
		}
		return null;
	}
    
    
	/**
	 * @Title: getChallengeCode
	 * @Description: 将邀请码字符串表示形式转换为long型
	 * @param challCode
	 * @return
	 * @version: 2012-3-7 上午09:55:34
	 */
	private long getChallengeCode(final String challCode) {
		if (challCode == null) {
			return 0;
		}
		byte bytes[] = challCode.getBytes();
		long challengeCode = TDataInputStream.getLongByBytes(bytes, false);
		return challengeCode;
	}
	
	
	
	/****
	 * @Title: registerReceiveAgreement
	 * @Description: 注册接受协议
	 * @version: 2012-11-7 上午10:49:15
	 */
	private  void registerReceiveAgreement(){
		doReceiveCS_SUB_HEART_CHECK();     //收301-------121 //心跳检测
		
		doReceiveCS_SUB_CONNECT_STATE();;  //收301-------122, 
		//进入第二个面板
		doReceiveCS_SUB_ChallengeInfo();   //收：301-----107

		doReceiveCS_SUB_ChallengeAreaState();  //约战区状态(如有加入人数，则自动更新人数)  接受:301---108

		doReceiveCS_SUB_GAME_START();      //游戏开始消息（人数足够，进入）  接受301---109

		doReceiveCS_SUB_InviteInfo();      //短信邀请内容(先只接受) 。。。接受：301---113 

		doReceiveCS_SUB_Code_Time_Out();   //邀请码过期通知  接受301----112
	}
	
	
	
	
	private Handler mChallengeHandler=new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what){ 
			case HANLDER_START_GAME:       //约战下发进入游戏
				if (FiexedViewHelper.getInstance().cardZoneFragment != null) {
					if (FiexedViewHelper.getInstance().cardZoneFragment.gameRoomAssociated != null) {
						FiexedViewHelper.getInstance().cardZoneFragment.gameRoomAssociated.enterRoom(msg.arg1);
					}
				}

				//2013-1-18添加加载屏蔽UI
				FiexedViewHelper.getInstance().skipToFragment(FiexedViewHelper.LOADING_VIEW);
				if(FiexedViewHelper.getInstance().loadingFragment!=null){
					FiexedViewHelper.getInstance().loadingFragment.setLoadingType(mResource.getString(R.string.challenge_into_game),NodeDataType.NODE_TYPE_NOT_DO);
				}
				
				//进入游戏，停止计时器，防止游戏中给服务器发退出请求
				if(timer!=null){
					timer.cancel();
					timer = null;
				}
				break;
			case HANDLER_CONNECT_FAIL_BREAK:  /***进入约战区连接失败或者断开***/
				leaveChallenge();//离开约战
				Toast.makeText(mAct, (String)msg.obj, Toast.LENGTH_LONG).show();
				break;
			case HANDLER_LOGIN_FAIL:      /***登录失败**/
				isLogined=false;
				String loginErr=(String) msg.obj;
				//关闭加载进度界面
				FiexedViewHelper.getInstance().skipToFragment(FiexedViewHelper.CARDZONE_VIEW);
				if(!Util.isEmptyStr(loginErr)){
					UtilHelper.showCustomDialog(mAct,loginErr);
				}
				break;
			case HANDLER_LOGIN_SUCCESS:      /***登录结果成功**/
				showChallengeBtnText(myChallengeCode);
				isLogined=true;
				//do nothing
				break;

			case HANDLER_SUB_HEART_CHECK_RECEIVE_SEND:      /***连续的心跳检测收发**/
				sendCS_SUB_HEART_CHECK();
				break;
			case HANDLER_CREATE_CODE_ERROR:            /**创建邀请码失败**/
				FiexedViewHelper.getInstance().skipToFragment(FiexedViewHelper.CARDZONE_VIEW);
				Toast.makeText(mAct, (String)msg.obj, Toast.LENGTH_LONG).show();

				break;
			case HANDLER_CREATE_CODE_SUCCESS:        /**创建邀请码成功**/
				//设置点击的创建按钮变为进入约战按钮
				create_challenge.setText(R.string.entry_challenge);
				//create_challenge.setBackgroundResource(R.drawable.btn_goto_challenge);
				challengcreate.setVisibility(View.GONE);
				challengwaiting.setVisibility(View.VISIBLE);
				break;
			case HANLDER_CHALLENGE_INFO_SUCCESS:        /**约战区信息返回成功 **/
				challengcreate.setVisibility(View.GONE);
				challengwaiting.setVisibility(View.VISIBLE);

				ChallengeInviteInfo inviteInfo=(ChallengeInviteInfo)msg.obj;
				//启动记时器
				startTimer();
				//显示剩余时间 
				tvResidueTime.setText(UtilHelper.getTimeSecond(inviteInfo.residueTime));
				//当前人数 
				tvcinvited_num.setText(inviteInfo.currentper+mResource.getString(R.string.ddz_person));
				//显示约战邀请码
				tvInvited_code.setText(inviteInfo.currentChallengeCodeText);

				break; 
			case HANDLER_CHALLENGE_TIMER_COUNT:        /**约战时间倒计时 **/
				String time=UtilHelper.getTimeSecond(residueTime);
				//显示剩余时间 
				tvResidueTime.setText(time); 
				break;
			case HANDLER_CHALLENGE_TIMER_COUNT_END:        /**约战时间倒计时结束 **/
				if(residueTime <= 0 && timer!=null){
					timer.cancel();
					timer = null;
				}
				leaveChallenge();
				//关闭约战房间UI
				FiexedViewHelper.getInstance().skipToFragment(FiexedViewHelper.CARDZONE_VIEW);

				break; 
			case HANDLER_CHALLENGE_AREA_STATE:            /**约战区状态  接受:301---108**/
				challengcreate.setVisibility(View.GONE);
				challengwaiting.setVisibility(View.VISIBLE);
				//当前人数 
				tvcinvited_num.setText(currentper+mResource.getString(R.string.ddz_person));

				break;
			case  HANDLER_CODE_TIME_OUT:                 /**消息：邀请码过期通知  接受301----112**/
				if(msg.arg1==JUDGE_ROOM_HOLDER){  /**邀请码房主**/
					if(challengwaiting.getVisibility()==View.VISIBLE){
						challengwaiting.setVisibility(View.GONE);
						challengcreate.setVisibility(View.VISIBLE);
					}
					create_challenge.setText(R.string.creat_challenge);
					//发送退出协议
					sendCS_SUB_EXIT_WAIT();
					UtilHelper.showCustomDialog(mAct,mResource.getString(R.string.challenge_overdue_1));
				}else{
					UtilHelper.showCustomDialog(mAct,mResource.getString(R.string.challenge_overdue_2));
				}
				break;
			case  HANDLER_ACCEPT_INVITE_ERROR:             /**应邀约战请求(应邀请求结果)失败  **/
				String text=String.valueOf(msg.obj);
				if(text!=null && text.length()>0){
					UtilHelper.showCustomDialog(mAct,text);
				}else{
					UtilHelper.showCustomDialog(mAct,mResource.getString(R.string.challenge_invite_failed));
				}
				break;

			case  HANDLER_ACCEPT_INVITE_SUCCESS:          /**应邀约战请求(应邀请求结果)成功  **/
				challengcreate.setVisibility(View.GONE);
				challengwaiting.setVisibility(View.VISIBLE);
				break;

			default:
				break;
			}
		}
	};
	
	
	
	
	
	//--------------------------------------创建约战邀请码（以及接受）----------------------------------------------------------
	/**
	 * @Title: sendCS_SUB_CreateCode
	 * @Description: 创建约战邀请码（以及接受）  发送：301--103  接受：301---104
	 * @version: 2012-2-23 下午04:56:26
	 */
	private void sendCS_SUB_CreateCode(){
		UserInfo user =HallDataManager.getInstance().getUserMe();
		if(user == null){
			return;
		}
		// 构造数据包
		TDataOutputStream tdous = new TDataOutputStream(false);
		tdous.writeInt(user.userID);

		NetSocketPak mConsumptionSock = new NetSocketPak(MDM_CHALLENGE_SYS,CS_SUB_CreateCode, tdous);

		// 定义接受数据的协议
		short[][] parseProtocol = {{ MDM_CHALLENGE_SYS, CS_SUB_CreateCodeResult } };
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				// 解析接受到的网络数据
				TDataInputStream tdis = netSocketPak.getDataInputStream(); 
				tdis.setFront(false);

				final byte result = tdis.readByte();// 创建结果 0：成功 非0：失败
				myChallengeCode = tdis.readLong(); // 约战邀请码
				Log.v(TAG, "创建邀请码:"+myChallengeCode);
				String text = tdis.readUTFByte();// 信息内容

				if(result != 0){// 邀请码创建失败 
					mChallengeHandler.sendMessage(mChallengeHandler.obtainMessage(HANDLER_CREATE_CODE_ERROR, text));
				}else{
					Message msg=mChallengeHandler.obtainMessage(HANDLER_CREATE_CODE_SUCCESS);
					mChallengeHandler.sendMessage(msg);
				}
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
	//--------------------------------------------------------------------------------------------------------------
	
	
	
	
	
	/**
	 * 约战心跳检查
	 */
	private void sendCS_SUB_HEART_CHECK(){
		TDataOutputStream tdous = new TDataOutputStream();
		NetSocketPak mConsumptionSock = new NetSocketPak(MDM_CHALLENGE_SYS,CS_SUB_HEART_CHECK, tdous);
		// 发送协议
		NetSocketManager.getInstance().sendData(mConsumptionSock);
		// 清理协议对象
		mConsumptionSock.free();
	}
	
	
	
	
	
	
	
	
	
	
	



	
	/***
	 * @Title: startTimer
	 * @Description: 启动计时器
	 * @version: 2012-9-27 下午05:21:48
	 */
	private void startTimer(){
		if(timer != null){
			timer.cancel();
			timer = null;
		}
		timer=new Timer(true);
		timer.schedule(new TimerTask(){
			@Override
			public void run() {
				if(residueTime>0){
					residueTime-=1000;
					mChallengeHandler.sendEmptyMessage(HANDLER_CHALLENGE_TIMER_COUNT); 
				}else{ 
					mChallengeHandler.sendEmptyMessage(HANDLER_CHALLENGE_TIMER_COUNT_END);
				}
			}
		},0, 1000);  //每隔1秒触发一次

	}
	
	
	
	
	/**
	 * @Title: sendCS_SUB_AcceptInvite
	 * @Description:应邀约战请求(应邀请求结果  ) 发送301---105 接受 301---106
	 * @version: 2012-3-2 下午05:36:15
	 */
	private void sendCS_SUB_AcceptInvite(final long code){
		UserInfo user = HallDataManager.getInstance().getUserMe();
		if(user == null){
			return;
		}
		// 构造数据包
		TDataOutputStream tdous = new TDataOutputStream(false);
		tdous.writeInt(user.userID);
		tdous.writeLong(code);
		NetSocketPak mConsumptionSock = new NetSocketPak(MDM_CHALLENGE_SYS,CS_SUB_AcceptInvite, tdous);
		// 定义接受数据的协议
		short[][] parseProtocol = {{ MDM_CHALLENGE_SYS, CS_SUB_AcceptInviteResult } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				// 解析接受到的网络数据
				TDataInputStream tdis = netSocketPak.getDataInputStream(); 
				tdis.setFront(false);
				final byte result = tdis.readByte(); //应邀结果 0：成功 非0：失败
				final String text = tdis.readUTFByte();// //信息内容
				Message msg=null;
				if(result != 0){ // 应邀失败
					msg=mChallengeHandler.obtainMessage(HANDLER_ACCEPT_INVITE_ERROR, text);
					mChallengeHandler.sendMessage(msg);
				}else{
					//myChallengeCode=code;
					mChallengeHandler.sendEmptyMessage(HANDLER_ACCEPT_INVITE_SUCCESS);
				}
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

	
	
	
	
	/**
	 * @Title: isEnterChallenge
	 * @Description: 是否进入约战(用户是当前房主，且邀请码不为0)
	 * @version: 2012-9-24 下午04:48:17
	 */
	private boolean isEnterChallenge(){
		boolean res=false;
		UserInfo user = HallDataManager.getInstance().getUserMe();
		if(user.userID == currentRoomUserID  &&  myChallengeCode != 0){
			res=true;	
		}

		return res;
	}
	
	

	
	
	/**
	 * @Title: sendUserLogin 
	 * @Description: 用户登录报名服务器 发：301---101,收301--102
	 * @version: 2012-2-23 下午04:25:36
	 */
	public void sendUserLogin(long dataID){
		TDataOutputStream tdous = new TDataOutputStream(false);
		tdous.writeLong(dataID);
		NetSocketPak mConsumptionSock = new NetSocketPak(MDM_CHALLENGE_SYS,CS_SUB_UserLogon, tdous);  
		// 定义接受数据的协议
		short[][] parseProtocol = { { MDM_CHALLENGE_SYS, CS_SUB_LogonResult } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				// 解析接受到的网络数据
				TDataInputStream tdis = netSocketPak.getDataInputStream(); 
				tdis.setFront(false);

				final byte result = tdis.readByte();  // 登录结果 0：成功 非0：失败
				myChallengeCode = tdis.readLong();    //约战邀请码(未创建邀请码的此值为0,（用于判断是否进入约战而不再请求创建邀请码）
				Byte len=tdis.readByte();
				String text=tdis.readUTF(len);
				//String text = tdis.readUTFByte();     //信息内容 
				
				if(result == 0){
					mChallengeHandler.sendEmptyMessage(HANDLER_LOGIN_SUCCESS);
				}else if(result == 1){      //登录失败就返回
					mChallengeHandler.sendEmptyMessage(HANDLER_LOGIN_FAIL);
				}else{
					Message msg=mChallengeHandler.obtainMessage(HANDLER_LOGIN_FAIL, text);
					mChallengeHandler.sendMessageDelayed(msg, 1000);//延时1秒发送 刷新UI 
				} 
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

	
	




	
	//----------------------------------------------心跳检测，先收，后发301---121----------------------------------------------------

		/**
		 * @Title: doReceiveCS_SUB_HEART_CHECK
		 * @Description: 心跳检测  121 收：301---121，然后发送
		 * @return
		 * @version: 2012-3-2 下午04:30:04
		 */
		private void doReceiveCS_SUB_HEART_CHECK(){  
			// 定义接受数据的协议
			short[][] parseProtocol = { { MDM_CHALLENGE_SYS, CS_SUB_HEART_CHECK } };
			// 创建协议解析器
			NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
				@Override
				public boolean doReceive(NetSocketPak netSocketPak) {
					// 解析接受到的网络数据
					TDataInputStream tdis = netSocketPak.getDataInputStream();  
					mChallengeHandler.sendEmptyMessage(HANDLER_SUB_HEART_CHECK_RECEIVE_SEND);

					return true;
				}
			};
			// 注册协议解析器到网络数据分发器中
			NetSocketManager.getInstance().addPrivateListener(nPListener); 
			nPListener.setOnlyRun(false);    //表示一直运行
		}

	
	
	
		
		
		//----------------------------------------------------------------------------------------------------------------
		/**
		 * @Title: doReceiveCS_SUB_CONNECT_STATE
		 * @Description: 连接状态 301----122
		 * @return
		 * @version: 2012-3-5 下午04:10:24
		 */
		private void doReceiveCS_SUB_CONNECT_STATE(){
			// 定义接受数据的协议
			short[][] parseProtocol = { { MDM_CHALLENGE_SYS, CS_SUB_CONNECT_STATE } };

			// 创建协议解析器
			NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
				@Override
				public boolean doReceive(NetSocketPak netSocketPak) {
					// 解析接受到的网络数据
					TDataInputStream tdis = netSocketPak.getDataInputStream(); 
					/** BYTE byState; //连接状态 0：连接成功 -------1：连接失败 2：连接断开 **/
					final byte state = tdis.readByte();
					if(state == 1 || state == 2){
						String text = "";
						if(state == 1){
							text = mResource.getString(R.string.challenge_net_busy);
						}else{
							text = mResource.getString(R.string.challenge_net_error);
						}
						Message msg=mChallengeHandler.obtainMessage(HANDLER_CONNECT_FAIL_BREAK, text);
						mChallengeHandler.sendMessage(msg);
					}
					return true;
				}
			};
			// 注册协议解析器到网络数据分发器中
			NetSocketManager.getInstance().addPrivateListener(nPListener); 
			nPListener.setOnlyRun(false);   //表示一直运行
		}
		
		
		
		/**
		 * @Title: doReceiveCS_SUB_ChallengeInfo
		 * @Description: 约战区信息返回 301----107
		 * @param command
		 * @return
		 * @version: 2012-3-2 上午11:18:13
		 */
		private void doReceiveCS_SUB_ChallengeInfo(){
			// 定义接受数据的协议
			short[][] parseProtocol = { { MDM_CHALLENGE_SYS, CS_SUB_ChallengeInfo } };
			// 创建协议解析器
			NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
				@Override
				public boolean doReceive(NetSocketPak netSocketPak) {
					// 解析接受到的网络数据
					TDataInputStream tdis = netSocketPak.getDataInputStream(); 
					tdis.setFront(false);
					
					ChallengeInviteInfo inviteInfo=new ChallengeInviteInfo(tdis);

					currentRoomUserID= inviteInfo.currentRoomUserID;//房主用户ID
					currentChallengeCodeText = inviteInfo.currentChallengeCodeText;// 约战邀请码
					currentChallengeCode=inviteInfo.currentChallengeCode;

					maxpers= inviteInfo.maxpers;// 要求总人数
					currentper = inviteInfo.currentper;// 当前人数
					residueTime=inviteInfo.residueTime; // 邀请码剩余有效时间（需要自己倒计时）

					mChallengeHandler.sendMessage(mChallengeHandler.obtainMessage(HANLDER_CHALLENGE_INFO_SUCCESS, inviteInfo));

					return true;
				}
			};
			// 注册协议解析器到网络数据分发器中
			NetSocketManager.getInstance().addPrivateListener(nPListener);
			nPListener.setOnlyRun(false);
		}

		
		
		
		
		
		/**
		 * @Title: doReceiveCS_SUB_ChallengeAreaState
		 * @Description: 约战区状态  接受:301---108
		 * @param command
		 * @return
		 * @version: 2012-3-5 上午10:53:05
		 */
		private void doReceiveCS_SUB_ChallengeAreaState(){
			/** WORD dwUserCount; //当前进入人数 **/ 
			// 定义接受数据的协议
			short[][] parseProtocol = { { MDM_CHALLENGE_SYS, CS_SUB_ChallengeAreaState } };
			// 创建协议解析器
			NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
				@Override
				public boolean doReceive(NetSocketPak netSocketPak) {
					// 解析接受到的网络数据
					TDataInputStream tdis = netSocketPak.getDataInputStream(); 
					currentper = tdis.readShort();// 当前进入人数  
					mChallengeHandler.sendEmptyMessage(HANDLER_CHALLENGE_AREA_STATE);
					return true;
				}
			};
			// 注册协议解析器到网络数据分发器中
			NetSocketManager.getInstance().addPrivateListener(nPListener);
		}
		
		
		
		
		
		/**
		 * @Title: doReceiveCS_SUB_GAME_START
		 * @Description: 游戏开始消息  301---109
		 * @return
		 * @version: 2012-3-5 上午10:49:46
		 */
		private void doReceiveCS_SUB_GAME_START(){
			// 定义接受数据的协议
			short[][] parseProtocol = { { MDM_CHALLENGE_SYS, CS_SUB_GAME_START } };
			// 创建协议解析器
			NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
				@Override
				public boolean doReceive(NetSocketPak netSocketPak) {
					// 解析接受到的网络数据
					TDataInputStream tdis = netSocketPak.getDataInputStream(); 
					tdis.setFront(false);

					final int roomID = tdis.readInt(); //房间ID
					tdis.readInt();                    // IP地址(网络IP)
					tdis.readShort();                  //端口

					//此处创建房间，并进入游戏
					RoomData room = new RoomData();
					room.RoomID = roomID;
					room.GameType = RoomData.GAME_GENRE_DONGGUAN_MATCH;
					HallDataManager.getInstance().setCurrentRoomData(room);

					//请求进入房间 
					//mChallengeHandler.sendEmptyMessage(HANLDER_START_GAME); 
					mChallengeHandler.obtainMessage(HANLDER_START_GAME, room.RoomID, room.GameType, room).sendToTarget();

					return true;
				}
			};
			// 注册协议解析器到网络数据分发器中
			NetSocketManager.getInstance().addPrivateListener(nPListener);
			nPListener.setOnlyRun(false);
		}


		
		/**
		 * @Title: doReceiveCS_SUB_InviteInfo
		 * @Description: 短信邀请内容 。。。接受：301---113
		 * @return
		 * @version: 2012-3-16 下午05:04:39
		 */
		private void doReceiveCS_SUB_InviteInfo(){
			// 定义接受数据的协议
			short[][] parseProtocol = {{ MDM_CHALLENGE_SYS, CS_SUB_InviteInfo } };
			// 创建协议解析器
			NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
				@Override
				public boolean doReceive(NetSocketPak netSocketPak) {

					// 解析接受到的网络数据
					TDataInputStream tdis = netSocketPak.getDataInputStream(); 
					serverShortMsgText = tdis.readUTFShort(); // 短信内容

					return true;
				}
			};
			// 注册协议解析器到网络数据分发器中
			NetSocketManager.getInstance().addPrivateListener(nPListener);
			nPListener.setOnlyRun(false);
		}


		
		
		/**
		 * @Title: doReceiveCS_SUB_Code_Time_Out
		 * @Description: 邀请码过期通知  接受301----112
		 * @param command
		 * @return
		 * @version: 2012-3-9 下午05:24:13
		 */
		private void doReceiveCS_SUB_Code_Time_Out(){
			// 定义接受数据的协议
			short[][] parseProtocol = {{ MDM_CHALLENGE_SYS, CS_SUB_Code_Time_Out } };
			// 创建协议解析器
			NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
				@Override
				public boolean doReceive(NetSocketPak netSocketPak) {
					// 解析接受到的网络数据
					TDataInputStream tdis = netSocketPak.getDataInputStream(); 
					tdis.setFront(false);


					final int dwRoomHolderID = tdis.readInt(); // 房主用户ID
					final long code = tdis.readLong(); // 约战邀请码
					String text = tdis.readUTFByte(); // 信息内容

					UserInfo user = HallDataManager.getInstance().getUserMe();

					if(user.userID == dwRoomHolderID){ // 我是过期邀请码房主
						if(currentChallengeCode <= 0
								|| (code == currentChallengeCode && code == myChallengeCode)){ // 当前约战邀请码有效，标识不在约战等待中
							//转回创建约战界面
							Message msg=mChallengeHandler.obtainMessage(HANDLER_CODE_TIME_OUT);
							msg.arg1=JUDGE_ROOM_HOLDER;
							mChallengeHandler.sendMessage(msg);
						}
						myChallengeCode = 0;
					}else{// 我不是过期邀请码房主
						myChallengeCode = 0;
						if(code == currentChallengeCode){ // 当前邀请码过期
							// 转回创建约战界面
							Message msg=mChallengeHandler.obtainMessage(HANDLER_CODE_TIME_OUT);
							msg.arg1=JUDGE_ROOM_NOT_HOLDER;
							mChallengeHandler.sendMessage(msg);
						}
					}

					return true;
				}
			};
			// 注册协议解析器到网络数据分发器中
			NetSocketManager.getInstance().addPrivateListener(nPListener); 
			nPListener.setOnlyRun(false);
		}
		
		
		
		/**
		 * @Title: sendCS_SUB_EXIT_WAIT
		 * @Description: 退出约战等待 301---110
		 * @version: 2012-3-5 上午11:02:35
		 */
		public  void sendCS_SUB_EXIT_WAIT(){
			UserInfo user = HallDataManager.getInstance().getUserMe();
			if(user == null){
				return;
			}
			// 构造数据包
			TDataOutputStream tdous = new TDataOutputStream(false);
			tdous.writeInt(user.userID);
			tdous.writeLong(currentChallengeCode);
			NetSocketPak mConsumptionSock = new NetSocketPak(MDM_CHALLENGE_SYS,CS_SUB_EXIT_WAIT, tdous);
			
			// 发送协议
			NetSocketManager.getInstance().sendData(mConsumptionSock);
			// 清理协议对象
			mConsumptionSock.free(); 
		}


	
		
}
