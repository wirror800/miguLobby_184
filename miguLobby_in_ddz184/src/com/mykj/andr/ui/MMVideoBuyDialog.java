package com.mykj.andr.ui;


import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mykj.andr.model.GoodsItem;
import com.mykj.andr.pay.PayManager;
import com.mykj.andr.provider.GoodsItemProvider;
import com.MyGame.Midlet.R;
import com.mykj.game.ddz.api.AnalyticsUtils;
import com.mykj.game.ddz.api.UC;
import com.mykj.game.utils.Util;

public class MMVideoBuyDialog extends AlertDialog implements View.OnClickListener{



	private Context mContext;
    private int mShopId=-1;
	
	private Button btnBuy;       //购买
	private Button btnCancel;    //取消

	private Button btnBuyDiamond; //买钻石
	private Button btnBuyBean;    //买乐豆
	private boolean isBuyDiamond = true;

	//private RelativeLayout contaner; // 容器，根据需要装载卡片或者列表
	private LinearLayout linear_goods;

	private View.OnClickListener mConfirmCallBack;
	private View.OnClickListener mCancelCallBack;

	private static List<BuyItem> mDiamondList = new ArrayList<BuyItem>();
	private static List<BuyItem> mBeanList = new ArrayList<BuyItem>();
	/**
	 * 构造函数
	 * @param context
	 * @param shopId
	 */
	public MMVideoBuyDialog(Context context,int shopId) {
		super(context);
		mContext = context;
		mShopId=shopId;
	}
	

	/**
	 * 构造函数
	 * @param context
	 */
	public MMVideoBuyDialog(Context context) {
		super(context);
		mContext = context;
	}
	
	
	
	
	private static int mDefaultDiamondId = 0;   //默认购买的钻石
	private static int mDefaultBeanId= 0;      //默认购买的乐豆
	public static boolean setData(String strXml){
		boolean rlt = false;
		if(!Util.isEmptyStr(strXml)){
			rlt = true;
			boolean defaultError = false;  //默认值错误
			List<BuyItem> dataList1 = new ArrayList<BuyItem>();
			List<BuyItem> dataList2 = new ArrayList<BuyItem>();
			int defaultDiamondId = 0;
			int defaultBeanId= 0;
			{
				int startIndex = strXml.indexOf("<?");
				if(startIndex > 0){
					strXml = strXml.substring(startIndex);
				}
			}
			try{
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
						String tagName = p.getName();
						if (tagName.equals("item")) {
							BuyItem item = new BuyItem();
							item.id=Integer.parseInt(p.getAttributeValue(null, "id"));
							item.desc = p.getAttributeValue(null, "desc");
							item.num = p.getAttributeValue(null, "num");
							item.price =  Integer.parseInt(p.getAttributeValue(null, "price"));
							item.send = p.getAttributeValue(null, "send");
							item.name = p.getAttributeValue(null, "name");
							if("1".equals(p.getAttributeValue(null, "type"))){
								dataList1.add(item);
							}else if("2".equals(p.getAttributeValue(null, "type"))){
								dataList2.add(item);
							}
							
						}else if(tagName.equals("default")){
							defaultError = true;
							if("1".equals(p.getAttributeValue(null, "type"))){
							defaultDiamondId = Integer.parseInt(p.nextText());
							}else if("2".equals(p.getAttributeValue(null, "type"))){
								defaultBeanId = Integer.parseInt(p.nextText());
							}
						}
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
				if(!defaultError){ //如果不是默认值错误，则认为解析失败，否则错误是可接受的
					rlt = false;
				}
			}
			if(rlt){
				mDiamondList.addAll(dataList1);
				mDefaultDiamondId = defaultDiamondId;
				
				mBeanList.addAll(dataList2);
				mDefaultBeanId = defaultBeanId;
			}
		}
		return rlt;
	}

	public static boolean isDataReady(){
		return !mDiamondList.isEmpty();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.mm_buy_dialog);

