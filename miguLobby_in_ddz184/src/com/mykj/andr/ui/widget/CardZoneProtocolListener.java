package com.mykj.andr.ui.widget;

import java.util.List;

import org.cocos2dx.util.GameUtilJni;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;

import com.mingyou.distributor.NetPrivateListener;
import com.mingyou.distributor.NetSocketPak;
import com.mingyou.login.RecoverForDisconnect;
import com.mykj.andr.model.BackPackItem;
import com.mykj.andr.model.HallDataManager;
import com.mykj.andr.model.NewUIDataStruct;
import com.mykj.andr.model.NodeData;
import com.mykj.andr.model.AllNodeData;
import com.mykj.andr.model.RankOrderInfo;
import com.mykj.andr.model.RoomData;
import com.mykj.andr.model.SavedMessage;
import com.mykj.andr.model.SystemMessage;
import com.mykj.andr.model.UserInfo;
import com.mykj.andr.net.NetSocketManager;
import com.mykj.andr.provider.BackPackItemProvider;
import com.mykj.andr.provider.NewCardZoneProvider;
import com.mykj.andr.provider.RankOrderProvider;
import com.mykj.andr.task.GameTask;
import com.mykj.andr.ui.CustomDialog;
import com.mykj.andr.ui.MMVideoBuyDialog;
import com.mykj.andr.ui.RankOrderDialog;
import com.mykj.andr.ui.fragment.LoadingFragment.NodeDataType;
import com.mykj.andr.ui.widget.Interface.CardZoneOnClickListener;
import com.mykj.andr.ui.widget.Interface.GameRoomAssociatedInterface;
import com.mykj.comm.io.TDataInputStream;
import com.mykj.comm.io.TDataOutputStream;
import com.mykj.comm.log.MLog;
import com.mykj.game.FiexedViewHelper;
import com.MyGame.Midlet.R;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.CenterUrlHelper;
import com.mykj.game.utils.CommonBeanHelper;
import com.mykj.game.utils.Log;
import com.mykj.game.utils.Toast;
import com.mykj.game.utils.Util;
import com.mykj.game.utils.UtilHelper;

public class CardZoneProtocolListener implements CardZoneOnClickListener {

	private static final String TAG = "CardZoneProtocolListener";

	private Activity mAct;
	
	private Resources mResource;

	private static CardZoneProtocolListener instance = null;

	/** 我的物品 */
	//private BackPackItem[] backPackItems;

	/** 当前数 **/
	private int curBackpackNum=0;

	/** 背包是否读取完毕 */
	//boolean recordBackpackType = false;

//	/** 记录背包中拥有的各个道具数量，合成材料展示需要用 */
//	@SuppressLint("UseSparseArrays")
//	public static Map<Integer, Integer> idHandleCountMap = new HashMap<Integer, Integer>();

	// -------------------------框架消息协议-----------------------------------------------

	private static final short MDM_GF_FRAME = 101; // 框架消息

	private static final short SUB_GF_MESSAGE = 300;// 系统消息 （游戏服务器推送）

	// ----------------------系统消息------------------------------------------------
	private static final short MDM_SYSTEM = 10; // 系统消息(主协议)

	// private static final short MSUB_GR_MESSAGE = 100; //系统消息下发(子协议) //未使用

	private static final short MDM_GR_COMMON_MESSAGE = 200; // 通用消息子协议

	// ---------------------房间出错协议-----------------------------------------------
	private static final short MDM_SERVER_ERROR = 18; // 出错主协议

	private static final short MSUB_ROOM_DISCONNECT = 0; // 服务器返回与房间断开断开连接(子协议)

	// ---------------------房间协议 -----------------------------------------
	private static final short MDM_ROOM = 14; // 房间主协议

	private static final short MSUB_CMD_ENTER_ROOM_SUCCEEDED = 0; // 成功：返回

	private static final short MSUB_CMD_ENTER_ROOM_FAILED = 1; // 失败：返回

	// private static final short MSUB_CMD_ENTER_ROOM_REQ = 2; //请求进入房间 //未使用

	// ---------------------------道具协议----------------------------------------------------
	/** 道具主协议 */
	private static final short MDM_PROP = 17;

	/** 子协议 */
	private static final short MSUB_CMD_PACK_PROP_LIST_REQ_EX = 795;

	/** 返回 失败/成功：子协议 **/
	private static final short MSUB_CMD_PACK_PROP_LIST_RESP = 763;

	// ---------------------handler what----------------------

	// private static final int HANDLER_DISCONNECT_GAME = 1; //
	// 服务器与房间断开链接18-00，在游戏中时处理

	private static final int HANDLER_DISCONNECT = 2; // 服务器与房间断开链接18-0

	private static final int HANDLER_ARCHITECTURE = 3; // 101-300协议

	private static final int HANDLER_COMMOM_MESSAGE = 4; // 通用消息文本下发

	private static final int HANDLER_TYPE_STANDARD_BUY = 5; // 道具购买弹出提示---(不单单是自由区，还有赚话费等区)

	private static final int HANDLER_TYPE_STANDARD_POP = 6; // 通用消息

	private static final int HANDLER_ROOM_SUCCESS = 7; // 进入房间成功（用户在房间状态）

	private static final int HANDLER_ROOM_NODES_FAIL = 8; // 进入房间失败

	private static final int HANDLER_SHOW_RANKING_SUCCESS=9;
	
	private static final int HANDLER_SHOW_RANKING_FAIL=10;
	
	private static final int HANDLER_TYPE_COMMOM_POP = 11; //  破产送文本下发
	
	private static final int HANDLER_OPEN_URL=12;  //打开指定URL
	
	private static final int HANDLER_QUICK_BUY_MSG=13;  //游戏快捷购买提示
	
