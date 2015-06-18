package com.mykj.game.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Xml;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import com.login.utils.DensityConst;
import com.mykj.andr.model.GoodsItem;
import com.mykj.andr.pay.PayManager;
import com.mykj.andr.provider.GoodsItemProvider;
import com.mykj.andr.ui.CustomDialog;
import com.mykj.andr.ui.FastBuyDialog;
import com.mykj.andr.ui.NodifyAccountSuccessDialog;
import com.mykj.andr.ui.widget.CrossExitDialog;
import com.mykj.andr.ui.widget.ExitDialog;
import com.mykj.andr.ui.widget.PayListDialog;
import com.mykj.game.FiexedViewHelper;
import com.MyGame.Migu.R;
import com.mykj.game.ddz.api.AnalyticsUtils;

/*******
 * 工具类，SD卡状态
 * 
 * @author
 */
public class UtilHelper {
	public static final String TAG = "UtilHelper";

	/** 未知 --要同web定义保存一致 */
	public static final int UNKNOW_TYPE = 0;
	/** 移动 --要同web定义保存一致 */
	public static final int MOVE_MOBILE_TYPE = 1;
	/** 联通 --要同web定义保存一致 */
	public static final int UNICOM_TYPE = 2;
	/** 电信 --要同web定义保存一致 */
	public static final int TELECOM_TYPE = 3;

	// 获得指定元素内容
	public static String getTagStr(String xmlStr, String tag) {
		String tagStr = "";
		if (xmlStr.indexOf(tag) > 0) {
			// 获取token成功
			int beginIndex = xmlStr.indexOf(tag + "=\"")
					+ (tag + "=\"").length();
			String tokenStr = xmlStr.substring(beginIndex, xmlStr.length());
			tagStr = tokenStr.substring(0, tokenStr.indexOf("\""));
		}
		return tagStr;
	}

	/**
	 * @Title: encodeHex
	 * @Description: 将INTEGER转换为八位的十六进制字符串
	 * @param integer
	 * @return
	 * @version: 2011-11-25 上午10:22:38
	 */
	public static String encodeHex(int integer) {
		String hexSTR = Long.toString(integer & 0xffffffff, 16);
		String returnStr = "00000000";
		int valid = returnStr.length() - hexSTR.length();
		if (valid > 0) {
			returnStr = returnStr.substring(0, valid) + hexSTR;
		} else {
			returnStr = hexSTR;
		}
		return returnStr;
	}

