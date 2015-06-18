package com.mykj.andr.task;

import android.content.res.Resources;

import com.mykj.andr.task.GameTask.TASK_LABEL_TYPE;
import com.mykj.comm.io.TDataInputStream;
import com.MyGame.Migu.R;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.Log;


public class OwnTaskActionDesc { 
	private static final String TAG="OwnTaskActionDesc";
	/**
	 * 动作id
	 * 1 为玩局
	 * 2 为赢局
	 * */ 
	private int ActionID;
	// 动作已经完成的数量(赢、玩多少局)
	private int ActionCounted;
	
	/**服务器下发任务完成条件*/
	private int mActionReqSum;
	
	private String taskTitle;
	private String taskGiftTips;
	
	
	public OwnTaskActionDesc(TDataInputStream dis){
		if(dis==null){
			return;
		}
		dis.setFront(false);
		ActionID=dis.readInt();
		ActionCounted=dis.readInt(); 
		Log.v(TAG, "ActionCounted="+ActionCounted);
	}
	
	public OwnTaskActionDesc(byte[] data){
		this(new TDataInputStream(data));
	}

	public int getActionID() {
		return ActionID;
	}

	public int getActionCounted() {
		return ActionCounted;
	}

	public void setAddActionCounted(int actionCounted) {
		ActionCounted += actionCounted;
	}
	
	
	
	
	public void setmActionReqSum(int actionReqSum) {
		this.mActionReqSum = actionReqSum;
	}

	public void setTaskTitle(String taskTitle) {
		this.taskTitle = taskTitle;
	}

	public void setTaskGiftTips(String taskGiftTips) {
		this.taskGiftTips = taskGiftTips;
	}

	
	public int getTaskStatus(){
		if(ActionCounted!=mActionReqSum){
			return 1;
		}else{
			return 2;
		}
	}
	
	public String getTaskToString(TASK_LABEL_TYPE type){
		String str=null;
		Resources resources = AppConfig.mContext.getResources();
		String yinStr = resources.getString(R.string.task_yin); // 赢
		String junStr = resources.getString(R.string.task_ju); // 局
		String wanStr = resources.getString(R.string.task_wan); // 玩
		String currentStatusStr = resources.getString(R.string.task_current_status); // 现在状态
		String doneStr = resources.getString(R.string.task_done); // 已完成
		// 赢局
		if (ActionID == 2){
			switch (type) {
			case TASK_LABEL_ALL:
				str = String.format("%1$s：" + yinStr + "%2$d" + junStr + ",%3$s" + currentStatusStr + ":%4$d/%5$d(" + junStr + ")",
						taskTitle, mActionReqSum, taskGiftTips,
						ActionCounted, mActionReqSum);
				break;
			case TASK_LABEL_DESC:
//				str = String.format("%1$s：赢%2$d局，%3$s", taskTitle,
//						mActionReqSum, taskGiftTips);
				str = String.format(yinStr + "%1$d" + junStr + ",%2$s",
						mActionReqSum, taskGiftTips);
				break;
			case TASK_LABEL_STATUS:
				if(ActionCounted!=mActionReqSum){
					str = String.format("%1$d/%2$d(" + junStr +")", ActionCounted,
							mActionReqSum);
				}else{
					str=doneStr;
				}
				break;
			}
		} 
		//玩局
		else if(ActionID == 1){
			switch (type) {
			case TASK_LABEL_ALL:
				str = String.format("%1$s：" + wanStr + "%2$d" + junStr +",%3$s" + currentStatusStr + ":%4$d/%5$d(" + junStr +")",
						taskTitle, mActionReqSum, taskGiftTips,
						ActionCounted, mActionReqSum);
				break;
			case TASK_LABEL_DESC:
//				str = String.format("%1$s：玩%2$d局，%3$s", taskTitle,
//						mActionReqSum, taskGiftTips);
				str = String.format(wanStr + "%1$d" + junStr +",%2$s",
				        mActionReqSum, taskGiftTips);
				break;
			case TASK_LABEL_STATUS:
				if(ActionCounted!=mActionReqSum){
					str = String.format("%1$d/%2$d(" + junStr +")", ActionCounted,
							mActionReqSum);
				}else{
					str=doneStr;
				}
				
				break;
			}
		}
		else{
           Log.e(TAG, "getTaskToString error!!!");
		}
		return str;
	}

}
