package com.MyGame.Migu;


import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import com.MyGame.Migu.R;


public class UpdateDialog extends Dialog{

	/**升级提示描述*/
	private TextView upgradeDesc ;
	/**确认升级按钮*/
	private Button btnUpgrade;
	/**取消升级按钮*/
	private Button btnCancel;

	public UpdateDialog(Context context) {
		super(context);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.update_dialog);
		initView();
	}

	private void initView(){
		upgradeDesc = (TextView) this.findViewById(R.id.upgrade_desc);
		btnUpgrade = (Button) this.findViewById(R.id.btn_upgrade);
		btnCancel = (Button) this.findViewById(R.id.btn_cancel);
	}

	/**
	 * 设置更新描述
	 * @param desc 更新描述内容 
	 */
	public void setUpgradeDesc(String desc){
		upgradeDesc.setText(desc);
	}

	/**
	 * 确定更新所做的操作
	 */
	public void setOnEnsureUpgradeListener(android.view.View.OnClickListener listener){
		btnUpgrade.setOnClickListener(listener);
	}

	/**
	 * 取消更新所做的操作
	 * 
	 */
	public void setOnCancelUpgradeListener(android.view.View.OnClickListener listener){
		btnCancel.setOnClickListener(listener);
	}

}
