package com.mykj.andr.logingift;

import java.util.ArrayList;
import java.util.Timer;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mykj.andr.model.HallDataManager;
import com.mykj.andr.model.LotteryConfigInfo;
import com.mykj.andr.model.LotteryDrowWinner;
import com.mykj.andr.provider.LotteryDrowProvider;
import com.mykj.andr.ui.CustomActivity;
import com.mykj.andr.ui.widget.TimerTaskForListViewRolling;
import com.mykj.game.FiexedViewHelper;
import com.MyGame.Migu.R;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.Log;
import com.mykj.game.utils.UtilHelper;

/**
 * 抽奖机主界面
 * 
 * @author JiangYinZhi
 * 
 */
public class LotteryDrowMainView extends RelativeLayout {

	private static final String lotteryMainViewTag = "LotteryMainView";

	// ------------------------------抽奖机成员变量--------------------------------------
	private Context mContext;
	private LotteryConfigInfo loConfigInfo;// 抽奖机的配置信息
	public int loHandlerWhat;// 抽奖机的handler处于什么阶段
	public static int fontSize1;// hint1的字体
	public static int fontSize2;// hint2的字体
	public static int fontSize3;// hint3的字体
	public static int fontSize4;// 图片下方奖品的名称的字体
	public static int fontSize5;// 奖品名称字体大小
	private ArrayList<LotteryDrowWinner> loDrowWinners;
	private ArrayList<Bitmap> bitmapList;

	// ----------------------------------抽奖机图片资源----------------------------------
	private Bitmap BGImg;// 背景图片
	private Bitmap backBtnNormalImg;// 返回按钮正常图片
	private Bitmap backBtnPressImg;// 返回按钮按下效果图片
	private Bitmap multiDrowBtnNormalImg;// 一键抽奖按钮正常图片
	private Bitmap multiDrowBtnPressImg;// 一键抽奖按钮按下图片
	private Bitmap onceDrowBtnNormalImg;// 立刻抽奖按钮正常图片
	private Bitmap onceDrowBtnPressImg;// 立刻抽奖按钮按下图片
	private Bitmap cheatBtnNormalImg;// 秘籍按钮正常图片
	private Bitmap cheatBtnPressImg;// 秘籍按钮按下图片
	private Bitmap addLedouBtnNormalImg;// 增加乐豆按钮正常图片
	private Bitmap addLedouBtnPressImg;// 增加乐豆按钮按下图片
	private Bitmap neon1Img;// 霓虹灯1的图片
	private Bitmap ledouImg;// 乐豆图片

	// --------------------------------抽奖机界面控件-------------------------------------
	private Button backBtn;// 返回按钮
	private Button multiDrowBtn;// 一键抽奖按钮
	private Button onceDrowBtn;// 立刻抽奖按钮
	private Button cheatBtn;// 秘籍按钮
	private Button addLedouBtn;// 增加乐豆按钮
	private TextView hint1TV; // 提示1
	private TextView hint2TV;// 提示2
	// private TextView hint3TV;// 提示3,提示乐豆抽奖需要消耗的乐豆数
	private ImageView ledouIV;// 提示乐豆的图片
	private LotteryDrowView loDrowView;// 抽奖视图
	private ListView winnerListLV;// 获奖名单listview
	private TimerTaskForListViewRolling winnerListTimerTask;

	// --------------------------------抽奖机各阶段标识-----------------------------------------
	/** 抽奖机正在下载资源 */
	public final static int LOTTERY_DROW_DOWNLOAD_RESOURCE = 1000;
	/** 抽奖机初始状态 */
	public final static int LOTTERY_DROW_PREVIOUS = 1001;
	/** 开始按下按钮，开始抽奖 */
	public static final int LOTTERY_ONCE_DROW_START = 1002;
	/** 抽奖进行中 */
	public final static int LOTTERY_ONCE_DROWING = 1003;
	/** 抽奖结束 */
	public final static int LOTTERY_ONCE_DROW_END = 1004;
	/** 抽奖成功 */
	public final static int LOTTERY_ONCE_DROW_SUCCESS = 1005;
	/** 抽奖完成 */
	public final static int LOTTERY_ONCE_DROW_FINISH = 1006;
	/** 抽奖超时 */
	public final static int LOTTERY_ONCE_DROW_TIMEOUT = 1007;
	/** 抽奖次数不够 */
	public final static int LOTTERY_NO_TIME = 1008;
	/** 抽奖失败 */
	public final static int LOTTERY_DROW_FAIL = 1009;
	/** 开始一键抽奖 */
	public final static int LOTTERY_MULTI_DROW_START = 1010;
	/** 一键抽奖显示奖品数量动画阶段 */
	public final static int LOTTERY_MULTI_DROW_SHOW_PRIZES_NUM = 1011;
	/** 一键抽奖显示窗帘动画阶段 */
	public final static int LOTTERY_MULTI_DROW_SHOW_CURTAIN_ANIM = 1012;
	/** 一键抽奖展示奖品动画 */
	public final static int LOTTERY_MULTI_DROW_SHOW_PRIZES_FINISH = 1013;
	/** 一键抽奖完成 */
	public final static int LOTTERY_MULTI_DROW_FINISH = 1014;
	/** 一键抽奖超时 */
	public final static int LOTTERY_MULTI_DROW_TIMEOUT = 1015;

