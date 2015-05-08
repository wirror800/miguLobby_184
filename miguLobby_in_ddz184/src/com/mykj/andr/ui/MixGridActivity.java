package com.mykj.andr.ui;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.mykj.andr.model.HallDataManager;
import com.mykj.andr.model.MixtureInfo;
import com.mykj.andr.provider.MixInfoProvider;
import com.mykj.andr.ui.adapter.MixtureAdapter;
import com.mykj.andr.ui.widget.CardZoneProtocolListener;
import com.MyGame.Midlet.R;
import com.mykj.game.ddz.api.AnalyticsUtils;
import com.mykj.game.moregame.MoregameActivity;
import com.mykj.game.utils.Log;
import com.mykj.game.utils.Toast;

public class MixGridActivity extends Activity implements OnClickListener {

	public static final int REFLASH_LISTVIEW = 9110;
	public static final int REFLASH_LISTVIEW_FAIL = 9111;
	private static final int START_ANIM = 9112;

	private GridView mixGridView;
	private ImageView btnFreeBean;    //免费赚豆按钮
	//MixtureInfo[] mixInfos;
	MixtureAdapter mixtureAdapter;

	/** 标记是否刷新合成材料 */
	private boolean flag = true;

	static MixGridActivity instance;

	public static MixGridActivity getInstance() {
		return instance;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mix_operate_activity);
		instance = this;
		Bundle bundle = getIntent().getExtras();
		if (bundle != null && "menu".equals(bundle.getString("entry"))) {
			flag = false;
			int userID = HallDataManager.getInstance().getUserMe().userID;
			CardZoneProtocolListener.getInstance(instance).requestBackPackList(
					userID, handler);
		}
		init();
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
	private void init() {
		((TextView)findViewById(R.id.tvTitle)).setText(R.string.goodsmis);
		findViewById(R.id.tvBack).setOnClickListener(this); // 返回
		
		List<MixtureInfo> mixInfos = MixInfoProvider.getInstance().getMixtureList();
		
		mixtureAdapter = new MixtureAdapter(this,mixInfos);

		mixGridView = (GridView) findViewById(R.id.mix_grid);
		mixGridView.setAdapter(mixtureAdapter);
		mixGridView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View v, int position,
					long id) {
				if (flag) {
					// 去合成
					Intent intent = new Intent(MixGridActivity.this,
							MixActivity.class);
					intent.putExtra("position", position);
					startActivity(intent);
				} else {
					Toast.makeText(MixGridActivity.this, MixGridActivity.this.getResources().getString(R.string.package_no_obtain_material),
							Toast.LENGTH_SHORT).show();
				}
			}
		});
		btnFreeBean = (ImageView) findViewById(R.id.btnFreeBean);
		btnFreeBean.setOnClickListener(this);
		handler.sendEmptyMessageDelayed(START_ANIM,300);
	}

	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case REFLASH_LISTVIEW:
				if (mixtureAdapter != null)
					mixtureAdapter.notifyDataSetChanged();// 刷新UI
				break;

			case REFLASH_LISTVIEW_FAIL:
				Toast.makeText(getApplication(), MixGridActivity.this.getResources().getString(R.string.package_net_error),
						Toast.LENGTH_SHORT).show();
				Log.e("合成列表", "合成道具图片下载失败，错误码为:" + msg.arg1);
				break;

			case CardZoneProtocolListener.HANDLER_PACK_QUERY_SUCCESS: // 获取背包列表数据成功
				flag = true;
				break;

			case CardZoneProtocolListener.HANDLER_PACK_QUERY_SUCCESS_NODATA:
				flag = true;
				break;
			case START_ANIM:
				if(btnFreeBean != null){
					AnimationDrawable ad = (AnimationDrawable) btnFreeBean.getBackground();
					ad.stop();
	                ad.start();
				}
				break;
			default:
				break;
			}
		}
	};

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.tvBack) {
			finish();
		}else if(id ==R.id.btnFreeBean){
			Intent intent = new Intent(MixGridActivity.this, MoregameActivity.class);
			startActivity(intent);
		}

	}
}
