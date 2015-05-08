package com.mykj.game.moregame;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mykj.andr.ui.widget.SysPopDialog;
import com.MyGame.Midlet.R;
import com.mykj.game.moregame.MoreGameManager.DownloadThread;
import com.mykj.game.utils.Log;
import com.mykj.game.utils.Toast;
import com.mykj.game.utils.Util;
import com.mykj.game.utils.UtilHelper;

public class MoregameActivity extends Activity {
	private static final String TAG = "MoregameActivity";

	private Context mContext;

	private Resources mResource;

	public MoreGameManager myManager = null;

	private ListView mList;

	private MoreGameListAdapter mListAdapter;

	private ProgressBar progress_gird;

	public static final int GET_GAME_VERSION_SUCCESS = 1; // 获取游戏列表信息成功
	public static final int GET_GAME_VERSION_FAIL = 2; // 获取游戏列表信息失败
	public static final int GET_GAME_VERSION_NO_NODE = 5; // 领取游戏列表为空
	public static final int LIST_BUTTON_CLICK = 3; // 列表按钮点击
	public static final int REWARD_FEEDBACK = 4; // 领取奖励回馈
	public static final int DOWNLOAD_FINISH_BACK = 6; // 下载完成回馈

	public static final int CLICK_TYPE_DOWNLOAD = 11;
	public static final int CLICK_TYPE_CONTINUE = 12;
	public static final int CLICK_TYPE_PAUSE = 13;
	public static final int CLICK_TYPE_INSTALL = 14;
	public static final int CLICK_TYPE_OPEN = 15;
	public static final int CLICK_TYPE_REWARD = 16;
	public static final int CLICK_TYPE_WEB = 17;

