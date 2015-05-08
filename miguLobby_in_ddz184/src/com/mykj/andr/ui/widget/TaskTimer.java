package com.mykj.andr.ui.widget;

import java.util.Timer;


/*****
 * 
 * @ClassName: TaskTimer
 * @Description: 娱乐赛任务线程()
 * @author zhanghuadong
 * @date 2012-10-16 上午11:24:27
 *
 */

public class TaskTimer extends Timer{

	/**距离开赛时间**/
	public int mdisTime=0;
	/**赛事名称**/
	public String matchName;
	/**节点dataId**/
	public long dataId=0;
	
	public TaskTimer(int disTime,String mMatchName,long mdataId){
		this.mdisTime=disTime;
	    this.matchName=mMatchName;
	    this.dataId=mdataId;
	}
	
	  
}
