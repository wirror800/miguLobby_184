package org.cocos2dx.util;

import java.io.File;
import java.util.Calendar;

import org.cocos2dx.lib.Cocos2dxActivity;

import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import com.mingyou.login.LoginSocket;
import com.mingyou.login.TcpShareder;
import com.mykj.andr.headsys.HeadManager;
import com.mykj.andr.model.HallDataManager;
import com.mykj.andr.model.NodeData;
import com.mykj.andr.model.PlatInfo;
import com.mykj.andr.model.RoomConfigData;
import com.mykj.andr.model.RoomData;
import com.mykj.andr.model.RoomInfoEx;
import com.mykj.andr.model.UserInfo;
import com.mykj.andr.task.J2CTaskData;
import com.mykj.andr.ui.fragment.Cocos2dxFragment;
import com.mykj.comm.log.MLog;
import com.mykj.game.FiexedViewHelper;
import com.mykj.game.ddz.api.AnalyticsUtils;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.Log;
import com.mykj.game.utils.Util;

public class GameUtilJni {
	private static String TAG = "GameUtilJni";

	public static Context mContext;
	
	/*****************************************************/
	//分区通知游戏事件定义
	//native函数onZoneEvent() 传入eventId
	/**快捷购买 **/
	public static final int EventZone_ShortcutBuy = 1;
	/**通知游戏统一返回分区***/
	public static final int EventZone_BackToCardZone=2;
	/** 重连开始 **/
	public static final int EventZone_BeginReconnect = 3;	
	/** 重连成功**/
	public static final int EventZone_ReconnectSucc = 4;	
	/** 通知游戏中返回登录**/
	public static final int EventZone_GameReLogin = 5;
	/** 游戏中发送购买请求**/
	public static final int EventZone_GameBuy = 6;
	
	public static final int EventZone_QuickGame = 7;
	
	/**游戏返回比赛报名界面*/
	public static final int EventZone_BackToMatchView=8;
	
	
	/*****************************************************/
	//游戏通知分区事件定义
    /** @brief  通知分区进行重连 */
	public static final int GameEvent_RequestReconnect = 1;
    /** @brief  通知分区处理游戏内快捷购买 */
	public static final int GameEvent_ShowGameQuickBuy = 2;
	
	/*****************************************************/
	
	
	
	
	/**
	 * 此类必须初始化
	 * @param context
	 */
	public static void init(Context context) {
		mContext = context;
	}



	/**
	 *************以下为java调用 C++接口******************
	 ************************************************
	 ************************************************
	 ************************************************
	 ************************************************
	 ************************************************/


	/**
	 * 进入游戏， 由分区调用， 由游戏实现
	 * 
	 * @return bool 转入游戏成功 返回true,失败或异常返回false
	 */
	public static native boolean intoGame(UserInfo myselfInfo, int mode);


	/**
	 * 初始化游戏资源，由分区调用，游戏实现，一般在转入游戏之前调用，可省略
	 * 
	 * @return bool: 有加载并成功则返回true,否则返回false
	 * ******/
	public static native boolean initGameRes();




	/** 网络数据协议解析，由网络模块调用，由游戏实现 **/
	public static native int parseGameNetData(byte[] buf, int Length);



	/** 网络异常，由网络模块调用，由游戏实现 */
	public static native void parseNetError(int code, String info);



	/**
	 * 六. 任务相关--暂定 任务在游戏中只是用于显示，不做交互处理  --- 作废
	 * 任务信息，由分区调用（分区根据协议组装任务描述串），由游戏实现（游戏接收任务描述并显示）
	 * 
	 * @param taskCount
	 *            当前任务数 （多个任务时任务描述用分割符"|@|"间隔）
	 * @param briefInfo
	 *            简短的任务描述，可选使用，如 局数任务中的2/5
	 * @param detailedInfo
	 *            详尽完整的任务描述
	 * @deprecated 请使用void updateJ2CTaskData(J2CTaskData taskData)
	 */
	public static native void updateTaskInfo(int taskCount, String briefInfo, String detailedInfo);

