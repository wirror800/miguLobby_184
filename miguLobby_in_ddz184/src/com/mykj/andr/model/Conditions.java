package com.mykj.andr.model;

import java.io.Serializable;

import com.mykj.comm.io.TDataInputStream;

public class Conditions implements Serializable{
	private static final long serialVersionUID = 1L;

	/** 类型ID **/
	public byte byCostID = -1;
	/** 值 **/
	private int dwValue = -1;
	/** 描述信息 **/
	private String szDec = null;

	public Conditions(TDataInputStream dis) {
		byCostID = dis.readByte();
		dwValue = dis.readInt();
		szDec = dis.readUTFByte();
	}

	public String getDesConditions() {
		return szDec;
	}

	public int getValueConditions() {
		return dwValue;
	}

	/**
	 * @Title: isSameValueAndType
	 * @Description: 判断是否相等
	 * @param con
	 * @return
	 * @version: 2012-3-27 下午06:42:37
	 */
	public boolean isSameValueAndType(final Conditions con) {
		if (con == null) {
			return false;
		}
		return con.byCostID == byCostID && con.dwValue == dwValue;
	}

	@Override
	public String toString() {
		return szDec;
	}

}