		initView();
	}



	/**
	 * 初始化界面
	 */
	private void initView() {
		btnBuy = (Button) findViewById(R.id.btnBuy);
		btnBuy.setOnClickListener(this);

		btnCancel = (Button) findViewById(R.id.btnCancel);
		btnCancel.setOnClickListener(this);


		btnBuyDiamond = (Button) findViewById(R.id.btnBuyDiamond);
		btnBuyDiamond.setOnClickListener(this);

		btnBuyBean = (Button) findViewById(R.id.btnBuyBean);
		btnBuyBean.setOnClickListener(this);


		findViewById(R.id.ivCancel).setOnClickListener(this);

		// 初始化控件
		//contaner = (RelativeLayout) findViewById(R.id.rel_shop_contaner);
		linear_goods=(LinearLayout) findViewById(R.id.linear_goods);

		showBuyDiamond();
		setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface dialog) {
				AnalyticsUtils.onClickEvent(mContext, UC.EC_261);
			}
		});
	}


	/**
	 * 购买钻石
	 */
	private void showBuyDiamond(){

		btnBuyDiamond.setBackgroundResource(R.drawable.diamond_light);
		btnBuyBean.setBackgroundResource(R.drawable.bean_dark);
		showScrollGoods(true);
	}


	/**
	 * 购买乐豆
	 */
	private void showBuyBean(){

		btnBuyDiamond.setBackgroundResource(R.drawable.diamond_dark);
		btnBuyBean.setBackgroundResource(R.drawable.bean_light);
		showScrollGoods(false);
	}

	private int mIndex=-1;
	List<BuyItem> showlist = new ArrayList<BuyItem>();
	private void showScrollGoods(boolean isDiamond){
		isBuyDiamond = isDiamond;
		LayoutInflater inflater = LayoutInflater.from(mContext);
		showlist.clear();
		int imgId;
		int defaultSlct;
		if(isDiamond){
			showlist.addAll(mDiamondList);
			imgId = R.drawable.diamond;
			if(mShopId != -1){
				defaultSlct = mShopId;
			}else{
				defaultSlct = mDefaultDiamondId;
			}
		}else{
			showlist.addAll(mBeanList);
			imgId = R.drawable.mm_bean;
			defaultSlct = mDefaultBeanId;
		}
		int focus = -1;
		for(int i = 0; i < showlist.size(); i++){
			if(showlist.get(i).id == defaultSlct){
				focus = i;
				break;
			}
		}
		if(focus != -1 && showlist.size() > 2){
			BuyItem item = showlist.remove(focus);
			showlist.add(1, item);
			mIndex = 1;
		}else{
			mIndex = 0;
		}

		linear_goods.removeAllViews();
		for (int i = 0; i < showlist.size(); i++) {
			BuyItem item  = showlist.get(i);
			final View card = inflater
					.inflate(R.layout.mmvideo_goods_item, null);
			card.setTag(i);
			((TextView)(card.findViewById(R.id.tvGoodsName))).setText(item.name);
			((ImageView)(card.findViewById(R.id.ivGoods))).setImageResource(imgId);
			TextView diamond=(TextView) card.findViewById(R.id.tvGoodsPresented);
			diamond.setText(item.num);
			TextPaint tp = diamond.getPaint(); 
			tp.setFakeBoldText(true);
			
			((TextView)(card.findViewById(R.id.tvPrice))).setText(item.desc);
			((TextView)(card.findViewById(R.id.tvGoodsGain))).setText(item.send);
			if(mIndex == i){
				card.setBackgroundResource(R.drawable.mm_select_bg);
			}
			
			card.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

					View view=linear_goods.getChildAt(mIndex);
					if(view!=null){
						view.setBackgroundDrawable(null);
					}
					card.setBackgroundResource(R.drawable.mm_select_bg);
					mIndex=(Integer) card.getTag();
				}
			});
			linear_goods.addView(card);
		}
		
		
		
		
		//***********************************************
