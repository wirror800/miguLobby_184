package com.mykj.andr.provider;

import java.util.ArrayList;
import java.util.List;

import com.mykj.andr.model.BackPackItem;
import com.mykj.andr.model.HallDataManager;
import com.mykj.andr.model.UserInfo;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.Util;

/********
 * 模拟背包中我的物品数据
 * @author  
 *
 */

public class BackPackItemProvider {
	private static final String TAG="BackPackItemProvider";
	private static BackPackItemProvider instance;
	private List<BackPackItem> mList=null;
	private boolean isFinish=false;

	private BackPackItemProvider(){
		mList=new ArrayList<BackPackItem>();
	}


	public static BackPackItemProvider getInstance(){
		synchronized (GoodsItemProvider.class) {
			if(instance==null)
				instance=new BackPackItemProvider();
		}
		return instance;
	}


	public void init(){
		if(mList!=null){
			mList.clear();
		}
		isFinish=false;
	}


	public  void setBackPackItem(BackPackItem[]array){
		if(array==null){
			return;
		}
		for (BackPackItem t : array) { 
			boolean has = false;
			for(BackPackItem t2 : mList){
				if(t2.id == t.id){
					has = true;
					break;
				}
			}
			if(!has){
				mList.add(t);
			}
		}
		saveBackPackStatus();
	}



	public  void addBackPageItem(BackPackItem info){
		mList.add(info);
	}

	public BackPackItem[] getBackPageItems(){
		return mList.toArray(new BackPackItem[mList.size()]);
	}

	public List<BackPackItem> getBackPageList(){ 
		return mList;
	}




	public boolean isFinish() {
		return isFinish;
	}


	public void setFinish(boolean isFinish) {
		this.isFinish = isFinish;
	}


	public int getPorpCount(int id){
		int res=0;
		for(BackPackItem item:mList){
			if(id==item.id){
				res=item.newHoldCount;
			}
		}
		return res;
	}


	/***
	 * @Title: getBackPackItem
	 * @Description: 根据道具ID获取道具全部信息
	 * @param shopID 道具ID
	 * @return
	 * @version: 2012-7-26 上午10:10:10
	 */
	public BackPackItem getBackPackItem(int shopID){

		BackPackItem current=null;

		for(BackPackItem info :mList){
			if(info.id==shopID){
				current=info;
				break;
			}
		}
		return current;
	}


	/**
	 * 保存背包显示状态
	 */
	private void saveBackPackStatus(){
		StringBuilder sb=new StringBuilder();
		UserInfo userInfo = HallDataManager.getInstance().getUserMe();
		String key_tag=userInfo.nickName;
		for(BackPackItem item:mList){
			sb.append(item.id);
		}
		String content=sb.toString();
		String md5=Util.getMD5(content);
		String savedMD5=Util.getStringSharedPreferences(AppConfig.mContext, BackPackItemProvider.TAG, "");
		if(!md5.equals(savedMD5)){
			Util.setStringSharedPreferences(AppConfig.mContext, BackPackItemProvider.TAG, md5);
			String tag=Util.getStringSharedPreferences(AppConfig.mContext, key_tag, AppConfig.DEFAULT_TAG);
			String[] strs=tag.split("&");
			if(strs!=null&&strs.length==3){
				int msg=Integer.parseInt(strs[1]);
				if(msg==1){
					strs[1]="0";
					StringBuilder sb1=new StringBuilder();
					sb1.append(strs[0]).append("&").append(strs[1]).append("&").append(strs[2]);
					Util.setStringSharedPreferences(AppConfig.mContext, key_tag,sb1.toString());
				}
			}
		}
	}
}
