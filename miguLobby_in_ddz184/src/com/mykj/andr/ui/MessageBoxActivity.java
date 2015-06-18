package com.mykj.andr.ui;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mykj.andr.model.HallDataManager;
import com.mykj.andr.model.NoticePersonInfo;
import com.mykj.andr.model.UserInfo;
import com.mykj.andr.provider.NoticePersonProvider;
import com.mykj.andr.provider.NoticeSystemProvider;
import com.mykj.andr.ui.adapter.NoticePersonalAdapter;
import com.mykj.andr.ui.adapter.NoticeSystemAdapter;
import com.mykj.game.FiexedViewHelper;
import com.mykj.game.MainApplication;
import com.MyGame.Migu.R;
import com.mykj.game.ddz.api.AnalyticsUtils;
import com.mykj.game.ddz.api.UC;
import com.mykj.game.moregame.MoregameActivity;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.Log;
import com.mykj.game.utils.Util;
import com.mykj.game.utils.UtilHelper;

/*************
 * 新版本：消息盒子主界面，处理Tab
 * 
 * @author zhanghuadong 2012-6-19
 */
public class MessageBoxActivity extends Activity implements OnClickListener {

	private static final String TAG = "MessageBoxActivity";

	// 当前选择的Tab索引
	private int currentTabPager = 0;
	private static final int TAB_NOTICE_SYSTEM = 0;// 系统
	private static final int TAB_NOTICE_PERSON = 1;// 个人

	// 承载View
	private ViewPager mViewPager;

	private TabContentPageAdapter mNoticePageAdapter;

	private LayoutInflater mInflater;

	private List<View> mviews;

	// 系统、个人 等内容Content
	private View mNoticeSystemView;
	private View mNoticePersonView;

	// 系统，个人 等消息Tab
	private TextView tvNoticeSystem, tvNoticePerson;
	private ListView lvNoticeSystem, lvNoticePerson;

	// 当列表数据没有时候提示消息
	TextView tvSNoticeMsg, tvPNoticeMsg;

	// 三个设配器
	private NoticeSystemAdapter mNoticeSystemAdapter;
	private NoticePersonalAdapter mNoticePersonalAdapter;

