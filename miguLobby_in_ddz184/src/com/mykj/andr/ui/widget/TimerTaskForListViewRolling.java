package com.mykj.andr.ui.widget;

import java.util.ArrayList;
import java.util.TimerTask;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mykj.andr.logingift.LotteryDrowHelper;
import com.mykj.andr.logingift.LotteryDrowMainView;
import com.mykj.andr.model.LotteryDrowWinner;
import com.MyGame.Midlet.R;

/**
 * 带有自动滑动的计时器，listview，数据只包含兩列String字符串(适用于抽奖机获奖名单列表)
 * 
 * @author Administrator
 * 
 */
public class TimerTaskForListViewRolling extends TimerTask {
	private ListView listView;
	private int smoothBy = 1;
	private Context mContext;
	private ArrayList<LotteryDrowWinner> winnerList;

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			listView.smoothScrollBy(smoothBy, 0);
		};
	};

	public TimerTaskForListViewRolling(ListView listView, Context context,
			ArrayList<LotteryDrowWinner> winnerList) {
		this.listView = listView;
		listView.setCacheColorHint(0);
		listView.setDividerHeight(0);
		listView.setFadingEdgeLength(0);
		listView.setScrollbarFadingEnabled(true);
		listView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_MOVE) {
					return true;
				}
				return false;
			}
		});
		this.mContext = context;
		this.winnerList = winnerList;
		if (winnerList != null && winnerList.size() > 0) {
			listView.setAdapter(new MyBaseAdapter());
		}
	}

	@Override
	public void run() {
		Message msg = handler.obtainMessage();
		handler.sendMessage(msg);
	}

	private class MyBaseAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return Integer.MAX_VALUE;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return (position % winnerList.size());
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				//每一行view的大小
				RelativeLayout layout = new RelativeLayout(mContext);
				
//				layout.setOrientation(LinearLayout.HORIZONTAL);
				TextView tv1 = new TextView(mContext);
				LotteryDrowHelper.setViewParam(tv1, layout,
						(int) LotteryDrowHelper.getDimension(
								R.dimen.lo_drow_view_winner_name_leftMargin,
								mContext),
						(int) LotteryDrowHelper.getDimension(
								R.dimen.lo_drow_view_winner_name_topMargin,
								mContext));
				tv1.setMaxEms(7);
				tv1.setSingleLine();
				tv1.setEllipsize(TextUtils.TruncateAt.valueOf("END"));
				tv1.setTextColor(mContext.getResources().getColor(
						R.color.winner_list_name_color));
				tv1.setTextSize(LotteryDrowMainView.fontSize3);
				TextView tv2 = new TextView(mContext);
				tv2.setMaxEms(6);
				tv2.setSingleLine();
				tv2.setEllipsize(TextUtils.TruncateAt.valueOf("END"));
				LotteryDrowHelper.setViewParam(tv2, layout,
						(int) LotteryDrowHelper.getDimension(
								R.dimen.lo_drow_view_winner_prodesc_leftMargin,
								mContext),
						(int) LotteryDrowHelper.getDimension(
								R.dimen.lo_drow_view_winner_prodesc_topMargin,
								mContext));
				tv2.setTextColor(mContext.getResources()
						.getColor(R.color.white));
				tv2.setTextSize(LotteryDrowMainView.fontSize3);
				// layout.addView(nameTV);
				// layout.addView(proDescTV);
				convertView = layout;

				holder = new ViewHolder();
				holder.tv1 = tv1;
				holder.tv2 = tv2;
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.tv1.setText(winnerList.get(position % winnerList.size()).getName());
			holder.tv2.setText(winnerList.get(position % winnerList.size()).getProDes());
			return convertView;
		}
	}

	// 获奖名单listview没一行的view
	private class ViewHolder {
		TextView tv1;// 获奖名称
		TextView tv2;// 商品描述
	}
}
