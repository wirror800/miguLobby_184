package com.mykj.andr.ui.fragment;

import java.util.List;

import org.cocos2dx.lib.Cocos2dxEditText;
import org.cocos2dx.lib.Cocos2dxGLSurfaceView;
import org.cocos2dx.lib.Cocos2dxRenderer;
import org.cocos2dx.util.GameUtilJni;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.mingyou.distributor.NetPrivateListener;
import com.mingyou.distributor.NetSocketPak;
import com.mykj.andr.model.HallDataManager;
import com.mykj.andr.model.RoomData;
import com.mykj.andr.net.NetSocketManager;
import com.mykj.andr.ui.MMVideoBuyDialog;
import com.mykj.andr.ui.widget.Interface.OnArticleSelectedListener;
import com.mykj.comm.io.TDataInputStream;
import com.mykj.game.FiexedViewHelper;
import com.MyGame.Migu.R;
import com.mykj.game.ddz.api.AnalyticsUtils;
import com.mykj.game.ddz.api.UC;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.CenterUrlHelper;
import com.mykj.game.utils.HalfWebDialog;
import com.mykj.game.utils.Log;
import com.mykj.game.utils.ModiyNickNameDialog;
import com.mykj.game.utils.Util;

public class Cocos2dxFragment extends FragmentModel {
	public static final String TAG = "Cocos2dxFragment";

	// private static final long DEFAULT_ROOMID=1080; //默认房间ID

	private Activity mAct;
	private Cocos2dxEditText mEditText;
	public OnArticleSelectedListener mListener;
	public Cocos2dxGLSurfaceView mGLView = null;

	/** 电量+信号 **/
	private TelephonyManager TelManager;

	private PhoneSignalListener PhoneSignal;
	private static boolean isPhoneSignalDestory = false;

	private Cocos2dHandler mHandler;

	public final static int HANDLER_MMVIDEO_HELP = 100;
	public final static int HANDLER_ENTRY_MMROOM = 101;
	public final static int HANDLER_ENTRY_ONRUME = 102;
	public final static int HANDLER_MODIY_NICKNAME_START = 103;
	public final static int HANDLER_MODIY_NICKNAME_SUCCESS = 104;
	public final static int HANDLER_MODIY_NICKNAME_FAIL = 105;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mAct = activity;
		try {
			mListener = (OnArticleSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnArticleSelectedListener");
		}
	}

