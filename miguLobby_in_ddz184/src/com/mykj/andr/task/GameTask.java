package com.mykj.andr.task;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.cocos2dx.util.GameUtilJni;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import com.mingyou.distributor.NetPrivateListener;
import com.mingyou.distributor.NetSocketPak;
import com.mykj.andr.model.HallDataManager;
import com.mykj.andr.model.UserInfo;
import com.mykj.andr.net.NetSocketManager;
import com.mykj.comm.io.TDataInputStream;
import com.mykj.comm.io.TDataOutputStream;
import com.mykj.game.FiexedViewHelper;
import com.mykj.game.utils.Log;

public class GameTask {
	private static final String TAG="GameTask";
	private static GameTask instance = null;
	//private Activity mActivity;

	/** 所有任务的详细信息 */
	private static List<TaskItem> mAllTaskList = null;

	/** 用户正在进行的任务的详细信息 */
	private static OwnMainTask mOwnMainTask = null;

	// -------------------------------------------------------------------------------------------------------------
	/** 任务主协议 **/
	private static final short MDM_GR_TASK = 40;
	/** 客户端请求接受任务子协议(C2S) */
	private static final short SUB_GR_TASK_QUERY = 4;
	/** 客户端请求取消任务子协议(C2S) */
	private static final short SUB_GR_TASK_CANCEL = 5;

	// --------------------------------------请求查询任务信息-----------------------------------------------

	/** 客户端请求查询任务信息子协议(C2S) **/
	private static final short SUB_GR_USER_QUERY_TASKINFO = 1;


	// ----------------------------------------------------------------------------------------------

	/** 用户完成任务子协议(S2C) */
	private static final short SUB_GR_TASK_COMPLETE = 6;
	/** 用户完成任务时的结束提示信息子协议(S2C) */
	private static final short SUB_GR_TASK_COMPLETE_MSG = 7;
	/** 用户完成任务后，接收到赠送物品的信息子协议(S2C) */
	private static final short SUB_GR_TASK_COMPLETE_GIFT_DATA = 8;
	/** 用户完成一局，下发有关动作数据(S2C) */
	private static final short SUB_GR_TASK_USER_ACTION = 9;


	// -------------------------------收发服务端发送用户当前任务信息:40-3--------------------------------------------
	/** 服务器发送用户当前任务信息子协议,用户自身正在进行的任务(S2C) */
	private static final short SUB_GR_TASK_USERINFO = 3;

	/** 服务器发送用户的任务信息（详细任务） **/
	private static final short SUB_GR_TASK_MAININFO = 2;


	/** 更新任务显示UI通知Handler **/
	private static final int UPDATA_USER_TASK_UI = 30;
	/**handler消息处理(用户完成一局，下发动作数据)***/
	public static final int HANDLER_TASK_USER_ACTION=409;


	// ------------------从C++那边迁移过来,增加自己修改------------------
	public enum TASK_LABEL_TYPE {
		TASK_LABEL_DESC, TASK_LABEL_STATUS, TASK_LABEL_ALL;
	}


	/** 获取所有任务的详细信息 */
	public static List<TaskItem> getAllTaskList() {
		return mAllTaskList;
	}

	/** 用户正在进行的任务的详细信息 */
	public static OwnMainTask getOwnMainTask() {
		return mOwnMainTask;
	}


	/**
	 * 通过任务id查询任务详情
	 * parm  taskid
	 * return TaskItem
	 */
	public static TaskItem getTaskInfo(int taskId) {
		if(mAllTaskList!=null){
			for (TaskItem temp: mAllTaskList) {
				if (temp.getTaskInfo().getTaskID() == taskId) {
					return temp;
				}
			}
		}
		return null;
	}


	/**
	 * @Title: hasTaskInfo
	 * @Description: 判断是否有任务信息
	 * @return
	 * @version: 2011-9-27 下午05:51:52
	 */
	public static boolean hasTaskInfo() {
		if (mOwnMainTask == null || mOwnMainTask.getTaskCount() <= 0
				|| mOwnMainTask.getCurrTaskInfo() == null
				|| mOwnMainTask.getCurrTaskInfo().trim().length() == 0) {
			return false;
		}
		return true;
	}



	private GameTask() {
	}

