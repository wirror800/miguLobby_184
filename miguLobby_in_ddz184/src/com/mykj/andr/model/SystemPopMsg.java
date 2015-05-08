package com.mykj.andr.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

import com.mykj.andr.provider.ActionInfoProvider;
import com.mykj.andr.ui.BackPackActivity;
import com.mykj.andr.ui.MarketActivity;
import com.mykj.andr.ui.MixGridActivity;
import com.mykj.andr.ui.widget.CardZoneProtocolListener;
import com.mykj.andr.ui.widget.SysPopDialog;
import com.mykj.comm.io.TDataInputStream;
import com.mykj.comm.io.TDataInputStream.MDataMark;
import com.mykj.game.FiexedViewHelper;
import com.mykj.game.utils.Log;
import com.mykj.game.utils.UtilHelper;

public class SystemPopMsg implements OnDismissListener {
	// 1：维护公告 2：活动公告
	// 3：赛事推广 0：其他

	public static final short MSG_ANN = 1;

	public static final short MSG_ACT = 2;

	public static final short MSG_POP = 3;

	// 1：登陆弹
	// 2：退出弹
	// 3：定时弹一次（时间可配置）
	// 4：定时弹多次

	public static final short POP_LOGIN = 1;

	public static final short POP_EXIT = 2;

	public static final short POP_TIME = 3;

	public static final short POP_INTERVAL = 4;

	//总消息条数，可能多个包发送
	public short totalCount;

	private static final String TAG = "SystemPopMsg";
	private ArrayList<PopMsgItem> popMsgList = new ArrayList<SystemPopMsg.PopMsgItem>();
	private SysPopDialog curDialog = null; //当前消息框
	private Context context;
	// 若ButtonType为1
	// 1：打开wap链接
	// 2：打开商城
	// 3：打开背包
	// 4：打开合成
	// 若ButtonType为2
	// 节点ID
	public static final short BTNTYPE_WAP = 1;

	public static final short BTNTYPE_SHOP = 2;

	public static final short BTNTYPE_PACK = 3;

	public static final short BTNTYPE_COM = 4;

	//private static final int UPDATE_CURRENT_SYSTEM_TIME = 1000;

	private int curRound = 0;  //轮次，每次newRound后前面轮的都不处理了
	private String mAction = "mykj.game.intent.action.systemPopMsg"; //闹钟action
	private AlarmManager mAlarmManager;
	private int indexAdd = 0;   //索引偏移量，因为可能多个包
	//private boolean showExitPop = false; //当前是否在显示退出弹框
	
	public SystemPopMsg(TDataInputStream dis, Context context) {
		this.context = context;
		mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		
		//注册广播,接收延时操作
		try{
			context.registerReceiver(mPopMsgReceiver, new IntentFilter(mAction));
		}catch(Exception e){
			Log.i(TAG, e.getMessage());
		}
		//新的一轮，初始化
		newRound();
		
		//解析网络数据
		readMsg(dis);
	}

	/**
	 * @return 是否正显示退出强弹框
	 */
	/*public boolean isExitPopShowing(){
		return showExitPop;
	}*/

	/**
	 * 读取网络数据
	 * @param dis 网络输入流
	 */
	synchronized public void readMsg(TDataInputStream dis) {

		short total = dis.readShort(); // 总消息数
		short count = dis.readShort(); // 本包消息数
		
		//设置总消息数
		if (totalCount == 0 && total != 0) {
			totalCount = total;
		}

		for (int i = 0; i < count; i++) {
			short popDataLen = dis.readShort();
			MDataMark mark = dis.markData(popDataLen);
			popMsgList.add(new PopMsgItem(dis, i + indexAdd, curRound));
			dis.unMark(mark);
		}
		indexAdd += count;
	}
	
	/**
	 * 新一轮，初始化一些参数
	 */
	public void newRound(){
		clearIntent();
		popMsgList.clear();
		curRound++;
		indexAdd = 0;
		totalCount = 0;
	}
	
