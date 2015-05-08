package com.mykj.andr.task;

import com.mykj.comm.io.TDataInputStream;

/**
 * @ClassName: HTASK_INFO
 * @Description: 任务的基本信息：标题、说明内容、任务结束提示信息
 * @author Link
 * @date 2011-5-23 上午10:23:26
 */
public class TaskInfo{
	/**
	 * struct tagTaskInfo { DWORD dwTaskID; //任务ID BYTE byTitleLen; //标题长度
	 * WORD wDescLen; //说明长度 BYTE byFinTextLen; //任务结束时的提示信息 BYTE
	 * byUnFinTextLen; //任务未完成提示 BYTE byGiftTip; //任务奖励提示 WORD wSort;
	 * //任务允许次数0为循环任务>0为次数任务 } //是否为循环
	 */
	/** 任务ID */
	private int TaskID;
	/** 标题长度 */
	private byte TitleLen;
	/** 说明长度 */
	private short DescLen;
	/** 任务结束时的提示信息长度 */
	private byte FinTextLen;
	/** 任务未完成提示 信息长度 */
	private byte UnFinTextLen;
	/** 任务奖励提示 信息长度 */
	private byte GiftTipLen;
	/** 任务循环次数 */
	private short SortTimes;
	/** 是否为循环 */
	private boolean bSort;
	/** 任务标题 */
	private String Title;
	/** 任务说明信息 */
	private String Desc;
	/** 任务结束时的提示信息 */
	private String FinText;
	/** 任务未完成提示信息 */
	private String UnFinText;
	/** 任务奖励提示信息 */
	private String GiftTip;



	public TaskInfo(TDataInputStream dis){
		if(dis==null){
			return;
		}
		dis.setFront(false);
		TaskID=dis.readInt();
		TitleLen=dis.readByte();
		DescLen=dis.readShort();
		FinTextLen=dis.readByte();
		UnFinTextLen=dis.readByte();
		GiftTipLen=dis.readByte();
		SortTimes=dis.readShort();
		bSort=SortTimes==0 ? true : false;
		Title=dis.readUTF(TitleLen);
		Desc=dis.readUTF(DescLen);
		FinText=dis.readUTF(FinTextLen);
		UnFinText=dis.readUTF(UnFinTextLen);
		GiftTip=dis.readUTF(GiftTipLen);
	}

	public TaskInfo(byte[] array){
		this(new TDataInputStream(array));
	}
	
	

	public int getTaskID() {
		return TaskID;
	}



	public byte getTitleLen() {
		return TitleLen;
	}



	public short getDescLen() {
		return DescLen;
	}



	public byte getFinTextLen() {
		return FinTextLen;
	}



	public byte getUnFinTextLen() {
		return UnFinTextLen;
	}



	public byte getGiftTipLen() {
		return GiftTipLen;
	}



	public short getSortTimes() {
		return SortTimes;
	}



	public boolean isbSort() {
		return bSort;
	}



	public String getTitle() {
		return Title;
	}



	public String getDesc() {
		return Desc;
	}



	public String getFinText() {
		return FinText;
	}



	public String getUnFinText() {
		return UnFinText;
	}



	public String getGiftTip() {
		return GiftTip;
	}




}
