package com.mykj.andr.ui;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.MyGame.Midlet.R;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.Log;
import com.mykj.game.utils.Toast;
import com.mykj.game.utils.UtilHelper;

public class HandselDialog extends Dialog implements
		android.view.View.OnClickListener, OnFocusChangeListener {
	
	private static final String TAG = "HandselDialog";

	DialogInterface.OnClickListener listener;

	Activity ctx;

	protected Button btnEnsure;
	protected Button btnCancel;

	protected EditText et1, et2, et3;

	private String et1Str, et2Str, et3Str;

	private String propName;

	protected TextView mHandselLabel;
	protected TextView mInfo1, mInfo2;
	protected ImageView mError1, mError2;
	protected ImageView mRight1, mRight2;
	protected ImageView arrow;

	private PopupWindow mPopupWindow;
	private MyAdapter mAdapter;
	private ListView mListView;
	private List<String> idStrings = new ArrayList<String>();
	private List<String> revStrings = new ArrayList<String>();

	FileOutputStream outStream;

	String fileStr;
	String[] arrayStr = new String[] {};
	FileInputStream inputStream;

	public static String handselId = "";
	public static String handselNum = "";

	protected HandselDialog(Activity context, String propName) {
		super(context);
		// TODO Auto-generated constructor stub
		ctx = context;
		this.propName = propName;
	}

	/**
	 * @return the listener
	 */
	public DialogInterface.OnClickListener getListener() {
		return listener;
	}

	/**
	 * @param listener
	 *            the listener to set
	 */
	public void setListener(DialogInterface.OnClickListener listener) {
		this.listener = listener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.AlertDialog#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.handsel_dialog);

		init();
	}

	private void init() {
		// TODO Auto-generated method stub
		btnEnsure = (Button) findViewById(R.id.btnConfir);
		btnCancel = (Button) findViewById(R.id.btnCancel);
		btnEnsure.setOnClickListener(this);
		btnCancel.setOnClickListener(this);
		et1 = (EditText) findViewById(R.id.et1_handsel);
		et2 = (EditText) findViewById(R.id.et2_handsel);
		et3 = (EditText) findViewById(R.id.et_handsel_num);
		et3.setText("1");
		et1.setOnFocusChangeListener(this);
		et1.setOnClickListener(this);
		et2.setOnFocusChangeListener(this);
		mHandselLabel = (TextView) findViewById(R.id.handsel_text);
		mHandselLabel.setText(AppConfig.mContext.getResources().getString(R.string.package_largess) + propName);
		mInfo1 = (TextView) findViewById(R.id.handsel_error);
		mInfo2 = (TextView) findViewById(R.id.handsel_error1);
		mError1 = (ImageView) findViewById(R.id.error1);
		mError2 = (ImageView) findViewById(R.id.error2);
		mRight1 = (ImageView) findViewById(R.id.right1);
		mRight2 = (ImageView) findViewById(R.id.right2);
		arrow = (ImageView) findViewById(R.id.down_icon);
		arrow.setOnClickListener(this);
		et2.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				updateInfo2();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});
		try {
			inputStream = ctx.getBaseContext().openFileInput("myfile.txt");
			if (inputStream != null) {
				fileStr = UtilHelper.convertStreamToString(inputStream);
				arrayStr = fileStr.split(",");
				idStrings = Arrays.asList(arrayStr);
				revStrings = reverseList(idStrings);
				Log.e(TAG, UtilHelper.convertStreamToString(inputStream));
			}
			inputStream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			Log.e(TAG, "缓存清除");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		mAdapter = new MyAdapter(ctx);
		mListView = new ListView(ctx);
		mListView.setAdapter(mAdapter);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.btnConfir) {
			if (listener != null) {
				et1Str = et1.getText().toString();
				et2Str = et2.getText().toString();
				et3Str = et3.getText().toString();
				if (et1Str.equals(et2Str) && (et1Str.length() > 0)
						&& (et3Str.length() > 0)) {
					handselId = et2Str;
					handselNum = et3Str;
					listener.onClick(HandselDialog.this, v.getId());
				} else {
					Toast.makeText(ctx, AppConfig.mContext.getResources().getString(R.string.package_largess_info_error), Toast.LENGTH_SHORT)
					.show();
				}

			}
		}else if(id == R.id.btnCancel){
			dismiss();// 退出
		}else if(id == R.id.down_icon){
			// 弹出下拉列表
			if (null == mPopupWindow) {
				mPopupWindow = new PopupWindow(mListView, et1.getWidth(),
						LayoutParams.WRAP_CONTENT);
				mPopupWindow.showAsDropDown(et1);
			} else if (mPopupWindow.isShowing()) {
				mPopupWindow.dismiss();
			} else {
				mPopupWindow.showAsDropDown(et1);
			}
		}else if(id == R.id.et1_handsel){
			if (null != mPopupWindow && mPopupWindow.isShowing()) {
				mPopupWindow.dismiss();
			}
		}

	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		int id = v.getId();
		if (id == R.id.et1_handsel) {
			updateInfo1();
		}else if(id == R.id.et2_handsel){
			updateInfo2();
		}

	}

	private void updateInfo1() {
		// TODO Auto-generated method stub
		et1Str = et1.getText().toString().trim();
		if (et1Str.equals("")) {
			mInfo1.setVisibility(View.INVISIBLE);
			mError1.setVisibility(View.GONE);
			mRight1.setVisibility(View.GONE);
		} else if (et1Str.length() > 15) {
			mInfo1.setVisibility(View.VISIBLE);
			mError1.setVisibility(View.VISIBLE);
			mRight1.setVisibility(View.GONE);
		} else if (et1Str.length() <= 15 && et1Str.length()>0) {
			mInfo1.setVisibility(View.INVISIBLE);
			mError1.setVisibility(View.GONE);
			mRight1.setVisibility(View.VISIBLE);
		}
	}

	private void updateInfo2() {
		// TODO Auto-generated method stub
		et2Str = et2.getText().toString().trim();
		et1Str = et1.getText().toString().trim();
		if (!et2Str.equals("") && !et1Str.equals("")) {
			if (!et2Str.equals(et1Str)) {
				mInfo2.setVisibility(View.VISIBLE);
				mError2.setVisibility(View.VISIBLE);
				mRight2.setVisibility(View.GONE);
			} else if (et2Str.equals(et1Str)) {
				mInfo2.setVisibility(View.INVISIBLE);
				mError2.setVisibility(View.GONE);
				mRight2.setVisibility(View.VISIBLE);
			}
		} else {
			mInfo2.setVisibility(View.INVISIBLE);
			mError2.setVisibility(View.GONE);
			mRight2.setVisibility(View.GONE);
		}
	}

	public void updateHandselSuccess() {
		et1.setText("");
		et2.setText("");
		mInfo1.setVisibility(View.INVISIBLE);
		mInfo2.setVisibility(View.INVISIBLE);
		mError1.setVisibility(View.GONE);
		mRight1.setVisibility(View.GONE);
		mError2.setVisibility(View.GONE);
		mRight2.setVisibility(View.GONE);
	}

	class MyAdapter extends BaseAdapter {
		LayoutInflater mLayoutInflater;

		class ViewHolder {
			TextView mIdText;
			ImageButton mIdDel;
		}

		public MyAdapter(Activity context) {

			mLayoutInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			if (revStrings.size() >= 5) {
				return 5;
			} else if (revStrings.size() == 1 && revStrings.get(0).length() == 0) {
				return 0;
			} else {
				return revStrings.size();
			}

		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			// TODO Auto-generated method stub
			final String id = revStrings.get(position);
			ViewHolder holder = new ViewHolder();
			if (convertView == null) {
				convertView = mLayoutInflater
						.inflate(R.layout.poplist_id, null);
				holder.mIdText = (TextView) convertView
						.findViewById(R.id.tv_id);
				holder.mIdDel = (ImageButton) convertView
						.findViewById(R.id.im_id);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			if (holder.mIdText != null) {
				holder.mIdText.setText(id);
				holder.mIdText.setOnClickListener(new AdapterView.OnClickListener() {

					@Override
					public void onClick(View v) {
						mPopupWindow.dismiss();
						et1.setText(id);
						et2.setText(id);
						mRight1.setVisibility(View.VISIBLE);
					}
				});
//				holder.mIdText.setOnTouchListener(new OnTouchListener() {
//
//					@Override
//					public boolean onTouch(View v, MotionEvent event) {
//						// TODO Auto-generated method stub
//						mPopupWindow.dismiss();
//						et1.setText(id);
//						et2.setText(id);
//						mRight1.setVisibility(View.VISIBLE);
//						return true;
//					}
//				});
			}
			holder.mIdDel.setOnClickListener(new AdapterView.OnClickListener() {

				@Override
				public void onClick(View v) {
					revStrings.remove(position);
					mAdapter.notifyDataSetChanged();
					fileStr = fileStr.replace(id + ",", "");
					try {
						outStream = ctx.openFileOutput("myfile.txt",
								Context.MODE_PRIVATE);
						outStream.write(fileStr.getBytes());
						outStream.close();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
			return convertView;
		}
	}

	public static List<String> reverseList(List<String> list) {
		List<String> revList = new ArrayList<String>();
		for (int i = list.size() - 1; i >= 0; i--) {
			revList.add(list.get(i));
		}
		return revList;
	}
}