	public LotteryDrowMainView(Context context) {
		super(context);
		this.mContext = context;
		init();
		scaleImg();
		setMainViewParm();
		setBackBtnParm();
		setWinnerListViewParm();
		setMultiDrowBtnParm();
		setOnceDrowBtnParm();
		setCheatBtnParm();
		setAddLedouBtnParm();
		setHintTVParm();
		setLotteryDrowView();
	}

	/**
	 * 添加抽奖配置信息
	 * 
	 * @param loConfigInfo
	 *            抽奖配置信息
	 */
	public void setLotteryConfigInfo(LotteryConfigInfo loConfigInfo) {
		this.loConfigInfo = loConfigInfo;
	}

	// 初始化相关数据
	private void init() {
		setLedouImg();
		setFontSize();
		loDrowWinners = new ArrayList<LotteryDrowWinner>();
		bitmapList = ((LotteryDrowActivity) mContext).getBitmapList();
	}

	// 设置字体
	private void setFontSize() {
		Activity mActivity = null;
		if (mContext != null) {
			mActivity = (Activity) mContext;
		} else {
			return;
		}
		DisplayMetrics metric = new DisplayMetrics();
		mActivity.getWindowManager().getDefaultDisplay().getMetrics(metric);
		int mDensity = metric.densityDpi;
		Log.i(lotteryMainViewTag, "density:  " + mDensity);
		if (mDensity == DisplayMetrics.DENSITY_LOW) {
			fontSize1 = 10;
			fontSize2 = 8;
			fontSize3 = 8;
			fontSize4 = 8;
			fontSize5 = 9;
		} else if (mDensity == DisplayMetrics.DENSITY_MEDIUM) {
			fontSize1 = 13;
			fontSize2 = 9;
			fontSize3 = 9;
			fontSize4 = 9;
			fontSize5 = 11;
		} else if (mDensity == DisplayMetrics.DENSITY_HIGH) {
			fontSize1 = 15;
			fontSize2 = 11;
			fontSize3 = 10;
			fontSize4 = 10;
			fontSize5 = 12;
		} else if (mDensity == DisplayMetrics.DENSITY_XHIGH) {
			fontSize1 = 17;
			fontSize2 = 12;
			fontSize3 = 11;
			fontSize4 = 14;
			fontSize5 = 15;
		} else if (mDensity == 480) {
			fontSize1 = 17;
			fontSize2 = 12;
			fontSize3 = 11;
			fontSize4 = 14;
			fontSize5 = 15;
		} else {
			fontSize1 = 18;
			fontSize2 = 13;
			fontSize3 = 12;
			fontSize4 = 15;
			fontSize5 = 16;
		}
	}

