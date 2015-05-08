package com.mykj.andr.ui.widget;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.mingyou.distributor.NetPrivateListener;
import com.mingyou.distributor.NetSocketPak;
import com.mykj.andr.model.HallDataManager;
import com.mykj.andr.model.NodeData;
import com.mykj.andr.model.RoomConfigData;
import com.mykj.andr.model.RoomData;
import com.mykj.andr.model.RoomInfoEx;
import com.mykj.andr.model.UserInfo;
import com.mykj.andr.net.NetSocketManager;
import com.mykj.andr.ui.fragment.CardZoneFragment;
import com.mykj.andr.ui.widget.Interface.GameRoomAssociatedInterface;
import com.mykj.comm.io.TDataInputStream;
import com.mykj.comm.io.TDataOutputStream;
import com.mykj.game.FiexedViewHelper;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.Log;

/***
 * 
 * @ClassName: GameRoomAssociatedWidget
 * @Description: 游戏房间关联组建
 * @author zhanghuadong
 * @date 2012-11-9 上午11:37:26
 */
public class GameRoomAssociatedWidget implements GameRoomAssociatedInterface {
	
	public static final String TAG = "GameRoomAssociatedWidget";

	
	private static GameRoomAssociatedWidget instance;
    
	private Activity mAct;

	private GameRoomAssociatedWidget(Activity act) {
		mAct=act;
	}

	
	public static GameRoomAssociatedWidget getInstance(Activity act){
		if(instance==null){
			instance=new GameRoomAssociatedWidget(act);
		}
		return instance;
	}
	
	
	
	/***
	 * @Title: QuickMatchNode
	 * @Description: 速配某节点（找到房间）
	 * @param nodeID 节点ID
	 * @version: 2012-11-9 上午11:45:39
	 */
	@Override
	public void quickMatchNode(final NodeData node) {
		// 定义接受数据的协议
		short[][] parseProtocol = { { MDM_QUICK_PLAY, MSUB_QUICK_PLAY_ROOMS_DATA } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				// 解析接受到的网络数据
					TDataInputStream tdis = netSocketPak.getDataInputStream();
					tdis.setFront(false);
					// 保存读取信息
					byte state = tdis.readByte(); // 速配结果 (0-失败   1-成功)
					Handler handler=CardZoneProtocolListener.getInstance(mAct).mProtocolHandler;
					if (state == 1) {
						/**************************************
						 * 房间速配成功:1
						 ***************************************/
						RoomData room = new RoomData(tdis); // 房间数据内容
						
						int num = tdis.readShort(); // 节点个数 ,此协议下发数据未使用
						if (num > 0) {                
							NodeData[] array = new NodeData[num];
							for (int i = 0; i < num; i++) {
								array[i] = new NodeData(tdis);
							}
						}
						
						Message msg =handler.obtainMessage();
						msg.what=HANDLER_MSUB_QUICK_PLAY_ROOMS_DATA_SUCCESS;
						msg.obj = node;
						Bundle data = new Bundle();
						data.putSerializable(TAG, room);
						msg.setData(data);
						handler.sendMessage(msg);
					}else{
						handler.sendEmptyMessage(HANDLER_MSUB_QUICK_PLAY_ROOMS_DATA_FAIL);
					}
				
				return true;
			}
		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		nPListener.setOnlyRun(false);
		//
		// 创建发送的数据包
		TDataOutputStream tdos = new TDataOutputStream(false);
		tdos.writeInt(node.ID); // NodeID
		tdos.writeByte(CardZoneDataListener.LOBBYTYPE); // lobbyType=3
		NetSocketPak pointBalance = new NetSocketPak(MDM_QUICK_PLAY, MSUB_QUICK_PLAY_ROOMS_REQ_EXT, tdos);
		NetSocketManager.getInstance().sendData(pointBalance);
	}

	
	/**
	 * 请求退出房间
	 */
	@Override
	public void exitRoom(int roomID) {
		TDataOutputStream tdous = new TDataOutputStream(true);
		tdous.writeInt(roomID);
		NetSocketPak mConsumptionSock = new NetSocketPak(MDM_ROOM, MSUB_CMD_LEAVE_ROOM_REQ, tdous);
		NetSocketManager.getInstance().sendData(mConsumptionSock);
		mConsumptionSock.free();
	}

	
	/**
	 * 请求进入房间
	 */
	@Override
	public void enterRoom(int roomID) {
		// 构造数据包
		TDataOutputStream tdous = new TDataOutputStream(false);
		tdous.writeInt(roomID);

		NetSocketPak enterRoomSocket = new NetSocketPak(MDM_ROOM, MSUB_CMD_ENTER_ROOM_REQ, tdous);
		NetSocketManager.getInstance().sendData(enterRoomSocket);
	}

	
	
	@Override
	public void quickGame() {

	}

	/**
	 * @Title: RequestGameSitDown
	 * @Description: 快速自动坐下请求
	 * @version: 2011-7-12 下午05:54:51
	 */
	@Override
	public void requestGameSitDown() {
		requestGameSitDown((short) -1, null);
	}

	
	
