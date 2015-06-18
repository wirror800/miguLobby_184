package com.mykj.andr.logingift;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mykj.andr.model.ActionInfo;
import com.mykj.andr.model.HallDataManager;
import com.mykj.andr.model.SystemPopMsg;
import com.mykj.andr.model.UserInfo;
import com.mykj.andr.provider.ActionInfoProvider;
import com.mykj.andr.ui.GetTicketActivity;
import com.mykj.andr.ui.UserCenterActivity;
import com.mykj.game.FiexedViewHelper;
import com.MyGame.Migu.R;
import com.mykj.game.ddz.api.AnalyticsUtils;
import com.mykj.game.ddz.api.UC;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.CenterUrlHelper;
import com.mykj.game.utils.ImageAsyncTaskDownload;
import com.mykj.game.utils.Toast;
import com.mykj.game.utils.Util;
import com.mykj.game.utils.UtilHelper;

/**
 * 
 * @ClassName: TheGiftDialog
 * @Description: 礼品弹出框
 * @author Administrator
 * @date 2013-5-2 上午11:56:52
 * 
 */
@SuppressLint("HandlerLeak")
public class TheGiftDialog extends AlertDialog implements View.OnClickListener {

	// 推荐活动标识，用于从活动列表中获取推荐的活动信息显示在弹出框中
	private final static String DEFAULT_ACTION = "1";

	private static Map<String, String> giftBeanMap = new HashMap<String, String>();

	private Activity mAct;

	private Button tabReward; // 免费奖励按钮
	private Button tabNotice; // 公告按钮
	private RelativeLayout layoutReward; // 奖励显示
	private RelativeLayout layoutNotice; // 公告显示
	private RelativeLayout layoutAction; // 活动显示

	private TextView noticeLabel; // 公告标题
	private TextView noticeContent; // 公告内容
	private Button noticeBtn; // 公告内容
	private ImageView jumpImg;
	private TranslateAnimation animation;
	// 关闭窗口
	private ImageView ivCancel;

	// 登陆送
	private RelativeLayout songdouArea;

	// 抽奖
	private RelativeLayout luckyDrawArea;

	// 做任务
	private RelativeLayout taskArea;

	// 兑换卷
	private RelativeLayout ticketArea;

	// 参加活动
	private Button joinActivityBtn;

	// 抽奖按钮
	private Button btnChoujiang;

	// 去做任务
	private Button btnDoTask;

	// 活动logo
	private ImageView ivAction;

	// 活动名称
	private TextView tvActionName;

	// 活动描述
	private TextView tvActionInfo;

	// 活动时间
	private TextView tvActionTime;

	// 连续登陆天数
	private TextView dayTv;

	// 登陆送信息
	private TextView giftDescTv;

	// 明天登陆奖励信息
	private TextView nextGiftDescTv;

	// 去兑换
	private Button btnGetTicket;

	private Resources mResource;

	private UserInfo userInfo;
	private final int defaultTitleColor = 0xffa0ff90;
	public TheGiftDialog(Activity context) {
		super(context);
		mAct = context;
		mResource = mAct.getResources();
	}

	DialogInterface.OnClickListener callBack;

	public DialogInterface.OnClickListener getCallBack() {
		return callBack;
	}

