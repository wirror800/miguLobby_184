package com.mykj.andr.net;

import com.mingyou.distributor.NetPrivateListener;
import com.mingyou.distributor.NetSocketPak;
import com.mingyou.login.TcpShareder;


public final class NetSocketManager{
	
	private static NetSocketManager instance;
	
	public static NetSocketManager getInstance(){
		if(instance == null){
			instance = new NetSocketManager();
		}
		return instance;
	}
	
	private NetSocketManager(){}

	/**
	 * 注册监听协议--可供应用层调用
	 * @param _listance
	 * @return
	 */
	public final void addPrivateListener(NetPrivateListener _listance){
		TcpShareder.getInstance().addTcpListener(_listance);
	}
	
	/**
	 * 发送--可供应用层调用
	 * @param netSocketPak
	 * @return
	 */
	public final boolean sendData(NetSocketPak data){
		TcpShareder.getInstance().reqNetData(data);
		return true;
	} 
	
	public final void send(byte []data){
		TcpShareder.getInstance().reqNetData(data);
	} 
	
	
}
