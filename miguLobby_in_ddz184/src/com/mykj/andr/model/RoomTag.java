package com.mykj.andr.model;

import com.mykj.comm.io.TDataInputStream;


public class RoomTag {
	public byte Type;   //标签类型
	public byte DescLen;  //描述语长度
	public String Desc;   //描述语

	public RoomTag(TDataInputStream dis){
		Type = dis.readByte();
		DescLen = dis.readByte();
		Desc = dis.readUTF(DescLen);
		if (Desc != null) {
			if (Desc.length() > 4) {
				Desc = Desc.substring(0, 4);
			}
			if (Desc.length() > 2) {
				Desc = Desc.substring(0, 2) + "\n" + Desc.substring(2, Desc.length());
			} else if (Desc.length() == 2) {
				Desc = Desc.charAt(0) + "\n" + Desc.charAt(1);
			}
		}
	}
}