	/**
	 * service 通知Main UI handler
	 */
	public Handler mGameVersionHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Log.v(TAG, "mGameVersionHandler , msg.what=" + msg.what);
			switch (msg.what) {
			// 获取游戏列表信息成功
			case GET_GAME_VERSION_SUCCESS:
				List<AppVersion> gamesList = myManager.getGamesConfig();
				listViewGameInit(gamesList);
				break;
			// 获取游戏列表信息失败
			case GET_GAME_VERSION_FAIL: {
				SysPopDialog dlg = new SysPopDialog(
						mContext,
						mResource.getString(R.string.ddz_retry),
						mResource.getString(R.string.Cancel),
						mResource
								.getString(R.string.ddz_obtain_server_info_failed),
						null, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								if (which == SysPopDialog.CONFIRM) {
									myManager
											.getServerInfo(mGameVersionHandler);
								} else if (which == SysPopDialog.CANCEL) {
									MoregameActivity.this.finish();
								}
							}
						}, false);
				dlg.setCancelable(false);
				dlg.show();
			}
				break;
			case LIST_BUTTON_CLICK: {

				clickHandle(msg);

			}
				break;
			case REWARD_FEEDBACK:
				rewardAnswer(msg.arg1, msg.arg2);
				break;
			case GET_GAME_VERSION_NO_NODE: {
				SysPopDialog dlg = new SysPopDialog(
						mContext,
						mResource.getString(R.string.Ensure),
						null,
						mResource
								.getString(R.string.ddz_function_not_available),
						null, new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								if (which == SysPopDialog.CONFIRM) {
									MoregameActivity.this.finish();
								}
							}
						}, false);
				dlg.setCancelable(false);
				dlg.show();
			}
				break;
			case DOWNLOAD_FINISH_BACK:
				// do nothing
				AppVersion item = mListAdapter.getItemById(msg.arg2);
				if (item != null && item.isWap()
						&& msg.arg1 == MoreGameManager.REWARD_SUCCESS) {
					item.setNotifyed();
					mListAdapter.notifyDataSetChanged();
				}
				break;
			default:
				break;
			}
		}

	};

	private void clickHandle(Message msg) {
		AppVersion item = mListAdapter.getItemById(msg.arg1);
		switch (msg.arg2) {
		case CLICK_TYPE_CONTINUE:
		case CLICK_TYPE_PAUSE:
		case CLICK_TYPE_DOWNLOAD:
			DownloadThread downloading = myManager.getDownloadThread(item
					.getGameId());
			if (downloading == null || downloading.isCancelled()) {
				myManager.startDownloadFile(item, adapterHandler);
			} else {
				myManager.getDownloadThread(item.getGameId()).cancel();
			}
			mListAdapter.notifyDataSetChanged();
			break;
		case CLICK_TYPE_INSTALL:
			Util.installApk(mContext,
					item.getAPKFilePath() + "/" + item.getAPKFileName());
			break;
		case CLICK_TYPE_OPEN:
			item.runApk();
			break;

		case CLICK_TYPE_REWARD:
			doReward(item);
			break;
		case CLICK_TYPE_WEB:
			UtilHelper.onWeb(mContext, item.getDownloadUrl());
			if (!item.isNotifyed()) {
				myManager.finishDownloadReport(item.getGameId(),
						mGameVersionHandler);
			}
			break;
		default:
			break;
		}

	}

	/**
	 * 申请获取奖励
	 * 
	 * @param item
	 *            申请的游戏
	 */
	private void doReward(AppVersion item) {
		showProgressBar();
		/* 向服务器发送请求 */
		myManager.commitReward(item.getGameId(), mGameVersionHandler);
	}

	/**
	 * 奖励回应
	 * 
	 * @param answer
	 * @param gameid
	 *            游戏id
	 */
	private void rewardAnswer(int answer, int gameid) {
		dismissListProgressBar();
		String toastInfo;

		/** 成功或者已领取 */
		if (answer == MoreGameManager.REWARD_SUCCESS
				|| answer == MoreGameManager.REWARD_REWARDER_BEFORE) {
			List<AppVersion> games = myManager.getGamesConfig();
			if (games != null) {
				for (AppVersion item : games) {
					if (item.getGameId() == gameid) {
						item.setReward(true);
						break;
					}
				}
			}
			myManager.setRewardLocal(gameid);
			mListAdapter.notifyDataSetChanged();
			if (answer == MoreGameManager.REWARD_SUCCESS) {
				toastInfo = mResource
						.getString(R.string.ddz_obtain_gift_success);
			} else {
				toastInfo = mResource.getString(R.string.ddz_has_obtain_gift);
			}
		} else { // 领取失败
			if (answer == MoreGameManager.REWARD_GAMEINFO_ERROR) { // 服务器没有下载成功记录，则再次提交一下载记录
				myManager.finishDownloadReport(gameid, mGameVersionHandler);
			}
			toastInfo = mResource.getString(R.string.ddz_obtain_gift_failed);
		}
		Toast.makeText(mContext, toastInfo, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG, "MoregameActivity is onCreate...");
		mContext = this;
		mResource = mContext.getResources();

		MoregameConfig.initPath(mContext);
		createParentPath();

		setContentView(R.layout.layout_more_game);

		myManager = new MoreGameManager(this);

		initViews();

		showProgressBar();

		// 获取游戏列表
		myManager.getServerInfo(mGameVersionHandler);

	}

	private void createParentPath() {
		File pth = new File(MoregameConfig.PARENT_PATH);
		if (!pth.exists()) {
			pth.mkdirs();
		}
		File srvpth = new File(MoregameConfig.SERVICE_DOWNLOAD_PATH());
		if (!srvpth.exists()) {
			srvpth.mkdirs();
		}
	}

	/**
	 * 显示加载进度条
	 */
	private void showProgressBar() {
		mList.setVisibility(View.INVISIBLE);
		progress_gird.setVisibility(View.VISIBLE);
		progress_gird.setIndeterminate(true);
	}

	/**
	 * 显示游戏ListView
	 */
	private void dismissListProgressBar() {
		mList.setVisibility(View.VISIBLE);
		progress_gird.setVisibility(View.GONE);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 初始化
	 */
	private void initViews() {
		((TextView) findViewById(R.id.tvTitle)).setText(mResource
				.getString(R.string.ddz_free_zhuandou));
		mList = (ListView) findViewById(R.id.more_game_list);
		progress_gird = (ProgressBar) findViewById(R.id.progress_gird);
		findViewById(R.id.tvBack).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}

	@Override
	public void onPause() {
		myManager.onPause();
		super.onPause();
	}

	@Override
	public void onDestroy() {
		myManager.onDestroy();
		super.onDestroy();

	}

	@Override
	public void onResume() {
		myManager.onResume();
		super.onResume();
		try{
			if(mListAdapter!=null){
				mListAdapter.notifyDataSetChanged();
			}
		}catch(Exception e){}
	}

	/**
	 * 刷新游戏下载状态图片
	 * 
	 * @param positon
	 */
	public void refreshGameIconStatus(int positon) {
		List<AppVersion> gamesList = myManager.getGamesConfig();
		int rate = gamesList.get(positon).mProgress;
		mListAdapter.chargeProgress(positon, rate);
	}

	/**
	 * 初始化listView
	 * 
	 * @param list
	 */
	private void listViewGameInit(List<AppVersion> list) {

		mListAdapter = new MoreGameListAdapter(this, mGameVersionHandler, list);

		mList.setAdapter(mListAdapter);

		dismissListProgressBar();
	}

	public static final int UPDATE_DOWNLOAD_PROGRESS = 0; // 更新下载进度

	public static final int SERVICE_DOWNLOAD_SUCCESS = 1; // 下载成功

	public static final int SERVICE_DOWNLOAD_FAIL = 2; // 下载失败

	public static final int SERVICE_DOWNLOAD_CANCLE = 3; // 下载暂停

	public static final int UPDATE_ICON = 4; // 更新图标

	// 游戏下载handle
	public Handler adapterHandler = new Handler() {
		public void handleMessage(Message msg) {
			Bundle bundle = msg.getData();
			int id = bundle.getInt("gameId");
			int rate = bundle.getInt("rate");
			Log.v(TAG, "gameid =" + id);
			Log.v(TAG, "rate=" + rate);

			switch (msg.what) {
			case UPDATE_DOWNLOAD_PROGRESS: {
				mListAdapter.chargeProgress(id, rate);
			}
				break;
			case SERVICE_DOWNLOAD_SUCCESS: {
				/*
				 * final AppVersion gameItem = (AppVersion) mListAdapter
				 * .getItemById(id);
				 */
				final int gameid = id;
				DownloadThread downloading = myManager
						.getDownloadThread(gameid);
				if (downloading != null && downloading.isCancelled()) {
					myManager.clearDownloadThread(gameid);
				}
				myManager.finishDownloadReport(gameid, mGameVersionHandler);
				mListAdapter.chargeProgress(id, rate);
			}
				break;
			case SERVICE_DOWNLOAD_FAIL: {
				final AppVersion gameItem = (AppVersion) mListAdapter
						.getItemById(id);
				if (gameItem != null) {
					mListAdapter.chargeProgress(id, rate);
					Toast.makeText(
							mContext,
							gameItem.getGameName()
									+ mResource
											.getString(R.string.ddz_download_failed),
							Toast.LENGTH_SHORT).show();
				}
			}
				break;
			case SERVICE_DOWNLOAD_CANCLE: {
				mListAdapter.chargeProgress(id, rate);
			}
				break;
			case UPDATE_ICON:
				if (mListAdapter != null) {
					mListAdapter.notifyDataSetChanged();
				}
				break;
			default:
				break;
			}
		}
	};

}