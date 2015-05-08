package com.mykj.andr.task;

import com.mykj.comm.io.TDataInputStream;

/**
 * @ClassName: HTASK_LIMIT
 * @Description: 任务限制条件信息：限制内型、后接起始数据、后接结束数据
 * @author Link
 * @date 2011-5-23 上午10:23:53
 */
public class TaskLimitInfo{
	/**
	 * WORD wLimitType; //限制类型 WORD wBeginLen; //后接起始数据长度 WORD wEndLen;
	 * //后接结束数据长度
	 */
	/** 限制类型 */
	private short LimitType;
	/** 后接起始数据长度 */
	private short BeginLen;
	/** 后接结束数据长度 */
	private short EndLen;
	/** 后接起始数据 */
	private String BeginData;
	/** 后接结束数据 */
	private String EndData;



	public TaskLimitInfo(TDataInputStream dis){
		if(dis==null){
			return;
		}
		dis.setFront(false);
		LimitType=dis.readShort();
		BeginLen=dis.readShort();
		EndLen=dis.readShort();
		BeginData=dis.readUTF(BeginLen);
		EndData=dis.readUTF(EndLen);
	}

	public TaskLimitInfo(byte[] array){
		this(new TDataInputStream(array));
	}

	@Override
	public String toString(){
		return "[]";
	}

	public short getLimitType() {
		return LimitType;
	}

	public short getBeginLen() {
		return BeginLen;
	}

	public short getEndLen() {
		return EndLen;
	}

	public String getBeginData() {
		return BeginData;
	}

	public String getEndData() {
		return EndData;
	}
	
	

}