	/** 多任务接口 */
	public static native void updateJ2CTaskData(J2CTaskData taskData);


	/**
	 * 分区通知游戏的通用接口，由分区调用，由游戏实现
	 * 
	 * @param eventId
	 *        事件ID
	 */
	public static native void onZoneEvent(int eventId);


	/**
	 * 分区通知游戏的喜报消息
	 * 
	 * @param eventId
	 *        事件ID
	 */
	public static native void onGoodNews(String context);
	
	/**系统事件ID--电量*/
	public static final int SystemEvent_PhonePower = 1;
	/**系统事件ID--信号量*/
	public static final int SystemEvent_PhoneSignal = 2;
	/**系统事件ID--网络类型*/
	public static final int SystemEvent_NetType = 3;
	/**
	* 更新系统事件，由系统广播触发分区调用，由游戏实现
	* @param eventId 事件id,见表定义
	* @param eventValue 事件值， 见表定义
	*/
	public static native void updateSystemEvent(int eventId,int eventValue);
	
	
	
	/**
	 * 分区提示游戏购买提示
	 * @param context
	 */
	public static native void onQuickBuyMsg(String context);
	
	
	/**
	 * 刷新用户钻石持有量
	 * @param sum
	 */
	public static native void refreshDiamond(int sum);

	
	/**
	 * 刷新用户昵称
	 * @param nickname
	 */
	public static native void refreshNickName(String nickname);
	
	/*********************************************/





	/**
	 *************以下为 C++调用java接口******************
	 ************************************************
	 ************************************************
	 ************************************************
	 ************************************************
	 ************************************************/



	/*****
	 * @Title: getRoomInfo
	 * @Description: 获得当前房间位置信息，由游戏调用，由分区实现
	 * @return
	 * @version: 2012-12-14 下午06:10:53
	 */
	public static RoomData getRoomInfo() {
		return HallDataManager.getInstance().getCurrentRoomData();
	}

	/**
	 * 获得当前所在分区的信息，由游戏调用，由分区实现
	 * 
	 * @return NodeData同协议中的分区节点结构
	 * @create by 2012.12.24
	 */
	public static NodeData getZoneInfo() {
		return HallDataManager.getInstance().getCurrentNodeData();
	}


	/***
	 * @Title: getRoomConfigInfo
	 * @Description:获取房间配置的接口
	 * @return
	 * @version: 2012-12-25 下午02:12:08
	 */
	public static RoomConfigData getRoomConfigInfo() {
		return HallDataManager.getInstance().getRoomConfigData();
	}
	/**
	 * 统计点击事件， 给游戏调用
	 * @param eventId 在友盟后台有对应的eventid
	 */
	public static void analyticsClickEvent(String eventId){
		AnalyticsUtils.onClickEvent(mContext, eventId);
	}


	/**
	 * 显示过渡界面,由分区实现，由游戏调用（如果已显示则不处理） 常用于游戏和分区跳转时，或者游戏中需求等待时
	 */
	public static void showLoadingView() {
		Log.e(TAG, "showLoadingView");
		FiexedViewHelper.getInstance().showAddCocos2dLoading();
	}



	/**
	 * 隐藏过渡界面,由分区实现，由游戏调用（如果已隐藏则不处理） 常用于游戏和分区跳转时
	 */
	public static void hideLoadingView() {
		Message msg = Cocos2dxActivity.handler.obtainMessage();
		msg.what = Cocos2dxActivity.HANDLER_HIDE_BG;
		Cocos2dxActivity.handler.sendMessage(msg);
		
//		Log.e(TAG, "hideLoadingView");
//		FiexedViewHelper.getInstance().removeCococs2dLoading();
	}


