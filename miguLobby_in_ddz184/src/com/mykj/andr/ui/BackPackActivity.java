package com.mykj.andr.ui;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mingyou.distributor.NetPrivateListener;
import com.mingyou.distributor.NetSocketPak;
import com.mykj.andr.model.BackPackItem;
import com.mykj.andr.model.DateDetailInfo;
import com.mykj.andr.model.HPropUseBack;
import com.mykj.andr.model.HallDataManager;
import com.mykj.andr.model.UserInfo;
import com.mykj.andr.net.NetSocketManager;
import com.mykj.andr.provider.BackPackItemProvider;
import com.mykj.andr.ui.adapter.BackPageAdapter;
import com.mykj.andr.ui.adapter.BackPageAdapter.HandselCallBack;
import com.mykj.andr.ui.adapter.BackPageAdapter.UseCallBack;
import com.mykj.andr.ui.widget.CardZoneProtocolListener;
import com.mykj.comm.io.TDataInputStream;
import com.mykj.comm.io.TDataOutputStream;
import com.mykj.game.MainApplication;
import com.MyGame.Midlet.R;
import com.mykj.game.ddz.api.AnalyticsUtils;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.CenterUrlHelper;
import com.mykj.game.utils.Log;
import com.mykj.game.utils.Toast;
import com.mykj.game.utils.Util;
import com.mykj.game.utils.UtilHelper;

/***
 * 
 * @ClassName: BackPackActivity
 * @Description:我的背包
 * @author Administrator
 * @date 2012-7-24 下午02:12:11
 * 
 */
