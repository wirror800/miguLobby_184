package com.mykj.andr.ui.widget;

import android.R.bool;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mykj.andr.model.GoodsItem;
import com.mykj.andr.pay.PayManager;
import com.MyGame.Midlet.R;
import com.mykj.game.ddz.api.AnalyticsUtils;
import com.mykj.game.ddz.api.UC;

/*****
 * 
 * @ClassName: PayListDialog
 * @Description: 多种支付购买对话框
 * @author zhd
 * @date 2013-2-22 下午02:51:34
 * 
 */
public class PayListDialog extends AlertDialog implements
		android.view.View.OnClickListener {

	/** 多种支付显示列表 ***/
	public static int[] paylist = null;

	/**支付类型列表***/
	public static int[] paysignlist = null;
	
	public static GoodsItem item;
	
	private Context mComtext;

	private static PayListDialog instance = null;

	private static String str_pay_name; // 道具名称
	private static CharSequence str_pay_cont; // 道具内容
	private static CharSequence str_pay_money; // 道具价格 n
	private static String str_pay_depict;// 道具介绍

	private static String str_pay_failure; // 支付失败显示

	public PayListDialog(Context context) {
		super(context);
		mComtext=context;
	}
	
	public static PayListDialog getInstance(Context context) {
		if (instance == null) {
			instance = new PayListDialog(context);
		}
		return instance;
	}
	
	public static void setPaylistDialog(String name, CharSequence price,
			CharSequence giftdesc, String intro, String _paylist,String pay_failure,final GoodsItem _item){
		str_pay_name = name;
		str_pay_cont = giftdesc;
		str_pay_money = price;
		str_pay_depict = intro;
		str_pay_failure=pay_failure;
		item=_item;
		
		paylist = getpaylist(_paylist);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.paylist_dialog);

		init();
	}

	private static int[] getpaylist(String plist) {
		String[] _paylist = plist.split(",");
		paylist = new int[_paylist.length];
		paysignlist = new int[_paylist.length];
		if(plist==null || plist.length()<1){
			return null;
		}
		for (int i = 0; i < _paylist.length; i++) {
			paylist[i] = -1;
		}
		int k=0;
		for (int i = 0; i < _paylist.length; i++) {
			paysignlist[i]= Integer.parseInt(_paylist[i]);
			int n = getPayImg(Integer.parseInt(_paylist[i]));
			if (!getListtype(paylist, n)) {
				paylist[k++] = n;
			}
		}
		return paylist;
	}

	private static boolean getListtype(int[] pay_list, int paysing) {
		for (int i = 0; i < pay_list.length; i++) {
			if (paysing == pay_list[i]) {
				return true;
			}
		}
		return false;
	}

	private static int getPayImg(int paysign) {
		if (paysign == PayManager.PAY_SIGN_MOBILE
				|| paysign == PayManager.PAY_SIGN_MOBILE_WAP
				|| paysign == PayManager.PAY_SIGN_TELECOM
				|| paysign == PayManager.PAY_SIGN_UNICOM_SMS
				/*|| paysign == PayManager.PAY_SIGN_UNICOM_UNIPAY*/
				|| paysign == PayManager.PAY_SIGN_MOBILE_MM
				|| paysign == PayManager.PAY_SIGN_TELECOM_EGAME
				/*|| paysign == PayManager.PAY_SIGN_SKYMOBILE*/) {
			return PAY_BTN_DISPLAY_SMS;
		} else if (paysign == PayManager.PAY_SIGN_ALIPAY) {
			return PAY_BTN_DISPLAY_ALI;
		} else if (paysign == PayManager.PAY_SIGN_WX_SDK) {
			return PAY_BTN_DISPLAY_WX;
		} else {
			return -1;
		}
	}

	/** 充值卡支付 **/
	public static final int PAY_BTN_DISPLAY_CZK = 0;
	/** 短信支付 **/
	public static final int PAY_BTN_DISPLAY_SMS = PAY_BTN_DISPLAY_CZK + 1;
	/** 支付宝支付 **/
	public static final int PAY_BTN_DISPLAY_ALI = PAY_BTN_DISPLAY_CZK + 2;
	/** 银联支付 **/
	public static final int PAY_BTN_DISPLAY_YL = PAY_BTN_DISPLAY_CZK + 3;
	/** 微信支付 **/
	public static final int PAY_BTN_DISPLAY_WX = PAY_BTN_DISPLAY_CZK + 4;
	/** 备用支付 **/
	public static final int PAY_BTN_DISPLAY_SIX = PAY_BTN_DISPLAY_CZK + 5;

	protected Button btn_pay_czk; // 充值卡支付
	protected Button btn_pay_sms; // 短信支付
	protected Button btn_pay_ali; // 支付宝支付
	protected Button btn_pay_yl; // 银联支付
	protected Button btn_pay_wx; // 微信支付
	protected Button btn_pay_six; // 微信支付
	private Button btn_cancel; // 取消按钮
	private TextView text_pay_name; // 道具名称
	private TextView text_pay_cont; // 道具内容
	private TextView text_pay_money; // 道具价格 n
	private TextView text_pay_depict;// 道具介绍
	private TextView text_pay_failure; // 支付失败显示
	private ImageView img_pay_prompt;

	private void init() {
		btn_pay_czk = (Button) findViewById(R.id.pay_but_1);
		btn_pay_czk.setOnClickListener(this);
		btn_pay_sms = (Button) findViewById(R.id.pay_but_2);
		btn_pay_sms.setOnClickListener(this);
		btn_pay_ali = (Button) findViewById(R.id.pay_but_3);
		btn_pay_ali.setOnClickListener(this);
		btn_pay_yl = (Button) findViewById(R.id.pay_but_4);
		btn_pay_yl.setOnClickListener(this);
		btn_pay_wx = (Button) findViewById(R.id.pay_but_5);
		btn_pay_wx.setOnClickListener(this);
		btn_pay_six = (Button) findViewById(R.id.pay_but_6);
		btn_pay_six.setOnClickListener(this);

		btn_cancel = (Button) findViewById(R.id.exit_btn_cancel);
		btn_cancel.setOnClickListener(this);

		text_pay_name = (TextView) findViewById(R.id.pay_tab_name);
		text_pay_cont = (TextView) findViewById(R.id.pay_text_cont);
		text_pay_money = (TextView) findViewById(R.id.pay_text_money);
		text_pay_depict = (TextView) findViewById(R.id.pay_text_depict);

		text_pay_failure = (TextView) findViewById(R.id.pay_text_failure);
		img_pay_prompt = (ImageView) findViewById(R.id.pay_img_prompt);
		setViewdata();
	}

	public void setViewdata() {
//		text_pay_name.setText(mComtext.getString(R.string.pay_prompt));.
		if(text_pay_cont==null){
			return;
		}
		
		if(str_pay_cont.length()>11){
			text_pay_cont.setTextSize(15);
		}else{
			text_pay_cont.setTextSize(18);
		}
		text_pay_cont.setText(str_pay_cont);
		text_pay_money.setText(str_pay_money);
		text_pay_depict.setText(str_pay_depict);
		
		if(str_pay_failure.equals("0")){
			text_pay_failure.setText("话费支付失败，请选择其他支付方式");
		}else{
			text_pay_failure.setVisibility(View.GONE);
			img_pay_prompt.setVisibility(View.GONE);
		}
		for (int i = 0; i < paylist.length; i++) {
			if (getresid(paylist[i]) != -1) {
				setButView(i, getresid(paylist[i]));
			}
		}
	}

	private int getresid(int payimg) {
		int resid = -1;
		switch (payimg) {
		case PAY_BTN_DISPLAY_CZK:
			resid = R.drawable.btn_pay_czk;
			break;
		case PAY_BTN_DISPLAY_SMS:
			resid = R.drawable.btn_pay_sms;
			break;
		case PAY_BTN_DISPLAY_ALI:
			resid = R.drawable.btn_pay_ali;
			break;
		case PAY_BTN_DISPLAY_YL:
			resid = R.drawable.btn_pay_yl;
			break;
		case PAY_BTN_DISPLAY_WX:
			resid = R.drawable.btn_pay_wx;
			break;
		case PAY_BTN_DISPLAY_SIX:
			resid = R.drawable.btn_pay_czk;
			break;
		}
		return resid;
	}

	/***
	 * 设置显示
	 */
	private void setButView(int cod, int resid) {
		switch (cod) {
		case PAY_BTN_DISPLAY_CZK:
			btn_pay_czk.setBackgroundResource(resid);
			btn_pay_czk.setVisibility(View.VISIBLE);
			break;
		case PAY_BTN_DISPLAY_SMS:
			btn_pay_sms.setBackgroundResource(resid);
			btn_pay_sms.setVisibility(View.VISIBLE);

			break;
		case PAY_BTN_DISPLAY_ALI:
			btn_pay_ali.setBackgroundResource(resid);
			btn_pay_ali.setVisibility(View.VISIBLE);

			break;
		case PAY_BTN_DISPLAY_YL:
			btn_pay_yl.setBackgroundResource(resid);
			btn_pay_yl.setVisibility(View.VISIBLE);

			break;
		case PAY_BTN_DISPLAY_WX:
			btn_pay_wx.setBackgroundResource(resid);
			btn_pay_wx.setVisibility(View.VISIBLE);
			break;
		case PAY_BTN_DISPLAY_SIX:
			btn_pay_six.setBackgroundResource(resid);
			btn_pay_six.setVisibility(View.VISIBLE);
			break;
		default:
			break;
		}
	}

	OnClickListener callBack;
	public void setDialogCallBack(OnClickListener onClickListener) {
		this.callBack = onClickListener;
	}
	
	View.OnClickListener butexit;
	public void setCallBack(View.OnClickListener butexit) {
		this.butexit = butexit;
	}
	
	private void clean(){
		if(str_pay_name != null || str_pay_name.length()>0){
			str_pay_name=null;
		}
		if(str_pay_cont != null || str_pay_cont.length()>0){
			str_pay_cont=null;
		}
		if(str_pay_money != null || str_pay_money.length()>0){
			str_pay_money=null;
		}
		if(str_pay_depict != null || str_pay_depict.length()>0){
			str_pay_depict=null;
		}
		if(paylist != null || paylist.length>0){
			paylist=null;
		}
		if(paysignlist != null || paysignlist.length>0){
			paysignlist=null;
		}
		if(str_pay_failure != null || str_pay_failure.length()>0){
			str_pay_failure=null;
		}
		dismiss();
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.pay_but_1:
			if (callBack != null) {
				callBack.onClick(PayListDialog.this, paylist[0]);
			}
			clean();
			AnalyticsUtils.onClickEvent(mComtext, UC.EC_247);
			break;
		case R.id.pay_but_2:
			if (callBack != null) {
				callBack.onClick(PayListDialog.this, paylist[1]);
			}
			clean();
			AnalyticsUtils.onClickEvent(mComtext, UC.EC_247);
			break;
		case R.id.pay_but_3:
			if (callBack != null) {
				callBack.onClick(PayListDialog.this, paylist[2]);
			}
			clean();
			AnalyticsUtils.onClickEvent(mComtext, UC.EC_247);
			break;
		case R.id.pay_but_4:
			if (callBack != null) {
				callBack.onClick(PayListDialog.this, paylist[3]);
			}
			clean();
			AnalyticsUtils.onClickEvent(mComtext, UC.EC_247);
			break;
		case R.id.pay_but_5:
			if (callBack != null) {
				callBack.onClick(PayListDialog.this, paylist[4]);
			}
			clean();
			AnalyticsUtils.onClickEvent(mComtext, UC.EC_247);
			break;
		case R.id.pay_but_6:
			if (callBack != null) {
				callBack.onClick(PayListDialog.this, paylist[5]);
			}
			clean();
			AnalyticsUtils.onClickEvent(mComtext, UC.EC_247);
			break;
		case R.id.exit_btn_cancel:
			if(butexit!=null){
				butexit.onClick(v);
			} 
			clean();
			AnalyticsUtils.onClickEvent(mComtext, UC.EC_249);
			break;
		default:
			break;
		}
	}
}
