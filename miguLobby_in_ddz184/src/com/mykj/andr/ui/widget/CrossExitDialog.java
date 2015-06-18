package com.mykj.andr.ui.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import com.MyGame.Migu.R;
import com.mykj.game.ddz.api.AnalyticsUtils;

/*****
 * 
 * @ClassName: ExitDialog
 * @Description: 自定义退出对话框
 * @author zhd
 * @date 2013-2-22 下午02:51:34
 * 
 */
public class CrossExitDialog extends AlertDialog implements
		android.view.View.OnClickListener {



	private Context mComtext;

	public CrossExitDialog(Context context) {
		super(context);
		mComtext = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.exit_dialog_cross);
		init();
	}

	View.OnClickListener callBack;

	public void setCallBack(View.OnClickListener callBack) {
		this.callBack = callBack;
	}

	OnClickListener dialogButtonCallBack;

	public void setDialogCallBack(OnClickListener mcallBack) {
		this.dialogButtonCallBack = mcallBack;
	}

	protected Button btn_continute_game; // 继续游戏
	protected Button btn_exit_game; // 退出游戏
	protected ImageView img_more_games; // 更多游戏
	private Button btn_cancel; // 取消按钮

	private void init() {
		btn_continute_game = (Button) findViewById(R.id.img_continute_game);
		btn_continute_game.setOnClickListener(this);
		btn_exit_game = (Button) findViewById(R.id.img_exit_game);
		btn_exit_game.setOnClickListener(this);
		btn_cancel = (Button) findViewById(R.id.exit_btn_cancel);
		btn_cancel.setOnClickListener(this);

		findViewById(R.id.img_circle_anim).startAnimation(
				AnimationUtils.loadAnimation(mComtext, R.anim.circle));
		findViewById(R.id.img_shouzhi).startAnimation(
				AnimationUtils.loadAnimation(mComtext, R.anim.finger_move));
		;

		img_more_games = (ImageView) findViewById(R.id.img_more_games);
		img_more_games.setOnClickListener(this);
	}


	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.img_exit_game) { // 退出游戏
			if (dialogButtonCallBack != null) {
				dialogButtonCallBack.onClick(CrossExitDialog.this, v.getId());
			}
			dismiss();
			AnalyticsUtils.onClickEvent(mComtext, "019");
		} else if (id == R.id.img_continute_game) { // 继续游戏
			dismiss();
			// 退出确认弹框-点击继续游戏 统计
			AnalyticsUtils.onClickEvent(mComtext, "020");
		}else if (id == R.id.img_more_games) { // 更多游戏
			if (callBack != null) {
				callBack.onClick(v);
			}
			dismiss();
		} else if (id == R.id.exit_btn_cancel) { // 取消按钮
			dismiss();
		}
	}

	public boolean onTouchEvent(MotionEvent event) {
		return true;
	}

}
