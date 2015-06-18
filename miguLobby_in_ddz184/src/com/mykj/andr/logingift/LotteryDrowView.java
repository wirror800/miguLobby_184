package com.mykj.andr.logingift;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.mykj.andr.model.LotteryDrowPrize;
import com.mykj.andr.provider.LotteryDrowProvider;
import com.mykj.andr.ui.CustomActivity;
import com.MyGame.Migu.R;
import com.mykj.game.utils.AppConfig;

/**
 * 抽奖机动画模块
 * 
 * @author JiangYinZhi
 * 
 */
public class LotteryDrowView extends View implements
		android.view.animation.Interpolator {

	// -----------------------------抽奖界面相关常量------------------------------------------
	private static final String TAG = "LOTTERY_DROW_VIEW";
	public static final int LOTTERY_PRIZE_NUM = 16;// 总奖品数量

	// ----------------------------抽奖界面的成员变量------------------------------------------
	private Context mContext;// 上下文
	private HashMap<String, Bitmap> prizeNumPieces;// 奖品数字图片集合体
	private ArrayList<LotteryDrowPrize> loDrowPrizes;// 抽奖奖励列表
	private HashMap<String, Bitmap> animPrizeNumPieces;// 奖品数字图片集合体(用于一键抽奖动画)
	private ArrayList<Bitmap> bitmapList;
	private float interval;// 奖品图片之间的间隔
	private float leftMargin; // 左边霓虹灯所占的宽度
	private float topMargin; // 上边霓虹灯所占的宽度

	// ----------------------------------抽奖机相关图片实例------------------------------------
	private Bitmap neonImg1;// 霓虹灯1
	private Bitmap neonImg2;// 霓虹灯2
	private Bitmap neonImg3;// 霓虹灯3
	private Bitmap defaultImg;// 缺省的图片，在图片没显示出来时候使用。\
	private Bitmap prizeNumImg;// 奖品数字的图片(未切割的)
	private Bitmap prizeAnimNumImg;// 奖品数字图片（用于动画的，未切割的）
	private Bitmap shadowImg;// 阴影部分图片
	private Bitmap prizeBG1Img;// 奖品背景图片1
	private Bitmap prizeBG2Img;// 奖品背景图片2
	private Bitmap prizeBG3Img;// 奖品背景图片3
	private Bitmap curtainImg;// 一键抽奖的窗帘背景
	private Bitmap multiGetPrizesLogoImg;// 一键抽奖结束logo图片
	private Bitmap cardEffectImg1;// 卡片效果1
	private Bitmap cardEffectImg2;// 卡片效果2
	private Bitmap cardEffectImg3;// 卡片效果3
	// -----------------------------------抽奖动画相关参数-------------------------------------
	/** 标志加速阶段 */
	public static final int ANIM_ACCELERATE = 0;
	/** 标志匀速阶段 */
	public static final int ANIM_NORMAL = 1;
	/** 标志减速阶段 */
	public static final int ANIM_DECELERATION = 2;
	/** 两个奖品匀速阶段走过一格的时间 */
	private static final long INTERVAL_TIME = 60;
	/** 需要变化的速度的格子数 */
	private static final int CHANGE_CELLS_NUM = 5;
	/** 动画超时圈数 */
	public static final int ANIM_TIMEOUT_NUM = 8;
	public int needChangeNum = CHANGE_CELLS_NUM;// 还需要改变几次，初始是CHANGE_NUM
	private int selectedIndex = -1;// 选中的位置(-1表示动画还未开启，-2表示已经开始一键抽奖动画)
	private int serverIndex = -1;// 服务器下发的抽中的奖品index；
	private boolean isOnceDrowAnimRunning = false;// 是否正在运行一次抽奖动画
	private int speedStep = -1;// 标记动画速度处于哪个阶段，-1是动画未开始，0表示加速阶段，1表示匀速阶段，2表示减速阶段
	private int anim_circle_num = 0;

	// ------------------------------------一键抽奖动画相关参数
	/** 一键抽奖准备的动画阶段 */
	public static final int MULTI_ANIM_READY = 0;
	/** 一键抽奖获取抽奖结果的动画阶段 */
	public static final int MULTI_ANIM_SHOW_PRIZES_NUM = 1;
	/** 一键抽奖完成动画后的阶段 */
	public static final int MULTI_ANIM_SHOW_CURTAIN = 2;
	/** 一键抽奖结束绘制奖品展示动画 */
	public static final int MULTI_ANIM_FINISH_PRIZES_SHOW = 3;
	/** 一键抽奖动画绘制完成 */
	public static final int MULTI_ANIM_FINISH_PRIZES_SHOW_FINISH = 4;
	/** 是否正在进行一键抽奖动画 */
	private boolean isMultiDrowAnimRunning = false;
	/** 一键抽奖后，展示数字，每段时间移动的距离 */
	public static final long MULTI_NUM_ANIM_SPEED = 5;
	private static final long MULTI_DROW_ANIM_INTERVAL_TIME = 260;// 一键抽奖动画格子间隔时间
	private static final long MULTI_DROW_ANIM_TIMEOUT_TIME = 5000;// 动画的超时时间
	public static final long MULTI_DROW_ANIM_MIN_TIME = 3000;// 一键抽奖的动画最少持续的时间
	private static int MULTI_DROW_CURTAIN_PULL_INTERVAL = 20;// 一键抽奖窗帘下拉时间，没段时间下拉距离
	private static int MULTI_DROW_PRIZES_INTERVAL = 10;// 一键抽奖奖品之间的间隔
	private ArrayList<LotteryDrowPrize> multiLoDrowPrizes;// 一键抽奖获奖列表
	private ArrayList<LotteryDrowPrize> multiLoDrowPrizesBySplit;//一键抽奖分割成单个获奖列表
	boolean isSingle = false;// 标识单双，true是标识单位的选中，false标识双位的选中
	private int multiAnimStep = -1;// 标记一键抽奖动画处于哪个阶段，-1表示动画未开始，0表示一将抽奖动画准备阶段,1表示抽奖结果动画阶段,2表示动画完成阶段
	private int multiCurtainHeight = 1;// 窗帘下拉的高度
	private int multiLoPrizeShowTag = 1;// 标记奖展示几个奖品
	private float multiPrizeShowWidth = -1;// 标记展示奖品
	private int neonImgShowTag = 0;// 霓虹灯展示标记
	private int cardEffectTag = 0;// 卡片特效标记
	private long multiStartTime = -1;// 一键抽奖开始时间；
	private boolean multiResultResponse = false;// 中奖结果返回成功

	// -------------------------------音效处理-----------------------------------------
	private SoundPool mSoundPool;// 音效池
	private int mOnceRunningSound;// 单次抽奖正在进行动画的音效
	private int mOnceSuccessSound;// 单次抽奖成功后的音效
	private int mMultiRunningSound;// 一键抽奖准备动画的音效
	private int mMultiEndSound;// 一键抽奖结束音效
	private int mMultiPrizeHintSound;// 一键抽奖中奖提示的音效
	private int mMultiShowPrize;// 一键抽奖显示奖品的音效

	private boolean isCompleteShowNumSound = false;// 展示数字的声效是否完成
	private boolean isCompleteShowCurtainSound = false;// 是否完成展示窗帘的音效
	private boolean isCompleteShowPrizeSound = false;// 是否完成展示奖品

	// ------------------------------构造函数-----------------------------------------
	public LotteryDrowView(Context mContext) {
		super(mContext);
		this.mContext = mContext;
		init();
	}

	public LotteryDrowView(Context mContext, AttributeSet attrs) {
		super(mContext, attrs);
		this.mContext = mContext;
		init();
	}

	// ---------------------------------------------抽奖数据的准备和数据变化的方法------------------------------
	/**
	 * 将抽奖配置赋值到类成员的奖励配置中
	 * 
	 * @param tempLoDrowPrizes
	 *            下发的抽奖机的奖品信息
	 */
	public void setLotteryDrowPrizes(
			ArrayList<LotteryDrowPrize> tempLoDrowPrizes) {
		if (loDrowPrizes == null) {
			loDrowPrizes = new ArrayList<LotteryDrowPrize>();// 初始化奖品集合
			LotteryDrowHelper.setImgPosition(loDrowPrizes,
					defaultImg.getWidth(), defaultImg.getHeight(), interval,
					leftMargin, topMargin);// 并设置每个奖品的位置
		}
		int total = loDrowPrizes.size();
		int temptotal = tempLoDrowPrizes.size();
		for (int i = 0; i < total; i++) {
			LotteryDrowPrize loDrowPrize = loDrowPrizes.get(i);
			for (int j = 0; j < temptotal; j++) {
				LotteryDrowPrize tempLoDrowPrize = tempLoDrowPrizes.get(j);
				if (i == j) {
					loDrowPrize.setId(tempLoDrowPrize.getId());
					loDrowPrize.setIndex(tempLoDrowPrize.getIndex());
					loDrowPrize.setType(tempLoDrowPrize.getType());
					loDrowPrize.setNum(tempLoDrowPrize.getNum());
					loDrowPrize.setName(tempLoDrowPrize.getName());
					loDrowPrize.setFileName(tempLoDrowPrize.getFileName());
					Bitmap numBitmap = LotteryDrowHelper.getBitmapFromNum(
							mContext, tempLoDrowPrize.getNum(), prizeNumPieces);
					loDrowPrize.setNumBitmap(numBitmap);
					loDrowPrize.setNumPosX(loDrowPrize.getNumPosX()
							- numBitmap.getWidth());
					if (i % 2 == 0) {
						loDrowPrize.setPrizeBGBitmap(prizeBG1Img);
					} else if (i % 2 == 1 && i / 2 != 2 && i / 2 != 6) {
						loDrowPrize.setPrizeBGBitmap(prizeBG2Img);
					} else if (i % 2 == 1 && (i / 2 == 2 || i / 2 == 6)) {
						loDrowPrize.setPrizeBGBitmap(prizeBG3Img);
					}
					break;
				}
			}

		}
		freshUI();
	}

	// -----------------------------------------初始化操作-------------------------------------------------

	// 获取图片，将图片缩放成适配的图片,缩放一些间隔
	private void scaleImg() {
		if (mContext != null) {
			if (interval == 0) {
				interval = LotteryDrowHelper.getDimension(R.dimen.img_interval,
						mContext);
			}
			if (leftMargin == 0) {
				leftMargin = LotteryDrowHelper.getDimension(
						R.dimen.lo_drow_view_neon_leftMargin, mContext)
						* LotteryDrowActivity.widthScale;
			}
			if (topMargin == 0) {
				topMargin = LotteryDrowHelper.getDimension(
						R.dimen.lo_drow_view_neon_topMargin, mContext)
						* LotteryDrowActivity.heightScale;
			}
		}
		if (neonImg1 == null || neonImg1.isRecycled()) {
			neonImg1 = LotteryDrowHelper.sacleBitmapFromAssert(mContext,
					"lo/lo_neon1.png", true, bitmapList);
		}
		if (neonImg2 == null || neonImg2.isRecycled()) {
			neonImg2 = LotteryDrowHelper.sacleBitmapFromAssert(mContext,
					"lo/lo_neon2.png", true, bitmapList);
		}
		if (neonImg3 == null || neonImg3.isRecycled()) {
			neonImg3 = LotteryDrowHelper.sacleBitmapFromAssert(mContext,
					"lo/lo_neon3.png", true, bitmapList);
		}
		if (defaultImg == null || defaultImg.isRecycled()) {
			defaultImg = LotteryDrowHelper.sacleBitmapFromAssert(mContext,
					"lo/lo_default_img.png", true, bitmapList);
		}
		if (prizeNumImg == null || prizeNumImg.isRecycled()) {
			prizeNumImg = LotteryDrowHelper.sacleBitmapFromAssert(mContext,
					"lo/lo_prize_num.png", true, bitmapList);
		}
		if (prizeAnimNumImg == null || prizeAnimNumImg.isRecycled()) {
			prizeAnimNumImg = LotteryDrowHelper.sacleBitmapFromAssert(mContext,
					"lo/lo_anim_prize_num.png", true, bitmapList);
		}
		if (shadowImg == null || shadowImg.isRecycled()) {
			shadowImg = LotteryDrowHelper.sacleBitmapFromAssert(mContext,
					"lo/lo_shadow.png", true, bitmapList);
		}
		if (prizeBG1Img == null || prizeBG1Img.isRecycled()) {
			prizeBG1Img = LotteryDrowHelper.sacleBitmapFromAssert(mContext,
					"lo/lo_prize_bg1.png", true, bitmapList);
		}
		if (prizeBG2Img == null || prizeBG2Img.isRecycled()) {
			prizeBG2Img = LotteryDrowHelper.sacleBitmapFromAssert(mContext,
					"lo/lo_prize_bg2.png", true, bitmapList);
		}
		if (prizeBG3Img == null || prizeBG3Img.isRecycled()) {
			prizeBG3Img = LotteryDrowHelper.sacleBitmapFromAssert(mContext,
					"lo/lo_prize_bg3.png", true, bitmapList);
		}

		// curtainImg = LotteryDrowHelper.sacleBitmapFromAssert(mContext,
		// "lo/lo_anim_curtain.png", true);
		if (curtainImg == null) {
			curtainImg = LotteryDrowHelper.getBitmapFromAssert(mContext,
					"lo/lo_anim_curtain.png");
		}
		if (multiGetPrizesLogoImg == null || multiGetPrizesLogoImg.isRecycled()) {
			multiGetPrizesLogoImg = LotteryDrowHelper.sacleBitmapFromAssert(
					mContext, "lo/lo_get_prize_logo.png", true, bitmapList);
		}
		if (cardEffectImg1 == null || cardEffectImg1.isRecycled()) {
			cardEffectImg1 = LotteryDrowHelper.sacleBitmapFromAssert(mContext,
					"lo/lo_card_effect1.png", true, bitmapList);
		}
		if (cardEffectImg2 == null || cardEffectImg2.isRecycled()) {
			cardEffectImg2 = LotteryDrowHelper.sacleBitmapFromAssert(mContext,
					"lo/lo_card_effect2.png", true, bitmapList);
		}
		if (cardEffectImg3 == null || cardEffectImg3.isRecycled()) {
			cardEffectImg3 = LotteryDrowHelper.sacleBitmapFromAssert(mContext,
					"lo/lo_card_effect3.png", true, bitmapList);
		}
	}

	// 初始化操作
	@SuppressWarnings("deprecation")
	private void init() {
		bitmapList = ((LotteryDrowActivity) mContext).getBitmapList();
		// 声效的相关初始化
		if (mSoundPool == null) {
			mSoundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
		}
		if (mOnceRunningSound == 0) {
			mOnceRunningSound = mSoundPool.load(mContext,
					R.raw.once_lottery_running, 1);
		}
		if (mOnceSuccessSound == 0) {
			mOnceSuccessSound = mSoundPool.load(mContext,
					R.raw.once_lottery_success, 1);
		}
		if (mMultiRunningSound == 0) {
			mMultiRunningSound = mSoundPool.load(mContext,
					R.raw.multi_lottery_running, 1);
		}
		if (mMultiEndSound == 0) {
			mMultiEndSound = mSoundPool.load(mContext, R.raw.multi_lottery_end,
					1);
		}
		if (mMultiPrizeHintSound == 0) {
			mMultiPrizeHintSound = mSoundPool.load(mContext,
					R.raw.multi_lottery_prize_hint, 1);
		}
		if (mMultiShowPrize == 0) {
			mMultiShowPrize = mSoundPool.load(mContext,
					R.raw.multi_lottery_show_prize, 1);
		}
		if (neonImg1 != null) {
			this.setBackgroundDrawable(new BitmapDrawable(neonImg1));// 设置背景
		}
		loDrowPrizes = new ArrayList<LotteryDrowPrize>();// 初始化奖品集合
		scaleImg();// 缩放图片
		LotteryDrowHelper.setImgPosition(loDrowPrizes, defaultImg.getWidth(),
				defaultImg.getHeight(), interval, leftMargin, topMargin);// 并设置每个奖品的位置
		// 启动抽奖机的activity，进行一键抽奖配置请求和获取抽奖名单请求
		LotteryDrowProvider.getInstance(mContext).requestMultiLotteryConfig();
		if (prizeNumPieces == null) {
			prizeNumPieces = LotteryDrowHelper.splitBitmap(prizeNumImg, 13, 1);// 切割数字图片获取数字图片集合
		}
		if (animPrizeNumPieces == null) {
			animPrizeNumPieces = LotteryDrowHelper.splitNumAnimBitmap(
					prizeAnimNumImg, 11, 1);
		}
	}

	// ------------------------------绘制图片的方法--------------------------------------

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (loDrowPrizes == null || loDrowPrizes.size() <= 0) {
			return;
		}
		Paint mPaint = new Paint();
		// 绘制立即抽奖的动画
		if (isOnceDrowAnimRunning) {
			onDrawOnceLotteryAnim(loDrowPrizes, canvas, mPaint);
		}
		// 绘制一键抽奖的动画
		if (isMultiDrowAnimRunning) {
			onDrawMultiLotteryAnim(loDrowPrizes, canvas, mPaint);
			if (multiAnimStep == MULTI_ANIM_FINISH_PRIZES_SHOW_FINISH) {
				return;
			}
		}
		// 没有抽奖的动画绘制的基本界面
		if (!isOnceDrowAnimRunning && !isMultiDrowAnimRunning) {
			ondrawBaseImgs(loDrowPrizes, canvas, mPaint);
		}
	}

	/**
	 * 绘制基本的图片，奖品的展示图片
	 * 
	 * @param loDrowPrizes
	 *            奖品列表
	 */
	public void ondrawBaseImgs(ArrayList<LotteryDrowPrize> loDrowPrizes,
			Canvas mCanvas, Paint mPaint) {
		if (loDrowPrizes == null || loDrowPrizes.size() <= 0) {
			return;
		}
		int total = loDrowPrizes.size();
		mCanvas.drawBitmap(neonImg1, 0, 0, mPaint);
		for (int i = 0; i < total; i++) {
			// 绘制每个奖品的相关图片和文字
			LotteryDrowPrize loDrowPrize = loDrowPrizes.get(i);
			Bitmap prizeBitmap = loDrowPrize.getPrizeBitmap();
			String loDrowPrizeName = loDrowPrize.getName();
			Bitmap loDrowPrizeNumBitmap = loDrowPrize.getNumBitmap();
			if (prizeBitmap != null) {
				mCanvas.drawBitmap(prizeBitmap, loDrowPrize.getImgPosX(),
						loDrowPrize.getImgPosY(), mPaint);
			}
			if (loDrowPrizeName != null && !loDrowPrizeName.trim().equals("")) {
				mPaint.reset();
				mPaint.setTextSize(LotteryDrowMainView.fontSize5);
				mPaint.setColor(Color.WHITE);
				mPaint.setTypeface(Typeface.DEFAULT_BOLD);
				mPaint.setAntiAlias(true);
				float txtPosX = (defaultImg.getWidth() - mPaint
						.measureText(loDrowPrizeName))
						/ 2
						+ loDrowPrize.getTxtPosX();
				mCanvas.drawText(loDrowPrizeName, txtPosX,
						loDrowPrize.getTxtPosY(), mPaint);
			}
			mPaint.reset();
			if (loDrowPrizeNumBitmap != null) {
				mCanvas.drawBitmap(loDrowPrizeNumBitmap,
						loDrowPrize.getNumPosX(), loDrowPrize.getNumPosY(),
						mPaint);
			}
		}
	}

	/**
	 * 绘制立即抽奖动画
	 * 
	 * @param loDrowPrizes
	 * @param mCanvas
	 */
	public void onDrawOnceLotteryAnim(ArrayList<LotteryDrowPrize> loDrowPrizes,
			Canvas mCanvas, Paint mPaint) {
		if (loDrowPrizes == null || loDrowPrizes.size() <= 0 || mCanvas == null
				|| mPaint == null) {
			return;
		}
		ondrawBaseImgs(loDrowPrizes, mCanvas, mPaint);
		int total = loDrowPrizes.size();
		for (int i = 0; i < total; i++) {
			LotteryDrowPrize loDrowPrize = loDrowPrizes.get(i);
			if (!loDrowPrize.isSelected()) {
				mCanvas.drawBitmap(shadowImg, loDrowPrize.getImgPosX(),
						loDrowPrize.getImgPosY(), mPaint);
			}
		}
	}

	/**
	 * 绘制一键抽奖动画
	 * 
	 * @param loDrowPrizes
	 * @param mCanvas
	 * @param mPaint
	 */
	public void onDrawMultiLotteryAnim(
			ArrayList<LotteryDrowPrize> loDrowPrizes, Canvas mCanvas,
			Paint mPaint) {
		if (loDrowPrizes == null || loDrowPrizes.size() <= 0 || mCanvas == null
				|| mPaint == null) {
			return;
		}
		int total = loDrowPrizes.size();
		switch (multiAnimStep) {
		// 绘制准备阶段动画
		case MULTI_ANIM_READY:
			ondrawBaseImgs(loDrowPrizes, mCanvas, mPaint);
			// 标识是否有图片开始选择
			if (selectedIndex == -1) {
				return;
			}
			for (int i = 0; i < total; i++) {
				LotteryDrowPrize loDrowPrize = loDrowPrizes.get(i);
				if (!loDrowPrize.isSelected()) {
					mCanvas.drawBitmap(shadowImg, loDrowPrize.getImgPosX(),
							loDrowPrize.getImgPosY(), mPaint);
				}
			}
			break;
		// 绘制中奖数字展示动画
		case MULTI_ANIM_SHOW_PRIZES_NUM:
			ondrawBaseImgs(loDrowPrizes, mCanvas, mPaint);

			for (int i = 0; i < total; i++) {
				LotteryDrowPrize loDrowPrize = loDrowPrizes.get(i);
				if (!loDrowPrize.isSelected()) {
					mCanvas.drawBitmap(shadowImg, loDrowPrize.getImgPosX(),
							loDrowPrize.getImgPosY(), mPaint);
				} else {
					Log.i(TAG, "ondrawLoIndex:" + loDrowPrize.getIndex()
							+ "  select:" + loDrowPrize.isSelected());
				}
			}
			// 绘制奖品的数字动画
			for (int j = 0; j < multiLoDrowPrizes.size(); j++) {
				LotteryDrowPrize multiLoDroePrize = multiLoDrowPrizes.get(j);
				mCanvas.drawBitmap(multiLoDroePrize.getNumAnimBitmap(),
						multiLoDroePrize.getMultiNumPosX(),
						multiLoDroePrize.getMultiNumPoxY(), mPaint);
			}
			break;
		// 绘制窗帘动画
		case MULTI_ANIM_SHOW_CURTAIN:
			ondrawBaseImgs(loDrowPrizes, mCanvas, mPaint);
			if (curtainImg != null) {
				Matrix matrix = new Matrix();
				float scaleWidth = (float) neonImg1.getWidth()
						/ curtainImg.getWidth();
				float scaleHeight = (float) multiCurtainHeight
						/ curtainImg.getHeight();
				Log.i(TAG, "scaleWidth: " + scaleWidth + " scaleHeight:"
						+ scaleHeight);
				matrix.postScale(scaleWidth, scaleHeight);
				mCanvas.drawBitmap(curtainImg, matrix, mPaint);
				if (multiCurtainHeight >= this.getHeight()) {
					mCanvas.drawBitmap(multiGetPrizesLogoImg, this.getWidth()
							/ 2 - multiGetPrizesLogoImg.getWidth() / 2,
							this.getHeight() / 2, mPaint);
					isCompleteShowCurtainSound = false;
					multiAnimStep = MULTI_ANIM_FINISH_PRIZES_SHOW;
				}
			}
			break;
		// 绘制奖品展示动画
		case MULTI_ANIM_FINISH_PRIZES_SHOW:
			// 绘制整屏窗帘
			Matrix matrix = new Matrix();
			float scaleWidth = (float) neonImg1.getWidth()
					/ curtainImg.getWidth();
			float scaleHeight = (float) neonImg1.getHeight()
					/ curtainImg.getHeight();
			matrix.postScale(scaleWidth, scaleHeight);
			mCanvas.drawBitmap(curtainImg, matrix, mPaint);
			// 绘制获得奖品的logo
			mCanvas.drawBitmap(multiGetPrizesLogoImg, this.getWidth() / 2
					- multiGetPrizesLogoImg.getWidth() / 2,
					this.getHeight() / 2, mPaint);
			// 绘制奖品图片逻辑
			if (multiLoPrizeShowTag > 0
					&& multiLoPrizeShowTag <= multiLoDrowPrizesBySplit.size()) {
				for (int i = 0; i < multiLoPrizeShowTag; i++) {
					LotteryDrowPrize multiLoDrowPrize = multiLoDrowPrizesBySplit
							.get(i);
					Bitmap showPrizeBitmap = multiLoDrowPrize
							.getAnimShowBitmap();
					if (multiPrizeShowWidth == -1) {
						multiPrizeShowWidth = showPrizeBitmap.getWidth() * 2 / 5;
					}
					float left = 0;
					if (i + 1 == multiLoPrizeShowTag) {
						// 判断是否要绘制特效图片
						if (cardEffectTag == 4) {
							if (!isCompleteShowPrizeSound) {
								((CustomActivity) AppConfig.mContext)
										.playAudio(mSoundPool, mMultiShowPrize);
								isCompleteShowPrizeSound = true;
							}
							float widthScale = multiPrizeShowWidth
									/ showPrizeBitmap.getWidth();
							left = (float) (this.getWidth() / 2 - multiPrizeShowWidth / 2);
							Matrix mMatrix = new Matrix();
							// mMatrix.setTranslate(left, 50);
							mMatrix.postScale(widthScale, 1);
							Bitmap scaleShowPrizeBitmap = Bitmap.createBitmap(
									showPrizeBitmap, 0, 0,
									showPrizeBitmap.getWidth(),
									showPrizeBitmap.getHeight(), mMatrix, true);
							mCanvas.drawBitmap(scaleShowPrizeBitmap, left, 50,
									mPaint);
						} else {
							if (cardEffectTag == 0) {
								cardEffectTag = 1;
							}
							if (cardEffectTag == 1) {
								showPrizeBitmap = cardEffectImg1;
							} else if (cardEffectTag == 2) {
								showPrizeBitmap = cardEffectImg2;
							} else if (cardEffectTag == 3) {
								showPrizeBitmap = cardEffectImg3;
							}
							multiPrizeShowWidth = showPrizeBitmap.getWidth();
							left = (float) (this.getWidth() / 2 - multiPrizeShowWidth / 2);
							mCanvas.drawBitmap(showPrizeBitmap, left, 50,
									mPaint);
						}
					} else if (i % 2 == 0 && i + 1 != multiLoPrizeShowTag) {
						left = (float) (this.getWidth() / 2
								- showPrizeBitmap.getWidth() / 2 - (showPrizeBitmap
								.getWidth() + MULTI_DROW_PRIZES_INTERVAL)
								* ((multiLoPrizeShowTag - i) / 2));
						mCanvas.drawBitmap(showPrizeBitmap, left, 50, mPaint);
					} else if (i % 2 != 0 && i + 1 != multiLoPrizeShowTag) {
						left = (float) (this.getWidth()
								/ 2
								- showPrizeBitmap.getWidth()
								/ 2
								+ (showPrizeBitmap.getWidth() + MULTI_DROW_PRIZES_INTERVAL)
								* ((multiLoPrizeShowTag - i) / 2 - 1)
								+ showPrizeBitmap.getWidth() + MULTI_DROW_PRIZES_INTERVAL);
						mCanvas.drawBitmap(showPrizeBitmap, left, 50, mPaint);
					}
				}
				if (cardEffectTag == 4) {
					if (multiPrizeShowWidth <= multiLoDrowPrizes.get(0)
							.getAnimShowBitmap().getWidth()) {
						multiPrizeShowWidth = multiPrizeShowWidth + 15;
					} else {
						if (multiLoPrizeShowTag < multiLoDrowPrizesBySplit.size()) {
							multiLoPrizeShowTag++;
							multiPrizeShowWidth = -1;
							cardEffectTag = 0;
							isCompleteShowPrizeSound = false;
						} else {
							cardEffectTag = 0;
							neonImgShowTag = 0;
							LotteryDrowActivity loDrowActivity = (LotteryDrowActivity) mContext;
							if (loDrowActivity != null) {
								loDrowActivity.getLotteryMainView().mLotteryHandler
										.sendEmptyMessage(LotteryDrowMainView.LOTTERY_MULTI_DROW_SHOW_PRIZES_FINISH);
							}
							multiAnimStep = MULTI_ANIM_FINISH_PRIZES_SHOW_FINISH;
							// // 显示动画结束
							// isMultiDrowAnimRunning = false;
							// return;
						}
					}
				}
			}
			break;
		// 奖品展示完成的画面
		case MULTI_ANIM_FINISH_PRIZES_SHOW_FINISH:
			// 绘制整屏窗帘
			Matrix matrix1 = new Matrix();
			float scaleWidth1 = (float) neonImg1.getWidth()
					/ curtainImg.getWidth();
			float scaleHeight1 = (float) neonImg1.getHeight()
					/ curtainImg.getHeight();
			matrix1.postScale(scaleWidth1, scaleHeight1);
			mCanvas.drawBitmap(curtainImg, matrix1, mPaint);
			// 绘制获得奖品的logo
			mCanvas.drawBitmap(multiGetPrizesLogoImg, this.getWidth() / 2
					- multiGetPrizesLogoImg.getWidth() / 2,
					this.getHeight() / 2, mPaint);
			float left = 0;
			for (int i = 0; i < multiLoDrowPrizesBySplit.size(); i++) {
				LotteryDrowPrize multiLoDrowPrize = multiLoDrowPrizesBySplit.get(i);
				Bitmap showPrizeBitmap = multiLoDrowPrize.getAnimShowBitmap();
				if (i % 2 == 0) {
					left = (float) (this.getWidth() / 2
							- showPrizeBitmap.getWidth() / 2 - (showPrizeBitmap
							.getWidth() + MULTI_DROW_PRIZES_INTERVAL)
							* ((multiLoPrizeShowTag - i) / 2));
					mCanvas.drawBitmap(showPrizeBitmap, left, 50, mPaint);
				} else if (i % 2 != 0) {
					left = (float) (this.getWidth()
							/ 2
							- showPrizeBitmap.getWidth()
							/ 2
							+ (showPrizeBitmap.getWidth() + MULTI_DROW_PRIZES_INTERVAL)
							* ((multiLoPrizeShowTag - i) / 2 - 1)
							+ showPrizeBitmap.getWidth() + MULTI_DROW_PRIZES_INTERVAL);
					mCanvas.drawBitmap(showPrizeBitmap, left, 50, mPaint);
				}
			}
			break;
		default:
			break;
		}
		// 绘制霓虹灯
		if (isMultiDrowAnimRunning || isOnceDrowAnimRunning) {
			onDrawNeonImg(mCanvas, mPaint);
		}
	}

	/**
	 * 刷新界面
	 */
	public void freshUI() {
		invalidate();
	}

	// --------------------------------------老虎机动画的逻辑的方法----------------------------------------------
	// 动画开启,run里为动画逻辑
	public void startOnceDrowAnim() {
		// 开始动画，清空数据
		stopMultiDrowAnim();
		stopOnceDrowAnim();
		// 开始动画，设置为加速
		speedStep = ANIM_ACCELERATE;
		Thread animThread = new Thread() {
			// 计算加速速率
			float rate = (float) (1 * 2) / (float) CHANGE_CELLS_NUM
					* CHANGE_CELLS_NUM;

			@Override
			public void run() {
				LotteryDrowActivity loDrowActivity = (LotteryDrowActivity) mContext;
				// 表示初始位置,用于计算圈数
				int initIndex = 0;
				if (selectedIndex != -1) {
					initIndex = selectedIndex;
				} else {
					initIndex = 0;
				}
				while (isOnceDrowAnimRunning) {
					// 如果标记的光标的位置是-1,则随机初始化光标位置,或者往下一个位置移动
					if (selectedIndex == -1) {
						Random random = new Random();
						selectedIndex = random.nextInt(LOTTERY_PRIZE_NUM) + 1;
						initIndex = selectedIndex;
					} else {
						if (selectedIndex == LOTTERY_PRIZE_NUM) {
							selectedIndex = 1;
						} else {
							selectedIndex = selectedIndex + 1;
						}
					}
					// 设置光标对准的格子为选中状态
					if (loDrowPrizes != null && loDrowPrizes.size() > 0) {
						for (int i = 0; i < loDrowPrizes.size(); i++) {
							LotteryDrowPrize loDrowPrize = loDrowPrizes.get(i);
							if (selectedIndex == i + 1) {
								loDrowPrize.setSelected(true);
							} else {
								loDrowPrize.setSelected(false);
							}
						}
					}
					// 走过一格一下声效
					((CustomActivity) AppConfig.mContext).playAudio(mSoundPool,
							mOnceRunningSound);
					// 走过一圈 计算圈数
					if (selectedIndex == initIndex) {
						anim_circle_num++;
					}
					// 如果超过超时的圈数，做超时操作
					if (anim_circle_num >= ANIM_TIMEOUT_NUM) {
						if (loDrowActivity != null) {
							loDrowActivity.getLotteryMainView().mLotteryHandler
									.sendEmptyMessage(LotteryDrowMainView.LOTTERY_ONCE_DROW_TIMEOUT);
						}
					}
					;
					Log.i(TAG, "动画位置:" + selectedIndex + "    " + "服务器下发的位置:"
							+ serverIndex + "圈数：" + anim_circle_num);
					Log.i(TAG, "阶段" + speedStep + "  需要改变的圈数" + needChangeNum);
					postInvalidate();
					// 判断接收到服务器抽奖结果的时候的处理(接收到服务器的抽奖结果，并且转过4圈)
					if (serverIndex != -1 && anim_circle_num > 4) {
						if ((serverIndex - selectedIndex == CHANGE_CELLS_NUM)
								|| (serverIndex + 16 - selectedIndex == CHANGE_CELLS_NUM)) {
							if (loDrowActivity != null) {
								speedStep = ANIM_DECELERATION;
								loDrowActivity.getLotteryMainView().mLotteryHandler
										.sendEmptyMessage(LotteryDrowMainView.LOTTERY_ONCE_DROW_END);
							}
						}
						// 4圈以后,当服务端和动画最终位置一致，并且减速完成以后
						if (serverIndex == selectedIndex
								&& needChangeNum == CHANGE_CELLS_NUM) {
							if (loDrowActivity != null) {
								LotteryDrowPrize loDrowPrize = loDrowPrizes
										.get(selectedIndex - 1);
								postInvalidate();
								((CustomActivity) AppConfig.mContext)
										.playAudio(mSoundPool,
												mOnceSuccessSound);
								try {
									sleep(500);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								// isOnceDrowAnimRunning =false;
								String resultHint = "中奖啦,恭喜您获得:"
										+ loDrowPrize.getName() + "×"
										+ loDrowPrize.getNum();
								Message msg = new Message();
								msg.what = LotteryDrowMainView.LOTTERY_ONCE_DROW_FINISH;
								msg.obj = resultHint;
								msg.arg1 = loDrowPrize.getType();// 传奖品类型，2的话中的是乐豆，进行乐豆展示书刷新
								loDrowActivity.getLotteryMainView().mLotteryHandler
										.sendMessage(msg);
								return;
							}
						}
					}
					try {
						// 根据不同情况计算睡眠时间进行动画
						float changeSpeed = getInterpolation(rate);
						if ((speedStep == ANIM_ACCELERATE) && needChangeNum < 0) {
							// 加速到匀速的值时，变成匀速阶段
							if (mContext != null) {
								loDrowActivity.getLotteryMainView().mLotteryHandler
										.sendEmptyMessage(LotteryDrowMainView.LOTTERY_ONCE_DROWING);
							}

							changeSpeed = 0;
							needChangeNum = 0;

						} else if ((speedStep == ANIM_DECELERATION)
								&& needChangeNum > CHANGE_CELLS_NUM) {
							changeSpeed = -1;
						}
						sleep((long) (INTERVAL_TIME + INTERVAL_TIME
								* changeSpeed));
					} catch (InterruptedException e) {
						Log.e(TAG, "睡眠时间出错", e);
					}
				}
			};
		};
		animThread.start();
		if (!isOnceDrowAnimRunning) {
			isOnceDrowAnimRunning = true;
		}
	}

	/**
	 * 停止单次抽奖动画
	 */
	public void stopOnceDrowAnim() {
		isOnceDrowAnimRunning = false;
		stopAudio();
		initOnceAnimData();
	}
	
	//停止音效
	public void stopAudio(){
		mSoundPool.pause(mOnceRunningSound);
		mSoundPool.pause(mOnceSuccessSound);
		mSoundPool.pause(mMultiRunningSound);
		mSoundPool.pause(mMultiEndSound);
		mSoundPool.pause(mMultiPrizeHintSound);
		mSoundPool.pause(mMultiShowPrize);
	}

	/***
	 * 初始动画数据
	 */
	public void initOnceAnimData() {
		neonImgShowTag = 0;
		anim_circle_num = 0;
		selectedIndex = -1;
		serverIndex = -1;
		speedStep = -1;
		needChangeNum = CHANGE_CELLS_NUM;
	}

	/**
	 * 获取动画的变化速率
	 */
	@Override
	public float getInterpolation(float arg0) {
		float speedRata = 0f;
		switch (speedStep) {
		case ANIM_ACCELERATE:
			// 加速的时候改变一次
			speedRata = arg0 * needChangeNum * needChangeNum / 8;
			needChangeNum--;
			break;
		case ANIM_NORMAL:
			speedRata = 0f;
			break;
		case ANIM_DECELERATION:
			// 减速的此时改变一次
			speedRata = arg0 * needChangeNum * needChangeNum / 8;
			needChangeNum++;
			break;

		default:
			break;
		}
		return speedRata;
	}

	/**
	 * 设置动画是哪个阶段，0：减速 1：匀速 2：减速
	 * 
	 * @param mAnimStep
	 */
	public void setAnimStep(int mAnimStep) {
		this.speedStep = mAnimStep;
	}

	/**
	 * 接收服务器抽奖结果
	 * 
	 * @param drowResult
	 */
	public void setLotteryDrowResult(int drowResult) {
		Log.i("抽奖结果:", drowResult + "");
		this.serverIndex = drowResult;
	}

	// ---------------------------------------一键抽奖动画逻辑-------------------------------------------------
	/**
	 * 一键抽奖动画
	 */
	public void startMultiDrowAnim() {
		// 设置当前时间 用于计算准备动画是不是达到最少的准备动画时间
		long startMultiDrowTime = System.currentTimeMillis();
		setMultiStartTime(startMultiDrowTime);
		Log.i("开始时间", startMultiDrowTime + "");
		stopMultiDrowAnim();
		stopOnceDrowAnim();
		// 标识可以进行阴影部分绘制
		selectedIndex = -2;
		multiAnimStep = MULTI_ANIM_READY;
		final long startTime = System.currentTimeMillis();// 初始时间 计算超时时间
		Thread animThread = new Thread() {

			@Override
			public void run() {
				super.run();
				while (isMultiDrowAnimRunning) {
					long endTime = 0;
					// 设置准备动画
					if (multiAnimStep == MULTI_ANIM_READY) {
						endTime = System.currentTimeMillis() - startTime;
						if (endTime > MULTI_DROW_ANIM_TIMEOUT_TIME) {
							LotteryDrowActivity loDrowActivity = (LotteryDrowActivity) mContext;
							if (loDrowActivity != null) {
								loDrowActivity.getLotteryMainView().mLotteryHandler
										.sendEmptyMessage(LotteryDrowMainView.LOTTERY_MULTI_DROW_TIMEOUT);
								return;
							}
						}
						setMultiReadyAnim(loDrowPrizes);
					}
					if (multiAnimStep == MULTI_ANIM_SHOW_PRIZES_NUM) {
						setMultiEndAnim();
					}
					if (multiAnimStep == MULTI_ANIM_SHOW_CURTAIN) {
						setMultiFinishAnim();
					}
					if (multiAnimStep == MULTI_ANIM_FINISH_PRIZES_SHOW) {
						setMultiPrizesShowAnim();
					}

				}
			}
		};
		animThread.start();
		if (isMultiDrowAnimRunning == false) {
			isMultiDrowAnimRunning = true;
		}
	}

	/**
	 * 停止一键抽奖动画
	 */
	public void stopMultiDrowAnim() {
		neonImgShowTag = 0;
		isMultiDrowAnimRunning = false;
		selectedIndex = -1;
		multiLoDrowPrizes = null;
		multiLoDrowPrizesBySplit = null;
		isSingle = false;
		multiAnimStep = -1;
		multiCurtainHeight = -1;
		multiLoPrizeShowTag = 1;
		multiPrizeShowWidth = -1;
		multiStartTime = -1;
		mSoundPool.autoPause();
		stopAudio();
		invalidate();
	}

	// 一键抽奖开始的准备动画
	private void setMultiReadyAnim(ArrayList<LotteryDrowPrize> loDrowPrizes) {
		if (loDrowPrizes != null && loDrowPrizes.size() > 0) {
			for (int i = 0; i < loDrowPrizes.size(); i++) {
				LotteryDrowPrize loDrowPrize = loDrowPrizes.get(i);
				if (isSingle == true) {
					if (i % 2 != 0) {
						// 收到中奖结果返回，防止同时改变数据
						if (!multiResultResponse) {
							loDrowPrize.setSelected(true);
						}
					} else {
						// 收到中奖结果返回，防止同时改变数据
						if (!multiResultResponse) {
							loDrowPrize.setSelected(false);
						}
					}
					((CustomActivity) AppConfig.mContext).playAudio(mSoundPool,
							mMultiRunningSound);
				}
				if (isSingle == false) {
					if (i % 2 == 0) {
						// 收到中奖结果返回，防止同时改变数据
						if (!multiResultResponse) {
							loDrowPrize.setSelected(true);
						}
					} else {
						// 收到中奖结果返回，防止同时改变数据
						if (!multiResultResponse) {
							loDrowPrize.setSelected(false);
						}
					}
				}
			}
		}
		postInvalidate();
		// 间隔一段时间
		try {
			if (isSingle == true) {
				isSingle = false;
			} else {
				isSingle = true;
			}
			Thread.sleep(MULTI_DROW_ANIM_INTERVAL_TIME);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// 一键抽奖结束动画，根据抽奖结果进行动画
	private void setMultiEndAnim() {
		// 展示数字时的音效
		if (!isCompleteShowNumSound) {
			((CustomActivity) AppConfig.mContext).playAudio(mSoundPool,
					mMultiEndSound);
			isCompleteShowNumSound = true;
		}
		if (loDrowPrizes == null || multiLoDrowPrizes == null) {
			return;
		}
		for (int i = 0; i < multiLoDrowPrizes.size(); i++) {
			LotteryDrowPrize multiLoDrowPrize = multiLoDrowPrizes.get(i);
			float numYPos = multiLoDrowPrize.getMultiNumPoxY()
					- MULTI_NUM_ANIM_SPEED;
			if (numYPos <= multiLoDrowPrize.getImgPosY()) {
				LotteryDrowActivity loDrowActivity = (LotteryDrowActivity) mContext;
				if (loDrowActivity != null) {
					loDrowActivity.getLotteryMainView().mLotteryHandler
							.sendEmptyMessage(LotteryDrowMainView.LOTTERY_MULTI_DROW_SHOW_CURTAIN_ANIM);
					isCompleteShowNumSound = false;
				}
			}
			multiLoDrowPrize.setMultiNumPoxY(numYPos);
		}
		try {
			Thread.sleep(75);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		postInvalidate();
	}

	/**
	 * 设置一键抽奖结束展示数字动画
	 */
	public void setMultiFinishAnim() {
		if (multiCurtainHeight <= this.getHeight()) {
			multiCurtainHeight = multiCurtainHeight
					+ (int) (MULTI_DROW_CURTAIN_PULL_INTERVAL * LotteryDrowActivity.heightScale);
		} else {
			multiCurtainHeight = this.getHeight();
		}
		if (!isCompleteShowCurtainSound) {
			((CustomActivity) AppConfig.mContext).playAudio(mSoundPool,
					mMultiPrizeHintSound);
			isCompleteShowCurtainSound = true;
		}
		postInvalidate();
		try {
			Thread.sleep(20);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void setMultiPrizesShowAnim() {
		postInvalidate();
		if (cardEffectTag == 4 || cardEffectTag == 0) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			try {
				Thread.sleep(150);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (cardEffectTag <= 3 && cardEffectTag > 0) {
			cardEffectTag++;
			if (cardEffectTag == 4) {
				multiPrizeShowWidth = -1;
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * 设置一键抽奖结束动画相关数据
	 * 
	 * @param multiAnimStep
	 */
	public void setMultiEndAnimData(
			ArrayList<LotteryDrowPrize> tempMultiLoDrowPrizes) {
		if (loDrowPrizes == null) {
			return;
		}
		multiResultResponse = true;
		multiLoDrowPrizes = null;
		multiLoDrowPrizesBySplit = null;
		multiLoDrowPrizes = new ArrayList<LotteryDrowPrize>();
		multiLoDrowPrizesBySplit = new ArrayList<LotteryDrowPrize>();;
		for (int i = 0; i < loDrowPrizes.size(); i++) {
			LotteryDrowPrize loDrowPrize = loDrowPrizes.get(i);
			for (int j = 0; j < tempMultiLoDrowPrizes.size(); j++) {
				LotteryDrowPrize tempMultiLoDrowPrize = tempMultiLoDrowPrizes
						.get(j);
				if (loDrowPrize.getIndex() == tempMultiLoDrowPrize.getIndex()) {

					Log.i(TAG,
							"showPrizeIndex: "
									+ tempMultiLoDrowPrize.getIndex()
									+ "  select:"
									+ tempMultiLoDrowPrize.isSelected());
					loDrowPrize.setSelected(true);
					tempMultiLoDrowPrize.setImgPosY(loDrowPrize.getImgPosY());
					Bitmap numAnimBitmap = LotteryDrowHelper
							.getAnimBitmapFromNum(mContext,
									tempMultiLoDrowPrize.getPrizeNum(),
									animPrizeNumPieces);
					tempMultiLoDrowPrize.setNumAnimBitmap(numAnimBitmap);
					tempMultiLoDrowPrize
							.setMultiNumPosX(loDrowPrize.getMultiNumPosX()
									+ (defaultImg.getWidth() - numAnimBitmap
											.getWidth()) / 2);
					tempMultiLoDrowPrize.setMultiNumPoxY(loDrowPrize
							.getMultiNumPoxY());
					tempMultiLoDrowPrize.setAnimShowBitmap(loDrowPrize
							.getAnimShowBitmap());
					multiLoDrowPrizes.add(tempMultiLoDrowPrize);
					for(int k = 0;k<tempMultiLoDrowPrize.getPrizeNum();k++){
						multiLoDrowPrizesBySplit.add(tempMultiLoDrowPrize);
					}
					break;
				} else {
					loDrowPrize.setSelected(false);
				}
			}
		}
		LotteryDrowActivity loDrowActivity = (LotteryDrowActivity) mContext;
		if (loDrowActivity != null) {
			setMultiAnimStep(LotteryDrowView.MULTI_ANIM_SHOW_PRIZES_NUM);
//			loDrowActivity.getLotteryMainView().mLotteryHandler
//					.sendEmptyMessage(LotteryDrowMainView.LOTTERY_MULTI_DROW_SHOW_PRIZES_NUM);
			multiResultResponse = false;
		}

	}

	public void setMultiAnimStep(int multiAnimStep) {
		this.multiAnimStep = multiAnimStep;
	}

	private void onDrawNeonImg(Canvas mCanvas, Paint mPaint) {
		if (neonImgShowTag == 1 || neonImgShowTag == 0) {
			mCanvas.drawBitmap(neonImg1, 0, 0, mPaint);
		} else if (neonImgShowTag == 2) {
			mCanvas.drawBitmap(neonImg2, 0, 0, mPaint);
		} else if (neonImgShowTag == 3) {
			mCanvas.drawBitmap(neonImg3, 0, 0, mPaint);
		}
		if (neonImgShowTag < 3) {
			neonImgShowTag++;
		} else {
			neonImgShowTag = 1;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
//		if (event.getAction() == MotionEvent.ACTION_DOWN) {
//			if (multiAnimStep == MULTI_ANIM_FINISH_PRIZES_SHOW_FINISH) {
//				stopMultiDrowAnim();
//				stopOnceDrowAnim();
//				postInvalidate();
//			}
//		}
		return super.onTouchEvent(event);
	}

	// --------------------------------------获取需要的类成员变量的方法-------------------------------------
	/**
	 * 获取缺省的奖品图片
	 * 
	 * @return
	 */
	public Bitmap getDefaultPrizeImg() {
		return defaultImg;
	}

	/**
	 * 获取奖品信息
	 * 
	 * @return 奖品信息
	 */
	public ArrayList<LotteryDrowPrize> getLoDrowPrizes() {
		return loDrowPrizes;
	}

	/**
	 * 获取单次抽奖是否正在进行动画
	 * 
	 * @return
	 */
	public boolean getOnceDrowIsRunning() {
		return isOnceDrowAnimRunning;
	}

	/**
	 * 获取一键抽奖是否正在进行动画
	 * 
	 * @return
	 */
	public boolean getMultiDrowIsRunning() {
		return isMultiDrowAnimRunning;
	}

	/**
	 * 设置一键抽奖的开始时间，用于计算一键抽奖开始经过多久
	 */
	public void setMultiStartTime(long multiStartTime) {
		this.multiStartTime = multiStartTime;
	}

	public long getMultiStartTime() {
		return multiStartTime;
	}

	// 动画界面回收
	public void recycle() {
		stopMultiDrowAnim();
		stopOnceDrowAnim();
		if (loDrowPrizes != null) {
			loDrowPrizes.clear();
			loDrowPrizes = null;
		}
		// prizeNumPieces = null;
		animPrizeNumPieces = null;

	}
}
