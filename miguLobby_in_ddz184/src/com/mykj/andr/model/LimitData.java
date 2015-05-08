package com.mykj.andr.model;

import java.io.Serializable;

import com.mykj.comm.io.TDataInputStream;
/***
 * 
 * @ClassName: LimitData
 * @Description: 限制条件数据实体类，C++中以结构体出现
 * @author zhanghuadong
 * @date 2012-8-31 下午04:24:01
 *
 */
public class LimitData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public LimitData(){}
	public LimitData(TDataInputStream dis){
		Type = dis.readByte();
		Min = dis.readInt();
		Max = dis.readInt();
	}
	 
	public byte Type;        //条件类型1-话费券 2-元宝 3-乐豆 4-积分
	public int Min;	         //最小值>=
	public int Max;	         //最大值<=

	public LimitData(byte[] array){
		this(new TDataInputStream(array));
	}
}