	/**
	 * @return 获得已有消息数
	 */
	public short getMsgSize() {
		if (popMsgList == null) {
			return 0;
		}
		return (short) popMsgList.size();
	}
	
	
	/**
	 * 清除已注册的延时广播
	 */
	private void clearIntent() {
		synchronized (popMsgList) {
			for (PopMsgItem item : popMsgList) {
				Intent intent = new Intent();
				intent.setAction(mAction);
				intent.putExtra("itemID", item.id);
				intent.putExtra("round", item.mRound);
				PendingIntent pi = PendingIntent.getBroadcast(context, item.id,
						intent, PendingIntent.FLAG_UPDATE_CURRENT);
				mAlarmManager.cancel(pi);
			}
		}
	}

	/**
	 * 销毁
	 */
	public void onDestroy(){
		clearIntent();
		try{
			context.unregisterReceiver(mPopMsgReceiver);
		}catch(Exception e){
			Log.i(TAG, e.getMessage());
		}
	}
	
	/**
	 * @author Administrator
	 * 弹出消息项，成员详细解析请参考手机协议
	 */
	public class PopMsgItem {
		public String title;    //标题
		public int titleColor = 0;   //标题颜色，0表示默认值
		public SpannableStringBuilder msg; //显示内容

		public short msgType;   //消息类型， MSG_ANN; MSG_ACT; MSG_POP 中的一个

		public short popType;	//弹出类型	POP_LOGIN，POP_EXIT，POP_TIME，POP_INTERVAL中的一个

		public short startTime; //起始时间，对POP_LOGIN，POP_EXIT无效

		public short endTime;	//结束时间，对POP_LOGIN，POP_EXIT无效

		public short popInterval; //间隔时间，对POP_LOGIN，POP_EXIT无效

		public short buttonType;  //按键类型，0为无按钮，1为去看看，2为报名

		public int buttonExt; //按键附加信息，BTNTYPE_WAP，BTNTYPE_SHOP，BTNTYPE_PACK，BTNTYPE_COM之一

		public String url;	//若是wap则是跳转连接

		public int id; 
		public int mRound;  //轮次，只处理最新轮次的
		
		public int popKind;  //玩法类型，若为0则都可弹出，否则跟当前游戏玩法一致才可弹出
		
		public int UrlOpenType;
		
		public PopMsgItem(TDataInputStream dis, int id, int round) {
			String content = dis.readUTFShort(); //若没有*#*则没有标题，否则前面是标题后面是内容
			msgType = dis.readShort();
			popType = dis.readShort();
			startTime = dis.readShort();
			endTime = dis.readShort();
			popInterval = dis.readShort();
			buttonType = dis.readShort();
			buttonExt = dis.readInt();
			url = dis.readUTFShort();
			this.id = id;
			mRound = round;
			int blockNum = 0;
			if(content != null && content.trim().length() > 0){
				int div = content.indexOf("*#*");
				if(div > 0 && div < content.length()){
					title = content.substring(0, div);
					content = content.substring(div + 3);
				}
				msg = new SpannableStringBuilder(content);
				try{
					/** 字体颜色等信息，由*号分成2+3n个数字(可以是16进制)串，n为第2个数字
					 *  第一个数字表示标题的颜色，若为0则使用默认，可以是十六进制和10进制
					 *  第2个数字表示色块个数
					 *  以后每3个组成一个色块信息
					 *  色块第一个数表示起始位置，以content的第一个字符为0
					 *  色块第二个数表示色块长度
					 *  色块第三个数表示色块颜色
					 */
					String ex = dis.readUTFShort(); //附加信息，由上面描述
					String[] info = ex.split("\\*");
					titleColor = parseInt(info[0]); 
					if((titleColor & 0xff000000) == 0 && (titleColor != 0)){
						titleColor |= 0xff000000;
					}
					blockNum = parseInt(info[1]); //色块个数
					for(int i = 0; i < blockNum; i++){
						int start = parseInt(info[i * 3 + 2]);
						int len = parseInt(info[i * 3 + 3]);
						int color = parseInt(info[i * 3 + 4]);
						
						int end = start + len;
						if(start >= content.length())
							start = content.length() - 1;
						if(end > content.length())
							end = content.length();
						if((color & 0xff000000) == 0 && (color != 0)){
							color |= 0xff000000;
						}
						msg.setSpan(new ForegroundColorSpan(color),start,end,Spannable.SPAN_EXCLUSIVE_INCLUSIVE); //设置指定位置文字的颜色
					}
					
					
				}
				catch (Exception e){
					e.printStackTrace();
				}

			}
			
			popKind = dis.readShort();
			UrlOpenType=dis.readShort();
			
			Log.i(TAG, "new Item " + popType + " start: " + startTime + " end: "+endTime + " interval:" + popInterval);
		}
		@SuppressLint("DefaultLocale")
		int parseInt(String str){
			int rlt = 0;
			try{
				String temp = str.trim().toLowerCase(Locale.getDefault()); //提取内容，去空格，转小写
				int radix = 10; //转换进制，默认十进制，0x开始为16进制，0开始为8进制，十六进制需将0x去掉，八进制前面那个0可以不去掉
				if(temp.startsWith("0x")){
					radix = 16;
					temp = temp.substring(2);
				}else if(temp.startsWith("-0x")){
					radix = 16;
					temp = "-" + temp.substring(3);
				}else if(temp.startsWith("0")){
					radix = 8;
				}else if(temp.startsWith("-0")){
					radix = 8;
				}
				rlt = Integer.parseInt(temp, radix);
			}catch (Exception e){
				Log.e(TAG, "parse wrong! " + str + " can't parse to a integer");
			}
			return rlt;
		}
	}

