package com.mykj.andr.provider;

import java.util.ArrayList;
import java.util.List;

import com.mykj.andr.model.ActionInfo;
import com.mykj.andr.model.HallDataManager;
import com.mykj.andr.model.SystemPopMsg;
import com.mykj.andr.model.UserInfo;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.Util;

public class ActionInfoProvider {
    private static final String TAG="ActionInfoProvider";
    
	private static ActionInfoProvider instance;

	private List<ActionInfo> list = null;
	
	
	private SystemPopMsg.PopMsgItem noticeItem = null;

	private ActionInfoProvider() {
		list = new ArrayList<ActionInfo>();
	}
	
	public static ActionInfoProvider getInstance() {
		if (instance == null)
			instance = new ActionInfoProvider();
		return instance;
	}

	public void init() {
		list.clear();
		noticeItem = null;
	}

	/**
	 * @return the list
	 */
	public List<ActionInfo> getList() {
		return list;
	}

	

	public  void setActionInfoProvider(ActionInfo[]array){
		if(array==null){
			return;
		}
		init();   //初始化数据(清空)
		ArrayList<ActionInfo> arrayList = new ArrayList<ActionInfo>(array.length);
		for (ActionInfo t : array) {
			arrayList.add(t);
		}
		setList(arrayList);
	}

	
	public void addActionInfo(ActionInfo info) {
		list.add(info);
	}

	
	public ActionInfo[] getActionInfo() {
		return list.toArray(new ActionInfo[list.size()]); 
	}
	
	/**
	 * @param list the list to set
	 */
	public void setList(List<ActionInfo> list) {
		this.list = list;
		saveActionStatus();
	}
	
	
	/**
	 * 保存活动动画现实状态
	 */
	private void saveActionStatus(){
		StringBuilder sb=new StringBuilder();
		UserInfo userInfo = HallDataManager.getInstance().getUserMe();
		String key_tag=userInfo.nickName;
		for(ActionInfo item:list){
			if(item!=null){
				String url=item.url;
				if(url!=null){
					sb.append(url.trim());
				}
			}
			
		}
		String content=sb.toString();
		String md5=Util.getMD5(content);
		String savedMD5=Util.getStringSharedPreferences(AppConfig.mContext, ActionInfoProvider.TAG, "");
		if(!md5.equals(savedMD5)){
			Util.setStringSharedPreferences(AppConfig.mContext, ActionInfoProvider.TAG, md5);
			String tag=Util.getStringSharedPreferences(AppConfig.mContext, key_tag, AppConfig.DEFAULT_TAG);
			String[] strs=tag.split("&");
			if(strs!=null&&strs.length==3){
				int act=Integer.parseInt(strs[0]);
				if(act==1){
					strs[0]="0";
					StringBuilder sb1=new StringBuilder();
					sb1.append(strs[0]).append("&").append(strs[1]).append("&").append(strs[2]);
					Util.setStringSharedPreferences(AppConfig.mContext, key_tag,sb1.toString());
				}
			}
		}
	}
	
	/**
	 * 设置消息，可能为null
	 * @param item
	 */
	public void setNoticeItem(SystemPopMsg.PopMsgItem item){
		noticeItem = item;
	}
	
	public SystemPopMsg.PopMsgItem getNoticeItem(){
		return noticeItem;
	}

}
