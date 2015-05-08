package com.mykj.andr.model;

import java.io.Serializable;

import com.mykj.comm.io.TDataInputStream;


/****
 * 
 * @ClassName: SystemMessage
 * @Description: 系统消息（主动下发）
 * @author 
 * @date 2012-9-26 下午05:42:12
 *
 */
public class SystemMessage implements Serializable {
	/**
	 * @Fields serialVersionUID: TODO(用一句话描述这个变量表示什么)
	 */
	private static final long serialVersionUID = 1L;
	
	public static final short     E_MT_STANDARD =0;			//标准消息类型 对应结构 tagMSG_CommonMessage
	public static final short	  E_MT_STANDARDPOPUP =1;			//标准弹出框消息类型 对应结构 tagMSG_Popup
	public static final short	  E_MT_HAVEURLPOPUP	=2;			//按钮带链接弹出框消息类型 对应结构 tagMSG_HaveUrlPopup
	public static final short     E_MT_COMMON_POPUP	=3;			//普通弹出消息,不带关闭操作 用于新破产送协议
			
	public static final short	  E_MT_PROPSHORTCUTBUY =101;		//道具快捷购买提示 对应结构 tagMSG_PropShortcutBuy
	public static final short	  E_MT_PROPUSE =102;		//道具使用提示,    对应结构 tagMSG_PropUse
	public static final short	  E_MT_REALNAME =201;	//实名认证提示 对应结构 tagMSG_HaveUrlPopup
    public static final short     E_MT_PROPSHORTCUTBUYNOBACK = 103;   //道具快捷购买提示，与tagMSG_CommonMessage一样，唯一差别是不返回分区
    public static final short     E_MT_WAITTIMEOUT		=204;  //比赛场组桌匹配超时
    public static final short     E_MT_VEDIO_NEED_VIP = 202;   //美女视频提示
	/** 事件 关闭大厅 */
	public static final byte TYPE_EVENT_CLOSE_HALL=0x0001;// 关闭大厅
	/** 事件 关闭房间 */
	public static final byte TYPE_EVENT_CLOSE_ROOM=0x0002;// 关闭房间
	/** 事件 关闭游戏 */
	public static final byte TYPE_EVENT_CLOSE_GAME=0x0004;// 关闭游戏
    /** 事件 快速开始游戏 */
	public static final byte TYPE_EVENT_QUICK_PLAY=0x0010;// 快速开始游戏
			
	/** 消息作用域 大厅 */
	public static final byte TYPE_SCOPE_HALL=0x0001;// 大厅
	/** 消息作用域 房间 */
	public static final byte TYPE_SCOPE_ROOM=0x0002;// 房间
	/** 消息作用域 桌子 */
	public static final byte TYPE_SCOPE_TABLE=0x0004;// 桌子

	/** 消息分类 系统消息 */
	public static final byte KIND_MSG_SYSTEM=1;// 系统消息
	/** 消息分类 系统消息 */
	public static final byte KIND_MSG_GM=2;// GM通知

	/** 消息展示方式 弹出框 */
	public static final byte TYPE_SHOW_POP=0x0001;
	/** 消息展示方式 模态弹出框 */
	public static final byte TYPE_SHOW_MODAL=0x0002;
	/** 消息展示方式 聊天框显示 */
	public static final byte TYPE_SHOW_CHAT=0x0004;
	/** 消息展示方式 走马灯显示 */
	public static final byte TYPE_SHOW_SCROLL=0x0008;

	/** 主体结构的数据的长度 exDataLen之前的数据 */
	public byte baseDataSize;

	/** 通过此ID确定ExData结构 */
	public short type;

	/** 消息类型 见eMSG_MsgKind */
	public byte kind;
	/** 消息事件(可多种并列)见eMSG_MsgEvent */
	public int event;
	/** 展示方式(可多种并列)见eMsgShowType */
	public int showType;
	/** 消息级别 数值越大优先级别越高（255为最高优先级别，没事别乱用） */
	public byte msgLevel;
	/** 作用域(可多种并列)见eMSG_MsgScope */
	public byte scope;
	/** 扩展数据长度 */
	public short exDataLen;

	/** -------------------HMSG_CommonMessage---------------- **/
	/** 文本大小 */
	public byte fontSize;
	/** 文本颜色 */
	public int fontColor;
	/** 标题长度 */
	public byte titleLen;
	/** 消息内容长度 */
	public short textLen;
	/** 标题 */
	public String title;
	/** 消息内容 */
	public String text;

	/** HMSG----------Popup------------- **/
	/** 图标ID */
	public short iconID;
	/** 延迟秒数 <=0 则无读秒 */
	public short delay;
	/** 消息框模式 1:OK 2:YES_NO 3:YES_?_NO */
	public byte mode;
	/** 中按钮内容长度 注：为0则无该位置按钮 */
	public byte mBtnLen;
	/** 右按钮内容长度注：为0则无该位置按钮 */
	public byte rBtnLen;
	/** 左按钮内容长度 注：为0则无该位置按钮 */
	public byte lBtnLen;
	/** 中按钮 */
	public String mBtn;
	/** 右按钮 */
	public String rBtn;
	/** 左按钮 */
	public String lBtn;
	/** ---------HMSG_HaveUrlPopup------------ **/
	/** 中键链接ID */
	public short mUrlID;
	/** 右键链接ID */
	public short rUrlID;
	/** 左键链接ID */
	public short lUrlID;
	/** 道具ID */
	public int propId;
	/** 用户超链接ID */
	public byte urlID;

	public short nodeId;
	
