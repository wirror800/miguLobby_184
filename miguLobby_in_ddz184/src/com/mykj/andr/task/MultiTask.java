package com.mykj.andr.task;

import java.util.ArrayList;
import java.util.List;

import com.mykj.comm.io.TDataInputStream;



public class MultiTask {
  /**包任务总个数*/	
  private int mPackageCount=0;	
  /**任务总个数*/	
  private int mCount =0;
  
  /**任务链表*/
  private List<TaskItem> mTaskList= new ArrayList<TaskItem>();
  
	public MultiTask(TDataInputStream dis){
		if(dis==null){
			return;
		}
		mTaskList.clear();
		dis.setFront(false);
		
		mPackageCount=dis.readShort();
		mCount = dis.readShort();
		if(mCount>0){
			for(int i=0;i<mPackageCount;i++){
				TaskItem item = new TaskItem(dis);
				mTaskList.add(item);	
			}
		}
		
		
	}
	
	/**获取多任务，所有任务的链表*/
	public List<TaskItem> getMultiTaskList(){
		return mTaskList;
	}
	
}