	/**
	 * 单例模式
	 * @param act
	 * @return
	 */
	public static GameTask getInstance() {
		if (instance == null) {
			instance = new GameTask();
		}
		return instance;
	}

	/***
	 * @Title: receiveListener
	 * @Description: 统一注册任务相关协议接受
	 * @version: 2013-1-8 下午05:49:59
	 */
	public void receiveListener() {
		receiveServerTaskInfo();                   //收发服务端发送任务信息:40-2
		receiveServerUserTaskInfo();               //收发服务端发送用户当前任务信息:40-3
		receiveHTask();                            //接受用户完成任务40--6
		doReceiveSUB_GR_TASK_COMPLETE_MSG();       //服务器发送完成任务时的结束提示信息40--7
		doReceiveSUB_GR_TASK_COMPLETE_GIFT_DATA(); //用户完成任务后，接收到赠送物品的信息返回40-8
		doReceiveSUB_GR_TASK_USER_ACTION();        //用户完成一局下发的有关动作数据 40---9
	}



	/**
	 * @Title: queryTaskInfo
	 * @Description: 请求查询任务信息(申请任务)  40---1
	 * @param userID
	 * @version: 2012-9-17 上午11:35:07 
	 */
	public void queryTaskInfo(int userID) {
		// 构造数据包
		TDataOutputStream tdous = new TDataOutputStream(false);
		tdous.writeInt(userID);
		NetSocketPak mConsumptionSock = new NetSocketPak(MDM_GR_TASK,
				SUB_GR_USER_QUERY_TASKINFO, tdous);
		// 发送协议
		NetSocketManager.getInstance().sendData(mConsumptionSock);
	}

	// -------------------------------------------------------------------------------------------------------------------

	/**
	 * @Title: querySubGRTask
	 * @Description: 请求接受任务
	 * @param userID
	 * @version: 2012-9-17 上午11:35:07
	 */
	public void querySubGRTask(int userID) {
		// 构造数据包
		TDataOutputStream tdous = new TDataOutputStream(false);
		tdous.writeInt(userID);

		NetSocketPak mConsumptionSock = new NetSocketPak(MDM_GR_TASK,
				SUB_GR_TASK_QUERY, tdous);
		// 发送协议
		NetSocketManager.getInstance().sendData(mConsumptionSock);
	}

