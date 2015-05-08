package com.mykj.andr.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mykj.andr.model.HallDataManager;
import com.mykj.andr.model.NoticePersonInfo;
import com.mykj.andr.model.UserInfo;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.Util;

/****************
 * 数据提供器,这应该是从数据库或者从服务端取数据，先模拟数据
 * 
 * @author zhanghuadong
 * 
 */
public class NoticePersonProvider {
	private static final String TAG="NoticePersonProvider";
	
	private static NoticePersonProvider instance;

	private NoticePersonProvider() {
		list = new ArrayList<NoticePersonInfo>();
		mList = new ArrayList<String>();
	}

	List<NoticePersonInfo> list = null;
	
	List<String> mList = null;

	public static NoticePersonProvider getInstance() {
		if (instance == null)
			instance = new NoticePersonProvider();
		return instance;
	}

	public void init() {
		list.clear();
		if (null != mList) {
			mList.clear();
		}
	}

	public void addPersonInfo(NoticePersonInfo info) {
		list.add(info);
	}

	public NoticePersonInfo[] getPersonInfos() {
		Collections.reverse(list); 
		return list.toArray(new NoticePersonInfo[list.size()]);
	}

	public List<NoticePersonInfo> geParsetNoticePersons() {
		return list;
	}
	
	/***
	 * @Title: addPersonArray
	 * @Description: 添加对象数组到list中
	 * @param array
	 * @version: 2012-11-28 下午03:09:51
	 */
	public void addPersonArray(NoticePersonInfo[]array){
		for(NoticePersonInfo info :array){
//			list.add(info);
			mList.add(info.fromServer);
		}
		Collections.reverse(list); 
		Collections.reverse(mList); 
		savePersonMsgStatus();
	}
	
	public void addPersonInfoStr(String info) {
		mList.add(info);
	}
	
	public List<String> getPersonStr() {
		return mList;
	}
	
	public void clsPersonStr() {
		mList.clear();
	}


	/**
	 * 保存活消息画现实状态
	 */
	private void savePersonMsgStatus(){
		StringBuilder sb=new StringBuilder();
		UserInfo userInfo = HallDataManager.getInstance().getUserMe();
		String key_tag=userInfo.nickName;
		for(NoticePersonInfo item:list){
			sb.append(item.msgContent.trim());
		}
		String content=sb.toString();
		String md5=Util.getMD5(content);
		String savedMD5=Util.getStringSharedPreferences(AppConfig.mContext, NoticePersonProvider.TAG, "");
		if(!md5.equals(savedMD5)){
			Util.setStringSharedPreferences(AppConfig.mContext, NoticePersonProvider.TAG, md5);
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

