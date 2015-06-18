package com.mykj.andr.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.mykj.andr.model.PopularizeDetailList.PopularizeDetail;
import com.MyGame.Migu.R;

public class PopularizeDetailAdapter extends ArrayListAdapter<PopularizeDetail> {

	private Context ctx;
	private GainListener listener;
	private Resources mResource;

	static class ViewHolder {
		TextView date;
		TextView duration;
		TextView state;
		TextView bouns;
		Button gain;
	}

	public PopularizeDetailAdapter(Context context, GainListener gainListener) {
		super(context);
		ctx = context;
		mResource = ctx.getResources();
		listener = gainListener;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder = null;
		if (view == null) {
			LayoutInflater inflater = ((Activity) ctx).getLayoutInflater();
			view = inflater.inflate(R.layout.poplarize_detail_item, null);
			holder = new ViewHolder();
			view.setTag(holder);
//			holder.date = (TextView) view.findViewById(R.id.tv_poplarize_date);
			holder.duration = (TextView) view
					.findViewById(R.id.tv_poplarize_duration);
			holder.state = (TextView) view
					.findViewById(R.id.tv_poplarize_state);
			holder.bouns = (TextView) view
					.findViewById(R.id.tv_poplarize_bouns);
			holder.gain = (Button) view.findViewById(R.id.btn_poplarize_gain);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		final PopularizeDetail detail = mList.get(position);
		if (detail != null) {
//			holder.date.setText(detail.beginTime);
			holder.duration.setText(detail.cond);;
			holder.bouns.setText(detail.reward);
			if (detail.status == 1) {
				holder.gain.setVisibility(View.INVISIBLE);
				holder.state.setVisibility(View.VISIBLE);
				holder.state.setText(mResource.getString(R.string.package_linqu) + detail.awardTime);
			} else if (detail.status == 0) {
				holder.state.setVisibility(View.INVISIBLE);
				holder.gain.setVisibility(View.VISIBLE);
				holder.gain.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						if (listener != null) {
							listener.gainBouns(detail);
						}
					}
				});
			}

		}
		return view;
	}

	public interface GainListener {
		public void gainBouns(PopularizeDetail detail);
	}
}
