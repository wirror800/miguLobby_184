package com.MyGame.Midlet.service;



import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.xmlpull.v1.XmlPullParser;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.format.Time;
import android.util.Log;
import android.widget.RemoteViews;

import com.MyGame.Midlet.R;
import com.MyGame.Midlet.util.AppConfig;
import com.MyGame.Midlet.util.Configs;




public class NotifiyMsg {
	private static final String TAG="NotifiyMsg";
	//生成HTTP请求URL

	private Context mContext;

	private String img;
	private Bitmap  iconBitmap=null;

	/**消息内容*/
	private String txt ;

	/**消息详情*/
	private String content ;
	/**
	 * 消息弹出后的动作
	 * */
	private int at;
	private String mUri;


	/**Notify消息弹出模式*/
	private String tm ;
	private String tf;
	private String te;
	/** 冷却时间, 单位小时, 即该消息弹出后, 冷却时间内不能再弹出.*/
	private int ct;
	public NotifiyMsg(Context context, XmlPullParser p) {

		mContext=context;
		this.img = p.getAttributeValue(null, "img");
		this.txt = p.getAttributeValue(null, "txt");
		this.content= p.getAttributeValue(null, "context");
		this.at =Integer.parseInt(p.getAttributeValue(null, "at"));
		this.mUri = p.getAttributeValue(null, "uri");		
		this.tm = p.getAttributeValue(null, "tm");
		this.tf = p.getAttributeValue(null, "tf");
		this.te = p.getAttributeValue(null, "te");
		this.ct = Integer.parseInt(p.getAttributeValue(null, "ct"));
		if(this.img!=null){
			downloadImgBitmap();			
		}
	}

	/**
	 * 获取Notification对象
	 * @return
	 */
	public Notification getMsgNotification(){
		@SuppressWarnings("deprecation")
		Notification notification = new Notification(R.drawable.app_launcher,
				mContext.getString(R.string.app_name),System.currentTimeMillis());

		Intent intent =getActionType(); //系统消息点击入口intent						

		//notifiy声音开关
		if(((MykjService)mContext).getNotifiySoundValue()){
			notification.defaults=Notification.DEFAULT_SOUND;
		}
		//notifiy震动开关
		if(((MykjService)mContext).getNotifiyVibrateValue()){
			notification.defaults|=Notification.DEFAULT_VIBRATE;
		}
		// 放置在"正在运行"栏目中  
		notification.flags = Notification.FLAG_ONGOING_EVENT;  

		RemoteViews contentView = new RemoteViews(mContext.getPackageName(), R.layout.mykj_notifiy_layout);  
		contentView.setImageViewBitmap(R.id.img_icon, getIconBitmap());
		contentView.setTextViewText(R.id.tvContent, txt);  
		// 指定个性化视图  
		notification.contentView = contentView; 

		// 指定内容意图  			
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, getID(),intent, PendingIntent.FLAG_UPDATE_CURRENT);  

		notification.contentIntent = contentIntent;  

