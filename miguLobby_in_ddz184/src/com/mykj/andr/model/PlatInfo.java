package com.mykj.andr.model;

import java.io.Serializable;

public class PlatInfo implements Serializable{
	/**
	 *  平台相关信息，游戏内用到
	 */
	private static final long serialVersionUID = 1L;
	
	public short gameId;               //游戏id
	public short clientId;             //平台id
	public short subClientId;          //子平台id
	public int clientVer;              //版本号信息，转换为int
	public PlatInfo(){
		
	}
}