//		
//		if(!isDiamond){
//			linear_goods.removeAllViews();
//
//			final View card = inflater.inflate(R.layout.mmvideo_goods_item, null);
//			GoodsItem goods=GoodsItemProvider.getInstance().findGoodsItemById(AppConfig.propId);
//			((ImageView)(card.findViewById(R.id.ivGoods))).setImageResource(R.drawable.mm_bean);
//			((TextView)(card.findViewById(R.id.tvGoodsName))).setText(goods.goodsName);
//
//			((TextView)(card.findViewById(R.id.tvGoodsPresented))).setText("");
//			((TextView)(card.findViewById(R.id.tvPrice))).setText(goods.getGoodsPrice());
//			((TextView)(card.findViewById(R.id.tvGoodsGain))).setText(goods.goodsPresented);
//			card.setBackgroundResource(R.drawable.mm_select_bg);
//			mIndex = 1;
//			linear_goods.addView(card);
//		}else{}
	}

	
	
	public void clearShopId(){
		mShopId=-1;
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
	public void hideBtnCancel(){
		if(btnCancel!=null){
			btnCancel.setVisibility(View.GONE);
		}
	}


	
	

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.btnBuy) {
			// 请求快速购买道具
			if (mConfirmCallBack != null) {
				mConfirmCallBack.onClick(v);
			}
			
			if(mIndex >= 0 && mIndex < showlist.size()){
				BuyItem item=showlist.get(mIndex);
				GoodsItem goodsItem=GoodsItemProvider.getInstance().findGoodsItemById(item.id);
				if(goodsItem!=null){
					PayManager.getInstance(mContext).requestBuyPropPlist(goodsItem,
							false, PayManager.GAME_PAY);
					
				}
				//统计数据
				analy(item);
			}
			dismiss();
		}else if (id == R.id.btnBuyDiamond) {
			showBuyDiamond();
		}else if(id == R.id.btnBuyBean){
			showBuyBean();
		}else if (id == R.id.ivCancel || id == R.id.btnCancel) {
			if (mCancelCallBack != null) {
				mCancelCallBack.onClick(v);
			} 
			dismiss();
		} 

	}
	
	private void analy(BuyItem item){
		//这个包含数字
		String count = item.num;
		String eventId = "";
		if(count.contains("50钻石")){
			eventId = UC.EC_262;
			AnalyticsUtils.onClickEvent(mContext, UC.EC_279);
		}else if(count.contains("100钻石")){
			eventId = UC.EC_263;
			AnalyticsUtils.onClickEvent(mContext, UC.EC_279);
		}else if(count.contains("200钻石")){
			eventId = UC.EC_264;
			AnalyticsUtils.onClickEvent(mContext, UC.EC_279);
		}else if(count.contains("500钻石")){
			eventId = UC.EC_265;
			AnalyticsUtils.onClickEvent(mContext, UC.EC_279);
		}else if(count.contains("1000钻石")){
			eventId = UC.EC_266;
			AnalyticsUtils.onClickEvent(mContext, UC.EC_279);
		}else if(count.contains("2000钻石")){
			eventId = UC.EC_267;
			AnalyticsUtils.onClickEvent(mContext, UC.EC_279);
		}else if(count.contains("5000钻石")){
			eventId = UC.EC_281;
			AnalyticsUtils.onClickEvent(mContext, UC.EC_279);
		}else if(count.contains("10000钻石")){
			eventId = UC.EC_282;
			AnalyticsUtils.onClickEvent(mContext, UC.EC_279);
		}else if(count.contains("4万乐豆")){
			eventId = UC.EC_268;
			AnalyticsUtils.onClickEvent(mContext, UC.EC_280);
		}else if(count.contains("12万乐豆")){
			eventId = UC.EC_269;
			AnalyticsUtils.onClickEvent(mContext, UC.EC_280);
		}else if(count.contains("25万乐豆")){
			eventId = UC.EC_270;
			AnalyticsUtils.onClickEvent(mContext, UC.EC_280);
		}else if(count.contains("40万乐豆")){
			eventId = UC.EC_271;
			AnalyticsUtils.onClickEvent(mContext, UC.EC_280);
		}else if(count.contains("70万乐豆")){
			eventId = UC.EC_272;
			AnalyticsUtils.onClickEvent(mContext, UC.EC_280);
		}else if(count.contains("150万乐豆")){
			eventId = UC.EC_273;
			AnalyticsUtils.onClickEvent(mContext, UC.EC_280);
		}else if(count.contains("300万乐豆")){
			eventId = UC.EC_274;
			AnalyticsUtils.onClickEvent(mContext, UC.EC_280);
		}else if(count.contains("800万乐豆")){
			eventId = UC.EC_275;
			AnalyticsUtils.onClickEvent(mContext, UC.EC_280);
		}
		if(!eventId.equals("")){
			AnalyticsUtils.onClickEvent(mContext, eventId);
		}
		
		
	}

	static class BuyItem{
		public int id;
		public String desc;
		public String num;
		public int price;
		public String send;
		public String name;
		public BuyItem(){}
	}

}