	private static final int HANDLER_WAIT_TIMEOUT=14;  //比赛场组桌匹配超时
	private static final int HANDLER_VEDIO_NEED_VIP = 15;   //弹出购买钻石会员
	
	/** 获取我的物品列表成功消息到Handler */
	public static final int HANDLER_PACK_QUERY_SUCCESS = 7630;

	/** 我的物品列表没有数据 **/
	public static final int HANDLER_PACK_QUERY_SUCCESS_NODATA = 76300;


	
	// ----------------------------------请求背包协议----------------------------------------------
//	private static final short SMT_INFO = 0x0001; // 信息消息
	
//	private static final short SMT_EJECT = 0x0002; // 弹出消息
//
//	private static final short SMT_GLOBAL = 0x0004; // 全局消息

	private static final short SMT_CLOSE_ROOM = 0x1000; // 关闭房间

	private static final short SMT_INTERMIT_LINE = 0x4000; // 中断连接

// private static final short SMT_CLOSE_GAME = 0x1000; //关闭游戏

	/**
	 * 私有构造函数
	 * 
	 * @param act
	 */
	private CardZoneProtocolListener(Activity act) {
		this.mAct = act;
		this.mResource= mAct.getResources(); 
	}

	/**
	 * 获取单例
	 * 
	 * @param act
	 * @return
	 */
	public static CardZoneProtocolListener getInstance(Activity act) {
		if (instance == null) {
			instance = new CardZoneProtocolListener(act);
		}
		return instance;
	}

	/**
	 * 协议初始化
	 */
	public void protocolInit() {

		// (监听服务下发消息，在进入房间后主动下发)
		getSystemMessage(); // ** V1.2.5 房间内部消息滚动   // 针对游戏内部的消息，分区不使用

		// (监听服务下发消息，在进入房间后主动下发: 收发服务端发送用户当前任务信息)
		GameTask.getInstance().receiveListener(); // 注册房间任务监听

		// 接受10---200协议
		doReceiveSystemCOMMON_MESSAGE(); // 注册通用系统消息的协议

		// 接受14---0,1
		receiveAutoRoomInfo();// 不再调用进入房间协议，直接速配成功服务端自动下发

		receiveRankOrder();

		roomDisConnect();
	}

	/**
	 * 协议处理handler
	 */
	@SuppressLint("HandlerLeak")
	public Handler mProtocolHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLER_DISCONNECT: // 服务器与房间断开链接18-0
				String popMessage = (String) msg.obj;
				short msgType=(short) msg.arg1;
				if ((msgType & SMT_CLOSE_ROOM) == SMT_CLOSE_ROOM) {
					if (FiexedViewHelper.getInstance().getCurFragment() != FiexedViewHelper.COCOS2DX_VIEW) {
						if (FiexedViewHelper.getInstance().getCurFragment() == FiexedViewHelper.LOADING_VIEW) {
							if (FiexedViewHelper.getInstance().loadingFragment != null) {
								FiexedViewHelper.getInstance().loadingFragment.cancelLoading();
							}
						} 
						FiexedViewHelper.getInstance().skipToFragment(FiexedViewHelper.CARDZONE_VIEW);
						
						if (!Util.isEmptyStr(popMessage)) {
							UtilHelper.showCustomDialog(mAct, popMessage);
						}
					} 

				}

				break;
			case HANDLER_ARCHITECTURE: // 101-300协议
				String info = (String) msg.obj;
				short messageType=(short) msg.arg1;

				if ((messageType & SMT_CLOSE_ROOM) == SMT_CLOSE_ROOM || (messageType & SMT_INTERMIT_LINE) == SMT_INTERMIT_LINE) {
					if (FiexedViewHelper.getInstance().getCurFragment() == FiexedViewHelper.COCOS2DX_VIEW) {
						// 与房间断开连接时主动将用用户起立，避免返回分区时发送起立请求引起嵌套死循环
						UserInfo userMe = HallDataManager.getInstance().getUserMe();
						if (userMe != null) {
							userMe.userStatus = UserInfo.USER_FREE;
						}
						// 返回分区列表
						GameUtilJni.onZoneEvent(GameUtilJni.EventZone_BackToCardZone);
						if (!Util.isEmptyStr(info)) {
							UtilHelper.showCustomDialog(mAct, info);
						}
					} 

				}
				break;
			case HANDLER_COMMOM_MESSAGE: // 通用消息文本下发
				SystemMessage baseMsg = (SystemMessage) msg.obj;
				if (baseMsg != null) {
					if (baseMsg.getCommonMessage() != null) {
						UtilHelper.showCustomDialog(mAct, baseMsg.getCommonMessage());
					}
				}

				break;