	/**
	 * 三. 从游戏中返回 返回分区（游戏未开始，游戏已开始），由游戏调用，由分区实现
	 * 
	 * @param bean
	 *            : 用户的乐豆(8个字节)
	 * @return bool: 跳转成功返回true,否则返回false
	 */
	public static boolean backZoneView(long bean) {
		Handler handler=FiexedViewHelper.getInstance().sHandler;
		Message msg = handler.obtainMessage();
		msg.what = FiexedViewHelper.HANDLER_SWITCH;
		Bundle data = new Bundle();
		data.putLong(FiexedViewHelper.BEAN, bean);
		msg.setData(data);
		handler.sendMessage(msg);
		return true;
	}


	/**
	 * 返回到登录，由游戏调用，由分区实现
	 * 
	 * @return bool: 跳转成功返回true,否则返回false
	 */
	public static boolean backLoginView() {
		Handler handler=FiexedViewHelper.getInstance().sHandler;
		Message msg = handler.obtainMessage();
		msg.what = FiexedViewHelper.HANDLER_LOGIN_VIEW;
		handler.sendMessage(msg);
		return true;
	}

	
	public static boolean backMatchView(){
		Handler handler=FiexedViewHelper.getInstance().sHandler;
		Message msg = handler.obtainMessage();
		msg.what = FiexedViewHelper.HANDLER_MATCH_VIEW;
		handler.sendMessage(msg);
		return true;
	}
	

	/** 发送网络协议数据，由分区实现，由游戏调用 */
	public static void sendNetData(byte[] buf) {
		TcpShareder.getInstance().reqNetData(buf);
	}



	/**
	 * 存储 用于游戏中存储需要的数据，根据需求每个游戏可能不一样，如：声音开关状态等 存储记录，由游戏调用，由分区实现
	 * 
	 * @param gameId
	 *            游戏ID，存储关联的游戏
	 * @param key
	 *            记录的键值
	 * @param value
	 *            需要存的数据
	 * @return bool 存储成功返回true,否则返回false
	 */
	public static boolean writeRecords(int gameId, String key, String value) {
		boolean result = false;
		try {
			SharedPreferences mPerferences = PreferenceManager.getDefaultSharedPreferences(mContext);
			Editor editor = mPerferences.edit();
			editor.putString(key, value);
			editor.commit();
			result = true;
		} catch (Exception e) {
			result = false;
		}
		return result;
	}

	/**
	 * 读取 读取记录，由游戏调用，由分区实现
	 * 
	 * @param gameId
	 *            游戏ID，记录关联的游戏
	 * @param key
	 *            记录的键值
	 * @return string 键值对应的存储数据，没有记录时返回""（长度为0的空字符串）。
	 */
	public static String readRecords(int gameId, String key) {
		SharedPreferences mPerferences = PreferenceManager.getDefaultSharedPreferences(mContext);
		return mPerferences.getString(key, "");
	}

	/**
	 * 获得当前时间
	 * 
	 * @return
	 */
	public static String getTime() {
		StringBuffer sb=new StringBuffer();
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minutes = c.get(Calendar.MINUTE);
		if(hour < 10){
			sb.append(0);
			sb.append(hour);
		}else{
			sb.append(hour);
		}
		sb.append(':');
		if(minutes<10){
			sb.append(0);
			sb.append(minutes);
		}else{
			sb.append(minutes);
		}
		return sb.toString();

	}



	/** 
	 * 获取SD根目录路径--可选实现，由分区实现 
	 * */
	public static String getSDCardDir() {
		String dir = " ";
		try {
			dir = Environment.getExternalStorageDirectory() + "/";
		} catch (Exception e) {
		}
		return dir;
	}



	/** 
	 * 获得包名，由分区实现 
	 */
	public static String getPackageName() {
		String path = null;
		try {
			path = mContext.getPackageName();
		} catch (Exception e) {
			path = "com.MyGame.Midlet";
		}

		return path;
	}




	/** 判断指定文件是否存在 不存在返回-1，由分区实现 */
	public static int isFileExist(String file) {
		File mfile = new File(Environment.getExternalStorageDirectory() + AppConfig.DOWNLOAD_FOLDER,file);
		if (!mfile.exists()) {
			return -1;
		}
		return 1;
	}



