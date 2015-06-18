package com.mykj.andr.ui;


import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.MyGame.Migu.R;
import com.mykj.game.utils.Util;

public class DownLoadDialog extends Dialog{

	//private TextView tvTitle;
	private TextView tvRate;
	private ProgressBar prgRate;

	public DownLoadDialog(Context context) {
		super(context);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.download_dialog);
		//tvTitle=(TextView) findViewById(R.id.tvTitle);
		tvRate=(TextView) findViewById(R.id.tvRate);
		prgRate=(ProgressBar) findViewById(R.id.prgRate);
	}


	public void setRateText(int rate,String strRate){
		StringBuilder sb=new StringBuilder();
		sb.append(rate);
		sb.append('%');
		sb.append(' ');

		if(!Util.isEmptyStr(strRate)){
			sb.append('(');
			sb.append(strRate);
			sb.append(')');
		}

		tvRate.setText(sb.toString());
	}

	public void setProgressBar(int rate){
		prgRate.setProgress(rate);
	}



}
