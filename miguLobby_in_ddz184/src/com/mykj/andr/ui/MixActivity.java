package com.mykj.andr.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.login.utils.DensityConst;
import com.mingyou.distributor.NetPrivateListener;
import com.mingyou.distributor.NetSocketPak;
import com.mykj.andr.model.HallDataManager;
import com.mykj.andr.model.MixtureInfo;
import com.mykj.andr.model.MixtureInfo.Consume;
import com.mykj.andr.model.UserInfo;
import com.mykj.andr.net.NetSocketManager;
import com.mykj.andr.provider.BackPackItemProvider;
import com.mykj.andr.provider.MixInfoProvider;
import com.mykj.andr.ui.widget.CardZoneProtocolListener;
import com.mykj.andr.ui.widget.SysPopDialog;
import com.mykj.comm.io.TDataInputStream;
import com.mykj.comm.io.TDataOutputStream;
import com.MyGame.Migu.R;
import com.mykj.game.ddz.api.AnalyticsUtils;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.ImageAsyncTaskDownload;
import com.mykj.game.utils.Log;
import com.mykj.game.utils.Toast;
import com.mykj.game.utils.Util;
import com.mykj.game.utils.UtilHelper;

public class MixActivity extends Activity implements OnClickListener {

	private static final String TAG = "MixActivity";
	/** 刷新UI */
	public static final int REFRESH = 1;
	/** 不刷新UI */
	public static final int UNREFRESH = 0;

	private Button btnReduce;
	private Button btnIncrease;
	private Button mMix;

	private ImageView mPropImg;
	private TextView mPropName, mPropDesc;
	private TextView mMixNum;
	private TextView mMixCost;
	private TextView mMixRate;
	private TextView mItemName;
	private TextView mItemNum;
	// private ImageView mItemImg;

	private LinearLayout cousumeItems;
	private RelativeLayout cousumeItem;

	private View mList;

	private LayoutInflater mInflater;

	private MixtureInfo[] mixInfos;
	private MixtureInfo mixInfo;

	private List<Consume> consumeList = new ArrayList<Consume>();

	private int pos;

	/** 用户信息 */
	private UserInfo userInfo;

	/** 合成请求 */
	private static final short LSUB_CMD_COM_PRO_REQ = 800;
	/** 合成返回 */
	private static final short LSUB_CMD_COM_PRO_RESP = 801;
	/** 道具主协议 */
	private static final short LS_MDM_PROP = 17;
	/** 合成成功 */
	private static final int HANDLER_MIX_SUCCESS = 8011;
	/** 合成失败 */
	private static final int HANDLER_MIX_FAIL = 8012;
	/** 合成概率失败 */
	protected static final int HANDLER_MIX_RATE_FAIL = 8014;

	private int userID = 0;
	private Context mContext;
	private Resources mResource;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;

		setContentView(R.layout.mix_activity);

		mResource = this.getResources();

		mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		UserInfo userInfo = HallDataManager.getInstance().getUserMe();
		userID = userInfo.userID;

