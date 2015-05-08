package com.mykj.andr.ui.adapter;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.login.utils.DensityConst;
import com.mykj.andr.model.MixtureInfo;
import com.MyGame.Midlet.R;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.ImageAsyncTaskDownload;
import com.mykj.game.utils.Util;

public class MixtureAdapter extends BaseAdapter{
	
	static final String TAG = "MixtureAdapter";
	
	/**刷新UI*/
	public static final int REFRESH=1;
	/**不刷新UI*/
	public static final int UNREFRESH=0;
	
	private Activity mAct;
	private List<MixtureInfo> mLists;
	private LayoutInflater mLayoutInflater;

	private static class ViewHolder {
		TextView stupidName;
		TextView stupidDesc;
		ImageView stupidIcon;
	}

	public MixtureAdapter(Activity act,List<MixtureInfo> lists) {
		mAct = act;
		mLists=lists;
		mLayoutInflater = (LayoutInflater) act
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
		final MixtureInfo infos = (MixtureInfo) getItem(position);
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.mix_grid_item, null);
			holder = new ViewHolder();
			holder.stupidName = (TextView) convertView.findViewById(R.id.title);
			holder.stupidDesc = (TextView) convertView.findViewById(R.id.desc);
			holder.stupidIcon = (ImageView) convertView.findViewById(R.id.image);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.stupidName.setText(infos.name);
		holder.stupidDesc.setText(infos.desc);
		holder.stupidIcon.setScaleType(ScaleType.CENTER_CROP);
		
		// 设置图片
		final String photoFileName=infos.logo;
		holder.stupidIcon.setTag(photoFileName);
		if (!Util.isEmptyStr(photoFileName)) {
			if (photoFileName.endsWith(".png")||photoFileName.endsWith(".jpg")) {
				int end = photoFileName.length() - 4;
				String photoName = photoFileName.substring(0, end);
				int drawableId = mAct.getResources().getIdentifier(photoName,
						"drawable", mAct.getPackageName());
				if (drawableId > 0) { // res有图片
					holder.stupidIcon.setImageResource(drawableId);
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
							holder.stupidIcon.setImageBitmap(scaleBitmap);
						}else{
							file.delete();
							holder.stupidIcon.setImageResource(R.drawable.goods_icon);
							String url=AppConfig.imgUrl+photoFileName;
							new ImageAsyncTaskDownload(url,photoFileName,holder.stupidIcon).execute();
						}

					}else{
						holder.stupidIcon.setImageResource(R.drawable.goods_icon);
						String url=AppConfig.imgUrl+photoFileName;
						new ImageAsyncTaskDownload(url,photoFileName,holder.stupidIcon).execute();
					}
				}
			}

		}
		
		return convertView;
	}

	
}
