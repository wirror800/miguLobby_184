package com.mykj.game.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.MyGame.Migu.R;
 

/**
 * 
 * @ClassName: Toast
 * @Description: 重新自己的Toast定制系统类似Toast，又不大量修改代码
 * @author  
 * @date 2013-6-18 下午05:15:06
 *
 */
public class Toast{
	
	public static int LENGTH_LONG=1;
	
	public static int LENGTH_SHORT=0;
	
	public static android.widget.Toast makeText(Context context, CharSequence text, int duration){
		android.widget.Toast result = new android.widget.Toast(context);
		
	    result.setGravity(Gravity.BOTTOM, 0, 20);
	    
	    LayoutInflater inflate = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflate.inflate(R.layout.toast_show, null);
        TextView tv = (TextView)v.findViewById(R.id.message);
        tv.setText(text); 
		result.setView(v);
		  
		return result;
	}
}
