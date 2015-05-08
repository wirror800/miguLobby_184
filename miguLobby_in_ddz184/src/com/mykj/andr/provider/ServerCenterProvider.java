package com.mykj.andr.provider;

import java.util.ArrayList;
import java.util.List;

import com.mykj.andr.model.ServerItemInfo;

/************
 * 服务中心数据提供：(模拟服务器端数据)
 * 
 * @author zhanghudong
 * 
 */
public class ServerCenterProvider {
	static ServerCenterProvider instance;

	private ServerCenterProvider() {
		list = new ArrayList<ServerItemInfo>();
	}

	public static ServerCenterProvider getInstance() {
		if (instance == null)
			instance = new ServerCenterProvider();
		return instance;
	}

	List<ServerItemInfo> list = null;

	public void init() {
		list.clear();
	}

	public void addServerItemInfo(ServerItemInfo info) {
		list.add(info);
	}

	public List<ServerItemInfo> geServerItemInfos() {
		return list;
	}

	public ServerItemInfo[] getServerItems() {
		return list.toArray(new ServerItemInfo[list.size()]);
	}

	public List<ServerItemInfo> getServerList() {
		return list;
	}

}
