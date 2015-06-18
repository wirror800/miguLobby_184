package com.mykj.andr.ui;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mykj.andr.model.GoodsItem;
import com.mykj.andr.model.HallDataManager;
import com.mykj.andr.model.UserInfo;
import com.mykj.andr.pay.PayManager;
import com.mykj.andr.provider.GoodsItemProvider;
import com.mykj.andr.ui.adapter.MarketAdapter;
import com.mykj.game.MainApplication;
import com.MyGame.Migu.R;
import com.mykj.game.ddz.api.AnalyticsUtils;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.CenterUrlHelper;
import com.mykj.game.utils.Log;
import com.mykj.game.utils.Util;
import com.mykj.game.utils.UtilHelper;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;

/**
 * 
 * @ClassName: MarketActivity
 * @Description: 商城
 * @author Administrator
 * @date 2012-7-23 下午02:21:24
 * 
 */
public class MarketActivity extends ListActivity implements
		OnItemClickListener, OnClickListener ,IWXAPIEventHandler {
	private static final String TAG = "MarketActivity";

	private Activity mAct;

	private int userID = 0;
	private String userToken = "";

	private MarketAdapter marketAdapter;
	private ListView lvMarket;

	private ImageView ivHelp, ivBackpack;

	/** 下载图片成功后更新UI */
	public static final int REFLASH_LISTVIEW = 20000;

	/** 下载图片失败后更新UI */
	public static final int REFLASH_LISTVIEW_FAIL = 20001;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.market_activity);
		MainApplication.sharedApplication().addActivity(this);
		mAct = this;
		PayManager.getInstance(mAct);  //重设PayManger mContext,防止商城购买后，切换到分区mContext变化
		init();

	}

	@Override
	public void onResume() {
		super.onResume();
		UserInfo user = HallDataManager.getInstance().getUserMe();
		String key_tag = user.nickName;
		String tag = Util.getStringSharedPreferences(mAct, key_tag,
				AppConfig.DEFAULT_TAG);
		String[] strs = tag.split("&");
		if (strs != null && strs.length == 3) {

			int back = Integer.parseInt(strs[1]);
			if (0 == back) {
				findViewById(R.id.btnPacket_newTag).setVisibility(View.VISIBLE);
			} else if (1 == back) {
				findViewById(R.id.btnPacket_newTag).setVisibility(
						View.INVISIBLE);
			}
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
	
	/**
	 * 控件初始化
	 */
	private void init() {
		((TextView)findViewById(R.id.tvTitle)).setText(R.string.mark);
		findViewById(R.id.tvTitle).setOnClickListener(this);
		findViewById(R.id.tvBack).setOnClickListener(this); // 返回

		ivHelp = (ImageView) findViewById(R.id.ivHelp);
		ivHelp.setOnClickListener(this);

		ivBackpack = (ImageView) findViewById(R.id.ivBackpack);
		ivBackpack.setOnClickListener(this);

		// //是否显示“新”标记
		// if(Util.getBooleanSharedPreferences(mAct, AppConfig.PACKAGE_TAG,
		// true)){
		// findViewById(R.id.btnPacket_newTag).setVisibility(View.VISIBLE);
		// }else{
		// findViewById(R.id.btnPacket_newTag).setVisibility(View.INVISIBLE);
		// }

		lvMarket = getListView();
		
		List<GoodsItem> lists = GoodsItemProvider.getInstance().getGoodsList();
		if(lists.size()>0){
			lists.get(0).isArrowUp = true;
		}
		marketAdapter = new MarketAdapter(mAct, lists);
		lvMarket.setAdapter(marketAdapter);
		lvMarket.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View convertView, int pos,
			long id) {
		GoodsItem item = (GoodsItem) marketAdapter.getItem(pos);
		item.isArrowUp = !item.isArrowUp;
		marketAdapter.notifyDataSetChanged();
		if (pos != 0) {
			lvMarket.setSelectionFromTop(pos, convertView.getHeight() / 2);
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.tvBack) {
			finish();
		} else if (id == R.id.ivHelp) {
			short urlid = 404;
			gotoWebView(urlid);
		} else if (id == R.id.ivBackpack) {
			toBackPackActivity();
			// finish();
		} else if (id == R.id.tvTitle) {
			// 显示当前顶部信息，按键声音
			if (lvMarket != null && lvMarket.getAdapter() != null) {
				if (lvMarket.getAdapter().getCount() > 0)
					lvMarket.setSelection(0);
			}
			// 声音
		}

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

		UtilHelper.onWeb(MarketActivity.this, finalUrl);
	}

	/**
	 * @Title: toBackPackActivity
	 * @Description:TODO跳转到我的物品
	 * @param userID
	 * @param clientID
	 * @param token
	 * @param move_mobile_key
	 * @param channelId
	 * @version: 2012-8-2 下午12:50:54
	 */
	private void toBackPackActivity() {
		Intent intent = new Intent(this, BackPackActivity.class);
		startActivity(intent);
	}

	/****
	 * 定义一个Handler处理线程发送的消息，并更新主UI线程
	 */
	@SuppressLint("HandlerLeak")
	public Handler mMarkHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case REFLASH_LISTVIEW: // 下载图片完成后更新ListView
				if (marketAdapter != null)
					marketAdapter.notifyDataSetChanged();// 刷新UI
				break;

			case REFLASH_LISTVIEW_FAIL:
				Log.e(TAG, "道具图片文件下载失败，错误码为：" + msg.arg1);
				break;

			default:
				break;
			}
		}
	};

	@Override
	public void onReq(BaseReq arg0) {
		
	}

	@Override
	public void onResp(BaseResp resp) {

		Log.d(TAG, "onPayFinish, errCode = " + resp.errCode);

		if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("提示");
			builder.setMessage(getString(R.string.pay_result_callback_msg,
					String.valueOf(resp.errCode)));
			builder.show();
		}

	}

}