	/** 直接退出程序，杀掉进程，由分区实现 */
	public static void exitApplication() {	
		LoginSocket.getInstance().closeNet();
		Log.v(TAG, "CExitApplication");
		android.os.Process.killProcess(android.os.Process.myPid());
	}


	/**
	 * 获得背景图片路径，用于换背景（暂时不用）
	 * @return
	 */
	public static String getBackGroundResPath(){
		String path="";
		if(AppConfig.bgPath!=null){
			path=AppConfig.bgPath;
		}
		return path;
	}
	
	/**
	 * 
	 * @param title	
	 * @param description
	 * @param resId 缩略图id 目前0为ddz_thumb，1为dbs_thumb
	 */
	public static void startWxActivity(String title, String description,int resId){
		Message msg = Cocos2dxActivity.handler.obtainMessage();
		msg.what = Cocos2dxActivity.HANDLER_WEIXIN_SHARE;
		Bundle bundle=new Bundle();
		bundle.putString("wx_title", title);
		bundle.putString("wx_description", description);
		bundle.putInt("wx_thumb", resId);
		msg.setData(bundle);
		Cocos2dxActivity.handler.sendMessage(msg);
	}
	
	
	
	/**
	 * @Title: VibrateShake
	 * @Description: 震动，震动模式为只震1秒
	 * @version: 2013-5-10 下午02:16:48
	 */
	public static void VibrateShake(int time){
		if(time<100){
			time = 100; //至少0.1秒
		}
		if(mContext==null)
			return;
		Vibrator vib = (Vibrator) mContext.getSystemService(Service.VIBRATOR_SERVICE);  
	    vib.vibrate(time);   
	}
	
	
	/**
	 * 游戏中快速购买接口
	 * 1 为显示游戏快捷支付
	 *-1为不显示游戏快捷支付
	 */
	public static boolean isGameBuyShow(){		
		boolean res=false;

		return res;

	}
	
	
	/**
	 * 游戏通知分区的通用接口，由游戏调用，由分区实现
	 * 
	 * @param eventId
	 *        事件ID
	 *  
	 */
	public static  void onGameEvent(int eventId){
		switch(eventId){
		case GameEvent_RequestReconnect:   //使用时候自行添加事件宏定义

			FiexedViewHelper.getInstance().reCutLoginAgain(true);
			break;
		case GameEvent_ShowGameQuickBuy:   //使用时候自行添加事件宏定义
			Message msg = Cocos2dxActivity.handler.obtainMessage();
			msg.what = Cocos2dxActivity.HANDLER_SHOW_BUY_DIALOG;
			Cocos2dxActivity.handler.sendMessage(msg);
			break;
		case 3:   //使用时候自行添加事件宏定义
			break;
		default:
			break;
		}
	}
	
	/**
	 * 游戏内购买道具
	 * @param propId
	 */
	public static void gameBuy(int propId){
		Message msg = Cocos2dxActivity.handler.obtainMessage();
		msg.what = Cocos2dxActivity.HANDLER_GAME_BUY;
		msg.arg1=propId;
		Cocos2dxActivity.handler.sendMessage(msg);
	}
	

	/**
	 * 游戏中输出日志（使用分区统计日志方式）
	 * @param type 日志类型： <=1:VERBOSE 2:DEBUG 3:INFO 4:WARN 5:ERROR
	 * @param text 日志内容
	 */
	public static void gameLog(int type,String text){
		if(text == null||text.length()<=0){
			return;
		}
		String gameTag = "GameLog";
		switch(type){
		case 2:
			MLog.d(gameTag,text);
			return;
		case 3:
			MLog.i(gameTag,text);
			return;
		case 4:
			MLog.w(gameTag,text);
			return;
		case 5:
			MLog.e(gameTag,text);
			return;
		default:
		{
			if(type<=1){
				MLog.v(gameTag,text);
			}
		}
		return;
		}
	}

	
	

