package com.mykj.andr.model;


import com.mykj.comm.io.TDataInputStream;

public class HPropData {
	/**道具ID*/
	public int PropID;
	/**道具名称*/
	public String PropName;
	/**道具描述*/
	public String PropDesc; 

	public HPropData(TDataInputStream tdis) {
		if (tdis == null) {
			return;
		}
		PropID = tdis.readInt();
		PropName = tdis.readUTFShort();
		PropDesc = tdis.readUTFShort();
	}
}