	/***
	 * 账户明细url
	 * 
	 * @param token
	 * @param uid
	 * @param channelID
	 * @param subchannelID
	 * @return
	 */
	public static String getMDetailUrl(String token, int uid, String channelID,
			String subchannelID) {

		try {
			token = URLEncoder.encode(token, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		long ts = System.currentTimeMillis();
		StringBuffer input = new StringBuffer();
		input.append("uid=");
		input.append(uid);
		input.append("&ts=");
		input.append(ts);
		input.append("&key=");
		input.append("87299596b8d9d642922a9a659aa70723");

		StringBuffer sb = new StringBuffer();
		sb.append(AppConfig.HOST);
		sb.append("/redir.aspx?urlid=80");
		sb.append("&at=");
		sb.append(token);
		sb.append("&uid=");
		sb.append(uid);
		sb.append("&ts=" + ts);
		sb.append("&verifystring=");
		sb.append(Util.md5(input.toString()));// 经过MD5编码？
		sb.append("&platid=");
		sb.append(3);
		sb.append("&channelid=");
		sb.append(channelID);
		sb.append("&subchannelid=");
		sb.append(subchannelID);
		return sb.toString();
	}

	/***
	 * 获得易宝支付URl
	 * 
	 * @param token
	 * @param uid
	 * @param count
	 * @param channelID
	 * @param subchannelID
	 * @return
	 */
	public static String getYIBAOUrl(String token, int uid, int count,
			String channelID, String subchannelID) {

		try {
			token = URLEncoder.encode(token, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		long ts = System.currentTimeMillis();
		StringBuffer input = new StringBuffer();
		input.append("uid=");
		input.append(uid);
		input.append("&ts=");
		input.append(ts);
		input.append("&key=");
		input.append("87299596b8d9d642922a9a659aa70723");
		StringBuffer sb = new StringBuffer();
		sb.append(AppConfig.HOST);
		sb.append("/redir.aspx?urlid=61");
		sb.append("&at=");
		sb.append(token);
		sb.append("&uid=");
		sb.append(uid);
		sb.append("&ts=" + ts);
		sb.append("&verifystring=");
		sb.append(Util.md5(input.toString()));// 经过MD5编码？
		sb.append("&shopid=0");
		sb.append("&amount=");
		sb.append(count * 100); // 这里添加金额分数
		sb.append("&platid=");
		sb.append(3);
		sb.append("&channelid=");
		sb.append(channelID);
		sb.append("&subchannelid=");
		sb.append(subchannelID);
		return sb.toString();
	}

	/***
	 * 根据制定背景图片分辨率与屏幕分别率进行比较得到缩放系数
	 * 
	 * @param act
	 * @return 返回缩放系数最小的
	 */
	public static float getScales(Activity act) {

		DisplayMetrics dm = new DisplayMetrics();
		act.getWindowManager().getDefaultDisplay().getMetrics(dm);

		// 获得背景图片宽高(背景图片作为判断不同分辨率，尺寸大小的设备使用，因为背景图片需要多份)
		Bitmap bit = BitmapFactory.decodeStream(act.getResources()
				.openRawResource(R.drawable.bg_common));

		int width = bit.getWidth();
		int height = bit.getHeight();
		return Math.max((dm.widthPixels * 1.0f / width * 1.0f),
				(dm.heightPixels * 1.0f / height * 1.0f));
	}

	/**
	 * sim卡信息：MCC(移动国家码，中国460)+MNC(移动网络码)+MSIN (有10位EF+M0M1M2M3+ABCD )
	 * 中国移动系统使用00
	 * 、02、07，中国联通GSM系统使用01，中国电信CDMA系统使用03，一个典型的IMSI号码为460030912121001;
	 * 
	 * @param act
	 * @return 返回不同运营商类型
	 */
	public static int getMobileCardType(Context context) {
		if (context == null)
			return UNKNOW_TYPE;
		try {
			TelephonyManager mTm = null;
			String msim = null;
			String msimName = null;
			String non = null;
			String imsi = null;
			try {
				mTm = (TelephonyManager) context
						.getSystemService(Context.TELEPHONY_SERVICE);
				msim = mTm.getSimOperator();
				imsi = mTm.getSubscriberId();
				msimName = mTm.getSimOperatorName();
				non = mTm.getNetworkOperatorName();

			} catch (Exception e1) {
			}

			Log.d(TAG, "卡的信息 SO：" + msim + ",SON：" + msimName + ",non:" + non
					+ ",SI:" + imsi);

			if (non != null && non.length() > 0) {
				// 判断网络运营商名字
				if (non.indexOf("CMCC") >= 0
						|| non.indexOf(AppConfig.mContext.getResources()
								.getString(R.string.config_yidong)) >= 0) {
					return MOVE_MOBILE_TYPE;
				} else if (non.indexOf("CUNT") >= 0
						|| non.indexOf(AppConfig.mContext.getResources()
								.getString(R.string.config_liantong)) >= 0) {
					return UNICOM_TYPE;
				} else if (non.indexOf("CTCC") >= 0
						|| non.indexOf(AppConfig.mContext.getResources()
								.getString(R.string.config_dianxin)) >= 0) {
					return TELECOM_TYPE;
				}
			}
			if (msimName != null && msimName.length() > 0) {
				// 判断运营商名字
				if (msimName.indexOf("CMCC") >= 0
						|| msimName.indexOf(AppConfig.mContext.getResources()
								.getString(R.string.config_yidong)) >= 0) {
					return MOVE_MOBILE_TYPE;
				} else if (msimName.indexOf("CUNT") >= 0
						|| msimName.indexOf(AppConfig.mContext.getResources()
								.getString(R.string.config_liantong)) >= 0) {
					return UNICOM_TYPE;
				} else if (msimName.indexOf("CTCC") >= 0
						|| msimName.indexOf(AppConfig.mContext.getResources()
								.getString(R.string.config_dianxin)) >= 0) {
					return TELECOM_TYPE;
				}
			}
			try {
				// 通过网络方式判断
				ConnectivityManager connec = (ConnectivityManager) context
						.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo info = connec
						.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
				String eInfo = info.getExtraInfo();
				if (eInfo != null && eInfo.length() > 0) {
					eInfo = eInfo.trim();
					if (eInfo.equals("uniwap") || eInfo.equals("3gnet")) {
						return UNICOM_TYPE;
					}
					if (eInfo.equals("cmnet") || eInfo.equals("cmwap")) {
						return MOVE_MOBILE_TYPE;
					}
				}
			} catch (Exception e) {
			}
			if (msim != null && msim.length() > 0) {
				// imsi
				if (msim.equals("46000") || msim.equals("46002")
						|| msim.equals("46007") || msim.equals("310026")) {
					return MOVE_MOBILE_TYPE;
				} else if (msim.equals("46001")) {
					return UNICOM_TYPE;
				} else if (msim.equals("46003")) {
					return TELECOM_TYPE;
				}
			}
			if (imsi != null && imsi.length() > 0) {
				// 判断IMSI,格式如：460030912121001
				String tmp = imsi.substring(3, 5);
				if (tmp != null) {
					switch (Integer.parseInt(tmp)) {
					case 1:
					case 6:
						return UNICOM_TYPE;
					case 0:
					case 2:
					case 7:
						return MOVE_MOBILE_TYPE;
					case 3:
					case 5:
						return TELECOM_TYPE;
					}
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "获取当前运营商信息出错");
			e.printStackTrace();
		}

		return UNKNOW_TYPE;
	}

	public static String getTopActName(Context ctx) {
		ActivityManager am = (ActivityManager) ctx
				.getSystemService(Context.ACTIVITY_SERVICE);
		ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
		String clsName = cn.getClassName();
		return clsName;
	}

	// --------------------------------------dialog对话框-------------------------------------------------

	/***
	 * @Title: showSocketAlertDialog
	 * @Description:网络断开提示框
	 * @param msg
	 *            ("网络连接断开，请检查您的网络或退出游戏稍后再试！)
	 * @param ctx
	 * @param listener
	 * @version: 2012-11-19 下午03:26:55
	 */
	public static void showSocketAlertDialog(CharSequence msg, Context ctx,
			final DialogInterface.OnClickListener mlistener,
			final DialogInterface.OnClickListener listener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setMessage(msg)
				.setCancelable(false)
				.setTitle(
						AppConfig.mContext.getResources().getString(
								R.string.prompt))
				.setNeutralButton(
						AppConfig.mContext.getResources().getString(
								R.string.ddz_set_net),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								if (mlistener != null)
									mlistener.onClick(dialog, which);
							}
						})
				.setPositiveButton(
						AppConfig.mContext.getResources().getString(
								R.string.ddz_empress_retry),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								if (listener != null)
									listener.onClick(dialog, id);
							}
						});
		AlertDialog alert = builder.create();
		try {
			alert.setOnKeyListener(new DialogInterface.OnKeyListener() {
				public boolean onKey(DialogInterface dialog, int keyCode,
						KeyEvent event) {
					if (keyCode == KeyEvent.KEYCODE_SEARCH) { // 屏蔽搜索键
						return true;
					} else {
						return false; // 默认返回 false
					}
				}
			});
			alert.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/****
	 * 弹出列表框
	 * 
	 * @param ctx
	 *            当前上下文
	 * @param title
	 *            标题 :消费记录，充值记录
	 * @param array
	 *            列表数组
	 */
	public static void showListViewRecord(Context ctx, CharSequence title,
			String[] array) {
		// showCustomDialog(ctx, msg, listener, mDrawableID)
		StringBuffer msg = new StringBuffer();
		if (array != null && array.length > 0) {
			msg.append(array[0]);
			for (int i = 1; i < array.length; i++) {
				msg.append("\n" + array[i]);
			}
		}
		CustomDialog dlg = new CustomDialog(ctx, msg.toString());
		dlg.show();
		/*
		 * AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		 * builder.setTitle(title).setCancelable(false).setItems(array, null)
		 * .setPositiveButton("确定", new DialogInterface.OnClickListener(){
		 * public void onClick(DialogInterface dialog,int id){ dialog.cancel();
		 * } }); AlertDialog alert = builder.create(); try { alert.show(); }
		 * catch (Exception e) { e.printStackTrace(); }
		 */
	}

	public static void showTimeColorDialog(final Context ctx, CharSequence msg,
			final OnClickListener listener, boolean showCancel,
			boolean showServerDial) {
		// 该对话框显示三秒
		showTimeColorDialog(ctx, msg, listener, showCancel, showServerDial,
				null);

	}

	public static void showTimeColorDialog(final Context ctx, CharSequence msg,
			final OnClickListener listener, boolean showCancel,
			boolean showServerDial, CharSequence confirmStr) {
		// 该对话框显示三秒
		showTimePromptDialog(ctx, msg, listener, showCancel, showServerDial, 3,
				confirmStr);
	}

	/**
	 * 服务器下方道具ID快速购买
	 * 
	 * @param ctx
	 * @param listener
	 * @param cancellistener
	 * @param goodsItem
	 * @param info
	 */
	public static void showCommonFastBuyDialog(Context ctx,
			final OnClickListener confirmlistener,
			final OnClickListener cancellistener, GoodsItem goodsItem,
			String info, String ensureBtnStr, String cancelBtnStr) {
		showBuyDialog(ctx, confirmlistener, cancellistener, goodsItem, info,
				ensureBtnStr, cancelBtnStr, false);
	}

	
	public static void showBuyDialog(final Context context, int propid,
			final boolean isHide, final boolean isConfirm,int action) {
		showBuyDialog(context, propid, isHide, isConfirm);
		AppConfig.talkingData(action,propid,-1,"-1");
	}
	
	
	/***
	 * @Title: showBuyDialog
	 * @Description: 普通弹出道具购买对话框，固定去登录送下发的快捷购买ID
	 * @param act
	 * @version: 2013-2-25 下午03:18:47
	 */
	public static void showBuyDialog(final Context context, int propid,
			final boolean isHide, final boolean isConfirm) {
		// TODO 立刻成为会员
		final GoodsItem goodsItem = UtilHelper.getGoodsItem(propid);
		if (null != goodsItem) {
//			showBuyDialog(context, new OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					// 调用快速购买方法
//					PayManager.getInstance(context).requestBuyPropPlist(goodsItem,
//							isConfirm, PayManager.FAST_BUY);
//				}
//			}, new OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					// 用户主动发起的快捷购买，关闭破产赠送请求
//					/*
//					 * if (HallAssociatedWidget.getInstance() != null) {
//					 * HallAssociatedWidget.getInstance().givingBankruptcy(); }
//					 */
//					PayManager.getInstance(context).requestRemovePlist(goodsItem,
//							isConfirm, PayManager.FAST_BUY);
//				}
//			}, goodsItem, "", null, null, isHide);
			PayManager.getInstance(context).requestBuyPropPlist(goodsItem,
					isConfirm, PayManager.FAST_BUY);
		}
	}

	/***
	 * @Title: showBuyDialog
	 * @Description: 弹出抽奖机的快捷购买，固定去登录送下发的快捷购买ID
	 * @param act
	 * @version: 2013-2-25 下午03:18:47
	 */
	public static void showLotteryBuyDialog(final Context context, String info,
			int propid, final boolean isHide, final boolean isConfirm) {
		// TODO 立刻成为会员
		final GoodsItem goodsItem = UtilHelper.getGoodsItem(propid);
		if (null != goodsItem) {
//			showBuyDialog(context, new OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					// 调用快速购买方法
					PayManager.getInstance(context).requestBuyPropPlist(goodsItem,
							isConfirm, PayManager.FAST_BUY);
//				}
//			}, new OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					// 关闭后调用破产赠送
//					// if (HallAssociatedWidget.getInstance() != null) {
//					// HallAssociatedWidget.getInstance().givingBankruptcy();
//					// }
//					PayManager.getInstance(context).requestRemovePlist(goodsItem,
//							isConfirm, PayManager.FAST_BUY);
//				}
//			}, goodsItem, info, null, null, isHide);
		}
	}

	private static FastBuyDialog fastBuyDialog;

	/**
	 * 快速购买
	 * 
	 * @param ctx
	 * @param listener
	 * @param cancellistener
	 * @param goodsItem
	 * @param info
	 * @param isHide
	 *            隐藏更多商品
	 * 
	 */
	public static void showBuyDialog(Context ctx,
			final OnClickListener confirmlistener,
			final OnClickListener cancellistener, GoodsItem goodsItem,
			String info, String ensureBtnStr, String cancelBtnStr,
			boolean isHide) {
		// 由于点击速度过快会导致弹出多个对话框，这里要控制此对话框只有一个弹出在外面；
		if (fastBuyDialog != null) {
			fastBuyDialog.dismiss();
			fastBuyDialog = null;
		}
		fastBuyDialog = new FastBuyDialog(ctx, goodsItem, info, ensureBtnStr,
				cancelBtnStr);

		fastBuyDialog.setConfirmCallBack(confirmlistener);
		fastBuyDialog.setCancelCallBack(cancellistener);
		try {
			fastBuyDialog.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 是否显示去商城按钮
		if (isHide) {
			fastBuyDialog.hideGotoMarket();
		}
		// 是否显示取消购买按钮
		if (!AppConfig.isShowCancel) {
			fastBuyDialog.hideBtnCancel();
		}

	}

	/**
	 * 商城购买成功提示对话框，显示一定时间
	 * 
	 * @param ctx
	 * @param msg
	 * @param listener
	 * @param mDrawableID
	 * @param showCancel
	 */
	private static void showTimePromptDialog(final Context ctx,
			CharSequence msg, final OnClickListener listener,
			boolean showCancel, boolean showServerDial, int time,
			CharSequence confirmStr) {
		int showServer = showServerDial ? CustomDialog.SHOW_SERVER_DIAL
				: CustomDialog.HIDE_SERVER_DIAL;
		final CustomDialog dialog = new CustomDialog(ctx, msg, showCancel,
				showServer, confirmStr,true);
		// final PromptDialog dialog = new PromptDialog(ctx, msg, mDrawableID,
		// showCancel);
		dialog.setConfirmCallBack(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (listener != null)
					listener.onClick(null);
				dialog.dismiss();// 退出
			}
		});
		try {
			dialog.show();
			android.os.Handler hander = new android.os.Handler();
			hander.postDelayed(new Runnable() {
				@Override
				public void run() {
					if (null != dialog && dialog.isShowing()) {
						dialog.dismiss();
					}

				}
			}, time * 1000);
		} catch (Exception e) {

		}
	}

	// -------------------------------dialog对话框---------------------------------------------------------

	/****
	 * @Title: getGoodsItem
	 * @Description: 根据道具ID获得道具对象
	 * @param propId
	 * @return
	 * @version: 2013-2-25 下午03:09:27
	 */
	public static GoodsItem getGoodsItem(int propId) {
		List<GoodsItem> goods = GoodsItemProvider.getInstance().getGoodsList();
		GoodsItem goodsItem = null;
		for (GoodsItem item : goods) {
			if (item.shopID == propId) {
				goodsItem = item;
				break;
			}
		}
		return goodsItem;
	}


	/***
	 * @Title: showPayListDialog
	 * @Description: 多种支付对话框
	 * @param ctx
	 * @param listener
	 * @param quickBuyBitmap
	 * @version: 2013-2-22 下午04:52:15
	 */
	public static void showPayListDialog(Context context,final DialogInterface.OnClickListener exitLlistener,
			final OnClickListener listener){
//		PayListDialog dialog = PayListDialog.getInstance(context);
		PayListDialog dialog = new PayListDialog(context);
		dialog.setDialogCallBack(new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 确定回调
				if (exitLlistener != null)
					exitLlistener.onClick(dialog, which);
			}
		});
		dialog.setCallBack(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// 确定回调
					if (listener != null)
						listener.onClick(v);
				}
			});
		dialog.setViewdata();
		try {
			if(!dialog.isShowing()){
				dialog.show();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	
	/***
	 * @Title: showExitDialog
	 * @Description: 显示退出对话框(包含各类信息：快捷购买、更多游戏)
	 * @param ctx
	 * @param listener
	 * @param quickBuyBitmap
	 * @version: 2013-2-22 下午04:52:15
	 */
	public static void showExitDialog(Context ctx,
			final OnClickListener listener,
			final DialogInterface.OnClickListener exitLlistener,
			final Bitmap quickBuyBitmap) {
		if (CrossGeneralizeHelper.isOpenInExitDialog()) {
			CrossExitDialog dialog = new CrossExitDialog(ctx);
			dialog.setCallBack(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// 确定回调
					if (listener != null)
						listener.onClick(v);
				}
			});
			dialog.setDialogCallBack(new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// 确定回调
					if (exitLlistener != null)
						exitLlistener.onClick(dialog, which);
				}
			});
			dialog.show();
		} else {
			ExitDialog dialog = new ExitDialog(ctx, quickBuyBitmap);
			dialog.setCallBack(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// 确定回调
					if (listener != null)
						listener.onClick(v);
				}
			});
			dialog.setDialogCallBack(new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// 确定回调
					if (exitLlistener != null)
						exitLlistener.onClick(dialog, which);
				}
			});
			dialog.show();
		}

	}

	/***
	 * @Title: showAccountNodifySuccess
	 * @Description: 弹出显示修改账号绑定成功
	 * @param ctx
	 * @param account
	 * @param nickName
	 * @param account
	 * @param douCount
	 * @param info
	 * @version: 2012-12-30 下午08:43:18
	 */
	public static void showAccountNodifySuccess(final Context ctx,
			String account, String nickName, String douCount, String info,
			final OnClickListener listener) {
		NodifyAccountSuccessDialog dialog = new NodifyAccountSuccessDialog(ctx,
				account, nickName, douCount, info);

		dialog.setCallBack(new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 确定回调
				if (listener != null)
					listener.onClick(null);
				dialog.dismiss();// 退出
			}
		});
		dialog.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				// TODO Auto-generated method stub
				if (listener != null)
					listener.onClick(null);
				dialog.dismiss();// 退出
			}
		});
		dialog.show();
	}

	/***
	 * @Title: showCustomDialog
	 * @Description: 一行文本对话框
	 * @param ctx
	 * @param msg
	 *            显示文本
	 * @version: 2012-10-11 下午02:42:57
	 */
	public static void showCustomDialog(Context ctx, String msg) {
		showCustomDialog(ctx, msg, null);
	}

	/**
	 * 显示底下带联系客服的对话框
	 * 
	 * @param ctx
	 * @param msg
	 */
	public static void showCustomDialogWithServer(Context ctx, CharSequence msg) {
		showCustomDialogWithServer(ctx, msg, null);
	}

	/**
	 * 提示框，3秒后自动关闭
	 * 
	 * @param ctx
	 * @param msg
	 * @param sec
	 */
	public static void showCustomDialog(Context ctx, String msg, int time) {
		showCustomDialog(ctx, msg, null, time);
	}

	/****
	 * @Title: showCustomDialog
	 * @Description: 可修改单个按钮对话框
	 * @param ctx
	 * @param msg
	 * @param listener
	 * @param mDrawableID
	 *            按钮ID，如果不需要修改按钮则传入-1
	 * @version: 2012-7-26 下午04:13:50
	 */
	public static CustomDialog showCustomDialog(final Context ctx,
			CharSequence msg, final OnClickListener listener) {
		CustomDialog dialog = new CustomDialog(ctx, msg);
		dialog.setConfirmCallBack(listener);
		// 用try，因为ctx可能已经无效了
		try {
			dialog.show();
		} catch (Exception e) {
			Log.i(TAG, e.toString());
		}
		return dialog;
	}

	/**
	 * 显示底下带联系客服的对话框
	 * 
	 * @param ctx
	 * @param msg
	 * @param listener
	 */
	public static void showCustomDialogWithServer(final Context ctx,
			CharSequence msg, final OnClickListener listener) {
		showCustomDialogWithServer(ctx, msg, listener, null);
	}

	public static void showCustomDialogWithServer(final Context ctx,
			CharSequence msg, final OnClickListener listener,
			CharSequence btnConfirmStr) {
		showCustomDialogWithServer(ctx, msg, listener, btnConfirmStr, null);
	}

	public static void showCustomDialogWithServer(final Context ctx,
			CharSequence msg, final OnClickListener listener,
			CharSequence btnConfirmStr, OnDismissListener dismissListen) {
		final CustomDialog dialog = new CustomDialog(ctx, msg,
				CustomDialog.SHOW_SERVER_DIAL, btnConfirmStr);
		dialog.setConfirmCallBack(listener);

		dialog.getWindow().setWindowAnimations(R.style.Dialog_Anim);
		if (dismissListen != null) {
			dialog.setOnDismissListener(dismissListen);
		}
		// 用try，因为ctx可能已经无效了
		try {
			dialog.show();
		} catch (Exception e) {
			Log.i(TAG, e.toString());
		}
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				dialog.dismiss();
			}
		}).start();
	}

	public static void showCustomDialogWithServer(final Context ctx,
			CharSequence msg, CharSequence btnConfirmStr,
			final OnClickListener confirmListener, CharSequence btnCancelStr,
			final OnClickListener cancelListener,
			OnDismissListener dismissListen) {
		final CustomDialog dialog = new CustomDialog(ctx, msg,
				CustomDialog.SHOW_SERVER_DIAL, btnConfirmStr, btnCancelStr);
		if (confirmListener != null) { // 设置确定的回调(右按键)
			dialog.setConfirmCallBack(confirmListener);
		}
		if (cancelListener != null) { // 设置取消的回调 (左按键)
			dialog.setCancelCallBack(cancelListener);
		}
		if (dismissListen != null) { // 设置消失的回调
			dialog.setOnDismissListener(dismissListen);
		}

		// 用try，因为ctx可能已经无效了
		dialog.show();
		try {
			dialog.show();
		} catch (Exception e) {
			Log.i(TAG, e.toString());
		}
//		new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
//				try {
//					Thread.sleep(3000);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//				dialog.dismiss();
//			}
//		}).start();
	}

	/**
	 * 显示取消按钮
	 * 
	 * @param ctx
	 * @param msg
	 * @param listener
	 * @param isShowCancle
	 */
	public static CustomDialog showCustomDialog(final Context ctx,
			CharSequence msg, final OnClickListener listener,
			boolean isShowCancle) {
		CustomDialog dialog = new CustomDialog(ctx, msg, isShowCancle);
		dialog.setConfirmCallBack(listener);
		try {
			dialog.show();
		} catch (Exception e) {
			Log.i(TAG, e.toString());
		}
		return dialog;
	}

	/**
	 * 显示底下带联系客服的对话框
	 * 
	 * @param ctx
	 * @param msg
	 * @param listener
	 * @param isShowCancle
	 */
	public static void showCustomDialogWithServer(final Context ctx,
			CharSequence msg, final OnClickListener listener, final OnClickListener listenercall,
			boolean isShowCancle) {
		final CustomDialog dialog = new CustomDialog(ctx, msg, isShowCancle,
				CustomDialog.SHOW_SERVER_DIAL,true);
		dialog.setConfirmCallBack(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 确定回调
				if (listener != null)
					listener.onClick(v);
				if(dialog.getRadio().isChecked()){
					PayManager.getInstance(ctx).setSharedPreferences(true);
				}
			}
		} );
		dialog.setCancelCallBack(listenercall);
		dialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface arg0) {
				// TODO Auto-generated method stub
				AppConfig.talkingData(AppConfig.ACTION_MARKET_NO ,AppConfig.propId,-1,"-1");  //7代表    商城列表 - 【购买】
				AnalyticsUtils.onClickEvent(ctx, "018");
			}
		});
		try {
			dialog.show();
		} catch (Exception e) {
			Log.i(TAG, e.toString());
		}
	}
	
	/**
	 * 显示取消按钮,N秒后自动消失
	 * 
	 * @param ctx
	 * @param msg
	 * @param listener
	 * @param isShowCancle
	 */
	public static void showCustomDialog(final Context ctx, CharSequence msg,
			final OnClickListener listener, boolean isShowCancle, int sec) {
		final CustomDialog dialog = new CustomDialog(ctx, msg, isShowCancle);
		dialog.setConfirmCallBack(listener);
		try {
			dialog.show();
			android.os.Handler hander = new android.os.Handler();
			hander.postDelayed(new Runnable() {
				@Override
				public void run() {
					if (null != dialog && dialog.isShowing()) {
						dialog.dismiss();
					}

				}
			}, sec * 1000);
		} catch (Exception e) {

		}
	}

	/**
	 * 提示框，3秒后自动关闭
	 * 
	 * @param ctx
	 * @param msg
	 * @param listener
	 * @param sec
	 */
	public static void showCustomDialog(final Context ctx, CharSequence msg,
			final OnClickListener listener, int sec) {
		final CustomDialog dialog = new CustomDialog(ctx, msg);
		dialog.setConfirmCallBack(listener);
		try {
			dialog.show();
			android.os.Handler hander = new android.os.Handler();
			hander.postDelayed(new Runnable() {
				@Override
				public void run() {
					if (null != dialog && dialog.isShowing()) {
						dialog.dismiss();
					}

				}
			}, sec * 1000);
		} catch (Exception e) {

		}
	}
	
	/**
	 * 提示框，不显示确定和取消按钮
	 * @param mAct
	 * @param msg
	 * @param listener 点击右上角关闭按钮监听
	 * @param isShowCancle
	 * @param isShowConfirm
	 */
	public static CustomDialog showCustomDialog(final Context ctx, String msg,
			final OnClickListener listener, boolean isShowCancle, boolean isShowConfirm) {
		final CustomDialog dialog = new CustomDialog(ctx, msg, false, false);
		dialog.setCancelCallBack(listener);
		dialog.show();
		return dialog;
	}

	/***
	 * 弹出日期选择对话框
	 * 
	 * @param ctx
	 *            当前Activity
	 * @param listener
	 *            日期选中回调监听
	 */
	public static void showDatePickDialog(Context ctx,
			final DateCallBack listener) {
		// 获得日历并设置当天
		final Calendar cd = Calendar.getInstance();
		cd.setTime(new Date());

		new DatePickerDialog(ctx, new OnDateSetListener() {
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				if (listener != null)
					listener.onDateCallBack(year, monthOfYear, dayOfMonth);
			}
		}, cd.get(Calendar.YEAR), cd.get(Calendar.MONTH),
				cd.get(Calendar.DAY_OF_MONTH)).show();
	}

	public interface DateCallBack {
		public String onDateCallBack(int year, int monthOfYear, int dayOfMonth);
	}

	/**
	 * @Title: parseSpannableFromMessage
	 * @Description: 设置字体高亮颜色
	 * @param msg
	 *            字符串
	 * @param forColor
	 *            高亮颜色
	 * @param start
	 *            高亮开始索引
	 * @param end
	 *            高亮结束索引
	 * @return
	 * @version: 2012-8-2 下午06:06:11
	 */
	public static SpannableString parseSpannableFromMessage(String msg,
			int forColor, int start, int end) {
		// 创建一个 SpannableString对象
		SpannableString sp = new SpannableString(msg);

		// 设置高亮样式二
		sp.setSpan(new ForegroundColorSpan(forColor), start, end,
				Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

		return sp;
	}

	/**
	 * @Title: parseSpannableFromMessage
	 * @Description: 提供2个高亮显示的SpannableString
	 * @param msg
	 *            字符串文本
	 * @param forColor
	 *            高亮显示颜色
	 * @param start
	 *            开始高亮索引
	 * @param end
	 *            结束高亮索引
	 * @param secstart
	 *            第二个开始高亮索引
	 * @param secend
	 *            第二个结束高亮索引
	 * @return
	 * @version: 2012-8-3 上午11:26:14
	 */
	public static SpannableString parseSpannableFromMessage(String msg,
			int forColor, int start, int end, int secstart, int secend) {
		// 创建一个 SpannableString对象
		SpannableString sp = new SpannableString(msg);

		// 第一个需要高亮
		sp.setSpan(new ForegroundColorSpan(forColor), start, end,
				Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

		// 第2个需要高亮
		sp.setSpan(new ForegroundColorSpan(forColor), secstart, secend,
				Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

		return sp;
	}

	/**
	 * @Title: getDate
	 * @Description: TODO(这里用一句话描述这个方法的作用)
	 * @return
	 * @version: 2012-8-1 下午04:23:22
	 */
	public static String getDate() {
		SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss", Locale.US);
		Date date = new Date();
		String ts = format.format(date);

		return ts;
	}

	public static void onWeb(Context context, String url, int uo) {
		if (uo == 1) {
			WebDialog webDialog = new WebDialog(context, R.style.BackgroundOnly);
			webDialog.setUrl(url);
		} else if (uo == 2) {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(url));
			context.startActivity(intent);
		} else {
			WebDialog webDialog = new WebDialog(context, R.style.BackgroundOnly);
			webDialog.setUrl(url);
		}
	}

	public static void onWeb(Context context, String url) {
		WebDialog webDialog = new WebDialog(context, R.style.BackgroundOnly);
		webDialog.setUrl(url);
	}

	public static void onWeb(final Context context, String url,
			OnDismissListener listener) {
		WebDialog webDialog = new WebDialog(context, R.style.BackgroundOnly);
		webDialog.setUrl(url);
		webDialog.setOnDismissListener(listener);
	}

	/**
	 * @Title: parseAttributeByName
	 * @Description: 从字符串data中获取name的值并返回
	 * @param name
	 *            名字
	 * @param data
	 *            字符串
	 * @return String 返回类型
	 * @throws
	 * @author DF
	 * @version: 2012-07-11
	 */
	public static String parseAttributeByName(String name, String data) {
		if (name == null || data == null) {
			return null;
		}
		int startPos = data.indexOf(name);
		if (startPos == -1) {
			return null;
		}
		int beginIndex = data.indexOf("\"", startPos) + 1;
		int endIndex = data.indexOf("\"", beginIndex);
		if (beginIndex < 0 || beginIndex > endIndex) {
			return null;
		}
		return data.substring(beginIndex, endIndex).trim();
	}

	// --------------------------------根据时间转换为字符串表示xx:xx:xx----------------------------
	public static String getTimeSecond(int interval) {
		String time = "";
		final int h = interval / (1000 * 60 * 60);
		if (h >= 24) {
			return (h / 24)
					+ AppConfig.mContext.getResources().getString(
							R.string.market_yuan);
		}
		final int m = interval / (1000 * 60) - h * 60;
		final int s = interval / 1000 - h * 60 * 60 - m * 60;
		time += getTimeNumber(h) + ":";
		time += getTimeNumber(m) + ":";
		time += getTimeNumber(s);
		return time;
	}

	private static String getTimeNumber(int time) {
		String str = "";
		if (time > 9) {
			str += time;
		} else {
			str += "0" + time;
		}
		return str;
	}

	// -------------------------------------------约战专区中弹出可接受用户输入对话框------------------------------------------
	/***
	 * @Title: showAlertDialog
	 * @Description: 带有确定，取消按钮的弹出框
	 * @param msg
	 * @param ctx
	 * @param listener
	 * @version: 2012-7-25 下午05:49:29
	 */
	public static void showCodeAlertDialog(Context ctx,
			final View.OnClickListener listener) {

		final EditText editText = new EditText(ctx);
		editText.setInputType(InputType.TYPE_CLASS_NUMBER
				| InputType.TYPE_CLASS_TEXT);

		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		builder.setView(editText);
		builder.setCancelable(false)
				.setTitle(
						AppConfig.mContext.getResources().getString(
								R.string.config_input_yaoqingma))
				.setNeutralButton(
						AppConfig.mContext.getResources().getString(
								R.string.Cancel),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						})
				.setPositiveButton(
						AppConfig.mContext.getResources().getString(
								R.string.Ensure),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								if (listener != null)
									listener.onClick(editText);
							}
						});
		AlertDialog alert = builder.create();
		try {
			alert.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ----------------------------------------------------------------------------------------------------------------------

	/** 显示排行榜 */
	public static void showRankWebView(short urlId, int userId, Context ctx) {
		String url = CenterUrlHelper.getWapUrl(urlId);
		String finalUrl = CenterUrlHelper.getUrl(url, userId);
		UtilHelper.onWeb(ctx, finalUrl);
	}

	public static void showWebView(String token, short urlId, int userId,
			Context ctx) {
		try {
			token = URLEncoder.encode(token, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String url = CenterUrlHelper.getWapUrl(urlId);
		url += "at=" + token + "&";
		String finalUrl = CenterUrlHelper.getUrl(url, userId);
		UtilHelper.onWeb(ctx, finalUrl);
	}

	/**
	 * 根据字符串，取得日期类
	 * 
	 * @param sDate
	 * @return
	 */
	public static Calendar getDate(String sDate) {
		if (sDate == null) {
			return null;
		}
		sDate = sDate.trim();
		if (sDate.length() == 7) {
			sDate += "-01";
		}
		StringTokenizer st = new StringTokenizer(sDate, "-");
		int year = 1999;
		int month = 9;
		int day = 31;
		try {
			year = Integer.parseInt(st.nextToken());
			month = Integer.parseInt(st.nextToken()) - 1;
			day = Integer.parseInt(st.nextToken());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new GregorianCalendar(year, month, day);
	}

	/**
	 * 比较两个日期类型的字符串，格式为（yyyy-mm-dd） 如果cale1大于cale2，返回1 如果cale1小于cale2，返回-1
	 * 如果相等，返回0 如果格式错误，返回-2
	 * 
	 * @param cale1
	 * @param cale2
	 * @return
	 */
	public static int compareCalendar(String cale1, String cale2) {
		Calendar calendar1 = getDate(cale1);
		Calendar calendar2 = getDate(cale2);
		if (calendar1 == null || calendar2 == null) {
			return -2;
		}
		return calendar1.compareTo(calendar2);
	}

	/**
	 * xml中解析键值对
	 * 
	 * @param strXml
	 * @param tagName
	 * @return
	 */
	public static String parseStatusXml(String strXml, String tagName) {
		String tagStr = "";
		try {
			// 定义工厂
			XmlPullParserFactory f = XmlPullParserFactory.newInstance();
			// 定义解析器
			XmlPullParser p = f.newPullParser();
			// 获取xml输入数据
			p.setInput(new StringReader(strXml));
			// 解析事件
			int eventType = p.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					if (p.getName().equals(tagName)) {
						tagStr = p.nextText();
					}
					break;
				case XmlPullParser.END_TAG:
					break;
				case XmlPullParser.END_DOCUMENT:
					break;
				default:
					break;
				}
				if (tagStr != null && tagStr.trim().length() > 0) {
					break;
				}
				// 用next方法处理下一个事件，否则会造成死循环。
				eventType = p.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tagStr;
	}

	public static String parseTagValueXml(String strXml, String tagName,
			String valueName) {
		String tagStr = "";
		try {
			// 定义工厂
			XmlPullParserFactory f = XmlPullParserFactory.newInstance();
			// 定义解析器
			XmlPullParser p = f.newPullParser();
			// 获取xml输入数据
			p.setInput(new StringReader(strXml));
			// 解析事件
			int eventType = p.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					if (p.getName().equals(tagName)) {
						tagStr = p.getAttributeValue("", valueName);
					}
					break;
				case XmlPullParser.END_TAG:
					break;
				case XmlPullParser.END_DOCUMENT:
					break;
				default:
					break;
				}
				if (tagStr != null && tagStr.trim().length() > 0) {
					break;
				}
				// 用next方法处理下一个事件，否则会造成死循环。
				eventType = p.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tagStr;
	}

	/****
	 * @Title: getSystemTime
	 * @Description: 活动系统时间
	 * @return
	 * @version: 2013-2-1 下午05:08:30
	 */
	public static String getSystemTime(boolean hour24Mode) {
		// yyyy-MM-dd hh:mm:ss
		String format = "HH:mm";
		if (!hour24Mode) {
			format = "hh:mm";
		}
		SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
		String date = sdf.format(new java.util.Date());
		return date;
	}

	/**
	 * 获取完善个人信息url
	 * 
	 * @param token
	 * @return
	 */
	public static String getMoreInfoUrl(String token) {
		return getContainBaseUrl(5001, token);
	}

	/****
	 * @Title: getMoreGamesUrl
	 * @Description: 更多游戏url
	 * @param token
	 * @return
	 * @version: 2013-2-26 下午01:53:46
	 */
	public static String getMoreGamesUrl(String token) {
		try {
			token = URLEncoder.encode(token, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return getContainBaseUrl(5000, token);
	}

	/***
	 * @Title: getContainBaseUrl
	 * @Description: 新url地址拼凑
	 * @param urlid
	 * @param tk
	 * @return
	 * @version: 2013-2-26 下午01:49:59
	 */
	public static String getContainBaseUrl(int urlid, String tk) {
		StringBuffer sb = new StringBuffer();
		sb.append(AppConfig.NEW_HOST);
		sb.append("?urlid=");
		sb.append(urlid);
		sb.append("&g=");

		String input = String.format(Locale.US,
				"<g k=%1$d p=%2$d g=%3$d c=%4$s sc=%5$s t=%6$d />", 0, 1,
				AppConfig.gameId, AppConfig.channelId + "",
				AppConfig.childChannelId + "", System.currentTimeMillis());
		String g = "";
		try {
			g = URLEncoder.encode(Base64.encode(input.toString().getBytes()),
					"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		sb.append(g);

		sb.append("&tk="); // 用户令牌(使用URL编码)
		try {
			sb.append(URLEncoder.encode(tk, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		int screenWidth = DensityConst.getWidthPixels();
		int screenHeight = DensityConst.getHeightPixels();
		String temp = String.format(Locale.US,
				"<m b=%1$s m=%2$s sw=%3$d sh=%4$d sys=%5$s sv=%6$s />",
				Util.getMobileBRAND(), Util.getMobileModel(), screenWidth,
				screenHeight, "android", Util.getOSVerion());
		String mi = "";
		try {
			mi = URLEncoder.encode(Base64.encode(temp.toString().getBytes()),
					"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		sb.append("&mi=");
		sb.append(mi);

		String v = Util.md5("g=" + g + "mi=" + mi);// 经过MD5编码？
		sb.append("&v=");
		sb.append(v); // 验证串，Md5(g=%smi=%skey)，%s分别传入g、mi参数值。

		return sb.toString();
	}

	/****
	 * @Title: getExitServerProprId
	 * @Description: 获取退出时候道具ID
	 * @return
	 * @version: 2013-2-27 上午10:26:10
	 */
	public static String getExitServerProprIdUrl() {
		StringBuffer sb = new StringBuffer();
		sb.append(AppConfig.NEW_HOST);
		sb.append("?ac=resource&tid=");
		sb.append(2);
		sb.append("&g=");
		// <g k=”%d” p=”%d” g=”%d” c=”%s” sc=”%s” t=”%d”>
		String input = String.format(Locale.US,
				"<g k=%1$d p=%2$d g=%3$d c=%4$s sc=%5$s t=%6$d e=%7$d/>", 0, 1,
				AppConfig.gameId, AppConfig.channelId,
				AppConfig.childChannelId, System.currentTimeMillis(), 2); // 1-->2
																			// 1.7.0修改为3个参数
		String g = "";
		try {
			g = URLEncoder.encode(Base64.encode(input.toString().getBytes()),
					"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		sb.append(g);
		return sb.toString();
	}

	/****
	 * @Title: invokePropIdThread
	 * @Description: 执行现场根据url获取道具ID
	 * @version: 2013-2-27 下午03:20:36
	 */
	public static void invokePropIdThread() {

		try {
			String result = doGetStatus(getExitServerProprIdUrl());
			if (!Util.isEmptyStr(result)) {
				try {
					String[] strs = result.split("#");

					if (strs != null && strs.length == 1) {
						AppConfig.propId = Integer.parseInt(strs[0]); // 道具赋值
					} else if (strs != null && strs.length == 2) {
						AppConfig.propId = Integer.parseInt(strs[0]); // 道具赋值
						int confirmon = Integer.parseInt(strs[1]); // 是否需要二次确认
						if (confirmon == 1) {
							AppConfig.isConfirmon = false;
						} else if (confirmon == 0) {
							AppConfig.isConfirmon = true;
						}
					} else if (strs != null && strs.length == 3) {
						AppConfig.propId = Integer.parseInt(strs[0]); // 道具赋值
						int confirmon = Integer.parseInt(strs[1]); // 是否需要二次确认
						int showCancel = Integer.parseInt(strs[2]); // 是否显示取消

						if (confirmon == 1) {
							AppConfig.isConfirmon = false;
						} else if (confirmon == 0) {
							AppConfig.isConfirmon = true;
						}

						if (showCancel == 1) {
							AppConfig.isShowCancel = false;
						} else if (showCancel == 0) {
							AppConfig.isShowCancel = true;
						}

					}
				} catch (NumberFormatException e) {
					Log.e(TAG, "道具格式转换异常！非数值");
				}
			}
		} catch (ConnectTimeoutException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static String getCmccSdkConfigUrl(Context context) {
		StringBuffer sb = new StringBuffer();
		sb.append(AppConfig.CMCC_SDK_URL);
		sb.append("?method=check&xmldata=");
		String xml = "";

		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("utf-8", null);
			serializer.startTag(null, "P");
			serializer.attribute(null, "uid", ""
					+ FiexedViewHelper.getInstance().getUserId()); // 用户ID
			serializer.attribute(null, "op", "" + getMobileCardType(context)); // 手机卡
			serializer.attribute(null, "os", "02"); // 02代表android
			serializer.attribute(null, "cid", AppConfig.channelId); // 主渠道号
			serializer.attribute(null, "scid", AppConfig.childChannelId); // 子渠道号
			serializer.attribute(null, "gid", "" + AppConfig.gameId); // 游戏ID
			serializer.attribute(null, "playid", ""
					+ FiexedViewHelper.getInstance().getGameType()); // 游戏玩法ID
			serializer.attribute(null, "pver", AppConfig.pay_version); // 支付组件版本号
			serializer.attribute(null, "versionName",
					Util.getVersionName(context));
			serializer.endTag(null, "P");
			serializer.endDocument();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		xml = writer.toString();
		try {
			xml = URLEncoder.encode(Base64.encode(xml.getBytes()), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		sb.append(xml);
		return sb.toString();
	}

	public static void reqCmccSdkConfig(Activity context) {
		String url = getCmccSdkConfigUrl(context);
		String strXml = Util.getConfigXmlByHttp(url);
		String res = parseStatusXml(strXml, "ydsdk");
		int config = 0;
		try {
			config = Integer.parseInt(res); // 道具赋值
			if(config != 1){
				config = 0;
			}
		} catch (NumberFormatException e) {
			config = 0;
		}
		AppConfig.setCmccSwitch(context, config);
	}

	public static String doGetStatus(String url)
			throws ConnectTimeoutException, Exception {
		// 最好进行url编码
		// url=URLDecoder.decode(url,"UTF-8");
		String data = "";

		DefaultHttpClient client = new DefaultHttpClient();// http客户端
		HttpGet httpGet = new HttpGet(url);

		try {

			HttpResponse response = client.execute(httpGet); // 发出实际的HTTP

			// 请求超时 10秒
			client.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
			// 读取超时
			client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
					10000);

			if (response.getStatusLine().getStatusCode() == 200) {
				// data = EntityUtils.toString(response.getEntity());

				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				// 返回相应字符串
				data = convertStreamToString(content);
				if (data != null && data.endsWith("\n")) {
					int len = data.length();
					data = data.substring(len - 2, len);
				}
				// URLDecoder.decode(data,"UTF-8");

				// 造成问题的原因是在编写json文件的时候，采用utf-8编码，utf8有个BOM格式，去掉这个
				if (data != null && data.startsWith("\ufeff")) {
					data = data.substring(1);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 关闭连接
			client.getConnectionManager().shutdown();
		}
		return data;
	}

	/**
	 * 流转字符串方法
	 * 
	 * @param is
	 * @return
	 */
	public static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	/**
	 * 刷话费券请求数据
	 * 
	 * @return
	 */
	public static String buildInfoHuafei() {
		StringBuffer sb = new StringBuffer();
		sb.append("<r>").append("<p n=\"");
		sb.append("mobilevoucher");
		sb.append("\"/>").append("</r>");
		return sb.toString();
	}

	/**
	 * 显示进度条
	 * 
	 * @param context
	 *            环境
	 * @param title
	 *            标题
	 * @param message
	 *            信息
	 * @param indeterminate
	 *            确定性
	 * @param cancelable
	 *            可撤销
	 * @return
	 */
	public static ProgressDialog showProgress(Context context,
			CharSequence title, CharSequence message, boolean indeterminate,
			boolean cancelable) {
		ProgressDialog dialog = new ProgressDialog(context);
		dialog.setTitle(title);
		dialog.setMessage(message);
		dialog.setIndeterminate(indeterminate);
		dialog.setCancelable(cancelable);
		// dialog.setDefaultButton(false);

		dialog.show();
		return dialog;
	}

	public static String listToString(List<String> stringList) {
		if (stringList == null) {
			return null;
		}
		StringBuilder result = new StringBuilder();
		boolean flag = false;
		for (String string : stringList) {
			if (flag) {
				result.append(",");
			} else {
				flag = true;
			}
			result.append(string);
		}
		return result.toString();
	}

	public static int stringToInt(String str) throws Exception {
		try {
			return Integer.parseInt(str.trim());
		} catch (Exception e) {
			throw e;
		}
	}

	public static int stringToInt(String str, int def) {
		int tInt = def;
		if (str != null && str.length() > 0) {
			try {
				tInt = Integer.parseInt(str.trim());
			} catch (Exception e) {
				tInt = def;
				e.printStackTrace();
			}
		}
		return tInt;
	}

	public static boolean isRunningActivity(Activity act) {
		if (act == null) {
			return false;
		}
		boolean isRunning = true;
		try {
			ActivityManager am = (ActivityManager) act
					.getSystemService(Activity.ACTIVITY_SERVICE);
			ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
			String clsName = cn.getClassName();
			if (!act.getClass().getName().equals(clsName)) {
				isRunning = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isRunning;
	}

}
