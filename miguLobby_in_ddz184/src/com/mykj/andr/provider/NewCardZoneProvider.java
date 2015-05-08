package com.mykj.andr.provider;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.mykj.andr.model.AllNodeData;
import com.mykj.andr.model.HallDataManager;
import com.mykj.andr.model.NewUIDataStruct;
import com.mykj.andr.model.NodeData;
import com.mykj.andr.model.UserInfo;
import com.mykj.game.utils.AppConfig;

public class NewCardZoneProvider {
	
	private List<NewUIDataStruct> mNewUIDataList;//列表UI数据结构
	private static NewCardZoneProvider instance;
	private Context mContext;

	/**
	 * 私有构造函数
	 */
	private NewCardZoneProvider() {
		mContext=AppConfig.mContext;
		mNewUIDataList=new ArrayList<NewUIDataStruct>();//列表UI数据结构
	}


	/**
	 * 单例
	 * @return
	 */
	public static NewCardZoneProvider getInstance() {
		if (instance == null){
			instance = new NewCardZoneProvider();
		}
		return instance;
	}


	/**
	 * 清除所有列表数据
	 */
	public void clearCardZoneProvider(){
		for(NewUIDataStruct item:mNewUIDataList){
			item.mSubNodeDataList.clear();
		}
		mNewUIDataList.clear();
		AllNodeData.getInstance(mContext).cleanNodeData();
	}


	/**
	 * 三玩法清除所有列表数据
	 */
	private void clearProvider(){
		for(NewUIDataStruct item:mNewUIDataList){
			item.mSubNodeDataList.clear();
		}
		mNewUIDataList.clear();
	}
	
	/**
	 * 获取所有的列表数据
	 * @return
	 */
	public List<NewUIDataStruct> getNewUIDataList() {
		return mNewUIDataList;
	}



	/**
	 * 1.5.0-1.5.2列表初始化
	 */
	public void initCardZoneProvider(){
		clearProvider();
		initCardZoneData();
		initCardListData();
	}

	/**
	 * 1.5.0-1.5.2
	 * 新协议重新刷新卡片数据
	 */
	public boolean resetCardZoneData(){
		for(NewUIDataStruct item:mNewUIDataList){
			if(item.ID==AllNodeData.getInstance(mContext).getRootID()){	
				List<NodeData> oldLists=item.mSubNodeDataList;
				List<NodeData> newLists=AllNodeData.getInstance(mContext).getCardZoneNodeDate();
				if(!oldLists.equals(newLists)){
					item.mSubNodeDataList=newLists;
					return true;
				}
			}

		}
		return false;
	}	


	/**
	 * 1.5.3 三玩法列表初始化
	 */
	public void initCardZoneProvider(final byte playId){
		clearProvider();
		initCardZoneData(playId);
		initCardListData(playId);
	}


	/**
	 * 1.5.3
	 * 三玩法协议,重新刷新卡片数据
	 */
	public boolean resetCardZoneData(final byte playId){
		for(NewUIDataStruct item:mNewUIDataList){
			if(item.ID==AllNodeData.getInstance(mContext).getRootID()){	
				List<NodeData> oldLists=item.mSubNodeDataList;
				List<NodeData> newLists=AllNodeData.getInstance(mContext).getCardZoneNodeDate(playId);
				if(!oldLists.equals(newLists)){
					item.mSubNodeDataList=newLists;
					return true;
				}
			}

		}
		return false;
	}	




	/**
	 * 1.3.0-1.4.2
	 * 初始化一级节点
	 */
	public void outerNetinitCardZoneData(NodeData nd){
		List<NodeData> subListData=new ArrayList<NodeData>();
		NewUIDataStruct item=new NewUIDataStruct(nd);
		if(item.ID==8){
			item.showCard=true;
		}
		item.mSubNodeDataList=subListData;
		mNewUIDataList.add(item);

	}

	//*****************************以下1.5.0-1.5.2  列表协议**********************************************

	/**
	 * 新协议初始化卡片数据
	 */
	private void initCardZoneData(){
		if(AllNodeData.getInstance(mContext).isShowCard()){
			NodeData nd=AllNodeData.getInstance(mContext).getCardNode();
			NewUIDataStruct item=new NewUIDataStruct(nd);
			item.showCard=true;
			item.mSubNodeDataList=AllNodeData.getInstance(mContext).getCardZoneNodeDate();
			mNewUIDataList.add(item);
		}
	}