	// 将图片缩放成适配的图片
	private void scaleImg() {
		if (BGImg == null) {
			BGImg = LotteryDrowHelper.sacleBitmapFromAssert(mContext,
					"lo/lo_bg.png", true, bitmapList);
		}
		if (backBtnNormalImg == null) {
			backBtnNormalImg = LotteryDrowHelper.sacleBitmapFromAssert(
					mContext, "lo/lo_back_normal.png", true, bitmapList);
		}
		if (backBtnPressImg == null) {
			backBtnPressImg = LotteryDrowHelper.sacleBitmapFromAssert(mContext,
					"lo/lo_back_select.png", true, bitmapList);
		}
		if (multiDrowBtnNormalImg == null) {
			multiDrowBtnNormalImg = LotteryDrowHelper.sacleBitmapFromAssert(
					mContext, "lo/lo_multi_drow_btn_normal.png", true,
					bitmapList);
		}
		if (multiDrowBtnPressImg == null) {
			multiDrowBtnPressImg = LotteryDrowHelper.sacleBitmapFromAssert(
					mContext, "lo/lo_multi_drow_btn_select.png", true,
					bitmapList);
		}
		if (onceDrowBtnNormalImg == null) {
			onceDrowBtnNormalImg = LotteryDrowHelper.sacleBitmapFromAssert(
					mContext, "lo/lo_once_drow_btn_normal.png", true,
					bitmapList);
		}
		if (onceDrowBtnPressImg == null) {
			onceDrowBtnPressImg = LotteryDrowHelper.sacleBitmapFromAssert(
					mContext, "lo/lo_once_drow_btn_select.png", true,
					bitmapList);
		}
		if (cheatBtnNormalImg == null) {
			cheatBtnNormalImg = LotteryDrowHelper.sacleBitmapFromAssert(
					mContext, "lo/lo_cheat_btn_normal.png", true, bitmapList);
		}
		if (cheatBtnPressImg == null) {
			cheatBtnPressImg = LotteryDrowHelper.sacleBitmapFromAssert(
					mContext, "lo/lo_cheat_btn_select.png", true, bitmapList);
		}
		if (addLedouBtnNormalImg == null) {
			addLedouBtnNormalImg = LotteryDrowHelper.sacleBitmapFromAssert(
					mContext, "lo/lo_add_ledou_normal.png", true, bitmapList);
		}
		if (addLedouBtnPressImg == null) {
			addLedouBtnPressImg = LotteryDrowHelper.sacleBitmapFromAssert(
					mContext, "lo/lo_add_ledou_select.png", true, bitmapList);
		}
		if (neon1Img == null) {
			neon1Img = LotteryDrowHelper.sacleBitmapFromAssert(mContext,
					"lo/lo_neon1.png", true, bitmapList);
		}

	}

	// 设置主界面参数
	@SuppressWarnings("deprecation")
	private void setMainViewParm() {
		BitmapDrawable BGDrawable = new BitmapDrawable(BGImg);
		this.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		this.setBackgroundDrawable(BGDrawable);
	}

