package com.mykj.andr.ui.widget;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;

import com.mingyou.distributor.NetPrivateListener;
import com.mingyou.distributor.NetSocketPak;
import com.mykj.andr.model.AllNodeData;
import com.mykj.andr.model.NodeData;
import com.mykj.andr.net.NetSocketManager;
import com.mykj.andr.provider.NewCardZoneProvider;
import com.mykj.andr.ui.widget.Interface.InvokeViewCallBack;
import com.mykj.comm.io.TDataInputStream;
import com.mykj.comm.io.TDataOutputStream;
import com.MyGame.Midlet.R;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.Log;


/*****
 * 
 * @ClassName: CardZoneDataListener
 * @Description: 请求并监听分区数据
 * @author zhanghuadong
 * @date 2012-11-1 下午04:56:12
 * 
 */
public class CardZoneDataListener {
	private static final String TAG="CardZoneDataListener";
	/** 房间列表主协议 **/
	public static final short MDM_LOBBY = 22;

	/** 子协议 */
	public static final short MSUB_CMD_REQ_SUBNODE_LIST_EX = 1;

	/** 节点列表数据返回（ //返回子节点列表） **/
	public static final short MSUB_CMD_ACK_SUBNODE_LIST = 2;


	/**请求游戏完整节点列表*/
	public static final short SUB_CMD_WHOLE_NODE_LIST_REQ = 5;
	/**返回完整节点列表*/
	public static final short  SUB_CMD_WHOLE_NODE_LIST_RESP = 6;     

	/**请求单个节点信息*/
	public static final short  SUB_CMD_SINGLE_NODE_LIST_REQ = 7;

	/**返回单个节点信息*/
	public static final short  SUB_CMD_SINGLE_NODE_LIST_RESP = 8;   

	/**请求带玩法属性的节点列表请求*/
	public static final short  SUB_CMD_NODE_LIST_WITH_PLAYID_REQ = 13;

	/**请求带玩法属性的节点列表返回*/
	public static final short  SUB_CMD_NODE_LIST_WITH_PLAYID_RESP = 14;

	/** 获取节点列表成功 **/
	public static final int GET_GAME_NODES_SUCCESS = 20;
	/** 获取节点列表失败 */
	public static final int GET_GAME_NODES_FAIL = 21;

	/** 1.5.0-1.5.2获取所有节点列表成功 */
	public static final int GET_ALL_NODES_SUCCESS=22;
	/** 1.5.0-1.5.2获取所有节点列表失败*/
	public static final int GET_ALL_NODES_FAIL=23;


	/** 1.5.0-1.5.2获取所有节点列表成功 */
	public static final int GET_SUB_NODES_SUCCESS=24;
	/**1.5.0-1.5.2 获取所有节点列表失败*/
	public static final int GET_SUB_NODES_FAIL=25;

	/**1.5.3获取所有节点列表静态数据成功*/
	public static final int GET_THREE_PLAY_MAIN_NODES_SUCCESS=26;
	/**1.5.3获取所有节点列表静态数据失败*/
	public static final int GET_THREE_PLAY_MAIN_NODES_FAIL=27;

	/**1.5.3获取所有节点列表动态数据成功*/
	public static final int  GET_THREE_PLAY_SUB_NODES_SUCCESS=28;
	/**1.5.3获取所有节点列表动态数据失败*/
	public static final int  GET_THREE_PLAY_SUB_NODES_FAIL=29;

	/** 标记全部为父节点（第一级卡片层节点）1 **/
	private final int CARD_ZONE_PARENT = 1;
	/** 标记全部为子节点（卡片层节点子节点） 0 **/
	private final int CARD_ZONE_CHILDREN = 0;

	/** 大厅版本类型(cocos2d-x中为3) **/
	public static final byte LOBBYTYPE = 3;

	public static  byte  GZIP=1;//0//1   //压缩方式 0-不压缩，1-压缩
	
	public static final int VERSION_1=1;   //列表协议第一版，每个节点单独请求
	public static final int VERSION_2=2;   //列表协议第二版，支持所有列表数据一次请求完成，本地缓存列表数据
	public static final int VERSION_3=3;   //列表协议第三版，支持斗地主3种玩法，列表数据zip压缩，本地缓存列表数据
	
	public static final int NODE_DATA_PROTOCOL_VER=VERSION_3;

	// --------------------------------------------------------------------------------------------------------
	private static CardZoneDataListener instance = null;

	private Activity mAct;

	private CardZoneDataListener(Activity ctx) {
		this.mAct = ctx;
	}

