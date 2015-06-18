package com.mykj.andr.ui.adapter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mykj.andr.model.NoticeSystemInfo;
import com.MyGame.Migu.R;
import com.mykj.game.utils.UtilHelper;

/****************
 * 系统公告Adapter
 * 
 * @author zhanghuadong 2012-6-18
 */

public class NoticeSystemAdapter extends ArrayListAdapter<NoticeSystemInfo> {

	/**消息标题分割符*/
	static final String SPLIT=">";
	
	ViewHolder holder;
	Activity act;
	String format = null; // 格式化

	public NoticeSystemAdapter(Activity act) {
		super(act);
		this.act = act;
		format = act.getResources().getString(R.string.titlecontain);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		NoticeSystemInfo info=mList.get(position);
		
		if (convertView == null) {
			// 获得界面解析器
			LayoutInflater inflater = (LayoutInflater) act
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.notice_system_item, null);
			holder = new ViewHolder();

			holder.lySTitle=(LinearLayout) convertView.findViewById(R.id.lySTitle);
			holder.ivTitleRead=(ImageView) convertView.findViewById(R.id.ivSTitleRead);
			holder.ivContentRead=(ImageView) convertView.findViewById(R.id.ivSContentRead);
			
			// 保留
			holder.tvTitle = (TextView) convertView.findViewById(R.id.tvSTitle);
			holder.tvContent = (TextView) convertView.findViewById(R.id.tvSContent);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final int uo = info.UrlOpenType;
		
		holder.tvContent.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			if(v.getTag()!=null){
				String content = (String) v.getTag();
				if (content.contains("http:")) {
					int index = content.lastIndexOf("http:");
					String url = content.substring(index);

					String regularExpression = "http://([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?";
					
					Pattern p = Pattern.compile(regularExpression);
					Matcher m = p.matcher(url);
					if (m.find()) {
						UtilHelper.onWeb(mContext, url,uo);  //URL打开方式 1 直接内嵌打开2 浏览器打开
					}
				}
			}
			}
		});
		
		String[] array = null;
		// 下面是赋值
		String fromServer = info.fromServer;
		if(fromServer!=null)
			array= fromServer.split(SPLIT);

		if (array!=null && array.length > 0) {
			if (array.length == 1) {
				holder.lySTitle.setVisibility(View.GONE);
				holder.ivTitleRead.setVisibility(View.INVISIBLE);
				holder.ivContentRead.setVisibility(View.VISIBLE);
				holder.tvTitle.setText("");     //防止Bug
				
				holder.tvContent.setText(array[0].trim());
				holder.tvContent.setTag(array[0].trim());
			} else if (array.length == 2) {
				holder.lySTitle.setVisibility(View.VISIBLE);
				holder.ivTitleRead.setVisibility(View.VISIBLE);
				holder.ivContentRead.setVisibility(View.INVISIBLE);
				
				holder.tvTitle.setText(array[0].trim());
				holder.tvContent.setText(array[1].trim());
				holder.tvContent.setTag(array[1].trim());
			} else {
				holder.lySTitle.setVisibility(View.VISIBLE);
				holder.ivTitleRead.setVisibility(View.VISIBLE);
				holder.ivContentRead.setVisibility(View.INVISIBLE);
				
				holder.tvTitle.setText(array[0]);
				StringBuilder sb=new StringBuilder();
				for(int i=1;i<array.length;i++){
					sb.append(array[i].trim());
				}
				holder.tvContent.setText(sb.toString().trim());
				holder.tvContent.setTag(sb.toString().trim());
			}
		}

		return convertView;
	}

	static class ViewHolder {
		// TextView tvDay;
		// TextView tvTime;
		TextView tvTitle;
		TextView tvContent;
		
		LinearLayout lySTitle;
		ImageView ivTitleRead;
		ImageView ivContentRead;
	}

}
