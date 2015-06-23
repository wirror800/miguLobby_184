package com.mykj.andr.ui.widget;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import android.os.Handler;
import android.os.Message;

import com.mingyou.distributor.NetPrivateListener;
import com.mingyou.distributor.NetSocketPak;
import com.mykj.andr.model.HallDataManager;
import com.mykj.andr.model.NewUIDataStruct;
import com.mykj.andr.model.NodeData;
import com.mykj.andr.model.NoticePersonInfo;
import com.mykj.andr.model.NoticeSystemInfo;
import com.mykj.andr.model.UserInfo;
import com.mykj.andr.net.NetSocketManager;
import com.mykj.andr.provider.NewCardZoneProvider;
import com.mykj.andr.provider.NoticePersonProvider;
import com.mykj.andr.provider.NoticeSystemProvider;
import com.mykj.andr.ui.fragment.LoadingFragment.NodeDataType;
import com.mykj.andr.ui.widget.Interface.HallAssociatedInterface;
import com.mykj.comm.io.TDataInputStream;
import com.mykj.comm.io.TDataOutputStream;
import com.mykj.game.FiexedViewHelper;
import com.MyGame.Midlet.R;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.Log;
import com.mykj.game.utils.Util;

/****
 * 
 * @ClassName: HallAssociatedWidget
 * @Description: 大厅相关接口
 * @author
 * @date 2012-11-9 下午12:47:38
 * 
 */
public class HallAssociatedWidget implements HallAssociatedInterface {

	private static final String TAG="HallAssociatedWidget";

	private static HallAssociatedWidget instance;

	private HallAssociatedWidget(){
	}

	public static HallAssociatedWidget getInstance(){
		if(instance==null){
			instance=new HallAssociatedWidget();
		}
		return instance;
	}


	@Override
	public void getUserCenterInfo(int userId, int clentId) {
		// 创建发送的数据包
		TDataOutputStream tdos = new TDataOutputStream(false);
		tdos.writeInt(userId);
		tdos.writeInt(clentId);

		NetSocketPak centerInfo = new NetSocketPak(LS_TRANSIT_LOGON,
				MSUB_USERCENTER_INFO_REQ, tdos);
		// 发送协议
		NetSocketManager.getInstance().sendData(centerInfo);
		// 清理协议对象
		centerInfo.free();

	}





	/**
	 * 快速开始判断
	 */
	@Override
	public void quickGame(){
		boolean breakOut = false;

		//缓存下来的节点列表(琼瑶列表)
		List<NewUIDataStruct> lists=NewCardZoneProvider.getInstance().getNewUIDataList();

		//初始化默认选择第一个节点
		NodeData defaultNode=null;
		if(lists!=null && lists.size()>0){
			defaultNode=lists.get(0).mSubNodeDataList.get(0);
		}

		//遍历判断是否有符合的节点，乐豆范围内&&支持快速游戏
		for(NewUIDataStruct item:lists){
			List<NodeData> nodes=item.mSubNodeDataList;
			for(NodeData node:nodes){
				if ((node.ExtType & 0x0001) > 0) { // 支持快速游戏
					UserInfo user = HallDataManager.getInstance().getUserMe();
					if (node.isQuickGame(user)) {
						defaultNode=node;
						breakOut=true;
						break;
					}
				}
			}
			if(breakOut){
				break;
			}
		}

		/*********************************
		 * 遍历完成
		 *********************************/

		FiexedViewHelper.getInstance().skipToFragment(FiexedViewHelper.LOADING_VIEW);
		if(FiexedViewHelper.getInstance().loadingFragment!=null){
			FiexedViewHelper.getInstance().loadingFragment.setLoadingType(AppConfig.mContext.getResources().getString(R.string.ddz_adapte_game_room), NodeDataType.NODE_TYPE_101);
		}

		HallDataManager.getInstance().setCurrentNodeData(defaultNode); // 保存能进节点

		FiexedViewHelper.getInstance().quickMatcheRoom(defaultNode);
	}

	@Override
	public void givingBankruptcy() {
		// 创建发送的数据包
		UserInfo userInfo = HallDataManager.getInstance().getUserMe();
		TDataOutputStream tdos = new TDataOutputStream(false);
		tdos.writeInt(userInfo.bean, false);
		NetSocketPak socketInfo = new NetSocketPak(LS_TRANSIT_LOGON,
				MSUB_CMD_PRESENT_ASK, tdos);
		// 发送协议
		NetSocketManager.getInstance().sendData(socketInfo);
		receiveGivingBankruptcy();
	}

