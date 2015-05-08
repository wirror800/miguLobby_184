package com.mykj.andr.provider;


import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.mingyou.distributor.NetPrivateListener;
import com.mingyou.distributor.NetSocketPak;
import com.mykj.andr.logingift.LotteryDrowActivity;
import com.mykj.andr.logingift.LotteryDrowView;
import com.mykj.andr.model.LotteryConfigInfo;
import com.mykj.andr.net.NetSocketManager;
import com.mykj.comm.io.TDataInputStream;
import com.mykj.comm.io.TDataOutputStream;
import com.mykj.game.FiexedViewHelper;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.Util;

/**
 * 抽奖机服务接口
 * 
 * @author JiangYinZhi
 * 
 */
public class LotteryDrowProvider {


	// -----------------------抽奖协议码-------------------------------------------
	/** 抽奖相关主协议码 */
	public static final short MDM_LOTTERY = 19;

	/** 请求获取抽奖奖励协议码 */
	public static final short LSUB_CMD_LOTTERY_REQ = 3;
	/** 返回获取抽奖奖励协议码 */
	public static final short LSUB_CMD_LOTTERY_RESP = 4;
	/** 请求更新抽奖次数协议码 */
	public static final short LSUB_CMD_UPDATE_LOT_TIMES_REQ = 5;
	/** 返回更新抽奖次数协议码 */
	public static final short LSUB_CMD_UPDATE_LOT_TIMES_RESP = 6;
	/** 请求获取抽奖次数协议码 */
	public static final short LSUB_CMD_GET_LOT_TIMES_REQ = 7;
	/** 返回获取抽奖次数协议码 */
	public static final short LSUB_CMD_GET_LOT_TIMES_RESP = 8;
	/** 请求获取抽奖奖励协议码 */
	public static final short LSUB_CMD_MULTI_LOTTERY_REQ = 11;
	/** 返回获取抽奖奖励协议码 */
	public static final short LSUB_CMD_MULTI_LOTTERY_RESP = 12;
	/** 请求获取一键抽奖配置协议码 */
	public static final short LSUB_CMD_MULTI_LOT_CONFIG_REQ = 13;
	/** 返回获取一键抽奖配置协议码 */
	public static final short LSUB_CMD_MULTI_LOT_CONFIG_RESP = 14;
	/** 请求获奖名单配置协议码 */
	public static final short LSUB_CMD_WINNER_LIST_REQ = 15;
	/** 返回获奖名单配置协议码 */
	public static final short LSUB_CMD_WINNER_LIST_RESP = 16;



	/*-----------------------常量--------------------*/
	// 一键抽奖版本号sharePreference里的标识
	public static final String MULTI_LOTTERY_VERSION = "multi_lottery_version";
	/** 表示上次抽奖图片是否下载完成,放在sharePreference里的key */
	public static final String BITMAPS_DOWNLOAD_FINISH_TAG = "multiIsDownloadFinish";
	/** 图片下载循环次数 */
	public static final int BITMAPS_DOWNLOAD_LOOP_NUM = 3;
	/** 表示图片未全部下载完成 */
	public static final int BITMAPS_DOWNLOAD_NO_FINISH = 0;
	

	// --------------------抽奖类成员--------------------------------------------------

	private static LotteryDrowProvider instance = null;

	private Context mContext;

	// ------------------抽奖类方法----------------------------------------------------

	private LotteryDrowProvider(Context context) {
		mContext = context;
	}

	/**
	 * 获取单例
	 * 
	 * @param context
	 * @return
	 */
	public static LotteryDrowProvider getInstance(Context context) {
		if (instance == null) {
			instance = new LotteryDrowProvider(context);
		}
		instance.mContext = context;
		return instance;
	}

