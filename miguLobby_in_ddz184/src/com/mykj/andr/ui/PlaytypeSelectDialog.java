package com.mykj.andr.ui;


import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.mykj.andr.model.AllNodeData;
import com.mykj.game.FiexedViewHelper;
import com.MyGame.Migu.R;

/**
 * @author Administrator
 * 玩法选择
 */
public class PlaytypeSelectDialog extends AlertDialog implements
		android.view.View.OnClickListener {

	private ImageView jingdianCard;		//经典卡片
	private ImageView laiziCard;		//癞子卡片
	private ImageView xiaobingCard;		//小兵卡片
	private Context mContext;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.playtype_select_dialog);
		init();
	}

	private void init() {
		Button btnCancel = (Button)findViewById(R.id.ivCancel);
		btnCancel.setOnClickListener(this);
		
		jingdianCard = (ImageView)findViewById(R.id.iv_jingdian);
		ImageView jingdianTag = (ImageView)findViewById(R.id.iv_tag_jingdian);	//经典角标
		laiziCard = (ImageView)findViewById(R.id.iv_laizi);
		ImageView laiziTag = (ImageView)findViewById(R.id.iv_tag_laizi);		//癞子角标
		laiziTag.setVisibility(View.GONE);
		xiaobingCard = (ImageView)findViewById(R.id.iv_xiaobing);
		ImageView xiaobingTag = (ImageView)findViewById(R.id.iv_tag_xiaobing);	//小兵角标
		xiaobingTag.setVisibility(View.GONE);
		jingdianCard.setOnClickListener(this);
		laiziCard.setOnClickListener(this);
		xiaobingCard.setOnClickListener(this);
		
	}

	public PlaytypeSelectDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		mContext = context;
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if(id == R.id.iv_jingdian){
			FiexedViewHelper.getInstance().setGameType(FiexedViewHelper.GAME_TYPE_NORMAL);
		}else if(id == R.id.iv_laizi){
			FiexedViewHelper.getInstance().setGameType(FiexedViewHelper.GAME_TYPE_LAIZI);
		}else if(id == R.id.iv_xiaobing){
			FiexedViewHelper.getInstance().setGameType(FiexedViewHelper.GAME_TYPE_XIAOBING);
		}else if(id == R.id.ivCancel){
			int oldType = FiexedViewHelper.getInstance().getGameType();
			if(oldType == FiexedViewHelper.GAME_TYPE_UNKNOW){
				//玩家没有选择新玩法且原来没有选择玩法，则用服务器推荐玩法
				int playType = AllNodeData.getInstance(mContext).getPlayId();
				FiexedViewHelper.getInstance().setGameType(playType);
			}
		}
		dismiss();
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub
		if(FiexedViewHelper.getInstance().getCurFragment() == FiexedViewHelper.CARDZONE_VIEW && !FiexedViewHelper.getInstance().isSkipFragment()){
			super.show();
			initIconBackground();
		}
	}
	
	private void initIconBackground(){
		int curSelect = FiexedViewHelper.getInstance().getGameType();

		if(curSelect == FiexedViewHelper.GAME_TYPE_NORMAL){
			jingdianCard.setBackgroundResource(R.drawable.playtype_select_bg);
			laiziCard.setBackgroundResource(0);
			xiaobingCard.setBackgroundResource(0);
		}else if(curSelect == FiexedViewHelper.GAME_TYPE_LAIZI){
			laiziCard.setBackgroundResource(R.drawable.playtype_select_bg);
			jingdianCard.setBackgroundResource(0);
			xiaobingCard.setBackgroundResource(0);
		}else if(curSelect == FiexedViewHelper.GAME_TYPE_XIAOBING){
			xiaobingCard.setBackgroundResource(R.drawable.playtype_select_bg);
			jingdianCard.setBackgroundResource(0);
			laiziCard.setBackgroundResource(0);
		}
	}

}