public class BackPackActivity extends ListActivity implements
		OnItemClickListener, OnClickListener, UseCallBack, HandselCallBack {
	private static final String TAG = "BackPackActivity";

	private BackPageAdapter mBackPageAdapter;
	private ListView lvBackPage;

	private LinearLayout progressBackPack;

	private ImageView ivHelp;
	/** 去商城 **/
	private Button btnToMarket;
	/** 合成 **/
	private ImageView ivMix;

	private RelativeLayout binguBoxLayout;

	private LinearLayout lyBackPack;

	private FileInputStream inputStream;
	private FileOutputStream outStream;

	private HandselDialog dialog;

	private String handselName;

	private int userID;
	private String userToken;
	private long guid;

	private HPropUseBack propBack;
	/** 使用游戏对象 */
	private static final byte GAME_OBJ = 0;

	public static final short PROP_TYPE_JB_USE = 50;

	public static boolean needRefreshBackpack;

	public static final int TICKETCHANGED = 119;

	/** 话费券ID */
	private int huafeiquanID = 403;

	/** 赠送请求 */
	private static final short LSUB_CMD_PRE_PRO_REQ = 804;
	/** 赠送返回 */
	private static final short LSUB_CMD_PRE_PRO_RESP = 805;
	/** 道具主协议 */
	private static final short LS_MDM_PROP = 17;
	/** 赠送成功 */
	private static final int HANDLER_HANDSEL_SUCCESS = 8010;
	/** 赠送失败 */
	private static final int HANDLER_HANDSEL_FAIL = 8011;
	/** 兑换话费券 */
	private static final int GOTO_TICKET = 255;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.backpack_activity);
		MainApplication.sharedApplication().addActivity(this);

		UserInfo userInfo = HallDataManager.getInstance().getUserMe();
		userID = userInfo.userID;
		userToken = userInfo.Token;
		guid = userInfo.guid;
		init();
		CardZoneProtocolListener.getInstance(BackPackActivity.this)
				.requestBackPackList(userID, handler);
		setBackPackShowTag(); // 保存背包new_tag状态
		needRefreshBackpack = true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (needRefreshBackpack) {
			needRefreshBackpack = false;
			if (BackPackItemProvider.getInstance().getBackPageItems() != null) {
				mBackPageAdapter.setList(BackPackItemProvider.getInstance()
						.getBackPageList());
				mBackPageAdapter.notifyDataSetChanged();
			}
			requestPropDateInfo(userID, huafeiquanID);
		}
		AnalyticsUtils.onPageStart(this);
		AnalyticsUtils.onResume(this);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		AnalyticsUtils.onPageEnd(this);
		AnalyticsUtils.onPause(this);
	}

	@Override
	protected void onDestroy(){
		MainApplication.sharedApplication().finishActivity(this);
		super.onDestroy();
	}
	

	private void setBackPackShowTag() {
		UserInfo userInfo = HallDataManager.getInstance().getUserMe();
		String key_tag = userInfo.nickName;
		String tag = Util.getStringSharedPreferences(AppConfig.mContext,
				key_tag, AppConfig.DEFAULT_TAG);
		String[] strs = tag.split("&");
		if (strs != null && strs.length == 3) {
			strs[1] = "1";
			StringBuilder sb = new StringBuilder();
			sb.append(strs[0]).append("&").append(strs[1]).append("&")
					.append(strs[2]);
			Util.setStringSharedPreferences(AppConfig.mContext, key_tag,
					sb.toString());

		}

	}

	private void init() {
		((TextView) findViewById(R.id.tvTitle)).setText(R.string.mygoods);
		findViewById(R.id.tvTitle).setOnClickListener(this);

		progressBackPack = (LinearLayout) findViewById(R.id.progressBackPack);
		lyBackPack = (LinearLayout) findViewById(R.id.lyBackPack);
		btnToMarket = (Button) findViewById(R.id.btnToMarket);
		btnToMarket.setOnClickListener(this);
		ivMix = (ImageView) findViewById(R.id.ivMix);
		ivMix.setOnClickListener(this);


		findViewById(R.id.tvBack).setOnClickListener(this); // 返回按钮

		ivHelp = (ImageView) findViewById(R.id.ivHelp);
		ivHelp.setOnClickListener(this);

		lvBackPage = getListView();
		List<BackPackItem> items = new ArrayList<BackPackItem>();
		mBackPageAdapter = new BackPageAdapter(BackPackActivity.this, items,
				this, this);

	}


	@Override
	public void onItemClick(AdapterView<?> parent, View convertView, int pos,
			long id) {
		BackPackItem item = (BackPackItem) mBackPageAdapter.getItem(pos);
		item.isArrowUp = !item.isArrowUp;
		mBackPageAdapter.notifyDataSetChanged();

		if (pos != 0) {
			lvBackPage.setSelectionFromTop(pos, convertView.getHeight() / 2);
		}
		if (item.isArrowUp) {
			if (BackPackItemProvider.getInstance().getBackPageList().get(pos).id == huafeiquanID) {
				showDetailDialog();
			}
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.tvBack) {
			finish();
		} else if (id == R.id.btnToMarket) {
			// 去商城c
			onGotoMarket();
			finish();
		} else if (id == R.id.ivMix) {
			// 去合成
			startActivity(new Intent(this, MixGridActivity.class));

		} else if (id == R.id.ivHelp) {
			short urlid = 404;
			gotoWebView(urlid);
		} else if (id == R.id.tvTitle) {
			// 显示当前顶部信息，按键声音
			if (lvBackPage != null && lvBackPage.getAdapter() != null) {
				if (lvBackPage.getAdapter().getCount() > 0)
					lvBackPage.setSelection(0);
			}
		}
	}

	// 使用接口回调
	@Override
	public void invoke(BackPackItem item) {

		final int YUBAO = 402;
		if (item.id == YUBAO) { // 元宝
			// 跳转到wap
			short urlId = 20006;
			showWebView(urlId);

		} else if (item.urlId != 0) {
			showWebView(item.urlId);
		} else {
			if ((item.Attribute[3] & 1) != 0) {
				// 可合成
				startActivity(new Intent(this, MixGridActivity.class));
			} else {
				// 使用按钮调用协议
				int exData = 0; // 附加参数（如需要GameID、RoomID的道具，可传入相关数据，默认为0）
				if (((item.Attribute[1] >> GAME_OBJ) & 1) == 1) {
					exData = AppConfig.gameId;
				}
				final long CLISEC = 0;
				final byte CBTYPE = 0;
				requestUsePack(userID, item.id, item.IndexID, exData, CLISEC,
						CBTYPE);
			}
		}
	}

	private void showDetailDialog() {
		// 显示话费券详细日期
		DateDetailInfo[] dateInfo = DateDetailInfo.getDateDetailInfo();
		boolean noDate = (dateInfo == null);
		boolean allPermanent = (null != dateInfo)
				&& (dateInfo.length == 1)
				&& (dateInfo[0] != null)
				&& (dateInfo[0].deadText != null)
				&& (dateInfo[0].deadText.equals(this.getResources().getString(
						R.string.package_detail_forever)));
		if (!(noDate || allPermanent)) {
			DetailDateDialog dialog = new DetailDateDialog(this);
			dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.show();
		}
	}

	private void showWebView(short urlId) {
		String muserToken = "";
		try {
			muserToken = URLEncoder.encode(userToken, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String url = CenterUrlHelper.getWapUrl(urlId);
		url += "at=" + muserToken + "&";
		String finalUrl = CenterUrlHelper.getUrl(url, userID);

		UtilHelper.onWeb(BackPackActivity.this, finalUrl,
				new OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						// 刷新UI
						CardZoneProtocolListener.getInstance(
								BackPackActivity.this).requestBackPackList(
								userID, handler);
					}
				});
	}

	/***
	 * @Title: gotoWebView
	 * @Description:帮助界面
	 * @param urlId
	 * @version: 2012-7-30 下午06:33:25
	 */
	private void gotoWebView(short urlId) {
		String muserToken = "";
		try {
			muserToken = URLEncoder.encode(userToken, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String url = CenterUrlHelper.getWapUrl(urlId);
		url += "at=" + muserToken + "&";
		String finalUrl = CenterUrlHelper.getUrl(url, userID);

		UtilHelper.onWeb(BackPackActivity.this, finalUrl);
	}

	/**
	 * @Title: onGotoMarket
	 * @Description: 跳转到商城
	 * @param userID
	 *            用户ID
	 * @param token
	 *            用户token串
	 * @param channelId
	 *            （ChannelID格式为 主渠道#子渠道。 如080#0000。 智运会2期修改 2012-02-01。
	 *            注：服务器需兼容旧的格式）
	 * @param move_mobile_key
	 *            如：PlatID=3，则Key为移动用户伪码key长度（来自移动）
	 * @version: 2012-7-24 下午03:08:11
	 */
	private void onGotoMarket() {
		Intent intent = new Intent(this, MarketActivity.class);
		startActivity(intent);
	}

	/****
	 * 定义一个Handler处理线程发送的消息，并更新主UI线程
	 */
	@SuppressLint("HandlerLeak")
	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case CardZoneProtocolListener.HANDLER_PACK_QUERY_SUCCESS: // 我的物品成功
				List<BackPackItem> items = BackPackItemProvider.getInstance()
						.getBackPageList();

				if (items.size() > 0) {
					items.get(0).isArrowUp = true;
					lvBackPage.setOnItemClickListener(BackPackActivity.this);
					/*** 2013-1-16 **/
					mBackPageAdapter.setList(items);
				}
				if ((BackPackItemProvider.getInstance().getBackPackItem(
						huafeiquanID) != null)
						&& DateDetailInfo.getDateDetailInfo() == null) {
					requestPropDateInfo(userID, huafeiquanID);
				}

//				lvBackPage.setAdapter(mBackPageAdapter);
				setListAdapter(mBackPageAdapter);
				mBackPageAdapter.notifyDataSetChanged();

				// 不显示进度条
				progressBackPack.setVisibility(View.GONE);
				// 不显示提示商城
				lyBackPack.setVisibility(View.GONE);

				break;

			case CardZoneProtocolListener.HANDLER_PACK_QUERY_SUCCESS_NODATA:
				/** 我的物品列表没有数据 **/
				BackPackItemProvider.getInstance().init();
				mBackPageAdapter.setList(BackPackItemProvider.getInstance()
						.getBackPageList());
//				lvBackPage.setAdapter(mBackPageAdapter);
				setListAdapter(mBackPageAdapter);
				if (BackPackItemProvider.getInstance().getBackPageItems().length <= 0) {
					// 不显示进度条
					progressBackPack.setVisibility(View.GONE);
					lyBackPack.setVisibility(View.VISIBLE);
				}
				break;

			case HANDLER_BACKPACK_USE_SUCCESS: // 使用成功
				UtilHelper.showCustomDialog(BackPackActivity.this,
						msg.obj.toString());
				// Toast.makeText(getApplication(), msg.obj.toString(),
				// Toast.LENGTH_LONG).show();
				// 刷新UI
				CardZoneProtocolListener.getInstance(BackPackActivity.this)
						.requestBackPackList(userID, handler);
				break;

			case HANDLER_BACKPACK_CBTYPE_ZERO: // 使用成功

				Toast.makeText(getApplication(), msg.obj.toString(),
						Toast.LENGTH_LONG).show();

				break;

			case HANDLER_GETTICKET_ACTIVITY:
				startGetTicketActivity();
				break;
			case REFLASH_LISTVIEW: // 下载图片完成后更新ListView
				if (mBackPageAdapter != null)
					mBackPageAdapter.notifyDataSetChanged();// 刷新UI
				break;

			case REFLASH_LISTVIEW_FAIL:
				Log.e(TAG, "道具图片文件下载失败，错误码为：" + msg.arg1);
				break;

			case HANDLER_SHIWU_INFO:
				Toast.makeText(getApplication(), msg.obj.toString(),
						Toast.LENGTH_LONG).show();
				break;

			case HANDLER_HANDSEL_SUCCESS: // 赠送成功
				String fileStr = "";
				try {
					inputStream = getBaseContext().openFileInput("myfile.txt");
					// inputStream.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					Log.e(TAG, "缓存清除");
					e.printStackTrace();
				}
				if (null != inputStream) {
					fileStr = UtilHelper.convertStreamToString(inputStream);
					try {
						inputStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (fileStr.indexOf(HandselDialog.handselId) < 0) {
					try {
						outStream = openFileOutput("myfile.txt",
								Context.MODE_APPEND);
						outStream.write((HandselDialog.handselId + ",")
								.getBytes());
						outStream.close();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				if (null != dialog && dialog.isShowing()) {
					dialog.dismiss();
				}
				Resources resource = BackPackActivity.this.getResources();
				int num = msg.arg2;
				SimpleDateFormat dateFormat = new SimpleDateFormat(
						"yyyy-MM-dd HH:mm");
				String date = dateFormat.format(new java.util.Date());
				AppConfig.personInfoList.add(resource
						.getString(R.string.package_zenfsong_lable_1)
						+ date
						+ resource.getString(R.string.package_zenfsong_lable_2)
						+ HandselDialog.handselId
						+ resource.getString(R.string.package_zenfsong_lable_3)
						+ num
						+ resource.getString(R.string.package_zenfsong_lable_4)
						+ handselName);
				Toast.makeText(getApplication(), msg.obj.toString(),
						Toast.LENGTH_SHORT).show();
				CardZoneProtocolListener.getInstance(BackPackActivity.this)
						.requestBackPackList(userID, handler);
				// 刷新消息盒子
				break;

			case HANDLER_HANDSEL_FAIL: // 赠送失败
				Toast.makeText(getApplication(), msg.obj.toString(),
						Toast.LENGTH_SHORT).show();
				break;

			case HANDLER_DATE_QUERY_SUCCESS:
				needShowDateDialog = false;
				showDateDialog();
				break;

			default:
				break;
			}
		}
	};

	/**
	 * @Title: getSec
	 * @Description: 获取客户端标识 sec[0]为cbOperateType sec[1]为dwTypeData sec[2]为resve
	 * @param cliSec
	 * @return
	 * @version: 2012-7-25 下午02:07:26
	 */
	/*
	 * public static int[] getSec(long cliSec) { int[] sec = new int[3]; sec[0]
	 * = (int) (cliSec & 0xffff); sec[1] = (int) ((cliSec >> 16) & 0xffffffff);
	 * sec[2] = (int) ((cliSec >> 48) & 0xffff); return sec; }
	 */

	// --------------------------------------消息------------------------------------------------

	private void initDateDialog() {
		// TODO Auto-generated method stub
		List<String> descList = new ArrayList<String>();
		DateDetailInfo[] detailInfos = DateDetailInfo.getDateDetailInfo();
		int index = 1;
		for (int i = 0; i < detailInfos.length; i++) {
			if ((null != detailInfos[i]) && (null != detailInfos[i].descText)) {
				descList.add(index + "." + detailInfos[i].descText);
				index++;
			}
		}
		descList.add(this.getResources()
				.getString(R.string.package_duihuan_tip));
		DateDialog.mListStr = descList.toArray(new String[descList.size()]);
	}

	private void showDateDialog() {
		// TODO Auto-generated method stub
		DateDetailInfo[] dateInfo = DateDetailInfo.getDateDetailInfo();
		boolean noDate = (dateInfo == null);
		boolean allPermanent = (null != dateInfo)
				&& (dateInfo.length == 1)
				&& (dateInfo[0] != null)
				&& (dateInfo[0].deadText != null)
				&& (dateInfo[0].deadText.equals(this.getResources().getString(
						R.string.package_detail_forever)));
		if (DateDetailInfo.dayOffMax > 0 && (!(noDate || allPermanent))) {
			initDateDialog();
			DateDialog dialog = new DateDialog(this);
			dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setListener(new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					startGetTicketActivity();
				}
			});
			if (null != DateDialog.mListStr && DateDialog.mListStr.length > 1) {
				dialog.show();
			}
		}
	}

	/** 道具过期信息请求 */
	static final short LSUB_CMD_USER_PRO_EXPIRED_REQ = 806;

	/** 道具过期信息接收 */
	static final short LSUB_CMD_USER_PRO_EXPIRED_RESP = 807;

	DateDetailInfo[] dateInfos;

	int currReturnDateNum;

	protected boolean needShowDateDialog = true;

	/** 使用SharedPreferences 来储存当前日期判断是否弹框 **/
	SharedPreferences mShared = null;

	public final static String SHARED_MAIN = "prop_date";

	static final int HANDLER_DATE_QUERY_SUCCESS = 332;

	private void requestPropDateInfo(final int userID, final int propID) {
		// TODO Auto-generated method stub
		// 创建发送的数据包
		dateInfos = null;
		TDataOutputStream tdos = new TDataOutputStream(false);
		tdos.writeInt(userID);
		tdos.writeInt(propID);
		NetSocketPak pointBalance = new NetSocketPak(MDM_PROP,
				LSUB_CMD_USER_PRO_EXPIRED_REQ, tdos);
		// 定义接受数据的协议
		short[][] parseProtocol = { { MDM_PROP, LSUB_CMD_USER_PRO_EXPIRED_RESP } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			public boolean doReceive(NetSocketPak netSocketPak) {
				// 解析接受到的网络数据
				try {
					TDataInputStream tdis = netSocketPak.getDataInputStream();
					tdis.setFront(false);

					int total = tdis.readShort();
					int num = tdis.readShort();
					DateDetailInfo.currDate = tdis.readInt();
					DateDetailInfo.dayOffMax = tdis.readShort();
					if (num <= 0) {
						setOnlyRun(true);
					} else {
						if (dateInfos == null && currReturnDateNum == 0) { // 尚未有数组对象情况下
							dateInfos = new DateDetailInfo[total];
						}
						// 累计接受到数据到数组中
						for (int i = 0; i < num; i++) {
							if (currReturnDateNum + i < dateInfos.length) {
								dateInfos[currReturnDateNum + i] = new DateDetailInfo(
										tdis, BackPackActivity.this);
								if (!(dateInfos[currReturnDateNum + i].propId
										.equals(String.valueOf(propID)))) {
									dateInfos[currReturnDateNum + i] = null;
								}
							}
						}
						currReturnDateNum += num; // 积累保存到全局变量，记录当前返回累计数目
						if (currReturnDateNum >= total) {
							// 读取完毕，交与主线程显示（同时恢复变量）
							setOnlyRun(true);
							currReturnDateNum = 0;
							boolean hasData = false; // 是否有有效数据
							if (dateInfos != null)
								for (int i = 0; i < dateInfos.length; i++) {
									if (dateInfos[i] != null) {
										hasData = true;
										break;
									}
								}
							if (!hasData) { // 没有有效数据
								dateInfos = null;
								return true;
							}
							DateDetailInfo.isDateDetailRefresh = false;
							DateDetailInfo.setDateDetailInfo(dateInfos);
							/** 开始保存入SharedPreferences **/
							mShared = getSharedPreferences(SHARED_MAIN,
									Context.MODE_PRIVATE);
							boolean isNotShownToday;
							isNotShownToday = !mShared.getString(
									String.valueOf(userID), "00000000").equals(
									String.valueOf(DateDetailInfo.currDate));
							if (needShowDateDialog && isNotShownToday) {
								handler.sendMessage(handler
										.obtainMessage(HANDLER_DATE_QUERY_SUCCESS));
								Editor editor = mShared.edit();
								editor.putString(String.valueOf(userID),
										String.valueOf(DateDetailInfo.currDate));
								/** put完毕必需要commit()否则无法保存 **/
								editor.commit();
							}
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
				// 数据处理完成，终止继续解析
				return true;
			}
		};

		nPListener.setOnlyRun(false);
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		// 发送协议
		NetSocketManager.getInstance().sendData(pointBalance);
		// 清理协议对象
		pointBalance.free();
	}

	/** 下载图片成功后更新UI */
	public static final int REFLASH_LISTVIEW = 20000;

	/** 下载图片失败后更新UI */
	public static final int REFLASH_LISTVIEW_FAIL = 20001;

	/** 新道具使用成功 */
	public static final int HANDLER_BACKPACK_USE_SUCCESS = 20;
	/** 新道具使用失败 */
	public static final int HANDLER_BACKPACK_USE_FAIL = 30;
	/** 新道具使用异常 */
	public static final int HANDLER_FAIL = 40;
	/** 启动话费券兑换 */
	public static final int HANDLER_GETTICKET_ACTIVITY = 80;
	/** 启动充值卡兑换 */
	// public static final int HANDLER_RECHARGE_ACTIVITY = 90;
	/** 实物道具使用 */
	public static final int HANDLER_SHIWU_INFO = 100;

	public static final int HANDLER_BACKPACK_CBTYPE_ZERO = 50;

	// ----------------------------------协议----------------------------------------------
	/** 主协议 */
	static final short MDM_PROP = 17;
	/** 子协议-新道具使用请求协议 */
	static final short MSUB_CMD_USE_PROP_REQ = 780;
	/** 子协议-新道具使用返回 */
	static final short MSUB_CMD_USE_PROP_RESP = 781;

	/**
	 * @Title: requestUsePack
	 * @Description: 使用背包物品请求(这里用一句话描述这个方法的作用)
	 * @param userID
	 * @param propID
	 * @param indexID
	 * @param exData
	 * @param cliSec
	 * @param cbType
	 * @param extXml
	 * @version: 2012-7-25 上午11:43:34
	 */
	private void requestUsePack(final int userID, int propID, long indexID,
			int exData, long cliSec, byte cbType) {

		// 创建发送的数据包
		TDataOutputStream tdos = new TDataOutputStream(false);
		tdos.writeInt(userID);
		tdos.writeInt(propID);
		tdos.writeLong(indexID);
		tdos.writeInt(exData);
		tdos.writeLong(cliSec);
		tdos.writeByte(cbType);

		NetSocketPak useGoods = new NetSocketPak(MDM_PROP,
				MSUB_CMD_USE_PROP_REQ, tdos);
		// 定义接受数据的协议
		short[][] parseProtocol = { { MDM_PROP, MSUB_CMD_USE_PROP_RESP } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {

			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				TDataInputStream tdis = netSocketPak.getDataInputStream();
				propBack = new HPropUseBack(tdis);

				int result = propBack.result;
				Message msg = handler.obtainMessage();
				msg.obj = propBack.msg;
				int type = propBack.type;

				if (result != HPropUseBack.TYPE_RESULT_DATA) {
					msg.what = HANDLER_BACKPACK_USE_SUCCESS;
				} else if (result == HPropUseBack.TYPE_RESULT_DATA) {// 这里有话费券、充值卡使用
					switch (type) {
					case HPropUseBack.TYPE_RESULT_CHONGZHIKA:
					case HPropUseBack.TYPE_RESULT_HUAFEIQUAN:
						msg.what = HANDLER_GETTICKET_ACTIVITY;
						break;
					case HPropUseBack.TYPE_RESULT_SHIWU:
						msg.what = HANDLER_SHIWU_INFO;
						break;

					default:
						msg.what = HANDLER_BACKPACK_CBTYPE_ZERO;
						break;
					}
				}
				handler.sendMessage(msg);
				return true;
			}

		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		// 发送协议
		NetSocketManager.getInstance().sendData(useGoods);
		// 清理协议对象
		useGoods.free();
	}

	/***
	 * @Title: requestHandsel
	 * @Description: TODO发起赠送请求
	 * @version: 2012-10-29 下午03:50:10
	 */
	public void requestHandsel(final long handselId, int propId,
			final short num, final String name) {

		// 创建发送的数据包
		TDataOutputStream tdos = new TDataOutputStream(false);
		tdos.writeLong(handselId);
		tdos.writeInt(propId);
		tdos.writeShort(num);

		NetSocketPak pointBalance = new NetSocketPak(LS_MDM_PROP,
				LSUB_CMD_PRE_PRO_REQ, tdos);
		// 定义接受数据的协议
		short[][] parseProtocol = { { LS_MDM_PROP, LSUB_CMD_PRE_PRO_RESP } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			public boolean doReceive(NetSocketPak netSocketPak) {
				// 解析接受到的网络数据
				try {
					TDataInputStream tdis = netSocketPak.getDataInputStream();
					tdis.setFront(false);

					short ret = tdis.readShort();
					String msg = tdis.readUTFShort();
					Log.e(TAG, msg);
					if (ret == 0) {
						handselName = name;
						Message message = new Message();
						message.what = HANDLER_HANDSEL_SUCCESS;
						message.obj = msg;
						message.arg2 = num;
						handler.sendMessage(message);
					} else {
						handler.sendMessage(handler.obtainMessage(
								HANDLER_HANDSEL_FAIL, msg));
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
				// 数据处理完成，终止继续解析
				return true;
			}
		};

		// nPListener.setOnlyRun(false);
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		// 发送协议
		NetSocketManager.getInstance().sendData(pointBalance);
		// 清理协议对象
		pointBalance.free();
	}

	private void startGetTicketActivity() {
		Intent getTicket = new Intent(getApplication(), GetTicketActivity.class);
		startActivityForResult(getTicket, GOTO_TICKET);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if ((requestCode == GOTO_TICKET)
				&& (resultCode == GetTicketActivity.TICKET_CHANGED)) {
			CardZoneProtocolListener.getInstance(BackPackActivity.this)
					.requestBackPackList(userID, handler);
			Log.e(TAG, "话费券使用");
			if (DateDetailInfo.isDateDetailRefresh) {
				requestPropDateInfo(userID, huafeiquanID);
			}
		}
	}

	@Override
	public void invokeHandsel(final BackPackItem item) {
		// TODO Auto-generated method stub
		if (((item.Attribute[3] >> 1) & 1) == 0) {
			dialog = new HandselDialog(BackPackActivity.this, item.backpackName);
			dialog.setListener(new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// 发送赠送
					long handselId = 0;
					short handselNum = 1;
					boolean enable = true;
					Resources resource = BackPackActivity.this.getResources();
					try {
						handselId = Long.parseLong(HandselDialog.handselId);
					} catch (Exception e) {
						// TODO: handle exception
						Toast.makeText(getApplication(),
								resource.getString(R.string.package_id_error),
								Toast.LENGTH_SHORT).show();
						enable = false;
					}
					try {
						handselNum = Short.parseShort(HandselDialog.handselNum);
					} catch (Exception e) {
						// TODO: handle exception
						Toast.makeText(
								getApplication(),
								resource.getString(R.string.package_largess_count_error),
								Toast.LENGTH_SHORT).show();
						enable = false;
					}
					if (guid == handselId) {
						Toast.makeText(
								getApplication(),
								resource.getString(R.string.package_largess_myself_error),
								Toast.LENGTH_SHORT).show();
					} else if (item.newHoldCount >= handselNum && enable
							&& (handselNum != 0)) {
						requestHandsel(handselId, item.id, handselNum,
								item.backpackName);
					} else if (enable) {
						Toast.makeText(
								getApplication(),
								resource.getString(R.string.package_not_enaghe_error),
								Toast.LENGTH_SHORT).show();
					}
				}
			});
			dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.show();
		} else {
			Resources resource = BackPackActivity.this.getResources();
			Toast.makeText(getApplication(),
					resource.getString(R.string.package_binded_error),
					Toast.LENGTH_SHORT).show();
		}
	}


}
