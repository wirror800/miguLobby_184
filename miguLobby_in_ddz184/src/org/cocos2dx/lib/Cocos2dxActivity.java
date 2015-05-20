/****************************************************************************
Copyright (c) 2010-2011 cocos2d-x.org

http://www.cocos2d-x.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 ****************************************************************************/

package org.cocos2dx.lib;

import java.io.File;
import java.util.Calendar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.Window;
import android.view.WindowManager;

import com.mykj.andr.ui.MMVideoBuyDialog;
import com.mykj.game.FiexedViewHelper;
import com.MyGame.Midlet.R;
import com.MyGame.Midlet.wxapi.WXEntryActivity;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.HalfWebDialog;
import com.mykj.game.utils.Log;
import com.mykj.game.utils.Toast;
import com.mykj.game.utils.Util;
import com.mykj.game.utils.UtilHelper;


/*public class Cocos2dxActivity extends Activity {*/   //继承support.v4.app.FragmentActivity;以便可以使用Fragment
public class Cocos2dxActivity extends FragmentActivity {
	protected static Cocos2dxMusic backgroundMusicPlayer;
	protected static Cocos2dxSound soundPlayer;
	private static Cocos2dxAccelerometer accelerometer;
	private static boolean accelerometerEnabled = false;
	
	public static Handler handler;
	
	public final static int HANDLER_SHOW_DIALOG = 1;
	
	public final static int HANDLER_SHOW_BUY_DIALOG = 2;
	
	public final static int HANDLER_WEIXIN_SHARE = 3;
	
	public final static int HANDLER_HIDE_BG = 4;
	
	public final static int HANDLER_SAVE_PICTURE = 5;
	
	public final static int HANDLER_GAME_BUY = 6;
	
	private static String packageName;


	/********************/

	private static native void nativeSetPaths(String apkPath);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		 
		 
		final Window win = getWindow();
		win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        
		
		// get frame size
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		accelerometer = new Cocos2dxAccelerometer(this);

		// init media player and sound player
		backgroundMusicPlayer = new Cocos2dxMusic(this);
		soundPlayer = new Cocos2dxSound(this);
		// init bitmap context
		Cocos2dxBitmap.setContext(this);
		
		initMp3Path();
		
		handler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case HANDLER_SHOW_DIALOG:
					showDialog(((DialogMessage) msg.obj).title,
							((DialogMessage) msg.obj).message);
					break;
				case HANDLER_SHOW_BUY_DIALOG:
					UtilHelper.showBuyDialog(Cocos2dxActivity.this,AppConfig.propId,
							true,AppConfig.isConfirmon,AppConfig.ACTION_RO0M);  //3代表  房间【＋】
					break;
				case HANDLER_WEIXIN_SHARE:
					/*Bundle bundle=msg.getData();
					String title=bundle.getString("wx_title");
					String description=bundle.getString("wx_description");
					int resId=bundle.getInt("wx_thumb");
					
					Intent intent = new Intent(Cocos2dxActivity.this, WXEntryActivity.class);
					intent.putExtra("wx_title", title);
					intent.putExtra("wx_description", description);
					intent.putExtra("wx_thumb", resId);
					startActivity(intent);*/
					Toast.makeText(Cocos2dxActivity.this, 
							"对不起分享功能暂时无法使用", Toast.LENGTH_SHORT).show();
					break;
				case HANDLER_HIDE_BG:
					FiexedViewHelper.getInstance().removeCococs2dLoading();
					break;
				case HANDLER_SAVE_PICTURE:
					Bundle b=msg.getData();
					savePicture(b);
					break;
				case HANDLER_GAME_BUY:
					int propId=msg.arg1;
					if(propId==0){
						if(MMVideoBuyDialog.isDataReady()){
							MMVideoBuyDialog dialog=new MMVideoBuyDialog(Cocos2dxActivity.this);
							dialog.show();
						}
					}else{
						UtilHelper.showBuyDialog(Cocos2dxActivity.this,propId,true,
								AppConfig.isConfirmon,AppConfig.ACTION_RO0M);// 3代表  房间【＋】 
					}
					break;
				