		notification.flags = Notification.FLAG_AUTO_CANCEL;  
		return notification;
	}



	/**
	 * 获取消息ID
	 * @return
	 */
	public int getID(){ 
		return txt.hashCode();
	} 


	/**
	 * 保存消息发送时的时间，单位 分钟
	 * */
	public void setMsgNotifiySendTime(){
		long time=System.currentTimeMillis();
		long min=time/60000;//当前分钟数		
		String id=getID()+"";
		Configs.setLongSharedPreferences(mContext,id,min);

	}


	/**
	 * 当前发送是否在cooldown中
	 * true 不在CD中
	 * */
	public boolean isAllowNotifiySendTime(){
		long ctMins=0;//(long)ct*60;
		if(AppConfig.Cooldown_MIN){
			ctMins=ct;
		}else{
			ctMins=(long)ct*60;
		}
		
		long time=System.currentTimeMillis();
		long min=time/60000;//当前分钟数	
		long lastsendtime=getMsgNotifiySendTime();

		if((min-lastsendtime)>ctMins){
			Log.v(TAG, "此条消息不在冷却中....，txt="+txt);
			return true;
		}else{
			Log.v(TAG, "此条消息在冷却中....，txt="+txt);
			return false;
		}

	}



	/**
	 * 当前时间是否允许弹出
	 * */
	public boolean getNotifiyTime(){
		boolean res=false;
		Date date=new Date();
		SimpleDateFormat df=new SimpleDateFormat("HH:mm");			
		String time=df.format(date);

		int a=Integer.parseInt(tm);

		if(a==1){
			Log.v(TAG, "立即notifiy消息模式");
			res= true;

		}else if(a==2){	
			Log.v(TAG, "指定时间点notifiy消息模式,消息弹出时间="+tf);

			long conMins=getExactlyConnectMin();
			Log.v(TAG, "指定多少分钟后启动service,conMins="+conMins);
			if(conMins==0){
				res= true;
			}else{
				setWatchdog(conMins*60*1000);
				res= false;
			}

		}
		else if(a==3){
			Log.v(TAG, "指定时间区间notifiy消息模式，消息弹出时间区间("+tf+","+te+")");
			long conMins=getExactlyConnectMin()+getRandomConnectMin();
			Log.v(TAG, "指定多少分钟后启动service,conMins="+conMins);

			long mill=conMins*60*1000;
			if(tf.compareTo(te)<0){
				if((time.compareTo(tf)>=0)&&(time.compareTo(te)<=0)){
					res= true;
				}else{
					setWatchdog(mill);
					res= false;
				}
			}else{
				if((time.compareTo(tf)>=0)||(time.compareTo(te)<=0)){
					res= true;
				}else{
					setWatchdog(mill);
					res= false;
				}
			}

		}	
		return res;
	}


	/**
	 * 获取消息标题图片
	 * @return
	 */
	private Bitmap getIconBitmap(){
		if(iconBitmap==null){			
			iconBitmap=BitmapFactory.decodeResource(mContext.getResources(),R.drawable.app_launcher);
		}
		return iconBitmap;
	}



	/**
	 * 消息弹出后的动作
	 * */		
	private Intent getActionType(){
		Intent intent=new Intent();
		Bundle bundle=new Bundle();
		bundle.putInt("at", at);   //消息弹出后的动作
		bundle.putString("content", content);//消息内容详情
		bundle.putString("uri", mUri);//消息uri
		intent.putExtras(bundle);
		intent.setClass(mContext, PopUpActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		return intent;

	}





	/**
	 * 获取消息发送时的时间，单位 分钟
	 * */
	private long getMsgNotifiySendTime(){
		String id=getID()+"";
		return Configs.getLongSharedPreferences(mContext,id,0);
	}



	

	/**
	 * 定时创建服务
	 * */
	private void setWatchdog(long mill) {

		AlarmManager mAlarmManager =(AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent();
		intent.setClass(mContext, MykjReceiver.class);
		intent.setAction("mykj.intent.action.BOOT_BROADCAST");
		PendingIntent pi=PendingIntent.getBroadcast(mContext, getID(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
		long timeNow = SystemClock.elapsedRealtime();
		long nextCheckTime = timeNow + mill; //下次启动的时间		
		mAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, nextCheckTime, pi);
		//setReqBroadcastDate();
		Log.v(TAG, "注册广播发送PendingIntent");
	}





	/**
	 * 获取消息弹出的时间间隔
	 * @return
	 */
	private long getExactlyConnectMin(){
		String nearest=null;
		long conMins=0; //距离下次连接的分钟数
		
		int what=Integer.parseInt(tm);
		if(what==2){
			nearest=tf;
		}else if(what==3){
			if(tf.compareTo(te)<0){
				nearest=tf;
			}else{
				nearest=te;
			}
		}
		if(Configs.isEmptyStr(nearest)){
			return 0;
		}

		Time t = new Time();
		t.setToNow(); //取得系统时间
		int h = t.hour;
		int min=t.minute;

		String[] tmpStrArray = nearest.split(":");
		int tfHour=Integer.parseInt(tmpStrArray[0]);
		int tfMin =Integer.parseInt(tmpStrArray[1]);

	
		if(h<=tfHour){
			conMins=(tfHour-h)*60+(tfMin-min);    	
		}else{
			conMins=(tfHour-h+24)*60+(tfMin-min);
		}
		if(conMins<0){
			conMins=-conMins;
		}


		return conMins;
	}


	private long getRandomConnectMin(){
		int conMins=0; //距离下次连接的分钟数

		String[] tmpStrtf = tf.split(":");
		int	tfHour=Integer.parseInt(tmpStrtf[0]);
		int	tfMin =Integer.parseInt(tmpStrtf[1]);

		String[] tmpStrte = te.split(":");
		int teHour=Integer.parseInt(tmpStrte[0]);
		int teMin =Integer.parseInt(tmpStrte[1]);


		if(tfHour<=teHour){
			conMins=(teHour-tfHour)*60+(teMin-tfMin);    	
		}else{
			conMins=(teHour-tfHour+24)*60+(teMin-tfMin);
		}

		if(conMins<0){
			conMins=-conMins;
		}

		Random rand = new Random();
		int i = rand.nextInt(conMins); //int范围类的随机数
		Log.v(TAG, "指定多少分钟后加上偏移量启动service，偏移时间="+i);
		return (long)i;

	}


	/** 
	 * 下载notifiy icon Bitmap
	 */  

	private void downloadImgBitmap() {  
		new Thread(){
			@Override
			public void run(){
				try {  
					//new URL对象  把网址传入  
					URL url = new URL(img);  
					//取得链接  
					URLConnection conn = url.openConnection();  
					conn.connect();  
					//取得返回的InputStream  
					InputStream is = conn.getInputStream();  
					//将InputStream变为Bitmap  
					iconBitmap = BitmapFactory.decodeStream(is);  
					is.close();  

				} catch (Exception e) {  
					e.printStackTrace();  
				}  
			}
		}.start();

	}

}
