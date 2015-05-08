package com.mykj.andr.model;

import java.io.Serializable;

import com.mykj.comm.io.TDataInputStream;
import com.mykj.comm.io.TDataInputStream.MDataMark;


/****************
 * 系统公告实体
 * @author zhanghuadong
 * 2012-6-18
 */
public class NoticeSystemInfo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
    
	//public String Day;    //今天，昨天，12-26
	//public String time;   //12:25
	//public String title;  //即将登陆
	//public String content;  //xxxx内容
	//-----------------------------------------------------------------------
	public String msgContent;
//	public byte fontSize;                           //因为老版本可能崩溃原因，服务端根本没发这两个东西
//	public int fontColor=0xffffff;
	//从服务端回来的字符串
	public String fromServer;
	
	public int UrlOpenType;   //URL打开方式 1 直接内嵌打开  2 浏览器打开
	
	public NoticeSystemInfo(TDataInputStream dis){
		if(dis==null){
			return;
		} 
		//int dataLen=dis.readShort(); // 此房间数据块长度
		//dis.markLen(dataLen);
		
		final int dataLen=dis.readShort();
		MDataMark mark=dis.markData(dataLen);
		
		
		short contentLength=dis.readShort();
		this.fromServer=msgContent=dis.readUTF(contentLength);
//		fontSize=dis.readByte();      //因为老版本可能崩溃原因，服务端根本没发这两个东西
//		fontColor=dis.readInt();
		
		UrlOpenType=dis.readShort();  //URL打开方式 1 直接内嵌打开  2 浏览器打开
		//dis.unMark();
		dis.unMark(mark);
	}
	
	
	//-----------------------------------------------------------------------
	public NoticeSystemInfo(){}
	public NoticeSystemInfo(String content){
		this.fromServer=content;
	}
	
	
	
	
	
}