	public SystemMessage(TDataInputStream dis){
		if(dis==null){
			return;
		}
		dis.setFront(false);

		baseDataSize=dis.readByte(); 
		type=dis.readShort();
		exDataLen=dis.readShort();

		scope=dis.readByte();
		kind=dis.readByte();
		event=dis.readInt();
		showType=dis.readInt();
		msgLevel=dis.readByte();

		dis.skip(baseDataSize-dis.getMarkBytes(dis.getMark()));
		
		dis.getMark();

		switch(type){
		case E_MT_STANDARD:
		case E_MT_PROPSHORTCUTBUYNOBACK:
			readSTANDARD(dis);
			dis.skip(exDataLen-dis.getMarkBytes(dis.getMark()));
			readTextAndTitle(dis);
			break;
		case E_MT_STANDARDPOPUP:
		case E_MT_COMMON_POPUP:
		case E_MT_VEDIO_NEED_VIP:
			readSTANDARD(dis);
			readTYPE_STANDARD_POP(dis);
			dis.skip(exDataLen-dis.getMarkBytes(dis.getMark()));
			readTextAndTitle(dis);
			readBtnText(dis);
			break;
		case E_MT_WAITTIMEOUT:
			readSTANDARD(dis);
			readTYPE_STANDARD_POP(dis);
			nodeId=dis.readShort();
			dis.skip(exDataLen-dis.getMarkBytes(dis.getMark()));
			readTextAndTitle(dis);
			readBtnText(dis);
			break;
		case E_MT_HAVEURLPOPUP:
		case E_MT_REALNAME:
			readSTANDARD(dis);
			readTYPE_STANDARD_POP(dis);
			readTYPE_STANDARD_URL(dis);
			dis.skip(exDataLen-dis.getMarkBytes(dis.getMark()));
			readTextAndTitle(dis);
			readBtnText(dis);
			break;
		case E_MT_PROPSHORTCUTBUY:
		case E_MT_PROPUSE:
			readSTANDARD(dis);
			readTYPE_STANDARD_POP(dis);
			propId=dis.readInt();   //939524096
			urlID=dis.readByte();
			dis.skip(exDataLen-dis.getMarkBytes(dis.getMark()));
			readTextAndTitle(dis);
			readBtnText(dis);
			break;
		default:
			break;
		}



	}

	/**
	 * @Title: readTextAndTitle
	 * @Description: 读取标题和文本
	 * @param dis
	 *        设定参数
	 * @return void 返回类型
	 * @throws
	 * @version: 2011-12-14 下午06:59:24
	 */
	private void readTextAndTitle(TDataInputStream dis){
		title=dis.readUTF(titleLen);
		text=dis.readUTF(textLen);
	}

	/****
	 * @Title: getCommonMessage
	 * @Description: 获得通用消息文本
	 * @return
	 * @version: 2013-2-27 下午01:59:14
	 */
	public String getCommonMessage(){
		return text;
	}



	/**
	 * @Title: readBtnText
	 * @Description:读出三个按钮的文本
	 * @param dis
	 *        设定参数
	 * @return void 返回类型
	 * @throws
	 * @version: 2011-12-14 下午06:58:56
	 */
	private void readBtnText(TDataInputStream dis){
		mBtn=dis.readUTF(mBtnLen);
		rBtn=dis.readUTF(rBtnLen);
		lBtn=dis.readUTF(lBtnLen);
		if(mode==1){
			if(mBtn!=null&&mBtn.trim().length()>0){
				rBtn=mBtn;
			}
		}else if(mode==2&&mBtn!=null&&mBtn.trim().length()>0){
			if(rBtn==null||rBtn.trim().length()==0){
				rBtn=mBtn;
			}else if(lBtn==null||lBtn.trim().length()==0){
				lBtn=mBtn;
			}
		}
	}



	/**
	 * @Title: readSTANDARD
	 * @Description: 常用弹出框消息结构
	 * @param dis
	 *        设定参数
	 * @return void 返回类型
	 * @throws
	 * @version: 2011-12-14 下午06:58:34
	 */
	private void readSTANDARD(TDataInputStream dis){
		fontSize=dis.readByte();
		fontColor=dis.readInt();
		titleLen=dis.readByte();
		textLen=dis.readShort();
	}


	/**
	 * @Title: readTYPE_STANDARD_POP
	 * @Description: 读出按钮带链接的消息弹出框
	 * @param dis
	 *        设定参数
	 * @return void 返回类型
	 * @throws
	 * @version: 2011-12-14 下午06:58:01
	 */
	private void readTYPE_STANDARD_POP(TDataInputStream dis){
		iconID=dis.readShort();
		delay=dis.readShort();
		short data=dis.readShort();
		mode=(byte)((data&0x000f)>>0);
		mBtnLen=(byte)((data&0x00f0)>>4);
		rBtnLen=(byte)((data&0x0f00)>>8);
		lBtnLen=(byte)((data&0xf000)>>12);
	}



	/**
	 * @Title: readTYPE_STANDARD_URL
	 * @Description: 读取带有超链接的提示信息
	 * @param dis
	 *        设定参数
	 * @return void 返回类型
	 * @throws
	 * @version: 2011-12-14 下午06:57:10
	 */
	private void readTYPE_STANDARD_URL(TDataInputStream dis){
		mUrlID=dis.readShort();
		rUrlID=dis.readShort();
		lUrlID=dis.readShort();
	}
	/**
	 * @Title: getUrlId
	 * @Description: 获取不为零的地址
	 * @param @return 设定参数
	 * @return short 返回类型
	 * @throws
	 * @version: 2011-12-28 下午02:09:51
	 */
	public short getUrlId(){
		if(mUrlID!=0){
			return mUrlID;
		}else if(rUrlID!=0){
			return rUrlID;
		}else if(lUrlID!=0){
			return lUrlID;
		}
		return 0;
	}

}
