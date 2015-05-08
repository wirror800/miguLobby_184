package com.mykj.andr.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mykj.andr.model.HallDataManager;
import com.mykj.andr.model.NoticeSystemInfo;
import com.mykj.andr.model.UserInfo;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.Util;

/****************
 * 数据提供器,这应该是从数据库或者从服务端取数据，先模拟数据
 * 
 * @author zhanghuadong
 * 
 */
public class NoticeSystemProvider {

	private static final String TAG="NoticeSystemProvider";
	
	private static NoticeSystemProvider instance;

	private NoticeSystemProvider() {
		list = new ArrayList<NoticeSystemInfo>();
	}

	public static NoticeSystemProvider getInstance() {
		if (instance == null)
			instance = new NoticeSystemProvider();
		return instance;
	}

	List<NoticeSystemInfo> list = null;

	public void init() {
		list.clear();
	}

	public void addSystemInfo(NoticeSystemInfo info) {
		list.add(info);
	}

	public NoticeSystemInfo[] getNoticeSystemInfos() {
		//Collections.reverse(list); 
		return list.toArray(new NoticeSystemInfo[list.size()]); 
	}

	public List<NoticeSystemInfo> geParsetNoticeSystems() {
		return list;
	}

	/***
	 * @Title: addSysytemArray
	 * @Description: 添加对象数组到list中
	 * @param array
	 * @version: 2012-11-28 下午03:09:51
	 */
	public void addSysytemArray(NoticeSystemInfo[]array){
		for(NoticeSystemInfo info :array){
			list.add(info);
		}
		Collections.reverse(list); 
		saveSystemMsgStatus();
	}
	
	
	
	
	/**
	 * 保存活消息画现实状态
	 */
	private void saveSystemMsgStatus(){
		StringBuilder sb=new StringBuilder();
		UserInfo userInfo = HallDataManager.getInstance().getUserMe();
		String key_tag=userInfo.nickName;
		for(NoticeSystemInfo item:list){
			sb.append(item.msgContent.trim());
		}
		String content=sb.toString();
		String md5=Util.getMD5(content);
		String savedMD5=Util.getStringSharedPreferences(AppConfig.mContext, NoticeSystemProvider.TAG, "");
		if(!md5.equals(savedMD5)){
			Util.setStringSharedPreferences(AppConfig.mContext, NoticeSystemProvider.TAG, md5);
			String tag=Util.getStringSharedPreferences(AppConfig.mContext, key_tag, AppConfig.DEFAULT_TAG);
			String[] strs=tag.split("&");
			if(strs!=null&&strs.length==3){
				int msg=Integer.parseInt(strs[2]);
				if(msg==1){
					strs[2]="0";
					StringBuilder sb1=new StringBuilder();
					sb1.append(strs[0]).append("&").append(strs[1]).append("&").append(strs[2]);
					Util.setStringSharedPreferences(AppConfig.mContext, key_tag,sb1.toString());
				}
			}
		}
	}
	
	
	
}
