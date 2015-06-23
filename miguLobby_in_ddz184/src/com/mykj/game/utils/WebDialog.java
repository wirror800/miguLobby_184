package com.mykj.game.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsListView.LayoutParams;

import com.MyGame.Midlet.R;

public class WebDialog extends Dialog{

	public boolean forceChange = false;
	private WebView webView = null;
	private Context _context =null;
	private ProgressDialog progressDlg = null;
	private static boolean bIsClean = true;
	private int oldOrientation = -5;
	private int newOrientation = -5;
	private static final String TAG = "WebDialog";


	public WebDialog(Context context) {
		super(context);
		_context = context;
		initDialog();

	}

	public WebDialog(Context context, boolean isForceChange, int orientation){
		this(context);
		setChangeOrientation(isForceChange, orientation);
	}

	public WebDialog(Context  context, int theme){
		super(context, theme);
		_context = context;
		initDialog();
	} 

	public WebDialog(Context context, int theme, boolean isForceChange, int orientation){
		this(context, theme);
		setChangeOrientation(isForceChange, orientation);
	}

	@SuppressLint("SetJavaScriptEnabled")
	public void initDialog(){   
		webView = new WebView(_context);
		webView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		this.setContentView(webView);
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
		webView.setWebChromeClient(new WebChromeClient(){
			public void onProgressChanged(WebView view, int progress) {
				if (progress == 100) {
					if (null != progressDlg && progressDlg.isShowing())
						progressDlg.dismiss();
					super.onProgressChanged(view, progress);
				}
			}
		});
		webView.setWebViewClient(new WebViewClient(){
			public boolean shouldOverrideUrlLoading(WebView view, String url) {       
				view.loadUrl(url);       
				return true;       
			}   
		});
	}

	public void setUrl(String url){
		if(bIsClean){
			webView.clearHistory();
			webView.clearCache(true);
			bIsClean = false;
		}

		webView.loadUrl(url);
		show();

		if (null == progressDlg){
			progressDlg = new ProgressDialog(_context);
			String message = AppConfig.mContext.getResources().getString(R.string.ddz_webview_loading);
			progressDlg.setMessage(message);
			progressDlg.show();
		}
	}

	@Override
	public void dismiss() {
		// TODO Auto-generated method stub
		super.dismiss();
		
		//横竖屏还原
		if(forceChange){
			try{
				((Activity)_context).setRequestedOrientation(oldOrientation);
			}catch(Exception e){
				Log.i(TAG, e.toString());
			}
		}
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub
		//切换横竖屏
				if(forceChange){
					try{
						((Activity)_context).setRequestedOrientation(newOrientation);
					}catch(Exception e){
						Log.i(TAG, e.toString());
						forceChange = false;
					}
				}
				
		super.show();
		
		
	}
	
	
	/**
	 * 设置是否横竖屏切换
	 * @param isForceChange 是否设置强制切换
	 * @param orientation 切换成什么样，ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE横屏，ActivityInfo.SCREEN_ORIENTATION_PORTRAIT竖屏
	 */
	private void setChangeOrientation(boolean  isForceChange, int orientation){
		forceChange = isForceChange;
		if(forceChange){
			try{
				oldOrientation = ((Activity)_context).getRequestedOrientation();
				if(oldOrientation == orientation){
					forceChange = false;
				}
				newOrientation = orientation;
			}catch(Exception e){
				Log.i(TAG, e.toString());
				forceChange = false;
			}
		}
	}
	
	
}