	// 设置返回按钮的参数
	private void setBackBtnParm() {
		backBtn = new Button(mContext);
		LotteryDrowHelper.setBtnParam(mContext, this, backBtn,
				backBtnNormalImg, backBtnPressImg,
				(int) LotteryDrowHelper.getDimension(
						R.dimen.lo_back_btn_leftMargin, mContext),
				(int) LotteryDrowHelper.getDimension(
						R.dimen.lo_back_btn_topMargin, mContext));
		backBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final LotteryDrowActivity mActivity = (LotteryDrowActivity) mContext;
				if ((loDrowView.getOnceDrowIsRunning() || loDrowView
						.getMultiDrowIsRunning())
						&& loHandlerWhat != LOTTERY_MULTI_DROW_SHOW_PRIZES_FINISH) {
					UtilHelper.showCustomDialog(mContext, "正在抽奖中...");
					return;
				}
				// if (loDrowView.getOnceDrowIsRunning()
				// || loDrowView.getMultiDrowIsRunning()) {
				// UtilHelper.showCustomDialog(mContext,
				// "正在进行抽奖,是否退回大厅,如若退回大厅,本次抽奖奖品将稍后进入您的背包.",
				// new OnClickListener() {
				//
				// @Override
				// public void onClick(View v) {
				// // TODO Auto-generated method stub
				// loDrowView.stopMultiDrowAnim();
				// loDrowView.stopOnceDrowAnim();
				// if (mActivity != null) {
				// mActivity.finish();
				// }
				// }
				// }, true);
				// } else {
				loDrowView.stopMultiDrowAnim();
				loDrowView.stopOnceDrowAnim();
				// 开启线程进行回收操作
				mActivity.finish();
			}
		});

	}

	// 设置一键抽奖按钮的参数
	private void setMultiDrowBtnParm() {
		if (multiDrowBtn != null) {
			this.removeView(multiDrowBtn);
		}
		multiDrowBtn = new Button(mContext);
		LotteryDrowHelper.setBtnParam(mContext, this, multiDrowBtn,
				multiDrowBtnNormalImg, multiDrowBtnPressImg,
				(int) LotteryDrowHelper.getDimension(
						R.dimen.lo_multi_drow_btn_leftMargin, mContext),
				(int) LotteryDrowHelper.getDimension(
						R.dimen.lo_multi_drow_btn_topMargin, mContext));
		multiDrowBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (loDrowView != null
						&& loHandlerWhat == LOTTERY_MULTI_DROW_SHOW_PRIZES_FINISH) {
					loDrowView.stopMultiDrowAnim();
					loDrowView.stopOnceDrowAnim();
					loDrowView.freshUI();
				}
				if (loDrowView != null && !loDrowView.getOnceDrowIsRunning()
						&& !loDrowView.getMultiDrowIsRunning()
						&& loHandlerWhat != LOTTERY_DROW_DOWNLOAD_RESOURCE) {
					if (loConfigInfo.leftLoNo == -1) {
						// UtilHelper.showCustomDialog(mContext,
						// "用户数据错误，请稍候重试~");
						// LotteryDrowProvider.getInstance(mContext).requestLotteryNum();
						return;
					}
					if (loConfigInfo.leftLoNo > 0
							&& loConfigInfo.leftLoNo < loConfigInfo.multiLoTimes) {
						if (loConfigInfo.multiBCostPower == 1) {
							int needLedouTimes = loConfigInfo.multiLoTimes
									- loConfigInfo.leftLoNo;// 还需要多少次乐豆抽奖
							UtilHelper.showCustomDialog(mContext, "是否花费"
									+ loConfigInfo.multiBCost * needLedouTimes
									+ "乐豆抽奖？", new OnClickListener() {

								@Override
								public void onClick(View v) {
									mLotteryHandler
											.sendEmptyMessage(LOTTERY_MULTI_DROW_START);
								}
							}, true);
						} else if (loConfigInfo.multiBCostPower == 0) {
							UtilHelper.showCustomDialog(mContext, "您的免费抽奖次数不足"
									+ loConfigInfo.multiLoTimes + "次，可进行单次抽奖！");
						}
						return;

					}
					if (loConfigInfo.leftLoNo == 0
							&& loConfigInfo.multiBCostPower == 1) {
						UtilHelper.showCustomDialog(mContext,
								"您的免费抽奖次数已经用完,是否花费" + loConfigInfo.multiBCost
										* loConfigInfo.multiLoTimes + "乐豆抽奖"
										+ loConfigInfo.multiLoTimes + "次?",
								new OnClickListener() {

									@Override
									public void onClick(View v) {
										mLotteryHandler
												.sendEmptyMessage(LOTTERY_MULTI_DROW_START);
									}
								}, true);
						return;
					} else if (loConfigInfo.leftLoNo == 0
							&& loConfigInfo.multiBCostPower == 0) {
						UtilHelper.showCustomDialog(mContext,
								"您的免费抽奖次数用完啦,请明天再来~");
						return;
					} else {
						mLotteryHandler
								.sendEmptyMessage(LOTTERY_MULTI_DROW_START);
					}
				}
			}
		});
	}

	// 设置立刻抽奖按钮的参数
	private void setOnceDrowBtnParm() {
		onceDrowBtn = new Button(mContext);
		LotteryDrowHelper.setBtnParam(mContext, this, onceDrowBtn,
				onceDrowBtnNormalImg, onceDrowBtnPressImg,
				(int) LotteryDrowHelper.getDimension(
						R.dimen.lo_once_drow_btn_leftMargin, mContext),
				(int) LotteryDrowHelper.getDimension(
						R.dimen.lo_once_drow_btn_topMargin, mContext));
		onceDrowBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (loDrowView != null
						&& loHandlerWhat == LOTTERY_MULTI_DROW_SHOW_PRIZES_FINISH) {
					loDrowView.stopMultiDrowAnim();
					loDrowView.stopOnceDrowAnim();
					loDrowView.freshUI();
				}
				if (loDrowView != null && !loDrowView.getOnceDrowIsRunning()
						&& !loDrowView.getMultiDrowIsRunning()
						&& loHandlerWhat != LOTTERY_DROW_DOWNLOAD_RESOURCE) {
					if (loConfigInfo.leftLoNo == -1) {
						// UtilHelper.showCustomDialog(mContext,
						// "免费抽奖次数获取失败，请稍候重试~");
						// LotteryDrowProvider.getInstance(mContext).requestLotteryNum();
						return;
					}
					if (loConfigInfo.leftLoNo == 0
							&& loConfigInfo.multiBCostPower == 1) {
						UtilHelper.showCustomDialog(mContext,
								"您的免费抽奖次数已经用完,是否使用" + loConfigInfo.multiBCost
										+ "乐豆抽奖?", new OnClickListener() {

									@Override
									public void onClick(View v) {
										LotteryDrowProvider.getInstance(
												mContext).requestLotteryResult(
												(byte) 0);
										mLotteryHandler
												.sendEmptyMessage(LOTTERY_ONCE_DROW_START);
									}
								}, true);
						return;
					} else if (loConfigInfo.leftLoNo == 0
							&& loConfigInfo.multiBCostPower == 0) {
						UtilHelper.showCustomDialog(mContext,
								"您的免费抽奖次数用完啦,请明天再来~");
						return;
					}
					// if (loConfigInfo.logonLotteryNo == 0) {
					// LotteryDrowProvider.getInstance(mContext)
					// .requestLotteryResult((byte) 0);
					// }
					else {
						LotteryDrowProvider.getInstance(mContext)
								.requestLotteryResult((byte) 1);
						mLotteryHandler
								.sendEmptyMessage(LOTTERY_ONCE_DROW_START);
					}
				}
			}
		});
	}

	// 设置秘籍按钮的参数
	private void setCheatBtnParm() {
		cheatBtn = new Button(mContext);
		LotteryDrowHelper.setBtnParam(mContext, this, cheatBtn,
				cheatBtnNormalImg, cheatBtnPressImg,
				(int) LotteryDrowHelper.getDimension(
						R.dimen.lo_cheat_btn_leftMargin, mContext),
				(int) LotteryDrowHelper.getDimension(
						R.dimen.lo_cheat_btn_topMargin, mContext));
		cheatBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (loDrowView.getOnceDrowIsRunning()
						|| loDrowView.getMultiDrowIsRunning()) {
					return;
				}
				if (loConfigInfo != null && loConfigInfo.multiHurl != null) {
					UtilHelper.onWeb(mContext, loConfigInfo.multiHurl);
				}
			}
		});
	}

	// 设置增加乐豆按钮的参数
	private void setAddLedouBtnParm() {
		addLedouBtn = new Button(mContext);
		LotteryDrowHelper.setBtnParam(mContext, this, addLedouBtn,
				addLedouBtnNormalImg, addLedouBtnPressImg,
				(int) LotteryDrowHelper.getDimension(
						R.dimen.lo_add_ledou_btn_leftMargin, mContext),
				(int) LotteryDrowHelper.getDimension(
						R.dimen.lo_add_ledou_btn_topMargin, mContext));
		addLedouBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 快捷购买
				UtilHelper.showBuyDialog(mContext, AppConfig.propId, false,
						AppConfig.isConfirmon,AppConfig.ACTION_LOTTERY); //1代表  抽奖【充】

			}
		});
	}

	// 设置提示textview的参数
	private void setHintTVParm() {
		if (mContext == null) {
			return;
		}
		// 设置提示1
		hint1TV = new TextView(mContext);
		hint1TV.setTextColor(mContext.getResources().getColor(R.color.white));
		hint1TV.setTextSize(fontSize1);
		hint1TV.setText("免费抽奖次数剩余：");
		LotteryDrowHelper.setViewParam(hint1TV, this, (int) LotteryDrowHelper
				.getDimension(R.dimen.lo_hint1_tv_leftMargin, mContext),
				(int) LotteryDrowHelper.getDimension(
						R.dimen.lo_hint1_tv_topMargin, mContext),
				(int) LotteryDrowHelper.getDimension(R.dimen.lo_hint1_tv_width,
						mContext), (int) LotteryDrowHelper.getDimension(
						R.dimen.lo_hint1_tv_height, mContext));
		// 设置提示2
		hint2TV = new TextView(mContext);
		hint2TV.setTextColor(mContext.getResources().getColor(
				R.color.hint2_color));
		hint2TV.setTextSize(fontSize2);
		hint2TV.setText("每日登陆可获得乐豆和抽奖机会!乐豆也能抽奖哦");
		LotteryDrowHelper.setViewParam(hint2TV, this, (int) LotteryDrowHelper
				.getDimension(R.dimen.lo_hint2_tv_leftMargin, mContext),
				(int) LotteryDrowHelper.getDimension(
						R.dimen.lo_hint2_tv_topMargin, mContext),
				(int) LotteryDrowHelper.getDimension(R.dimen.lo_hint2_tv_width,
						mContext), (int) LotteryDrowHelper.getDimension(
						R.dimen.lo_hint2_tv_height, mContext));
		// // 设置提示3
		// hint3TV = new TextView(mContext);
		// hint3TV.setTextColor(mContext.getResources().getColor(
		// R.color.hint3_color));
		// hint3TV.setTextSize(fontSize3);
		// hint3TV.setText("抽奖每回合消耗    乐豆");
		// LotteryDrowHelper.setViewParam(hint3TV, this, (int) LotteryDrowHelper
		// .getDimension(R.dimen.lo_hint3_tv_leftMargin, mContext),
		// (int) LotteryDrowHelper.getDimension(
		// R.dimen.lo_hint3_tv_topMargin, mContext),
		// (int) LotteryDrowHelper.getDimension(R.dimen.lo_hint3_tv_width,
		// mContext), (int) LotteryDrowHelper.getDimension(
		// R.dimen.lo_hint3_tv_height, mContext));
	}

	private void setWinnerListViewParm() {
		winnerListLV = new ListView(mContext);
		LotteryDrowHelper.setViewParam(winnerListLV, this,
				(int) LotteryDrowHelper.getDimension(
						R.dimen.lo_winner_list_leftMargin, mContext),
				(int) LotteryDrowHelper.getDimension(
						R.dimen.lo_winner_list_topMargin, mContext),
				(int) LotteryDrowHelper.getDimension(
						R.dimen.lo_winner_list_width, mContext),
				(int) LotteryDrowHelper.getDimension(
						R.dimen.lo_winner_list_height, mContext));
		// winnerListLV.setBackgroundColor(Color.BLACK);

	}

	public void setLedouImg() {
		FiexedViewHelper.getInstance().requestUserBean(lLotteryHandler);

	}

	// 设置抽奖机动画的view
	private void setLotteryDrowView() {
		loDrowView = new LotteryDrowView(mContext);
		// LotteryDrowHelper.setViewParam(loDrowView, this,
		// (int) LotteryDrowHelper.getDimension(
		// R.dimen.lo_drow_view_leftMargin, mContext),
		// (int) LotteryDrowHelper.getDimension(
		// R.dimen.lo_drow_view_topMargin, mContext), neon1Img
		// .getWidth(), neon1Img.getHeight());
		RelativeLayout.LayoutParams layoutParams = new LayoutParams(
				neon1Img.getWidth(), neon1Img.getHeight());
		layoutParams
				.setMargins(
						(int) (LotteryDrowHelper.getDimension(
								R.dimen.lo_drow_view_leftMargin, mContext) * LotteryDrowActivity.widthScale),
						(int) (LotteryDrowHelper.getDimension(
								R.dimen.lo_drow_view_topMargin, mContext) * LotteryDrowActivity.heightScale),
						0, 0);
		this.addView(loDrowView, layoutParams);
	}

	/**
	 * 获取抽奖机动画的view
	 */
	public LotteryDrowView getLotteryDrowView() {
		return loDrowView;
	}

	/**
	 * 抽奖界面的handler，处理相关动画的更新
	 */
	public Handler mLotteryHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			loHandlerWhat = msg.what;
			switch (msg.what) {
			case LOTTERY_DROW_DOWNLOAD_RESOURCE:
				Log.d(lotteryMainViewTag, "正在下载资源");
				hint2TV.setText("抽奖机正在下载资源，请稍后...");
				break;
			case LOTTERY_DROW_PREVIOUS:
				Log.d(lotteryMainViewTag, "抽奖机初始化阶段");
				hint2TV.setText("每日登陆可获得乐豆和抽奖机会!乐豆也能抽奖哦");
				break;
			case LOTTERY_ONCE_DROW_START:
				Log.d(lotteryMainViewTag, "开始抽奖");
				if (loDrowView != null) {
					loDrowView.startOnceDrowAnim();
				}
				break;
			case LOTTERY_ONCE_DROWING:
				Log.d(lotteryMainViewTag, "正在抽奖中");
				break;
			case LOTTERY_ONCE_DROW_END:
				Log.d(lotteryMainViewTag, "抽奖结束");
				break;
			case LOTTERY_ONCE_DROW_SUCCESS:
				Log.d(lotteryMainViewTag, "抽奖成功");
				break;
			case LOTTERY_ONCE_DROW_FINISH:
				String resultHint = (String) msg.obj;
				byte type = (byte) msg.arg1;
				if (type == 2) {
					setLedouImg();
				}
				UtilHelper.showCustomDialog(mContext, resultHint, null, false)
						.setOnDismissListener(new OnDismissListener() {

							@Override
							public void onDismiss(DialogInterface arg0) {
								loDrowView.stopOnceDrowAnim();
								loDrowView.freshUI();
							}
						});

				Log.d(lotteryMainViewTag, "抽奖完成");
				break;
			case LOTTERY_ONCE_DROW_TIMEOUT:
				UtilHelper.showCustomDialog(mContext, "网络超时,请重试..");
				loDrowView.stopOnceDrowAnim();
				Log.d(lotteryMainViewTag, "抽奖超时");
				break;
			case LOTTERY_NO_TIME:
				Log.d(lotteryMainViewTag, "没有抽奖次数");
				break;
			case LOTTERY_DROW_FAIL:
				Log.d(lotteryMainViewTag, "抽奖失败");
				break;
			case LOTTERY_MULTI_DROW_START:
				loDrowView.startMultiDrowAnim();
				LotteryDrowProvider.getInstance(mContext)
						.requestMultiLotteryResult();
				break;

			case LOTTERY_MULTI_DROW_SHOW_CURTAIN_ANIM:
				loDrowView
						.setMultiAnimStep(LotteryDrowView.MULTI_ANIM_SHOW_CURTAIN);
				break;
			case LOTTERY_MULTI_DROW_TIMEOUT:
				loDrowView.stopMultiDrowAnim();
				UtilHelper.showCustomDialog(mContext, "网络超时,请重试..");
				break;
			case LOTTERY_MULTI_DROW_SHOW_PRIZES_FINISH:
				setLedouImg();
				// loDrowView.stopMultiDrowAnim();
				break;
			case LOTTERY_MULTI_DROW_FINISH:
				break;
			default:
				break;
			}
		};
	};

	// private int startLedouNo = -1;// 记录刷新乐豆前的数据
	/*** 刷新乐豆界面handler **/
	public static final int HANDLER_REFRESH_BEAN = 2333;

	public Handler lLotteryHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CustomActivity.HANDLER_USER_BEAN:
				// 先判断状态码，状态码为0表示成功，否则重试
				String statusStr = UtilHelper.parseStatusXml(
						String.valueOf(msg.obj), "status");
				int status = UtilHelper.stringToInt(statusStr, -1); // 默认为-1
				String beanStr = UtilHelper.parseStatusXml(
						String.valueOf(msg.obj), "bean");
				int bean = UtilHelper.stringToInt(beanStr, -1); // 默认为-1
				String ticketStr = UtilHelper.parseStatusXml(
						String.valueOf(msg.obj), "mobilevoucher");

				if (status == 0 && bean >= 0) {// 成功
					// // 设置乐豆
					// if(startLedouNo == -1){
					// startLedouNo = bean;
					// }
					HallDataManager.getInstance().getUserMe().setBean(bean);
					Message msg1 = lLotteryHandler.obtainMessage();
					msg1.what = LotteryDrowMainView.HANDLER_REFRESH_BEAN;
					msg1.obj = bean;
					lLotteryHandler.sendMessage(msg1);
					// LotteryDrowHelper.ledouAnim(startLedouNo, bean,
					// lLotteryHandler);
					// startLedouNo = bean;

				}
				break;
			case HANDLER_REFRESH_BEAN:
				int ledouNo = Integer.parseInt(msg.obj.toString());
				if (ledouIV != null) {
					removeView(ledouIV);
				}
				ledouIV = new ImageView(mContext);
				ledouImg = LotteryDrowHelper.getLedouBitmapFromNum(mContext,
						ledouNo, LotteryDrowHelper.splitBitmap(
								LotteryDrowHelper.getBitmapFromAssert(mContext,
										"lo/lo_ledou_num.png"), 10, 1));
				ledouIV.setImageBitmap(ledouImg);
				int ledouLeftMargin = (int) (LotteryDrowHelper.getDimension(
						R.dimen.lo_add_ledou_btn_leftMargin, mContext) - ledouImg
						.getWidth());
				LotteryDrowHelper
						.setViewParam(ledouIV, LotteryDrowMainView.this,
								ledouLeftMargin,
								(int) LotteryDrowHelper.getDimension(
										R.dimen.lo_drow_view_ledou_topMargin,
										mContext), ledouImg.getWidth(),
								ledouImg.getHeight());
				break;
			default:
				break;
			}
			;

		};
	};

	/**
	 * 获取一键抽奖次数
	 * 
	 * @param multiLoTimes一键抽奖次数
	 *            ，获取抽奖配置下发的
	 */
	public void setMultiLoTimes(int multiLoTimes) {
		if (multiLoTimes < 0) {
			return;
		}
		Bitmap loMultiDrowNormalBtnBMP = LotteryDrowHelper
				.sacleBitmapFromAssert(mContext,
						"lo/lo_multi_drow_btn_normal.png", true, bitmapList);
		Bitmap loMultiDrowSelectBtnBMP = LotteryDrowHelper
				.sacleBitmapFromAssert(mContext,
						"lo/lo_multi_drow_btn_select.png", true, bitmapList);
		Bitmap multiTimesNormalHintBMP = loMultiDrowNormalBtnBMP.copy(
				Bitmap.Config.ARGB_4444, true);
		Bitmap multiTimesSelectHintBMP = loMultiDrowSelectBtnBMP.copy(
				Bitmap.Config.ARGB_4444, true);
		// 组装一键抽奖次数的按钮图片
		Bitmap multiDrowNormalBMP = LotteryDrowHelper.buildMultiDrowBtnBitmap(
				mContext, multiLoTimes, multiTimesNormalHintBMP);
		Bitmap multiDrowSelectBMP = LotteryDrowHelper.buildMultiDrowBtnBitmap(
				mContext, multiLoTimes, multiTimesSelectHintBMP);
		multiDrowBtnNormalImg = Bitmap.createBitmap(multiDrowNormalBMP);
		multiDrowBtnPressImg = Bitmap.createBitmap(multiDrowSelectBMP);
		setMultiDrowBtnParm();
	}

	/**
	 * 设置文字1
	 * 
	 * @param hint1Str
	 */
	public void setHint1TV(String hint1Str) {
		hint1TV.setText(hint1Str);
	}

	/**
	 * 设置文字2
	 * 
	 * @param hint2Str
	 */
	public void setHint2TV(String hint2Str) {
		hint2TV.setText(hint2Str);
	}

	/**
	 * 设置文字3
	 * 
	 * @param hint2Str
	 */
	// public void setHint3TV(String hint3Str) {
	// hint3TV.setText(hint3Str);
	// }

	// private class WinnerListAdapter extends BaseAdapter {
	//
	// @Override
	// public int getCount() {
	// return loDrowWinners.size();
	// }
	//
	// @Override
	// public Object getItem(int arg0) {
	// return loDrowWinners.get(arg0);
	// }
	//
	// @Override
	// public long getItemId(int arg0) {
	// return arg0;
	// }
	//
	// @Override
	// public View getView(int arg0, View arg1, ViewGroup arg2) {
	// ViewHolder holder;
	// if (arg1 == null) {
	// LinearLayout layout = new LinearLayout(mContext);
	// layout.setOrientation(LinearLayout.HORIZONTAL);
	// TextView nameTV = new TextView(mContext);
	// LotteryDrowHelper.setViewParam(nameTV, layout,
	// (int) LotteryDrowHelper.getDimension(
	// R.dimen.lo_drow_view_winner_name_leftMargin,
	// mContext),
	// (int) LotteryDrowHelper.getDimension(
	// R.dimen.lo_drow_view_winner_name_topMargin,
	// mContext));
	// nameTV.setTextColor(mContext.getResources().getColor(
	// R.color.winner_list_name_color));
	// nameTV.setTextSize(fontSize3);
	// TextView proDescTV = new TextView(mContext);
	// LotteryDrowHelper.setViewParam(proDescTV, layout,
	// (int) LotteryDrowHelper.getDimension(
	// R.dimen.lo_drow_view_winner_prodesc_leftMargin,
	// mContext),
	// (int) LotteryDrowHelper.getDimension(
	// R.dimen.lo_drow_view_winner_prodesc_topMargin,
	// mContext));
	// proDescTV.setTextColor(mContext.getResources().getColor(
	// R.color.white));
	// proDescTV.setTextSize(fontSize3);
	// // layout.addView(nameTV);
	// // layout.addView(proDescTV);
	// arg1 = layout;
	//
	// holder = new ViewHolder();
	// holder.winnerName = nameTV;
	// holder.proDesc = proDescTV;
	// arg1.setTag(holder);
	// } else {
	// holder = (ViewHolder) arg1.getTag();
	// }
	// holder.winnerName.setText(loDrowWinners.get(arg0).getName());
	// holder.proDesc.setText(loDrowWinners.get(arg0).getProDes());
	// return arg1;
	// }
	// }
	//
	// // 获奖名单listview没一行的view
	// private class ViewHolder {
	// TextView winnerName;// 获奖名称
	// TextView proDesc;// 商品描述
	// }

	public void setLotteryDrowWinners(ArrayList<LotteryDrowWinner> loDrowWinners) {
		if (loDrowWinners != null) {
			for (int i = 0; i < loDrowWinners.size(); i++) {
				LotteryDrowWinner loDrowWinner = loDrowWinners.get(i);
				this.loDrowWinners.add(loDrowWinner);
			}
			winnerListTimerTask = new TimerTaskForListViewRolling(winnerListLV,
					mContext, loDrowWinners);
			new Timer().schedule(winnerListTimerTask, 100, 100);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// 按下操作，如果是处于一键抽奖展示动画界面，将还原
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (loHandlerWhat == LOTTERY_MULTI_DROW_SHOW_PRIZES_FINISH) {
				loDrowView.stopMultiDrowAnim();
				loDrowView.stopOnceDrowAnim();
				loDrowView.freshUI();
			}
		}

		return super.onTouchEvent(event);
	}

}
