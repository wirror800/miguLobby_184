package com.mykj.andr.ui.widget;


import android.os.Handler;
import android.os.Message;

import com.mingyou.distributor.NetPrivateListener;
import com.mingyou.distributor.NetSocketPak;
import com.mykj.andr.model.ActionInfo;
import com.mykj.andr.net.NetSocketManager;
import com.mykj.andr.ui.widget.Interface.ActionInfoInterface;
import com.mykj.comm.io.TDataInputStream;
import com.mykj.comm.io.TDataOutputStream;
import com.mykj.game.FiexedViewHelper;
import com.mykj.game.utils.AppConfig;

public class ActionInfoWidget implements ActionInfoInterface {
	
	 /** 活动 */
	private ActionInfo[] actionInfos;
	/** 当前数 **/
	private int currReturnActionNum = 0;
	 
	
	private static ActionInfoWidget instance;
	
	private ActionInfoWidget(){
	
	}
	
	public static ActionInfoWidget getInstance(){
		if(instance==null){
			instance=new ActionInfoWidget();
		}
		return instance;
	}
	
	
	
	
	@Override
	public void requestActionInfoList() {
		// 创建发送的数据包
		TDataOutputStream tdos = new TDataOutputStream(false);
		tdos.writeShort(2);
		tdos.writeInt(AppConfig.gameId);
		NetSocketPak pointBalance = new NetSocketPak(LS_TRANSIT_LOGON,
				MSUB_CMD_ACTIVITY_LIST_ASK, tdos);
		// 定义接受数据的协议
		short[][] parseProtocol = { { LS_TRANSIT_LOGON,
				MSUB_CMD_ACTIVITY_LIST_ACK } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			public boolean doReceive(NetSocketPak netSocketPak) {
				// 解析接受到的网络数据
				try {
					TDataInputStream tdis = netSocketPak.getDataInputStream();
					tdis.setFront(false);


					int total = tdis.readShort(); // 活动总个数
					int num = tdis.readShort();   // 当次活动个数
					//byte len= tdis.readByte();//名称长度
					String url = tdis.readUTFShort();
					if (num > 0) {
						if (actionInfos == null && currReturnActionNum == 0) { // 尚未有数组对象情况下
							actionInfos = new ActionInfo[total];
						}
						// 累计接受到数据到数组中
						for (int i = 0; i < num; i++) {
							if (currReturnActionNum + i < actionInfos.length) {
								actionInfos[currReturnActionNum + i] = new ActionInfo(
										tdis, url);
							}
						}
						currReturnActionNum += num; // 积累保存到全局变量，记录当前返回累计数目
						if (currReturnActionNum >= total) {
							// 读取完毕，交与主线程显示（同时恢复变量）
							//NetSocketManager.getInstance().removePrivateListener(this);
							currReturnActionNum = 0;
                            
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
				//无论是否有活动都应该弹出页面
				if(FiexedViewHelper.getInstance().cardZoneFragment!=null){
                	Handler handler=FiexedViewHelper.getInstance().cardZoneFragment.cardZoneHandler;
                	Message actionMsg=handler.obtainMessage();
                	actionMsg.what=HANDLER_ACT_QUERY_SUCCESS;
                	actionMsg.obj=actionInfos;
                	handler.sendMessage(actionMsg);
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
