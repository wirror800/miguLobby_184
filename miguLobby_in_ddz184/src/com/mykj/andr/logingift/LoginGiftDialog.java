package com.mykj.andr.logingift;

 
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mykj.andr.model.HallDataManager;
import com.mykj.andr.model.UserInfo;
import com.MyGame.Migu.R;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.Log;
import com.mykj.game.utils.UtilHelper;

/**
 * 
 * @ClassName: PromptEditDialog
 * @Description: TODO(这里描述这个类的作用)
 * @author Administrator
 * @date 2012-7-30 上午11:34:52
 *
 */
public class LoginGiftDialog extends AlertDialog implements
		android.view.View.OnClickListener{
	
	/** 登录送数据 */
	public String giftStr;	
	/** 礼包标题 */
	public String title;
	/** 用户连续登陆天数 */
	public String day;
	/**获得礼品*/
	public String giftdesc;
	/**会员登录送的倍数*/
	public static String mbmultiple;
	/** 道具信息是否显示 */
	public String propshow;
	/** 道具编号 */
	public static int propid;
	/** 礼包描述信息 */
	public String desc;
	
	protected TextView tvUserGiftInfo;
	protected TextView tvGiftLabel;
	protected TextView tvMemberLabel;
	protected TextView tvGift1st;
	protected TextView tvGift2nd;
	protected TextView tvGift3rd;
	protected TextView tvGift4th;
	protected TextView tvGift5th;

	protected Button	btnConfir;
	protected ImageView	ivCancel;
	
	
	//
	protected ImageView	gift1st;
	protected ImageView	gift2nd;
	protected ImageView	gift3rd;
	protected ImageView	gift4th;
	protected ImageView	gift5th;	


	Context		ctx;
	
	
	//强引用GC未能回收
	//Map<Integer, ImageView> giftMap = new HashMap<Integer, ImageView>();
	
	static Map<String, String> giftBeanMap = new HashMap<String, String>();
     
	
	public LoginGiftDialog(Context context, String giftStr) {
		super(context);
		this.ctx = context;
		this.giftStr = giftStr;
	}
	
	
	
	public void setGitStr(String mgiftStr){	
		this.giftStr = mgiftStr;
	}
	
	
	DialogInterface.OnClickListener callBack;

	public DialogInterface.OnClickListener getCallBack() {
		return callBack;
	}

	public void setCallBack(DialogInterface.OnClickListener callBack) {
		this.callBack = callBack;
	}
	
	static {
		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.login_gift_dialog);

		init();
	}
	
	private void init() {
		btnConfir=(Button)findViewById(R.id.btnConfir);   //成为会员，会员:会员续费
   
		UserInfo user=HallDataManager.getInstance().getUserMe();
		if(user!=null){
			if(user.masterOrder>10){   //会员等级
				//btnConfir.setText("会员续费");
				btnConfir.setBackgroundResource(R.drawable.xufei_huiyuan_selector);
			}
		}
		
		btnConfir.setOnClickListener(this);
		
		ivCancel=(ImageView)findViewById(R.id.ivCancel);
		ivCancel.setOnClickListener(this);
		
		//5张背景图片
		gift1st = (ImageView)findViewById(R.id.gift_1st_img);
		gift2nd = (ImageView)findViewById(R.id.gift_2nd_img);
		gift3rd = (ImageView)findViewById(R.id.gift_3rd_img);
		gift4th = (ImageView)findViewById(R.id.gift_4th_img);
		gift5th = (ImageView)findViewById(R.id.gift_5th_img);
		
		tvGift1st = (TextView)findViewById(R.id.gift_1st_label);
		tvGift2nd = (TextView)findViewById(R.id.gift_2nd_label);
		tvGift3rd = (TextView)findViewById(R.id.gift_3rd_label);
		tvGift4th = (TextView)findViewById(R.id.gift_4th_label);
		tvGift5th = (TextView)findViewById(R.id.gift_5th_label);
		
		ImageView time_1= (ImageView)findViewById(R.id.time_1);
		ImageView time_2= (ImageView)findViewById(R.id.time_2);
		ImageView time_3= (ImageView)findViewById(R.id.time_3);
		ImageView time_4= (ImageView)findViewById(R.id.time_4);
		ImageView time_5= (ImageView)findViewById(R.id.time_5);
		
		
		//强引用GC未能回收
		//setGiftMap();
		
		tvUserGiftInfo = (TextView)findViewById(R.id.buyLabel);
		tvGiftLabel = (TextView)findViewById(R.id.balanceLabel);
		tvMemberLabel = (TextView)findViewById(R.id.member_mbmultiple);
		if (!giftStr.equals("")){
			parseGiftPack(giftStr);
			if (parseStatusXml(giftStr, "gift")){
				
				tvGift1st.setText(parseSourceData(giftBeanMap.get("1"),AppConfig.UNIT));
				tvGift2nd.setText(parseSourceData(giftBeanMap.get("2"),AppConfig.UNIT));
				tvGift3rd.setText(parseSourceData(giftBeanMap.get("3"),AppConfig.UNIT));
				tvGift4th.setText(parseSourceData(giftBeanMap.get("4"),AppConfig.UNIT));
				tvGift5th.setText(parseSourceData(giftBeanMap.get("5"),AppConfig.UNIT));
				
				//tvGift1st.setText(Html.fromHtml("<font color=#fceca3>"+giftBeanMap.get("1")+"</font>")+ AppConfig.UNIT);
				//tvGift2nd.setText(Html.fromHtml("<font color=#fceca3>"+giftBeanMap.get("2")+"</font>")+ AppConfig.UNIT);
				//tvGift3rd.setText(Html.fromHtml("<font color=#fceca3>"+giftBeanMap.get("3")+"</font>")+ AppConfig.UNIT);
				//tvGift4th.setText(Html.fromHtml("<font color=#fceca3>"+giftBeanMap.get("4")+"</font>")+ AppConfig.UNIT);
				//tvGift5th.setText(Html.fromHtml("<font color=#fceca3>"+giftBeanMap.get("5")+"</font>")+ AppConfig.UNIT);
			}
			
			Resources resources = ctx.getResources();
			tvUserGiftInfo.setText(resources.getString(R.string.login_gift_continuity_1)+ day + resources.getString(R.string.login_gift_continuity_2)+ giftdesc + resources.getString(R.string.login_gift_continuity_3));
			tvGiftLabel.setText(desc);
			tvMemberLabel.setText(mbmultiple);
			
			//这里需要改为图片背景
			//设置渐变
			onAlpha(Integer.parseInt(day), getGiftIv(Integer.parseInt(day)));
			
			//下面设置当前登陆多少天标记
			int daytemp=Integer.parseInt(day);
			if(daytemp==1){
				time_1.setVisibility(View.VISIBLE);
				gift1st.setBackgroundResource(R.drawable.login_gift_item_selected);
			}else if(daytemp==2){
				time_2.setVisibility(View.VISIBLE);
				gift2nd.setBackgroundResource(R.drawable.login_gift_item_selected);
			}else if(daytemp==3){
				time_3.setVisibility(View.VISIBLE);
				gift3rd.setBackgroundResource(R.drawable.login_gift_item_selected);
			}else if(daytemp==4){
				time_4.setVisibility(View.VISIBLE);
				gift4th.setBackgroundResource(R.drawable.login_gift_item_selected);
			}else if(daytemp==5 ||daytemp>5 ){
				time_5.setVisibility(View.VISIBLE);
				gift5th.setBackgroundResource(R.drawable.login_gift_item_selected);
			}
		}
		
	}
	
	protected int getLength(String beanMap){
		if(beanMap!=null){
			return beanMap.length();
		}
		return 0;
	}
	
	protected SpannableString parseSourceData(String source,String ends){
		int length=getLength(source);
		SpannableString sp=new SpannableString(source+ends);
		int end=length;
		try{
			if(end>=1){
		    	sp.setSpan(new ForegroundColorSpan(Color.parseColor("#fceca3")),0,end,Spannable.SPAN_EXCLUSIVE_INCLUSIVE); 
		    }else{
		    	sp.setSpan(new ForegroundColorSpan(Color.parseColor("#fceca3")),0,0,Spannable.SPAN_EXCLUSIVE_INCLUSIVE); 
		    }
		}catch(Exception ex){
		}
		return sp;
	}
	
	
	
	
	
	public void parseGiftPack(String giftStr) {
		title = UtilHelper.parseAttributeByName("title", giftStr);// 礼包标题
		day = UtilHelper.parseAttributeByName("day", giftStr);// 用户连续登陆天数
		giftdesc= UtilHelper.parseAttributeByName("giftdesc", giftStr);//获得礼品
		mbmultiple= UtilHelper.parseAttributeByName("mbmultiple", giftStr);//会员倍数
		propshow = UtilHelper.parseAttributeByName("propshow", giftStr);// 道具信息是否显示
		propid = Integer.parseInt(UtilHelper.parseAttributeByName("propid", giftStr));// 道具编号
		desc = UtilHelper.parseAttributeByName(" desc", giftStr);// 礼包描述信息
	}

	//强引用GC未能回收
	/**public void setGiftMap() {
		giftMap.put(1, gift1st); 
		giftMap.put(2, gift2nd); 
		giftMap.put(3, gift3rd); 
		giftMap.put(4, gift4th); 
		giftMap.put(5, gift5th); 
	}**/
	
	public ImageView getGiftIv(int day) { 
		if(day==1){
			return gift1st;
		}else if(day==2){
			return gift2nd; 
		}else if(day==3){
			return gift3rd;
		}else if(day==4){
			return gift4th;
		}else{
			return gift5th;
		} 
	}
	
	@Override
	public void onClick(View v){
		int id = v.getId();
		if (id == R.id.btnConfir) {
			if(callBack!=null){
				callBack.onClick(LoginGiftDialog.this, v.getId());
			} 
		}else if(id == R.id.ivCancel){
			dismiss();//退出
		}
	}
	
	public void onAlpha(int day, ImageView iv) {
		
		//iv.setImageResource(R.drawable.login_rewards_light);
		
		/***
		final AlphaAnimation alphaAnimation = new AlphaAnimation(0.5f, 1.0f);  
		alphaAnimation.setDuration(250);  
		alphaAnimation.setRepeatCount(Animation.INFINITE);  
		alphaAnimation.setRepeatMode(Animation.RESTART);  
		iv.setAnimation(alphaAnimation);  
		alphaAnimation.start(); 
		iv.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				alphaAnimation.setRepeatCount(1);
				return false;
			}
		});
		***/
		
		
	}

	 public static boolean parseStatusXml(String strXml, String tagName) {
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
					if (tag.equals(tagName)){												
						giftBeanMap.put(p.getAttributeValue(null, "day"), p.getAttributeValue(null, "count"));
						Log.e("====day", p.getAttributeValue(null, "day"));
						Log.e("====count", p.getAttributeValue(null, "count"));
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
	
}
