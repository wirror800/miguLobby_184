package com.mykj.andr.model;

import java.io.Serializable;

import com.mykj.comm.io.TDataInputStream;

/***
 * 
 * @ClassName: RoomConfigData
 * @Description: 房间配置信息
 * @author zhd
 * @date 2012-12-25 下午01:55:22
 *
 */
public class RoomConfigData implements Serializable {
	/**
	 * @Fields serialVersionUID: TODO(用一句话描述这个变量表示什么)
	 */
	private static final long serialVersionUID = 1L;

	
	// 比赛场类型
	/** 移动海选赛(自动排位) **/
	public static final short MATCH_TYPE_OLD_PRIMARY = 0x00;
	/** 海选赛(自动排位)，智运会 **/
	public static final short MATCH_TYPE_PRIMARY = 0x01;
	/** 黑马赛(自动排位) ，智运会 **/
	public static final short MATCH_TYPE_PROMOTION = 0x02;
	/** 半决赛(自动排位)，智运会 **/
	public static final short MATCH_TYPE_SEMIFINAL = 0x03;
	/** 决赛-个体(定位)，智运会 **/
	public static final short MATCH_TYPE_FINAL_UNIT = 0x04;
	/** 决赛-团体(定位)，智运会 **/
	public static final short MATCH_TYPE_FINAL_Team = 0x05;
	/** 满人开赛 **/
	public static final short MATCH_TYPE_ENTERTAINMENT = 0x06;
	/** 晋级赛(自动排位) **/
	public static final short MATCH_TYPE_PRIMARY_PROMOTION = 0x07;
	/** 约战赛) **/
	public static final short MATCH_TYPE_GATHER = 0x08; // 约战赛 add lsh 2012.2.27
	/** 定点赛 **/
	public static final short MATCH_TYPE_TIME_ENTERTAINMENT = 0x09;// 定点赛
	//
	
	/** 类型I D (斗地主、象棋等GameID) **/
	public short wKindID = 0;
	/** 桌子数目 **/
	public short tableCount = 0;
	/** 椅子数目 **/
	public short chairCount = 0;
	/** 允许进入普通用户最大数 **/
	public short maxUser = 0;
	/** 比赛类型,表示满人赛 **/
	public short machType = 0;
	
	public int res1=0;
	
	/** 游戏类型 **/
	public short wGameGenre = 0;
	/** 隐藏信息 **/
	public byte hideUserInfo = 0;
	
	public int res2=0;
	
	public RoomConfigData(TDataInputStream dis){
		if(dis == null){
			return;
		}
		wKindID = dis.readShort();// 类型I D (斗地主、象棋等GameID)
		tableCount = dis.readShort();// 桌子数目
		chairCount = dis.readShort();// 椅子数目
		short value = dis.readShort(); // 最大用户数和类型组装体
		maxUser = (short)(value & 0xfff); // 允许进入普通用户最大数
		machType = (short)((value >> 12) & 0xf); // 比赛类型限种 6表示满人赛
		res1=dis.readInt();// （保留）,用户美女视频帮助url下发
		wGameGenre = dis.readShort();// 游戏类型
		hideUserInfo = dis.readByte();// 隐藏信息
		res2=dis.readByte();// （保留）
	}
	
	/**
	 * @Title: isMatchType
	 * @Description: 是否指定类型
	 * @param type
	 * @return
	 * @version: 2011-12-19 下午04:23:20
	 */
	public boolean isMatchType(short type){
		return machType == type;
	}
	
	/**
	 * @Title: isShowMatchPanel
	 * @Description:是否显示比赛信息面板
	 * @return
	 * @version: 2011-12-19 下午05:16:42
	 */
	public boolean isShowMatchPanel(){
		return machType >= MATCH_TYPE_PRIMARY
				&& machType <= MATCH_TYPE_FINAL_Team;
	}
}