	private ImageView btnFreeBean;    //免费赚豆按钮
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notice_activity);
		MainApplication.sharedApplication().addActivity(this);
		// 获取传递过来的参数

		init();
		
		setMsgShowTag();
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
	
	private void setMsgShowTag(){
		UserInfo userInfo = HallDataManager.getInstance().getUserMe();
		String key_tag=userInfo.nickName;
		String tag=Util.getStringSharedPreferences(this, key_tag, AppConfig.DEFAULT_TAG);
		String[] strs=tag.split("&");
		if(strs!=null&&strs.length==3){
			strs[2]="1";
			StringBuilder sb=new StringBuilder();
			sb.append(strs[0]).append("&").append(strs[1]).append("&").append(strs[2]);
			Util.setStringSharedPreferences(this, key_tag,sb.toString());

		}

	}
	
	/**
	 * 控件初始化
	 */
	private void init() {
		((TextView)findViewById(R.id.tvTitle)).setText(R.string.messageBox);
		findViewById(R.id.tvTitle).setOnClickListener(this);
		findViewById(R.id.tvBack).setOnClickListener(this);

		tvNoticeSystem = (TextView) findViewById(R.id.tvNoticeSystem);
		tvNoticePerson = (TextView) findViewById(R.id.tvNoticePerson);

		tvNoticeSystem.setOnClickListener(this);
		tvNoticePerson.setOnClickListener(this);

		// 设置颜色半透明
		tvNoticePerson.setTextColor(Color.argb(128, 255, 255, 255));
		tvNoticeSystem.setTextColor(Color.rgb(255, 255, 255));

		initTabContent();
		initPager();
		
		btnFreeBean = (ImageView) findViewById(R.id.btnFreeBean);
		btnFreeBean.setOnClickListener(this);
		startAnimHander.sendEmptyMessageDelayed(0,300); //启动动画
	}

	int one = 0;

	/***
	 * 初始化TabContent内容布局
	 */
	private void initTabContent() {
		// 实例化ArrayList,容纳Tab页面内容
		mviews = new ArrayList<View>();
		mInflater = getLayoutInflater();

		// 解析布局文件

		mNoticeSystemView = mInflater.inflate(R.layout.notice_system, null);
		mNoticePersonView = mInflater.inflate(R.layout.notice_person, null);

		// 从View中找寻ListView
		lvNoticeSystem = (ListView) mNoticeSystemView
				.findViewById(R.id.lvNoticeSystem);
		lvNoticePerson = (ListView) mNoticePersonView
				.findViewById(R.id.lvNoticePerson);

		// 当列表数据没有时候提示消息
		tvSNoticeMsg = (TextView) mNoticeSystemView
				.findViewById(R.id.tvSNoticeMsg);
		tvPNoticeMsg = (TextView) mNoticePersonView
				.findViewById(R.id.tvPNoticeMsg);

		initListViewCotent();
		// 添加进容器中

		mviews.add(mNoticeSystemView);
		mviews.add(mNoticePersonView);
	}

	/****
	 * 初始化ListView内容
	 */
	private void initListViewCotent() {
		// 系统消息数据来源以及适配器
		mNoticeSystemAdapter = new NoticeSystemAdapter(this);
		// 个人消息数据来源以及适配器
		mNoticePersonalAdapter = new NoticePersonalAdapter(this);

		/*********************
		 * 添加ListView数据
		 ********************/
		initDataSource();

	}

	/***
	 * 初始化ViewPager
	 */
	private void initPager() {
		mNoticePageAdapter = new TabContentPageAdapter(mviews);

		// 获取ViewPager并添加设配器，默认显示第1项，索引为0
		mViewPager = (ViewPager) findViewById(R.id.mViewPager);
		mViewPager.setAdapter(mNoticePageAdapter);
		setImageBackground(0);
		mViewPager.setCurrentItem(TAB_NOTICE_SYSTEM);
		currentTabPager = TAB_NOTICE_SYSTEM; // 当前默认选择Tab索引

		// Page页面改变事件
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// Log.d(TAG, "onPageScrollStateChanged - " + arg0);
				// 状态有三个0空闲，1是增在滑行中，2目标加载完毕
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// 从1到2滑动，在1滑动前调用
			}

			@Override
			public void onPageSelected(int pageIndex) {

				switch (pageIndex) {
				case 0:
					mViewPager.setCurrentItem(TAB_NOTICE_SYSTEM);
					if (currentTabPager == 1) {
						// 设置颜色半透明
						tvNoticePerson.setTextColor(Color.argb(128, 255, 255,
								255));
						tvNoticeSystem.setTextColor(Color.rgb(255, 255, 255));

					}
					break;
				case 1:
					mViewPager.setCurrentItem(TAB_NOTICE_PERSON);
					if (currentTabPager == 0) {
						// 设置颜色半透明
						tvNoticePerson.setTextColor(Color.rgb(255, 255, 255));
						tvNoticeSystem.setTextColor(Color.argb(128, 255, 255,
								255));

					}
					break;
				}

				currentTabPager = pageIndex; // 当前选择Tab索引
				setImageBackground(pageIndex);
			}
		});
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.tvNoticeSystem) {
			setImageBackground(TAB_NOTICE_SYSTEM);
			mViewPager.setCurrentItem(TAB_NOTICE_SYSTEM);
		} else if (id == R.id.tvNoticePerson) {
			setImageBackground(TAB_NOTICE_PERSON);
			mViewPager.setCurrentItem(TAB_NOTICE_PERSON);
		} else if (id == R.id.tvBack) {
			finish();
		} else if (id == R.id.tvTitle) {
			// 显示当前顶部信息，按键声音
			if (lvNoticeSystem != null && lvNoticeSystem.getAdapter() != null) {
				if (lvNoticeSystem.getAdapter().getCount() > 0)
					lvNoticeSystem.setSelection(0);
			}
			if (lvNoticePerson != null && lvNoticePerson.getAdapter() != null) {
				if (lvNoticePerson.getAdapter().getCount() > 0)
					lvNoticePerson.setSelection(0);
			}
			// 声音
		}else if(id == R.id.btnFreeBean){
			Intent intent = new Intent(MessageBoxActivity.this, MoregameActivity.class);
			startActivity(intent);
			AnalyticsUtils.onClickEvent(this, UC.EC_225);
		}

	}

	/***
	 * 根据Tab索引切换图片背景
	 * 
	 * @param position
	 */
	private void setImageBackground(int position) {
		switch (position) {
		case 0:
			tvNoticeSystem.setBackgroundResource(R.drawable.left_tab_fg);
			tvNoticePerson.setBackgroundResource(R.drawable.right_tab_bg);

			break;
		case 1:
			tvNoticeSystem.setBackgroundResource(R.drawable.left_tab_bg);
			tvNoticePerson.setBackgroundResource(R.drawable.right_tab_fg);
			break;

		default:
			break;
		}
	}

	private void savePersonInfo(int userID) {
		// 将个人消息根据userid不同保存本地
		List<String> mList = NoticePersonProvider.getInstance().getPersonStr();
		List<String> mFileList = new ArrayList<String>();
		if (null != mList && mList.size() > 0) {
			String fileStr = UtilHelper.listToString(mList);
			String fileStr_ = "";
			FileOutputStream outStream;
			FileInputStream inputStream;
			try {
				inputStream = getBaseContext().openFileInput(userID + ".txt");
				if (inputStream != null) {
					fileStr_ = UtilHelper.convertStreamToString(inputStream);
				}
				inputStream.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (fileStr_.indexOf(fileStr) < 0) {
				try {
					outStream = openFileOutput(userID + ".txt",
							Context.MODE_APPEND);
					outStream.write((fileStr + ",").getBytes());
					outStream.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		FileInputStream inputStream;
		try {
			inputStream = getBaseContext().openFileInput(userID + ".txt");
			if (inputStream != null) {
				String fileStr = UtilHelper.convertStreamToString(inputStream);
				String[] arrayStr = fileStr.split(",");
				mFileList = Arrays.asList(arrayStr);
				Log.e(TAG, UtilHelper.convertStreamToString(inputStream));
			}
			inputStream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "缓存清除");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (null != mFileList && mFileList.size() > 0) {
			for (int i = 0; i < mFileList.size(); i++) {
				NoticePersonInfo info = new NoticePersonInfo(mFileList.get(i));
				NoticePersonProvider.getInstance().addPersonInfo(info);
			}
//			NoticePersonProvider.getInstance().clsPersonStr();
		}
	}

	private void initDataSource() {
		// 有数据
		if (NoticeSystemProvider.getInstance().getNoticeSystemInfos() != null
				&& NoticeSystemProvider.getInstance().getNoticeSystemInfos().length > 0) {
			mNoticeSystemAdapter.setList(NoticeSystemProvider.getInstance()
					.getNoticeSystemInfos()); // 通过Provider获取数据
			lvNoticeSystem.setAdapter(mNoticeSystemAdapter);
			tvSNoticeMsg.setVisibility(View.GONE);
		} else {
			tvSNoticeMsg.setVisibility(View.VISIBLE);
		}
		
		int userId=FiexedViewHelper.getInstance().getUserId();
		savePersonInfo(userId);
		for (int i = 0; i < AppConfig.personInfoList.size(); i++) {
			NoticePersonInfo info = new NoticePersonInfo(
					AppConfig.personInfoList.get(AppConfig.personInfoList
							.size() - i - 1));
			NoticePersonProvider.getInstance().addPersonInfo(info);
		}
		AppConfig.personInfoList.clear();
		if (NoticePersonProvider.getInstance().getPersonInfos() != null
				&& NoticePersonProvider.getInstance().getPersonInfos().length > 0) {

			mNoticePersonalAdapter.setList(NoticePersonProvider.getInstance()
					.getPersonInfos());
			lvNoticePerson.setAdapter(mNoticePersonalAdapter);
			tvPNoticeMsg.setVisibility(View.GONE);
			lvNoticePerson.setVisibility(View.VISIBLE);
			NoticePersonProvider.getInstance().init();
		} else {
			lvNoticePerson.setVisibility(View.GONE);
			tvPNoticeMsg.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		MainApplication.sharedApplication().finishActivity(this);
//		AppConfig.personInfoList.clear();
	}
	/**
	 * 仅启动动画
	 */
	private Handler startAnimHander = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			if(btnFreeBean != null){
				AnimationDrawable ad = (AnimationDrawable) btnFreeBean.getBackground();
				ad.stop();
                ad.start();
			}
		}
		
	};
}
