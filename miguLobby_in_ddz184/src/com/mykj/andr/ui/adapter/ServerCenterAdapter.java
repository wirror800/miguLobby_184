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
import android.widget.TextView;

import com.login.utils.DensityConst;
import com.mykj.andr.model.ServerItemInfo;
import com.MyGame.Migu.R;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.ImageAsyncTaskDownload;
import com.mykj.game.utils.Util;


/******
 * 服务中心Adapter
 * 
 * @author zhanghuadong
 * 
 */
public class ServerCenterAdapter extends BaseAdapter{

	static final String TAG="ServerCenterAdapter";
	
	private Activity mAct;
	private List<ServerItemInfo> mLists;
	
	public ServerCenterAdapter(Activity act,List<ServerItemInfo> lists) {
		mAct = act;
		mLists=lists;
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
		final ServerItemInfo infos = (ServerItemInfo) getItem(position);
		if (convertView == null) {
			// 获得界面解析器
			LayoutInflater inflater = (LayoutInflater) mAct
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.server_item,null);
			holder = new ViewHolder();
			
			holder.tvServerItem = (TextView) convertView
					.findViewById(R.id.tvServerItem);
			holder.ivServerItem=(ImageView)convertView.findViewById(R.id.ivServerItem);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}


		holder.ivServerItem.setDrawingCacheEnabled(true);

		// 设置图片
		final String photoFileName=infos.img_name;
		holder.ivServerItem.setTag(photoFileName);
		if (!Util.isEmptyStr(photoFileName)) {
			if (photoFileName.endsWith(".png")||photoFileName.endsWith(".jpg")) {
				int end = photoFileName.length() - 4;
				String photoName = photoFileName.substring(0, end);
				int drawableId = mAct.getResources().getIdentifier(photoName,
						"drawable", mAct.getPackageName());
				if (drawableId > 0) { // res有图片
					holder.ivServerItem.setImageResource(drawableId);
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
							holder.ivServerItem.setImageBitmap(scaleBitmap);
						}else{
							file.delete();
							holder.ivServerItem.setImageResource(R.drawable.unknown);
							String url=AppConfig.imgUrl+photoFileName;
							new ImageAsyncTaskDownload(url,photoFileName,holder.ivServerItem).execute();
						}

					}else{
						holder.ivServerItem.setImageResource(R.drawable.unknown);
						String url=infos.img_url;
						new ImageAsyncTaskDownload(url,photoFileName,holder.ivServerItem).execute();
					}
				}
			}

		}


		holder.tvServerItem.setText(infos.title);
		holder.tvServerItem.setTag(infos); // ServerItemInfo保存到Tag

		return convertView;
	}




	public static class ViewHolder {
		public ImageView ivServerItem;
		public TextView tvServerItem;
	}




}
