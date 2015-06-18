package com.MyGame.Midlet.service;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

import com.MyGame.Migu.R;
import com.MyGame.Midlet.util.Configs;

public class GameInfoItem {
	private static final String TAG="GameInfoItem";
	private String    fileName = null;
	private String    urlPath = null;
	private Context   mContext;
	private Drawable  iconDrawable	= null;

	public GameInfoItem(Context context,String url)
	{
		this.mContext=context;
		this.urlPath = url;
		this.fileName = Configs.getFileNameFromUrl(url);

		if(Configs.isMediaMounted())
		{
			try
			{
				String path=((MykjService)mContext).getGameInfoPath();
				String folder=((MykjService)mContext).getFolder();
				final File iconFile = new File(path,fileName);
				final File iconFiletmp = new File(path,fileName+".tmp");
				File parent=iconFile.getParentFile();
				if(!parent.exists()){
					parent.mkdirs();
				}

				File[] list=parent.getParentFile().listFiles();
				for(File entry : list) {
					if(!entry.getName().equals(parent.getName())&&entry.getName().startsWith(folder)){
						Configs.deleteDir(entry);
					}
				} 
				if(iconFile.exists() && iconFile.isFile())
				{
					iconDrawable = Configs.getDrawableFromFile(mContext,iconFile,DisplayMetrics.DENSITY_HIGH);

				}else{
					//downloadImgBitmap(iconFile);
					new Thread(){
						@Override
						public void run(){
							if(Configs.downloadImgBitmap(urlPath,iconFiletmp)){
								iconFiletmp.renameTo(iconFile);
								iconDrawable=Configs.getDrawableFromFile(mContext,iconFile,DisplayMetrics.DENSITY_HIGH);
							}
						}
					}.start();
				}

			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}


	public Drawable getGameInfoIcon(){
		if(iconDrawable==null){
			Bitmap iconBitmap=BitmapFactory.decodeResource(mContext.getResources(), R.drawable.default_gameinfo);
			iconDrawable = new BitmapDrawable(mContext.getResources(),iconBitmap);
			return iconDrawable;
		}
		return iconDrawable;
	}




}
