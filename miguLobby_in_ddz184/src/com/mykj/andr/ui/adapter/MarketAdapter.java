package com.mykj.andr.ui.adapter;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.login.utils.DensityConst;
import com.mykj.andr.model.GoodsItem;
import com.mykj.andr.pay.PayManager;
import com.MyGame.Migu.R;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.ImageAsyncTaskDownload;
import com.mykj.game.utils.Util;
import com.mykj.game.utils.UtilHelper;

public class MarketAdapter extends BaseAdapter {
	/** 刷新UI */
	public static final int REFRESH = 1;
	/** 不刷新UI */
	public static final int UNREFRESH = 0;
	// 是否禁用乐币称呼（改为点数）
	boolean mIsEnableFlag = false;

	private Activity mAct;
	private List<GoodsItem> mLists;


	public MarketAdapter(Activity act, List<GoodsItem> lists) {
		this.mAct = act;
		this.mLists = lists;
	}



	@Override
	public int getCount() {
		return mLists.size();
	}

	@Override
	public Object getItem(int position) {
		return mLists.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		final GoodsItem goodItem = (GoodsItem) getItem(position);
		if (convertView == null) {
			LayoutInflater inflater = mAct.getLayoutInflater();
			convertView = inflater.inflate(R.layout.market_item, null);
			holder = new ViewHolder();

			holder.ivCorn = (ImageView) convertView.findViewById(R.id.ivCorn);
			holder.ivGoods = (ImageView) convertView.findViewById(R.id.ivGoods);
			holder.tvGoodsName = (TextView) convertView.findViewById(R.id.tvGoodsName);
			holder.tvGoodsPresented = (TextView) convertView.findViewById(R.id.tvGoodsPresented);
			holder.tvGoodsGain = (TextView) convertView.findViewById(R.id.tvGoodsGain);
			// holder.ivGoodsBuy = (ImageView)row.findViewById(R.id.ivGoodsBuy);
			holder.ivGoodsBuy = (Button) convertView.findViewById(R.id.ivGoodsBuy);
			holder.lyChild = (LinearLayout) convertView.findViewById(R.id.lyChild); // 子项容器
			holder.tvGoodsDesc = (TextView) convertView.findViewById(R.id.tvGoodsDesc);
			holder.ivArrow = (ImageView) convertView.findViewById(R.id.ivArrow); // 设置Iitem折叠状态

			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		// 设置图片
		final String photoFileName=goodItem.goodsPhoto;
		holder.ivGoods.setTag(photoFileName);
		holder.ivGoods.setBackgroundResource(R.drawable.goods_icon);
		if (!Util.isEmptyStr(photoFileName)) {
			if (photoFileName.endsWith(".png")||photoFileName.endsWith(".jpg")) {
				int end = photoFileName.length() - 4;
				String photoName = photoFileName.substring(0, end);
				int drawableId = mAct.getResources().getIdentifier(photoName,
						"drawable", mAct.getPackageName());
				if (drawableId > 0) { // res有图片
					holder.ivGoods.setImageResource(drawableId);
				}else{
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
							holder.ivGoods.setImageBitmap(scaleBitmap);
						}else{
							file.delete();
							holder.ivGoods.setImageResource(R.drawable.goods_icon);
							String url=AppConfig.imgUrl+photoFileName;
							new ImageAsyncTaskDownload(url,photoFileName,holder.ivGoods).execute();
						}

					}else{
						holder.ivGoods.setImageResource(R.drawable.goods_icon);
						String url=AppConfig.imgUrl+photoFileName;
						new ImageAsyncTaskDownload(url,photoFileName,holder.ivGoods).execute();
					}
				}
			}

		}
		// 购买按钮
		holder.ivGoodsBuy.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				GoodsItem item = (GoodsItem) v.getTag();
				PayManager.getInstance(mAct).requestBuyPropPlist(item, true, PayManager.MARKET_BUY);
				AppConfig.talkingData(AppConfig.ACTION_MARKET ,item.shopID,-1,"-1");  //7代表    商城列表 - 【购买】
			}
		});
		holder.ivGoodsBuy.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				if (action == MotionEvent.ACTION_DOWN) {
					((Button) v)
					.setBackgroundResource(R.drawable.btn_orange_pressed);
				} else if (action == MotionEvent.ACTION_CANCEL
						|| action == MotionEvent.ACTION_UP) {
					((Button) v)
					.setBackgroundResource(R.drawable.btn_orange_normal);
				}
				return false;
			}
		});
		holder.tvGoodsDesc.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (v.getTag() != null) {
					String content = (String) v.getTag();
					if (content.contains("http:")) {
						int index = content.lastIndexOf("http:");
						String url = content.substring(index);
						String regularExpression = "http://([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?";
						Pattern p = Pattern.compile(regularExpression);
						Matcher m = p.matcher(url);
						if (m.find()) {
							UtilHelper.onWeb(mAct, url);
//							Intent web = new Intent(mAct,WebViewActivity.class);
//							web.putExtra(WebViewActivity.URL, url);
//							mAct.startActivity(web);
						}
					}
				}
			}
		});



		// ---------------------------下面开始设置数据---------------------


		holder.tvGoodsName.setText(goodItem.goodsName); // 物品名称
		if (goodItem.goodsPresented != null
				&& goodItem.goodsPresented.trim().length() > 1){
			holder.tvGoodsPresented.setText(goodItem.goodsPresented); // 赠送
		}else{
			holder.tvGoodsPresented.setText(""); // 赠送
		}

		holder.tvGoodsGain.setText(goodItem.getGoodsPrice());

		if (goodItem != null && goodItem.goodsDescrip != null) {
			holder.tvGoodsDesc.setText(goodItem.goodsDescrip); // 子项商品详细描述
			holder.tvGoodsDesc.setTag(goodItem.goodsDescrip.trim());
		}else{
			holder.tvGoodsDesc.setText("");
			holder.tvGoodsDesc.setTag("");
		}
		holder.ivGoodsBuy.setTag(goodItem);
		// 根据不同值设置角标记:1：打折，2：热卖，3，推荐
		if (goodItem.cornID != 0) {
			if (goodItem.cornID == 1) {
				holder.ivCorn.setBackgroundResource(R.drawable.dazhe);
			} else if (goodItem.cornID == 2) {
				holder.ivCorn.setBackgroundResource(R.drawable.remai);
			} else if (goodItem.cornID == 3) {
				holder.ivCorn.setBackgroundResource(R.drawable.tuijian);
			}
		} else {
			holder.ivCorn.setBackgroundResource(0); // 使用默认标记,透明
		}

		if (goodItem.isArrowUp) {
			holder.ivArrow.setBackgroundResource(R.drawable.arrow_up);
			holder.lyChild.setVisibility(View.VISIBLE);
		} else {
			holder.ivArrow.setBackgroundResource(R.drawable.arrow_down);
			holder.lyChild.setVisibility(View.GONE);
		}
		return convertView;
	}

	class ViewHolder {
		ImageView ivCorn; // 热卖，推荐，打折标记
		ImageView ivGoods; // 商品图标
		TextView tvGoodsName; // 商品名称
		TextView tvGoodsPresented; // 增送信息
		TextView tvGoodsGain; // 乐币，点数
		// ImageView ivGoodsBuy; // 购买按钮
		Button ivGoodsBuy;
		LinearLayout lyChild; // 子项容器
		TextView tvGoodsDesc; // 详细描述
		ImageView ivArrow ;// 折叠显示
	}



}
