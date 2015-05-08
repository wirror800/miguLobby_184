package com.mykj.andr.task;

import com.mykj.comm.io.TDataInputStream;
import com.mykj.comm.io.TDataInputStream.MDataMark;

public class TaskItem {
	/**后置任务限制*/
	private static final byte TASKLIMIT=1;

	/**后置任务奖励*/
	private static final byte TASKGIFT=2;

	/**后置任务动作*/
	private static final byte TASKACTION=3;

	/**后置任务标示符*/
	private static final byte TASK_NEXT_MISSION=4;

	/**任务基本信息:标题、说明内容、结束提示信息*/
	private TaskInfo mTaskInfo=null;

	/**后置任务的id*/
	private int[] mTaskMissions;

	/**任务完成条件*/
	private TaskLimitInfo[] mTaskLimits;

	/**任务奖励信息*/
	private TaskGiftInfo[] mTaskGifts;

	/**任务的动作信息*/
	private TaskActionInfo[] mTaskActionInfos;


	public TaskItem(TDataInputStream dis){
		if(dis==null){
			return;
		}
		int dataLen=dis.readShort();  //本任务数据长度
		if(dataLen<=0){
			return;
		}
		MDataMark mark=dis.markData(dataLen);

		mTaskInfo=new TaskInfo(dis);

		while(dis.isCanRead(1)){        
			// 读取标识
			short type=dis.readShort();
			//类型标识读取
			readByType(dis,type);
		}

		dis.unMark(mark);

	}

	/**获取任务基本信息*/	
	public TaskInfo getTaskInfo(){
		return mTaskInfo;
	}

	/**获取后置任务标示id*/
	public int[] getTaskMissions(){
		return mTaskMissions;
	}

	/**获取任务完成条件*/
	public TaskLimitInfo[] getTaskLimits(){
		return mTaskLimits;
	}

	/**获取任务奖励信息*/
	public TaskGiftInfo[] getTaskGifts(){
		return mTaskGifts;
	}

	/**获取任务的动作信息*/
	public TaskActionInfo[] getTaskActionInfos(){
		return mTaskActionInfos;
	}

	
	/**
	 * @Title: readByType
	 * @Description: 根据类型标识读取
	 * @param dis
	 * @param dataType
	 * @author Link
	 * @version: 2011-5-23 下午01:42:53
	 */
	private void readByType(TDataInputStream dis,short dataType){
		short counts=dis.readShort();
		if(counts<1){
			return;
		}
		switch(dataType){
		case TASKLIMIT:
			mTaskLimits=new TaskLimitInfo[counts];
			for(int i=0;i<counts;i++){
				mTaskLimits[i]=new TaskLimitInfo(dis);
			}
			break;
		case TASKGIFT:
			mTaskGifts=new TaskGiftInfo[counts];
			for(int i=0;i<counts;i++){
				mTaskGifts[i]=new TaskGiftInfo(dis);
			}
			break;
		case TASKACTION:
			mTaskActionInfos=new TaskActionInfo[counts];
			for(int i=0;i<counts;i++){
				mTaskActionInfos[i]=new TaskActionInfo(dis);
			}
			break;
		case TASK_NEXT_MISSION:
			mTaskMissions=new int[counts];
			for(int i=0;i<counts;i++){
				mTaskMissions[i]=dis.readInt();
			}
			break;
		}
	}







}
