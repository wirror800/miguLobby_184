package com.mykj.andr.model;

import java.io.Serializable;

import com.mykj.comm.io.TDataInputStream;
import com.mykj.comm.io.TDataInputStream.MDataMark;

/***
 * 
 * @ClassName: RoomData
 * @Description: 房间信息类，C++中以结构体出现
 * @author zhanghuadong
 * @date 2012-8-31 下午04:24:01
 *
 */
public class RoomData implements Serializable{
	/**
	 * @Fields serialVersionUID: TODO( 
	 */
	private static final long	serialVersionUID	= 1L;

	// ---游戏类型值定义--与服务器定义对应(取与运算)---
	/** 积分类型 */
	public final static short GAME_GENRE_SCORE = 0x0001;
	/**
	 * 开心果类型
	 * 
	 * @deprecated
	 */
	public final static short GAME_GENRE_GOLD = 0x0002;
	/**
	 * 赛币类型
	 * 
	 * @deprecated
	 */
	public final static short GAME_GENRE_MATCHCUR = 0x0004;
	/**
	 * 训练类型
	 * 
	 * @deprecated
	 */
	public final static short GAME_GENRE_EDUCATE = 0x0008;
	/** 欢乐豆类型 */
	public final static short GAME_GENRE_BEAN = 0x0010;
	/**
	 * 类型（暂时没有使用）
	 * 
	 * @deprecated
	 */
	public final static short GAME_GENRE_HUNAN_MATCH = 0x0020;
	/** 比赛类型 */
	public final static short GAME_GENRE_DONGGUAN_MATCH = 0x0040;
	/**
	 * 拔旗赛比赛类型
	 * 
	 * @deprecated
	 */
	public final static short GAME_GENRE_FLAG = 0x0080;
	/**
	 * 金币类型 (对应139的旧金币类型)
	 * 
	 * @deprecated
	 */
	public final static short GAME_GENRE_REAL_GOLD = 0x0100;

	//---------------------------c++--------------------
	public int RoomID;//	房间ID
	public int NodeID;//	父节点ID
	public short GameType;//	游戏类型

	public byte SortID;//排序编号
	public byte TableCount;//桌子个数
	public byte ChairCount;//每个桌子椅子数
	public String Name;//节点名称
	//0 – Data无数据 1- 人数缓冲 2- 字符串(忙、满、闲),3  游戏状态 BYTE 1字节, 0表示未开始 1表示已经开始游戏
	public byte StateType;
	public byte StateLen = 0;/** 后接数据长度 **/
	public String StateData = null;/** 后接数据 **/
	public byte GameState;//StateType为3时用
	public int people;//StateType为1时用,人数
	public String desc;//StateType为2时用 为字符串（忙、满、闲） 
	public short TableID;//桌子编号 (默认为-1 表示无效) 新加字段 (add by pqh LobbyMsg.h 
	public byte playId;


	public RoomData(){}


	public RoomData(byte[] array){
		this(new TDataInputStream(array));
	}

	public RoomData(TDataInputStream dis){
		if(dis==null){
			return;
		}
		//int dataLen=dis.readShort(); // 此房间数据块长度
		//dis.markLen(dataLen);

		final int dataLen=dis.readShort();
		MDataMark mark=dis.markData(dataLen);


		RoomID = dis.readInt(); // 节点标识
		NodeID = dis.readInt(); // 父节点

		GameType = dis.readShort(); // 游戏类型
		SortID = dis.readByte(); // 排序编号
		TableCount = dis.readByte(); // 桌子个数
		ChairCount = dis.readByte(); // 每个桌子椅子数
		Name = dis.readUTFByte(); // 名称
		StateType = dis.readByte(); // 后接数据类型
		StateLen = dis.readByte(); // 后接数据长度
		switch(StateType){
		case 0: // 无数据
			break;
		case 1: // DWORD 4个 字节
			people =dis.readInt();
			break;
		case 2: // StateLen utf-8
			StateData = "(" + dis.readUTF(StateLen) + ")";
			break;
		case 3:
			GameState = dis.readByte(); // 游戏状态 BYTE 1字节, 0表示未开始
			break;
		default:
			break;
		} 
		TableID = dis.readShort(); // 桌子编号 (默认为-1 表示无效)
		playId=dis.readByte();
		//dis.unMark();
		dis.unMark(mark);
	}






}
