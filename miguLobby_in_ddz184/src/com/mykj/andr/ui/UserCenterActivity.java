package com.mykj.andr.ui;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.login.utils.DensityConst;
import com.mingyou.accountInfo.AccountItem;
import com.mingyou.accountInfo.LoginInfoManager;
import com.mingyou.distributor.NetPrivateListener;
import com.mingyou.distributor.NetSocketPak;
import com.mykj.andr.headsys.HeadAdapter;
import com.mykj.andr.headsys.HeadInfo;
import com.mykj.andr.headsys.HeadManager;
import com.mykj.andr.model.City;
import com.mykj.andr.model.HallDataManager;
import com.mykj.andr.model.Province;
import com.mykj.andr.model.UserCenterData;
import com.mykj.andr.model.UserInfo;
import com.mykj.andr.net.NetSocketManager;
import com.mykj.andr.provider.UserCenterProvider;
import com.mykj.comm.io.TDataInputStream;
import com.mykj.comm.io.TDataOutputStream;
import com.mykj.game.FiexedViewHelper;
import com.MyGame.Midlet.R;
import com.mykj.game.ddz.api.AnalyticsUtils;
import com.mykj.game.moregame.MoregameActivity;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.CenterUrlHelper;
import com.mykj.game.utils.Log;
import com.mykj.game.utils.ProvinceConfig;
import com.mykj.game.utils.ScoreConfig;
import com.mykj.game.utils.Util;
import com.mykj.game.utils.UtilHelper;

