package com.mykj.andr.model;

import java.io.Serializable;

import com.mykj.comm.io.TDataInputStream;

public class GoodNews implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**消息类型*/
	private byte MsgType;

	/**显示类型*/
	private byte ShowType;

	/**文本大小*/
	private byte TextSize;

	/**操作保留，方便以后扩展*/
	@SuppressWarnings("unused")
	private byte OpReserve;

	/**文本颜色*/
	private int TextColor;

	/**链接编号*/
	private short UrlID;

	/**消息长度*/
	private short MsgLen;

	/**Msg*/
	private String Msg;


	public GoodNews(){}

	public GoodNews(byte[] array){
		this(new TDataInputStream(array));
	}

	public GoodNews(TDataInputStream dis){
		if (dis == null) {
			return;
		}
		dis.setFront(false);
		MsgType=dis.readByte();
		ShowType=dis.readByte();
		TextSize=dis.readByte();
		OpReserve=dis.readByte();
		TextColor=dis.readInt();
		UrlID=dis.readShort();
		MsgLen=dis.readShort();
		Msg=dis.readUTF(MsgLen);
	}

	public byte getMsgType() {
		return MsgType;
	}

	public byte getShowType() {
		return ShowType;
	}

	public byte getTextSize() {
		return TextSize;
	}

	public int getTextColor() {
		return TextColor;
	}

	public short getUrlID() {
		return UrlID;
	}

	public String getMsg() {
		return Msg;
	}
	
	
}
