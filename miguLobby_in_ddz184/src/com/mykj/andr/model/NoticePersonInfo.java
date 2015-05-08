package com.mykj.andr.model;

import java.io.Serializable;

import com.mykj.comm.io.TDataInputStream;
import com.mykj.comm.io.TDataInputStream.MDataMark;
/****************
 * 个人公告实体 
 * @author zhanghuadong
 * 2012-6-18
 */
public class NoticePersonInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//public String Day = "";    //今天，昨天，12-26
	//public String time= "";   //12:25
	//public String title= "";  //即将登陆
	//public String content= "";  //xxxx内容
	
	//------------------------------------------------
	public String msgContent;
	public byte fontSize;
	public int fontColor=0xffffff;
	public int leaveTime;  //留言时间1211091011,12年11月9日10时11分
	
	
	public NoticePersonInfo(TDataInputStream dis){
		if(dis==null){
			return;
		}
		
		//int dataLen=dis.readShort(); // 此房间数据块长度
		//dis.markLen(dataLen);
		
		final int dataLen=dis.readShort();
		MDataMark mark=dis.markData(dataLen);
		
		
		short len=dis.readShort();
		this.fromServer=msgContent=dis.readUTF(len);
		fontSize=dis.readByte();
		fontColor=dis.readInt();
		leaveTime=dis.readInt();
		
		//dis.unMark();
		dis.unMark(mark);
	}
	
	
	
	
	//----------------------------------------------------
	public NoticePersonInfo(){}
	public NoticePersonInfo(String content){
		this.fromServer=content;
	}
	//从服务端回来的字符串
	public String fromServer;
	
	
	
	
	
}
