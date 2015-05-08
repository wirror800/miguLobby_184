package com.mykj.andr.ui.adapter;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.login.utils.DensityConst;
import com.mykj.andr.model.ActionInfo;
import com.MyGame.Midlet.R;
import com.mykj.game.utils.ImageAsyncTaskDownload;
import com.mykj.game.utils.Util;

public class ActionAdapter extends BaseAdapter{

	private Activity mAct;
	private List<ActionInfo> mLists;
	private Resources mResource;

	public ActionAdapter(Activity act,List<ActionInfo> list) {
		mAct = act;
		mLists=list;
		mResource = act.getResources();
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
			row = inflater.inflate(R.layout.action_view_item, null);
			holder = new ViewHolder();

			holder.actionDate = (TextView) row.findViewById(R.id.tvActionDate);
			holder.actionName = (TextView) row.findViewById(R.id.tvActionName);
			holder.actionDetails = (TextView) row
					.findViewById(R.id.tvActionDetails);
			holder.actionState = (TextView) row
					.findViewById(R.id.tvActionState);
			holder.actionImg = (ImageView) row.findViewById(R.id.ivActionImage); // 图片
			holder.actionIcon = (ImageView) row.findViewById(R.id.ivActionIcon);

			row.setTag(holder);
		} else {
			holder = (ViewHolder) row.getTag();
		}

		final ActionInfo info = (ActionInfo) getItem(position);

		if (info != null) {
			holder.actionDate.setText("(" + info.date_begin + mResource.getString(R.string.action_to)
					+ info.date_end + ")");
			holder.actionName.setText(info.name);
			holder.actionDetails.setText(info.details);
			// 0=新，1=火
			String icon = info.iconID;

			if (icon.equals("0")) {
				holder.actionIcon.setBackgroundResource(R.drawable.action_new);
			} else if (icon.equals("1")) {
				holder.actionIcon.setBackgroundResource(R.drawable.action_hot);
			} else {
				holder.actionIcon.setBackgroundResource(R.drawable.action_tag);
			}

			int i = 0;
			if (null != info.state && (!info.state.equals(""))) {
				i = Integer.parseInt(info.state);
			}
			// 1=正在进行2=即将开始0=已关闭
			switch (i) {
			case 1:
				holder.actionState.setText(mResource.getString(R.string.action_doing));
				break;

			case 2:
				holder.actionState.setText(mResource.getString(R.string.action_will_begin));
				break;

			case 0:
				holder.actionState.setText(mResource.getString(R.string.action_closed));
				break;

			default:
				break;
			}
		}
		
		// 设置图片
		final String photoFileName=info.picName;
		holder.actionImg.setTag(photoFileName);
		if (!Util.isEmptyStr(photoFileName)) {
			if (photoFileName.endsWith(".png")||photoFileName.endsWith(".jpg")) {
				int end = photoFileName.length() - 4;
				String photoName = photoFileName.substring(0, end);
				int drawableId = mAct.getResources().getIdentifier(photoName,
						"drawable", mAct.getPackageName());
				if (drawableId > 0) { // res有图片
					holder.actionImg.setImageResource(drawableId);
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
							holder.actionImg.setImageBitmap(scaleBitmap);
						}else{
							file.delete();
							holder.actionImg.setImageResource(R.drawable.ad_default);
							String url=info.baseUrl + "/" +photoFileName;
							new ImageAsyncTaskDownload(url,photoFileName,holder.actionImg).execute();
						}

					}else{
						holder.actionImg.setImageResource(R.drawable.ad_default);
						String url=info.baseUrl + "/" +photoFileName;
						new ImageAsyncTaskDownload(url,photoFileName,holder.actionImg).execute();
					}
				}
			}

		}
		
		return row;
	}

	private static class ViewHolder {
		TextView actionDate;
		TextView actionName;
		TextView actionDetails;
		TextView actionState;
		ImageView actionImg;
		ImageView actionIcon;
	}


	

}