	public static CardZoneDataListener getInstance(Activity ctx) {
		if (instance == null) {
			instance = new CardZoneDataListener(ctx);
		}
		return instance;
	}

	/***
	 * @Title: invokeCardZoneData
	 * @Description: 发送请求获得分区数据
	 * @version: 2012-11-1 下午05:02:39
	 */
	public void invokeReveiveCardZoneData() {		
		switch(NODE_DATA_PROTOCOL_VER){
		case VERSION_1:
			getGameNodes(0);//列表协议第一版，每个节点单独请求
			break;
		case VERSION_2:
			getGameAllNodes();//列表协议第二版，支持所有列表数据一次请求完成，本地缓存列表数据
			break;
		case VERSION_3:
			getAllNodeWithThreePlay();//列表协议第三版，支持斗地主3种玩法，列表数据zip压缩，本地缓存列表数据
			break;
		default:
			break;
		}
	}

	/**
	 * 请求实时房间数据，如，房间名称，游戏人数
	 * @param nodeId
	 */
	private void reqRealTimeRoomData(final int nodeId){
		//发送服务器请求
		TDataOutputStream dos = new TDataOutputStream(false);
		dos.writeInt(nodeId);		
		dos.writeShort((short) AppConfig.gameId);	
		dos.writeByte(LOBBYTYPE);

		NetSocketPak data = new NetSocketPak(MDM_LOBBY, SUB_CMD_SINGLE_NODE_LIST_REQ, dos);
		NetSocketManager.getInstance().sendData(data);


		// 定义接受数据的协议
		short[][] parseProtocol = { { MDM_LOBBY, SUB_CMD_SINGLE_NODE_LIST_RESP } };
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				// 解析接受到的网络数据
				TDataInputStream tdis = netSocketPak.getDataInputStream();

				AllNodeData.getInstance(mAct).updateRoomData(tdis);
				if(AllNodeData.getInstance(mAct).isSubRecFinish()){
					Message msg=cardZoneDataHandler.obtainMessage();
					switch(CardZoneDataListener.NODE_DATA_PROTOCOL_VER){
					case CardZoneDataListener.VERSION_1://列表协议第一版，每个节点单独请求
						break;
					case CardZoneDataListener.VERSION_2://列表协议第二版，支持所有列表数据一次请求完成，本地缓存列表数据
						msg.what=GET_SUB_NODES_SUCCESS;
						break;
					case CardZoneDataListener.VERSION_3://列表协议第三版，支持斗地主3种玩法，列表数据zip压缩，本地缓存列表数据
						msg.what=GET_THREE_PLAY_SUB_NODES_SUCCESS;
						break;
					default:
						break;
					}
					cardZoneDataHandler.sendMessage(msg);					
				}

				return true;
			}
		};
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		nPListener.setOnlyRun(false);
	}


	/** 当前遍历到的父节点索引(第一层节点) ***/
	int currentIndex = 0;

	/** 当前传入的ParentID **/
	int currentParentID = 0;

	/** 第一层节点数(父节点数) **/
	int totolTopNum = 0;

	/**
	 * ParentID INT(4--字节) 父节点 （0—表示取根节点下的子节点） GameID WORD(2--字节) 游戏ID
	 * （0—表示取所有游戏） LobbyType BYTE(1--字节) 大厅版本类型： 0 – 免费游戏版（默认） 1 – G+包版
	 * 
	 * @Title: getGameNodes
	 * @Description:4.6.1.1.请求游戏节点信息
	 * @param ParentID父节点ID
	 * @param GameID游戏ID
	 *            ，斗地主为100
	 * @param LobbyType大厅版本类型
	 * @version: 2012-9-3 下午04:07:07
	 */
	private void getGameNodes(int ParentID) {
		currentParentID = ParentID;
		TDataOutputStream dos = new TDataOutputStream(false);
		dos.writeInt(currentParentID);
		dos.writeShort((short) AppConfig.gameId);
		dos.writeByte(LOBBYTYPE);
		NetSocketPak data = new NetSocketPak(MDM_LOBBY, MSUB_CMD_REQ_SUBNODE_LIST_EX, dos);
		NetSocketManager.getInstance().sendData(data);

		// 定义接受数据的协议
		short[][] parseProtocol = { { MDM_LOBBY, MSUB_CMD_ACK_SUBNODE_LIST } };
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				// 解析接受到的网络数据
				TDataInputStream tdis = netSocketPak.getDataInputStream();
				tdis.setFront(false);
				final int num = tdis.readShort(); // 节点个数
				if (num > 0) {
					NodeData[] array = new NodeData[num];
					for (int i = 0; i < num; i++) {
						if (currentParentID == 0) {
							array[i] = new NodeData(tdis);
							totolTopNum = num;
						} else
							array[i] = new NodeData(tdis);
					}
					// 读取完毕操作
					// 发送一个Handler信息到UI
					Message msg = cardZoneDataHandler.obtainMessage(GET_GAME_NODES_SUCCESS);
					msg.obj = array;
					msg.arg1 = currentParentID == CARD_ZONE_CHILDREN ? CARD_ZONE_PARENT : CARD_ZONE_CHILDREN; // 标记当前是否为父节点(1为一级父节点，0为子节点)
					cardZoneDataHandler.sendMessage(msg);

				}
				return true;
			}
		};
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		nPListener.setOnlyRun(false);
	}




	/**
	 * 获取游戏的所有房间节点
	 * 第一步，先从缓存文件读取
	 * 缓存文件没有，发送协议获取
	 */
	private void getGameAllNodes(){
		String ver=AllNodeData.getInstance(mAct).getVersion();
		TDataOutputStream dos = new TDataOutputStream(false);
		dos.writeShort((short) AppConfig.gameId);		
		dos.writeByte(LOBBYTYPE);
		dos.writeUTF(ver, 32);//32字节

		NetSocketPak data = new NetSocketPak(MDM_LOBBY, SUB_CMD_WHOLE_NODE_LIST_REQ, dos);
		// 定义接受数据的协议
		short[][] parseProtocol = { { MDM_LOBBY, SUB_CMD_WHOLE_NODE_LIST_RESP } };
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				// 解析接受到的网络数据
				TDataInputStream tdis = netSocketPak.getDataInputStream();
				int res=AllNodeData.getInstance(mAct).addTDataInputStream(tdis);
				if(AllNodeData.getInstance(mAct).isRecFinish()){

					if(res==AllNodeData.NODE_DATA_SOCKET){
						Message msg = cardZoneDataHandler.obtainMessage(GET_ALL_NODES_SUCCESS);							
						cardZoneDataHandler.sendMessage(msg);		
						AllNodeData.getInstance(mAct).saveNodeDataToFile();
					}else if(res==AllNodeData.NODE_DATA_LOCAL){
						reqRealTimeRoomData(AllNodeData.getInstance(mAct).getRootID());
					}
				}

				return true;
			}
		};
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		NetSocketManager.getInstance().sendData(data);
		nPListener.setOnlyRun(false);
	}

	private static  byte mCurPacket=0;

	/**
	 * 三玩法列表请求协议
	 */
	private void getAllNodeWithThreePlay(){
		String ver=AllNodeData.getInstance(mAct).getVersion();
		TDataOutputStream dos = new TDataOutputStream(false);
		dos.writeShort((short) AppConfig.gameId);		
		dos.writeByte(LOBBYTYPE);
		dos.writeUTF(ver, 32);//32字节
		dos.writeByte(GZIP);  //压缩方式

		NetSocketPak data = new NetSocketPak(MDM_LOBBY, SUB_CMD_NODE_LIST_WITH_PLAYID_REQ, dos);
		// 定义接受数据的协议
		short[][] parseProtocol = { { MDM_LOBBY, SUB_CMD_NODE_LIST_WITH_PLAYID_RESP } };
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				// 解析接受到的网络数据
				TDataInputStream compressed = netSocketPak.getDataInputStream();
				TDataInputStream unCompressed=null;
				if(GZIP==1){
					byte totalPacket=compressed.readByte();
					byte currentPacket=compressed.readByte();
					if(totalPacket==0){
						compressed.reset();
						parseNodeData(compressed);
						return true;
					}
					mCurPacket+=currentPacket;
					for(int i=0; i<currentPacket; i++){
						unCompressed=zipInflate(compressed); //zip解压
						parseNodeData(unCompressed);
					}
					if(totalPacket==mCurPacket){
						Log.v(TAG, "NodeData receive complete!");
					}
				}else{
					unCompressed=compressed;
					parseNodeData(unCompressed);
				}
				return true;
			}
		};
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		NetSocketManager.getInstance().sendData(data);
		nPListener.setOnlyRun(false);

	}


	private void parseNodeData(TDataInputStream tdis){
		int res=AllNodeData.getInstance(mAct).addTDataInputStream(tdis);
		if(AllNodeData.getInstance(mAct).isRecFinish()){
			if(res==AllNodeData.NODE_DATA_SOCKET){
				Message msg = cardZoneDataHandler.obtainMessage(GET_THREE_PLAY_MAIN_NODES_SUCCESS);							
				cardZoneDataHandler.sendMessage(msg);		
				AllNodeData.getInstance(mAct).saveNodeDataToFile();
			}else if(res==AllNodeData.NODE_DATA_LOCAL){
				reqRealTimeRoomData(AllNodeData.getInstance(mAct).getRootID());
			}
		}
	}







	public static TDataInputStream zipInflate(TDataInputStream tdis){
		TDataInputStream unPress=null;
		int buffLen = tdis.readInt();
		byte[] bytes=tdis.readBytes(buffLen);
		Inflater inflater=new Inflater(false);

		InflaterInputStream in = new InflaterInputStream(new ByteArrayInputStream(bytes),inflater);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int count;
		try {
			while ((count = in.read(buffer)) != -1) {
				out.write(buffer, 0, count);
			}
		} catch (IOException e) {
			Log.e(TAG, "列表数据GZIP解压错误!!!");
		}
		unPress=new TDataInputStream(out.toByteArray(),false);
		return unPress;
	}




	@SuppressLint("HandlerLeak")
	Handler cardZoneDataHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GET_GAME_NODES_SUCCESS: // 节点列表成功(自由对战，赚话费专区，智运会专区，好友约占区，及其下的每一节点（连续请求）)
				NodeData[] datas = (NodeData[]) msg.obj;
				if (msg.arg1 == CARD_ZONE_PARENT) {
					if (datas != null && datas.length > 0) {

						NodeData rootNode=new NodeData();
						rootNode.ID=8;
						rootNode.Name=mAct.getResources().getString(R.string.ddz_room_tuijian);
						NewCardZoneProvider.getInstance().outerNetinitCardZoneData(rootNode);

						// 顶层节点(自由对战，赚话费专区，智运会专区，好友约占区)
						for (NodeData data : datas) {
							NewCardZoneProvider.getInstance().outerNetinitCardZoneData(data);
							// 再次请求发送游戏列表协议
							getGameNodes(data.ID);
						}
					}
				} else { // 二级列表
					if (datas != null && datas.length > 0) {
						for (NodeData data : datas) {
							NewCardZoneProvider.getInstance().outerNetinitListData(data);
						}
					}

					currentIndex++;
					if (currentIndex == totolTopNum) {
						Log.e("TAG", "请求分区列表成功，跳转。");
						NewCardZoneProvider.getInstance().outerNetinitRecommend();
						// 二级列表全部接受完成后跳转界面
						skipToCardZoneView();
						currentIndex = 0;
					}
				}
				break;
			case GET_ALL_NODES_SUCCESS: 
			case GET_SUB_NODES_SUCCESS:
				NewCardZoneProvider.getInstance().initCardZoneProvider();
				skipToCardZoneView();
				break;

			case GET_THREE_PLAY_MAIN_NODES_SUCCESS: 
			case GET_THREE_PLAY_SUB_NODES_SUCCESS:
				
				skipToCardZoneView();
				break;
			default:
				break;
			}
		}
	};

	InvokeViewCallBack invokeCallBack = null;

	public void setInvokeViewCallBack(InvokeViewCallBack mInvokeView) {
		this.invokeCallBack = mInvokeView;
	}

	/***
	 * @Title: showToCocods2dView
	 * @Description: 切换到cocos2d-x界面
	 * @return
	 * @version: 2012-12-12 下午06:34:42
	 */
    /*private boolean skipToCocods2dView() {
		if (invokeCallBack != null) {
			invokeCallBack.skipToCocods2dView();
			return true;
		}
		return false;
	}*/

	/***
	 * @Title: showToCardZoneView
	 * @Description:切换到分区界面
	 * @return
	 * @version: 2012-12-12 下午06:35:08
	 */
	private boolean skipToCardZoneView() {
		if (invokeCallBack != null) {
			invokeCallBack.skipToCardZoneView();
			return true;
		}
		return false;
	}

	/***
	 * @Title: showToLoginView
	 * @Description: 切换到登录界面
	 * @return
	 * @version: 2012-12-12 下午06:35:27
	 */
    /*private boolean skipToLoginView() {
		if (invokeCallBack != null) {
			invokeCallBack.skipToLoginView();
			return true;
		}
		return false;
	}*/

	/***
	 * @Title: hallComeInSuccess
	 * @Description: 从大厅进来
	 * @return
	 * @version: 2012-12-13 下午05:07:08
	 */
	public boolean hallComeInSuccess() {
		if (invokeCallBack != null) {
			invokeCallBack.hallComeInSuccess();
			return true;
		}
		return false;
	}

}
