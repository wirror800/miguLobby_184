package com.mykj.andr.model;

import java.io.Serializable;

import com.mykj.comm.io.TDataInputStream;

public class RmLimit implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RmLimit(){}
	public RmLimit(TDataInputStream dis){
		Type = dis.readByte();
		Min = dis.readInt();
		Max = dis.readInt();
	}
	 
	private byte Type;        //条件类型1-话费券 2-元宝 3-乐豆 4-积分
	private int Min;	         //最小值>=
	private int Max;	         //最大值<=

	public RmLimit(byte[] array){
		this(new TDataInputStream(array));
	}
	
	
	public byte getType() {
		return Type;
	}
	
	
	public int getMin() {
		return Min;
	}
	
	
	public int getMax() {
		return Max;
	}


}
