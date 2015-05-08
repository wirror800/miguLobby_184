package com.mykj.andr.ui;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.minyou.android.net.HttpConnector;
import com.minyou.android.net.IRequest;
import com.minyou.android.net.NetService;
import com.mykj.andr.model.HallDataManager;
import com.mykj.andr.model.PopularizeDetailList;
import com.mykj.andr.model.PopularizeDetailList.PopularizeDetail;
import com.mykj.andr.model.PopularizeFriendList;
import com.mykj.andr.model.PopularizeFriendList.PopularizeFriend;
import com.mykj.andr.ui.adapter.PopularizeDetailAdapter;
import com.mykj.andr.ui.adapter.PopularizeDetailAdapter.GainListener;
import com.mykj.andr.ui.adapter.PopularizeListAdapter;
import com.mykj.comm.io.TDataInputStream;
import com.MyGame.Midlet.R;
import com.mykj.game.ddz.api.AnalyticsUtils;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.CenterUrlHelper;
import com.mykj.game.utils.Toast;
import com.mykj.game.utils.UtilHelper;

public class PopularizeActivity extends Activity {

	private LinearLayout mPopularizeFriendLin;
	private ListView mPopularizeFriend;
	private ListView mPopularizeDetail;
	private TextView mBack;
	private TextView mLabel;
	private TextView mUnreachLabel;
	private TextView mTopLabel;
	private PopularizeDetailAdapter mDetailAdapter;
	private PopularizeListAdapter mListAdapter;
	private GainListener gainListener;
	private Context context;
	private String xmlFriendList = null;
	private String xmlDetailList = null;
	private ArrayList<PopularizeFriend> friendList;
	private ArrayList<PopularizeDetail> detailList;

	private String method;
	private int gameid;
	private int uid;
	private int euid;
	private ProgressDialog mProgress = null;

