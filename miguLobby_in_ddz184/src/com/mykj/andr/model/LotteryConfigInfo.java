package com.mykj.andr.model;

import java.io.File;
import java.io.Serializable;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.mykj.andr.logingift.LotteryDrowActivity;
import com.mykj.andr.provider.LotteryDrowProvider;
import com.mykj.comm.io.TDataInputStream;
import com.mykj.game.utils.Log;
import com.mykj.game.utils.Util;

public class LotteryConfigInfo implements Serializable {
	public static final String LOTTERY_CONFIG = "lottery.cfg";
	private static final long serialVersionUID = 1L;

	private static LotteryConfigInfo instance;
	private Context mContext;

	public boolean isConfigFinish = false;
	public boolean isResultFinish = false;
	public boolean isTimesFinish = false;
	public boolean isMultiConfigFinish = false;
	public boolean isMultiResultFinish = false;
	public boolean isWinnerListFinish = false;

	public byte code;

	/** 单次抽奖配置参数 */
	// public byte total;// 总的道具总数
	// public String version;// 版本
	// public String lurl;// 图标url
	// public String hurl;// 秘籍url
	// public String xml;// 配置的xml信息
	// public int bCost;//乐豆抽奖费用
	// public byte bCostPower;//乐豆抽奖开关(0代表关闭抽奖开关，1代表打开抽奖开关)
	
	public short leftLoNo = -1;//剩余抽奖次数

	/** 抽奖结果的参数 */
	public byte resultCode = -1;// 抽奖结果code
	public byte index;// 奖品抽中的位置
	public int prodID;// 抽中的奖品ID
//	public short lotLeftNo = -1;// 剩余抽奖次数
	public String prodDesc;// 抽中奖品的描述信息
	public short logonLotteryNo;// 登录送抽奖次数
	public short taskLotteryNo;// 登录送抽奖次数
	public short buyLotteryNo;// 购买抽奖次数
	public short otherLotteryNo;// 其他抽奖次数

	/** 多次抽奖配置参数 */
	public byte multiTotal;// 一键抽奖总的道具数
	public String multiVersion;// 一键抽奖的版本号
	public String multiLurl;// 一键抽奖图标的url
	public String multiHurl;// 一键抽奖秘籍的url
	public int multiBCost;// 一键抽奖乐豆抽奖费用
	public byte multiBCostPower;// 一键抽奖乐豆抽奖开关
	public short multiLoTimes = -1;// 一键抽奖所需要的抽奖次数
	public int multiThreshold;// 一键抽奖乐豆抽奖门槛
	public int multiMaxRaffle;// 一键抽奖每天允许抽奖的最大次数
	public String multiXml;// 一键抽奖奖品列表
	// =
	// "<res><b i=\"1\" t=\"2\" p=\"0\" c=\"100\" d=\"乐豆\" bit=\"ledou_20130606.png\" /><b i=\"2\" t=\"1\" p=\"417\" c=\"3\" d=\"话费券碎片\" bit=\"HFQshuipian_20130606.png\" /><b i=\"3\" t=\"1\" p=\"5002\" c=\"1\" d=\"三星GALAXY\" bit=\"SMshouji_20130606.png\" /><b i=\"4\" t=\"2\" p=\"0\" c=\"100\" d=\"乐豆\" bit=\"ledou_20130606.png\" /><b i=\"5\" t=\"1\" p=\"403\" c=\"50\" d=\"话费券\" bit=\"huafeiquan_20130606.png\" /><b i=\"6\" t=\"1\" p=\"421\" c=\"3\" d=\"小乐豆\" bit=\"xiaoledou_20130606.png\" /><b i=\"7\" t=\"1\" p=\"422\" c=\"10\" d=\"1万乐豆\" bit=\"ledouguang_20130606.png\" /><b i=\"8\" t=\"1\" p=\"306\" c=\"1\" d=\"大奖赛门票\" bit=\"mengpiao_20130606.png\" /><b i=\"9\" t=\"1\" p=\"403\" c=\"5\" d=\"话费券\" bit=\"huafeiquan_20130606.png\" /><b i=\"10\" t=\"2\" p=\"0\" c=\"500\" d=\"乐豆\" bit=\"ledou_20130606.png\" /><b i=\"11\" t=\"2\" p=\"0\" c=\"10000\" d=\"乐豆\" bit=\"shuangledou_20130606.png\" /><b i=\"12\" t=\"2\" p=\"0\" c=\"100\" d=\"乐豆\" bit=\"ledou_20130606.png\" /><b i=\"13\" t=\"1\" p=\"421\" c=\"1\" d=\"小乐豆\" bit=\"xiaoledou_20130606.png\" /><b i=\"14\" t=\"2\" p=\"0\" c=\"100\" d=\"乐豆\" bit=\"ledou_20130606.png\" /><b i=\"15\" t=\"2\" p=\"0\" c=\"100\" d=\"乐豆\" bit=\"ledou_20130606.png\" /><b i=\"16\" t=\"1\" p=\"403\" c=\"3\" d=\"话费券\" bit=\"huafeiquan_20130606.png\" /></res>";