	/**
	 * 新协议初始化列表数据
	 */
	private void initCardListData(){
		List<NodeData> firstnodes=AllNodeData.getInstance(mContext).getFirstNodeDate(); //抽屉节点
		for(NodeData nd:firstnodes){
			NewUIDataStruct item=new NewUIDataStruct(nd);
			item.showCard=false;
			item.mSubNodeDataList=AllNodeData.getInstance(mContext).getSecondNodeDate(nd.ID);
			mNewUIDataList.add(item);
		}
	}
	//*****************************以上1.5.0-1.5.2  列表协议**********************************************


	//**********************************以下1.5.3     新三玩法**********************************************
	/**
	 * 新三玩法初始化卡片数据
	 */
	private void initCardZoneData(byte palyId){
		if(AllNodeData.getInstance(mContext).isShowCard()){
			NodeData nd=AllNodeData.getInstance(mContext).getCardNode();
			NewUIDataStruct item=new NewUIDataStruct(nd);
			item.showCard=true;
			item.mSubNodeDataList=AllNodeData.getInstance(mContext).getCardZoneNodeDate(palyId);
			mNewUIDataList.add(item);
		}
	}


	/**
	 * 新协议三玩法初始化列表数据
	 */
	private void initCardListData(byte palyId){
		List<NodeData> firstnodes=AllNodeData.getInstance(mContext).getFirstNodeDate(palyId); //抽屉节点
		for(NodeData nd:firstnodes){
			NewUIDataStruct item=new NewUIDataStruct(nd);
			item.showCard=false;
			item.mSubNodeDataList=AllNodeData.getInstance(mContext).getSecondNodeDate(nd.ID,palyId);
			mNewUIDataList.add(item);
		}
	}

	//**********************************以上1.5.3     新三玩法**********************************************



	//********************************以下 1.3.0-1.4.2  列表协议支持******************************************
	List<NodeData> mListData=new ArrayList<NodeData>();

	/**
	 * 现网初始化二级节点
	 */
	public void outerNetinitListData(NodeData nd){
		mListData.add(nd);
		for(NewUIDataStruct item:mNewUIDataList){
			if(item.ID==nd.ParentID){
				item.mSubNodeDataList.add(nd);
				break;
			}
		}
	}

	/**
	 * 现网初始化推荐节点子节点
	 */
	public void outerNetinitRecommend(){
		for(NewUIDataStruct item:mNewUIDataList){
			if(item.ID==8){	
				item.mSubNodeDataList.add(getQuickEntryNodeDate());
				item.mSubNodeDataList.add(getRecommendNodeDate());
				item.mSubNodeDataList.add(mNewUIDataList.get(2).mNodeData);
			}
		}
	}



	/**
	 * 现网获取推荐节点
	 * @return
	 */
	private NodeData getQuickEntryNodeDate(){
		NodeData node=null;

		UserInfo userInfo = HallDataManager.getInstance().getUserMe();
		int myBean = userInfo.bean;
		List<NodeData> entryList = findNodeDataByBean(myBean);
		if(entryList.size()>=1){
			node=entryList.get(0);
		}else{
			node=mListData.get(0);
		}
		return node;
	}






	/**
	 * 现网获取推荐节点
	 * @return
	 */
	private NodeData getRecommendNodeDate(){
		NodeData node=null;
		for(NodeData nodeData:mListData){
			if(nodeData.IconID!=0){
				node= nodeData;
				break;
			}
		}

		if(node==null){
			node=mListData.get(1);
		}
		return node;
	}



	/**
	 * 获取乐豆要求跟用户乐豆数相符合的房间
	 * @param bean
	 * @return
	 */
	private List<NodeData> findNodeDataByBean(int bean){
		List<NodeData> list=new ArrayList<NodeData>();
		for(NodeData node:mListData){
			int max = -1;// 乐豆的最大值
			int min = -1;
			if (node.limits != null) {
				for (int k = 0; k < node.limits.length; k++) // 查找该房间是否是乐豆快速房间
				{
					if (node.limits[k].Type == 3) // 找到乐豆类型
					{
						max = node.limits[k].Max; // 找到最大值
						min = node.limits[k].Min; // 找到最小值

						if(bean>=min && bean<=max){
							list.add(node);
						}
					}
				}

			}
		}
		return list;
	}
	//********************************以上 1.3.0-1.4.2  列表协议支持******************************************

}