	private void receiveGivingBankruptcy(){
		// 定义接受数据的协议
		short[][] parseProtocol = { { LS_TRANSIT_LOGON, MSUB_CMD_PRESENT_ACK } };
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			public boolean doReceive(NetSocketPak netSocketPak) {
				try {
					TDataInputStream tdis = netSocketPak.getDataInputStream();
					InputStreamReader in = new InputStreamReader(tdis);
					BufferedReader buffer = new BufferedReader(in);
					String strLine;
					String data = ""; // 平凑的xml字符串
					while (((strLine = buffer.readLine()) != null)) {
						data += strLine;
					}
					/********************************************
					 * <result status=”0” bean=”100” msg=”赠送提示内容”/>
					 * 其中status=0成功，失败
					 *******************************************/
					Handler handler=FiexedViewHelper.getInstance().cardZoneFragment.cardZoneHandler;
					Message msg=handler.obtainMessage();
					msg.what=HANDLER_BANKRUPTCY;
					msg.obj=data;
					handler.sendMessage(msg);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				return true;
			}
		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		nPListener.setOnlyRun(false);
	}


	/**初始化系统消息的一些参数*/
	private void resetSystemParam(){
		array=null;
		currentSystemNum = 0;
		isRecordSyste = false;
	}
	private void resetPersonParam(){
		pArray=null;
		currentPersonNum = 0;
		isRecordPerson = false;
	}

	//当前获取系统消息数 
	int currentSystemNum=0;
	NoticeSystemInfo[] array=null;
	boolean isRecordSyste=false;

	//当前获取个人消息数
	int currentPersonNum=0;
	NoticePersonInfo[] pArray=null;
	boolean isRecordPerson=false;


	@Override
	public void requestSystemMessage(int userid) {
		resetSystemParam();
		resetPersonParam();
		TDataOutputStream tdos = new TDataOutputStream(false);
		tdos.writeInt(userid);
		//tdos.writeUTF(mobileCode);
		//mobileCode = getChannelID() + MOBILE_CODE_TAIL;  "02ANDROID1"    //android客户端标识
		tdos.writeUTFByte(AppConfig.channelId+"02ANDROID1"); 
		tdos.writeUTFByte("0");

		/* 客户端协议版本,1.6.3新增
		 * wanghj
		 * ((x<<20) + (y<<10) + z)	客户端协议版本号规则
			例如：1.6.3 x=1 y=6 z=3 再接上面的公式转换得到一个整型数值*/
		int CmdVer = Util.getProtocolCode(AppConfig.ZONE_VER);
		
		tdos.writeInt(CmdVer);     //客户端协议版本

		/*是否计费用户，1.6.3新增*/
		int statusBit = HallDataManager.getInstance().getUserMe().statusBit;
		int switchBit = 1<<24;   //计费用户标识位第25位
		int charge = ((statusBit & switchBit) == 0) ? 0 : 1; 
		tdos.writeByte(charge);    //是否计费用户

		NetSocketPak socketInfo = new NetSocketPak(LS_TRANSIT_LOGON,MSUB_SYSMSG_REQUEST, tdos);
		// 定义接受数据的协议
		short[][] parseProtocol = { 
				{ LS_TRANSIT_LOGON,MSUB_SYSMSG_ROLL_MSG },
				{ LS_TRANSIT_LOGON,MSUB_SYSMSG_LEAVE_WORD }
		};
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			public boolean doReceive(NetSocketPak netSocketPak) {
				TDataInputStream tdis = netSocketPak.getDataInputStream();
				tdis.setFront(false);

				if (netSocketPak.getSub_gr() == MSUB_SYSMSG_ROLL_MSG) {
					final int total = tdis.readShort(); //总数
					final int count = tdis.readShort(); //当前获取数目 

					if (count >0){
						if (array == null && currentSystemNum == 0) { // 尚未有数组对象情况下
							array = new NoticeSystemInfo[total];
						}
						// 累计接受到数据到数组中
						for (int i = 0; i < count; i++) {
							if (currentSystemNum + i < array.length) {
								array[currentSystemNum + i] = new NoticeSystemInfo(tdis);
							}
						}

						currentSystemNum += count; // 积累保存到全局变量，记录当前返回累计数目
						if (currentSystemNum >= total) {
							tdis.readByte();  //byte  staus=tdis.readByte(); –增加状态位，0今天没有请求过，1-今天已经请求过
							// 读取完毕，交与主线程显示（同时恢复变量）
							//NetSocketManager.getInstance().removePrivateListener(this);
							currentSystemNum = 0;
							isRecordSyste = true;
							//发送handler保存数据
							NoticeSystemProvider.getInstance().init();
							NoticeSystemProvider.getInstance().addSysytemArray(array);
						}
					}
					//---------------------------------------------------------------------------------------
				}else if(netSocketPak.getSub_gr() == MSUB_SYSMSG_LEAVE_WORD){  //个人消息
					final int ptotal = tdis.readShort(); //总数
					final int pcount = tdis.readShort(); //当前获取数目 
					if (pcount <= 0) {

					}else if(!AppConfig.isReceive){
						
						if (pArray == null && currentPersonNum == 0) { // 尚未有数组对象情况下
							pArray = new NoticePersonInfo[ptotal];
						}
						// 累计接受到数据到数组中
						for (int i = 0; i < pcount; i++) {
							if (currentPersonNum + i < pArray.length) {
								pArray[currentPersonNum + i] = new NoticePersonInfo(tdis);
							}
						}
						currentPersonNum += pcount; // 积累保存到全局变量，记录当前返回累计数目
						if (currentPersonNum >= ptotal) {
							// 读取完毕，交与主线程显示（同时恢复变量）
							tdis.readByte();  //Staus –增加状态位，0今天没有请求过，1-今天已经请求过
							//NetSocketManager.getInstance().removePrivateListener(this);
							AppConfig.isReceive = true;     //状态改变应等接收完毕才改变，不然程序跑不进来这里了
							currentPersonNum = 0;
							isRecordPerson = true;
							//发送handler
							NoticePersonProvider.getInstance().init();
							NoticePersonProvider.getInstance().addPersonArray(pArray);
						}
					} else if(pcount == 1 && AppConfig.isReceive){
						tdis.readByte();
						String msg = tdis.readUTFShort();
						com.mykj.game.utils.Log.e("test__", msg);
						NoticePersonProvider.getInstance().addPersonInfoStr(msg);
					} 
					//类型，内容，时间
					//msgManager::instance()->writeXml("2", iter->msgContent.c_str(), "");
				}
				return true;
			}
		};

		nPListener.setOnlyRun(false);
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		// 发送协议
		NetSocketManager.getInstance().sendData(socketInfo);
		socketInfo.free();
	}







}