	/**
	 * 按钮点击处理
	 * @param act
	 * @param item
	 */
	static public void toWhere(Context act, PopMsgItem item) {
		if(item == null){
			return;
		}
		if(item.buttonType == 1){
			if (item.buttonExt == BTNTYPE_WAP) {
				if (item.url != null && !"".equals(item.url.trim())) {
					UtilHelper.onWeb(act, item.url,item.UrlOpenType);
				}
			} else if (item.buttonExt == BTNTYPE_SHOP) {
				act.startActivity(new Intent(act, MarketActivity.class));
			} else if (item.buttonExt == BTNTYPE_PACK) {
				Intent intent = new Intent(act, BackPackActivity.class);
				act.startActivity(intent);
			} else if (item.buttonExt == BTNTYPE_COM) {
				Intent intent = new Intent(act, MixGridActivity.class);
				intent.putExtra("entry", "menu");
				act.startActivity(intent);
			}
		}else{
			List<NodeData> lists=AllNodeData.getInstance(act).getAllNodeDate();
			if(lists != null && lists.size() > 0){
				for(NodeData data:lists){
					if(data.ID==item.buttonExt){
						CardZoneProtocolListener.getInstance((Activity)act).invokeCardZoneListViewItem(data);
						return;
					}
				}
			}
		}
	}

/*	private String leftBtn = null;
	
	private void initBtn(short buttonType) {
		if (buttonType == 0) {
			leftBtn = "";		
		} else if (buttonType == 1) {
			leftBtn = "去看看";
		} else if (buttonType == 2) {
			leftBtn = "报名";
		}
	}*/

