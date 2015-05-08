package com.mykj.andr.ui.widget;


import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.TextView;

import com.mykj.game.utils.UtilHelper;

public class CustomDigitalClock extends TextView {
	
	private Thread mTime;
    private boolean isRun=true;
    
    private static final int Handler_clock=21;
	
	public CustomDigitalClock(Context context) {
		super(context);
	}

	public CustomDigitalClock(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	private void initClock() {
		if(mTime == null){
			mTime=new Thread(new Runnable() {
				public void run() {
					while(isRun){
						handler.sendEmptyMessage(Handler_clock);
						 try {
								Thread.sleep(60*1000);
							} catch (Exception e) {
								//e.printStackTrace();
							}
					}
				}
			});
			mTime.start(); 
		}
	}
	
	@Override
	protected void onAttachedToWindow() {
		isRun = true;
		initClock();
		super.onAttachedToWindow();
	}
	

	Handler handler = new Handler(){
		final boolean hour24Mode = true; //固定用24小时格式
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Handler_clock:
				setText(UtilHelper.getSystemTime(hour24Mode));
				break;
				default:break;
			  }
			}
	};
	
	
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		isRun = false;
		try {
			if(mTime!=null){
				synchronized (mTime) {
					mTime.notify();
				}
				mTime = null;
			}
		} catch (Exception e) {
			
		}

		 
	}

}
