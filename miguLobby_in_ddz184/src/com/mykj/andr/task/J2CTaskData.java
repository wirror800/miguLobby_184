package com.mykj.andr.task;
/**
 * 传入cpp的任务数据结构类
 *
 */
public class J2CTaskData {
   /**任务id*/
   public int taskId;
   /**
    * 1 正在进行
    * 任务完成状态*/
   public int taskStatus;
   /**任务简短描述*/
   public String briefInfo;
   /**任务详细描述*/
   public String detailedInfo;
   /**任务奖励描述*/
   public String giftInfo;
   /**
    * 任务奖励类型
    * 1 金币
	* 2  豆
	* 3  道具
	* 4  特殊物品,比如活动物品
    * */
   public int giftType;
   
   @Override
   public String toString(){
	   StringBuilder sb=new StringBuilder();
	   sb.append("taskId=").append(taskId).append("&");
	   sb.append("taskStatus=").append(taskStatus).append("&");
	   sb.append("briefInfo=").append(briefInfo).append("&");
	   sb.append("detailedInfo=").append(detailedInfo).append("&");
	   sb.append("giftInfo=").append(giftInfo).append("&");
	   sb.append("giftType=").append(giftType);
	   return sb.toString();
   }
}
