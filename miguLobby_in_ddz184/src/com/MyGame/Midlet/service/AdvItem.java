package com.MyGame.Midlet.service;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.MyGame.Midlet.R;
import com.MyGame.Midlet.util.Configs;



public class AdvItem
{
	private static final String TAG="AdvItem";
	private int sTime = 2000;
	private String urlPath = null;
	private String onClick = null;
	private String fileName = null;
	private ImageView imageView=null;
	
	private Context mContext;
	/** 图标的实例 */
	private Drawable	iconDrawable	= null;

	public AdvItem(Context context,int sTime, String url, String onClick)
	{
		this.mContext=context;
		this.sTime = sTime;
		this.urlPath = url;
		this.onClick = onClick;
		this.fileName = Configs.getFileNameFromUrl(url);

		if(Configs.isMediaMounted())
		{
			try
			{
				String path=((MykjService)mContext).getAdvPath();			
				final File iconFile = new File(path,fileName);
				final File iconFiletmp = new File(path,fileName+".tmp");
				File parent=iconFile.getParentFile();
				if(!parent.exists()){
					parent.mkdirs();
				}

				File[] list=parent.getParentFile().listFiles();
				for(File entry : list) {
					if(!entry.getName().equals(parent.getName())&&entry.getName().startsWith("ads_Ver")){
						Configs.deleteDir(entry);
					}
				} 
				if(iconFile.exists() && iconFile.isFile())
				{
					iconDrawable = Configs.getDrawableFromFile(mContext,iconFile,DisplayMetrics.DENSITY_HIGH);

				}else{
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

			}
		}
	}



	public ImageView getAdvIcon(){
		imageView= new ImageView(mContext);
		imageView.setScaleType(ScaleType.FIT_XY);
		
		if(iconDrawable==null){
			Bitmap iconBitmap=BitmapFactory.decodeResource(mContext.getResources(), R.drawable.adv_default);
			iconDrawable = new BitmapDrawable(mContext.getResources(),iconBitmap);
			
		}
		imageView.setImageDrawable(iconDrawable);
		
		return imageView;
	}


	public String getOnClick(){
		return onClick;
	}


}
