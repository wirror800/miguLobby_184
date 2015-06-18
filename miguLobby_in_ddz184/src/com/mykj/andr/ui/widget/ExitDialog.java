package com.mykj.andr.ui.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mykj.andr.model.HallDataManager;
import com.mykj.andr.model.UserInfo;
import com.MyGame.Migu.R;
import com.mykj.game.ddz.api.AnalyticsUtils;
import com.mykj.game.ddz.api.UC;
import com.mykj.game.utils.Util;
import com.mykj.game.utils.UtilHelper;


/*****
 * 
 * @ClassName: ExitDialog
 * @Description: 自定义退出对话框
 * @author zhd
 * @date 2013-2-22 下午02:51:34
 *
 */
public class ExitDialog extends AlertDialog implements android.view.View.OnClickListener{

	Bitmap bitmap =null;
	
	/** 注册送数据 */
	public String giftStr;	
	/** 显示标题 */
	public String title;	
	/** 礼包描述 */
	public String desc;
	/** 用户连续登陆天数 */
	public String day;
	/** 道具id，如果为0表示乐豆 */
	public String type;
	/** 礼包数量 */
	public String count;
	
	private Context mComtext;
	
	public ExitDialog(Context context,Bitmap quickBuyBitmap) {
		super(context);
		mComtext = context;
		this.bitmap=quickBuyBitmap;
	}
	 
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.exit_dialog);
		
		UserInfo userInfo = HallDataManager.getInstance().getUserMe();
		giftStr = Util.getStringSharedPreferences(mComtext, String.valueOf(userInfo.userID) + "_regGift", null);
		if(giftStr != null && !"".equals(giftStr)){
			parseGiftPack(giftStr);
		}

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
	
	
	
	
	protected Button	btn_continute_game;   //继续游戏
	protected Button	btn_exit_game;        //退出游戏
	protected ImageView img_quick_buy;        //快捷购买
	protected ImageView img_more_games;       //更多游戏
	private	  Button    btn_cancel;           //取消按钮
	private TextView gift_name;               //礼包描述
	private TextView tvTop;
	private TextView tvOption;
	private ImageView gift_iv;                //赠送礼包图片
	private void init(){
		btn_continute_game=(Button)findViewById(R.id.img_continute_game);		
		btn_continute_game.setOnClickListener(this);
		btn_exit_game=(Button)findViewById(R.id.img_exit_game);
		btn_exit_game.setOnClickListener(this); 
		btn_cancel = (Button)findViewById(R.id.exit_btn_cancel);
		btn_cancel.setOnClickListener(this);
		gift_name = (TextView)findViewById(R.id.gift_name);
		tvTop = (TextView)findViewById(R.id.tvTop);
		tvOption = (TextView)findViewById(R.id.tvOption);
		gift_iv = (ImageView)findViewById(R.id.gift_iv);
		
		int currentDay = 4;  //下面有小于4的判断
		try{
			currentDay = Integer.valueOf(day);
		}catch(Exception e){
			currentDay = 4;
		}
		if(!Util.isEmptyStr(giftStr) && currentDay < 4){
			tvTop.setText(title);
			gift_name.setText(desc);
			tvOption.setText(mComtext.getResources().getString(R.string.exit_lable_3));
			
			switch (currentDay) {
			case 1:
				gift_iv.setImageResource(R.drawable.reg_gift_1);
				break;
			case 2:
				gift_iv.setImageResource(R.drawable.reg_gift_2);
				break;
			case 3:
				gift_iv.setImageResource(R.drawable.reg_gift_3);
				break;

			default:
				break;
			}
			
			findViewById(R.id.lyimages).setVisibility(View.GONE);
			findViewById(R.id.reggift_area).setVisibility(View.VISIBLE);
		}else{
			findViewById(R.id.lyimages).setVisibility(View.VISIBLE);
			findViewById(R.id.reggift_area).setVisibility(View.GONE);
		}
		
		//快捷购买
		img_quick_buy=(ImageView)findViewById(R.id.img_quick_buy);
		img_quick_buy.setOnClickListener(this); 
		if(bitmap!=null){
			img_quick_buy.setImageBitmap(bitmap);
		}
			
		img_more_games=(ImageView)findViewById(R.id.img_more_games);
		img_more_games.setOnClickListener(this); 
	}
	
	public void parseGiftPack(String giftStr) {
		title = UtilHelper.parseAttributeByName("reg_title", giftStr);
		desc = UtilHelper.parseAttributeByName("reg_desc", giftStr);
		day = UtilHelper.parseAttributeByName("reg_day", giftStr);
		type= UtilHelper.parseAttributeByName("reg_type", giftStr);
		count= UtilHelper.parseAttributeByName("reg_count", giftStr);
	}
	
	@Override
	public void onClick(View v){
		int id = v.getId();
		if (id == R.id.img_exit_game) {           //退出游戏
			if(dialogButtonCallBack!=null){
				dialogButtonCallBack.onClick(ExitDialog.this, v.getId());
				//GameUtilJni.exitApplication();
			}
			dismiss();
			AnalyticsUtils.onClickEvent(mComtext, "019");
		}else if(id == R.id.img_continute_game){  //继续游戏
			dismiss();
//			退出确认弹框-点击继续游戏  统计
			AnalyticsUtils.onClickEvent(mComtext, "020");
		}else if(id == R.id.img_quick_buy){  //快捷购买
			if(callBack!=null){
				callBack.onClick(v);
				AnalyticsUtils.onClickEvent(mComtext, UC.EC_200);
			} 
			dismiss();
		}else if(id == R.id.img_more_games){  //更多游戏
			if(callBack!=null){
				callBack.onClick(v);
				AnalyticsUtils.onClickEvent(mComtext, UC.EC_201);
			} 
			dismiss();
		}else if(id == R.id.exit_btn_cancel){  //取消按钮
			dismiss();
		}
	}

	public boolean onTouchEvent (MotionEvent event){
		return true;
	}

	
	
}