	/***
	 * @Title: SitDown
	 * @Description: 坐下请求（手机端椅子号默认为-1）
	 * @param TableID桌子号
	 * @param szTablePass桌子密码
	 * @version: 2012-9-4 上午11:24:13
	 */
	private void requestGameSitDown(short TableID, String szTablePass) {
		// 定义接受数据的协议
		short[][] parseProtocol = { { MDM_USER, MSUB_GR_SIT_FAILED } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				TDataInputStream tdis = netSocketPak.getDataInputStream();
				tdis.setFront(false);
				if (netSocketPak.getSub_gr() == MSUB_GR_SIT_FAILED) { // 坐下失败
					String errorMsg = tdis.readUTFShort(); // 坐下失败原因
					Handler handler=FiexedViewHelper.getInstance().cardZoneFragment.cardZoneHandler;
					
					Message msg = handler.obtainMessage();
					msg.what=CardZoneFragment.HANDLER_SIT_DOWN_FAIL;
					msg.obj=errorMsg;
					handler.sendMessage(msg);
				}
				return true;
			}
		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		nPListener.setOnlyRun(false);
		short ChairID = -1;// 手机端椅子号为-1
		TDataOutputStream out = new TDataOutputStream(false);

		out.writeShort(TableID);
		out.writeShort(ChairID);
		if (szTablePass == null){
			out.writeUTFByte("");
		}
		NetSocketPak socketPak = new NetSocketPak(MDM_USER, MSUB_GR_USER_SIT_REQ, out);
		NetSocketManager.getInstance().sendData(socketPak);
		socketPak.free();
	}


	@Override
	public void receiveUserStatus() {
		// 定义接受数据的协议
		short[][] parseProtocol = { { MDM_USER, MSUB_GR_USER_STATUS } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {

				if (FiexedViewHelper.getInstance().getCurFragment() != FiexedViewHelper.COCOS2DX_VIEW) {

					TDataInputStream tdis = netSocketPak.getDataInputStream();
					tdis.setFront(false);
					// 获取玩家状态，根据用户状态判断坐下成功
					UserInfo socketUserInfo = new UserInfo(tdis); // 读取用户信息数据
					// 单例保存的用户对象
					UserInfo myuserInfo = HallDataManager.getInstance().getUserMe(); // 自己的用户信息
					int myUserID = myuserInfo.userID; // 获得自己的用户ID
					if (socketUserInfo.userID == myUserID) { // 本尊

						int MyStatus = socketUserInfo.userStatus;

						Log.e(TAG, "接收本用户状态成功：" + MyStatus);
						// 坐下成功
						if (MyStatus == UserInfo.USER_PLAY || 
							MyStatus == UserInfo.USER_OFFLINE ||
							MyStatus == UserInfo.USER_SYSTEMCONTROL|| 
							MyStatus == UserInfo.USER_SET || 
							MyStatus == UserInfo.USER_READY) {
							myuserInfo.setValue(socketUserInfo); // 更新自己的用户数据
							Handler handler=FiexedViewHelper.getInstance().cardZoneFragment.cardZoneHandler;
							// 显示游戏界面 ,发送场景信息
							handler.sendEmptyMessage(CardZoneFragment.HANDLER_USER_STATUS_SIT_DOWN_SUCCESS);
						}
						return false;
					}
				} 
				return false;
			}
		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		nPListener.setOnlyRun(false);
	}

	
	
	/***
	 * 游戏房间基本配置信息
	 */
	@Override
	public void receiveRoomConfigData() {
		// 定义接受数据的协议
		short[][] parseProtocol = { { MDM_CONFIG, MSUB_GR_ROOM_INFO }, 
				                    { MDM_CONFIG, MSUB_GR_ROOM_INFO_EX }};
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				if (netSocketPak.getSub_gr() == MSUB_GR_ROOM_INFO) {
					TDataInputStream tdis = netSocketPak.getDataInputStream();
					tdis.setFront(false);
					RoomConfigData matchRoomInfo = new RoomConfigData(tdis);
					HallDataManager.getInstance().setRoomConfigData(matchRoomInfo); // 设置房间配置信息
					RoomData room = HallDataManager.getInstance().getCurrentRoomData();
					if (room != null) {
						// 更新房间节点游戏类型，因为娱乐报名赛没有返回房间节点，所以这里必须更新
						room.GameType = matchRoomInfo.wGameGenre;
						
					}
		
				} else if (netSocketPak.getSub_gr() == MSUB_GR_ROOM_INFO_EX) {
					TDataInputStream tdis = netSocketPak.getDataInputStream();
					tdis.setFront(false);
					RoomInfoEx roominfo=new RoomInfoEx();
					roominfo.dwRoomAttrib1=  tdis.readInt();// 房间控制
					roominfo.dwRoomAttrib2 = tdis.readInt();// 保留
					roominfo.dwRoomAttrib3 = tdis.readInt();// 保留
					AppConfig.setRoomInfoEx(roominfo);
				}
				return true;
			}
		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		nPListener.setOnlyRun(false);
	}


}
