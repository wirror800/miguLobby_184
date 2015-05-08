package com.mykj.andr.ui.widget;

import android.os.Handler;
import android.os.Message;

import com.mingyou.distributor.NetPrivateListener;
import com.mingyou.distributor.NetSocketPak;
import com.mykj.andr.model.HallDataManager;
import com.mykj.andr.model.NodeData;
import com.mykj.andr.model.RoomData;
import com.mykj.andr.net.NetSocketManager;
import com.mykj.andr.ui.widget.Interface.LoginAssociatedInterface;
import com.mykj.comm.io.TDataInputStream;
import com.mykj.comm.io.TDataOutputStream;
import com.mykj.game.FiexedViewHelper;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.Log;

/***
 * 
 * @ClassName: LoginAssociatedWidget
 * @Description: 登录关联组建
 * @author zhanghuadong
 * @date 2012-11-9 上午11:37:26
 */
public class LoginAssociatedWidget implements LoginAssociatedInterface {


	private static LoginAssociatedWidget instance;
	private LoginAssociatedWidget(){

	}

	public static LoginAssociatedWidget getInstance(){
		if(instance==null){
			instance=new LoginAssociatedWidget();
		}
		return instance;
	}

	@Override
	public void requestWhiteBind(int platId, int uid, String apiKey, String TAT) {
		// 创建发送的数据包
		TDataOutputStream tdos = new TDataOutputStream();
		tdos.writeInt(platId);
		tdos.writeInt(uid);
		tdos.writeUTFByte(apiKey);
		tdos.writeUTFByte(TAT);

		final NetSocketPak requestWhiteBind = new NetSocketPak(MDM_LOGIN,
				MSUB_CMD_TAT_WHITE_BIND, tdos);
		// 定义接受数据的协议
		short[][] parseProtocol = { { MDM_LOGIN, MSUB_CMD_LOGIN_V2_ERR },
				{ MDM_LOGIN, MSUB_CMD_LOGIN_V2_USERINFO } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {

			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				try {
					TDataInputStream tdis = netSocketPak.getDataInputStream();
					short sub_gr = netSocketPak.getSub_gr();
					if (sub_gr == MSUB_CMD_LOGIN_V2_USERINFO) {

						//showDialog(SuperDialogActivity.SINGLE_BUTTON_DIALOG,
						//		"您已成为正式用户，请重新登录游戏后生效");

						Log.e(TAG, "MSUB_CMD_LOGIN_V2_USERINFO");
					} else if (sub_gr == MSUB_CMD_LOGIN_V2_ERR) {
						byte ret = tdis.readByte();
						String errMsg = tdis.readUTFByte();
						Log.e(TAG, "白名单绑定："+String.valueOf(ret) + errMsg);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				// 数据处理完成，终止继续解析
				return true;
			}
		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		// 发送协议
		NetSocketManager.getInstance().sendData(requestWhiteBind);
		// 清理协议对象
		requestWhiteBind.free();
	}

	@Override
	public void breakLine(byte LobbyType) {
		// 定义接受数据的协议
		short[][] parseProtocol = { { MDM_ROOM, MSUB_CMD_SELECT_CUT_DATA } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				// 解析接受到的网络数据
				TDataInputStream tdis = netSocketPak.getDataInputStream();
				tdis.setFront(false);
				// 断线返回结果 （0- 无断线信息 1-有断线信息）
				final byte state = tdis.readByte();

				if (state == 1) {  //1 有断线数据；0无断线数据
					/***********************************
					 * 1-有断线信息
					 * 2-记录断线时房间信息
					 ***********************************/
					final RoomData room = new RoomData(tdis); // 房间数据内容（参考房间列表的房间数据内容）
					HallDataManager.getInstance().setCurrentRoomData(room);

					int num = tdis.readShort(); // 节点个数
					NodeData[] array = null;
					if (num > 0) {
						array = new NodeData[num];
						for (int i = 0; i < array.length; i++) {
							//2012-12-7
							array[i] = new NodeData(tdis);
						}
					}

					if (array != null) {
						int len = array.length;
						for (int i = 0; i < len; i++) {
							NodeData temp = array[i];
							if (temp != null && temp.ID == room.NodeID) { // 当前房间节点==节点ID
								HallDataManager.getInstance().setCurrentNodeData(temp);
								break;
							}
						}
					}
					// 游戏ID
					final short gameId = tdis.readShort();
					if(gameId==AppConfig.gameId){
						//发送通知
						Handler hanlder=FiexedViewHelper.getInstance().cardZoneFragment.cardZoneHandler;
						Message msg=hanlder.obtainMessage();
						msg.what=HANDLER_CUT_LINK_HAVE_DATA;
						msg.arg1=room.RoomID;
						hanlder.sendMessage(msg);
					}

				} 

				return true;
			}
		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		nPListener.setOnlyRun(false);



		// 构造数据包
		TDataOutputStream tdous = new TDataOutputStream(false);
		tdous.writeByte(LobbyType);
		NetSocketPak mConsumptionSock = new NetSocketPak(MDM_ROOM,MSUB_CMD_SELECT_CUT_REQ_EXT, tdous);
		// 发送协议
		NetSocketManager.getInstance().sendData(mConsumptionSock);
	}

	@Override
	public void exitRoom(int RoomID) {
		// 构造数据包
		TDataOutputStream tdous = new TDataOutputStream(true);
		tdous.writeInt(RoomID);
		NetSocketPak mConsumptionSock = new NetSocketPak(MDM_ROOM,
				MSUB_CMD_LEAVE_ROOM_REQ, tdous); 
		NetSocketManager.getInstance().sendData(mConsumptionSock); 
		// 清理协议对象
		mConsumptionSock.free();
	}

	@Override
	public void querySwitchGame(int userID, int gameID) {
		// TODO Auto-generated method stub

	}




}
