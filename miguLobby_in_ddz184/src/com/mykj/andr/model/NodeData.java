package com.mykj.andr.model;

import java.io.Serializable;

import com.mykj.comm.io.TDataInputStream;
import com.mykj.comm.io.TDataInputStream.MDataMark;


/***
 * 
 * @ClassName: NodeData
 * @Description: 节点信息实体类，C++中以结构体出现
 * @author zhanghuadong
 * @date 2012-8-31 下午04:24:01
 * 
 */
public class NodeData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	/******************以下为静态常量**********************/
	// 节点类型
	/** 根节点 **/
	public static final short NODE_ROOT = 100;

	/** 普通节点 **/
	public static final short NODE_NORMAL = 101;

	/** 房间节点 **/
	public static final short NODE_ROOM = 102;

	/** 旁观桌子节点 **/
	public static final short NODE_TABLE = 107;

	/** 旁观分区节点 **/
	public static final short NODE_LOOK = 108;

	/** 报名节点 **/
	public static final byte NODE_ENROLL = 109;

	/** 约战节点 **/
	public static final byte NODE_CHALLENGE = 111;

	/** 美女视频节点 **/
	public static final byte NODE_MM_VIDEO = 113;
	
	
	/**0-不显示**/
	public static final byte NODE_NO_SHOW=0;
	/**1-点4击节点显示**/
	public static final byte NODE_SHOW=1; 
	/**2-进入房间显示**/
	public static final byte NODE_IN_ROOM_SHOW=2;

	/**无数据**/
	public static final int NONE=0;
	/**人数**/
	public static final int ONUSER=1;
	/**（忙、满、闲）**/
	public static final int BUZY=2;

	private static final short startType = NODE_ROOT;

	private static final short endType = NODE_CHALLENGE;


	/*****************以下协议房间节点数据***********************/

	public int ID; // 节点标识
	public int ParentID; // 父节点
	/**
	 * 100--根节点 ,101--普通节点, 102--房间节, 103--网络站点节点 ,104--exe节点 ,105--拔棋赛节点
	 * 106--网页游戏专用房间，大厅中只显示的节点, 107-桌子节点 A vs B ,108-旁观分区节点 ,109-报名节点
	 */
	public byte Type; // 节点类型

	public byte SubType; // 0--子节点非房间节点 1-子节点属房间节点
	public byte SortID; // 排序编号

	public byte NameLen; // 节点名称长度
	public String Name; // 名称


	public byte StateType; // 0 – Data无数据 1- 人数缓冲 2- 字符串(忙、满、闲) 3-桌子状态
	public byte StateLen; // 后接数据长度
	/**
	 * StateType = 0 表示无数据 StateType =1 这位 DWORD 4 字节 人数 StateType =2
	 * 为字符串（忙、满、闲）utf8 StateType =3 游戏状态 BYTE 1字节, 0表示未开始 1表示已经开始游戏
	 */
	public int onLineUser; //在线人数
	public String StateData; //忙、满、闲状态
	public byte lookTableState = -1; //游戏状态


	public short TableID; // 桌子编号 (默认为-1 表示无效) 新加字段 2011-8-4

	public long dataID; // 标识（默认为-1表示无效）新加字段 2011-10-18

	/** 节点信息显示类型 **/
	public byte cbShowType=-1; // 显示类型,0-不显示1-点击节点显示2-进入房间显示

	public short wNoteLen; // 节点说明信息长度,无说明数据
	public String noteText; // 节点说明信息

	public byte IconID=0; // 图标编号,默认0没有图标
	public int TextColor; // 文本颜色
	public short ExtType; // 附加属性可同时表示16种状态，详见下面说明

	public byte LimitCount;
	public LimitData[] limits = null;

	public byte rmLimitCount;

	/**协议新增房间准入条件*/
	public RmLimit[] rmLimits=null;

	public byte Recommend; //服务器推荐位，1表示推荐，0不推荐， 所有房间只有一个推荐房间	
	public byte PlayID;  //玩法ID

	/**新平台A计划添加字段*/
	public byte RoomTagCount;  //房间节点标签数 （数据只有SubType = 1时才会被填充）
	public RoomTag[] RoomTags; //房间标签
	public byte NewRecommendFlag;   //新的推荐标识（目前只用于美女猜猜猜） 0:不推荐 1:推荐
	public byte NewRecommendVisible;//新的推荐节点是否在列表中显示（目前只用于美女猜猜猜）0:不显示 1:显示


	/***********parseNoteText(Note)分析得出*********/
	/**主标题**/
	public String MTContent="";
	/**副标题**/
	public String STContent="";
	/**详细说明**/
	public String BD1Content="";
	public String BD2Content="";

	/**底分10乐豆，开赛时间等说明**/
	public String GRContent="";
	/**报名条件**/
	public String GTContent="";
	public String GSContent="";


	/***************************************************/
	public NodeData(){}

	public NodeData(byte[] array){
		this(new TDataInputStream(array));
	}

	public NodeData(TDataInputStream dis){
		if (dis == null) {
			return;
		}

		dis.setFront(false);

		final int len=dis.readShort();
		MDataMark mark=dis.markData(len);


		ID = dis.readInt(); // 节点标识
		ParentID = dis.readInt(); // 父节点
		Type = dis.readByte(); // 节点类型

		SubType = dis.readByte(); // 子节点类型
		SortID = dis.readByte(); // 排序编号

		NameLen= dis.readByte();//名称长度
		Name=dis.readUTF(NameLen);


		StateType = dis.readByte(); // 后接数据类型
		StateLen = dis.readByte(); // 后接数据长度
		switch (StateType) {
		case 0: // 无数据
			break;
		case 1: // DWORD 4个 字节
			int num = dis.readInt();
			onLineUser=num;
			//StateData = "(" + num + "人在线)";
			break;
		case 2: // StateLen utf-8
			StateData = "(" + dis.readUTF(StateLen) + ")";  //（忙、满、闲）
			break;
		case 3:
			lookTableState = dis.readByte(); // 游戏状态 BYTE 1字节, 0表示未开始
			break;
		default:
			break;
		}


		TableID = dis.readShort(); // 桌子编号 (默认为-1 表示无效)
		dataID = dis.readLong(); // 报名ID （ C++那边使用nodeData，我这里是猜测为对应dataID：//标识（默认为-1表示无效）用于娱乐比赛场登录服务器所用, by vincent）
		cbShowType = dis.readByte(); // 房间信息显示类型，显示类型,0-不显示1-点击节点显示2-进入房间显示

		wNoteLen=dis.readShort();
		noteText = dis.readUTF(wNoteLen);
		//解析noteText
		parseNoteText(noteText);

		IconID=dis.readByte(); // 图标编号,默认0没有图标-----说是要配角标为卡片图标
		TextColor=dis.readInt();// 文本颜色
		ExtType=dis.readShort(); //附加属性可同时表示16种状态

		LimitCount = dis.readByte(); // 限制条件个数
		if (LimitCount > 0) {
			limits = new LimitData[LimitCount];
			for (int i = 0; i < LimitCount; i++) {
				limits[i] = new LimitData(dis);
			}
		}

		rmLimitCount= dis.readByte();
		if (rmLimitCount > 0) {
			rmLimits = new RmLimit[rmLimitCount];
			for (int i = 0; i < rmLimitCount; i++) {
				rmLimits[i] = new RmLimit(dis);
			}
		}

		Recommend=dis.readByte();//服务器房间推荐

		PlayID=dis.readByte();  //获取节点玩法ID



		/**新平台A计划添加字段*/

		RoomTagCount=dis.readByte();  //房间节点标签数 （数据只有SubType = 1时才会被填充）

		if (RoomTagCount > 0) {
			RoomTags = new RoomTag[RoomTagCount];
			for (int i = 0; i < RoomTagCount; i++) {
				RoomTags[i] = new RoomTag(dis);
			}
		}

		NewRecommendFlag=dis.readByte();

		NewRecommendVisible=dis.readByte();

		dis.unMark(mark);
	}


	public final boolean isShowType() {
		if (Type >= startType && Type <= endType) {
			return true;
		}
		return false;
	}

	/**
	 * @Title: isHaveSubNode
	 * @Description: 节点是否包含子节点
	 * @return
	 * @version: 2011-11-9 上午11:37:14
	 */
	public boolean isHaveSubNode() {
		return SubType != 1 && Type != NODE_ENROLL;
	}




	public boolean isQuickGame(UserInfo user){
		boolean res=false;
		if ((ExtType & 0x0001) > 0) { // 支持快速游戏
			if (canEnter(user)) {
				res=true;
			}
		}
		return res;
	}





	private boolean canEnter(UserInfo user) {
		if (limits == null) {
			return true;
		}
		for (int i = 0; i < limits.length; i++) {
			if (limits[i].Type == 1) { // 话费卷

			} 
			/*else if (limits[i].Type == 2) {// 元宝
				if (user.yuanBao >= limits[i].Min && user.yuanBao <= limits[i].Max) {
					return true;
				}
			} */
			else if (limits[i].Type == 3) {// 乐豆
				if (user.bean >= limits[i].Min && user.bean <= limits[i].Max) {
					return true;
				}
			} else if (limits[i].Type == 4) {// 积分
				if (user.score >= limits[i].Min && user.score <= limits[i].Max) {
					return true;
				}
			}
		}
		return false;
	}


	//---------------------下面是字符串截取，用来解析noteText数据信息----------------------------------
	/***
	 * @Title: parseNoteText
	 * @Description:   获得标签内容
	 * @param noteText 解析标签
	 * @return
	 * @version: 2012-9-13 下午02:51:12
	 */
	protected void parseNoteText(String noteText){
		if(noteText ==null||noteText.length()<=0)
		{
			return;
		}
		MTContent=getTabContent(noteText.trim(),"<MT>","</MT>");
		STContent=getTabContent(noteText.trim(),"<ST>","</ST>");

		BD1Content=getTabContent(noteText.trim(),"<BD1>","</BD1>");
		BD2Content=getTabContent(noteText.trim(),"<BD2>","</BD2>");

		GRContent=getTabContent(noteText.trim(),"<GR>","</GR>");
		GTContent=getTabContent(noteText.trim(),"<GT>","</GT>");
		GSContent=getTabContent(noteText.trim(),"<GS>","</GS>");
	}


	/***
	 * @Title: getTabContent
	 * @Description: 获得标签内容
	 * @param source 源字符串
	 * @param sTag   开始标签
	 * @param eTag   结束标签
	 * @return
	 * @version: 2012-9-13 下午02:51:12
	 */
	protected String getTabContent(String source,String sTag,String eTag){

		int start = source.indexOf(sTag);
		int end =source.indexOf(eTag);

		if(start < 0 || end < 0)
			return "";
		if(end < start)
			return "";
		if(source.length() < 8)
			return "";
		int Len = end-(start+4);
		if(Len < 0 )
			return ""; 
		return source.substring(start+sTag.length(), end);
	}






}