	/**
	 * When creating, retrieve this instance's number from its arguments.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		telPhontLisntener();
		// listenerModifyNickBean();
		if (mHandler == null) {
			mHandler = new Cocos2dHandler();
		}
	}

	/**
	 * The Fragment's UI is just a simple text view showing its instance number.
	 */

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.cocos2d_layout, container, false);

		mGLView = (Cocos2dxGLSurfaceView) view.findViewById(R.id.surface_view);
		mEditText = (Cocos2dxEditText) view.findViewById(R.id.cocos2d_input);

		mGLView.setTextField(mEditText);

		RoomData roomData = HallDataManager.getInstance().getCurrentRoomData();
		final int playId = roomData.playId;
		Log.v(TAG, "game playId=" + playId);

		mGLView.mRenderer
				.setCococs2dxRendererDisplay(new Cocos2dxRenderer.Cococs2dxRendererDisplay() {
					public void onDisplay() {
						FiexedViewHelper.getInstance().gotoGame(playId); // 正常首次进入
						mGLView.onResume();
						FiexedViewHelper.getInstance().sHandler
								.sendEmptyMessage(FiexedViewHelper.CLEAR_COCOS2D_BACKGOUND);
					}
				});

		return view;
	}

	@SuppressLint("HandlerLeak")
	public final class Cocos2dHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case HANDLER_MMVIDEO_HELP:
				int urlId = msg.arg1;
				if (urlId != 0) {
					int userId = FiexedViewHelper.getInstance().getUserId();
					String userToken = FiexedViewHelper.getInstance()
							.getUserToken();

					String url = CenterUrlHelper.getWapUrl(urlId);
					url += "at=" + userToken + "&";

					String finalUrl = CenterUrlHelper.getUrl(url, userId);

					HalfWebDialog webDialog = new HalfWebDialog(mAct, finalUrl);
					webDialog.show();
				}
				break;
			case HANDLER_ENTRY_MMROOM:
				break;
			case HANDLER_ENTRY_ONRUME:
				if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
					mGLView.onResume();
				} else {
					mHandler.sendEmptyMessageDelayed(HANDLER_ENTRY_ONRUME, 100);
				}
				break;
			case HANDLER_MODIY_NICKNAME_START:
				ModiyNickNameDialog modiyDialog = new ModiyNickNameDialog(mAct,
						mHandler, mGold);
				modiyDialog.show();
				AnalyticsUtils.onClickEvent(mAct, UC.EC_276);
				break;
			case HANDLER_MODIY_NICKNAME_SUCCESS:

				break;
			case HANDLER_MODIY_NICKNAME_FAIL:
				Bundle bundle = msg.getData();
				String content = bundle.getString("content");

				if (!Util.isEmptyStr(content)) {
					Toast.makeText(mAct, content, Toast.LENGTH_SHORT).show();
				}

				break;
			default:
				break;
			}
		}

	}

	/** 请求修改主协议 */
	public static final short MDM_VIDEO_GAMEFRAME = 101;
	/** 修改昵称需要多少金币 */
	private static final short SUB_GF_NICK_GOLD_INFO = 1007;

	private int mGold = 0;

	private void listenerModifyNickBean() {
		// 定义接受数据的协议
		short[][] parseProtocol = { { MDM_VIDEO_GAMEFRAME,
				SUB_GF_NICK_GOLD_INFO } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			public boolean doReceive(NetSocketPak netSocketPak) {
				// 解析接受到的网络数据
				TDataInputStream tdis = netSocketPak.getDataInputStream();
				tdis.setFront(false);
				mGold = tdis.readInt();
				return true;
			}
		};

		nPListener.setOnlyRun(true);
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
	}

	public Handler getHandler() {
		return mHandler;
	}

	@Override
	public void onResume() {
		super.onResume();
		mHandler.sendEmptyMessage(HANDLER_ENTRY_ONRUME);// mGLView.onResume();
														// //延时onresume
	}

	@Override
	public void onPause() {
		super.onPause();
		mGLView.onPause();

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mEditText.setVisibility(View.GONE);
		destroyPhontLisntener();
		mGLView = null;
		mHandler = null;
	}

	@Override
	public int getFragmentTag() {
		// TODO Auto-generated method stub
		return FiexedViewHelper.COCOS2DX_VIEW;
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		mGLView.onKeyDown(KeyEvent.KEYCODE_BACK, null);
	}

	/**
	 * 监听手机信号和电量
	 */
	private void telPhontLisntener() {

		/** 电量监听 ***/
		mAct.registerReceiver(mBatInfoReceiver, new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED));
		/** 网络变化监听 */
		mAct.registerReceiver(mConnectionChangeReceiver, new IntentFilter(
				ConnectivityManager.CONNECTIVITY_ACTION));

		// registerReceiver(mWifiRssiReceiver, new
		// IntentFilter(WifiManager.RSSI_CHANGED_ACTION));
	}

	/**
	 * 取消监听
	 */
	private void destroyPhontLisntener() {
		mAct.unregisterReceiver(mBatInfoReceiver);
		mAct.unregisterReceiver(mConnectionChangeReceiver);
		if (isPhoneSignalDestory && TelManager != null && PhoneSignal != null) {
			TelManager.listen(PhoneSignal, PhoneStateListener.LISTEN_NONE);
		}

	}

	/**
	 * 电量监听
	 * */
	private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
				final int level = intent.getIntExtra(
						BatteryManager.EXTRA_LEVEL, -1);
				if (level != -1) {
					AppConfig.phonePower = level;
					GameUtilJni.updateSystemEvent(
							GameUtilJni.SystemEvent_PhonePower, level);
				}
			}
		}
	};

	/**
	 * 网络状态监听器
	 */
	private BroadcastReceiver mConnectionChangeReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(
					ConnectivityManager.CONNECTIVITY_ACTION)) {
				ConnectivityManager connectivity = (ConnectivityManager) mAct
						.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo info = connectivity.getActiveNetworkInfo();
				if (info != null) {
					int apn = info.getType();
					AppConfig.phoneApnType = apn;
					if (apn == ConnectivityManager.TYPE_WIFI) {
						int level = getWifiLevel();

						AppConfig.phoneSignal = level;
						GameUtilJni.updateSystemEvent(
								GameUtilJni.SystemEvent_PhoneSignal, level);
					} else {
						/*** 信号监听 ***/
						PhoneSignal = new PhoneSignalListener();
						TelManager = (TelephonyManager) mAct
								.getSystemService(Context.TELEPHONY_SERVICE);
						TelManager.listen(PhoneSignal,
								PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
						isPhoneSignalDestory = true;
					}

					GameUtilJni.updateSystemEvent(
							GameUtilJni.SystemEvent_NetType, apn);
				}

			}
		}
	};

	/**
	 * 获取wifi信号水平
	 * 
	 * @return
	 */
	private int getWifiLevel() {
		int level;
		int rssi;
		WifiManager wifiManager = (WifiManager) mAct
				.getSystemService(Context.WIFI_SERVICE);
		final WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		rssi = wifiInfo.getRssi();

		if (rssi < -100 || rssi > 0)
			level = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
		else if (rssi >= -50)
			level = SIGNAL_STRENGTH_GREAT;
		else if (rssi >= -70)
			level = SIGNAL_STRENGTH_GOOD;
		else if (rssi >= -90)
			level = SIGNAL_STRENGTH_MODERATE;
		else
			level = SIGNAL_STRENGTH_POOR;
		Log.v(TAG, "wifi信号强度=" + level);
		return level;
	}

	public static final int SIGNAL_STRENGTH_NONE_OR_UNKNOWN = 0;
	public static final int SIGNAL_STRENGTH_POOR = 1;
	public static final int SIGNAL_STRENGTH_MODERATE = 2;
	public static final int SIGNAL_STRENGTH_GOOD = 3;
	public static final int SIGNAL_STRENGTH_GREAT = 4;

	/**
	 * 内部类---手机信号监听
	 * 
	 * @author Administrator
	 * 
	 */
	private class PhoneSignalListener extends PhoneStateListener {
		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			super.onSignalStrengthsChanged(signalStrength);
			int level;
			if (AppConfig.phoneApnType != ConnectivityManager.TYPE_WIFI) {
				if (signalStrength.isGsm()) {
					level = getGsmLevel(signalStrength); // gsm信号强度
				} else {
					int cdmaLevel = getCdmaLevel(signalStrength); // 联通3G信号强度
					int evdoLevel = getEvdoLevel(signalStrength); // 电信3G信号强度

					if (evdoLevel == SIGNAL_STRENGTH_NONE_OR_UNKNOWN) {
						/* We don't know evdo, use cdma */
						level = getCdmaLevel(signalStrength);
					} else if (cdmaLevel == SIGNAL_STRENGTH_NONE_OR_UNKNOWN) {
						/* We don't know cdma, use evdo */
						level = getEvdoLevel(signalStrength);
					} else {
						/* We know both, use the lowest level */
						level = cdmaLevel < evdoLevel ? cdmaLevel : evdoLevel;
					}
				}

				AppConfig.phoneSignal = level;
				GameUtilJni.updateSystemEvent(
						GameUtilJni.SystemEvent_PhoneSignal, level);
			}
		}

		/**
		 * GSM信号强度
		 * 
		 * @param signalStrength
		 * @return
		 */
		private int getGsmLevel(SignalStrength signalStrength) {
			int level;
			int asu = signalStrength.getGsmSignalStrength();
			if (asu <= 2 || asu == 99)
				level = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;
			else if (asu >= 12)
				level = SIGNAL_STRENGTH_GREAT;
			else if (asu >= 8)
				level = SIGNAL_STRENGTH_GOOD;
			else if (asu >= 5)
				level = SIGNAL_STRENGTH_MODERATE;
			else
				level = SIGNAL_STRENGTH_POOR;
			Log.v(TAG, "GSM信号强度=" + level);
			return level;
		}

		/**
		 * 联通3G信号强度
		 * 
		 * @param signalStrength
		 * @return
		 */
		private int getCdmaLevel(SignalStrength signalStrength) {
			final int cdmaDbm = signalStrength.getCdmaDbm();
			final int cdmaEcio = signalStrength.getCdmaEcio();
			int levelDbm;
			int levelEcio;

			if (cdmaDbm >= -75)
				levelDbm = SIGNAL_STRENGTH_GREAT;
			else if (cdmaDbm >= -85)
				levelDbm = SIGNAL_STRENGTH_GOOD;
			else if (cdmaDbm >= -95)
				levelDbm = SIGNAL_STRENGTH_MODERATE;
			else if (cdmaDbm >= -100)
				levelDbm = SIGNAL_STRENGTH_POOR;
			else
				levelDbm = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;

			// Ec/Io are in dB*10
			if (cdmaEcio >= -90)
				levelEcio = SIGNAL_STRENGTH_GREAT;
			else if (cdmaEcio >= -110)
				levelEcio = SIGNAL_STRENGTH_GOOD;
			else if (cdmaEcio >= -130)
				levelEcio = SIGNAL_STRENGTH_MODERATE;
			else if (cdmaEcio >= -150)
				levelEcio = SIGNAL_STRENGTH_POOR;
			else
				levelEcio = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;

			int level = (levelDbm < levelEcio) ? levelDbm : levelEcio;
			Log.v(TAG, "电信3G信号强度=" + level);
			return level;
		}

		/**
		 * 电信3G信号强度
		 * 
		 * @param signalStrength
		 * @return
		 */
		private int getEvdoLevel(SignalStrength signalStrength) {
			int evdoDbm = signalStrength.getEvdoDbm();
			int evdoSnr = signalStrength.getEvdoSnr();
			int levelEvdoDbm;
			int levelEvdoSnr;

			if (evdoDbm >= -65)
				levelEvdoDbm = SIGNAL_STRENGTH_GREAT;
			else if (evdoDbm >= -75)
				levelEvdoDbm = SIGNAL_STRENGTH_GOOD;
			else if (evdoDbm >= -90)
				levelEvdoDbm = SIGNAL_STRENGTH_MODERATE;
			else if (evdoDbm >= -105)
				levelEvdoDbm = SIGNAL_STRENGTH_POOR;
			else
				levelEvdoDbm = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;

			if (evdoSnr >= 7)
				levelEvdoSnr = SIGNAL_STRENGTH_GREAT;
			else if (evdoSnr >= 5)
				levelEvdoSnr = SIGNAL_STRENGTH_GOOD;
			else if (evdoSnr >= 3)
				levelEvdoSnr = SIGNAL_STRENGTH_MODERATE;
			else if (evdoSnr >= 1)
				levelEvdoSnr = SIGNAL_STRENGTH_POOR;
			else
				levelEvdoSnr = SIGNAL_STRENGTH_NONE_OR_UNKNOWN;

			int level = (levelEvdoDbm < levelEvdoSnr) ? levelEvdoDbm
					: levelEvdoSnr;
			Log.v(TAG, "电信3G信号强度=" + level);
			return level;
		}
	}

	// ---------------------内部类手机信号监听-------------------------------------------------

}