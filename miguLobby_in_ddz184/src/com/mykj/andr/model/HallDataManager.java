package com.mykj.andr.model;

import com.mykj.game.utils.Log;





/****
 * 
 * @ClassName: HallDataManager
 * @Description: 大厅相关数据管理器(单例类)
 * @author zhanghuadong
 * @date 2012-9-4 下午02:25:24
 *
 */
public class HallDataManager {

	static HallDataManager _instance;
	private HallDataManager(){}
	public static HallDataManager getInstance(){
		if(_instance==null)
			_instance=new HallDataManager();
		return _instance;
	}

	//--------------------------DDZ快捷购买保存到的变量------------------
	private int proId=-1;
	private String pmessage="";
	public void setProId(int mProId){
		this.proId=mProId;
	}
	public int getProId(){
		int pid = this.proId;
		this.proId = -1;
		return pid;
	}

	public void setProMessage(String pMsg){
		this.pmessage=pMsg;
	}
	public String getProMessage(){
		String msg=this.pmessage;
		this.pmessage="";
		return msg;
	}





	//===================================是否点击快速游戏（快速游戏不进入房间显示）====================
	private boolean m_ClickQuickGame=false;
	/***
	 * @Title: setClickQuickGame
	 * @Description: 设置是否快速游戏
	 * @param isClickQuickGame
	 * @version: 2012-9-28 下午06:01:37
	 */
	public void setClickQuickGame(boolean isClickQuickGame){
		this.m_ClickQuickGame=isClickQuickGame;
	}
	public boolean getClickQuickGame(){
		return m_ClickQuickGame;
	}

	private boolean m_gameSitDown=false;
	/***
	 * @Title: setGameSitDown
	 * @Description: 设置是否坐下
	 * @param sitdown
	 * @version: 2012-11-16 下午01:55:34
	 */
	public void setGameSitDown(boolean sitdown){
		this.m_gameSitDown=sitdown;
	}
	public boolean getGameSitDown(){
		return this.m_gameSitDown;
	} 


	//=================================获取场地类型================================
	public boolean getMatchGameType(){ 
		//-------------------------------------------------------------
		//获取场地类型（比赛场还是普通场
		RoomData roomData=HallDataManager.getInstance().getCurrentRoomData();
		if(roomData!=null){
			int GameType =roomData.GameType;
			if (GameType < 0){
				return false;
			}
			if( (GameType & RoomData.GAME_GENRE_DONGGUAN_MATCH) == RoomData.GAME_GENRE_DONGGUAN_MATCH )
				return true;
			else
				return false;
		}
		return false;

	}

	//===================================设置并获取当前房间===========================================
	RoomData _RoomData;

	/****
	 * @Title: getCurrentRoomData
	 * @Description: 获得用户进入的房间
	 * @return
	 * @version: 2012-9-21 下午03:32:17
	 */
	public RoomData getCurrentRoomData(){
		return _RoomData;
	}
	/**
	 * @Title: setCurrentRoomData
	 * @Description: 保存用户当前进入的房间
	 * @param currentRoom
	 * @version: 2012-9-21 下午03:31:23
	 */
	public void setCurrentRoomData(RoomData currentRoom){
		this._RoomData=currentRoom;
	}
	//================================保存房间配置信息====================================================
	RoomConfigData _mRoomConfigData;
	public RoomConfigData getRoomConfigData(){
		if(_mRoomConfigData!=null){
			return _mRoomConfigData;
		}
		return null;
	}
	public void setRoomConfigData(RoomConfigData mRoomConfigData){
		this._mRoomConfigData=mRoomConfigData;
	}

	//-----------------设置并获取当前节点----------------
	NodeData _NodeData;
	public NodeData getCurrentNodeData(){
		return _NodeData;
	}
	public void setCurrentNodeData(NodeData nodeData){
		this._NodeData=nodeData;
	} 
	//---------------------------跳转到cocos2d-x前，所在的分区索引----------------------------------------
	int selection_index=0;
	public void setSelectionIndex(int sindex){
		this.selection_index=sindex;
	}

	public int getSelectionIndex(){
		return this.selection_index;
	}




	//-------------------获取用户信息---------------------
	/** 记录我的用户信息 */
	private UserInfo userMe = new UserInfo();
	/**
	 * 获得我的用户信息
	 * 
	 * @return the userMe
	 */
	public UserInfo getUserMe(){
		return userMe;
	}

	public void setUserMeBean(int bean){
		userMe.bean = bean;
	}

	/**
	 * 设置我的用户信息对象
	 * 
	 * @param userMe
	 *        the userMe to set
	 */
	public void setUserMe(UserInfo userMe){
		this.userMe = userMe;
	} 

	/**
	 * 设置当前用户头像
	 * @param id
	 */
	public void setUserHead(short id){
		this.userMe.setFaceId(id);
	}
	
	VipData _VipData;
	public void setVipData(VipData vipData){
		this._VipData = vipData;
	}
	
	public VipData getVipData(){
		return _VipData;
	}
	
	
	/**无**/
	public static final int NOT=0;
	/**报名**/
	public static final int ATTEND=1;
	/**退赛**/
	public static final int EXIT=2;

	/**报名请求类型：0：无  1：报名 2 ：退赛*/
	int attendeReqType=ATTEND;
	/**获得报名请求类型：0：无  1：报名 2 ：退赛*/
	public int getAttendeReqType() {
		return attendeReqType;
	}
	/**报名请求类型：0：无  1：报名 2 ：退赛*/
	public void setAttendeReqType(int attendeReqType) {
		this.attendeReqType = attendeReqType;
		Log.e("HallData", "attendeReqType = "+attendeReqType);
	}




}
