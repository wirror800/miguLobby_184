package com.MyGame.Migu;

import com.MyGame.Migu.R;
import com.mingyou.login.LoginHttp;
import com.mingyou.login.LoginHttp.HttpEvent;
import com.mingyou.login.LoginHttp.HttpReceiveEventCallBack;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegeditActivity extends Activity implements OnClickListener{

	private Context mContext;
	private Button btnBack;
	private Button btnRegister;
	private EditText etAccount;
	private EditText etPassword;
	private EditText etPswConfirm;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext=this;
		setContentView(R.layout.register_view);

		btnBack=(Button)findViewById(R.id.btnBack);
		btnBack.setOnClickListener(this);
		btnRegister=(Button)findViewById(R.id.btnRegister);
		btnRegister.setOnClickListener(this);

		etAccount=(EditText)findViewById(R.id.etAccount);
		etPassword=(EditText)findViewById(R.id.etPassword);
		etPswConfirm=(EditText)findViewById(R.id.etPswConfirm);
	}
	@Override
	public void onClick(View v) {
		int resId=v.getId();
		if(resId==R.id.btnBack){
			finish();
		}else if(resId==R.id.btnRegister){

			final String accoutText = etAccount.getText().toString();
			final String pswText = etPassword.getText().toString();
			final String confirmText = etPswConfirm.getText().toString();

			if (!pswText.equals(confirmText)) {
				Toast.makeText(this,getResources().getString(R.string.login_repassword_error),
						Toast.LENGTH_SHORT).show();
				return;
			}

			final Dialog mDialog = new ProgressDialog(mContext);
			mDialog.show();
			LoginHttp.reqRegistration(accoutText, pswText, new HttpReceiveEventCallBack() {
				@Override
				public void onReceive(final HttpEvent event) {
					mDialog.cancel();
					Builder builder = new AlertDialog.Builder(mContext);
					if (event.getResult() == HttpEvent.LOGIN_SUCCESSED) {
						String alertText = getResources().getString(R.string.login_registe_success_1)
								+ "\n "
								+ getResources().getString(R.string.login_registe_success_2)
								+ ":"
								+ accoutText
								+ "\n"
								+ getResources().getString(R.string.login_registe_success_3)
								+ ":"
								+ pswText
								+ "\n"
								+ getResources().getString(R.string.login_registe_success_4)
								+ "。";

						builder.setTitle(getResources().getString(R.string.login_tip));
						builder.setMessage(alertText);
						builder.setPositiveButton(getResources().getString(R.string.login_ensure),
								new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface arg0, int arg1) {
								Bundle bundle = new Bundle();
								bundle.putString("account", accoutText); 
								bundle.putString("password",pswText); 
								Intent intent = new Intent(); 
								intent.putExtras(bundle); 
								setResult(MyGameMidlet.REGEDIT_OK, intent); 
								finish();
							}
						});
						builder.show();
					} else {
						builder.setTitle(getResources().getString(R.string.login_tip));
						builder.setMessage(event.getMessage());
						builder.setPositiveButton(getResources().getString(R.string.login_ensure),
								new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface arg0, int arg1) {

							}
						});
						builder.show();
					}
				}
			});

		}

	}



}
