package com.mykj.andr.ui;

import java.io.File;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.login.utils.DensityConst;
import com.mykj.andr.model.GoodsItem;
import com.mykj.andr.model.HallDataManager;
import com.mykj.andr.model.UserInfo;
import com.MyGame.Midlet.R;
import com.mykj.game.ddz.api.AnalyticsUtils;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.Util;

public class FastBuyDialog extends AlertDialog implements View.OnClickListener{

	private TextView tvLimitLabel;     //服务器下发 进入服务器限制字段
	private TextView tvPropName;   //商品名字
	private TextView tvPropPoint;  //商品价格
	private TextView tvPropLabel;
	private TextView tvBuyLabel;
	private Button btnBuy;       //购买
	private Button btnCancel;    //取消
	private TextView tvGotoMarket;  //更多商品
	private ImageView imgClose;     //叉
	private ImageView imgPropImg;   //商品图片

	private ImageView imgFirstTitle;   //首充标题
	private ImageView imgFirstIcon;    //首充图标
	private TextView tvFirstDesc;      //首充描述
	private TextView tvCommTitle;      //一般情况下标题
//	private TextView tvServer;         //联系客服说明
//	private ImageButton ibServer;      //联系客服按钮
	
	private String info = "";
	private String subInfo1;
	private String subInfo2;
	private String ensureBtnStr;
	private String cancelBtnStr;
	
	/** 快捷购买的商品 */
	private GoodsItem goodsItem;
	private Context mContext;
	private View.OnClickListener mConfirmCallBack;
	private View.OnClickListener mCancelCallBack;
	