	/**
	 * @Title: requestLotteryResult
	 * @param drowType
	 *            抽奖类型 0：使用乐豆抽奖 1：使用抽奖次数抽奖
	 * @Description: 解析请求获取抽奖结果的协议
	 */
	public void requestLotteryResult(byte drowType) {
		int userId = FiexedViewHelper.getInstance().getUserId();
		int gameId = AppConfig.gameId;
		// 创建发送的数据包
		TDataOutputStream tdos = new TDataOutputStream();
		tdos.setFront(false);
		tdos.writeInt(userId);
		tdos.writeShort(gameId);
		tdos.writeByte(drowType);

		NetSocketPak requestGoods = new NetSocketPak(MDM_LOTTERY,
				LSUB_CMD_LOTTERY_REQ, tdos);

		short[][] parseProtocol = { { MDM_LOTTERY, LSUB_CMD_LOTTERY_RESP } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {

			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {

				TDataInputStream tdis = netSocketPak.getDataInputStream();

				LotteryConfigInfo.getInstance(mContext).setLotteryResult(tdis);
				if (LotteryConfigInfo.getInstance(mContext).isResultFinish) {
					Handler handler=((LotteryDrowActivity)mContext).mLotteryUIHandler;
					handler.sendEmptyMessage(LotteryDrowActivity.HANDLER_LOTTERY_RESULT_SUCCESS);
				}
				return true;
			}
		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);

		// 发送协议
		NetSocketManager.getInstance().sendData(requestGoods);
		// 清理协议对象
		requestGoods.free();
	}

	/**
	 * @Title: requestLotteryNum
	 * @Description: 解析请求获取抽奖次数的协议
	 * @param userID
	 *            用户ID
	 * @param gameID
	 *            游戏ID
	 * @version:
	 * @param handler
	 */
	public void requestLotteryNum() {
		int userId = FiexedViewHelper.getInstance().getUserId();
		// 创建发送的数据包
		TDataOutputStream tdos = new TDataOutputStream();
		tdos.setFront(false);
		tdos.writeInt(userId);
		tdos.writeShort(AppConfig.gameId);

		NetSocketPak requestGoods = new NetSocketPak(MDM_LOTTERY,
				LSUB_CMD_GET_LOT_TIMES_REQ, tdos);

		short[][] parseProtocol = { { MDM_LOTTERY, LSUB_CMD_GET_LOT_TIMES_RESP } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {

			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				TDataInputStream tdis = netSocketPak.getDataInputStream();
				LotteryConfigInfo.getInstance(mContext).setLotteryTimes(tdis);
				Handler handler = ((LotteryDrowActivity) mContext).mLotteryUIHandler;
				Message msg = handler.obtainMessage();
				if (LotteryConfigInfo.getInstance(mContext).isTimesFinish) {
					msg.what = LotteryDrowActivity.HANDLER_LOTTERY_NUM_SUCCESS;
				} else {
					msg.what = LotteryDrowActivity.HANDLER_LOTTERY_NUM_FAIL;
				}
				handler.sendMessage(msg);
				return true;
			}
		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);

		// 发送协议
		NetSocketManager.getInstance().sendData(requestGoods);
		// 清理协议对象
		requestGoods.free();
	}

	/**
	 * @Title: requestLotteryConfig
	 * @Description: 解析请求一键抽奖配置协议
	 */
	public void requestMultiLotteryConfig() {
		final Handler handler = ((LotteryDrowActivity) mContext).mLotteryUIHandler;
		handler.sendEmptyMessage(LotteryDrowActivity.HANDLER_MULTI_LOTTERY_CONFIG_REQ);
		//mLotteryHandler.sendEmptyMessage(HANDLER_MULTI_LOTTERY_CONFIG_REQ);
		int userId = FiexedViewHelper.getInstance().getUserId();
		short gameId = (short) AppConfig.gameId;

		final String multiLoVersion = Util.getStringSharedPreferences(mContext,
				MULTI_LOTTERY_VERSION, "");

		short channelID = 0;
		short childChannelID = 0;
		if (!Util.isEmptyStr(AppConfig.channelId)) {
			channelID = Short.parseShort(AppConfig.channelId);
		}
		if (!Util.isEmptyStr(AppConfig.childChannelId)) {
			childChannelID = Short.parseShort(AppConfig.childChannelId);
		}

		int mobileVersion = Util.getProtocolCode(AppConfig.ZONE_VER);
		// 创建发送的数据包
		TDataOutputStream tdos = new TDataOutputStream();
		tdos.setFront(false);
		tdos.writeInt(userId);
		tdos.writeShort(gameId);
		tdos.writeShort(channelID);
		tdos.writeShort(childChannelID);
		tdos.writeInt(mobileVersion);
		tdos.writeUTF(multiLoVersion, 32);// 32字节
		NetSocketPak requestGoods = new NetSocketPak(MDM_LOTTERY,
				LSUB_CMD_MULTI_LOT_CONFIG_REQ, tdos);
		short[][] parseProtocol = { { MDM_LOTTERY,
				LSUB_CMD_MULTI_LOT_CONFIG_RESP } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {

			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {

				TDataInputStream tdis = netSocketPak.getDataInputStream();
				LotteryConfigInfo.getInstance(mContext).setMultiLotteryConfig(
						tdis);
				if (LotteryConfigInfo.getInstance(mContext).isMultiConfigFinish) {
					Message msg = handler.obtainMessage();
					msg.obj = multiLoVersion;
					msg.what = LotteryDrowActivity.HANDLER_MULTI_LOTTERY_CONFIG_SUCCESS;
					handler.sendMessage(msg);
				} else {
					handler.sendEmptyMessage(LotteryDrowActivity.HANDLER_MULTI_LOTTERY_CONFIG_FAIL);
				}

				return true;
			}
		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);

		// 发送协议
		NetSocketManager.getInstance().sendData(requestGoods);
		// 清理协议对象
		requestGoods.free();
	}

	/**
	 * @Title: requestMultiLotteryResult
	 * @Description: 解析请求获取一键抽奖结果的协议
	 */
	public boolean requestMultiLotteryResult() {
		final Handler handler = ((LotteryDrowActivity) mContext).mLotteryUIHandler;
		final long startSystemTime = System.currentTimeMillis();
		int userId = FiexedViewHelper.getInstance().getUserId();
		int gameId = AppConfig.gameId;
		short multiLoTimes = LotteryConfigInfo.getInstance(mContext)
				.getMultiLoTimes();
		if (multiLoTimes <= 0) {
			return false;
		}
		// 创建发送的数据包
		TDataOutputStream tdos = new TDataOutputStream();
		tdos.setFront(false);
		tdos.writeInt(userId);
		tdos.writeShort(gameId);
		tdos.writeShort(multiLoTimes);

		NetSocketPak requestMultiLoResult = new NetSocketPak(MDM_LOTTERY,
				LSUB_CMD_MULTI_LOTTERY_REQ, tdos);

		short[][] parseProtocol = { { MDM_LOTTERY, LSUB_CMD_MULTI_LOTTERY_RESP } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {

			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				TDataInputStream tdis = netSocketPak.getDataInputStream();
				LotteryConfigInfo.getInstance(mContext).setMultiLotteryResult(
						tdis);
				if (LotteryConfigInfo.getInstance(mContext).isMultiResultFinish) {
					// requestLotteryNum();
					Message msg = handler.obtainMessage();
					msg.what = LotteryDrowActivity.HANDLER_MULTI_LOTTERY_RESULT_SUCCESS;
					long multiDrowInterval = System.currentTimeMillis()
							- startSystemTime;
					if (multiDrowInterval < LotteryDrowView.MULTI_DROW_ANIM_MIN_TIME) {
						handler.sendMessageDelayed(msg,
								LotteryDrowView.MULTI_DROW_ANIM_MIN_TIME
										- multiDrowInterval);
					} else {
						handler.sendMessage(msg);
					}
				}

				return true;
			}
		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);

		// 发送协议
		NetSocketManager.getInstance().sendData(requestMultiLoResult);
		// 清理协议对象
		requestMultiLoResult.free();
		return true;
	}

	/**
	 * @Title: requestWinnerList
	 * @Description: 获取抽奖名单
	 */
	public boolean requestWinnerList() {
		int userId = FiexedViewHelper.getInstance().getUserId();
		int gameId = AppConfig.gameId;
		int total = 0;
		// 创建发送的数据包
		TDataOutputStream tdos = new TDataOutputStream();
		tdos.setFront(false);
		tdos.writeInt(userId);
		tdos.writeShort(gameId);
		tdos.writeInt(total);

		NetSocketPak requestWinnerList = new NetSocketPak(MDM_LOTTERY,
				LSUB_CMD_WINNER_LIST_REQ, tdos);

		short[][] parseProtocol = { { MDM_LOTTERY, LSUB_CMD_WINNER_LIST_RESP } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {

			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				TDataInputStream tdis = netSocketPak.getDataInputStream();
				LotteryConfigInfo.getInstance(mContext).setWinnerListData(tdis);

				if (LotteryConfigInfo.getInstance(mContext).isWinnerListFinish) {
					Handler handler = ((LotteryDrowActivity) mContext).mLotteryUIHandler;
					handler.sendEmptyMessage(LotteryDrowActivity.HANDLER_WINNER_LIST_RESULT_SUCCESS);
				}

				return true;
			}
		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);

		// 发送协议
		NetSocketManager.getInstance().sendData(requestWinnerList);
		// 清理协议对象
		requestWinnerList.free();
		return true;
	}


}