	/**
	 * 显示立即弹框，登录弹和退出弹属于这种
	 * @param type POP_LOGIN POP_EXIT
	 * @return 是否显示，若服务器没有下发该类弹框则不显示，返回false
	 */
	private boolean showImmediateDialog(short type) {
		boolean showed = false;
		PopMsgItem tempItem= null;
		int curGameType = FiexedViewHelper.getInstance().getGameType();
		synchronized (popMsgList) {
			for (int i = popMsgList.size() - 1; i >= 0; i--) {
				PopMsgItem item = popMsgList.get(i);
				if (item.popType == type) {
					if (type == POP_LOGIN) {
						//有当前游戏特定模式的优先显示，否则显示通用的
						tempItem = item;
						popMsgList.remove(item);
						if(tempItem.popKind == curGameType){ //特定的优先显示
							ActionInfoProvider.getInstance().setNoticeItem(tempItem);
							tempItem = null;
							Log.i(TAG, "pop a kind login");
							break;
						}else if(tempItem.popKind != 0){ //不符合要求
							tempItem = null;
						}
						
					} else if (type == POP_EXIT) {
						tempItem = item;
						if(tempItem.popKind == curGameType){ //特定优先显示
							showed |= showDialog(tempItem);
							tempItem = null;
							Log.i(TAG, "pop a kind exit");
							break;
						}else if(tempItem.popKind != 0){ //不符合要求
							tempItem = null;
						}
						
						
					}
				}
			}
			
			if(tempItem != null){  //没有特定显示且有通用需要显示
				if(tempItem.popType == POP_LOGIN){
					ActionInfoProvider.getInstance().setNoticeItem(tempItem);
					Log.i(TAG, "pop a common login");
				}else{
					showed |= showDialog(tempItem);
					Log.i(TAG, "pop a common exit");
				}
			}
		}
		return showed;
	}

	/**
	 * 显示登录弹框
	 */
	public void showLoginDialog() {
		showImmediateDialog(POP_LOGIN);
	}

	/**
	 * 显示退出弹框
	 * @return 是否显示退出弹框，若服务器没有下发退出弹框，则不显示，返回false
	 */
	public boolean showExitDialog() {
		return showImmediateDialog(POP_EXIT);
		//return showExitPop;
	}
	
	/**
	 * 显示弹框
	 * @param item
	 * @return 是否显示弹框，如在游戏中或正显示退出弹框，则不显示此次弹框，返回false,否则返回true
	 */
	private boolean showDialog(final PopMsgItem item) {
		int curFrag = FiexedViewHelper.getInstance().getCurFragment();
		//不在分区主界面
		if(curFrag != FiexedViewHelper.CARDZONE_VIEW){
			return false;
		}
		//不是对应玩法
		int playType = FiexedViewHelper.getInstance().getGameType();
		if(playType != item.popKind && item.popKind != 0){
			return false;
		}
		ActionInfoProvider.getInstance().setNoticeItem(item);
		return FiexedViewHelper.getInstance().cardZoneFragment.showNotice();
		/*initBtn(item.buttonType);
		SysPopDialog dialog = new SysPopDialog(context, leftBtn, null,
				item.msg, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (which == SysPopDialog.CONFIRM) {
							toWhere(context, item);
							showExitPop = false;
						} else if (item.popType == POP_EXIT) {
							// 若是退出弹框，则取消和关闭都会退出游戏
							FiexedViewHelper.getInstance().exitGame();
						}
					}
				});
		dialog.setOnDismissListener(this);
		
		//新的弹框取消旧的弹框
		if (curDialog != null) {
			curDialog.dismiss();
		}
		
		dialog.show();
		curDialog = dialog;*/
	}


