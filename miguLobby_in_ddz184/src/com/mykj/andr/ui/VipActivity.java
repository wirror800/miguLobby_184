package com.mykj.andr.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.mingyou.distributor.NetPrivateListener;
import com.mingyou.distributor.NetSocketPak;
import com.mykj.andr.model.HallDataManager;
import com.mykj.andr.model.VipData;
import com.mykj.andr.net.NetSocketManager;
import com.mykj.andr.provider.VipXmlParser;
import com.mykj.andr.ui.adapter.VipMineAdapter;
import com.mykj.andr.ui.adapter.VipPrivilegeAdapter;
import com.mykj.andr.ui.adapter.VipProfileAdapter;
import com.mykj.andr.ui.widget.CustomProgressBar;
import com.mykj.comm.io.TDataInputStream;
import com.mykj.comm.io.TDataOutputStream;
import com.MyGame.Migu.R;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.Log;
import com.mykj.game.utils.Toast;

/**
 * 
 * @ClassName: VipActivity
 * @Description: Vip功能
 * @author  
 * @date 2013-6-7 下午04:49:26
 *
 */
public class VipActivity extends Activity implements OnClickListener{

	
	private static final short MDM_USER = 2;
	/**请求个人VIP信息*/
	private static final short MSUB_GR_USER_VIP_REQ	= 19; 
	/**返回个人VIP信息*/
	private static final short MSUB_GR_USER_VIP_RESPONSE =20;
	/**领取奖励协议***/
	private static final short MSUB_GR_USER_VIP_ADWARD_REQ = 21;
	/***领取响应*/
	private static final short MSUB_GR_USER_VIP_ADWARD_RESPONSE =22;

	//个人VIP信息返回
	public static final int USER_VIP_MESSAGE_HANDLER=2402;

	
	protected static final String TAG = "VipActivity";

	// 当前选择的Tab索引
	protected int currentTabPager = 0;
	private static final int TAB_VIP_MINE = 0;// 我的VIP
	private static final int TAB_VIP_PRIVILEGE = 1;// VIP特权
	private static final int TAB_VIP_PROFILE   = 2;// 简介
	
	
	private TabContentPageAdapter mVipPageAdapter;
	private ViewPager mViewPager;
	private LayoutInflater mInflater;
	private List<View> mViews;
	
	private View vip_mine;
	private View vip_privilege;
	private View vip_profile; 
	
	protected ListView lvVip;
	protected ListView lvprivilege;
	protected ListView lvprofile; 
	
	TextView tv_vipmine;
	TextView tv_vip_privilege;
	TextView tv_vip_profile;
	 
	CustomProgressBar customProgressBar;
	TextView tvdegree;
	TextView tvEndDate;
    
	Button btnGivePresent;
	Button btnRenewalVip;
	
	private int offset = 0; // 动画图片偏移量
	private int bmpW;
	private ImageView tabArrows; // 动画图片

