package com.mykj.game.utils;


import java.io.IOException;

import org.cocos2dx.lib.Cocos2dxMusic;
import org.cocos2dx.lib.Cocos2dxSound;

import android.content.Context;

public class PreloadCocos2dRes {

	private Context mContext;
	private static PreloadCocos2dRes instance=null;
	//private  Cocos2dxSound soundPlayer;
	//private  Cocos2dxMusic backgroundMusicPlayer;

	private PreloadCocos2dRes(Context context){
		mContext=context;
	}

	public static PreloadCocos2dRes getInstance(Context context){
		if(instance==null){
			instance=new PreloadCocos2dRes(context);
		}
		return instance;
	}

	
	
	/**
	 * 预加载声音
	 * @param path
	 */
	public void perloadSoundRes(Cocos2dxSound soundPlayer, String resDir){
		try {
			String[] paths=mContext.getAssets().list(resDir);
			for(String path:paths){
				String filePath=resDir+"/"+path;
				soundPlayer.preloadEffect(filePath);

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	
	/**
	 * 预加载背景音乐
	 * @param path
	 */
	public void perloadMusicRes(Cocos2dxMusic backgroundMusicPlayer,String path){
				backgroundMusicPlayer.preloadBackgroundMusic(path);
	}

	
	
	
	
}