	/**
	 * 构造函数
	 * @param context
	 * @param item
	 * @param info
	 */
	public FastBuyDialog(Context context, GoodsItem item, String info,String ensureBtnStr, String cancelBtnStr) {
		super(context);
		this.mContext = context;
		this.goodsItem = item;
		this.info = info;
		this.ensureBtnStr = ensureBtnStr;
		this.cancelBtnStr = cancelBtnStr;
	}






	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.buy_dialog);

		init();
	}



	/**
	 * 初始化界面
	 */
	private void init() {
		tvLimitLabel = (TextView) findViewById(R.id.limit_label);
		tvPropName = (TextView) findViewById(R.id.prop_name);
		tvPropPoint = (TextView) findViewById(R.id.prop_price);
		tvPropLabel = (TextView) findViewById(R.id.buy_bean);
		tvBuyLabel = (TextView) findViewById(R.id.buy_label);
		imgPropImg = (ImageView) findViewById(R.id.prop_img);
		imgClose = (ImageView) findViewById(R.id.ivCancel);
		btnBuy = (Button) findViewById(R.id.btnFastBuy);
		btnCancel = (Button) findViewById(R.id.btnCancel);
		tvGotoMarket = (TextView) findViewById(R.id.tvGotoMarket);

		if (goodsItem.goodsName != null){
			tvPropName.setText(goodsItem.goodsName);
		}

		tvPropPoint.setText(goodsItem.getGoodsPrice());

		tvPropLabel.setText(goodsItem.goodsPresented);

		// 2012-12-26新增商品描述
		// 2014-2-19 新增高亮需求
		String descrip=goodsItem.goodsDescrip;
//		String buyConfirm=mContext.getString(R.string.buy_confirm);
//		if(descrip.contains(buyConfirm)){
//			int i=descrip.indexOf(buyConfirm, 0);
//			String content =descrip.substring(0,i-1);
//			String str=buyConfirm+mContext.getString(R.string.wenhao);
//			StringBuffer sb = new StringBuffer();
//			sb.append(content);
//			sb.append(mContext.getString(R.string.juhao));
//			sb.append('\n');
//			sb.append(str);
//			String msg = sb.toString();
//			CharSequence colorStr = PayManager.setPayTextStyle(msg, content.length()+1,
//					str.length());
//			tvBuyLabel.setText(colorStr);
//		}else{
//			tvBuyLabel.setText(descrip);
//		}
		
		String hilights = goodsItem.hilightWords;
		if(!Util.isEmptyStr(hilights)){
			int color = 0xffffff00;    //高亮色
			SpannableStringBuilder builder = new SpannableStringBuilder(descrip);
			if(hilights.indexOf("|") > 0){
				String[] hilight = hilights.split("\\|");
				for(int i = 0; i < hilight.length; i++){
					int index = 0;		
					while((index = descrip.indexOf(hilight[i], index)) >= 0){
						builder.setSpan(new ForegroundColorSpan(color), index, index
								+ hilight[i].length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
						index += hilight[i].length();
					}
				}
			}else{
				int index = 0;		
				while((index = descrip.indexOf(hilights, index)) >= 0){
					builder.setSpan(new ForegroundColorSpan(color), index, index
							+ hilights.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
					index += hilights.length();
				}
			}
			tvBuyLabel.setText(builder);
		}else{
			tvBuyLabel.setText(descrip);
		}
		
		if (info.indexOf('^') > 0) {
			subInfo1 = info.substring(0, info.indexOf('^'));
			subInfo2 = info.substring(info.indexOf('^') + 1, info.length());
			tvLimitLabel.setText(subInfo1);
			//tvBuyLabel.setText(subInfo2);
		} else if (info.length() > 1) {
			tvLimitLabel.setText(info);
		} else {
			tvLimitLabel.setVisibility(View.GONE);
		}

		setPropImg();

		btnBuy.setOnClickListener(this);
		btnCancel.setOnClickListener(this);
		tvGotoMarket.setOnClickListener(this);
		imgClose.setOnClickListener(this);

		imgFirstTitle = (ImageView)findViewById(R.id.ivFirstTitle);
		imgFirstIcon  = (ImageView)findViewById(R.id.firstIcon);
		tvFirstDesc = (TextView)findViewById(R.id.firtstDesc);
		tvCommTitle = (TextView)findViewById(R.id.system_pop_dialog_title);

		//V1.6.4新增，根据用户是否付费弹出不同快捷购买界面
		UserInfo usr = HallDataManager.getInstance().getUserMe();
		int showFirstFlag = 1 << 26;        //statusBit第27位标示是否根据用户是否付费弹出不同框，1标示区别用户，0标示不区别用户
		boolean isShowFirst = (usr.statusBit & showFirstFlag) != 0;
		int payUserFlag = 1 << 24;              //statusBit第25位标示是否付费用户，0标示非付费，1标示付费
		boolean isPayUser = (usr.statusBit & payUserFlag) != 0;
		if(isShowFirst && !isPayUser){
			imgFirstTitle.setVisibility(View.VISIBLE);
			imgFirstIcon.setVisibility(View.VISIBLE);
			tvFirstDesc.setVisibility(View.VISIBLE);
			tvCommTitle.setVisibility(View.INVISIBLE);
		}else{
			tvCommTitle.setVisibility(View.VISIBLE);
			imgFirstTitle.setVisibility(View.GONE);
			imgFirstIcon.setVisibility(View.GONE);
			tvFirstDesc.setVisibility(View.GONE);
		}

		if(!Util.isEmptyStr(ensureBtnStr)){
			btnBuy.setText(ensureBtnStr);
		}else{
			if(isShowFirst && !isPayUser){
				btnBuy.setText(getContext().getResources().getString(R.string.market_buy));  //非付费用户购买
			}else{
				btnBuy.setText(getContext().getResources().getString(R.string.Ensure));  //付费用户确定
			}
		}
		if(!Util.isEmptyStr(cancelBtnStr)){
			btnCancel.setText(cancelBtnStr);
		}
		
		//联系客服
//		tvServer = (TextView)findViewById(R.id.tv_server);
//		tvServer.setText(getContext().getResources().getString(R.string.server_description)+ServerDialog.SERVER_PHONE);
//		ibServer = (ImageButton)findViewById(R.id.iv_dial);
//		ibServer.setOnClickListener(this);
//		tvServer.setOnClickListener(this);
	}


	/**
	 * 设置商品图片
	 */
	private void setPropImg() {
		String photoFileName=goodsItem.goodsPhoto;
		if(Util.isEmptyStr(photoFileName) || photoFileName.indexOf('.') <= 0){
			return;
		}
		String photoName = photoFileName.substring(0,
				photoFileName.indexOf('.'));
		int drawableId = mContext.getResources().getIdentifier(photoName,
				"drawable", mContext.getPackageName());
		if (drawableId > 0) { // res有图片
			imgPropImg.setBackgroundResource(drawableId);
		} else{
			String iconDir=Util.getIconDir();
			File file=new File(iconDir,photoFileName);
			if(file.exists()){
				Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
				if(bitmap!=null){
					int width = bitmap.getWidth();
					int height = bitmap.getHeight();
					int disWidth = DensityConst.getWidthPixels();
					Bitmap scaleBitmap = Bitmap.createScaledBitmap(bitmap, width
							* disWidth / 800, height * disWidth / 800, true);
					imgPropImg.setImageBitmap(scaleBitmap);
				}
			}

		}
	}



	/**
	 * 设置取消按键监听
	 * @param callBack
	 */
	public void setCancelCallBack(View.OnClickListener callBack) {
		mCancelCallBack=callBack;
	}


	/**
	 * 设置确定按键监听
	 * @param callBack
	 */
	public void setConfirmCallBack(View.OnClickListener callBack) {
		mConfirmCallBack=callBack;
	}



	/**
	 * 隐藏更多商品按钮
	 */
	public void hideGotoMarket(){
		if(tvGotoMarket!=null){
			tvGotoMarket.setVisibility(View.GONE);
		}
	}


	/**
	 * 隐藏更多商品按钮
	 */
	public void hideBtnCancel(){
		if(btnCancel!=null){
			btnCancel.setVisibility(View.GONE);
		}
	}



	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.btnFastBuy) {
			// 请求快速购买道具
			if (mConfirmCallBack != null) {
				mConfirmCallBack.onClick(v);
				///快捷购买弹框-确认购买，支付宝  统计
				AnalyticsUtils.onClickEvent(mContext, "022");
			}
		} else if (id == R.id.tvGotoMarket) {
			// 进入商城
			AppConfig.talkingData(AppConfig.ACTION_BUY_MORE ,AppConfig.propId,-1,"-1");  //7代表    商城列表 - 【购买】
			mContext.startActivity(new Intent(mContext, MarketActivity.class));
		} else if (id == R.id.ivCancel || id == R.id.btnCancel) {
			if (mCancelCallBack != null) {
				mCancelCallBack.onClick(v);
				//快捷购买弹框-取消购买-统计
				AnalyticsUtils.onClickEvent(mContext, "023");
			} 
		} else if(id == R.id.iv_dial || id == R.id.tv_server){
			String phonenum=ServerDialog.SERVER_PHONE;
			if(!Util.isEmptyStr(phonenum)){
				Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+phonenum));  
				mContext.startActivity(intent); 
			}
		}
		dismiss();
	}





}