	public void setCallBack(DialogInterface.OnClickListener callBack) {
		this.callBack = callBack;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initActionData();
		boolean isShowActionInfo = defaultAction != null;
		setContentView(R.layout.the_gift_dialog_new);

		/*
		 * if(isShowActionInfo){ //setContentView(R.layout.the_gift_dialog); }
		 * else{ //setContentView(R.layout.the_gift_no_action_dialog); }
		 */
		init();
		if (isShowActionInfo) { 
			layoutAction.setVisibility(View.VISIBLE);
			initActionView();
		} else {
			layoutAction.setVisibility(View.INVISIBLE);
		}
		initData();
		initGiftInfo();
		initLuckyDrawInfo();
		this.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				// TODO Auto-generated method stub
				showExitNotice = false;
				SystemPopMsg.PopMsgItem item = ActionInfoProvider.getInstance()
						.getNoticeItem();
				if(item != null && item.popType == SystemPopMsg.POP_EXIT){
					ActionInfoProvider.getInstance().setNoticeItem(null);
					initNoticeData();
				}
			}
		});
	}

	public void showReward() {
		tabReward.setBackgroundResource(R.drawable.logpop_reward_light);
		tabNotice.setBackgroundResource(R.drawable.logpop_notice_dark);
		layoutReward.setVisibility(View.VISIBLE);
		layoutNotice.setVisibility(View.INVISIBLE);
		jumpImg.clearAnimation();
	}

	public boolean showNotice() {
		try{  //短信重回空指针异常
			tabReward.setBackgroundResource(R.drawable.logpop_reward_normal_dark);
			tabNotice.setBackgroundResource(R.drawable.logpop_notice_light);

			layoutReward.setVisibility(View.INVISIBLE);
			layoutNotice.setVisibility(View.VISIBLE);
			jumpImg.startAnimation(animation);
		}catch(Exception e){
			return false;
		}
		if(!showExitNotice){
			initNoticeData();
			return true;
		}else{
			return false;
		}

	}

	private void init() {

		// 初始化公共部分
		tabReward = (Button) findViewById(R.id.logpop_tab_reward);
		tabNotice = (Button) findViewById(R.id.logpop_tab_notice);
		ivCancel = (ImageView) findViewById(R.id.ivCancel);
		layoutReward = (RelativeLayout) findViewById(R.id.logpop_reward_layout);
		layoutNotice = (RelativeLayout) findViewById(R.id.logpop_notice_layout);
		tabReward.setOnClickListener(this);
		tabNotice.setOnClickListener(this);
		ivCancel.setOnClickListener(this);

		// 初始化奖励界面
		songdouArea = (RelativeLayout) findViewById(R.id.songdou_area);
		joinActivityBtn = (Button) findViewById(R.id.btn_activity);
		btnChoujiang = (Button) findViewById(R.id.btn_choujiang);
		btnDoTask = (Button) findViewById(R.id.btn_dotask);
		btnGetTicket = (Button) findViewById(R.id.btn_ticket);
		luckyDrawArea = (RelativeLayout) findViewById(R.id.choujiang_area);
		taskArea = (RelativeLayout) findViewById(R.id.task_area);
		ticketArea = (RelativeLayout) findViewById(R.id.ticket_area);

		songdouArea.setOnClickListener(this);
		joinActivityBtn.setOnClickListener(this);
		btnChoujiang.setOnClickListener(this);
		btnDoTask.setOnClickListener(this);
		btnGetTicket.setOnClickListener(this);
		luckyDrawArea.setOnClickListener(this);
		taskArea.setOnClickListener(this);
		ticketArea.setOnClickListener(this);

		/*cardZoneFragment初始化时已做了这个，所以这里不需要再次请求，否则可能状态不对(对是否第一次的判断错误)*/
		//		hallAssociated = HallAssociatedWidget.getInstance();
		userInfo = HallDataManager.getInstance().getUserMe();
		//		hallAssociated.requestSystemMessage(userInfo.userID);

		tvActionName = (TextView) findViewById(R.id.activity_name);
		tvActionInfo = (TextView) findViewById(R.id.activity_info);
		ivAction = (ImageView) findViewById(R.id.activity_iv);
		tvActionTime = (TextView) findViewById(R.id.activity_time);

		dayTv = (TextView) findViewById(R.id.songdou_lable1);
		giftDescTv = (TextView) findViewById(R.id.songdou_lable2);

		nextGiftDescTv = (TextView) findViewById(R.id.songdou_lable3);

		choujiang_lable1 = (TextView) findViewById(R.id.choujiang_lable1);
		choujiang_lable2 = (TextView) findViewById(R.id.choujiang_lable2);
		layoutAction = (RelativeLayout) findViewById(R.id.logpop_active_area);

		// 初始化公告界面
		noticeLabel = (TextView) findViewById(R.id.tvLabel);
		noticeContent = (TextView) findViewById(R.id.tvContent);
		noticeBtn = (Button) findViewById(R.id.confirmBtn);
		noticeBtn.setOnClickListener(this);

		jumpImg = (ImageView) findViewById(R.id.iv_dlg_pump);
		animation = new TranslateAnimation(0, 0, 0, -20);
		animation.setDuration(300);
		animation.setRepeatCount(Animation.INFINITE);
		animation.setRepeatMode(Animation.REVERSE);

	}

	private ActionInfo defaultAction;

	private String giftPack; // 登陆送信息

	private String luckyDrawPack; // 抽奖送信息

	private String day; // 连续登陆天数

	private String giftdesc; // 奖励信息

	private String nextDayGiftDesc; // 明天登陆奖励信息

	private TextView choujiang_lable1;

	private TextView choujiang_lable2;


	/**
	 * 初始化活动视图
	 */
	private void initActionView() {

		if (defaultAction != null) {
			//下载图片
			String photoFileName=defaultAction.bitmapUrl;
			String url=defaultAction.baseUrl+"/"+photoFileName;
			new ImageAsyncTaskDownload(url,photoFileName,ivAction).execute();
			//设置文字
			tvActionName.setText(defaultAction.name);
			tvActionInfo.setText(defaultAction.details);
			tvActionTime.setText(defaultAction.date_begin + " - "
					+ defaultAction.date_end);
		}

	}


	/**
	 * 初始化活动数据
	 */
	private void initActionData() {
		// 活动列表
		List<ActionInfo> actionInfos = ActionInfoProvider.getInstance()
				.getList();

		// 获取推荐活动
		for (ActionInfo actionInfo : actionInfos) {
			if (DEFAULT_ACTION.equals(actionInfo.type)) {
				defaultAction = actionInfo;
				break;
			}
		}
	}
	private boolean showExitNotice = false;
	private void initNoticeData() {
		// 初始化公告消息数据
		SystemPopMsg.PopMsgItem item = ActionInfoProvider.getInstance()
				.getNoticeItem();
		noticeLabel.setVisibility(View.GONE);
		noticeContent.setText(mResource.getString(R.string.login_gift_no_notice));
		noticeBtn.setVisibility(View.GONE);
		if (item != null) {
			if(item.title != null && item.title.trim().length() > 0){
				noticeLabel.setVisibility(View.VISIBLE);
				noticeLabel.setText(item.title);
				if(item.titleColor == 0){
					noticeLabel.setTextColor(defaultTitleColor);
				}else{
					noticeLabel.setTextColor(item.titleColor);
				}
			}
			if(item.msg != null){
				noticeContent.setText(item.msg);
			}

			if (item.buttonType == 1) {
				noticeBtn.setVisibility(View.VISIBLE);
				noticeBtn.setText(mResource.getString(R.string.login_gift_goto_look));
			} else if (item.buttonType == 2) {
				noticeBtn.setVisibility(View.VISIBLE);
				noticeBtn.setText(mResource.getString(R.string.login_gift_sign_up));
			}
			if(item.popType == SystemPopMsg.POP_EXIT){
				showExitNotice = true;
			}
		}
	}

	private void initData() {
		String uid = String.valueOf(userInfo.userID);
		giftPack = Util.getStringSharedPreferences(mAct, uid, null);

		luckyDrawPack = Util.getStringSharedPreferences(mAct, uid
				+ "_luckyDraw", null);

		parseStatusXml(giftPack, "gift");
	}

	/**
	 * 初始化登陆送信息
	 */
	private void initGiftInfo() {
		if (!"".equals(giftPack) && null != giftPack) {
			day = UtilHelper.parseAttributeByName("day", giftPack);// 用户连续登陆天数
			giftdesc = UtilHelper.parseAttributeByName("giftdesc", giftPack);// 获得礼品
			int giftDay = 0;
			if(Util.isNumeric(day)){ 
				giftDay = Integer.parseInt(day);

				giftDay = giftDay < 5 ? (giftDay + 1) : 5;
				nextDayGiftDesc = giftBeanMap.get(String.valueOf(giftDay));

				dayTv.setText(mResource.getString(R.string.login_gift_continuity_4) + day + mResource.getString(R.string.login_gift_continuity_day));
				giftDescTv.setText(mResource.getString(R.string.login_gift_reward) + giftdesc);
				nextGiftDescTv.setText(mResource.getString(R.string.login_gift_tomorrow_reward) + nextDayGiftDesc + AppConfig.UNIT);
			}
		}	
	}

	private void initLuckyDrawInfo() {
		luckyDrawPack = Util.getStringSharedPreferences(mAct, userInfo.userID
				+ "_luckyDraw", "");
		String[] luckyDrawData = luckyDrawPack.split(",");

		if (luckyDrawData.length > 1) {
			choujiang_lable1.setText(mResource.getString(R.string.login_gift_continuity_4) + luckyDrawData[0] + mResource.getString(R.string.login_gift_continuity_day));
			choujiang_lable2.setText(mResource.getString(R.string.login_gift_reward_1) + luckyDrawData[1] + mResource.getString(R.string.login_gift_continuity_time));
		}
	}

	public static boolean parseStatusXml(String strXml, String tagName) {
		if(Util.isEmptyStr(strXml)){
			return false;
		}
		// boolean isParseSuccess = false;
		boolean isParseSuccess = false;
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
					String tag = p.getName();
					if (tag.equals(tagName)) {
						giftBeanMap.put(p.getAttributeValue(null, "day"),
								p.getAttributeValue(null, "count"));
					}
					isParseSuccess = true;
					break;
				case XmlPullParser.END_TAG:
					break;
				case XmlPullParser.END_DOCUMENT:
					break;
				default:
					break;
				}
				// 用next方法处理下一个事件，否则会造成死循环。
				eventType = p.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
			isParseSuccess = false;
		}
		return isParseSuccess;
	}


	private LoginGiftDialog dialog;


	@Override
	public void onClick(View view) {
		int id = view.getId();
		if (id == R.id.ivCancel) {
			// 退出
			if(showExitNotice){
				FiexedViewHelper.getInstance().exitGame();
			}
			dismiss();
		} else if (id == R.id.songdou_area) {
			dismiss();
			/*********************
			 * 有登录送数据，才弹出
			 */
			if (null != giftPack && !"".equals(giftPack)) {
				showGiftDialog(mAct, new android.view.View.OnClickListener() {
					@Override
					public void onClick(View v) {
						UtilHelper.showBuyDialog(mAct, LoginGiftDialog.propid,
								false, true);
					}
				}, giftPack); // 这里没有数据的话就不会显示《描述信息》

			} else {
				Toast.makeText(mAct, mResource.getString(R.string.login_gift_has_obtain), Toast.LENGTH_LONG).show();
			}

		} else if (id == R.id.btn_activity) {
			dismiss();
			if (null != defaultAction && null != defaultAction.url
					&& defaultAction.url.length() > 0) {
				String url = defaultAction.url+"?";
				int userId=FiexedViewHelper.getInstance().getUserId();
				String finalUrl=CenterUrlHelper.getUrl(url,userId);
				int uo=defaultAction.uo;
				UtilHelper.onWeb(mAct, finalUrl, uo);
				AnalyticsUtils.onClickEvent(mAct, UC.EC_202);
			}
		} else if (id == R.id.choujiang_area || id == R.id.btn_choujiang) {

			dismiss();
			mAct.startActivity(new Intent(mAct, LotteryDrowActivity.class));
			AnalyticsUtils.onClickEvent(mAct, UC.EC_214);
		} else if (id == R.id.task_area || id == R.id.btn_dotask) {
			FiexedViewHelper.getInstance().quickGame();
			dismiss();
			AnalyticsUtils.onClickEvent(mAct, UC.EC_215);
		} else if (id == R.id.logpop_tab_reward) {
			showReward();
		} else if (id == R.id.logpop_tab_notice) {
			showNotice();
		} else if (id == R.id.confirmBtn) {
			dismiss();
			SystemPopMsg.PopMsgItem item = ActionInfoProvider.getInstance().getNoticeItem();
			if(item != null){
				SystemPopMsg.toWhere(mAct, item);
			}
		} else if (id == R.id.ticket_area || id == R.id.btn_ticket){
			
			String url=AppConfig.NEW_GETTICKET_URL;
			
			String userToken= FiexedViewHelper.getInstance().getUserToken();
			int userId=FiexedViewHelper.getInstance().getUserId();
			
			url += "&at=" + userToken + "&";
			String finalUrl = CenterUrlHelper.getUrl(url, userId);
			UtilHelper.onWeb(mAct, finalUrl);
			
			/*Intent getTicket = new Intent(mAct, GetTicketActivity.class);
			mAct.startActivityForResult(getTicket, 0);*/
			AnalyticsUtils.onClickEvent(mAct, UC.EC_216);
		} 

	}

	/***
	 * @Title: showGiftDialog
	 * @Description: 登录送对话框
	 * @param mAct
	 * @param listener
	 * @param giftStr
	 * @version: 2013-1-11 下午01:46:01
	 */
	private void showGiftDialog(Activity mAct,
			final android.view.View.OnClickListener listener, String giftStr) {
		// if (dialog == null) {
		dialog = new LoginGiftDialog(mAct, giftStr);

		dialog.setCallBack(new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 确定回调
				if (listener != null)
					listener.onClick(null);
				dialog.dismiss();// 退出
			}
		});
		// }
		dialog.setGitStr(giftStr);
		dialog.show();
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub
		if(FiexedViewHelper.getInstance().getCurFragment() == FiexedViewHelper.CARDZONE_VIEW
				&& !FiexedViewHelper.getInstance().isSkipFragment()){
			super.show();
		}
	}

}
