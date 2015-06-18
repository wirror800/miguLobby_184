package com.mykj.game.utils;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.MyGame.Migu.R;

public class HalfWebDialog extends AlertDialog implements View.OnClickListener{


	private WebView webView = null;

	@SuppressWarnings("unused")
	private Context mContext =null;

	private String mUrl;


	public HalfWebDialog(Context context,String url) {
		super(context);
		mContext = context;
		mUrl=url;

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview_dialog);
		initDialog();
	}


	@SuppressLint("SetJavaScriptEnabled")
	public void initDialog(){   

		webView=(WebView) findViewById(R.id.webview);
		findViewById(R.id.ivCancel).setOnClickListener(this); // 退出
		webView.setScrollBarStyle(0);
		webView.getSettings().setJavaScriptEnabled(true); 

		webView.getSettings().setBuiltInZoomControls(true);
		try {
			webView.getSettings().setAppCacheEnabled(true);
			webView.getSettings().setLoadsImagesAutomatically(true);
			webView.getSettings().setSaveFormData(true);
			webView.getSettings().setSupportZoom(true);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		webView.setWebViewClient(new WebViewClient(){
			public boolean shouldOverrideUrlLoading(WebView view, String url) {       
				view.loadUrl(url);       
				return true;       
			}   
		});

		webView.loadUrl(mUrl);
	}



	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		if (id == R.id.ivCancel) {
			dismiss();
		}
	}


}