package com.mykj.andr.ui.adapter;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.login.utils.DensityConst;
import com.mykj.andr.model.BackPackItem;
import com.MyGame.Midlet.R;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.ImageAsyncTaskDownload;
import com.mykj.game.utils.Util;
import com.mykj.game.utils.UtilHelper;

/**
 * 
 * @ClassName: BackPageAdapter
 * @Description: 背包adapter
 * @author Administrator
 * @date 2012-7-23 下午02:06:44
 * 
 */
public class BackPageAdapter extends BaseAdapter{

	/** 刷新UI */
	public static final int REFRESH = 1;
	/** 不刷新UI */
	public static final int UNREFRESH = 0;
	private List<BackPackItem> mLists;
	private Activity mAct;
	private UseCallBack mUseCallBack;
	private HandselCallBack mHandselCallBack;

	public BackPageAdapter(Activity act, List<BackPackItem> lists,UseCallBack callBack,
			HandselCallBack handselCallBack) {
		mAct = act;
		mLists=lists;
		mUseCallBack = callBack;
		mHandselCallBack = handselCallBack;
	}


	/**
	 * 重设数据
	 * @param listViews
	 */
	public void setList(List<BackPackItem> lists){
		mLists.clear();
		mLists.addAll(lists);
		this.notifyDataSetChanged();
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
		View row = convertView;
		ViewHolder holder;
		if (row == null) {
			LayoutInflater inflater = mAct.getLayoutInflater();
			row = inflater.inflate(R.layout.backpack_item, null);
			holder = new ViewHolder();

			holder.ivGoods = (ImageView) row.findViewById(R.id.ivGoods);
			holder.tvGoodsName = (TextView) row.findViewById(R.id.tvGoodsName);
			holder.tvGoodsNum = (TextView) row.findViewById(R.id.tvGoodsNum);
			holder.btnGoodsUse = (Button) row.findViewById(R.id.btnGoodsUse);
			holder.btnHandsel = (Button) row.findViewById(R.id.btnHandsel);

			holder.lyChild = (LinearLayout) row.findViewById(R.id.lyChild); // 子项容器
			holder.tvGoodsDesc = (TextView) row.findViewById(R.id.tvGoodsDesc);
			holder.ivArrow = (ImageView) row.findViewById(R.id.ivArrow);

			row.setTag(holder);
		} else {
			holder = (ViewHolder) row.getTag();
		}

		BackPackItem pItem = (BackPackItem) getItem(position);

		// 赠送按钮
		holder.btnHandsel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				BackPackItem item = (BackPackItem) v.getTag();
				if (mHandselCallBack != null) {
					mHandselCallBack.invokeHandsel(item);
				}
			}
		});

		// 购买按钮
		holder.btnGoodsUse.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				BackPackItem item = (BackPackItem) v.getTag();
				if (mUseCallBack != null) {
					mUseCallBack.invoke(item);
				}
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
//							Intent web = new Intent(mAct, WebViewActivity.class);
//							web.putExtra(WebViewActivity.URL, url);
//							mAct.startActivity(web);
						}
					}
				}
			}
		});

		if (((pItem.Attribute[3] >> 1) & 1) != 0) {
			holder.btnHandsel.setEnabled(false);
		} else {
			holder.btnHandsel.setEnabled(true);
		}

		// 设置Iitem折叠状态
		if (pItem.isArrowUp) {
			holder.ivArrow.setBackgroundResource(R.drawable.arrow_up);
			holder.lyChild.setVisibility(View.VISIBLE);
		} else {
			holder.ivArrow.setBackgroundResource(R.drawable.arrow_down);
			holder.lyChild.setVisibility(View.GONE);
		}


		// 设置图片
		final String photoFileName=pItem.backpackPhoto;
		holder.ivGoods.setTag(photoFileName);
		holder.ivGoods.setImageResource(R.drawable.goods_icon);
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


		holder.tvGoodsName.setText(pItem.backpackName); // 物品名称
		// 数量
		holder.tvGoodsNum.setText(mAct.getResources().getString(R.string.action_count) + pItem.newHoldCount);
		if (pItem != null && pItem.backpackDescrip != null) {
			holder.tvGoodsDesc.setText(pItem.backpackDescrip); // 子项商品详细描述
			holder.tvGoodsDesc.setTag(pItem.backpackDescrip.trim());
		}else{
			holder.tvGoodsDesc.setText(""); // 子项商品详细描述
			holder.tvGoodsDesc.setTag("");
		}
		
		holder.btnGoodsUse.setTag(pItem);
		holder.btnHandsel.setTag(pItem);
		return row;
	}

	private static class ViewHolder {
		ImageView ivGoods; // 商品图标
		TextView tvGoodsName; // 商品名称
		TextView tvGoodsNum; // 数目
		Button btnGoodsUse; // 使用按钮
		Button btnHandsel; // 赠送按钮
		LinearLayout lyChild; // 子项容器
		TextView tvGoodsDesc; // 详细描述
		ImageView ivArrow; // 折叠显示
	}

	

	// 使用接口回调
	public interface UseCallBack {
		void invoke(BackPackItem item);
	}

	// 使用接口回调赠送
	public interface HandselCallBack {
		void invokeHandsel(BackPackItem item);
	}


}
