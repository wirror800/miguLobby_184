package com.mykj.andr.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.MyGame.Migu.R;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.Toast;

public class DateDialog extends Dialog implements
		android.view.View.OnClickListener {

	private Context ctx;

	private DialogInterface.OnClickListener listener;

	private ListView mDateList;

	private Button ivEnsure;

	private Button ivCancel;

	public static String[] mListStr;

	public DateDialog(Context context) {
		super(context);
		ctx = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.huafeiquan_dialog);
		initUI();
	}

	private void initUI() {
		ivEnsure = (Button) findViewById(R.id.ivEnsure);
		ivCancel = (Button) findViewById(R.id.ivCancel);
		ivEnsure.setOnClickListener(this);
		ivCancel.setOnClickListener(this);

		mDateList = (ListView) findViewById(R.id.date_list);
		mDateList.setAdapter(new ArrayAdapter<String>(ctx,
				R.layout.simple_list_item, mListStr));
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.ivEnsure) {
			if (null != listener) {
				listener.onClick(DateDialog.this, v.getId());
			} else {
				Toast.makeText(ctx, AppConfig.mContext.getString(R.string.ddz_function_not_available), Toast.LENGTH_SHORT).show();
			}
			dismiss();
		} else if (id == R.id.ivCancel) {
			dismiss();
		}
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
}