	/***
	 * @Title: receiveHTask
	 * @Description:  (接受)用户完成任务40-6
	 * @version: 2013-1-8 下午05:44:35
	 */
	private void receiveHTask() {
		// 定义接受数据的协议
		short[][] parseProtocol = { { MDM_GR_TASK, SUB_GR_TASK_COMPLETE } };

		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				// 解析接受到的网络数据
				TDataInputStream tdis = netSocketPak.getDataInputStream();
				/** 用户ID */
				tdis.readInt();
				/** 任务ID */
				int taskId = tdis.readInt();
				/** 任务状态 */
				byte status = tdis.readByte();
				try {
					tdis.close();
				} catch (IOException e) {

				}
				/** 改变任务的状态 */
				mOwnMainTask.changeStatus(taskId, status);
				return true;
			}
		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		nPListener.setOnlyRun(false);
	}

	/**
	 * @Title: cancelSubGRTask
	 * @Description: 请求取消任务
	 * @param userID
	 *            用户id
	 * @version: 2012-9-17 下午03:32:57
	 */
	public void cancelSubGRTask(int userID) {
		// 构造数据包
		TDataOutputStream tdous = new TDataOutputStream(false);
		tdous.writeInt(userID);
		NetSocketPak mConsumptionSock = new NetSocketPak(MDM_GR_TASK,
				SUB_GR_TASK_CANCEL, tdous);
		// 发送协议
		NetSocketManager.getInstance().sendData(mConsumptionSock);
	}

	// ---------------------------------------------------------------------------------------




	/***
	 * @Title: updateTaskUserInfo
	 * @Description: 更新用户任务的完成情况
	 * @return
	 * @version
	 */
	public void updateTaskUserInfo() {		
		//OwnSubTask[] subTasks=GameTask.getOwnMainTask().getmOwnSubTask();
		OwnSubTask[] subTasks=null;
		OwnMainTask ownMainTask=GameTask.getOwnMainTask();
		if(ownMainTask!=null){
			subTasks=ownMainTask.getmOwnSubTask();
		}
		
		List<TaskItem> taskLists = GameTask.getAllTaskList();
		if (subTasks== null||subTasks.length <= 0||taskLists==null||taskLists.isEmpty()) {
			return;
		}
		for(OwnSubTask subTask : subTasks){
			int taskID = subTask.getTaskID();
			OwnTaskActionDesc[] taskActionDescs=subTask.getOwnTaskActionDesc();
			for (TaskItem taskItem: taskLists) {
				if (taskID == taskItem.getTaskInfo().getTaskID()) {

					// 遍历任务动作类
					TaskActionInfo[] taskActionInfos=taskItem.getTaskActionInfos();

					TaskInfo taskInfo= taskItem.getTaskInfo();
					String taskTitle=taskInfo.getTitle();
					String giftTip=taskInfo.getGiftTip();

					if(taskActionInfos.length!=taskActionDescs.length){
						Log.e(TAG, "task action info length error");
						break;
					}

					for(int i=0;i<taskActionInfos.length;i++){
						int actionCount = taskActionInfos[i].getActionCount();
						taskActionDescs[i].setmActionReqSum(actionCount);

						taskActionDescs[i].setTaskGiftTips(giftTip);
						taskActionDescs[i].setTaskTitle(taskTitle);

					}
				}

			}
		}

	}

	/**
	 * 获取用户任务的提示文本内容
	 * @param type
	 * @return
	 */
	public String getFormatTaskLabel(TASK_LABEL_TYPE type) {
		StringBuilder sb=new StringBuilder();		
		OwnSubTask[] subTasks=null;
		OwnMainTask ownMainTask=GameTask.getOwnMainTask();
		if(ownMainTask!=null){
			subTasks=ownMainTask.getmOwnSubTask();
		}
		
		if(subTasks==null||subTasks.length <= 0){
			return "";
		}
		for(OwnSubTask subTask:subTasks){
			OwnTaskActionDesc[] descs=subTask.getOwnTaskActionDesc();
			if(descs==null){
				continue;
			}
			for(OwnTaskActionDesc actiondesc: descs){
				String str=actiondesc.getTaskToString(type);
				if(str!=null&&str.length()>0){
					sb.append(str);	
				}
			}
			sb.append('\n');
		}
		return sb.toString();
	}





	/***
	 * @Title: getServerUserTaskInfo
	 * @Description: 收发服务端发送用户当前任务信息:40-3
	 * @version: 2012-9-17 下午07:23:11
	 */
	private void receiveServerUserTaskInfo() {
		// 定义接受数据的协议
		short[][] parseProtocol = { { MDM_GR_TASK, SUB_GR_TASK_USERINFO } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				// 解析接受到的网络数据
				TDataInputStream tdis = netSocketPak.getDataInputStream();
				tdis.setFront(false);

				if (mOwnMainTask != null){			
					OwnSubTask[] subTasks=mOwnMainTask.getmOwnSubTask();
					if(subTasks!=null){
						for(OwnSubTask sub:subTasks){
							J2CTaskData sendTask=sub.getSendTaskData();
							sendTask.briefInfo="";
							sendTask.detailedInfo="";
							sendTask.taskStatus=-1; //通知cpp清除任务数据
							GameUtilJni.updateJ2CTaskData(sendTask);
							//Log.v(TAG, "sendTask="+sendTask.toString());
							sub.cleanTaskSelf();
						}			
					}
					mOwnMainTask = null;
				}

				/** 读取用户任务数据 */
				mOwnMainTask = new OwnMainTask(tdis);
				// 更新用户任务数据显示
				updateTaskUserInfo();
				// 发送通知，更新房间内任务显示UI
				mTaskHandler.sendEmptyMessage(UPDATA_USER_TASK_UI);
				return true;
			}
		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		nPListener.setOnlyRun(false);
	}

	// -------------------------------------------------------------------------------------------------------------------

	/***
	 * @Title: receiveServerTaskInfo
	 * @Description: 收发服务端发送任务信息:40-2
	 * @version: 2012-9-17 下午07:23:11
	 */
	private void receiveServerTaskInfo() {
		// 定义接受数据的协议
		short[][] parseProtocol = { { MDM_GR_TASK, SUB_GR_TASK_MAININFO } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				// 解析接受到的网络数据
				TDataInputStream tdis = netSocketPak.getDataInputStream();

				//				if(mAllTaskList!=null){
				//					mAllTaskList.clear(); //此处不能clear。因为任务协议有分包的情况
				//				}
				MultiTask multiTask=new MultiTask(tdis);
				List<TaskItem> taskItem = multiTask.getMultiTaskList();
				if(taskItem!=null && taskItem.size()>0){
					if(mAllTaskList == null){
						mAllTaskList = new ArrayList<TaskItem>();
					}
					for(int i =0;i<taskItem.size();i++){
						mAllTaskList.add(taskItem.get(i));
					}
				}

				return true;
			}
		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		nPListener.setOnlyRun(false);
	}


	/** 用户完成任务时的结束提示信息通知Handler **/
	public static final int USER_HTASK_COMPLETE_HANDLER= 407;
	/**
	 * @Title: doReceiveSUB_GR_TASK_COMPLETE_MSG
	 * @Description: 用户完成任务时的结束提示信息返回 40-----7
	 * @version: 2013-1-8 下午05:30:40
	 */
	private void doReceiveSUB_GR_TASK_COMPLETE_MSG(){
		// 定义接受数据的协议
		short[][] parseProtocol = { { MDM_GR_TASK, SUB_GR_TASK_COMPLETE_MSG } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				
				//时长任务新增，游戏需要收到此消息
				byte[] buffer = netSocketPak.getBufferByte();
				GameUtilJni.parseGameNetData(buffer, buffer.length);
				
				// 解析接受到的网络数据
				TDataInputStream tdis = netSocketPak.getDataInputStream();
				tdis.setFront(false);
				/** 用户完成任务时的结束提示信息 */
				String mesg = tdis.readUTFShort();
				if(mesg!=null && mesg.length()>0){
					onTaskCompleteMsg(mesg);

					//Message msg=mTaskHandler.obtainMessage(USER_HTASK_COMPLETE_HANDLER);
					//msg.obj=mesg;
					//onTaskCompleteMsg(String.valueOf(msg.obj));
					//mTaskHandler.sendMessage(msg);
				}  
				return true;
			}
		};
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		nPListener.setOnlyRun(false);
	}


	/**
	 * @Title: doReceiveSUB_GR_TASK_COMPLETE_GIFT_DATA
	 * @Description: 用户完成任务后，接收到赠送物品的信息子协议(S2C)40---8
	 * 用户完成任务后，接收到赠送物品信息
	 * 目前不用处理，server会下发游戏结束的字符串信息
	 * @version: 2013-1-8 下午05:16:08
	 */
	private void doReceiveSUB_GR_TASK_COMPLETE_GIFT_DATA() {
		// 定义接受数据的协议
		short[][] parseProtocol = { { MDM_GR_TASK, SUB_GR_TASK_COMPLETE_GIFT_DATA } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				// 解析接受到的网络数据
				TDataInputStream tdis = netSocketPak.getDataInputStream();
				tdis.setFront(false);
				// userId
				tdis.readInt();
				// taskId
				tdis.readInt();
				// taskStatus
				short giftCount = tdis.readByte();
				if(giftCount < 0){
					return true;
				}
				TaskGiftInfo[] taskGiftItemInfos = new TaskGiftInfo[giftCount];
				for(int i = 0;i < giftCount;i++){
					taskGiftItemInfos[i] = new TaskGiftInfo(tdis);
				}
				return true;
			}
		};
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		nPListener.setOnlyRun(false);
	}


	/**
	 * @Title: doReceiveSUB_GR_TASK_USER_ACTION
	 * @Description: 解析用户完成一局下发的有关动作数据 40---9
	 * @version: 2011-9-2 下午02:32:45
	 */
	private void doReceiveSUB_GR_TASK_USER_ACTION() {
		// 定义接受数据的协议
		short[][] parseProtocol = { { MDM_GR_TASK, SUB_GR_TASK_USER_ACTION } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				// 解析接受到的网络数据
				TDataInputStream tdis = netSocketPak.getDataInputStream();
				tdis.setFront(false);
				/** 用户id */
				tdis.readInt();  //int userId=tdis.readInt(); // 未使用
				/** 变化动作 */
				int action = tdis.readInt();
				/** 动作变化量 */
				int changeActionCount = tdis.readInt();

				/** 改变任务的相关信息 */
				if (mOwnMainTask != null) {
					mOwnMainTask.changeAction(action, changeActionCount);
					// 发送通知，更新房间内任务显示UI
					mTaskHandler.sendEmptyMessage(UPDATA_USER_TASK_UI);
				}
				return true;
			}
		};
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		nPListener.setOnlyRun(false);
	}





	/**
	 * @Title: clrearTask
	 * @Description: 清理任务信息
	 * @version: 2011-9-20 上午10:17:54
	 */
	public void clrearTask() {
		if(mAllTaskList!=null){
			mAllTaskList.clear();
		}
		if (mOwnMainTask != null){			
			OwnSubTask[] subTasks=mOwnMainTask.getmOwnSubTask();
			if(subTasks!=null){
				for(OwnSubTask sub:subTasks){
					J2CTaskData sendTask=sub.getSendTaskData();
					sendTask.taskStatus=-1; //通知cpp清除任务数据
					GameUtilJni.updateJ2CTaskData(sendTask);
					Log.v(TAG, "sendTask="+sendTask.toString());
				}			
			}
			mOwnMainTask = null;
		}
	}


	/***************************************************
	 * 任务处理Handler
	 * 
	 **************************************************/

	@SuppressLint("HandlerLeak")
	public Handler mTaskHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case UPDATA_USER_TASK_UI: // 更新任务信息UI，服务下发40-2,40-3，组装数据后更新显示任务UI

				//获取多任务组合字符串，中间使用"\n"隔开，TextView直接显示为换行
				String detailAll=GameTask.getInstance().getFormatTaskLabel(TASK_LABEL_TYPE.TASK_LABEL_ALL);

				//任务TextView控件显示
				if(FiexedViewHelper.getInstance().freeRoomInfoFragment!=null){
					FiexedViewHelper.getInstance().freeRoomInfoFragment.setTaskInfo(detailAll);
				}
				//多任务传入cpp
				//OwnSubTask[] subTasks=GameTask.getOwnMainTask().getmOwnSubTask();
				
				OwnSubTask[] subTasks=null;
				OwnMainTask ownMainTask=GameTask.getOwnMainTask();
				if(ownMainTask!=null){
					subTasks=ownMainTask.getmOwnSubTask();
				}
				
				
				if(subTasks!=null){
					for(OwnSubTask sub:subTasks){
						J2CTaskData sendTask=sub.getSendTaskData();
						GameUtilJni.updateJ2CTaskData(sendTask);
						Log.v(TAG, "sendTask="+sendTask.toString());
					}
				}		
				break;
			case USER_HTASK_COMPLETE_HANDLER: 
				//onTaskCompleteMsg(String.valueOf(msg.obj));
				break;
			case HANDLER_TASK_USER_ACTION:     //一局完成后，应该再次请求任务
				UserInfo user=HallDataManager.getInstance().getUserMe();
				queryTaskInfo(user.userID); //申请任务
				break;                         
			default:
				break;
			}
		}
	};


	/***
	 * @Title: onTaskCompleteMsg
	 * @Description: 用户完成任务时的结束提示信息返回
	 * @param strMsg
	 * @version: 2013-1-11 下午06:03:10
	 */
	private void onTaskCompleteMsg(String strMsg){
		if(strMsg!=null){
			//int taskCount = 1;
			//GameUtilJni.updateTaskInfo(taskCount, "", strMsg);    //任务完成提示，通知cpp   
			//多任务传入cpp
			//OwnSubTask[] subTasks=GameTask.getOwnMainTask().getmOwnSubTask();
			OwnSubTask[] subTasks=null;
			OwnMainTask ownMainTask=GameTask.getOwnMainTask();
			if(ownMainTask!=null){
				subTasks=ownMainTask.getmOwnSubTask();
			}
			
			if(subTasks!=null){
				for(OwnSubTask sub:subTasks){
					J2CTaskData sendTask=sub.getSendTaskData();
					if(sendTask.taskStatus==2){
						sendTask.detailedInfo=strMsg;
						sendTask.briefInfo="";						
						GameUtilJni.updateJ2CTaskData(sendTask);
						//Log.v(TAG, "sendTask="+sendTask.toString());
						sub.cleanTaskSelf();
					}

				}
			}
		}
		UserInfo user=HallDataManager.getInstance().getUserMe();
		queryTaskInfo(user.userID); //申请任务
	}




}
