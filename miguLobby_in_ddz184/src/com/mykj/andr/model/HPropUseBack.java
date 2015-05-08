/**
 * @Title: HPropUseBack.java
 * @Package com.myou.mjava.hall.socket.struc
 * @Description: TODO(用一句话描述该文件做什么)
 * @author Administrator
 * @date 2012-3-7 下午05:53:27
 * @version V1.0
 * @sine Copyright 名游网络手机部 Corporation 2012 版权所有
 */
package com.mykj.andr.model;

import com.mykj.comm.io.TDataInputStream;

/**
 * @ClassName: HPropUseBack
 * @Description: TODO(这里描述这个类的作用)
 * @author Administrator
 * @date 2012-3-7 下午05:53:27
 */
public class HPropUseBack{
	/** 充值卡使用返回提示输入数据 */
	public static final int TYPE_RESULT_CHONGZHIKA=1;
	/** 话费券使用返回提示输入数据 */
	public static final int TYPE_RESULT_HUAFEIQUAN=2;
	/** 实物道具使用返回提示输入数据 */
	public static final int TYPE_RESULT_SHIWU=3;
	/** 32 为用户没有此道具 */
	public static final int TYPE_RESULT_BUY=32;
	/** 使用失败，需要提供扩展数据 */
	public static final int TYPE_RESULT_DATA=81;
	/** 道具数量不足 */
	public static final int TYPE_RESULT_NOTNUMBER=165;
	/** 用户ID */
	public int userID;
	/** 道具ID */
	public int propID;
	/** 使用结果 */
	public int result;
	/** 道具索引ID */
	public long indexID;
	/** 道具使用返回msg */
	public String msg="";
	/** 客户端标识 */
	public long clisec;
	/** 类型 */
	public byte type;
	/** 充值卡和话费券 */
	public boolean isBind;
	/**元宝兑换urlID*/
	public short urlId;
	/**解绑道具ID*/
	public int jbId;
	/**解绑道具索引ID*/
	public int jbIndexId;
	/**绑定的电话号码*/
	public String bindTel;
	/**道具的有效期*/
	public String validDate;
	
	public HPropUseBack(TDataInputStream tdis){
		if(tdis==null){
			return;
		}
		tdis.setFront(false);
		userID=tdis.readInt();
		propID=tdis.readInt();
		result=tdis.readInt();
		if(tdis.available()>0){
			indexID=tdis.readLong();
		}
		if(tdis.available()>0){
			msg=tdis.readUTFByte().trim();
		}
		if(tdis.available()>0){
			clisec=tdis.readLong();
		}
		short extlen=0;
		type=tdis.readByte();
		if(type!=0){
			extlen=tdis.readShort();
		}
		if(extlen>0&&result==TYPE_RESULT_DATA){// 使用失败，需要提供扩展数据
			switch(type){
				case TYPE_RESULT_CHONGZHIKA:
				case TYPE_RESULT_HUAFEIQUAN:
					urlId=tdis.readShort();
					isBind=tdis.readByte()==0 ? false : true;
					jbId=tdis.readInt();
					bindTel=tdis.readUTFByte();
					break;
				case TYPE_RESULT_SHIWU:
					urlId=tdis.readShort();
					validDate=tdis.readUTF(10);
					break;
				default:
					break;
			}
		}
	}
}
