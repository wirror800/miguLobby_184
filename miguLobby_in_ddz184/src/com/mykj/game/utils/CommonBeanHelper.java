package com.mykj.game.utils;

import com.mykj.andr.model.HallDataManager;
import com.mykj.andr.model.NodeData;
import com.mykj.andr.model.UserInfo;

public class CommonBeanHelper {

	/****
	 * @Title: NeedEnquireUserQuickGame
	 * @Description: 判断用户乐豆是否超房间上限
	 * @param nodeData
	 * @version: 2012-10-25 上午11:25:51
	 */
	public static boolean NeedEnquireUserQuickGame(NodeData node) {
		UserInfo userInfo = HallDataManager.getInstance().getUserMe();
		int MyBean = userInfo.bean;
			int Max = -1;// 乐豆的最大值
			if (node.limits != null) {
				for (int k = 0; k < node.limits.length; k++) // 查找该房间是否是乐豆快速房间
				{
					if (node.limits[k].Type == 3) // 找到乐豆类型
					{
						Max = node.limits[k].Max; // 找到最大值
						break;
					}
				}
			}
			if (MyBean > Max && Max > 0)// 该房间进不去了
				return true;
		return false;
	}
	
	/*****
	 * @Title: bdjectRoomCanCome
	 * @Description: 判断能进入服务房间的条件
	 * @param node
	 * @return
	 * @version: 2013-3-23 下午06:00:24
	 */
	public static  boolean bdjectRoomCanCome(NodeData node){
		UserInfo userInfo = HallDataManager.getInstance().getUserMe();
		int MyBean = userInfo.bean;
			int Max = -1;// 乐豆的最大值
			if (node.rmLimits != null) {
				for (int k = 0; k < node.rmLimits.length; k++) // 查找该房间是否是乐豆快速房间
				{
					if (node.rmLimits[k].getType() == 3) // 找到乐豆类型
					{
						Max = node.rmLimits[k].getMax(); // 找到最大值
						break;
					}
				}
			}
			if (MyBean > Max && Max > 0)// 该房间进不去了
				return true;
		return false;
	}
	
	
	
	/***
	 * @Title: isSuperiorBean
	 * @Description:判断用户乐豆是否在该赛场乐豆上下限
	 * @param nodeData
	 * @return
	 * @version: 2012-11-8 上午11:13:14
	 */
	public static  boolean isSuperiorLimitBean(NodeData nodeData){
		UserInfo userInfo = HallDataManager.getInstance().getUserMe();
		int MyBean = userInfo.bean;
		
		    int Max = -1;//乐豆最大值
			int Min = -1;//乐豆最小值
			
			if(nodeData.limits!=null)
			{
				for(int k=0; k<nodeData.limits.length; k++)
				{
					if(nodeData.limits[k].Type == 3) //找到乐豆类型
					{
						Max = nodeData.limits[k].Max;//找到最大值
						Min = nodeData.limits[k].Min;//找到最小值
						break;
					}
				}
			}
			
			if((MyBean>Min)&&(MyBean<Max || Max <= 0)){
				return true;
			} 
		return false;
	}
	
	/***
	 * @Title: isLimitBeanMix
	 * @Description:判断用户乐豆是否小于该节点最小值限制
	 * @param nodeData
	 * @return
	 * @version: 2012-11-16 下午02:14:34
	 */
	public static boolean isLimitBeanMix(NodeData nodeData){
		UserInfo userInfo = HallDataManager.getInstance().getUserMe();
		int MyBean = userInfo.bean; 
			int Min = -1;//乐豆最小值
			
			if(nodeData.limits!=null)
			{
				for(int k=0; k<nodeData.limits.length; k++)
				{
					if(nodeData.limits[k].Type == 3) //找到乐豆类型
					{ 
						Min = nodeData.limits[k].Min;//找到最小值
						break;
					}
				}
			}
			if(MyBean<Min){
				return true;
			} 
		return false;
	}
	
	
	
}