	/** 多次抽奖结果参数 */
	public byte multiPrizeNo;// 一键抽奖的奖品数
//	public short multiLotLeftNo;// 一键抽奖剩余抽奖次数
	public String multiPrizeXml;// 一键抽奖的奖品列表

	/** 获奖名单列表参数 */
	public int winnerListTotal = -1;// 获奖名单的获奖人数

	private String winnerListXml;// 获奖名单的获奖列表

	public final static int BEAN_NO_ENOUGH = 10;// 乐豆不足的错误码

	/**
	 * 私有构造函数
	 */
	private LotteryConfigInfo(Context context) {
		mContext = context;
	}

	/**
	 * 获取单例
	 * 
	 * @return
	 */
	public static LotteryConfigInfo getInstance(Context context) {
		if (instance == null) {
			instance = new LotteryConfigInfo(context);
		}
		instance.mContext = context;
		return instance;
	}

	/** 设置一键抽奖配置缓存数据 */
	public void setMultiLotteryConfig(TDataInputStream dis) {
		if (dis == null) {
			return;
		}
		dis.setFront(false);
		short headLength = dis.readShort();// 读取头长度,头长度用于跳过多少字节直接读取xml，便于以后版本扩展
		int tempCode = dis.readByte();
		if (tempCode == 2) {
			// read form file
			TDataInputStream local_tdis = readLotteryData();
			setLocalMultiLotteryData(local_tdis);
			isMultiConfigFinish = true;
		} else if (tempCode == 0) {
			// 根据头文件标识的跳过字节数 读取xml数据体
			dis.skip(headLength - 3);
			multiXml = dis.readUTFShort();// 配置的xml数据体信息
			// 跳过3字节进行正常读取
			dis.reset();
			dis.skip(3);
			multiTotal = dis.readByte();// 道具总数
			multiVersion = dis.readUTFShort();// 版本
			multiLurl = dis.readUTFShort();// 图标url
			multiHurl = dis.readUTFShort();// 秘籍url
			multiBCost = dis.readShort();// 乐豆抽奖费用
			multiBCostPower = dis.readByte();// 乐豆抽奖开关
			multiLoTimes = dis.readShort();// 一键抽奖次数
			multiThreshold = dis.readInt();// 乐豆抽奖门槛
			multiMaxRaffle = dis.readInt();// 每天允许的最大抽奖次数
			// multiXml = dis.readUTFShort();// 配置的xml数据体信息
			Log.v("MultiLotteryConfig", "total:" + multiTotal + "   "
					+ "version:" + multiVersion + "  " + "lurl:" + multiLurl
					+ "  " + "hurl:" + multiHurl + "  " + "bCost:" + multiBCost
					+ "  " + "bCostPower:" + multiBCostPower + "  " + "times:"
					+ multiLoTimes + "  " + "threshold:" + multiThreshold
					+ "  " + "maxRaffle:" + multiMaxRaffle + " " + "xml:"
					+ multiXml);
			saveLotteryData(dis);// 保存到文件
			Util.setStringSharedPreferences(mContext,
					LotteryDrowProvider.MULTI_LOTTERY_VERSION, multiVersion);
			isMultiConfigFinish = true;
		} else {
			sendErrorMsg(tempCode);
			isMultiConfigFinish = false;
		}

	}

	/** 设置一键抽奖结果缓存数据 */
	public void setMultiLotteryResult(TDataInputStream dis) {
		if (dis == null) {
			return;
		}
		dis.setFront(false);
		byte code = dis.readByte();
		if (code == 0) {
			multiPrizeNo = dis.readByte();
			leftLoNo = dis.readShort();
			multiPrizeXml = dis.readUTFShort();
			isMultiResultFinish = true;
		} else {
			multiPrizeNo = 0;
//			multiLotLeftNo = 0;
			multiPrizeXml = null;
			isMultiResultFinish = false;
			sendErrorMsg(code);
		}
	}

	/** 设置抽奖结果缓存数据 */
	public void setLotteryResult(TDataInputStream dis) {
		if (dis == null) {
			return;
		}
		dis.setFront(false);
		byte code = dis.readByte();
		if (code == 0) {
			index = dis.readByte();
			prodID = dis.readInt();
			leftLoNo = dis.readShort();
			prodDesc = dis.readUTFShort();
			isResultFinish = true;
		} else {
			index = 0;
			prodID = 0;
//			lotLeftNo = 0;
			prodDesc = null;
			sendErrorMsg(code);
			isResultFinish = false;
		}
		if (code == 10) {// 乐豆次数不够
			resultCode = 10;
		} else {
			resultCode = -1;
		}
	}

