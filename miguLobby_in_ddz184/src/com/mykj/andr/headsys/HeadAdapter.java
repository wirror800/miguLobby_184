package com.mykj.andr.headsys;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.MyGame.Midlet.R;
import com.mykj.game.utils.UtilHelper;


/**
 * @author Administrator  wanghj
 *
 * 头像列表适配器
 */
public class HeadAdapter extends BaseAdapter {

	private Activity mAct;
	private List<HeadInfo> mLists;    //头像列表

	public HeadAdapter(Activity act, List<HeadInfo> lists) {
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
		final HeadInfo goodItem = (HeadInfo) getItem(position);
		if (convertView == null) {
			LayoutInflater inflater = mAct.getLayoutInflater();
			convertView = inflater.inflate(R.layout.market_item, null);
			holder = new ViewHolder();
			holder.ivCorn = (ImageView) convertView.findViewById(R.id.ivCorn);
			holder.ivGoods = (ImageView) convertView.findViewById(R.id.ivGoods);
			holder.tvGoodsName = (TextView) convertView.findViewById(R.id.tvGoodsName);
			holder.tvGoodsGain = (TextView) convertView.findViewById(R.id.tvGoodsGain);
			holder.ivGoodsBuy = (Button) convertView.findViewById(R.id.ivGoodsBuy);
			holder.lyChild = (LinearLayout) convertView.findViewById(R.id.lyChild); // 子项容器
			holder.tvGoodsDesc = (TextView) convertView.findViewById(R.id.tvGoodsDesc);
			holder.ivArrow = (ImageView) convertView.findViewById(R.id.ivArrow); // 设置Iitem折叠状态
			holder.tvShortDesc = (TextView) convertView.findViewById(R.id.tvGoodsPresented);
			holder.tvExpire = (TextView) convertView.findViewById(R.id.tvExpireDate);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		// 设置图片
		holder.ivGoods.setImageDrawable(HeadManager.getInstance().getZoneHead(mAct, goodItem.getId()));
		
		// 购买按钮
		holder.ivGoodsBuy.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				HeadInfo item = (HeadInfo) v.getTag();
				if(item.getId() == HeadManager.getInstance().getCurId()){
					return;
				}
				if(!item.isHaved()){   //没有拥有，点击购买
					HeadManager.getInstance().requestBuyHead(item);
				}else{   //不是当前使用的，点击使用
					HeadManager.getInstance().requestUseHead(item.getId());
				}
			}
		});
//		holder.ivGoodsBuy.setOnTouchListener(new OnTouchListener() {
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				int action = event.getAction();
//				if (action == MotionEvent.ACTION_DOWN) {
//					((Button) v)
//					.setBackgroundResource(R.drawable.btn_orange_pressed);
//				} else if (action == MotionEvent.ACTION_CANCEL
//						|| action == MotionEvent.ACTION_UP) {
//					((Button) v)
//					.setBackgroundResource(R.drawable.btn_orange_normal);
//				}
//				return false;
//			}
//		});
		holder.ivGoodsBuy.setTag(goodItem);
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
						}
					}
				}
			}
		});

		// ---------------------------下面开始设置数据---------------------
		holder.tvGoodsName.setText(goodItem.getName()); // 物品名称		
		holder.tvGoodsGain.setText(goodItem.getGoodsPrice());     //价格
		holder.tvShortDesc.setText(goodItem.getShortDesc());     //简单描述
		int expireTime = goodItem.getExpireDate();
		if(expireTime > 0){
			int y = expireTime / 10000;
			int m = expireTime % 10000 / 100;
			int d = expireTime % 100;
			holder.tvExpire.setText(""+y+"-"+m+"-"+d+"过期");       //过期时间
		}else{
			holder.tvExpire.setText("");       //过期时间
		}
		holder.tvGoodsDesc.setText(goodItem.getFullDesc()); // 子项商品详细描述
				
		// 根据不同值设置角标记:1：打折，2：热卖，3，推荐
		int cornId = goodItem.getCornId();
		if (cornId != 0) {
			if (cornId == 1) {
				holder.ivCorn.setBackgroundResource(R.drawable.dazhe);
			} else if (cornId == 2) {
				holder.ivCorn.setBackgroundResource(R.drawable.remai);
			} else if (cornId == 3) {
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
		
		if(goodItem.getId() == HeadManager.getInstance().getCurId()){  //按钮状态
			holder.ivGoodsBuy.setText("使用中");
			holder.ivGoodsBuy.setEnabled(false);
		}else if(goodItem.isHaved()){
			holder.ivGoodsBuy.setText("使用");
			holder.ivGoodsBuy.setEnabled(true);
		}else{
			holder.ivGoodsBuy.setText("购买");
			holder.ivGoodsBuy.setEnabled(true);
		}
		
		return convertView;
	}

	class ViewHolder {
		ImageView ivCorn; // 热卖，推荐，打折标记
		ImageView ivGoods; // 商品图标
		TextView tvGoodsName; // 商品名称
		TextView tvGoodsGain; // 价格
		TextView tvShortDesc; //短描述
		TextView tvExpire;    //过期时间
		Button ivGoodsBuy;    //购买或使用按钮
		LinearLayout lyChild; // 子项容器
		TextView tvGoodsDesc; // 详细描述
		ImageView ivArrow ;// 折叠显示
	}
}