	/**
	 * 推迟弹框
	 * @param id 弹框id
	 * @param round 弹框轮次
	 * @param delay 延迟时间，单位毫秒
	 */
	private void delayDialog(int id, int round, long delay){
		Intent intent = new Intent();
		intent.setAction(mAction);
		intent.putExtra("itemID", id);
		intent.putExtra("round",round);
		PendingIntent pi = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		mAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + delay, pi);
	}
	
	/**
	 * 显示弹一次的对话框
	 */
	public void showOneTimeDialog() {
		showDelayDialog(POP_TIME);
	}
	
	/**
	 * 显示一段时间多次弹的对话框
	 */
	public void showIntervalTimeDialog() {
		showDelayDialog(POP_INTERVAL);
	}
	
	/**
	 * 显示延迟的对话框
	 * @param type 类型， POP_TIME, POP_INTERVAL
	 */
	private void showDelayDialog(short type) {
		synchronized (popMsgList) {
			for (int i = 0; i < popMsgList.size(); i++) {
				PopMsgItem item = popMsgList.get(i);
				if (item.popType == type) {
					if (inTime(item.startTime, item.endTime)) {
						// 处于时间段内，立马显示，推迟一个周期，以免开始就把登录弹框取消
						delayDialog(item.id, item.mRound,
								item.popInterval * 60 * 1000);
					} else {
						// 当前不在时间段，等待下一次时间段
						delayDialog(item.id, item.mRound,
								getStartTime(item.startTime));
					}
				}
			}
		}
	}
	
	/**
	 * 获得起始时间距离现在的毫秒数
	 * @param time 起始时间距离00:00的分钟数，如10:00则time=600
	 * @return 起始时间距离现在的毫秒数
	 */
	private long getStartTime(short time){
		short waitTime = getWaitTime(time);
		if(waitTime < 0){
			//今天已经过了，则计算距离明天起始时间
			return (24 * 60 + waitTime) * 60L * 1000;
		}else{
			return waitTime * 60L * 1000;
		}
	}
	
	/**
	 * 获得结束时间距离现在的毫秒数
	 * @param time 结束时间距离00:00的分钟数，如10:00则time=600
	 * @return 结束时间距离现在的毫秒数，可能是负数，则表示已经过了结束时间
	 */
	private long getStopTime(short time){
		return getWaitTime(time) * 60L * 1000;
	}
	
	/**
	 * 获得等待分钟数
	 * @param time 目标时间距离00:00的分钟数，如10:00则time=600
	 * @return 目标时间距离现在的分钟数，可能是负数，表示已经过了目标时间
	 */
	private short getWaitTime(short time){
		String tS = getCurrentTime();
		String t[] = tS.split(":");
		short curTime = (short)(Integer.parseInt(t[0]) * 60 + Integer.parseInt(t[1]));
		return (short)(time - curTime);
	}
	
	
	/**
	 * 当前是否在时间段
	 * @param startTime 起始时间
	 * @param endTime 结束时间
	 * @return 在时间段返回true，否则返回false
	 */
	private boolean inTime(short startTime, short endTime){
		String tS = getCurrentTime();
		String t[] = tS.split(":");
		short curTime = (short)(Integer.parseInt(t[0]) * 60 + Integer.parseInt(t[1]));
		if(curTime >= startTime && curTime <= endTime){
			return true;
		}
		return false;
	}

	
	private BroadcastReceiver mPopMsgReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			int round = intent.getIntExtra("round", -1);
			//只处理当前轮次的消息
			if(action.equals(mAction) && round == curRound){
				PopMsgItem item = getItemById(intent.getIntExtra("itemID", -1));
				if(item != null){
					boolean isShow = showDialog(item);
					if(item.popType == POP_TIME){
						Log.i(TAG,"pop a pop time");
						if(isShow){
							synchronized (popMsgList) {
								popMsgList.remove(item);
							}
						}else{
							delayDialog(item.id, item.mRound,item.popInterval * 60 * 1000);
						}
					}else if(item.popType == POP_INTERVAL){
						Log.i(TAG,"pop a interval");
						if(getStopTime(item.endTime) < item.popInterval * 60 * 1000 && isShow){
							synchronized (popMsgList) {
								popMsgList.remove(item);
							}
						}else{
							delayDialog(item.id, item.mRound, item.popInterval * 60 * 1000);
						}
					}
				}
			}
		}};
	
		
	/**
	 * 通过id获得item
	 * @param id
	 * @return
	 */
	PopMsgItem getItemById(int id) {
		synchronized (popMsgList) {
			if (!popMsgList.isEmpty()) {
				for (PopMsgItem item : popMsgList) {
					if (item.id == id) {
						return item;
					}
				}
			}
			return null;
		}

	}

	/**
	 * 获取当前时间
	 * 
	 * @return "HH:MM"格式时间
	 */
	public static String getCurrentTime() {
		Date data = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		return sdf.format(data);
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		// TODO Auto-generated method stub
		if (dialog == curDialog) {
			curDialog = null;
		}
	}
}
