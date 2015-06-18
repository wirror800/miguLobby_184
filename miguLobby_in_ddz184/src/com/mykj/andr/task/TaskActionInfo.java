package com.mykj.andr.task;

import com.mykj.comm.io.TDataInputStream;
import com.MyGame.Migu.R;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.Log;

/**
 * @ClassName: HTASK_ACTION_INFO
 * @Description: 任务动作信息：动作ID、此动作的个数
 * @author Link
 * @date 2011-5-23 上午10:25:17
 */
public class TaskActionInfo{
	/**
	 * DWORD dwActionID 动作id DWORD dwActionCount 动作个数
	 */
	/** 动作id */
	private int ActionID;
	/** 动作个数 */
	private int ActionCount;

	

	public TaskActionInfo(TDataInputStream dis){
		if(dis==null){
			return;
		}
		dis.setFront(false);
		ActionID=dis.readInt();
		ActionCount=dis.readInt();   //动作总计数
		Log.e("ActionCount", ActionCount+"<<");
	}

	public TaskActionInfo(byte[] array){
		this(new TDataInputStream(array));
	}

	public String getActionName(){
		switch(ActionID){
		case 2:
			return AppConfig.mContext.getResources().getString(R.string.task_yin_jun);
		default:
			return AppConfig.mContext.getResources().getString(R.string.task_jun_shu);
		}
	}

	public int getActionID() {
		return ActionID;
	}

	public int getActionCount() {
		return ActionCount;
	}

	public void setActionID(int actionID) {
		ActionID = actionID;
	}

	public void setActionCount(int actionCount) {
		ActionCount = actionCount;
	}
	
	
}