	// Tab卡的距离
	private int one = 0;
	private int two = 0;
    	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vip_main);
		initlize();
	}
	
	/**
	 * 控件初始化
	 */
	private void initlize() { 
		((TextView)findViewById(R.id.tvTitle)).setText(R.string.vip);
		findViewById(R.id.tvBack).setOnClickListener(this);

		tv_vipmine = (TextView) findViewById(R.id.tv_vipmine);
		tv_vip_privilege = (TextView) findViewById(R.id.tv_vip_privilege);
		tv_vip_profile = (TextView) findViewById(R.id.tv_vip_profile);

		tabArrows = (ImageView) findViewById(R.id.tab_arrows);
		
		tv_vipmine.setOnClickListener(this);
		tv_vip_privilege.setOnClickListener(this);
		tv_vip_profile.setOnClickListener(this);

		// 设置颜色半透明
		tv_vipmine.setTextColor(Color.argb(128, 255, 255, 255));
		tv_vip_privilege.setTextColor(Color.rgb(255, 255, 255));
		tv_vip_profile.setTextColor(Color.rgb(255, 255, 255));

		initTabContent();
	}
	
	/***
	 * 初始化TabContent内容布局
	 */
	private void initTabContent() {
		// 实例化ArrayList,容纳Tab页面内容
		mViews = new ArrayList<View>();
		mInflater = getLayoutInflater();

		// 解析布局文件
		vip_mine = mInflater.inflate(R.layout.vip_mine, null);
		vip_privilege = mInflater.inflate(R.layout.vip_privilege, null);
		vip_profile = mInflater.inflate(R.layout.vip_profile, null);

		// 从View中找寻ListView
		lvVip = (ListView) vip_mine.findViewById(R.id.lvVip);
		lvprivilege = (ListView) vip_privilege.findViewById(R.id.lvprivilege);
		lvprofile = (ListView) vip_profile.findViewById(R.id.lvprofile);
		
	 
        customProgressBar=(CustomProgressBar)vip_mine.findViewById(R.id.customProgressBar);
		tvdegree=(TextView)vip_mine.findViewById(R.id.tvdegree);
		tvEndDate=(TextView)vip_mine.findViewById(R.id.tvEndDate); 
		
		btnGivePresent=(Button)vip_mine.findViewById(R.id.btnGivePresent);
		btnGivePresent.setOnClickListener(this);
		btnRenewalVip=(Button)vip_mine.findViewById(R.id.btnRenewalVip); 
		btnRenewalVip.setOnClickListener(this);
		 
		// 当列表数据没有时候提示消息
		//tvSNoticeMsg = (TextView) vip_mine.findViewById(R.id.tvSNoticeMsg);
		//tvPNoticeMsg = (TextView) vip_privilege.findViewById(R.id.tvPNoticeMsg);
		//tvPNoticeMsg = (TextView) vip_profile.findViewById(R.id.tvPNoticeMsg);

		initImageView();
		
		initListViewCotent();
		// 添加进容器中
		mViews.add(vip_mine);
		mViews.add(vip_privilege);
		mViews.add(vip_profile);
		
		initPager(); 
	}
	
	private final int px2dip(Context context, float pxValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (pxValue / scale + 0.5f);  
    }  
	int tvBasicWidth;
	boolean hasMeasured = false;
	
	public void initImageView(){
        ViewTreeObserver vto = tv_vipmine.getViewTreeObserver();
        
        vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
        {
            public boolean onPreDraw()
            {
				if (hasMeasured  == false)
                {
					tvBasicWidth = tv_vipmine.getMeasuredWidth();
                    //获取到宽度后，可用于计算 

                    hasMeasured = true;
                    bmpW = BitmapFactory.decodeResource(getResources(), R.drawable.arrows)
            				.getWidth();// 获取图片宽度

            		DisplayMetrics dm = new DisplayMetrics();
            		getWindowManager().getDefaultDisplay().getMetrics(dm);
            		int screenW = dm.widthPixels;// 获取分辨率宽度
            		Log.e(TAG, "int screenW:" + screenW);

            		offset = tvBasicWidth / 2 - px2dip(VipActivity.this, bmpW+0.0f)/2;

            		Log.e(TAG, "int screenW:" + bmpW);
            		Log.e(TAG, "int screenW:" + offset);
            		Log.e(TAG, "int tvBasicWidth:" + tvBasicWidth);
            		// 计算偏移量，因为有3个Tab则屏幕宽度/3,一个偏移量为(screenW
            		// / 3 - bmpW) / 2
            		Matrix matrix = new Matrix();
            		matrix.postTranslate(offset, 0);
            		tabArrows.setImageMatrix(matrix);

            		one = offset + tvBasicWidth/2 + px2dip(VipActivity.this, bmpW+0.0f)/2;// 页卡1 -> 页卡2 偏移量
            		two = one + tvBasicWidth + px2dip(VipActivity.this, bmpW+0.0f)/2; // 页卡1 -> 页卡3 偏移量

                }
                return true;
            }
        });
        
	}
	
	VipMineAdapter mVipMineAdapter=null;
	VipPrivilegeAdapter mVipPrivilegeAdapter=null;
	VipProfileAdapter  mVipProfileAdapter=null;
	/****
	 * 初始化ListView内容
	 */
	private void initListViewCotent() {
		mVipMineAdapter = new VipMineAdapter(this);
		mVipPrivilegeAdapter = new VipPrivilegeAdapter(this);
		mVipProfileAdapter = new VipProfileAdapter(this);
	}
	
	/***
	 * 初始化ViewPager
	 */
	private void initPager() {
		mVipPageAdapter = new TabContentPageAdapter(mViews);

		// 获取ViewPager并添加设配器，默认显示第1项，索引为0
		mViewPager = (ViewPager) findViewById(R.id.center_view_pager);
		mViewPager.setAdapter(mVipPageAdapter);
		
		
		setImageBackground(0);
		mViewPager.setCurrentItem(TAB_VIP_MINE);
		currentTabPager = TAB_VIP_MINE; // 当前默认选择Tab索引

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
				Animation animation = null;
				switch (pageIndex) {
				case TAB_VIP_MINE:
					mViewPager.setCurrentItem(TAB_VIP_MINE);
					if (currentTabPager == 1) {
						animation = new TranslateAnimation(one, 0, 0, 0);
					} else if (currentTabPager == 2) {
						animation = new TranslateAnimation(two, 0, 0, 0);
					}
					break;
				case TAB_VIP_PRIVILEGE:
					mViewPager.setCurrentItem(TAB_VIP_PRIVILEGE);
					if (currentTabPager == 0) {
						animation = new TranslateAnimation(offset, one, 0, 0);
					} else if (currentTabPager == 2) {
						animation = new TranslateAnimation(two, one, 0, 0);
					}
					break;
				case TAB_VIP_PROFILE:
					mViewPager.setCurrentItem(TAB_VIP_PROFILE);
					if (currentTabPager == 0) {
						animation = new TranslateAnimation(0, two - 10, 0, 0);
					} else if (currentTabPager == 1) {
						animation = new TranslateAnimation(one, two - 5, 0, 0);
					}
					break;
				}

				currentTabPager = pageIndex; // 当前选择Tab索引
				setImageBackground(pageIndex); 
				// -------------------------------------------------------
				animation.setFillAfter(true);// True:图片停在动画结束位置
				animation.setDuration(700);
				tabArrows.startAnimation(animation);
			}
		});
	}
	
	/***
	 * 根据Tab索引切换图片背景
	 * 
	 * @param position
	 */
	private void setImageBackground(int position) {
		// TODO Auto-generated method stub
		switch (position) {
		case 0:
			tv_vipmine.setBackgroundResource(R.drawable.center_tab_left_fg);

			tv_vip_privilege.setBackgroundResource(R.drawable.center_tab_middle_bg);

			tv_vip_profile.setBackgroundResource(R.drawable.center_tab_right_bg);

			break;
		case 1:
			tv_vipmine.setBackgroundResource(R.drawable.center_tab_left_bg);

			tv_vip_privilege.setBackgroundResource(R.drawable.center_tab_middle_fg);

			tv_vip_profile.setBackgroundResource(R.drawable.center_tab_right_bg);

			break;
		case 2:
			tv_vipmine.setBackgroundResource(R.drawable.center_tab_left_bg);

			tv_vip_privilege.setBackgroundResource(R.drawable.center_tab_middle_bg);

			tv_vip_profile.setBackgroundResource(R.drawable.center_tab_right_fg); 
			
			break;
		default:
			break;
		}
	}
	
	
	
	@Override
	public void onClick(View v) {
		 if(v.getId()==R.id.btnGivePresent){        //赠送
			 Toast.makeText(getApplication(), this.getResources().getString(R.string.ddz_function_not_available), Toast.LENGTH_LONG).show();
		 }else if(v.getId()==R.id.btnRenewalVip){   //VIP续费
			 startMarket();
		 }else if(v.getId()==R.id.tvBack){
			 finish();
		 }else if (v.getId()==R.id.tv_vipmine) {
			setImageBackground(TAB_VIP_MINE);
			mViewPager.setCurrentItem(TAB_VIP_MINE);
		   } else if (v.getId()==R.id.tv_vip_privilege) {
			 setImageBackground(TAB_VIP_PRIVILEGE);
			 mViewPager.setCurrentItem(TAB_VIP_PRIVILEGE);
		} else if (v.getId()==R.id.tv_vip_profile) {
			setImageBackground(TAB_VIP_PROFILE);
			mViewPager.setCurrentItem(TAB_VIP_PROFILE);
	   } 
	}

	/**
	 * @Title: startMarket
	 * @Description: 跳转到商城界面
	 * @version: 2013-6-8 下午05:03:23
	 */
	protected void startMarket(){
		Intent i=new Intent(this,MarketActivity.class);
		startActivity(i);
		finish();
	}
	
	/**
	 * @Title: sendTOReceive
	 * @Description: 发送领取协议
	 * @version: 2013-6-8 下午05:16:30
	 */
	protected void sendTOReceive(){
		int userID=HallDataManager.getInstance().getUserMe().userID;
		TDataOutputStream tdos = new TDataOutputStream();
		tdos.writeInt(AppConfig.gameId, false);//游戏ID
		tdos.writeInt(userID, false);//用户ID
		NetSocketPak pointBalance = new NetSocketPak(MDM_USER, MSUB_GR_USER_VIP_ADWARD_REQ, tdos);
		// 发送协议
		NetSocketManager.getInstance().sendData(pointBalance);
		// 清理协议对象
		pointBalance.free(); 
	}
	
	protected void sendVip(){
		int userID=HallDataManager.getInstance().getUserMe().userID;
		int ver=0;
		ver=Integer.getInteger(VipXmlParser.Ver);
		TDataOutputStream tdos = new TDataOutputStream();
		tdos.writeInt(ver, false);//版本号
		tdos.writeInt(AppConfig.gameId, false);//游戏ID
		tdos.writeInt(userID, false);//用户ID
		NetSocketPak pointBalance = new NetSocketPak(MDM_USER, MSUB_GR_USER_VIP_REQ, tdos);
		// 发送协议
		NetSocketManager.getInstance().sendData(pointBalance);
		// 清理协议对象
		pointBalance.free(); 
	}
	
	
	/**
	 * @Title: receiveToGet
	 * @Description: 领取响应
	 * @version: 2013-6-8 下午05:17:14
	 */
	protected void receiveToGet(){
		// 定义接受数据的协议
		short[][] parseProtocol = { { MDM_USER, MSUB_GR_USER_VIP_ADWARD_RESPONSE } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			public boolean doReceive(NetSocketPak netSocketPak) {
				TDataInputStream tdis=netSocketPak.getDataInputStream();
				tdis.setFront(false);
				byte result=tdis.readByte();
				if(result==0){         //0成功
					
				}else if(result==1){  //1 非VIP不能领取
					
				}else if(result==2){  //2领取失败
					
				} 
				return true;
			}
		};
		nPListener.setOnlyRun(false);
		NetSocketManager.getInstance().addPrivateListener(nPListener);
	}
	
	/**
	 * @Title: receiveVipData
	 * @Description: 获取Vip信息
	 * @version: 2013-6-8 下午05:47:50
	 */
	protected void receiveVipData(){
		// 定义接受数据的协议
		short[][] parseProtocol = { { MDM_USER, MSUB_GR_USER_VIP_RESPONSE } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			public boolean doReceive(NetSocketPak netSocketPak) {
				TDataInputStream tdis=netSocketPak.getDataInputStream();
				tdis.setFront(false);
				short result=tdis.readShort();
				if(result!=0){         //为0则无VIP信息，表示非VIP
					VipData vipData=new VipData(tdis);
					HallDataManager.getInstance().setVipData(vipData);
					mhandler.obtainMessage(USER_VIP_MESSAGE_HANDLER).sendToTarget();
				}
				return true;
			}
		};
		nPListener.setOnlyRun(false);
		NetSocketManager.getInstance().addPrivateListener(nPListener);
	}
	
	private void setUIMsg(VipData vipData){
		tvdegree.setText(String.valueOf(vipData.dwGroup));
		tvEndDate.setText(vipData.cOutTimer);
		customProgressBar.setProgress(vipData.dwGroup);
		//customProgressBar.setMax(max);
	}
	
	
	Handler mhandler=new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case USER_VIP_MESSAGE_HANDLER:      //个人VIP信息返回成功
				setUIMsg(HallDataManager.getInstance().getVipData());
				break;
			}
		 }
	};
	
	
}