			case HANDLER_TYPE_STANDARD_BUY: // 道具购买弹出提示---(不单单是自由区，还有赚话费等区)
				SystemMessage baseMessage = (SystemMessage) msg.obj;
				int propId=baseMessage.propId;
				String propMessage=baseMessage.text;
				String ensureBtnStr = null;
				String cancelBtnStr = null;
				if(baseMessage.lBtn != null){
					ensureBtnStr = baseMessage.lBtn;
				}else if(baseMessage.mBtn != null){
					ensureBtnStr = baseMessage.mBtn;
				} 
				if(baseMessage.rBtn != null){
					cancelBtnStr = baseMessage.rBtn;
				}
				if (FiexedViewHelper.getInstance().getCurFragment() == FiexedViewHelper.COCOS2DX_VIEW) {
	
					if((baseMessage.event & 0x0002) != 0){   //event & 2 != 0 表示要关闭房间，交由游戏弹出快捷购买
						SavedMessage savedMsg=new SavedMessage();
						savedMsg.mPropId=propId;
						savedMsg.mPropMessage=propMessage;
						savedMsg.ensureBtnStr = ensureBtnStr;
						savedMsg.cancelBtnStr = cancelBtnStr;
						FiexedViewHelper.setSavedMessage(savedMsg);
						GameUtilJni.onZoneEvent(GameUtilJni.EventZone_ShortcutBuy);   
					}else{   //不关闭房间，由分区弹快捷购买
						if(FiexedViewHelper.getInstance().cardZoneFragment != null){
							FiexedViewHelper.getInstance().cardZoneFragment.showQuickBuyDialog(propId, propMessage, ensureBtnStr, cancelBtnStr);
						}
					}
				} else {
					if (FiexedViewHelper.getInstance().getCurFragment() == FiexedViewHelper.LOADING_VIEW) {
						if(FiexedViewHelper.getInstance().loadingFragment!=null){
							FiexedViewHelper.getInstance().loadingFragment.cancelLoading();
						}
					}
					FiexedViewHelper.getInstance().skipToFragment(FiexedViewHelper.CARDZONE_VIEW);
					FiexedViewHelper.getInstance().cardZoneFragment.showQuickBuyDialog(propId,propMessage, ensureBtnStr, cancelBtnStr);
	
				}
				break;
			case HANDLER_TYPE_STANDARD_POP: // 通用消息
			case HANDLER_TYPE_COMMOM_POP:
				SystemMessage sysMsg = (SystemMessage) msg.obj;
				String content = sysMsg.text;
				final int event=msg.arg1;

				if (!Util.isEmptyStr(content)) {
					UtilHelper.showCustomDialog(mAct, content,
							new OnClickListener() {
						@Override
						public void onClick(View v) {
							if ((event & SystemMessage.TYPE_EVENT_CLOSE_HALL) == SystemMessage.TYPE_EVENT_CLOSE_HALL) {
								FiexedViewHelper.getInstance().goToReLoginView();
								return;
							}
							if ((event & SystemMessage.TYPE_EVENT_CLOSE_ROOM) == SystemMessage.TYPE_EVENT_CLOSE_ROOM
									||(event & SystemMessage.TYPE_EVENT_CLOSE_GAME) == SystemMessage.TYPE_EVENT_CLOSE_GAME) {
								GameUtilJni.onZoneEvent(GameUtilJni.EventZone_BackToCardZone);
							}
							if ((event & SystemMessage.TYPE_EVENT_QUICK_PLAY) == SystemMessage.TYPE_EVENT_QUICK_PLAY) {
								if (FiexedViewHelper.getInstance().getCurFragment() == FiexedViewHelper.COCOS2DX_VIEW) {
									SavedMessage savedMsg=new SavedMessage();
									savedMsg.mIsQuickGame=true;
									FiexedViewHelper.setSavedMessage(savedMsg);
									GameUtilJni.onZoneEvent(GameUtilJni.EventZone_QuickGame);
								}
							}
						}
					});
				}
				break;
			case HANDLER_WAIT_TIMEOUT:{
				SystemMessage sysTimeOut = (SystemMessage) msg.obj;
				String title = sysTimeOut.text;
				short nodeId=sysTimeOut.nodeId;
				final NodeData node=AllNodeData.getInstance(mAct).findNodeDataById(nodeId);
				/** 右按钮 */
				String rBtn=sysTimeOut.rBtn;
				/** 左按钮 */
				String lBtn=sysTimeOut.lBtn;
				
 				if (!Util.isEmptyStr(title)) {
					CustomDialog dialog=new CustomDialog(mAct,title,lBtn,rBtn);
					dialog.setConfirmCallBack(new OnClickListener() {
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							//FiexedViewHelper.getInstance().skipToFragment(FiexedViewHelper.CARDZONE_VIEW);
							FiexedViewHelper.getInstance().quickGame();  //换桌
						}
					});
					dialog.setCancelCallBack(new OnClickListener() {
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							FiexedViewHelper.getInstance().quickMatcheRoom(node);//换场
						}
					});
					dialog.show();
				}
			}
				break;
			case HANDLER_OPEN_URL:
				SystemMessage sysMessage = (SystemMessage) msg.obj;
				String msgStr = sysMessage.text;
				final short  mUrlID = sysMessage.mUrlID;
				final short  rUrlID = sysMessage.rUrlID;
				final short  lUrlID = sysMessage.lUrlID;
				Builder alertDialog = new AlertDialog.Builder(mAct);
				alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
				alertDialog.setTitle("系统提示!");
				if(!Util.isEmptyStr(msgStr)){
					alertDialog.setMessage(msgStr);
				}
				if(mUrlID!=0){
					alertDialog.setNeutralButton(sysMessage.mBtn,new DialogInterface.OnClickListener(){
						@Override
						 public void onClick(DialogInterface dialog, int whichButton) {
							UtilHelper.onWeb(mAct, CenterUrlHelper.getWapUrl(mUrlID));
		                  }
					});
				}
				if(rUrlID!=0){
					alertDialog.setPositiveButton(sysMessage.mBtn,new DialogInterface.OnClickListener(){
						@Override
						 public void onClick(DialogInterface dialog, int whichButton) {
							UtilHelper.onWeb(mAct, CenterUrlHelper.getWapUrl(rUrlID));
		                  }
					});
				}
				if(lUrlID!=0){
					alertDialog.setNegativeButton(sysMessage.mBtn,new DialogInterface.OnClickListener(){
						@Override
						 public void onClick(DialogInterface dialog, int whichButton) {
							UtilHelper.onWeb(mAct, CenterUrlHelper.getWapUrl(lUrlID));
		                  }
					});
				}
				alertDialog.show();
				break;
			case HANDLER_QUICK_BUY_MSG:
				String strText = (String)msg.obj;
				if(!Util.isEmptyStr(strText)){
					//Toast.makeText(mAct, strText, Toast.LENGTH_LONG).show();
					if(FiexedViewHelper.getInstance().getCurFragment() == FiexedViewHelper.COCOS2DX_VIEW){
						GameUtilJni.onQuickBuyMsg(strText);
					}
				}
				break;
			case HANDLER_ROOM_SUCCESS: // 进入房间成功（用户在房间状态）
				// jason2013.03.18进入房间成功需要根据用户状态判断是否立即转入游戏界面
				final UserInfo userInfo = HallDataManager.getInstance().getUserMe();
				final int result = userInfo.userStatus;
				MLog.e(TAG,"---收到进入房间成功---uesrID ="+userInfo.userID+", 状态："+result+", 是否断线重连？"+RecoverForDisconnect.isSendBeginReconnect);
				if (RecoverForDisconnect.isSendBeginReconnect) { // 正在断线重连
					RecoverForDisconnect.isSendBeginReconnect = false;
					if (result >= UserInfo.USER_SET) { // jason此处大于坐下状态都需要立即转入游戏界面
						Log.e(TAG, "分区通知给游戏中断线重回成功，游戏会发送场景请求");
						GameUtilJni.onZoneEvent(GameUtilJni.EventZone_ReconnectSucc);
					} else {//可以优化为再请求坐下
						// 提示游戏已结束
						UtilHelper.showCustomDialog(mAct, mResource.getString(R.string.ddz_game_over), new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								GameUtilJni.onZoneEvent(GameUtilJni.EventZone_BackToCardZone); // 通知游戏返回分区
							}
						});
					}
				} else {
					// 进入房间显示,快速游戏不进入房间显示
					NodeData nodeData = HallDataManager.getInstance().getCurrentNodeData();
					if (nodeData != null) {
						if (NodeData.NODE_NORMAL == nodeData.Type
								||NodeData.NODE_MM_VIDEO == nodeData.Type) { // 普通节点101
							intoGameRoomNormalNodeSuccess(msg, nodeData, 0);
						} else if ((NodeData.NODE_ENROLL == nodeData.Type) || (NodeData.NODE_CHALLENGE == nodeData.Type)) { // 报名节点(赚话费专区),约战节点
							intoGameRoomHCOtherNodeSuccess(nodeData, 0);
						}
					}
				}
				break;
			case HANDLER_ROOM_NODES_FAIL: // 进入房间失败
				intoGameRoomFail(msg, 0);
				break;
			case GameRoomAssociatedInterface.HANDLER_MSUB_QUICK_PLAY_ROOMS_DATA_FAIL: // 速配失败
				Toast.makeText(mAct, mResource.getString(R.string.ddz_adapte_failed), Toast.LENGTH_LONG).show();
				break;
			case GameRoomAssociatedInterface.HANDLER_MSUB_QUICK_PLAY_ROOMS_DATA_SUCCESS: // 速配房间成功:自由战区:速配--->接收进入房间成功失败--->坐下
				// 2:然后进入房间详细UI
				// 获得节点以及房间信息
				//NodeData node = (NodeData) msg.obj;   //未使用
				Bundle data = msg.getData();
				RoomData roomData = (RoomData) data.getSerializable(GameRoomAssociatedWidget.TAG);
				HallDataManager.getInstance().setCurrentRoomData(roomData);