public class UserCenterActivity extends Activity implements OnClickListener,
RadioGroup.OnCheckedChangeListener {

	private final static String TAG = "UserCenterActivity";

	private static final int TAB_INDEX_BASIC = 0;    //第一页， 基本资料
	private static final int TAB_INDEX_DETAILS = 1;  //第二页，  详细资料
	private static final int TAB_INDEX_MATCH = 2;    //第三页 ， 比赛资料
	private static final int TAB_INDEX_HEAD = 3;     //第四页，  修改头像

	private byte sexId;
	private int currentTabPager = 0;

	private ViewPager mViewPager;

	private TabContentPageAdapter mCenterPageAdapter;

	private LayoutInflater mInflater;

	private List<View> mViews;

	private String realName;
	private String idCardNo;
	private String mobile;
	private String provinceCode = "-1";
	private String cityCode = "-1";
	private String nickName;

	private View mBasicView;
	private View mDetailView;
	private View mMatchView;
	private View mHeadView;
	private TextView tvBasic, tvDetail, tvMatch, tvHead;
	private TextView tvAccount, tvID, tvBeans, tvGold, tvTicket;
	private TextView tvRank, tvPoint;

	private Button btnModify, btnEnsure, btnExchange, btnReceive;
	private Button btnSave;
	private Button btnRank, btnQuery;

	private ImageView ivDiamond;

	private TextView tvRealName, tvCard;

	private EditText etNickName, etRealName, etCard, etPhoneNum;

	private RadioGroup mRadioGroup;
	private RadioButton btnMale, btnFemale;

	private TextView tvDashiScore; // 大师分
	private TextView tvDashiName; // 称号
	private TextView tvWhatDashiScore; // 什么师大师分

	private ImageView tabArrows; // 动画图片

	// Tab卡的距离
	private int one = 0;
	private int two = 0;
	private int three = 0;
	// 省份信息
	private List<Province> provinces = new ArrayList<Province>();

	private ImageView provinceIv;
	private ImageView cityIv;

	private LayoutInflater mInflate;
	private Context mContext;

	private View provinceWindow;
	private ListView provinceList;
	private int pwidth;
	private EditText provinceEt;
	private EditText cityEt;
	private TextView tvProvince;
	private TextView tvCity;
	private TextView tvMoreInfo;
	private PopupWindow provincePopupWindow;

	private View cityWindow;
	private ListView cityList;
	private PopupWindow cityPopupWindow;
	private CityAdapter cityAdapter;
	private ProvinceAdapter provinceAdapter;


	private ListView mHeadListView;     //头像listView
	private HeadAdapter marketAdapter = null; 

	private Resources mResource;

	private ImageView btnFreeBean;    //免费赚豆按钮

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_center_activity);
		mContext=this;
		mResource = this.getResources();

		mInflate = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		if(ProvinceConfig.provinces == null){
			ProvinceConfig.initProvinces(this);
		}
		provinces = ProvinceConfig.provinces;

		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		init();

		measuredTabWidth();

		int userId=FiexedViewHelper.getInstance().getUserId();

		UserCenterData userData=UserCenterProvider.getInstance().getUserCenterData();

		if(userData!=null){
			setCenterText(userData);
		}else{
			getUserCenterInfo(userId, AppConfig.clientID);
		}

		modifyUserInfoEx();

		modifyUserInfoListener();

		if(HeadManager.getInstance().isGetHeadMarketListFinish()){
			if(!HeadManager.getInstance().isGetHeadPackFinish()){
				HeadManager.getInstance().requestHeadPackList(userId); //请求已有头像
			}
		}else{
			HeadManager.getInstance().requestHeadMarketList(this);
		}
		HeadManager.getInstance().setUpdateHanler(handler, HANDLER_UPDATE_HEAD); //设置更新handler
	}




	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == GetTicketActivity.TICKET_CHANGED) {
			int ticket = data.getExtras().getInt("ticket");
			tvTicket.setText(String.valueOf(ticket) + " 元");
		}

	}


	@Override
	protected void onResume() {
		super.onResume();
		HeadManager.getInstance().setContext(this);   //设置头像上下文
		AnalyticsUtils.onPageStart(this);
		AnalyticsUtils.onResume(this);
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		AnalyticsUtils.onPageEnd(this);
		AnalyticsUtils.onPause(this);
		HeadManager.getInstance().setContext(null);   //取消头像上下文
	}

	/**
	 * 测量tab栏宽度
	 */
	private void measuredTabWidth(){
		ViewTreeObserver vto = tvBasic.getViewTreeObserver();

		vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
		{
			public boolean onPreDraw()
			{
				int tvTabWidth = tvBasic.getMeasuredWidth();//TAB控件宽度 ，所有TAB页控件布局等宽

				int bmpWidth = BitmapFactory.decodeResource(getResources(), R.drawable.arrows)
						.getWidth();// 获取箭头图片宽度

				int w=DensityConst.getDip(bmpWidth)/2; // 获取箭头图片宽度的一半, px-->dip

				int offset = tvTabWidth / 2 - w; //箭头在 tab页上的偏移量

				Matrix matrix = new Matrix();
				matrix.postTranslate(offset, 0);
				tabArrows.setImageMatrix(matrix);

				one = tvTabWidth;        // 页卡1 -> 页卡2 偏移量
				two = one + tvTabWidth ; // 页卡1 -> 页卡3 偏移量
				three = two + tvTabWidth ; // 页卡1 -> 页卡4 偏移量
				return true;
			}
		});

	}

	private void init() {
		((TextView)findViewById(R.id.tvTitle)).setText(R.string.usercenter);
		findViewById(R.id.tvBack).setOnClickListener(new OnClickListener() { // 返回
			@Override
			public void onClick(View v) {

				finish();
			}
		});
		tvBasic = (TextView) findViewById(R.id.tv_basic);
		tvDetail = (TextView) findViewById(R.id.tv_detail);
		tvMatch = (TextView) findViewById(R.id.tv_match);
		tvHead = (TextView) findViewById(R.id.tv_head);
		tabArrows = (ImageView) findViewById(R.id.tab_arrows);

		initTabContent();
		initPager();

		btnFreeBean = (ImageView) findViewById(R.id.btnFreeBean);
		btnFreeBean.setOnClickListener(this);
		handler.sendEmptyMessageDelayed(HANDLER_START_ANIM,300);  //启动动画
	}

	/**
	 * 计算弹出框的宽度
	 * 
	 * @param view
	 * @return
	 */
	boolean viewHasMeasured = false;
	private void getWidth() {	

		ViewTreeObserver vto = provinceEt.getViewTreeObserver();

		vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
		{
			public boolean onPreDraw()
			{
				if (viewHasMeasured  == false)
				{
					pwidth = provinceEt.getMeasuredWidth();
					viewHasMeasured = true;
					initProvincePopuWindow();
					initCityPopuWindow();
				}
				return true;
			}
		});
	}




	/**
	 * 初始化viewpager
	 */
	private void initPager() {
		mCenterPageAdapter = new TabContentPageAdapter(mViews);

		// 获取ViewPager并添加设配器，默认显示第1项，索引为0
		mViewPager = (ViewPager) findViewById(R.id.center_view_pager);
		mViewPager.setAdapter(mCenterPageAdapter);

		setImageBackground(0);
		mViewPager.setCurrentItem(TAB_INDEX_BASIC);
		currentTabPager = TAB_INDEX_BASIC; // 当前默认选择Tab索引

		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int pageIndex) {
				Animation animation = null;
				if(pageIndex == currentTabPager){
					return;
				}
				switch (pageIndex) {
				case TAB_INDEX_BASIC:  //第一页
					if (currentTabPager == TAB_INDEX_DETAILS) {  //第二页-->第一页
						animation = new TranslateAnimation(one, 0, 0, 0);
					} else if (currentTabPager == TAB_INDEX_MATCH) {  //第三页-->第一页
						animation = new TranslateAnimation(two, 0, 0, 0);
					} else if(currentTabPager == TAB_INDEX_HEAD){ //第四页-->第一页
						animation = new TranslateAnimation(three, 0, 0, 0);
					}
					break;
				case TAB_INDEX_DETAILS: //第二页
					if (currentTabPager == TAB_INDEX_BASIC) { //第一页-->第二页
						animation = new TranslateAnimation(0, one, 0, 0);
					} else if (currentTabPager == TAB_INDEX_MATCH) { //第三页-->第二页
						animation = new TranslateAnimation(two, one, 0, 0);
					} else if(currentTabPager == TAB_INDEX_HEAD){ //第四页-->第二页
						animation = new TranslateAnimation(three, one, 0, 0);
					}
					break;
				case TAB_INDEX_MATCH: //第三也
					if (currentTabPager == TAB_INDEX_BASIC) { //第一页-->第三页
						animation = new TranslateAnimation(0, two - 10, 0, 0);
					} else if (currentTabPager == TAB_INDEX_DETAILS) { //第二页-->第三页
						animation = new TranslateAnimation(one, two - 10, 0, 0);
					} else if(currentTabPager == TAB_INDEX_HEAD){//第四页-->第三页
						animation = new TranslateAnimation(three, two - 10, 0, 0);
					}
					break;
				case TAB_INDEX_HEAD:  //第四也
					if(currentTabPager == TAB_INDEX_BASIC){//第一页-->第四页
						animation = new TranslateAnimation(0, three - 15, 0, 0);
					} else if(currentTabPager == TAB_INDEX_DETAILS){ //第二页-->第四页
						animation = new TranslateAnimation(one, three - 15, 0, 0);
					} else if(currentTabPager == TAB_INDEX_MATCH){ //第三页-->第四页
						animation = new TranslateAnimation(two, three - 15, 0, 0);
					}
					break;
				}

				currentTabPager = pageIndex; // 当前选择Tab索引
				setImageBackground(pageIndex);

				// --------------------------
				animation.setFillAfter(true);// True:图片停在动画结束位置
				animation.setDuration(700);
				tabArrows.startAnimation(animation);

			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				//do nothing
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				//do nothing
			}

		});
	}


	/**
	 * 设置tab页背景
	 * @param position
	 */
	private void setImageBackground(int position) {
		switch (position) {
		case TAB_INDEX_BASIC:  //第一页， 基本资料
			tvBasic.setBackgroundResource(R.drawable.center_tab_left_fg);

			tvDetail.setBackgroundResource(R.drawable.center_tab_middle_bg);

			tvMatch.setBackgroundResource(R.drawable.center_tab_middle_bg);

			tvHead.setBackgroundResource(R.drawable.center_tab_right_bg);
			break;
		case TAB_INDEX_DETAILS: //第二页，  详细资料
			tvBasic.setBackgroundResource(R.drawable.center_tab_left_bg);

			tvDetail.setBackgroundResource(R.drawable.center_tab_middle_fg);

			tvMatch.setBackgroundResource(R.drawable.center_tab_middle_bg);
			tvHead.setBackgroundResource(R.drawable.center_tab_right_bg);
			break;
		case TAB_INDEX_MATCH: //第三页 ， 比赛资料
			tvBasic.setBackgroundResource(R.drawable.center_tab_left_bg);

			tvDetail.setBackgroundResource(R.drawable.center_tab_middle_bg);

			tvMatch.setBackgroundResource(R.drawable.center_tab_middle_fg);
			tvHead.setBackgroundResource(R.drawable.center_tab_right_bg);
			break;
		case TAB_INDEX_HEAD: //第四页，  修改头像
			tvBasic.setBackgroundResource(R.drawable.center_tab_left_bg);

			tvDetail.setBackgroundResource(R.drawable.center_tab_middle_bg);

			tvMatch.setBackgroundResource(R.drawable.center_tab_middle_bg);
			tvHead.setBackgroundResource(R.drawable.center_tab_right_fg);
			break;
		default:
			break;
		}
	}
	
	
    /**
     * 初始化viewpager
     */
	private void initTabContent() {
		mViews = new ArrayList<View>();
		mInflater = getLayoutInflater();
		mBasicView = mInflater.inflate(R.layout.basic_info, null);
		initLayBasic(mBasicView);
		initDaShiScore(mBasicView);

		mDetailView = mInflater.inflate(R.layout.detail_info, null);
		initLayDetail(mDetailView);

		mMatchView = mInflater.inflate(R.layout.match_info, null);
		initLayMatch(mMatchView);

		mHeadView = mInflater.inflate(R.layout.head_info, null);
		initHead(mHeadView);

		// 添加进容器中
		mViews.add(mBasicView);
		mViews.add(mDetailView);
		mViews.add(mMatchView);
		mViews.add(mHeadView);
	}

	
	
	private void initProvincePopuWindow() {

		provinceWindow = mInflate.inflate(R.layout.pw_province_window, null);
		provinceList = (ListView) provinceWindow.findViewById(R.id.pw_province_list);
		provinceAdapter = new ProvinceAdapter(mPopHandler, provinces);
		provinceList.setAdapter(provinceAdapter);
		provincePopupWindow = new PopupWindow(provinceWindow, pwidth, LayoutParams.WRAP_CONTENT, true);
		provincePopupWindow.setBackgroundDrawable(null);

		provincePopupWindow.setFocusable(true);
	}
	
	

	private void initCityPopuWindow() {

		cityWindow = mInflate.inflate(R.layout.pw_city_window, null);
		cityList = (ListView) cityWindow.findViewById(R.id.pw_city_list);
		List<City> detailCitys = new ArrayList<City>();
		detailCitys.add(new City("-1", this.getResources().getString(R.string.chose_city)));
		cityAdapter = new CityAdapter(mPopHandler, detailCitys);
		cityList.setAdapter(cityAdapter);
		cityPopupWindow = new PopupWindow(cityWindow, pwidth, LayoutParams.WRAP_CONTENT, true);
		cityPopupWindow.setBackgroundDrawable(null);

		cityPopupWindow.setFocusable(true);
	}
	
	

	public void popupWindwShowing(int type) {
		if(type == 0){
			provincePopupWindow.showAsDropDown(provinceEt);
		}
		if(type == 1){
			cityPopupWindow.showAsDropDown(cityEt);
		}
	}
	
	

	public class ProvinceAdapter extends BaseAdapter {

		private Handler handler;

		private List<Province> list;

		public ProvinceAdapter(Handler handler, List<Province> list) {
			this.list = list;
			this.handler = handler;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ProvinceViewHolder holder = null;
			if (convertView == null) {
				holder = new ProvinceViewHolder();
				// 下拉项布局
				convertView = LayoutInflater.from(mContext).inflate(R.layout.province_popu_item, null);
				holder.name = (TextView) convertView.findViewById(R.id.tv_name);

				convertView.setTag(holder);
			} else {
				holder = (ProvinceViewHolder) convertView.getTag();
			}

			final Province currentProvince = list.get(position);

			holder.name.setText(currentProvince.getName());

			if(provinceCode.equals(currentProvince.getId())){
				holder.name.setBackgroundColor(mContext.getResources().getColor(R.color.area_bg_color));
			}else{
				holder.name.setBackgroundColor(mContext.getResources().getColor(R.color.white));
			}

			holder.name.setOnClickListener(new View.OnClickListener(){

				@Override
				public void onClick(View v) {
					v.setBackgroundColor(mContext.getResources().getColor(R.color.area_bg_color));
					handler.sendMessage(handler.obtainMessage(0, currentProvince));
				}
			});
			return convertView;
		}

	}
	
	

	public class CityAdapter extends BaseAdapter {

		private Handler handler;

		private List<City> list;

		public CityAdapter(Handler handler, List<City> list) {
			this.handler = handler;
			this.list = list;
		}

		@Override
		public int getCount() {
			return list == null ? 0 : list.size();
		}

		@Override
		public Object getItem(int position) {
			return list == null ? null : list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		public void setList(List<City> list) {
			this.list = list;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			CityViewHolder holder = null;
			final int curPosition = position;
			if (convertView == null) {
				holder = new CityViewHolder();
				// 下拉项布局
				convertView = LayoutInflater.from(mContext).inflate(R.layout.city_popu_item, null);
				holder.name = (TextView) convertView.findViewById(R.id.tv_name);

				convertView.setTag(holder);
			} else {
				holder = (CityViewHolder) convertView.getTag();
			}

			final City currentCity = list.get(position);

			holder.name.setText(currentCity.getName());

			if(cityCode.equals(currentCity.getId())){
				holder.name.setBackgroundColor(mContext.getResources().getColor(R.color.area_bg_color));
			}else{
				holder.name.setBackgroundColor(mContext.getResources().getColor(R.color.white));
			}

			holder.name.setOnClickListener(new View.OnClickListener(){

				@Override
				public void onClick(View v) {
					Object obj;
					obj = list.get(curPosition);
					v.setBackgroundColor(mContext.getResources().getColor(R.color.area_bg_color));
					handler.sendMessage(handler.obtainMessage(1, obj));
				}
			});
			return convertView;
		}

	}

	static class ProvinceViewHolder {
		TextView name;
	}

	static class CityViewHolder {
		TextView name;
	}

	Province curProvince;

	@SuppressLint("HandlerLeak")
	private Handler mPopHandler = new Handler() {
		/**
		 * 处理Hander消息
		 */
		@Override
		public void handleMessage(Message message) {
			switch (message.what) {
			// 填充省份信息
			case 0:
				Province province = (Province)message.obj;
				curProvince = province;
				provinceEt.setText(province.getName());
				provinceCode = province.getId();
				cityAdapter.setList(province.getCitys());
				cityAdapter.notifyDataSetChanged();
				cityEt.setText(mContext.getResources().getString(R.string.chose_city));
				cityCode = "-1";

				// 选中下拉项，下拉框消失
				dismiss(0);
				break;
			case 1:
				City city = (City)message.obj;
				cityEt.setText(city.getName());
				cityCode = city.getId();
				// 选中下拉项，下拉框消失
				dismiss(1);
				break;
			}

		}
	};

	private void dismiss(int type) {
		PopupWindow window = type ==0 ? provincePopupWindow : cityPopupWindow;
		if(window != null && window.isShowing()){
			window.dismiss();
		}
	}

	private void initDaShiScore(View mMatchView) {

		tvDashiScore = (TextView) mMatchView.findViewById(R.id.tvDashiScore);
		tvDashiName = (TextView) mMatchView.findViewById(R.id.tvDashiName);
		tvWhatDashiScore = (TextView) mMatchView
				.findViewById(R.id.tvWhatDashiScore);
		tvWhatDashiScore.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);// 文字带下划线

		tvWhatDashiScore.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				UserCenterData userData=UserCenterProvider.getInstance().getUserCenterData();
				if(userData!=null){
					showWebView(userData.masterHelpID);
				}
			}
		});

	}

	/****
	 * @Title: initsetValue
	 * @Description: 设置大师分内容
	 * @param score
	 * @version: 2013-2-1 上午09:09:29
	 */
	private void initsetValue(int score, short urlId) {
		if (tvDashiScore != null) {
			tvDashiScore.setText(score + "");
		}
		if (tvDashiName != null) {
			tvDashiName.setText(ScoreConfig.getShowNamefromScore(
					AppConfig.gameId, score));
		}
		if (tvWhatDashiScore != null) {
			tvWhatDashiScore.setTag(String.valueOf(urlId));
		}
	}

	private void initLayMatch(View v) {
		tvPoint = (TextView) v.findViewById(R.id.point);
		tvRank = (TextView) v.findViewById(R.id.rank);

		btnRank = (Button) v.findViewById(R.id.btn_rank);
		btnQuery = (Button) v.findViewById(R.id.btn_query);

		btnRank.setOnClickListener(this);
		btnQuery.setOnClickListener(this);
	}

	private void initLayDetail(View v) {
		tvRealName = (TextView) v.findViewById(R.id.tvRealName);
		tvCard = (TextView) v.findViewById(R.id.card_text);

		etRealName = (EditText) v.findViewById(R.id.real_name);
		etCard = (EditText) v.findViewById(R.id.card);
		etPhoneNum = (EditText) v.findViewById(R.id.phone_num);

		btnSave = (Button) v.findViewById(R.id.large_save);
		btnSave.setOnClickListener(this);

		provinceIv = (ImageView) v.findViewById(R.id.province_btn);
		cityIv = (ImageView) v.findViewById(R.id.city_btn);
		provinceEt = (EditText) v.findViewById(R.id.area_et);
		cityEt = (EditText) v.findViewById(R.id.city_et);

		tvProvince = (TextView) v.findViewById(R.id.tvProvince);
		tvCity = (TextView) v.findViewById(R.id.tvCity);

		tvMoreInfo = (TextView) v.findViewById(R.id.more_info);
		tvMoreInfo.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);

		tvBasic.setOnClickListener(this);
		tvDetail.setOnClickListener(this);
		tvMatch.setOnClickListener(this);
		tvHead.setOnClickListener(this);
		provinceIv.setOnClickListener(this);
		cityIv.setOnClickListener(this);
		provinceEt.setOnClickListener(this);
		cityEt.setOnClickListener(this);
		tvMoreInfo.setOnClickListener(this);

		getWidth(); // 获取省份框宽度
	}

	private void initLayBasic(View v) {
		tvAccount = (TextView) v.findViewById(R.id.account);
		tvID = (TextView) v.findViewById(R.id.id);
		tvBeans = (TextView) v.findViewById(R.id.bean);
		tvGold = (TextView) v.findViewById(R.id.gold);
		tvTicket = (TextView) v.findViewById(R.id.ticket);
		//tvNickName = (TextView) v.findViewById(R.id.nick_text);

		// 修改密码： 游客账号进入个人中心不应该有修改密码2013-1-26
		btnModify = (Button) v.findViewById(R.id.change);

		btnEnsure = (Button) v.findViewById(R.id.ensure);
		btnExchange = (Button) v.findViewById(R.id.exchange);
		btnReceive = (Button) v.findViewById(R.id.receive);

		ivDiamond = (ImageView) v.findViewById(R.id.diamond);

		updateUI();

		btnModify.setOnClickListener(this);
		btnEnsure.setOnClickListener(this);
		btnExchange.setOnClickListener(this);
		btnReceive.setOnClickListener(this);

		etNickName = (EditText) v.findViewById(R.id.nick_name);

		mRadioGroup = (RadioGroup) v.findViewById(R.id.sex_radio);
		mRadioGroup.setOnCheckedChangeListener(this);
		btnMale = (RadioButton) v.findViewById(R.id.btn_male);
		btnFemale = (RadioButton) v.findViewById(R.id.btn_female);
	}


	private void initHead(View v){
		mHeadListView = (ListView)v.findViewById(R.id.headlist);
		if(HeadManager.getInstance().isGetHeadPackFinish()){   //已经下载信息完成
			List<HeadInfo> lists = HeadManager.getInstance().getHeadInfoList();
			if(lists.size()>0){
				lists.get(0).isArrowUp = true;
			}
			marketAdapter = new HeadAdapter(this, lists);
			mHeadListView.setAdapter(marketAdapter);
		}
		mHeadListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View convertView, int pos,
					long id) {
				// TODO Auto-generated method stub
				HeadInfo item = (HeadInfo) marketAdapter.getItem(pos);
				item.isArrowUp = !item.isArrowUp;
				marketAdapter.notifyDataSetChanged();
				if (pos != 0) {
					mHeadListView.setSelectionFromTop(pos, convertView.getHeight() / 2);
				}
			}});
	}

	@Override
	public void onClick(View v) {
		String userToken=FiexedViewHelper.getInstance().getUserToken();
		int id = v.getId();
		if (id == R.id.tv_basic) {
			setImageBackground(TAB_INDEX_BASIC);
			mViewPager.setCurrentItem(TAB_INDEX_BASIC);
		} else if (id == R.id.tv_detail) {
			setImageBackground(TAB_INDEX_DETAILS);
			mViewPager.setCurrentItem(TAB_INDEX_DETAILS);
		} else if (id == R.id.tv_match) {
			setImageBackground(TAB_INDEX_MATCH);
			mViewPager.setCurrentItem(TAB_INDEX_MATCH);
		}else if(id == R.id.tv_head){
			setImageBackground(TAB_INDEX_HEAD);
			mViewPager.setCurrentItem(TAB_INDEX_HEAD);
		} else if (id == R.id.change) {
			startModifyPasswordActivity(userToken);
		} else if (id == R.id.ensure) {
			nickName = etNickName.getText().toString().trim();
			if (nickName.length() > 0) {
				modifyUserInfo(nickName, sexId);
			}
		} else if (id == R.id.exchange) {
			try {
				UserCenterData userData=UserCenterProvider.getInstance().getUserCenterData();
				if(userData!=null){
					showWebView(userData.duihuanurlid);
				}
			} catch (Exception e) {

			}
		} else if (id == R.id.receive) {
			//startGetTicketActivity();
			String url=AppConfig.NEW_GETTICKET_URL;
			int userId=FiexedViewHelper.getInstance().getUserId();
			url += "&at=" + userToken + "&";
			String finalUrl = CenterUrlHelper.getUrl(url, userId);
			UtilHelper.onWeb(UserCenterActivity.this, finalUrl);

		} else if (id == R.id.large_save) {
			// "^[1-9]\\d{7}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}$";
			// String isIDCard2 =
			// "^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{4}$";

			mobile = etPhoneNum.getText().toString();

			if ("".equals(realName) || null == realName) {
				realName = etRealName.getText().toString();
			}
			if ("".equals(idCardNo) || null == idCardNo) {
				idCardNo = etCard.getText().toString();
			}

			// if (!(Pattern.compile(isIDCard1).matcher(idCardNo).matches() ||
			// Pattern
			// .compile(isIDCard2).matcher(idCardNo).matches())) {
			// Toast.makeText(getApplication(), "请输入正确的身份证格式",
			// Toast.LENGTH_SHORT).show();
			// }

			boolean isCard = "".equals(idCardNo) || (idCardNo.length() >= 15);

			boolean isPhoneNum = "".equals(mobile)
					|| Pattern.compile("^1[3458]\\d{9}$").matcher(mobile)
					.matches();
			String areaCode = provinceCode + cityCode;
			boolean isAreaCode = "-1-1".equals(areaCode) || areaCode.indexOf("-1") == -1;
			boolean isAllNull = "".equals(idCardNo) && "".equals(mobile)
					&& "".equals(realName) && "-1-1".equals(areaCode);
			if (isCard && isPhoneNum && isAreaCode && (!isAllNull)) {
				modifyUserInfoExSave(realName, idCardNo, mobile, areaCode);
			} else {
				if (!isCard) {
					Toast.makeText(getApplication(), mResource.getString(R.string.info_shenfenzheng_error),
							Toast.LENGTH_SHORT).show();
				} else if (!isPhoneNum) {
					Toast.makeText(getApplication(), mResource.getString(R.string.info_phone_num_error),
							Toast.LENGTH_SHORT).show();
				} else if (!isAreaCode && !"-1".equals(provinceCode) && "-1".equals(cityCode)){
					// 输入省市信息
					Toast.makeText(getApplication(), mResource.getString(R.string.info_citycode_error),
							Toast.LENGTH_SHORT).show();

				}
				else {
					Toast.makeText(getApplication(), mResource.getString(R.string.info_empty),
							Toast.LENGTH_SHORT).show();
				}

			}
		} else if (id == R.id.btn_rank) {
			UserCenterData userData=UserCenterProvider.getInstance().getUserCenterData();
			if(userData!=null){
				showWebView(userData.paimingurlid);
			}
		} else if (id == R.id.btn_query) {
			UserCenterData userData=UserCenterProvider.getInstance().getUserCenterData();
			if(userData!=null){
				showWebView(userData.morepaimingurlid);
			}
		} else if (id == R.id.province_btn) {
			dismiss(1);
			if(!provincePopupWindow.isShowing()){
				popupWindwShowing(0);
			}else{
				dismiss(0);
			}

		} else if (id == R.id.city_btn) {
			dismiss(0);
			if(!cityPopupWindow.isShowing()){
				popupWindwShowing(1);
			}else{
				dismiss(1);
			}
		} else if (id == R.id.area_et) {
			dismiss(1);
			if(!provincePopupWindow.isShowing()){
				popupWindwShowing(0);
			}else{
				dismiss(0);
			}

		} else if (id == R.id.city_et) {
			dismiss(0);
			if(!cityPopupWindow.isShowing()){
				popupWindwShowing(1);
			}else{
				dismiss(1);
			}
		}
		else if(id == R.id.btnFreeBean){
			//免费赚豆
			Intent intent = new Intent(mContext, MoregameActivity.class);
			startActivity(intent);
		}
		else if(id == R.id.more_info){
			String finalUrl = UtilHelper.getMoreInfoUrl(userToken);
			UtilHelper.onWeb(mContext, finalUrl);
		}

	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		int id = group.getId();
		if (id == R.id.sex_radio) {
			if (checkedId == btnMale.getId()) {
				sexId = 1;
			} else if (checkedId == btnFemale.getId()) {
				sexId = 0;
			}
		}

	}

	public void startModifyPasswordActivity(String token) {
		Intent modify = new Intent(getApplication(),
				ModifyPasswordActivity.class);
		startActivity(modify);
	}

	public void startGetTicketActivity() {
		Intent getTicket = new Intent(getApplication(), GetTicketActivity.class);
		startActivityForResult(getTicket, 0);
	}

	/****
	 * 定义一个Handler处理线程发送的消息，并更新主UI线程
	 */
	@SuppressLint("HandlerLeak")
	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {

			int userId=FiexedViewHelper.getInstance().getUserId();
			switch (msg.what) {
			case HANDLER_USERCENTER_SUCCESS: // 个人中心数据下发成功

				UserCenterData userData=UserCenterProvider.getInstance().getUserCenterData();
				if(userData!=null){
					setCenterText(userData);
				}
				break;

			case HANDLER_USERCENTER_FAIL: // 接受错误数据
				Toast.makeText(getApplication(), msg.obj.toString(),
						Toast.LENGTH_SHORT).show();
				break;

			case HANDLER_MODIFY_SUCCESS: // 修改昵称，性别成功
				Toast.makeText(getApplication(), msg.obj.toString(),
						Toast.LENGTH_SHORT).show();
				UserInfo userInfo = HallDataManager.getInstance().getUserMe();
				userInfo.gender = sexId;
				userInfo.nickName = nickName;

				UserCenterData data=UserCenterProvider.getInstance().getUserCenterData();
				if(data!=null){
					data.setNickName(nickName);
					data.setSex(sexId);
				}
				break;

			case HANDLER_MODIFY_EX_SUCCESS:

				updateUserInfo(realName, idCardNo, mobile);
				Toast.makeText(getApplication(), msg.obj.toString(),
						Toast.LENGTH_SHORT).show();

				break;

			case HANDLER_MODIFY_EX_FAIL:
				Toast.makeText(getApplication(), msg.obj.toString(),
						Toast.LENGTH_SHORT).show();

				break;

			case HANDLER_MODIFYPASSWORD_FAIL: // 接受错误数据
				if (msg.obj == null) {
					msg.obj = mResource.getString(R.string.info_modify_failed);
				}
				Toast.makeText(getApplication(), msg.obj.toString(),
						Toast.LENGTH_SHORT).show();
				break;

			case HANDLER_USERINFO_SUCCESS:

				updateUserInfo(realName, idCardNo, mobile);

				break;

			case HANDLER_USERINFO_FAIL:
				Toast.makeText(getApplication(), msg.obj.toString(),
						Toast.LENGTH_SHORT).show();
				break;

			case HANDLER_FAIL: // 网络解析数据出现Error
				Toast.makeText(getApplication(), mResource.getString(R.string.info_obtain_failed),
						Toast.LENGTH_SHORT).show();
				break;

			case HANDLER_HUAFEIQUAN_SUCCESS:
				String ticket=msg.obj.toString();
				tvTicket.setText(ticket);
				break;
			case HANDLER_START_ANIM:
				if(btnFreeBean != null){
					AnimationDrawable ad = (AnimationDrawable) btnFreeBean.getBackground();
					ad.stop();
					ad.start();
				}
				break;
			case HANDLER_AREA_INFO:
				for (Province province : provinces) {
					if(provinceCode.equals(province.getId())){
						provinceEt.setVisibility(View.GONE);
						provinceIv.setVisibility(View.GONE);
						tvProvince.setText(province.getName());
						tvProvince.setVisibility(View.VISIBLE);
						List<City> cityList = province.getCitys();
						for (City city : cityList) {
							if(cityCode.equals(city.getId())){
								cityEt.setVisibility(View.GONE);
								cityIv.setVisibility(View.GONE);
								tvCity.setText(city.getName());
								tvCity.setVisibility(View.VISIBLE);
								break;
							}
						}
						break;
					}
				}
				break;
			case HANDLER_UPDATE_HEAD:    //更新头像
				if(HeadManager.getInstance().isGetHeadPackFinish()){    //头像数据加载完成
					if(marketAdapter == null){   //没初始化，则初始化
						List<HeadInfo> lists = HeadManager.getInstance().getHeadInfoList();
						if(lists.size()>0){
							lists.get(0).isArrowUp = true;
						}
						marketAdapter = new HeadAdapter(UserCenterActivity.this, lists);
						mHeadListView.setAdapter(marketAdapter);
					}else{   //已初始化，更新显示
						marketAdapter.notifyDataSetChanged();
						mHeadListView.invalidate();
						if(msg.obj != null){
							Toast.makeText(mContext, (String)msg.obj, Toast.LENGTH_SHORT).show();
						}
					}
				}else{
					HeadManager.getInstance().requestHeadPackList(userId);
				}
				break;
			default:
				break;
			}
		}
	};

	// ---------------------定义消息发送标识，提供与handler------------------------

	public static final int HANDLER_USERCENTER_SUCCESS = 10;

	public static final int HANDLER_USERCENTER_FAIL = 20;

	public static final int HANDLER_FAIL = 30;

	public static final int HANDLER_MODIFY_SUCCESS = 40;

	public static final int HANDLER_MODIFY_FAIL = 50;

	public static final int HANDLER_MODIFYPASSWORD_FAIL = 60;

	public static final int HANDLER_USERINFO_SUCCESS = 80;

	public static final int HANDLER_USERINFO_FAIL = 70;

	public static final int HANDLER_MODIFY_EX_SUCCESS = 90;

	public static final int HANDLER_MODIFY_EX_FAIL = 100;

	public static final int HANDLER_HUAFEIQUAN_SUCCESS = 110;

	private static final int HANDLER_START_ANIM = 120;   //启动动画

	private static final int HANDLER_AREA_INFO = 121; // 处理省市信息

	private static final int HANDLER_UPDATE_HEAD = 130;  //更新头像
	// -----------------------------------协议数据处理--------------------------------------------------
	private static final short LS_TRANSIT_LOGON = 18;
	/** 子协议-个人信息修改请求 */
	private static final short MSUB_CMD_MODIFY_USERINFO = 1;
	/** 子协议-个人信息修改结果 */
	private static final short MSUB_CMD_MODIFY_RESULT = 2;
	/** 子协议-个人中心信息请求 */
	private static final short MSUB_USERCENTER_INFO_REQ = 115;
	/** 子协议-个人中心信息返回 */
	private static final short MSUB_USERCENTER_INFO_RESP = 116;
	/** 子协议-个人中心信息读取失败 */
	private static final short MSUB_USERCENTER_INFO_FAIL = 117;
	/** 子协议-获取用户信息请求 */
	private static final short MSUB_CMD_USERINFO_EX_REQ = 22;
	/** 子协议-获取用户信息结果 */
	private static final short MSUB_CMD_USERINFO_EX_RESP = 23;
	/** 子协议-用户信息保存请求 */
	private static final short MSUB_CMD_USERINFO_EX_SAVE_REQ = 24;
	/** 子协议-用户信息保存结果 */
	private static final short MSUB_CMD_USERINFO_EX_SAVE_RESP = 25;

	// --------------个人中心-----------------

	private void getUserCenterInfo(int userId, int clentId) {
		// 创建发送的数据包
		TDataOutputStream tdos = new TDataOutputStream(false);
		tdos.writeInt(userId);
		// tdos.writeInt(8080);
		tdos.writeInt(clentId);

		NetSocketPak centerInfo = new NetSocketPak(LS_TRANSIT_LOGON,
				MSUB_USERCENTER_INFO_REQ, tdos);
		// 定义接受数据的协议
		short[][] parseProtocol = {
				{ LS_TRANSIT_LOGON, MSUB_USERCENTER_INFO_RESP },
				{ LS_TRANSIT_LOGON, MSUB_USERCENTER_INFO_FAIL } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			public boolean doReceive(NetSocketPak netSocketPak) {
				// 解析接受到的网络数据
				try {
					short sub_gr = netSocketPak.getSub_gr();
					if (sub_gr == MSUB_USERCENTER_INFO_RESP) {
						TDataInputStream tdis = netSocketPak.getDataInputStream();
						UserCenterData userData= new UserCenterData(tdis);
						UserCenterProvider.getInstance().setUserCenterData(userData);
						Message msg = handler.obtainMessage(HANDLER_USERCENTER_SUCCESS);
						handler.sendMessage(msg);

					} else if (sub_gr == MSUB_USERCENTER_INFO_FAIL) {
						TDataInputStream tdis = netSocketPak
								.getDataInputStream();
						tdis.readByte();  //byte errorCode ,未使用
						String errMsg = tdis.readUTFByte();
						Log.e(TAG, errMsg);
						handler.sendMessage(handler.obtainMessage(
								HANDLER_USERCENTER_FAIL, errMsg));
					}

				} catch (Exception e) {
					handler.sendMessage(handler.obtainMessage(HANDLER_FAIL));
					e.printStackTrace();
				}
				// 数据处理完成，终止继续解析
				return true;
			}
		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		// 发送协议
		NetSocketManager.getInstance().sendData(centerInfo);
		// 清理协议对象
		centerInfo.free();
	}

	private void modifyUserInfo(String nickName, byte sexId) {
		// 创建发送的数据包
		int userId=FiexedViewHelper.getInstance().getUserId();
		TDataOutputStream tdos = new TDataOutputStream(false);
		tdos.writeInt(userId);
		tdos.writeByte(sexId);
		tdos.writeUTFByte(nickName);

		NetSocketPak userInfo = new NetSocketPak(LS_TRANSIT_LOGON,
				MSUB_CMD_MODIFY_USERINFO, tdos);

		// 发送协议
		NetSocketManager.getInstance().sendData(userInfo);
	}

	private void modifyUserInfoListener() {
		// 定义接受数据的协议
		short[][] parseProtocol = { { LS_TRANSIT_LOGON, MSUB_CMD_MODIFY_RESULT } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			public boolean doReceive(NetSocketPak netSocketPak) {
				// 解析接受到的网络数据
				try {
					TDataInputStream tdis = netSocketPak.getDataInputStream();
					byte result = tdis.readByte();
					String errMsg = tdis.readUTFByte();
					if (result == 1) {// 成功
						handler.sendMessage(handler.obtainMessage(
								HANDLER_MODIFY_SUCCESS, errMsg));
					} else if (result == 0) {// 失败

						handler.sendMessage(handler.obtainMessage(
								HANDLER_USERINFO_FAIL, errMsg));
					}
					//
				} catch (Exception e) {
					//
					handler.sendMessage(handler.obtainMessage(HANDLER_FAIL));
					e.printStackTrace();
				}
				// 数据处理完成，终止继续解析
				return true;
			}
		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		nPListener.setOnlyRun(false);
	}

	private void modifyUserInfoEx() {
		// 创建发送的数据包
		TDataOutputStream tdos = new TDataOutputStream();
		String test = buildUserInfoEx();
		tdos.setFront(false);
		tdos.writeUTF(test);

		NetSocketPak userInfoEx = new NetSocketPak(LS_TRANSIT_LOGON,
				MSUB_CMD_USERINFO_EX_REQ, tdos);
		// 定义接受数据的协议
		short[][] parseProtocol = { { LS_TRANSIT_LOGON,
			MSUB_CMD_USERINFO_EX_RESP } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			public boolean doReceive(NetSocketPak netSocketPak) {
				TDataInputStream tdis = netSocketPak.getDataInputStream();
				// 解析接受到的网络数据
				try {
					String data = new String(tdis.readBytes(),"UTF-8");
					String status = UtilHelper.parseStatusXml(data, "status");
					//String statusnote = UtilHelper.parseStatusXml(data, "statusnote");

					if (UtilHelper.stringToInt(status, -1) == 0) {
						realName = UtilHelper.parseStatusXml(data, "realname");
						idCardNo = UtilHelper.parseStatusXml(data, "idcardno");
						mobile = UtilHelper.parseStatusXml(data, "mobile");
						String areaCode = UtilHelper.parseStatusXml(data, "areacode");
						if(!Util.isEmptyStr(areaCode)){
							provinceCode = areaCode.substring(0, 2);
							cityCode = areaCode.substring(2, 4);
							handler.sendEmptyMessage(HANDLER_AREA_INFO);
						}
						Message msg = handler
								.obtainMessage(HANDLER_USERINFO_SUCCESS);
						handler.sendMessage(msg);

						//20130617 增加话费券
						String ticket = UtilHelper.parseStatusXml(data,"mobilevoucher");
						if(Util.isEmptyStr(ticket)){
							ticket=0+mContext.getString(R.string.market_yuan);
						}else{
							ticket=ticket+mContext.getString(R.string.market_yuan);
						}

						handler.sendMessage(handler.obtainMessage(
								HANDLER_HUAFEIQUAN_SUCCESS, ticket));

					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				// 数据处理完成，终止继续解析
				return true;
			}
		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		// 发送协议
		NetSocketManager.getInstance().sendData(userInfoEx);
		// 清理协议对象
		userInfoEx.free();
	}

	private void modifyUserInfoExSave(String realname, String idcardno,
			String mobile, String areaCode) {
		// 创建发送的数据包

		TDataOutputStream tdos = new TDataOutputStream();
		String userinfoex = buildUserInfoExSave(realname, idcardno, mobile, areaCode);
		tdos.setFront(false);
		tdos.writeUTF(userinfoex);

		NetSocketPak userInfoExSave = new NetSocketPak(LS_TRANSIT_LOGON,
				MSUB_CMD_USERINFO_EX_SAVE_REQ, tdos);
		// 定义接受数据的协议
		short[][] parseProtocol = { { LS_TRANSIT_LOGON,
			MSUB_CMD_USERINFO_EX_SAVE_RESP } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			public boolean doReceive(NetSocketPak netSocketPak) {
				// 解析接受到的网络数据
				try {
					TDataInputStream tdis = netSocketPak.getDataInputStream();
					String data = new String(tdis.readBytes(),"UTF-8");
					String status = UtilHelper.parseStatusXml(data, "status");
					String statusnote = UtilHelper.parseStatusXml(data, "statusnote");
					if (UtilHelper.stringToInt(status, -1) == 0) {
						handler.sendMessage(handler.obtainMessage(
								HANDLER_MODIFY_EX_SUCCESS, statusnote));
					} else {
						handler.sendMessage(handler.obtainMessage(
								HANDLER_MODIFY_EX_FAIL, statusnote));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				// 数据处理完成，终止继续解析
				return true;
			}
		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		// 发送协议
		NetSocketManager.getInstance().sendData(userInfoExSave);
		// 清理协议对象
		userInfoExSave.free();
	}

	/****
	 * @Title: setCenterText
	 * @Description: 设置个人中心数据
	 * @param mHIndividualInfo
	 * @version: 2013-1-9 上午10:37:35
	 */
	private void setCenterText(UserCenterData userData) {
		tvAccount.setText(userData.account);
		tvID.setText(String.valueOf(userData.ID));
		tvBeans.setText((String.valueOf(userData.leDou)));
		tvGold.setText(String.valueOf(userData.yuanbao));
		tvTicket.setText(String.valueOf(userData.huafeiquan) + " "+mResource.getString(R.string.market_yuan));
		tvPoint.setText(String.valueOf(userData.zhoujifen));
		tvRank.setText(String.valueOf(userData.zhoupaiming));
		if(userData.sex==1){
			btnMale.setChecked(true);
		}else{
			btnFemale.setChecked(true);
		}
		etNickName.setText(userData.nickName);

		// 设置数据
		initsetValue(userData.masterScore, userData.masterHelpID);

	}

	/*
	 * private void setUserInfo(long bean,byte gender,String nickName){
	 * //2013-1-9新增：更新用户数据信息 if(userInfo!=null){
	 * userInfo.lBean=userInfo.bean=(int) bean; userInfo.gender=gender;
	 * userInfo.nickName=nickName; } }
	 */

	private void updateUserInfo(String realName, String idCardNo, String mobile) {
		if (realName.trim().equals("")) {
			etRealName.setVisibility(View.VISIBLE);
			tvRealName.setVisibility(View.GONE);
		} else {
			tvRealName.setText(realName);
			etRealName.setVisibility(View.GONE);
			tvRealName.setVisibility(View.VISIBLE);
		}
		if (idCardNo.trim().equals("")) {
			etCard.setVisibility(View.VISIBLE);
			tvCard.setVisibility(View.GONE);
		} else {
			if (idCardNo.length() > 14) {
				tvCard.setText(idCardNo.substring(0, idCardNo.length() - 4)
						+ "****");
			} else {
				tvCard.setText(idCardNo);
			}

			etCard.setVisibility(View.GONE);
			tvCard.setVisibility(View.VISIBLE);
		}

		// 设置省市信息
		if(provinceCode != null && !"".equals(provinceCode)){
			for (Province province : provinces) {
				if(!"-1".equals(provinceCode) && provinceCode.equals(province.getId())){
					/*provinceEt.setText(province.getName());*/
					provinceEt.setVisibility(View.GONE);
					provinceIv.setVisibility(View.GONE);
					tvProvince.setText(province.getName());
					tvProvince.setVisibility(View.VISIBLE);
					List<City> cityList = province.getCitys();
					for (City city : cityList) {
						if(!"-1".equals(cityCode) && cityCode.equals(city.getId())){
							/*cityEt.setText(city.getName());*/
							cityEt.setVisibility(View.GONE);
							cityIv.setVisibility(View.GONE);
							tvCity.setText(city.getName());
							tvCity.setVisibility(View.VISIBLE);
							break;
						}
					}
					/*cityAdapter.setList(cityList);
					cityAdapter.notifyDataSetChanged();*/
					break;
				}
			}
		}

		etPhoneNum.setText(mobile);
	}

	private String buildUserInfoEx() {
		StringBuffer sb = new StringBuffer();
		sb.append("<r>").append("<p n=\"");
		sb.append("realname");
		sb.append("\"/>").append("<p n=\"");
		sb.append("idcardno");
		sb.append("\"/>").append("<p n=\"");
		sb.append("mobile");
		sb.append("\"/>").append("<p n=\"");
		sb.append("mobilevoucher"); //追加话费券请求 20130617
		sb.append("\"/>").append("<p n=\"");
		sb.append("areacode"); //追加区域编码
		sb.append("\"/>").append("</r>");
		return sb.toString();
	}

	private String buildUserInfoExSave(String realname, String idcardno,
			String mobile, String areaCode) {

		StringBuffer sb = new StringBuffer();
		sb.append("<r>").append("<p n=\"");
		sb.append("realname").append("\"v=\"");
		sb.append(realname);
		sb.append("\"/>").append("<p n=\"");
		sb.append("idcardno").append("\"v=\"");
		sb.append(idcardno);
		sb.append("\"/>").append("<p n=\"");
		sb.append("mobile").append("\"v=\"");
		sb.append(mobile);
		sb.append("\"/>").append("<p n=\"");
		sb.append("areacode").append("\"v=\"");
		sb.append(areaCode);
		sb.append("\"/>").append("</r>");
		return sb.toString();
	}

	private void showWebView(short urlId) {
		int userId=FiexedViewHelper.getInstance().getUserId();
		String userToken;
		try {
			userToken = URLEncoder.encode(FiexedViewHelper.getInstance().getUserToken(),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			userToken="";
		}
		String url = CenterUrlHelper.getWapUrl(urlId);
		url += "at=" + userToken + "&";
		String finalUrl = CenterUrlHelper.getUrl(url, userId);
		UtilHelper.onWeb(mContext, finalUrl);
	}


	/*private String beansUtil(String str1) {
		str1 = new StringBuilder(str1).reverse().toString(); // 先将字符串颠倒顺序
		String str2 = "";
		for (int i = 0; i < str1.length(); i++) {
			if (i * 3 + 3 > str1.length()) {
				str2 += str1.substring(i * 3, str1.length());
				break;
			}
			str2 += str1.substring(i * 3, i * 3 + 3) + ",";
		}
		if (str2.endsWith(",")) {
			str2 = str2.substring(0, str2.length() - 1);
		}
		str1 = new StringBuilder(str2).reverse().toString();
		return str1;
	}*/

	/****
	 * @Title: updateUI
	 * @Description: 显示隐藏相关UI
	 * @version: 2013-1-26 下午02:36:36
	 */
	private void updateUI() {
		UserInfo userInfo = HallDataManager.getInstance().getUserMe();
		byte userLoginType = userInfo.loginType;
		byte vip = userInfo.memberOrder;
		if (userLoginType == AccountItem.ACC_TYPE_TEMP
				&& !LoginInfoManager.getInstance().isBind()) { // 是游客
			btnModify.setVisibility(View.GONE);
			btnModify.setEnabled(false);
		} else {
			btnModify.setVisibility(View.VISIBLE);
			btnModify.setEnabled(true);
		}
		if (vip > 10) { // 是会员
			ivDiamond.setVisibility(View.VISIBLE);
		} else {
			ivDiamond.setVisibility(View.GONE);
		}
	}

}