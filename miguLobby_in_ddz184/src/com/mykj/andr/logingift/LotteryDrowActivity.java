package com.mykj.andr.logingift;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.mingyou.login.RecoverForDisconnect;
import com.mingyou.login.SocketLoginListener;
import com.mykj.andr.model.LotteryConfigInfo;
import com.mykj.andr.model.LotteryDrowPrize;
import com.mykj.andr.model.LotteryDrowWinner;
import com.mykj.andr.provider.LotteryDrowProvider;
import com.MyGame.Midlet.R;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.Log;
import com.mykj.game.utils.Util;
import com.mykj.game.utils.UtilHelper;

/**
 * 抽奖机activity
 * 
 * @author JiangYinZhi
 * 
 */
public class LotteryDrowActivity extends Activity {
	private static final String TAG = "LotteryDrowActivity";

	public static float widthScale = 0;// 宽度缩放值
	public static float heightScale = 0;// 高度缩放值

	private Context mContext;
	private LotteryDrowMainView lotteryMainView;
	private ArrayList<Bitmap> bitmapList = null;// 所有bitmap列表，用于释放所有bitmap
	private ArrayList<LotteryDrowPrize> tempLoDrowPrizes = null;

	/** 返回抽奖次数成功 */
	public static final int HANDLER_LOTTERY_NUM_SUCCESS = 302;
	/** 返回抽奖次数失败 */
	public static final int HANDLER_LOTTERY_NUM_FAIL = 303;
	/** 返回获取奖品名单 */
	public static final int HANDLER_WINNER_LIST_RESULT_SUCCESS = 308;
	/** 表示图片全部下载完成 */
	public static final int BITMAPS_DOWNLOAD_FINISH = 1;;
	public static final int HANDLER_LOTTERY_RESULT_SUCCESS = 301;
	/** 请求一键抽奖配置信息成功 */
	public static final int HANDLER_MULTI_LOTTERY_CONFIG_REQ = 304;
	/** 显示图片 */
	public static final int HANDLER_MULTI_LOTTERY_DL_SUCCESS = 309;
	/** 下载图片失败 */
	public static final int HANDLER_MULTI_LOTTERY_DL_FAIL = 310;
	/** 返回一键抽奖配置信息成功 */
	public static final int HANDLER_MULTI_LOTTERY_CONFIG_SUCCESS = 305;
	/** 返回一键抽奖结果信息成功 */
	public static final int HANDLER_MULTI_LOTTERY_RESULT_SUCCESS = 307;
	/** 抽奖机异常处理 */
	public static final int HANDLER_LOTTERY_ERROR = 312;
	/** 返回一键抽奖配置信息失败 */
	public static final int HANDLER_MULTI_LOTTERY_CONFIG_FAIL = 306;

	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		tempLoDrowPrizes = new ArrayList<LotteryDrowPrize>();
		init();
		lotteryMainView = new LotteryDrowMainView(this);
		setContentView(lotteryMainView);
		LotteryDrowProvider.getInstance(mContext).requestLotteryNum();
		LotteryDrowProvider.getInstance(mContext).requestWinnerList();
	}

	@SuppressLint("HandlerLeak")
	public Handler mLotteryUIHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			final LotteryConfigInfo lotteryConfig = LotteryConfigInfo
					.getInstance(mContext);
			switch (msg.what) {
			case HANDLER_LOTTERY_NUM_SUCCESS:
				int lotteryNo = LotteryConfigInfo.getInstance(mContext)
						.getLotteryTimes();
				lotteryMainView.setHint1TV("免费抽奖次数剩余:" + lotteryNo);
				break;
			case HANDLER_LOTTERY_NUM_FAIL:
				Toast.makeText(mContext,
						mContext.getString(R.string.lucky_is_failed),
						Toast.LENGTH_SHORT).show();
				break;
			case HANDLER_WINNER_LIST_RESULT_SUCCESS:
				String winnerListXml = LotteryConfigInfo.getInstance(mContext)
						.getWinnerListXml();
				ArrayList<LotteryDrowWinner> loDrowWinners = new ArrayList<LotteryDrowWinner>();
				boolean isParseWinnerListXml = LotteryDrowHelper
						.parseLotteryWinnersXML(loDrowWinners, winnerListXml);
				if (isParseWinnerListXml) {
					lotteryMainView.setLotteryDrowWinners(loDrowWinners);
				}
				break;
			// 获取抽奖结果成功
			case HANDLER_LOTTERY_RESULT_SUCCESS:
				lotteryMainView
						.setHint1TV("免费抽奖次数剩余:" + lotteryConfig.leftLoNo);
				// requestLotteryNum();
				lotteryMainView.getLotteryDrowView().setLotteryDrowResult(
						lotteryConfig.index);
				break;
			case HANDLER_MULTI_LOTTERY_CONFIG_REQ:
				lotteryMainView.mLotteryHandler
						.sendEmptyMessage(LotteryDrowMainView.LOTTERY_DROW_DOWNLOAD_RESOURCE);
				// lotteryMainView.setHint2TV("抽奖机正在下载资源，请稍后...");
				break;
			case HANDLER_MULTI_LOTTERY_DL_SUCCESS:
				// 判断所有图片下载是否完成，完成设置sharePreference设置为真
				Util.setBooleanSharedPreferences(mContext,
						LotteryDrowProvider.BITMAPS_DOWNLOAD_FINISH_TAG, true);
				// 所有图片下载完成，将初始化到还未动画界面
				lotteryMainView.mLotteryHandler
						.sendEmptyMessage(LotteryDrowMainView.LOTTERY_DROW_PREVIOUS);
				lotteryMainView.getLotteryDrowView().freshUI();
				break;

			// 抽奖机下载图片失败
			case HANDLER_MULTI_LOTTERY_DL_FAIL:
				// LotteryDrowView failLoDrowView = lotteryMainView
				// .getLotteryDrowView();
				// ArrayList<LotteryDrowPrize> failLoDrowPrizes = failLoDrowView
				// .getLoDrowPrizes();
				// int idFailDrowPrize = msg.arg1;
				// LotteryDrowPrize failLoDrowPrize = failLoDrowPrizes
				// .get(idFailDrowPrize);
				// Bitmap fBitmap = lotteryMainView.getLotteryDrowView()
				// .getDefaultPrizeImg();
				// failLoDrowPrize.setPrizeBitmap(fBitmap);
				// lotteryMainView.getLotteryDrowView().freshUI();
				UtilHelper.showCustomDialog(mContext, "下载资源出错，请稍候...");
				break;
			// 获取一键抽奖配置成功
			case HANDLER_MULTI_LOTTERY_CONFIG_SUCCESS:
				String multiLotteryVersion = msg.obj.toString();
				String multiXml = lotteryConfig.multiXml;
				// 获取抽奖配置成功，设置主界面的抽奖配置
				lotteryMainView.setLotteryConfigInfo(lotteryConfig);
				lotteryMainView.setMultiLoTimes(lotteryConfig.multiLoTimes);

				// 发起抽奖次数请求
				LotteryDrowProvider.getInstance(mContext).requestLotteryNum();

				if (tempLoDrowPrizes != null) {
					tempLoDrowPrizes.clear();
				}
				// 解析奖励的配置以将相关信息设置到抽奖界面
				boolean isParseXml = LotteryDrowHelper.parseLotteryPrizesXML(
						tempLoDrowPrizes, multiXml);
				if (isParseXml) {
					LotteryDrowView loDrowViewConfigSuccess = lotteryMainView
							.getLotteryDrowView();
					if (loDrowViewConfigSuccess != null) {
						loDrowViewConfigSuccess
								.setLotteryDrowPrizes(tempLoDrowPrizes);
					} else {
						return;
					}
					LotteryDrowHelper.downloadLotteryImgs(mContext,
							Util.getLotteryDir(),
							loDrowViewConfigSuccess.getLoDrowPrizes(),
							multiLotteryVersion, lotteryConfig,
							mLotteryUIHandler);
				}
				break;
			// 获取一键抽奖结果成功
			case HANDLER_MULTI_LOTTERY_RESULT_SUCCESS:
				// 一键抽奖，抽中的奖品列表
				lotteryMainView
						.setHint1TV("免费抽奖次数剩余:" + lotteryConfig.leftLoNo);
				ArrayList<LotteryDrowPrize> tempMultiLoDrowPrizes = new ArrayList<LotteryDrowPrize>();
				String multiPrizeXml = lotteryConfig.multiPrizeXml;
				Log.i(TAG, multiPrizeXml);
				boolean isParsePrizeXml = LotteryDrowHelper
						.parseMultiLotteryPrizesXML(tempMultiLoDrowPrizes,
								multiPrizeXml);
				if (isParsePrizeXml) {
					LotteryDrowView loDrowViewConfigMultiResult = lotteryMainView
							.getLotteryDrowView();
					if (loDrowViewConfigMultiResult != null) {
						loDrowViewConfigMultiResult
								.setMultiEndAnimData(tempMultiLoDrowPrizes);
					} else {
						return;
					}
				}
				break;
			case HANDLER_LOTTERY_ERROR:
				int errorCode = msg.arg1;
				lotteryMainView.getLotteryDrowView().stopMultiDrowAnim();
				lotteryMainView.getLotteryDrowView().stopOnceDrowAnim();
				lotteryDrowErrorHandler(errorCode);
				break;
			default:
				break;

			}
		}
	};

	// 初始化相关参数
	private void init() {
		// 计算缩放值，用于适配
		float[] scaleValues = LotteryDrowHelper.getScale(this);
		widthScale = scaleValues[0];
		heightScale = scaleValues[1];
		if (bitmapList == null) {
			bitmapList = new ArrayList<Bitmap>();
		}
	}

	/**
	 * 返回抽奖机界面的方法
	 * 
	 * @return
	 */
	public LotteryDrowMainView getLotteryMainView() {
		return lotteryMainView;
	}

	@Override
	protected void onDestroy() {
		recycleBitmap();
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	// 回收资源图片内存
	public void recycleBitmap() {
		if (bitmapList != null && bitmapList.size() > 0) {
			for (int i = 0; i < bitmapList.size(); i++) {
				Bitmap tempBitmap = bitmapList.get(i);
				tempBitmap.recycle();
				if (tempBitmap != null) {
					tempBitmap = null;
				}
				bitmapList.remove(i);
			}
		}
		bitmapList.clear();
		bitmapList = null;
	}

	public ArrayList<Bitmap> getBitmapList() {
		return bitmapList;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if ((lotteryMainView.getLotteryDrowView().getOnceDrowIsRunning() || lotteryMainView
					.getLotteryDrowView().getMultiDrowIsRunning())
					&& lotteryMainView.loHandlerWhat != LotteryDrowMainView.LOTTERY_MULTI_DROW_SHOW_PRIZES_FINISH) {
				UtilHelper.showCustomDialog(mContext, "正在抽奖中...");
				break;
			}
			lotteryMainView.getLotteryDrowView().stopMultiDrowAnim();
			lotteryMainView.getLotteryDrowView().stopOnceDrowAnim();
			finish();
			break;

		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	public static final int ERROR_NO_OPEN_LOTTERY = 3;// 服务器没有开启抽奖机
	public static final int ERROR_NET_TIMEOUT = 6;// 服务器繁忙
	public static final int ERROR_USER_DATA = 8;// 用户数据丢失
	public static final int ERROR_BEAN_NOT_ENOUGH = 10; // 乐豆不足
	public static final int ERROR_DISALLOW_LOTTERY = 11; // 抽奖功能关闭
	public static final int ERROR_USER_NO_LOGIN = 14;// 用户没有登录
	public static final int ERROR_THRESHOLD = 15; // 乐豆低于门槛
	public static final int ERROR_MAX_RAF_OVERFLOW = 17; // 当天抽奖次数超过上限
	public static final int ERROR_WINNER_EMPTY = 19; // 获奖名单为空

	public void lotteryDrowErrorHandler(int errorCode) {
		final LotteryConfigInfo lotteryConfig = LotteryConfigInfo
				.getInstance(mContext);
		switch (errorCode) {
		case ERROR_NO_OPEN_LOTTERY:
			UtilHelper.showCustomDialog(mContext, "未开启抽奖机功能",
					new OnClickListener() {

						@Override
						public void onClick(View v) {
							finish();

						}
					}, false);
		case ERROR_DISALLOW_LOTTERY:
			UtilHelper.showCustomDialog(mContext, "抽奖功能正在维护中...");
			break;
		case ERROR_BEAN_NOT_ENOUGH:
			UtilHelper.showLotteryBuyDialog(mContext, "抽奖一次要消耗"
					+ lotteryConfig.multiBCost + "乐豆.您的乐豆不足，是否购买乐豆？",
					AppConfig.propId, false, AppConfig.isConfirmon);
			break;
		case ERROR_THRESHOLD:
			UtilHelper.showLotteryBuyDialog(mContext, "乐豆数"
					+ lotteryConfig.multiThreshold + "以上才可以抽奖哦，是否充值乐豆？",
					AppConfig.propId, false, AppConfig.isConfirmon);
			break;
		case ERROR_MAX_RAF_OVERFLOW:
			UtilHelper.showCustomDialog(mContext, "您当天的抽奖次数已超过上限...");
			break;
		case ERROR_USER_DATA:
			UtilHelper.showCustomDialog(mContext, "用户数据错误，请重新登录再进行抽奖...");
			RecoverForDisconnect.getInstance().start(mContext,
					new SocketLoginListener() {

						@Override
						public void onSuccessed(Message arg0) {
							UtilHelper.showCustomDialog(mContext,
									arg0.toString());
						}

						@Override
						public void onFiled(Message arg0, int arg1) {
							UtilHelper.showCustomDialog(mContext,
									arg0.toString());
						}
					}, false, 0);
			break;
		case ERROR_USER_NO_LOGIN:
			UtilHelper.showCustomDialog(mContext, "用户数据错误，请重新登录再进行抽奖...");
			RecoverForDisconnect.getInstance().start(mContext,
					new SocketLoginListener() {

						@Override
						public void onSuccessed(Message arg0) {
							UtilHelper.showCustomDialog(mContext,
									arg0.toString());
						}

						@Override
						public void onFiled(Message arg0, int arg1) {
							UtilHelper.showCustomDialog(mContext,
									arg0.toString());
						}
					}, false, 0);
			break;
		case ERROR_NET_TIMEOUT:
			UtilHelper.showCustomDialog(mContext, "服务器繁忙，请稍候重试...");
		default:
			break;
		}
	}

}
