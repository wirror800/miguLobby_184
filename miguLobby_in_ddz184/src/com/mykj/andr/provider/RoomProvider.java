package com.mykj.andr.provider;

import java.util.ArrayList;
import java.util.List;

import com.mykj.andr.model.RoomData;
 
/****
 * 
 * @ClassName: RoomProvider
 * @Description: 卡片分区下多个房间区域
 * @author zhanghuadong
 * @date 2012-9-11 上午11:38:44
 *
 */
public class RoomProvider{
	static RoomProvider instance;
	private RoomProvider(){
		list=new ArrayList<RoomData>();
	}
	
	List<RoomData> list=null;
	public static RoomProvider getInstance(){
		if(instance==null)
			instance=new RoomProvider();
		return instance;
	}
	
	public void setList(ArrayList<RoomData> mlist) {
		this.list = mlist;
	}
	public void setList(List<RoomData> mlist) {
		this.list = mlist;
	}
	 
	 
	//测试数据
	/*public static RoomData[] getRoomItemDatas(){
		RoomData[] array=new RoomData[4];

		for(int i=0;i<array.length;i++){
			RoomData info=new RoomData();
			if(i%2==1){
				info.RoomID=i+1;
				info.Name="50倍场"+i;
				info.people=1234; 
				info.roomOptions="2000-2w乐豆准入";
			 }
			else{
				info.RoomID=i+1;
				info.Name="20倍场"+i;
				info.people=6541;    
				info.roomOptions="1000-2w乐豆准入";
		 }
			 
			array[i]=info;
		} 
		return array;
	}*/
	/**
	 * @Title: init
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @version: 2012-7-23 上午11:30:48
	 */
	public void init(){
		list.clear();
	}
	
	public  void addRoomItem(RoomData info){
		list.add(info);
	}
	
	public RoomData[] getRoomItems(){
		return list.toArray(new RoomData[list.size()]);
	}
	/**
	 * @Title: geParsetGoodsItems
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @return
	 * @version: 2012-7-23 上午11:31:18
	 */
	public List<RoomData> geParsetRoomItems(){ 
		return list;
	}
}
