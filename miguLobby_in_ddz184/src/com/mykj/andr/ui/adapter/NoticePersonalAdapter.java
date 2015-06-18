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

import com.mykj.andr.model.NoticePersonInfo;
import com.MyGame.Migu.R;
import com.mykj.game.utils.UtilHelper;

/****************
 * 个人通知Adapter
 * 
 * @author zhanghuadong 2012-6-18
 */
public class NoticePersonalAdapter extends ArrayListAdapter<NoticePersonInfo> {


	/**消息标题分割符*/
	private static final String SPLIT=">";

	private ViewHolder holder;
	private Activity mAct;
	
	public NoticePersonalAdapter(Activity act) {
		super(act);
		this.mAct = act;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {

			// 获得界面解析器
			LayoutInflater inflater = (LayoutInflater) mAct
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			convertView = inflater.inflate(R.layout.notice_person_item, null);
			holder = new ViewHolder();

			holder.lyPTitle=(LinearLayout) convertView.findViewById(R.id.lyPTitle);
			holder.ivTitleRead=(ImageView) convertView.findViewById(R.id.ivTitleRead);
			holder.ivContentRead=(ImageView) convertView.findViewById(R.id.ivContentRead);

			//holder.tvDay = (TextView) convertView.findViewById(R.id.tvPFromDay);
			//holder.tvTime = (TextView) convertView.findViewById(R.id.tvPTime);
			holder.tvTitle = (TextView) convertView.findViewById(R.id.tvPTitle);
			holder.tvContent = (TextView) convertView.findViewById(R.id.tvPContent);
			holder.tvContent.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if(v.getTag()!=null){
						String content = (String) v.getTag();
						if (content.contains("http:")) {
							int index = content.lastIndexOf("http:");
							String url = content.substring(index);

							String regularExpression = "http://([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?";
							// Pattern p =
							// Pattern.compile("^(http|www|ftp|)?(://)?(//w+(-//w+)*)(//.(//w+(-//w+)*))*((://d+)?)(/(//w+(-//w+)*))*(//.?(//w)*)(//?)?(((//w*%)*(//w*//?)*(//w*:)*(//w*//+)*(//w*//.)*(//w*&)*(//w*-)*(//w*=)*(//w*%)*(//w*//?)*(//w*:)*(//w*//+)*(//w*//.)*(//w*&)*(//w*-)*(//w*=)*)*(//w*)*)$",Pattern.CASE_INSENSITIVE);
							Pattern p = Pattern.compile(regularExpression);
							Matcher m = p.matcher(url);
							if (m.find()) {
								UtilHelper.onWeb(mAct, url);
								//Intent web = new Intent(mAct, WebViewActivity.class);
								//web.putExtra(WebViewActivity.URL, url);
								//mAct.startActivity(web);
							}
						}
					}
				}
			});
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}



		// 下面是赋值
		String fromServer = mList.get(position).fromServer;
		String[] array = null;
		if(fromServer!=null)
			array= fromServer.split(SPLIT);

		if (array!=null &&array.length > 0) {
			if (array.length == 1) {
				holder.lyPTitle.setVisibility(View.GONE);
				holder.ivTitleRead.setVisibility(View.INVISIBLE);
				holder.ivContentRead.setVisibility(View.VISIBLE);

				holder.tvTitle.setText("");     //防止Bug
				holder.tvContent.setText(array[0].trim());
				holder.tvContent.setTag(array[0].trim());
			} else if (array.length == 2) {
				holder.lyPTitle.setVisibility(View.VISIBLE);
				holder.ivTitleRead.setVisibility(View.VISIBLE);
				holder.ivContentRead.setVisibility(View.INVISIBLE);


				holder.tvTitle.setText(array[0].trim());
				holder.tvContent.setText(array[1].trim());
				holder.tvContent.setTag(array[1].trim());
			} else {
				holder.lyPTitle.setVisibility(View.VISIBLE);
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
		//TextView tvDay;
		//TextView tvTime;
		TextView tvTitle;
		TextView tvContent;

		LinearLayout lyPTitle;
		ImageView ivTitleRead;
		ImageView ivContentRead;
	}

}
