package com.mykj.andr.ui;

import java.io.File;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mingyou.login.struc.NotifiyDownLoad;
import com.mingyou.login.struc.VersionInfo;
import com.mingyou.login.struc.VersionInfo.GifModel;
import com.MyGame.Midlet.R;
import com.mykj.game.utils.ImageManager;
import com.mykj.game.utils.Util;


public class UpdateDialog extends AlertDialog{

	/**升级提示描述*/
	private TextView tvUpdateContent ;
	
	private TextView tvVersion;
	
	private TextView tvApkSize;
	/**确认升级按钮*/
	private Button btnCancel;
	/**取消升级按钮*/
	private Button btnConfir;

	private Context mContext;
	
	private LinearLayout system_pop_ll_left;
	
	private LinearLayout gifContent;
	
	private VersionInfo versionInfo;
	
	private int rate;
	
	private ImageView ivCancal;
	
	/** 进度说明 */
	private TextView tvRate;

	/** 进度条 */
	private ProgressBar prgRate;
	
	public UpdateDialog(Context context, VersionInfo versionInfo) {
		super(context);
		mContext=context;
		this.versionInfo = versionInfo;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.ddz_update_dialog);
		initView();
	}
	
	private void initView(){
		tvUpdateContent = (TextView) this.findViewById(R.id.tvUpdateContent);
		
		tvVersion = (TextView) this.findViewById(R.id.tvVersion);
		tvApkSize=(TextView) this.findViewById(R.id.tvApkSize);
		btnCancel = (Button) this.findViewById(R.id.btnCancel);
		btnConfir = (Button) this.findViewById(R.id.btnConfir);
		ivCancal = (ImageView) this.findViewById(R.id.iv_cancel);
		system_pop_ll_left= (LinearLayout) this.findViewById(R.id.system_pop_ll_left);
		
		tvRate = (TextView) this.findViewById(R.id.tvRate);
		prgRate = (ProgressBar) this.findViewById(R.id.prgRate);
		gifContent = (LinearLayout) this.findViewById(R.id.gifContent);
		
		String downpath = NotifiyDownLoad.getSdcardPath() + NotifiyDownLoad.APKS_PATH; // 最新版本下载目录
		String fileName = NotifiyDownLoad.getFileNameFromUrl(versionInfo._upUrl); // 最新版本文件名
		File downFile = new File(downpath, fileName);
		if(downFile.exists()){
			system_pop_ll_left.setVisibility(View.GONE);
			btnConfir.setText(mContext.getString(R.string.update_Install));
		}else{
			system_pop_ll_left.setVisibility(View.VISIBLE);
			btnConfir.setText(mContext.getString(R.string.update_Confir));
		}
	}

	/**
	 * 设置更新描述
	 * @param desc 更新描述内容 
	 */
	public void setUpgradeDesc(String desc){
		tvUpdateContent.setText(desc);
	}

	public void setVersion(String version){
		tvVersion.setText(tvVersion.getText() + version);
	}
	
	public void setApkSize(String size){
		tvApkSize.setText(tvApkSize.getText() + size);
	}
	
	public void setGifContent() {
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		List<GifModel> gifModelList = versionInfo.gifModelList;
		for (GifModel model : gifModelList) {
			View card = inflater.inflate(R.layout.version_update_gift_item, null);
			ImageView ivImage = (ImageView)card.findViewById(R.id.gif_img);
			ImageManager.getInstance().loadImageView(getContext(), ivImage, model.picUrl, R.drawable.goods_icon);
			TextView tfName = (TextView)card.findViewById(R.id.gif_name);
			tfName.setText(model.gifName);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);  
			params.setMargins(20, 0, 0, 0);
			gifContent.addView(card, params);
		}
	}
	
	/**
	 * 确定更新所做的操作
	 */
	public void setOnEnsureUpgradeListener(android.view.View.OnClickListener listener){
		btnConfir.setOnClickListener(listener);
	}

	/**
	 * 取消更新所做的操作
	 * 
	 */
	public void setOnCancelUpgradeListener(android.view.View.OnClickListener listener){
		btnCancel.setOnClickListener(listener);
		ivCancal.setOnClickListener(listener);
	}


	public void setOnCancelUpgradeListener(boolean isMustUpdate ,android.view.View.OnClickListener listener){
		if(isMustUpdate){
			system_pop_ll_left.setVisibility(View.GONE);
			btnConfir.setText(mContext.getString(R.string.Ensure));
			this.setCancelable(false);
		}
		btnCancel.setOnClickListener(listener);
		ivCancal.setOnClickListener(listener);
	}
	
	public void setProgressBar(int rate) {
		this.rate = rate;
		if (prgRate != null) {
			prgRate.setProgress(this.rate);
		}
	}

	public void setRateText(int rate, String strRate) {
		StringBuilder sb = new StringBuilder();
		sb.append(rate);
		sb.append('%');
		sb.append(' ');

		if (!Util.isEmptyStr(strRate)) {
			sb.append('(');
			sb.append(strRate);
			sb.append(')');
		}

		if (tvRate != null) {
			tvRate.setText(sb.toString());
		}
	}
	
	public void showInstall(){
		system_pop_ll_left.setVisibility(View.GONE);
		btnConfir.setText(mContext.getString(R.string.update_Install));
	}
	
}
