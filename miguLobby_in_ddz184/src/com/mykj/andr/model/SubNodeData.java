package com.mykj.andr.model;

import java.io.Serializable;

import com.mykj.comm.io.TDataInputStream;
import com.mykj.comm.io.TDataInputStream.MDataMark;





public class SubNodeData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int ID; // 节点标识
	private byte NameLen;
	private String Name;
	private int Persons;//在线人数
	
	public SubNodeData(){}

	public SubNodeData(byte[] array){
		this(new TDataInputStream(array));
	}

	public SubNodeData(TDataInputStream dis){
		if (dis == null) {
			return;
		}

		dis.setFront(false);
		final int len=dis.readShort();
		MDataMark mark=dis.markData(len);

		ID = dis.readInt(); // 节点标识
		NameLen= dis.readByte();//名称长度
		Name=dis.readUTF(NameLen); //房间名称
		Persons = dis.readInt(); // 游戏人数
		dis.unMark(mark);
	}

	
	
	
	public int getID() {
		return ID;
	}



	public byte getNameLen() {
		return NameLen;
	}



	public String getName() {
		return Name;
	}



	public int getPersons() {
		return Persons;
	}

	
}
