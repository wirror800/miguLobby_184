package com.mykj.andr.task;

import com.mykj.comm.io.TDataInputStream;
import com.MyGame.Midlet.R;
import com.mykj.game.utils.AppConfig;

public class OwnMainTask {
	// 用户id
	int userId;
	// 用户任务数量
	private short taskCount;
	// 用户任务完成情况
	private OwnSubTask[] mOwnSubTask;
	
	public OwnMainTask(TDataInputStream dis){
		if(dis==null){
			return;
		}
		dis.setFront(false);
		userId=dis.readInt();
		taskCount=dis.readShort();
		if(taskCount<1){
			return;
		}
		mOwnSubTask=new OwnSubTask[taskCount];
		for(int i=0;i<taskCount;i++){
			mOwnSubTask[i]=new OwnSubTask(dis);
		}
	}
	
	public OwnMainTask(byte[] data){
		this(new TDataInputStream(data));
	}
	
			
	/**
	 * 获取子任务
	 * @return
	 */
	public OwnSubTask[] getmOwnSubTask() {
		return mOwnSubTask;
	}

	/**
	 * @Title: changeAction
	 * @Description: 改变任务的动作信息
	 * @param actionId
	 * @param changeCount
	 * @version: 2011-9-2 下午03:54:58
	 */
	public void changeAction(int actionId,int changeCount){
		if(mOwnSubTask==null) {
			return;
		}
		for(int i=0;i<taskCount;i++){
			OwnSubTask temp=mOwnSubTask[i];
			if(temp!=null&&temp.getStatus()==1){
				for(int j=0;j<temp.getActionCount();j++){
					OwnTaskActionDesc tempAction=temp.getOwnTaskActionDesc()[j];
					if(tempAction!=null&&tempAction.getActionID()==actionId){
						tempAction.setAddActionCounted(changeCount);
						
					}
				}
			}
		}
	}
	
	/**
	 * @Title: changeStatus
	 * @Description: 改变指定任务的状态
	 * @param taskId
	 * @param status
	 * @version: 2011-9-2 下午03:56:34
	 */
	public void changeStatus(int taskId,byte status){
		for(int i=0;i<taskCount;i++){
			OwnSubTask temp=mOwnSubTask[i];
			if(temp!=null&&temp.getTaskID()==taskId){
				temp.setStatus(status);
			}
		}
	}
	
	public String toString(){
		if(mOwnSubTask!=null&&mOwnSubTask.length!=0){
			int len=mOwnSubTask.length;
			for(int i=0;i<len;i++){
				OwnSubTask temp=mOwnSubTask[i];
				if(temp==null){
					continue;
				}
				if(temp.getOwnTaskActionDesc()!=null){
					for(int j=0;j<temp.getActionCount();j++){
						OwnTaskActionDesc tempAction=temp.getOwnTaskActionDesc()[j];
						if(tempAction.getActionCounted()==0){
							TaskItem mainInfo=GameTask.getTaskInfo(temp.getTaskID());
							if(mainInfo!=null){
								return mainInfo.getTaskInfo().getDesc();
							}
						}
					}
				}
				
			}
		}
		return null;
	}
	
	/**@Title: getCurrTaskInfo
	 * @Description: 获得当前任务的状态信息
	 * @return
	 * @version: 2011-9-2 下午05:30:58
	 */
	public String getCurrTaskInfo(){
		StringBuffer sb=new StringBuffer();
		if(mOwnSubTask!=null&&mOwnSubTask.length!=0){
			int len=mOwnSubTask.length;
			for(int i=0;i<len;i++){
				OwnSubTask temp=mOwnSubTask[i];
				if(temp==null){
					continue;
				}
				if(temp.getOwnTaskActionDesc()!=null){
					for(int j=0;j<temp.getActionCount();j++){
						OwnTaskActionDesc tempAction=temp.getOwnTaskActionDesc()[j];
						TaskItem mainInfo=GameTask.getTaskInfo(temp.getTaskID());
						if(mainInfo!=null&&tempAction!=null){
							TaskActionInfo actionInfo = mainInfo.getTaskActionInfos()[0];
							if(actionInfo!=null) {
								sb.append(AppConfig.mContext.getResources().getString(R.string.task_lable));
								sb.append(actionInfo.getActionName()+"("+tempAction.getActionCounted()+"/"+actionInfo.getActionCount()+") ");
								sb.append(mainInfo.getTaskGifts());
								return sb.toString();
							}
						}
					}
				}
				
			}
		}
		return "";
	}
	
	
	
	/**@Title: getUnFinText
	 * @Description: 获得当前任务的为完成信息
	 * @return
	 * @version: 2011-9-2 下午05:38:55
	 */
	public String getUnFinText(){
		StringBuffer sb = new StringBuffer();;
		if(mOwnSubTask!=null&&mOwnSubTask.length!=0){
			int len=mOwnSubTask.length;
			for(int i=0;i<len;i++){
				OwnSubTask temp=mOwnSubTask[i];
				if(temp==null){
					continue;
				}
				if(temp.getOwnTaskActionDesc()!=null){
					for(int j=0;j<temp.getActionCount();j++){
						OwnTaskActionDesc tempAction=temp.getOwnTaskActionDesc()[j];
						TaskItem mainInfo=GameTask.getTaskInfo(temp.getTaskID());
						if(mainInfo!=null&&tempAction!=null){
							TaskActionInfo actionInfo = mainInfo.getTaskActionInfos()[0];
							if(actionInfo!=null) {
								int leftCount = actionInfo.getActionCount() - tempAction.getActionCounted();
								/**任务已完成*/
								if(leftCount==0) {
									return null;
								}
								String str = mainInfo.getTaskInfo().getUnFinText();
								if(str!=null) {
									int indexD = str.indexOf('d');
									int index = str.indexOf("%");
									/**没有需要替换的字符串，直接返回*/
									if(indexD==-1&&index==-1) {
										return str;
									}
									/**替换需要替换的字符串*/
									if(indexD!=-1) {
										sb.append(str.substring(0,indexD-1));
									}
									sb.append(leftCount);
									if(index!=-1) {
										sb.append(str.substring(index+2));
									}
								}
								return temp.toString();
							}
						}
					}
				}
				
			}
		}
		return "";
	}

	
	
	public short getTaskCount() {
		return taskCount;
	}

	
	
}
