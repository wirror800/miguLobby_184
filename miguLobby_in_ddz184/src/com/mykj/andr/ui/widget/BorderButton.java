package com.mykj.andr.ui.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.Button;

import com.MyGame.Midlet.R;

public class BorderButton extends Button {
	protected Paint paint;
	protected int color;
	protected int borderColor;
	protected float borderWidth;
	public BorderButton(Context context) {
		super(context);
		afterInit(context, null);
	}

	public BorderButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		afterInit(context, attrs);
	}

	public BorderButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub

		afterInit(context, attrs);
	}

	/**
	 * 
	 * 初始化自己的变量
	 * 
	 * @param context
	 * @param attrs
	 */
	private void afterInit(Context context, AttributeSet attrs) {
		if (attrs != null) {
			TypedArray a = context.obtainStyledAttributes(attrs,
					R.styleable.BorderButton);
			borderColor = a.getColor(R.styleable.BorderButton_borderColor, 0);
			//borderWidth = a.getDimension(R.styleable.BorderButton_borderWidth, 0);
			borderWidth = a.getFloat(R.styleable.BorderButton_borderWidth, 0);
			a.recycle();
		} else {
			borderColor = 0;
			borderWidth = 0;
		}

		ColorStateList clors = getTextColors();
		color = clors.getColorForState(getDrawableState(), 0);
		paint = new Paint(); // 设置一个画笔
		paint.setAntiAlias(true);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeCap(Paint.Cap.ROUND);
		
	}

	/**因为获得颜色的函数参数比较纠结，所以在设置时获得*/
	@Override
	public void setTextColor(int color) {
		this.color = color;
		super.setTextColor(color);
	}

	@Override
	public void setTextColor(ColorStateList colors) {
		super.setTextColor(colors);
		color = colors.getColorForState(getDrawableState(), 0);
	}

	/**
	 * 设置边颜色
	 * @param color
	 */
	public void setBorderColor(int color) {
		borderColor = color;
	}

	public void setBorderWidth(float width){
		borderWidth = width;
	}
	
	/**
	 * 获得左边padding，参考TextView的getExtendedPaddingTop
	 * @return
	 */
	public int getExtendedPaddingLeft() {

		int left = getCompoundPaddingLeft();
		int right = getCompoundPaddingRight();
		int viewwh = getWidth() - left - right;
		int layoutwh = (int) getLayout().getLineWidth(0); //注意这里就只支持一行了

		if (layoutwh >= viewwh) {
			return left;
		}

		final int gravity = getGravity() & Gravity.HORIZONTAL_GRAVITY_MASK;
		if (gravity == Gravity.LEFT) {
			return left;
		} else if (gravity == Gravity.RIGHT) {
			return left + viewwh - layoutwh;
		} else { // (gravity == Gravity.CENTER_VERTICAL)
			return left + (viewwh - layoutwh) / 2;
		}
	}

	/**
	 * 获得左边padding，参考TextView的getExtendedPaddingBottom
	 * @return
	 */
	public int getExtendedPaddingRight() {

		int left = getCompoundPaddingLeft();
		int right = getCompoundPaddingRight();
		int viewwh = getWidth() - left - right;
		int layoutwh = (int) getLayout().getLineWidth(0); //注意这里就只支持一行了

		if (layoutwh >= viewwh) {
			return right;
		}

		final int gravity = getGravity() & Gravity.HORIZONTAL_GRAVITY_MASK;
		if (gravity == Gravity.LEFT) {
			return right + viewwh - layoutwh;
		} else if (gravity == Gravity.RIGHT) {
			return right;
		} else { // (gravity == Gravity.CENTER_VERTICAL)
			return right + (viewwh - layoutwh) / 2;
		}
	}

	/**
	 * 获得水平偏移，参考TextView的getVerticalOffset
	 * @param forceNormal
	 * @return
	 */
	private int getHorizentalOffset(boolean forceNormal) {
		int hoffset = 0;
		int mGravity = getGravity();
		final int gravity = mGravity & Gravity.HORIZONTAL_GRAVITY_MASK;
		Layout l = getLayout();
		if (gravity != Gravity.LEFT) {
			int boxwh = getMeasuredWidth() - getExtendedPaddingLeft()
					- getExtendedPaddingRight();
			int textwh = l.getWidth();
			if (textwh < boxwh) {
				if (gravity == Gravity.RIGHT)
					hoffset = boxwh - textwh;
				else
					// (gravity == Gravity.CENTER_VERTICAL)
					hoffset = (boxwh - textwh) >> 1;
			}
		}
		return hoffset;
	}

	/**
	 * 
	 * 这是textView的私有函数，所以copy出来了
	 * @param forceNormal
	 * @return
	 */
	private int getVerticalOffset(boolean forceNormal) {
		int voffset = 0;
		int mGravity = getGravity();
		final int gravity = mGravity & Gravity.VERTICAL_GRAVITY_MASK;

		Layout l = getLayout();

		if (gravity != Gravity.TOP) {
			int boxht;
			boxht = getMeasuredHeight() - getExtendedPaddingTop()
					- getExtendedPaddingBottom();
			int textht = l.getHeight();

			if (textht < boxht) {
				if (gravity == Gravity.BOTTOM)
					voffset = boxht - textht;
				else
					// (gravity == Gravity.CENTER_VERTICAL)
					voffset = (boxht - textht) >> 1;
			}
		}
		return voffset;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		//一些TextView的变量，奇怪protected的但是不能直接用，就获得过来了
		int mScrollX = getScrollX();
		int mScrollY = getScrollY();
		int mRight = getRight();
		int mLeft = getLeft();
		int mBottom = getBottom();
		int mTop = getTop();
		
		canvas.translate(0, 0); // 将位置移动画纸的坐标点:0,0
		// Draw the background for this view
		// super.onDraw(canvas);

		final int compoundPaddingLeft = getCompoundPaddingLeft();
		final int compoundPaddingRight = getCompoundPaddingRight();
		final int scrollX = mScrollX;
		final int scrollY = mScrollY;
		final int right = mRight;
		final int left = mLeft;
		final int bottom = mBottom;
		final int top = mTop;

		int extendedPaddingTop = getExtendedPaddingTop();
		int extendedPaddingBottom = getExtendedPaddingBottom();

		float clipLeft = compoundPaddingLeft + scrollX;
		float clipTop = extendedPaddingTop + scrollY;
		float clipRight = right - left - compoundPaddingRight + scrollX;
		float clipBottom = bottom - top - extendedPaddingBottom + scrollY;

		canvas.clipRect(clipLeft - borderWidth, clipTop - borderWidth, clipRight + borderWidth,
				clipBottom + borderWidth);

		int voffsetText = 0;
		int hoffsetText = 0;
		int mGravity = getGravity();
		// translate in by our padding

		/* shortcircuit calling getVerticaOffset() */
		if ((mGravity & Gravity.VERTICAL_GRAVITY_MASK) != Gravity.TOP) {
			voffsetText = getVerticalOffset(false);
		}
		if ((mGravity & Gravity.HORIZONTAL_GRAVITY_MASK) != Gravity.LEFT) {
			hoffsetText = getHorizentalOffset(false);
		}
		
		
		
		canvas.translate(getExtendedPaddingLeft() + hoffsetText,
				extendedPaddingTop + voffsetText);
		
		

		// 使用path绘制路径文字
		canvas.save();

		Path path = new Path();
		path.moveTo(- borderWidth, getLayout().getLineBaseline(0)); // 移动到 坐标10,10
		path.lineTo(getLayout().getLineWidth(0) + borderWidth, getLayout().getLineBaseline(0));
		Paint citePaint = new Paint(paint);
		citePaint.setTextSize(getTextSize());
		
		//绘制边
		if ((borderColor & 0xff000000) != 0) {
			citePaint.setStyle(Style.STROKE);
			citePaint.setColor(borderColor);
			citePaint.setStrokeWidth(borderWidth * 2);
			canvas.drawTextOnPath(getText().toString(), path, borderWidth, 0, citePaint);
		}

		//绘制字本身
		if ((color & 0xff000000) != 0) {
			citePaint.setStyle(Style.FILL);
			citePaint.setColor(color);
			canvas.drawTextOnPath(getText().toString(), path, borderWidth, 0, citePaint);
		}
		
		canvas.restore();
	}
}