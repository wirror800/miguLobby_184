package com.mykj.andr.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ProgressBar;

public class CustomProgressBar extends ProgressBar{
	
	String	text;
	Paint	mPaint;
	
	public CustomProgressBar(Context context){
		super(context);
		initText();
	}
	
	public CustomProgressBar(Context context,AttributeSet attrs,int defStyle){
		super(context, attrs, defStyle);
		initText();
	}
	
	public CustomProgressBar(Context context,AttributeSet attrs){
		super(context, attrs);
		initText();
	}
	
	@Override
	public synchronized void setProgress(int progress){
		setText(progress);
		super.setProgress(progress);
	}
	
	@Override
	protected synchronized void onDraw(Canvas canvas){
		super.onDraw(canvas);
		Rect rect = new Rect();
		this.mPaint.getTextBounds(this.text, 0, this.text.length(), rect);
		float x = (getWidth() / 2) - rect.centerX();
		//int y = getHeight() - rect.centerY()/2;
		float y = (getHeight() / 2)+6.0f;
		canvas.drawText(this.text, x, y, this.mPaint); 
	}
	
	// 初始化，画笔
	private void initText(){
		this.mPaint = new Paint();
		this.mPaint.setColor(Color.BLACK);
	}
	
	private void setText(){
		setText(this.getProgress());
	}
	
	// 设置文字内容
	private void setText(int progress){
		//int i = (progress * 100) / this.getMax();
		//this.text = String.valueOf(i) + "%";
		this.text=String.valueOf(progress)+"/"+this.getMax();
	}
	
}
