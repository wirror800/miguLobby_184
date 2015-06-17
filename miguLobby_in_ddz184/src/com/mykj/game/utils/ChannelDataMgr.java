package com.mykj.game.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;

import com.mingyou.accountInfo.AccountItem;
import com.mingyou.accountInfo.LoginInfoManager;

/**
 * 封装所有渠道相关的处理：获取，计算，存储等
 * @author FWQ 20130524
 *
 */
public class ChannelDataMgr {

	private static String TAG="ChannelDataMgr";
	
	private static ChannelDataMgr instance = null;
	private ChannelDataMgr(){}
	
	
	public static ChannelDataMgr getInstance(){
		if(instance == null){
			instance = new ChannelDataMgr();
		}
		return instance;
	}

	
	/**
	 * 初始化所有渠道相关的数据：主渠道、子渠道、client、cid等
	 * <br>对外暴露的初始化唯一接口
	 */
	public void initAllData(Activity act){
		if(act == null){
			throw new NullPointerException("ChannelDataMgr initAllData Activity is NULL");
		}
		
		try {
			initChannels(act);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(AppConfig.isReadChannelFromSD){
			//部署到交叉推广站点的专用包，需要从SD卡读取渠道数据
			readChanneFromSDCard(act);
		}
		
		// 根据channelId 初始化clientID，CID
		AppConfig.initClientCID();
	}
	
	/**
	 * 初始化渠道相关信息  add by FWQ 20130219
	 *             update by FWQ 20130308(增加控制不使用SD卡存储渠道)
	 * 步骤：
	 * 1：先读取Bundle传入的渠道号记录到channelId中
	 * 2：如果没有传入渠道号(即channelId为空)，就从程序的渠道配置文件channel.properties中读取渠道记录到channelId
	 * 3：检查外部记录文件(SD卡或程序目录)是否有渠道记录，如果有则直接使用外部记录的渠道号，以上步骤得到的channelId不使用；
	 *      如果外部没有记录渠道号，则把通过以上步骤获取到的渠道号channelId记录到外部记录文件，并正式使用channelId作为渠道号。
	 */
	private void initChannels(Activity act){
		String channelId = null; //临时记录主渠道
		String childChannelId = null;//临时记录子渠道
		String token=null; //临时移动token
		String channelFiledName ="channel.cfg"; //记录渠道数据的文件名

		File channelFile = new File(act.getFilesDir(),channelFiledName);
		
		// 获取大厅传递的数据,获取Bundle的信息
		Bundle bundle = act.getIntent().getExtras();
		if(null != bundle){
			token = bundle.getString("TOKEN");
			channelId = bundle.getString("CHANNEL_ID");//读取传入的主渠道
			childChannelId = bundle.getString("CHILD_CHANNEL_ID");//读取传入的子渠道
			//记录登录方式
			if(!Util.isEmptyStr(channelId)){
				//记录为由大厅启动的游戏
				AppConfig.setLaunchType(AppConfig.HALL_TAG);
			}
			setCmccToken(token);
		}
		
	
		if(Util.isEmptyStr(AppConfig.channelId) && !Util.isEmptyStr(channelId) && !Util.isEmptyStr(childChannelId)){  //大厅传入
			AppConfig.channelId=channelId;
			AppConfig.childChannelId=childChannelId;
			//writeChannelToFile(channelFile, channelId, childChannelId);
		}else if(channelFile.exists()){                              //程序data/data
			String[] data_file = readChannelByFile(channelFile);
			if(data_file!=null && data_file.length==2){
				String channelId_File = data_file[0];
				String childChannelId_File = data_file[1];
				if(!Util.isEmptyStr(channelId_File)){
					AppConfig.channelId=channelId_File;
					AppConfig.childChannelId=childChannelId_File;
				}
			}
			
		}
		
		if(Util.isEmptyStr(AppConfig.channelId)||Util.isEmptyStr(AppConfig.childChannelId)){  //程序assert
			try {
				Properties pro = new Properties();
				InputStream is = act.getAssets().open("channel.properties");
				pro.load(is);
				AppConfig.channelId = pro.getProperty("channelId").trim();
				AppConfig.childChannelId = pro.getProperty("childChannelId").trim();
				is.close();
				writeChannelToFile(channelFile, AppConfig.channelId, AppConfig.childChannelId);
			} catch (Exception e) {
				Log.e(TAG, "channelId read error");
			}
		}
		
	}

	/**
	 * 记录渠道数据
	 * @param _file
	 * @param _channel
	 * @param _childChannel
	 */
	private void writeChannelToFile(File _file,String _channel,String _childChannel){
		if(_file == null || _channel == null || _channel.length() <=0){
			return;
		}
		try {
			if(_file.getParentFile()!=null&&!_file.getParentFile().exists()){
				//目录不存在则创建目录
				_file.getParentFile().mkdirs();
			}
			if(!_file.exists()){
				//文件不存在则创建文件
				_file.createNewFile();
			}
			Properties pro = new Properties();
			
			FileOutputStream fos = new FileOutputStream(_file);
			
			pro.setProperty("channelId", _channel);
			pro.setProperty("childChannelId", _childChannel);
			
			pro.store(fos, "Update channelId value");
		   
	        fos.close();// 关闭流   
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 读取渠道数据
	 * @param _file
	 * @return
	 */
	private String[] readChannelByFile(File _file){
		if(_file == null || !_file.exists()){
			return null;
		}
		String re[] = new String[2];
		try {
			Properties pro = new Properties();
			FileInputStream fis = new FileInputStream(_file);
			pro.load(fis);
			re[0] = pro.getProperty("channelId");
			re[1] = pro.getProperty("childChannelId");
			fis.close();
		}  catch (Exception e) {
			e.printStackTrace();
		}
		return re;
	}
	
	
	private  String cmccToken=null;
	
	public  String getCmccToken() {
		return cmccToken;
	}

	public  void setCmccToken(String token) {
		cmccToken = token;
	}

	
	
	
	
	//-----------------------以下处理仅供交叉推广功能使用---------------------------------------
	
	/**
	 * 更多游戏下在前 先把渠道号记录到SD卡中（仅供交叉推广使用）
	 * @author FWQ 20130314
	 */
	public void writeChannelToSDCard(){
		if(!Util.isMediaMounted()){
			return;
		}
		String channelFiledName ="channel.cfg"; //记录渠道数据的文件名
		FileOutputStream fos = null;
		try {
			File mfile_sd = new File(Environment.getExternalStorageDirectory()+ AppConfig.DOWNLOAD_FOLDER ,channelFiledName);
			if(mfile_sd.getParentFile()!=null&&!mfile_sd.getParentFile().exists()){
				//目录不存在则创建目录
				mfile_sd.getParentFile().mkdirs();
			}
			if(!mfile_sd.exists()){
				//文件不存在则创建文件
				mfile_sd.createNewFile();
			}

			fos = new FileOutputStream(mfile_sd);

			byte []channelB = AppConfig.channelId.getBytes();
			byte []childchannelB = AppConfig.childChannelId.getBytes();
			fos.write(channelB.length);
			fos.write(channelB);
			fos.write(childchannelB.length);
			fos.write(childchannelB);
			fos.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if(fos!=null){
				try {
					fos.close();
				} catch (IOException e) {
				}
				fos = null;
			}
		}	

	}

	
	/**
	 * 从SD卡读取渠道数据（仅供交叉推广站点专用包使用）
	 * @author FWQ 20130318
	 */
	private void readChanneFromSDCard(Activity act){
		if(!Util.isMediaMounted()){
			return;
		}
		String channelFiledName ="channel.cfg"; //记录渠道数据的文件名
		FileInputStream fis = null;
		try {
			File mfile_sd = new File(Environment.getExternalStorageDirectory() 
					+ AppConfig.DOWNLOAD_FOLDER,channelFiledName);
			if(!mfile_sd.exists()){
				return;
			}
			fis = new FileInputStream(mfile_sd);
			int len = fis.read();
			if(len > 0){
				byte []data = new byte[len];
				fis.read(data);
				String channel = new String(data);
				//修正渠道号,只取3位
				if(channel!=null){
					channel = channel.trim();
					if(channel.length()>3){
						channel = channel.substring(channel.length()-3);
					}
				}
				AppConfig.channelId = channel;
			}
			len = fis.read();
			if(len > 0){
				byte []data = new byte[len];
				fis.read(data);
				AppConfig.childChannelId = new String(data);
			}
			//记录到程序中
			File channelFile = new File(act.getFilesDir()+"/"+channelFiledName);
			writeChannelToFile(channelFile, AppConfig.channelId, AppConfig.childChannelId);

		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if(fis!=null){
				try {
					fis.close();
				} catch (IOException e) {
				}
				fis = null;
			}
		}	

	}
 

}