				default:
					break;
				}
			}
		};
	}

	
	
	private  void savePicture(Bundle bundle){
		String userName=bundle.getString("userName");
		String matchName=bundle.getString("matchName");
		int rank=bundle.getInt("rank");
		String date=bundle.getString("date");
		String[] awards=bundle.getStringArray("awards");
		int awardCount=bundle.getInt("awardCount");

		
		try {
			if (!Util.isMediaMounted()) {
				Toast.makeText(this, this.getResources().getString(R.string.ddz_sdcard_error), Toast.LENGTH_SHORT).show();
				return;
			}
			String rankStr=this.getResources().getString(R.string.match_di)+rank+this.getResources().getString(R.string.match_ming);
			
			Bitmap bmp = Util.getImageFromAssetsFile("common/comm_commendation.png"); // 加载大图
			if (null == bmp) // 加载图片失败
				return;

			Canvas canvas = new Canvas(bmp);
			Paint paint = new Paint();
			paint.setColor(Color.BLACK);
			paint.setTextSize(16);
			canvas.drawText(userName+":", 86, 105, paint); // 绘制用户名

			paint.setTextSize(16);
			paint.setColor(Color.RED);
			canvas.drawText(matchName, 206, 150, paint); // 绘制比赛场
			
			paint.setTextSize(16);
			paint.setColor(Color.RED);
			canvas.drawText(rankStr, 127, 180, paint); // 绘制比赛获得名次

			paint.setTextSize(16);
			paint.setColor(Color.LTGRAY);
			canvas.drawText(date, 438, 270, paint); // 绘制日期

			paint.setTextSize(16); // 绘制奖品
			paint.setColor(Color.RED);
			for (int i = 0; i < awardCount; i++) {
				canvas.drawText(awards[i], 134, 249 + i * 17, paint);
			}
			// 绘制完毕，获得日期作为文件名保存
			Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DATE);
			int hour = c.get(Calendar.HOUR);
			int minute = c.get(Calendar.MINUTE);
			int second = c.get(Calendar.SECOND);

			StringBuilder sb=new StringBuilder();
			
			sb.append(year).append(month).append(day);
			sb.append(hour).append(minute).append(second);

			// 获取日期完毕，保存图片
			Util.saveBitmap(this,sb.toString(), bmp);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	public static String getCurrentLanguage() {
		String languageName = java.util.Locale.getDefault().getLanguage();
		return languageName;
	}

	public static void showMessageBox(String title, String message) {
		Message msg = new Message();
		msg.what = HANDLER_SHOW_DIALOG;
		msg.obj = new DialogMessage(title, message);

		handler.sendMessage(msg);
	}

	public static void enableAccelerometer() {
		accelerometerEnabled = true;
		accelerometer.enable();
	}

	public static void disableAccelerometer() {
		accelerometerEnabled = false;
		accelerometer.disable();
	}

	public static void preloadBackgroundMusic(String path){
		if(Util.isOMS()){
			return;
		}
		backgroundMusicPlayer.preloadBackgroundMusic(path);
	}

	public static void playBackgroundMusic(String path, boolean isLoop){
		if(Util.isOMS()){
			return;
		}
		backgroundMusicPlayer.playBackgroundMusic(path, isLoop);
	}

	public static void stopBackgroundMusic(){
		if(Util.isOMS()){
			return;
		}
		backgroundMusicPlayer.stopBackgroundMusic();
	}

	public static void pauseBackgroundMusic(){
		if(Util.isOMS()){
			return;
		}
		backgroundMusicPlayer.pauseBackgroundMusic();
	}

	public static void resumeBackgroundMusic(){
		if(Util.isOMS()){
			return;
		}
		backgroundMusicPlayer.resumeBackgroundMusic();
	}

	public static void rewindBackgroundMusic(){
		if(Util.isOMS()){
			return;
		}
		backgroundMusicPlayer.rewindBackgroundMusic();
	}

	public static boolean isBackgroundMusicPlaying(){
		if(Util.isOMS()){
			return false;
		}
		return backgroundMusicPlayer.isBackgroundMusicPlaying();
	}

	public static float getBackgroundMusicVolume(){
		if(Util.isOMS()){
			return 0;
		}
		return backgroundMusicPlayer.getBackgroundVolume();
	}

	public static void setBackgroundMusicVolume(float volume){
		if(Util.isOMS()){
			return;
		}
		backgroundMusicPlayer.setBackgroundVolume(volume);
	}

	public static int playEffect(String path, boolean isLoop){
		if(Util.isOMS()){
			return -1;
		}
		return soundPlayer.playEffect(path, isLoop);
	}

	public static void stopEffect(int soundId){
		if(Util.isOMS()){
			return;
		}
		soundPlayer.stopEffect(soundId);
	}

	public static void pauseEffect(int soundId){
		if(Util.isOMS()){
			return;
		}
		soundPlayer.pauseEffect(soundId);
	}

	public static void resumeEffect(int soundId){
		if(Util.isOMS()){
			return;
		}
		soundPlayer.resumeEffect(soundId);
	}

	public static float getEffectsVolume(){
		if(Util.isOMS()){
			return 0;
		}
		return soundPlayer.getEffectsVolume();
	}

	public static void setEffectsVolume(float volume){
		if(Util.isOMS()){
			return;
		}
		soundPlayer.setEffectsVolume(volume);
	}

	public static void preloadEffect(String path){
		if(Util.isOMS()){
			return;
		}
		soundPlayer.preloadEffect(path);
	}

	public static void unloadEffect(String path){
		if(Util.isOMS()){
			return;
		}
		soundPlayer.unloadEffect(path);
	}

	public static void stopAllEffects(){
		if(Util.isOMS()){
			return;
		}
		soundPlayer.stopAllEffects();
	}

	public static void pauseAllEffects(){
		if(Util.isOMS()){
			return;
		}
		soundPlayer.pauseAllEffects();
	}

	public static void resumeAllEffects(){
		if(Util.isOMS()){
			return;
		}
		soundPlayer.resumeAllEffects();
	}

	public static void end(){
		backgroundMusicPlayer.end();
		soundPlayer.end();
	}
    
    public static String getCocos2dxPackageName(){
    	return packageName;
    }
    
    public static void terminateProcess(){
    	android.os.Process.killProcess(android.os.Process.myPid());
    }

	public void onWeb(String url){
	}
	
    @Override
    protected void onResume() {
    	super.onResume();
    	if (accelerometerEnabled) {
    	    accelerometer.enable();
    	}
    }

    @Override
    protected void onPause() {
    	super.onPause();
    	if (accelerometerEnabled) {
    	    accelerometer.disable();
    	}
    }

    /**
    //2012-12-28 新增
    @Override
    protected void onStop() {
    	super.onStop();
    	if (accelerometerEnabled) {
    	    accelerometer.disable();
    	}
    }
    ****/
    
    protected void setPackageName(String packageName) {
    	Cocos2dxActivity.packageName = packageName;
    	
    	String apkFilePath = "";
        ApplicationInfo appInfo = null;
        PackageManager packMgmr = getApplication().getPackageManager();
        try {
            appInfo = packMgmr.getApplicationInfo(packageName, 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Unable to locate assets, aborting...");
        }
        apkFilePath = appInfo.sourceDir;
        Log.w("apk path", apkFilePath);

        // add this link at the renderer class
        nativeSetPaths(apkFilePath);
    }
    
    private void showDialog(String title, String message){
    	Dialog dialog = new AlertDialog.Builder(this)
	    .setTitle(title)
	    .setMessage(message)
	    .setPositiveButton("Ok",
	    new DialogInterface.OnClickListener()
	    {
	    	public void onClick(DialogInterface dialog, int whichButton){
	    		
	    	}
	    }).create();

	    dialog.show();
    }
    
    /**音效资源文件放置SD卡的目录*/
    private static String mp3SDbasePath = "";
    
    /**初始化SD卡中放置资源的目录*/
    private static void initMp3Path(){
    	boolean isSDCard = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    	if(isSDCard){
    		String basepath = Environment.getExternalStorageDirectory() + "/mykjmp3";
    		File file = new File(basepath);
    		if(file.exists()){
    			mp3SDbasePath = basepath;
    		}else{
    			mp3SDbasePath = "";
    		}
    	}else{
    		mp3SDbasePath = "";
    	}
    }
    
    public static String getPathFormSDCard(String path){
    	return mp3SDbasePath+"/"+path;
    }
    
    public static boolean isLoadFromSDCard(String path){
    	if(mp3SDbasePath==null || mp3SDbasePath.length()<=0){
    		return false;
    	}
    	File file = new File(mp3SDbasePath+"/"+path);
    	if(file.isFile()&&file.exists()){
    		return true;
    	}
    	return false;
    }
    
}

class DialogMessage {
	public String title;
	public String message;
	
	public DialogMessage(String title, String message){
		this.message = message;
		this.title = title;
	}
}