//				if(!HallDataManager.getInstance().getInvokeLogic()){
//					FiexedViewHelper.getInstance().skipToFragment(FiexedViewHelper.FREE_ROOM);
//					if (FiexedViewHelper.getInstance().freeRoomInfoFragment != null) {
//						FiexedViewHelper.getInstance().freeRoomInfoFragment.setRoomInfo(node.BD1Content);
//						FiexedViewHelper.getInstance().freeRoomInfoFragment.setNodeId(node.ID);
//					}
//				}
				break;

			case HANDLER_SHOW_RANKING_SUCCESS:
				final RankOrderDialog dlg = new RankOrderDialog(AppConfig.mContext, RankOrderProvider.getInstance().getList());
				dlg.show();
				if(dlg.isShowing()){
					dlg.setOnDismissListener(new OnDismissListener(){
						@Override
						public void onDismiss(DialogInterface dialog) {
							RankOrderProvider.getInstance().setFinishStatus(false);
						}

					});
				}
				break;
			case HANDLER_SHOW_RANKING_FAIL:
				break;
			case HANDLER_VEDIO_NEED_VIP:
			{
				SystemMessage sysTimeOut = (SystemMessage) msg.obj;
				String title = sysTimeOut.text;
				/** 右按钮 */
				String rBtn=sysTimeOut.rBtn;
				/** 左按钮 */
				String lBtn=sysTimeOut.lBtn;
				final int event1=sysTimeOut.event;
				
				//特殊处理，若服务器要求退出，则先退出
				if ((event1 & SystemMessage.TYPE_EVENT_CLOSE_ROOM) == SystemMessage.TYPE_EVENT_CLOSE_ROOM
						||(event1 & SystemMessage.TYPE_EVENT_CLOSE_GAME) == SystemMessage.TYPE_EVENT_CLOSE_GAME) {
					
					if(FiexedViewHelper.getInstance().getCurFragment() == FiexedViewHelper.COCOS2DX_VIEW){
						GameUtilJni.onZoneEvent(GameUtilJni.EventZone_BackToCardZone);
					}else if (FiexedViewHelper.getInstance().getCurFragment() == FiexedViewHelper.LOADING_VIEW) {
						if(FiexedViewHelper.getInstance().loadingFragment!=null){
							FiexedViewHelper.getInstance().loadingFragment.cancelLoading();
						}
						FiexedViewHelper.getInstance().skipToFragment(FiexedViewHelper.CARDZONE_VIEW);
					}
				}
				
 				if (!Util.isEmptyStr(title)) {
					CustomDialog dialog=new CustomDialog(mAct,title,lBtn,rBtn);
					dialog.setConfirmCallBack(new OnClickListener() {
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							if(MMVideoBuyDialog.isDataReady()){
								MMVideoBuyDialog dialog=new MMVideoBuyDialog(mAct);
								dialog.show();
							}
						}
					});
					dialog.show();
					dialog.setBtnCancelGone();
				}
			}
				break;
			default:
				break;
			}
		}
	};

	
    private static final short MDM_USER   = 101 ;  //比赛排名主协议
	
	/** 游戏比赛用户信息请求 **/
	//private static final short SUB_GR_MATCH_RANKING_LIST_REQ = 1102;//比赛排名子协议,发送
	/** 比赛前几名排名信息返回（服务器下发） **/
	private static final short SUB_GR_MATCH_RANKING_LIST_RESP = 1103;//比赛排名子协议，接收
	
	private void receiveRankOrder(){ 
		short [][] parseProtocol = {{MDM_USER,SUB_GR_MATCH_RANKING_LIST_RESP}};
		//创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			public boolean doReceive(NetSocketPak netSocketPak) {
				Log.v(TAG, "receiveRankOrder");
				//解析接受到的网络数据
				TDataInputStream tdis = netSocketPak.getDataInputStream();
				tdis.setFront(false);

				int sum=tdis.readShort(); // 总人数
				int cur=tdis.readShort(); // 当前人数
				if(RankOrderProvider.getInstance().getFinishStatus()){
					return true;
				}else{
					RankOrderProvider.getInstance().clear();
				}
				for (int i = 0; i < cur; i++) {
					RankOrderInfo matchInfo = new RankOrderInfo(tdis);
					RankOrderProvider.getInstance().add(matchInfo);
				}
				
				if(sum==cur){  //数据接收完成 跳转界面
					RankOrderProvider.getInstance().setFinishStatus(true);
					mProtocolHandler.sendEmptyMessage(HANDLER_SHOW_RANKING_SUCCESS);
				
				}else{
					mProtocolHandler.sendEmptyMessage(HANDLER_SHOW_RANKING_FAIL);
				}
				return true;
			}
		};
		//注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		nPListener.setOnlyRun(false);
	}
	
	
	
	/**
	 * @Title: getSystemMessage
	 * @Description: 监听系统下发消息
	 * @version: 2012-9-20 上午10:22:42
	 */
	private void getSystemMessage() { // 定义接受数据的协议
		short[][] parseProtocol = { { MDM_GF_FRAME, SUB_GF_MESSAGE } };
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				TDataInputStream tdis = netSocketPak.getDataInputStream();
				tdis.setFront(false);
				short msgType = tdis.readShort();
				String msgStr = tdis.readUTFShort(); // 消息体 UTF8
				
				Message msg=mProtocolHandler.obtainMessage();
				msg.what=HANDLER_ARCHITECTURE;
				msg.obj=msgStr;
				msg.arg1=msgType;
				mProtocolHandler.sendMessage(msg);
				return true;
			}

		};

		NetSocketManager.getInstance().addPrivateListener(nPListener);
		nPListener.setOnlyRun(false);
	}

	/**
	 * @Title: doReceiveMDM_GR_COMMON_MESSAGE
	 * @Description: 通用系统消息的协议解析 接受：10---200
	 * @return boolean 返回类型
	 * @throws
	 * @version: 2011-12-12 上午09:48:51
	 */
	public void doReceiveSystemCOMMON_MESSAGE() {
		// 定义接受数据的协议
		short[][] parseProtocol = { { MDM_SYSTEM, MDM_GR_COMMON_MESSAGE } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				// 解析接受到的网络数据
				TDataInputStream tdis = netSocketPak.getDataInputStream();

				final SystemMessage baseMessage = new SystemMessage(tdis);
				// final int showType = baseMessage.showType;
				final int type = baseMessage.type;
				final int event = baseMessage.event;
				
				final byte scope= baseMessage.scope;
				if(scope==SystemMessage.TYPE_SCOPE_ROOM){
					Message message = mProtocolHandler.obtainMessage();
					message.what=HANDLER_QUICK_BUY_MSG;    //1.7.0新增游戏内快捷购买提示
					message.obj=baseMessage.text;
					mProtocolHandler.sendMessage(message);
				}
				
				Message msg = mProtocolHandler.obtainMessage();
				msg.obj = baseMessage;
				msg.arg1=event;  //消息事件
				
				
				switch (type) {
				case SystemMessage.E_MT_STANDARD:// 标准消息类型 对应结构
					// tagMSG_CommonMessage
					break;
				case SystemMessage.E_MT_STANDARDPOPUP:// 标准弹出框消息类型 对应结构
					// tagMSG_Popup
					// 通用消息
					msg.what=HANDLER_TYPE_STANDARD_POP;
					mProtocolHandler.sendMessage(msg);
					break;
				case SystemMessage.E_MT_HAVEURLPOPUP: // 按钮带链接弹出框消息类型 对应结构
					// tagMSG_HaveUrlPopup
					msg.what=HANDLER_OPEN_URL;
					mProtocolHandler.sendMessage(msg);
					break;

				case SystemMessage.E_MT_PROPSHORTCUTBUY:// 道具快捷购买提示 对应结构
				case SystemMessage.E_MT_PROPSHORTCUTBUYNOBACK: //道具快捷购买，游戏中不返回分区
					// 道具快捷购买提示
					msg.what=HANDLER_TYPE_STANDARD_BUY;
					mProtocolHandler.sendMessage(msg);
					break;
				case SystemMessage.E_MT_PROPUSE: // 道具使用提示, 对应结构 tagMSG_PropUse
					/** 消息类型 道具快捷购买使用提示 */
					break;
				case SystemMessage.E_MT_REALNAME:// 实名认证提示 对应结构
					msg.what=HANDLER_COMMOM_MESSAGE;
					mProtocolHandler.sendMessage(msg);
					break;
				case SystemMessage.E_MT_COMMON_POPUP:  //破产送提示
					msg.what=HANDLER_TYPE_COMMOM_POP;
					mProtocolHandler.sendMessage(msg);
					break;
				case SystemMessage.E_MT_WAITTIMEOUT:  //比赛场组桌匹配超时
					msg.what=HANDLER_WAIT_TIMEOUT;
					mProtocolHandler.sendMessage(msg);	
					break;
				case SystemMessage.E_MT_VEDIO_NEED_VIP:
					msg.what=HANDLER_VEDIO_NEED_VIP;
					mProtocolHandler.sendMessage(msg);	
					break;
				default:
					break;
				}
				return true;
			}
		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		nPListener.setOnlyRun(false);// 设置一直运行
	}

	/***
	 * @Title: roomDisConnect
	 * @Description: 服务器返回与房间断开断开连接
	 * @version: 2012-9-4 下午04:00:47
	 */
	private void roomDisConnect() {
		// 定义接受数据的协议
		short[][] parseProtocol = { { MDM_SERVER_ERROR, MSUB_ROOM_DISCONNECT } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				if (FiexedViewHelper.getInstance().getCurFragment() == FiexedViewHelper.COCOS2DX_VIEW) {
                   //byte[] buffer = netSocketPak.getBufferByte();
                   //GameUtilJni.parseGameNetData(buffer, buffer.length);
					return true;
				}
				
				// 解析接受到的网络数据
				TDataInputStream tdis = netSocketPak.getDataInputStream();
				tdis.setFront(false);
				short msgType = tdis.readShort();
				String msgStr = tdis.readUTFShort();

				Message msg=mProtocolHandler.obtainMessage();
				msg.what=HANDLER_DISCONNECT;
				msg.arg1=msgType;
				msg.obj=msgStr;
				mProtocolHandler.sendMessage(msg);
				return true;
			}
		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		nPListener.setOnlyRun(false);
	}

	/*****
	 * @Title: ReceiveAutoRoomInfo
	 * @Description:不再调用进入房间协议，直接速配成功服务端自动下发
	 * @version: 2012-10-15 下午06:19:12
	 */
	private void receiveAutoRoomInfo() {
		// 定义接受数据的协议(成功，失败)双
		short[][] parseProtocol = { { MDM_ROOM, MSUB_CMD_ENTER_ROOM_FAILED }, { MDM_ROOM, MSUB_CMD_ENTER_ROOM_SUCCEEDED } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				// 解析接受到的网络数据
				TDataInputStream tdis = netSocketPak.getDataInputStream();
				tdis.setFront(false);
				if (netSocketPak.getSub_gr() == MSUB_CMD_ENTER_ROOM_SUCCEEDED) {
					/***********************************************
					 * 进入房间成功
					 **********************************************/
					// (四：进入房间后根据用户选择是否立即坐下)
					// 数据：返回本人用户信息（以PC结构体下发，很别扭但没办法，历史遗留问题）
					/** 保存用户信息到全局单例中 **/
					UserInfo.setValueForRoomParse(tdis, HallDataManager.getInstance().getUserMe());
					Message msgobj = mProtocolHandler.obtainMessage(HANDLER_ROOM_SUCCESS);
					mProtocolHandler.sendMessage(msgobj);
				} else if (netSocketPak.getSub_gr() == MSUB_CMD_ENTER_ROOM_FAILED) {

					/***********************************************
					 * 进入房间失败
					 **********************************************/
					int ErrorCode = tdis.readInt();
					String ErrMsg = tdis.readUTFShort();
					Message msg = mProtocolHandler.obtainMessage(HANDLER_ROOM_NODES_FAIL);
					msg.arg1 = ErrorCode;
					msg.obj = ErrMsg;
					mProtocolHandler.sendMessage(msg);
				}
				return true;
			}
		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		nPListener.setOnlyRun(false);
	}

	/****
	 * @Title: intoGameRoomNormalNodeSuccess
	 * @Description: 进入游戏房间成功，普通节点处理(自由对战区、智运会区)
	 * @param msg
	 *            消息实体
	 * @param invokeState
	 *            调用状态
	 * @version: 2013-3-6 上午10:34:55
	 */
	private void intoGameRoomNormalNodeSuccess(Message msg, NodeData nodeData, int invokeState) {
		if (HallDataManager.getInstance().getUserMe().userStatus == UserInfo.USER_PLAY) {
			// 已经在比赛中
			FiexedViewHelper.getInstance().skipToFragment(FiexedViewHelper.COCOS2DX_VIEW);
		} else {
			// 尚未比赛，显示房间详细
			preparePlayGame(nodeData);
			//TODO: 请求坐下现在改为由服务器自动坐下，客服端不要请求坐下
		}
	}

	/****
	 * @Title: intoGameRoomHCOtherNodeSuccess
	 * @Description: 进入游戏房间成功，约战，娱乐赛节点处理
	 * @param nodeData
	 *            当前节点
	 * @param invokeState
	 *            调用状态
	 * @version: 2013-3-6 上午10:47:48
	 */
	public void intoGameRoomHCOtherNodeSuccess(NodeData nodeData, int invokeState) {
		if (HallDataManager.getInstance().getUserMe().userStatus == UserInfo.USER_PLAY) {
			// 游戏请求场景
			/*********************************
			 * 切换到cocos2d-x游戏
			 ********************************/
			FiexedViewHelper.getInstance().skipToFragment(FiexedViewHelper.COCOS2DX_VIEW);
		} else {
			if (FiexedViewHelper.getInstance().cardZoneFragment.gameRoomAssociated != null) {
				FiexedViewHelper.getInstance().cardZoneFragment.gameRoomAssociated.requestGameSitDown();
			}
		}
		// 因为切换后，在游戏中，而当前UI还是赚话费专区，
		// 因此，必须清除掉当前UI，一般游戏中返回时不至于显示赚话费专区房间信息
		if (NodeData.NODE_ENROLL == nodeData.Type) {
			// 关闭定时器，节省内存
			if(FiexedViewHelper.getInstance().amusementFragment!=null){
				FiexedViewHelper.getInstance().amusementFragment.ApplyOnlineAndMatchPersonTimer(false);
			}
			// 关闭比赛赚话费房间UI
			FiexedViewHelper.getInstance().skipToFragment(FiexedViewHelper.COCOS2DX_VIEW);
		} else {
			FiexedViewHelper.getInstance().skipToFragment(FiexedViewHelper.COCOS2DX_VIEW);
		}
	}

	/***
	 * @Title: intoGameRoomFail
	 * @Description: 进入房间失败
	 * @param msg
	 *            消息实体
	 * @param invokeState
	 *            调用状态
	 * @version: 2013-3-6 上午10:26:18
	 */
	private void intoGameRoomFail(Message msg, int invokeState) {
		String errMsg = (String) msg.obj;
		if (!Util.isEmptyStr(errMsg)) {
			// 有断线重连信息，并进入房间失败(强退)
			UtilHelper.showCustomDialog(mAct, errMsg);
		}
		FiexedViewHelper.getInstance().skipToFragment(FiexedViewHelper.CARDZONE_VIEW);
	}

	
	
	/**  
	 * 1.5.3不用请求坐下
	 * 
	 */
	private void preparePlayGame(NodeData nodeData) {
			// 快速进入逻辑, 不显示房间详细信息，直接进入游戏
			if (FiexedViewHelper.getInstance().cardZoneFragment.gameRoomAssociated != null) {
				FiexedViewHelper.getInstance().cardZoneFragment.gameRoomAssociated.requestGameSitDown();
			}
			if(FiexedViewHelper.getInstance().getCurFragment()==FiexedViewHelper.LOADING_VIEW){
				if (FiexedViewHelper.getInstance().loadingFragment != null 
						&& nodeData.Type!=NodeData.NODE_MM_VIDEO) {
					FiexedViewHelper.getInstance().loadingFragment.setLoadingType(mResource.getString(R.string.ddz_into_fanzuobi), NodeDataType.NODE_TYPE_101);
				}
			
		}
	}

	
	
	@Override
	public void invokeListItem(NodeData node, boolean isQuickEntry) {
		invokeCardZoneListViewItem(node);
	}

	/****
	 * @Title: invokeCardZoneListViewItem
	 * @Description: 调用列表快速进入
	 * @param item
	 *            列表项
	 * @param shortCut
	 *            是否快捷进入
	 * @version: 2013-3-5 下午05:07:32
	 */
	public void invokeCardZoneListViewItem(NodeData node) {
		/***********************************
		 * 2013-3-23 新增判断房间准入条件而非客户端节点准入条件
		 ******************************************/
		//panjy 2013-6-11 start
		if (CommonBeanHelper.bdjectRoomCanCome(node)) {
			String quickMessage = mResource.getString(R.string.ddz_level);
			UtilHelper.showCustomDialog(mAct, quickMessage, new OnClickListener() {
				@Override
				public void onClick(View v) {
					//****************************************
					// * 原本调用快速游戏逻辑 现改为直接调用高手场逻辑
					// **************************************
					superiorLogicInvoke(mAct);
				}
			});
			return;
		}
		//panjy 2013-6-11 end*/
		// 这里根据不同节点类型进入不同面板UI(比较C++那边数据)
		if(verNotSupport(node.Type)){
			UtilHelper.showCustomDialog(mAct, "亲爱的玩家,由于检测到您的版本过低,需要您升级斗地主客户端才能进行游戏");
			return;
		}
		
		
		HallDataManager.getInstance().setCurrentNodeData(node);// 保存选中节点
		
		switch (node.Type) {
		// ===========================================约战节点========================================================
		case NodeData.NODE_CHALLENGE: // 约战节点-----111
			FiexedViewHelper.getInstance().skipToFragment(FiexedViewHelper.CHALLENGE_ROOM);
			if(FiexedViewHelper.getInstance().challengeFragment!=null){
				FiexedViewHelper.getInstance().challengeFragment.sendUserLogin(node.dataID);
			}
			break;
		// ==========================================================================================================
		case NodeData.NODE_NORMAL: // 普通节点(自由对战、智运会) 101
		case NodeData.NODE_MM_VIDEO: // 美女视频节点
			// （五）显示房间详细，并请求查询任务->服务下发任务信息
			UserInfo userInfo = HallDataManager.getInstance().getUserMe();

			GameTask.getInstance().queryTaskInfo(userInfo.userID);

			// 登录时间太快，取消loading界面，否则loading一闪而过
			FiexedViewHelper.getInstance().skipToFragment(FiexedViewHelper.LOADING_VIEW);
			if (FiexedViewHelper.getInstance().loadingFragment != null) {
				FiexedViewHelper.getInstance().loadingFragment.setLoadingType(mResource.getString(R.string.ddz_into_game_room), NodeDataType.NODE_TYPE_101);
			}
		
			FiexedViewHelper.getInstance().quickMatcheRoom(node);
			break;
		case NodeData.NODE_ROOM: // 房间节点102

			break;
		case NodeData.NODE_ENROLL: // 报名节点,娱乐比赛场专用 109
			FiexedViewHelper.getInstance().skipToFragment(FiexedViewHelper.AMUSEMENT_ROOM);
			if(FiexedViewHelper.getInstance().amusementFragment!=null){
				FiexedViewHelper.getInstance().amusementFragment.sendLoginDataId(node.dataID);
			}
			break;
		default:
			break;
		}

	}

	
	private boolean verNotSupport(byte type){
        boolean res=false;
		int playId=FiexedViewHelper.getInstance().getGameType();
		if(playId == FiexedViewHelper.GAME_TYPE_XIAOBING ||playId ==FiexedViewHelper.GAME_TYPE_LAIZI){
			if(type==NodeData.NODE_CHALLENGE ||type==NodeData.NODE_ENROLL){
				res=true;
			}
		}
		return res;
	}
	
	
	/***
	 * @Title: superiorLogicInvoke
	 * @Description: 进入更高一级的赛场
	 * @param act
	 * @version: 2012-11-8 上午11:03:21
	 */
	public void superiorLogicInvoke(Activity act) {
		NodeData entryNode = null;
		boolean breakOut = false;
		//初始化默认选择第一个节点
		 
		
		List<NewUIDataStruct> lists = NewCardZoneProvider.getInstance().getNewUIDataList();
		if(lists!=null && lists.size()>0){
			entryNode=lists.get(0).mSubNodeDataList.get(0);
		}
		for (NewUIDataStruct item : lists) {

			List<NodeData> nodes = item.mSubNodeDataList;
			for (NodeData node : nodes) {
				if (node.Type == NodeData.NODE_NORMAL) { // 普通节点(自由战、智运会)
					// 普通节点(自由战、智运会)
					if (CommonBeanHelper.isSuperiorLimitBean(node) && (node.ExtType & 0x0001) > 0) {
						entryNode = node; // 符合房间乐豆上下限
						breakOut = true;
						break;
					}

				}

			}
			if (breakOut) {
				break;
			}
		}

		if (entryNode != null) {
			FiexedViewHelper.getInstance().skipToFragment(FiexedViewHelper.LOADING_VIEW);
			if (FiexedViewHelper.getInstance().loadingFragment != null) {
				FiexedViewHelper.getInstance().loadingFragment.setLoadingType(mResource.getString(R.string.ddz_into_game_room), NodeDataType.NODE_TYPE_101);
			}
			FiexedViewHelper.getInstance().quickMatcheRoom(entryNode);
			HallDataManager.getInstance().setCurrentNodeData(entryNode);// 保存选中节点
		}
	}



	/****
	 * @Title: exitFreeRoom
	 * @Description: 返回键，退出房间
	 * @version: 2013-2-2 下午03:26:42
	 */
	public void exitFreeRoom() {
		/*******************************
		 * 退出自由对战区房间: 发：14----3
		 ******************************/
		// （六）退出房间，发送(请求离开房间协议).以及清除任务信息
		GameTask.getInstance().clrearTask();

		RoomData room = HallDataManager.getInstance().getCurrentRoomData(); // 获取速配成功进入房间保存的房间信息
		if (room != null) {
			// 请求离开房间
			FiexedViewHelper.getInstance().cardZoneFragment.loginAssociated.exitRoom(room.RoomID);
		}
		// 关闭自由作战区房间
		FiexedViewHelper.getInstance().skipToFragment(FiexedViewHelper.CARDZONE_VIEW);
	}

	/***
	 * @Title: requestBackPackList
	 * @Description: TODO获取背包列表
	 * @version: 2012-7-23 下午02:51:10
	 */
	public void requestBackPackList(int userID, final Handler handler) {
		BackPackItemProvider.getInstance().init();
		// 创建发送的数据包
		TDataOutputStream tdos = new TDataOutputStream(false);
		tdos.writeInt(userID);
		NetSocketPak pointBalance = new NetSocketPak(MDM_PROP, MSUB_CMD_PACK_PROP_LIST_REQ_EX, tdos);
		// 定义接受数据的协议
		short[][] parseProtocol = { { MDM_PROP, MSUB_CMD_PACK_PROP_LIST_RESP } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			public boolean doReceive(NetSocketPak netSocketPak) {
				// 解析接受到的网络数据
				try {
					TDataInputStream tdis = netSocketPak.getDataInputStream();
					tdis.setFront(false);

					int total = tdis.readShort(); // 商品总个数
					int num = tdis.readShort(); // 当次商品个数
					BackPackItem[] backPackItems;
					if (num <= 0) {
						if(handler!=null){
							Message msg=handler.obtainMessage(HANDLER_PACK_QUERY_SUCCESS_NODATA);
							handler.sendMessage(msg);
						}
					} else {
						backPackItems= new BackPackItem[num];
						// 累计接受到数据到数组中
						for (int i = 0; i < num; i++) {
							backPackItems[i] = new BackPackItem(tdis);
						}
						BackPackItemProvider.getInstance().setBackPackItem(backPackItems);
						curBackpackNum += num; // 积累保存到全局变量，记录当前返回累计数目

						if (curBackpackNum == total) {
							if(handler!=null){
								Message msg=handler.obtainMessage(HANDLER_PACK_QUERY_SUCCESS);
								handler.sendMessage(msg);
							}
							BackPackItemProvider.getInstance().setFinish(true);
							curBackpackNum=0;
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

}
