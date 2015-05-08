package com.mykj.andr.task;

import java.util.List;

import com.mykj.andr.task.GameTask.TASK_LABEL_TYPE;
import com.mykj.comm.io.TDataInputStream;

public class OwnSubTask {
	/**
	 * DWORD dwTaskID; //任务ID WORD wCount; //动作信息数量 BYTE Status; //状态
	 */
	// 任务id
	private int TaskID;

	// 任务动作信息数量
	private short actionCount;

	// 任务状态
	private byte Status;

	// 用户任务动作信息数据
	private OwnTaskActionDesc[] mOwnTaskActionDesc;

	public OwnSubTask(TDataInputStream dis) {
		if (dis == null) {
			return;
		}
		dis.setFront(false);
		TaskID = dis.readInt();
		actionCount = dis.readShort();
		Status = dis.readByte();
		if (actionCount < 1) {
			return;
		}
		mOwnTaskActionDesc = new OwnTaskActionDesc[actionCount];
		for (int i = 0; i < actionCount; i++) {
			mOwnTaskActionDesc[i] = new OwnTaskActionDesc(dis);
		}
	}

	public OwnSubTask(byte[] data) {
		this(new TDataInputStream(data));
	}

	public int getTaskID() {
		return TaskID;
	}

	public short getActionCount() {
		return actionCount;
	}

	public void setStatus(byte status) {
		Status = status;
	}

	public byte getStatus() {
		return Status;
	}

	public OwnTaskActionDesc[] getOwnTaskActionDesc() {
		return mOwnTaskActionDesc;
	}

	public J2CTaskData getSendTaskData() {
		StringBuilder sb1 = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();
		J2CTaskData data = new J2CTaskData();
		data.taskId = TaskID;
		data.taskStatus = Status;

		if (mOwnTaskActionDesc != null) {
			for (OwnTaskActionDesc des : mOwnTaskActionDesc) {
				String str1 = des.getTaskToString(TASK_LABEL_TYPE.TASK_LABEL_STATUS); // 简单
				String str2 = des.getTaskToString(TASK_LABEL_TYPE.TASK_LABEL_DESC); // 详细
				sb1.append(str1);
				sb2.append(str2);

				if (des.getTaskStatus() == 2) {
					data.taskStatus = 2;
				}
			}
			data.briefInfo = sb1.toString();
			data.detailedInfo = sb2.toString();
		}

		List<TaskItem> taskLists = GameTask.getAllTaskList();
		if (taskLists != null) {
			for (TaskItem taskItem : taskLists) {
				if (TaskID == taskItem.getTaskInfo().getTaskID()) {
					data.giftInfo = taskItem.getTaskInfo().getUnFinText();
					TaskGiftInfo[] gifts = taskItem.getTaskGifts();
					if (gifts != null) {
						for (TaskGiftInfo item : gifts) {
							if (item != null) {
								data.giftType = item.getType();
							}
						}
					}
				}
			}
		}

		return data;
	}

	public void cleanTaskSelf() {
		TaskID = 0;
		actionCount = 0;
		Status = 0;
		mOwnTaskActionDesc = null;
	}

}