		initData();
		init();
		initConsume();

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		AnalyticsUtils.onPageStart(this);
		AnalyticsUtils.onResume(this);
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		AnalyticsUtils.onPageEnd(this);
		AnalyticsUtils.onPause(this);
	}

	private void initData() {
		pos = getIntent().getExtras().getInt("position");
		mixInfos = MixInfoProvider.getInstance().getMixtureInfo();
		mixInfo = mixInfos[pos];
		consumeList = mixInfo.consumeList;
	}

	private void init() {
		userInfo = HallDataManager.getInstance().getUserMe();
		((TextView)findViewById(R.id.tvTitle)).setText(R.string.mix);
		findViewById(R.id.tvBack).setOnClickListener(this); // 返回
		mMix = (Button) findViewById(R.id.btn_mix);
		btnReduce = (Button) findViewById(R.id.btnReduce);
		btnIncrease = (Button) findViewById(R.id.btnIncrease);

		mMix.setOnClickListener(this);
		btnReduce.setOnClickListener(this);
		btnIncrease.setOnClickListener(this);
		mPropName = (TextView) findViewById(R.id.mix_prop_name);
		mPropDesc = (TextView) findViewById(R.id.mix_prop_desc);
		mPropImg = (ImageView) findViewById(R.id.mix_prop_img);
		mMixNum = (TextView) findViewById(R.id.tvMixNum);
		mMixCost = (TextView) findViewById(R.id.mix_cost);
		mMixRate = (TextView) findViewById(R.id.mix_precent);
		mPropName.setText(mixInfo.name);
		mPropDesc.setText(mixInfo.desc);
		mMixCost.setText(String.valueOf(mixInfo.beanCost));
		if (userInfo.memberOrder > 10) {
			mMixRate.setText(String.valueOf(mixInfo.vr) + "%");
		} else {
			mMixRate.setText(String.valueOf(mixInfo.or) + "%");
		}
		cousumeItems = (LinearLayout) findViewById(R.id.mix_material_list);

		// 设置图片
		String photoFileName = mixInfo.logo;
		String photoName = photoFileName.substring(0,
				photoFileName.indexOf('.'));
		int drawableId = mContext.getResources().getIdentifier(photoName,
				"drawable", mContext.getPackageName());
		if (drawableId > 0) { // res有图片
			mPropImg.setBackgroundResource(drawableId);
		} else{
			String iconDir=Util.getIconDir();
			File localFile=new File(iconDir,photoFileName);
			if(localFile.exists()){
				Bitmap bitmap = BitmapFactory.decodeFile(localFile.getPath());
				if(bitmap!=null){
					int width = bitmap.getWidth();
					int height = bitmap.getHeight();
					int disWidth = DensityConst.getWidthPixels();
					Bitmap scaleBitmap = Bitmap.createScaledBitmap(bitmap, width
							* disWidth / 800, height * disWidth / 800, true);
					mPropImg.setImageBitmap(scaleBitmap);
				}
			}

		}	

	}

	private void initConsume() {
		int len = consumeList.size();

		for (int i = 0; i < len; i++) {
			mList = mInflater.inflate(R.layout.mix_list_item, null);
			mItemName = (TextView) mList.findViewById(R.id.mix_list_name);
			mItemNum = (TextView) mList.findViewById(R.id.mix_list_num);
			ImageView mItemImg = (ImageView) mList
					.findViewById(R.id.mix_list_img);
			mItemImg.setBackgroundResource(R.drawable.unknown);
			Consume mConsume = consumeList.get(i);
			mItemName.setText(mConsume.name);
			String handCount = "";
			int count=BackPackItemProvider.getInstance().getPorpCount(mConsume.id);
			if (count != 0) {
				handCount = String.valueOf(count);
				mItemNum.setText(handCount + "/" + String.valueOf(mConsume.num));
			} else if (mConsume.t == 0) {
				mItemNum.setText(String.valueOf(mConsume.num));
			} else {
				handCount = "0";
				mItemNum.setText(handCount + "/" + String.valueOf(mConsume.num));
			}

			if (mConsume.t == 0) {
				mItemImg.setImageResource(R.drawable.dj_ledou);
			} else {
				setImageRes(mConsume.logo, mItemImg);
				mItemImg.setScaleType(ScaleType.CENTER_CROP);
			}

			cousumeItems.addView(mList, i);
			cousumeItem = (RelativeLayout) mList
					.findViewById(R.id.mix_list_item);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			lp.setMargins(7, 0, 0, 0);
			cousumeItem.setLayoutParams(lp);
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.tvBack) {
			finish();
		} else if (id == R.id.btn_mix) {
			boolean enough = true;
			short i = Short.parseShort(mMixNum.getText().toString());
			if (userInfo.memberOrder >= mixInfo.vip
					&& userInfo.exp >= mixInfo.exp) {
				int len = consumeList.size();
				for (int j = 0; j < len; j++) {
					Consume mConsume = consumeList.get(j);
					//
					int count=BackPackItemProvider.getInstance().getPorpCount(mConsume.id);
					if (count!=0) {
						int hand = count;
						int need = mConsume.num;
						enough = enough && (hand >= need * i);
					} else {
						enough = false;
					}
				}
				if (enough) {
					requestMix(mixInfo, i);
				} else {
					Toast.makeText(getApplication(), mResource.getString(R.string.package_synthetic_material_shortage),
							Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(getApplication(), mResource.getString(R.string.package_level),
						Toast.LENGTH_SHORT).show();
			}
		} else if (id == R.id.btnReduce) {
			int numReduce = Integer.valueOf(mMixNum.getText().toString());
			if (numReduce > 1) {
				numReduce--;
			}
			mMixNum.setText(Integer.toString(numReduce));
			mMixCost.setText(String.valueOf((mixInfo.beanCost) * numReduce));
			for (int j = 0; j < consumeList.size(); j++) {
				String handCount = "";
				Consume mConsume = consumeList.get(j);
				mItemNum = (TextView) cousumeItems.getChildAt(j).findViewById(
						R.id.mix_list_num);
				int count=BackPackItemProvider.getInstance().getPorpCount(mConsume.id);
				if (count!= 0) {
					handCount = String.valueOf(count);
					mItemNum.setText(handCount + "/"
							+ String.valueOf(mConsume.num * numReduce));
				} else if (mConsume.t == 0) {
					mItemNum.setText(String.valueOf(mConsume.num * numReduce));
				} else {
					handCount = "0";
					mItemNum.setText(handCount + "/"
							+ String.valueOf(mConsume.num * numReduce));
				}
			}
		} else if (id == R.id.btnIncrease) {
			int numIncrease = Integer.valueOf(mMixNum.getText().toString());
			numIncrease++;
			mMixNum.setText(Integer.toString(numIncrease));
			mMixCost.setText(String.valueOf((mixInfo.beanCost) * numIncrease));
			for (int j = 0; j < consumeList.size(); j++) {
				String handCount = "";
				Consume mConsume = consumeList.get(j);
				mItemNum = (TextView) cousumeItems.getChildAt(j).findViewById(
						R.id.mix_list_num);
				int count=BackPackItemProvider.getInstance().getPorpCount(mConsume.id);
				if (count!= 0) {
					handCount = String.valueOf(count);
					mItemNum.setText(handCount + "/"
							+ String.valueOf(mConsume.num * numIncrease));
				} else if (mConsume.t == 0) {
					mItemNum.setText(String.valueOf(mConsume.num * numIncrease));
				} else {
					handCount = "0";
					mItemNum.setText(handCount + "/"
							+ String.valueOf(mConsume.num * numIncrease));
				}
			}
		}

	}

	/***
	 * @Title: requestMix
	 * @Description: TODO发起合成请求
	 * @version: 2012-10-29 下午03:50:10
	 */
	public void requestMix(MixtureInfo mixInfo, final short num) {

		// 创建发送的数据包
		TDataOutputStream tdos = new TDataOutputStream(false);
		tdos.writeShort(mixInfo.index);
		tdos.writeShort(num);

		NetSocketPak pointBalance = new NetSocketPak(LS_MDM_PROP,
				LSUB_CMD_COM_PRO_REQ, tdos);
		// 定义接受数据的协议
		short[][] parseProtocol = { { LS_MDM_PROP, LSUB_CMD_COM_PRO_RESP } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			public boolean doReceive(NetSocketPak netSocketPak) {
				// 解析接受到的网络数据
				try {
					TDataInputStream tdis = netSocketPak.getDataInputStream();
					tdis.setFront(false);
					byte[] bdata = new byte[tdis.available()];
					tdis.read(bdata);
					String dataStr = new String(bdata, "UTF-8");
					String msg = dataStr.replace("\n", "");
					msg=Util.removeBom(msg);
					Log.e(TAG, msg);
					String status = UtilHelper.parseStatusXml(msg, "status");

					if (status.equals("0")) {
						Message message = mMixHandler.obtainMessage(
								HANDLER_MIX_SUCCESS, msg);
						message.arg1 = (int) num;
						mMixHandler.sendMessage(message);
					} else if (status.equals("9001")) {
						Message message = mMixHandler.obtainMessage(
								HANDLER_MIX_RATE_FAIL, msg);
						message.arg1 = (int) num;
						mMixHandler.sendMessage(message);
					} else {
						mMixHandler.sendMessage(mMixHandler.obtainMessage(
								HANDLER_MIX_FAIL, msg));
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
				// 数据处理完成，终止继续解析
				return true;
			}
		};

		nPListener.setOnlyRun(false);
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		// 发送协议
		NetSocketManager.getInstance().sendData(pointBalance);
		// 清理协议对象
		pointBalance.free();
	}

	
	
	@SuppressLint("HandlerLeak")
	private Handler mMixHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLER_MIX_SUCCESS:
				// 合成成功
				BackPackActivity.needRefreshBackpack = true;
				String successed = UtilHelper.parseStatusXml(
						msg.obj.toString(), "successed");
				CardZoneProtocolListener.getInstance(MixActivity.this)
				.requestBackPackList(userID, mMixHandler);
				if (mContext != null) {
					SysPopDialog dialog = new SysPopDialog(MixActivity.this,
							"", mResource.getString(R.string.Ensure), mResource.getString(R.string.package_synthetic_success) + mixInfo.name + successed
							+ mResource.getString(R.string.package_input_you_package), null);

					dialog.show();
				}

				break;

			case HANDLER_MIX_RATE_FAIL:
				// 合成概率失败
				int num_ = msg.arg1;
				int bean_ = HallDataManager.getInstance().getUserMe().bean;
				bean_ = bean_ - mixInfo.beanCost * num_;
				HallDataManager.getInstance().getUserMe().setBean(bean_);
				// 合成失败 HANDLER_MIX_FAIL
				String _toolinfo = UtilHelper.parseStatusXml(
						msg.obj.toString(), "toolinfo");
				if (mContext != null) {
					SysPopDialog dialog = new SysPopDialog(MixActivity.this,
							"", mResource.getString(R.string.Ensure), _toolinfo, null);

					dialog.show();
				}
				break;

			case HANDLER_MIX_FAIL:
				// 合成失败 HANDLER_MIX_FAIL
				String toolinfo = UtilHelper.parseStatusXml(msg.obj.toString(),
						"toolinfo");
				if (mContext != null) {
					SysPopDialog dialog = new SysPopDialog(MixActivity.this,
							"", mResource.getString(R.string.Ensure), toolinfo, null);

					dialog.show();
				}
				break;

			case CardZoneProtocolListener.HANDLER_PACK_QUERY_SUCCESS: // 获取背包列表数据成功
				for (int i = 0; i < consumeList.size(); i++) {
					String handCount = "";
					Consume mConsume = consumeList.get(i);
					mItemNum = (TextView) cousumeItems.getChildAt(i)
							.findViewById(R.id.mix_list_num);
					int count=BackPackItemProvider.getInstance().getPorpCount(mConsume.id);
					if (count!= 0) {
						handCount = String.valueOf(count);
						mItemNum.setText(handCount + "/"
								+ String.valueOf(mConsume.num));
					} else if (mConsume.t == 0) {
						mItemNum.setText(String.valueOf(mConsume.num));
					} else {
						handCount = "0";
						mItemNum.setText(handCount + "/"
								+ String.valueOf(mConsume.num));
					}
				}
				mMixNum.setText("1");
				mMixCost.setText(String.valueOf(mixInfo.beanCost));

				break;

			case CardZoneProtocolListener.HANDLER_PACK_QUERY_SUCCESS_NODATA:
				Log.e("mix", "我的物品没有数据！");
				break;

			default:
				break;
			}
		}
	};

	

	private void setImageRes(String photoFileName, ImageView imageView) {
		if (!Util.isEmptyStr(photoFileName)) {
			String iconDir=Util.getIconDir();
			if (photoFileName.endsWith(".png")||photoFileName.endsWith(".jpg")) {
				int end = photoFileName.length() - 4;
				String photoName = photoFileName.substring(0, end);
				int drawableId = mContext.getResources().getIdentifier(photoName,
						"drawable", mContext.getPackageName());
				if (drawableId > 0) { // res有图片
					imageView.setImageResource(drawableId);
				}else{
					File file=new File(iconDir,photoFileName);
					if(file.exists()){
						Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
						if(bitmap!=null){
							int width = bitmap.getWidth();
							int height = bitmap.getHeight();
							int disWidth = DensityConst.getWidthPixels();
							Bitmap scaleBitmap = Bitmap.createScaledBitmap(bitmap, width
									* disWidth / 800, height * disWidth / 800, true);
							imageView.setImageBitmap(scaleBitmap);
						}else{
							file.delete();
							imageView.setImageResource(R.drawable.goods_icon);
							String url=AppConfig.MIXICON_PATH+"/"+photoFileName;
							new ImageAsyncTaskDownload(url,photoFileName,imageView).execute();
						}

					}else{
						imageView.setImageResource(R.drawable.goods_icon);
						String url=AppConfig.MIXICON_PATH+"/"+photoFileName;
						new ImageAsyncTaskDownload(url,photoFileName,imageView).execute();
					}
				}
			}
             
		}
	}

}