	// private HashMap<Integer, String> detailMap = new HashMap<Integer,
	// String>();
	private SparseArray<String> sparseDetailArray = new SparseArray<String>();
	private SparseArray<String> sparseFriendArray = new SparseArray<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.popularize_activity);
		context = this;
		uid = HallDataManager.getInstance().getUserMe().userID;
		gameid = AppConfig.gameId;

		initializeUI();
		mProgress = UtilHelper.showProgress(context, null, this.getResources().getString(R.string.data_loading), false, true);
		if (sparseFriendArray.get(uid) != null) {
			xmlFriendList = sparseFriendArray.get(uid);
			initializeAdapter(uid);
		} else {
			initializeData(uid);
		}
		initializeListener();
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
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
	private void initializeListener() {
		// TODO Auto-generated method stub
		mPopularizeFriend.requestFocus();
		mPopularizeFriend.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mPopularizeFriend.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				// TODO Auto-generated method stub
				detailList = null;
				mDetailAdapter.notifyDataSetChanged();
				PopularizeListAdapter.setCurrPos(pos);
				mListAdapter.notifyDataSetChanged();
				if (friendList.get(pos) != null) {
					euid = friendList.get(pos).uid;
					getFriendDetail(euid);
				}

			}
		});

		gainListener = new GainListener() {

			@Override
			public void gainBouns(PopularizeDetail detail) {
				// TODO Auto-generated method stub
				getBouns(detail);
			}
		};
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	private void initializeData(final int uid) {
		// TODO 获取推广好友列表

		final HttpConnector http = NetService.getInstance()
				.createHttpConnection(null);
		http.addEvent(new IRequest() {

			@Override
			public void doError(Message msg) {
				closeProgress();
				NetService.getInstance().removeHttpConnector(http.getTarget());
			}

			@Override
			public void handler(byte[] buf) {
				// TODO Auto-generated method stub

				xmlFriendList = TDataInputStream.getUTF8String(buf);
				String status = UtilHelper.parseStatusXml(xmlFriendList,
						"status");
				if (status != null && status.equals("0")) {
					sparseFriendArray.put(uid, xmlFriendList);
				}
				initializeAdapter(uid);
				// Log.e("test_spread", xmlFriendList);
				NetService.getInstance().removeHttpConnector(http.getTarget());
			}

			@Override
			public String getParam() {
				// TODO Auto-generated method stub
				StringBuffer buffer = new StringBuffer();
				method = "get_spreadee_list";
				buffer.append("method=").append(method);
				buffer.append("&gameid=").append(gameid);
				buffer.append("&uid=").append(uid);
				buffer.append("&format=").append("xml");
				buffer.append("&apikey=").append(CenterUrlHelper.apikey);
				buffer.append("&op=").append(System.currentTimeMillis());
				String params = buffer.toString();
				String sign = CenterUrlHelper.getSign(params,
						CenterUrlHelper.secret);
				return params + sign;
			}

			@Override
			public String getHttpUrl() {
				// TODO Auto-generated method stub
				return AppConfig.WEIXIN_SHARE;
			}

			@Override
			public byte[] getData() {
				// TODO Auto-generated method stub
				return null;
			}
		});
		http.connect();
	}

	private void initializeAdapter(int uid) {
		closeProgress();
		PopularizeFriendList popularizeFriendList = new PopularizeFriendList(
				xmlFriendList);
		friendList = popularizeFriendList.getFriendList();
		if (null != friendList && friendList.size() > 0) {
			mLabel.setVisibility(View.GONE);
			mUnreachLabel.setVisibility(View.GONE);
			mPopularizeFriend.setVisibility(View.VISIBLE);
			mPopularizeDetail.setVisibility(View.VISIBLE);
			mPopularizeFriendLin.setVisibility(View.VISIBLE);
			mListAdapter = new PopularizeListAdapter(context);
			mListAdapter.setList(friendList);
			mPopularizeFriend.setAdapter(mListAdapter);
			mPopularizeFriend.setSelected(true);
			mPopularizeFriend.setSelection(PopularizeListAdapter.getCurrPos());
			euid = friendList.get(PopularizeListAdapter.getCurrPos()).uid;
			getFriendDetail(euid);
		} else {
			mPopularizeFriend.setVisibility(View.GONE);
			mPopularizeDetail.setVisibility(View.GONE);
			mPopularizeFriendLin.setVisibility(View.GONE);
			mLabel.setVisibility(View.VISIBLE);
		}

		mDetailAdapter = new PopularizeDetailAdapter(context, gainListener);
	}

	private void getFriendDetail(final int euid) {
		// TODO Auto-generated method stub
		if (sparseDetailArray.get(euid) != null) {
			xmlDetailList = sparseDetailArray.get(euid);
			initializeDetailAdapter(euid);
			// mDetailAdapter.notifyDataSetChanged();
		} else {
			refreshFriendDetail(euid);
		}
	}

	private void refreshFriendDetail(final int euid) {
		// TODO Auto-generated method stub
		final HttpConnector http = NetService.getInstance()
				.createHttpConnection(null);
		http.addEvent(new IRequest() {

			@Override
			public void doError(Message msg) {
				NetService.getInstance().removeHttpConnector(http.getTarget());
				refresh = false;
			}

			@Override
			public void handler(byte[] buf) {
				// TODO Auto-generated method stub

				xmlDetailList = TDataInputStream.getUTF8String(buf);

				initializeDetailAdapter(euid);
				NetService.getInstance().removeHttpConnector(http.getTarget());
			}

			@Override
			public String getParam() {
				// TODO Auto-generated method stub
				StringBuffer buffer = new StringBuffer();
				method = "get_spreadee_detail";
				buffer.append("method=").append(method);
				buffer.append("&gameid=").append(gameid);
				buffer.append("&uid=").append(uid);
				buffer.append("&euid=").append(euid);
				buffer.append("&format=").append("xml");
				buffer.append("&apikey=").append(CenterUrlHelper.apikey);
				buffer.append("&op=").append(System.currentTimeMillis());
				String params = buffer.toString();
				String sign = CenterUrlHelper.getSign(params,
						CenterUrlHelper.secret);
				return params + sign;
			}

			@Override
			public String getHttpUrl() {
				// TODO Auto-generated method stub
				return AppConfig.WEIXIN_SHARE;
			}

			@Override
			public byte[] getData() {
				// TODO Auto-generated method stub
				return null;
			}
		});
		http.connect();

	}

	private boolean refresh = false;

	private void initializeDetailAdapter(int euid) {
		// TODO Auto-generated method stub
		PopularizeDetailList popularizeDetailList = new PopularizeDetailList(
				xmlDetailList);
		detailList = popularizeDetailList.getDetailList();
		if (null != detailList && detailList.size() > 0) {
			mUnreachLabel.setVisibility(View.GONE);
			mPopularizeDetail.setVisibility(View.VISIBLE);
			sparseDetailArray.put(euid, xmlDetailList);
			mDetailAdapter.setList(detailList);
			if (refresh) {
				mDetailAdapter.notifyDataSetChanged();
				refresh = false;
			} else {
				mPopularizeDetail.setAdapter(mDetailAdapter);
			}
		} else {
			mPopularizeDetail.setVisibility(View.GONE);
			mUnreachLabel.setVisibility(View.VISIBLE);
		}

	}

	private void initializeUI() {
		mBack = (TextView) findViewById(R.id.tvBack);
		mBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		mLabel = (TextView) findViewById(R.id.poplarize_label);
		mUnreachLabel = (TextView) findViewById(R.id.poplarize_unreach_label);
		mTopLabel = (TextView) findViewById(R.id.tvTitle);
		mTopLabel.setText(this.getResources().getString(R.string.my_share));
		mPopularizeFriend = (ListView) findViewById(R.id.poplarize_friend);
		mPopularizeDetail = (ListView) findViewById(R.id.poplarize_detail);
		mPopularizeFriendLin = (LinearLayout) findViewById(R.id.poplarize_lin_friend);
	}

	protected void getBouns(final PopularizeDetail detail) {
		// TODO Auto-generated method stub
		final HttpConnector http = NetService.getInstance()
				.createHttpConnection(null);
		http.addEvent(new IRequest() {

			@Override
			public void doError(Message msg) {
				NetService.getInstance().removeHttpConnector(http.getTarget());
			}

			@Override
			public void handler(byte[] buf) {
				// TODO Auto-generated method stub

				String bounsStr = TDataInputStream.getUTF8String(buf);
				String status = UtilHelper.parseStatusXml(bounsStr, "status");
				String statusnote = UtilHelper.parseStatusXml(bounsStr,
						"statusnote");
				if (statusnote != null && statusnote.equals(PopularizeActivity.this.getResources().getString(R.string.ddz_success))) {
					statusnote = PopularizeActivity.this.getResources().getString(R.string.obtain_gift_success);
				}
				Toast.makeText(context, statusnote, Toast.LENGTH_SHORT).show();
				if (status != null && status.equals("0")) {
					// 刷新领取奖励后
					refresh = true;
					refreshFriendDetail(euid);
				}
				NetService.getInstance().removeHttpConnector(http.getTarget());
			}

			@Override
			public String getParam() {
				// TODO Auto-generated method stub
				StringBuffer buffer = new StringBuffer();
				method = "get_profit";
				buffer.append("method=").append(method);
				buffer.append("&gameid=").append(gameid);
				buffer.append("&uid=").append(uid);
				buffer.append("&euid=").append(euid);
				buffer.append("&condid=").append(detail.condId);
				buffer.append("&format=").append("xml");
				buffer.append("&apikey=").append(CenterUrlHelper.apikey);
				buffer.append("&op=").append(System.currentTimeMillis());
				String params = buffer.toString();
				String sign = CenterUrlHelper.getSign(params,
						CenterUrlHelper.secret);
				return params + sign;
			}

			@Override
			public String getHttpUrl() {
				// TODO Auto-generated method stub
				return AppConfig.WEIXIN_SHARE;
			}

			@Override
			public byte[] getData() {
				// TODO Auto-generated method stub
				return null;
			}
		});
		http.connect();
	}
	
	void closeProgress() {
		try {
			if (mProgress != null) {
				mProgress.dismiss();
				mProgress = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