	/**
	 * 绘制奖状到Bitmap,并保存到sdcard
	 * 
	 * @param userName
	 *            :玩家昵称
	 * @param matchName
	 *            ：比赛名称
	 * @param rank
	 *            ：排名
	 * @param date
	 *            ：日期
	 * @param awards
	 *            ：奖品
	 * @param awardCount
	 *            ：奖品个数
	 */
	public static void saveCommendation(String userName, String matchName, int rank, String date, String[] awards, int awardCount) {
		Message msg = Cocos2dxActivity.handler.obtainMessage();
		msg.what = Cocos2dxActivity.HANDLER_SAVE_PICTURE;
		Bundle bundle=new Bundle();
		bundle.putString("userName", userName);
		bundle.putString("matchName", matchName);
		bundle.putInt("rank", rank);
		bundle.putString("date", date);
		bundle.putStringArray("awards", awards);
		bundle.putInt("awardCount", awardCount);
		msg.setData(bundle);
		Cocos2dxActivity.handler.sendMessage(msg);
		
	}

	/**
	* 获取指定的系统事件信息， 由游戏调用，由分区实现
	*@param eventId 事件id，见表定义
	*@return int  事件对应的值
	*/
	public static int getSystemEvent(int eventId){
		int reVlaue = 0;
		switch(eventId){
		case SystemEvent_PhonePower:
			reVlaue = AppConfig.phonePower; //需实现
			break;
		case SystemEvent_PhoneSignal:
			reVlaue = AppConfig.phoneSignal; //需实现
			break;
		case SystemEvent_NetType:
			reVlaue =AppConfig.phoneApnType; //需实现
			break;
		}
		return reVlaue;
	}
	
	
	/**
	 * 获取房间附件信息
	 * @return
	 */
	public static RoomInfoEx getRoomInfoEx(){
		return AppConfig.getRoomInfoEx();
	}
	

	
	/**
	 * talking data 统计接口
	 * @param str
	 */
	public static void talkingData(String str){
		/*if(!Util.isEmptyStr(str)){
			NodeData node=HallDataManager.getInstance().getCurrentNodeData();
			if(node!=null){
				String s=node.Name+"-"+str;
				((CustomActivity)mContext).doTalkingData(s);
			}

		}*/
	}
	
	/**
	 * 
	 * 获取头像路径
	 * @param id
	 * @return 头像路径，如果没有则返回null
	 */
	public static String getHeadImgPath(int id){
		return HeadManager.getInstance().getImgFullFileName(id);
	}
	
	
	/**
	 * 获取平台相关信息
	 * @return
	 */
	public static PlatInfo getPlatInfo(){
		PlatInfo info = new PlatInfo();
		info.gameId = (short) AppConfig.gameId;
		info.clientId = (short) AppConfig.clientID;
		try{
			info.subClientId = Short.parseShort(AppConfig.childChannelId);
		}catch(Exception e){
			
		}
		info.clientVer = Util.getProtocolCode(Util.getVersionName(mContext));
		return info;
	}
	
	
	/**
	 * 是否是移动包月会员
	 * @return
	 */
	public static boolean getIsCmccVip(){
		return false;
	}
	
	
	
	/**
	 * 游戏中显示帮助窗口
	 */
	public static void popVideoGameHelp(int urlId){
		
//		Cocos2dxFragment frag=FiexedViewHelper.getInstance().cocos2dxFragment;
//		Handler handler=frag.getHandler();
//		
//		Message msg = handler.obtainMessage();
//		msg.arg1=urlId;
//		msg.what = Cocos2dxFragment.HANDLER_MMVIDEO_HELP;
//		handler.sendMessage(msg);
		
	
	}
	
	
	/**
	 * 游戏中修改用户昵称
	 */
	public static void popModiyNickName(){
		
//		Cocos2dxFragment frag=FiexedViewHelper.getInstance().cocos2dxFragment;
//		Handler handler=frag.getHandler();
//		
//		Message msg = handler.obtainMessage();
//		msg.what = Cocos2dxFragment.HANDLER_MODIY_NICKNAME_START;
//		handler.sendMessage(msg);
	
	}
	
	
	
	
}