	/** 设置抽奖次数缓存数据 */
	public void setLotteryTimes(TDataInputStream dis) {
		if (dis == null) {
			return;
		}
		dis.setFront(false);
		byte code = dis.readByte();
		if (code == 0) {
			logonLotteryNo = dis.readShort();// 登录送抽奖次数
			taskLotteryNo = dis.readShort();// 任务送抽奖次数
			buyLotteryNo = dis.readShort();// 购买抽奖次数
			otherLotteryNo = dis.readShort();// 其他渠道抽奖次数
			leftLoNo = (short) (logonLotteryNo + taskLotteryNo + buyLotteryNo + otherLotteryNo);
			isTimesFinish = true;
		} else {
			leftLoNo = -1;
			sendErrorMsg(code);
			isTimesFinish = false;
		}
	}

	/** 设置获奖列表缓存数据 */
	public void setWinnerListData(TDataInputStream dis) {
		if (dis == null) {
			return;
		}
		dis.setFront(false);
		short headLength = dis.readShort();
		byte code = dis.readByte();
		if (code == 0) {
			// 根据头长度，跳过
			dis.skip(headLength - 3);
			winnerListXml = dis.readUTFShort();
			// 重设，跳过3个字节，进行正常读取
			dis.reset();
			dis.skip(3);
			winnerListTotal = dis.readInt();
			winnerListXml = dis.readUTFShort();
			isWinnerListFinish = true;
		} else {
			winnerListTotal = -1;
			winnerListXml = null;
			sendErrorMsg(code);
			isWinnerListFinish = false;
		}

	}

	/** 保存抽奖配置到本地文件 */
	public void saveLotteryData(TDataInputStream dis) {
		dis.reset();
		File file = mContext.getFileStreamPath(LOTTERY_CONFIG);
		if (file.exists()) {
			mContext.deleteFile(LOTTERY_CONFIG);
		}
		byte[] b = dis.readBytes();
		Util.saveToFile(file, b);
	}

	/** 读取本地抽奖配置文件 */
	public TDataInputStream readLotteryData() {
		TDataInputStream tdis = null;
		File file = mContext.getFileStreamPath(LOTTERY_CONFIG);
		if (file.exists()) {
			byte[] b = Util.readBytesFromFile(file);
			tdis = new TDataInputStream(b, false);
		}
		return tdis;
	}

	/** 将本地的抽奖配置文件设置到缓存数据当中 */
	private void setLocalMultiLotteryData(TDataInputStream dis) {
		if (dis == null) {
			return;
		}
		dis.setFront(false);
		short headLength = dis.readShort();
		// 根据头文件标识的跳过字节数 读取xml数据体
		dis.skip(headLength - 2);
		multiXml = dis.readUTFShort();// 配置的xml数据体信息
		// 跳过3字节进行正常读取
		dis.reset();
		dis.setFront(false);
		dis.skip(3);

		multiTotal = dis.readByte();// 道具总数
		multiVersion = dis.readUTFShort();// 版本
		multiLurl = dis.readUTFShort();// 图标url
		multiHurl = dis.readUTFShort();// 秘籍url
		multiBCost = dis.readShort();// 乐豆抽奖费用
		multiBCostPower = dis.readByte();// 乐豆抽奖开关
		multiLoTimes = dis.readShort();// 一键抽奖次数
		multiThreshold = dis.readInt();// 乐豆抽奖门槛
		multiMaxRaffle = dis.readInt();// 每天允许的最大抽奖次数
	}

	/**
	 * 获取抽奖次数
	 * 
	 * @return
	 */
	public int getLotteryTimes() {
		int times = 0;
		if (isTimesFinish) {
			times = leftLoNo;
		}
		return times;
	}

	/**
	 * 返回获奖者名单
	 * 
	 * @return
	 */
	public String getWinnerListXml() {
		String list = null;

		if (isWinnerListFinish) {
			list = winnerListXml;
		}
		return list;
	}

	/***
	 * 获取一键抽奖的抽奖次数
	 * 
	 * @return 下发的配置的一键抽奖次数
	 */
	public short getMultiLoTimes() {
		return multiLoTimes;
	}

	private void sendErrorMsg(int errorCode) {
		final Handler handler = ((LotteryDrowActivity) mContext).mLotteryUIHandler;
		Message msg = handler.obtainMessage();
		msg.what = LotteryDrowActivity.HANDLER_LOTTERY_ERROR;
		msg.arg1 = errorCode;
		handler.sendMessage(msg);
	}
}
