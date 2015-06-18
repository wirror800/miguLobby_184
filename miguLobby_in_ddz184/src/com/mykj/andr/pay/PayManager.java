package com.mykj.andr.pay;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;

import com.mingyou.accountInfo.LoginInfoManager;
import com.mingyou.distributor.NetPrivateListener;
import com.mingyou.distributor.NetSocketPak;
import com.mykj.andr.headsys.HeadInfo;
import com.mykj.andr.headsys.HeadManager;
import com.mykj.andr.model.BackPackItem;
import com.mykj.andr.model.GoodsItem;
import com.mykj.andr.model.HPropData;
import com.mykj.andr.model.HallDataManager;
import com.mykj.andr.model.UserInfo;
import com.mykj.andr.net.NetSocketManager;
import com.mykj.andr.pay.payment.AlixPayment;
import com.mykj.andr.pay.payment.MMPayment;
import com.mykj.andr.pay.payment.MobileCMwapPayment;
import com.mykj.andr.pay.payment.MobilePayment;
import com.mykj.andr.pay.payment.MobileSDKPayment;
import com.mykj.andr.pay.payment.TelecomEgamePayment;
import com.mykj.andr.pay.payment.TelecomPayment;
import com.mykj.andr.pay.payment.UnicomSmsPayment;
import com.mykj.andr.pay.payment.UnipayPayment;
import com.mykj.andr.pay.payment.WXPayment;
import com.mykj.andr.provider.BackPackItemProvider;
import com.mykj.andr.provider.GoodsItemProvider;
import com.mykj.andr.ui.BackPackActivity;
import com.mykj.andr.ui.fragment.CardZoneFragment;
import com.mykj.andr.ui.widget.CardZoneDataListener;
import com.mykj.andr.ui.widget.PayListDialog;
import com.mykj.comm.io.TDataInputStream;
import com.mykj.comm.io.TDataOutputStream;
import com.mykj.game.FiexedViewHelper;
import com.MyGame.Migu.R;
import com.mykj.game.ddz.api.AnalyticsUtils;
import com.mykj.game.utils.AppConfig;
import com.mykj.game.utils.Log;
import com.mykj.game.utils.MobileHttpApiMgr;
import com.mykj.game.utils.Toast;
import com.mykj.game.utils.Util;
import com.mykj.game.utils.UtilHelper;
//import com.mykj.andr.pay.payment.MMPayment;

public class PayManager {
	private final static String TAG = "PayManager";

	/** 平台ID **/
	private static final int MOBILEPLATID = 3;
	/** 使用游戏对象 */
	private static final byte GAME_OBJ = 0;

	private static PayManager instance = null;
	private static Context mContext = null;
	private boolean isConfirm = true; // 支付二次确认开关
	private ProgressDialog mProgressDialog;
	private static SparseArray<Long> buyMap = null;

	/** 当前数 **/
	private static int curBackpackNum = 0;

	public static final int HANDLER_SIGNTYPE_SUCCUSS = 0; // 获取支付方式成功
	public static final int HANDLER_SIGNTYPE_FAIL = 1; // 获取支付方式失败
	public static final int HANDLER_NEW_SMS_SUCCESS = 2; // 发送购买短信
	public static final int HANDLER_NEW_SMS_FAIL = 3; // 发送短信失败
	public static final int HANDLER_MARKET_BUY_SUCCESS_UPDATE = 4;// 刷新背包和个人信息
	public static final int HANDLER_MARKET_BUY_BEAN_SUCCESS = 5; // 购买商品返回失败，购买游戏货币属于此情况
	public static final int HANDLER_PACK_QUERY_SUCCESS = 6; // 查询背包数据列表成功
	public static final int HANDLER_PACK_QUERY_SUCCESS_NODATA = 7; // 查询背包数据列表成功，但没数据
	public static final int HANDLER_PACK_QUERY_FAIL = 8; // 背包列表协议接收失败
	public static final int HANDLER_MARKET_USE_SUCCESS = 9; // 道具使用成功
	public static final int HANDLER_MOBILE_WAP_SUCCESS = 10; // 移动wap支付
	public static final int HANDLER_YIDONGMM_SUCCESS = 11; // 移动MM支付
	public static final int HANDLER_YIDONGMM_FAIL = 12; // 移动MM失败
	public static final int HANDLER_BUY_HEAD_SUCCESS = 13; // 购买头像成功
	public static final int HANDLER_BUY_HEAD_FAIL = 14; // 购买头像失败
	public static final int HANDLER_TO_ALIPAY = 15;// 支付宝支付
	public static final int HANDLER_ALIPAY_SUCCESS = 16;// 支付宝支付成功
	public static final int HANDLER_ALIPAY_FAIL = 17;// 支付宝支付失败

	public static final int HANDLER_MARKET_BUY_FAIL_AND_TRY = 18; // 购买商品返回失败,再次发起购买
	public static final int HANDLER_MARKET_BUY_FAIL_NOT_TRY = 19; // 购买商品返回失败，购买结束

	public static final int HANDLER_MARKET_BUY_FAIL_AND_SMS = 50; // 短信购买失败处理

	public static final int  HANDLER_MARKET_BUY_CMCC_SDK_RETRY=20;   // 移动SDK计费重试接口
	public static final int HANDLER_PAYLIST_SUCCUSS = 21; // 获取多种支付列表成功
	public static final int HANDLER_PAYLIST_FAIL = 22; // 获取多种支付列表失败
	
	
	//客户端本地支付标示
	public static final int PAY_SIGN_MOBILE = 0; // 移动
	public static final int PAY_SIGN_ALIPAY = 1; // 支付宝
	public static final int PAY_SIGN_MOBILE_MM = 39; // 移动MM 市场需求1.7.0 去掉
	public static final int PAY_SIGN_MOBILE_WAP = 88;// 移动cmwap
	public static final int PAY_SIGN_TELECOM = 101; // 电信
	//public static final int PAY_SIGN_UNICOM_UNIPAY = 107; // 联通unipay
	public static final int PAY_SIGN_UNICOM_SMS = 111; // 联通短信支付
	public static final int PAY_SIGN_MOBILE_SDK = 103;// 移动sdk
	//public static final int PAY_SIGN_SKYMOBILE = 26;// 斯凯SDK
	public static final int PAY_SIGN_WX_SDK = 147;//微信支付
	public static final int PAY_SIGN_TELECOM_EGAME = 37;// 电信爱游戏

	/**支付购买类型   上传全部支付列表***/
	public static final int PAY_LIST_ALL = 1;
	/**支付购买类型    短信购买时上传部分支付参数***/
	public static final int PAY_LIST_PART = PAY_LIST_ALL+1;
	/**支付购买类型    单种购买时不上传支付参数***/
	public static final int PAY_LIST_NO = PAY_LIST_ALL+2;
	
	/** 购买方式 */
	public static final int FAST_BUY = 0; // 快捷购买
	public static final int MARKET_BUY = 1; // 商城购买
	public static final int BANKRUPTCY = 2; // 破产送
	public static final int GAME_PAY = 3; // 游戏内钻石购买
	public static final int SMS_PAY = 4; // 支付失败短信购买
	
	public static boolean IS_BUY_CANCLE = false; // 是否取消购买，是的话则不用去请求订单，否则去购买。

	/** 道具主协议 */
	private static final short MDM_PROP = 17;
	/** 子协议-签名信息获取 **/
	private static final short MSUB_CMD_SIGNATURE_REQ = 790;
	/** 子协议-签名信息返回 **/
	private static final short MSUB_CMD_SIGNATURE_RESP = 791;
	/** 子协议-签名信息获取 **/
	private static final short MSUB_CMD_PAYLIST_REQ = 812;
	/** 子协议-签名信息返回 **/
	private static final short MSUB_CMD_PAYLIST_RESP = 813;
	/** 子协议-请求购买商品协议 */
	private static final short MSUB_CMD_BUY_SHOP_REQ = 756;

	/** 子协议-获取背包道具列表 */
	private static final short MSUB_CMD_PACK_PROP_LIST_REQ_EX = 795;
	/** 子协议-返回 失败/成功获取背包道具列表 **/
	private static final short MSUB_CMD_PACK_PROP_LIST_RESP = 763;
	/** 子协议-新道具使用请求协议 */
	private static final short MSUB_CMD_USE_PROP_REQ = 780;
	/** 子协议-新道具使用返回 */
	private static final short MSUB_CMD_USE_PROP_RESP = 781;
	/** 子协议-失败/成功：购买商品结果返回 */
	private static final short MSUB_CMD_BUY_SHOP_RESP = 757;
	
	/**是否关闭提示二次确认支付**/
	public static boolean pay_ridio_type =false;

	/**
	 * 私有构造函数
	 * 
	 * @param context
	 */
	private PayManager() {
	}

	public static PayManager getInstance(Context context) {
		if (instance == null) {
			instance = new PayManager();
		}
		mContext = context;

		return instance;
	}

	// 用于第三方支付初始化
	public void thirdPayInit() {
		// 支付初始化在此处添加
		MMPayment.getInstance(mContext).initPayment(); //移动MM支付初始化
		//UnipayPay在android5.0上崩溃
      //UnipayPayment.getInstance(mContext).initPayment();// 联通Unipay支付初始化
		WXPayment.getInstance(mContext).initPayment();
	}

	public void startPlistdialog(final int from) {
		final GoodsItem item=PayListDialog.item;
		if (PayListDialog.paylist == null || PayListDialog.paylist.length < 1 || item == null) {
			Log.e(TAG, "支付列表参数异常：paylist ="+PayListDialog.paylist +"item = "+ item);
			return;
		}
		
		UtilHelper.showPayListDialog(mContext,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case PayListDialog.PAY_BTN_DISPLAY_CZK:
							Toast.makeText(mContext, "充值卡支付暂未开通，敬请期待！",
									Toast.LENGTH_SHORT).show();
							break;
						case PayListDialog.PAY_BTN_DISPLAY_SMS:
							if (getSigntype() != null) {
								requestBuyProp(item, isConfirm,0,PAY_LIST_PART,-1);
							} else {
								Toast.makeText(mContext,
										mContext.getString(R.string.buyerror),
										Toast.LENGTH_SHORT).show();
							}
							// Toast.makeText(mContext, "短信支付",
							// Toast.LENGTH_SHORT).show();
							break;
						case PayListDialog.PAY_BTN_DISPLAY_ALI:
							requestBuyProp(item, isConfirm, PAY_SIGN_ALIPAY,PAY_LIST_NO,-1);
							// Toast.makeText(mContext, "支付宝支付",
							// Toast.LENGTH_SHORT).show();
							break;
						case PayListDialog.PAY_BTN_DISPLAY_YL:
							Toast.makeText(mContext, "银联支付暂未开通，敬请期待！",
									Toast.LENGTH_SHORT).show();
							break;
						case PayListDialog.PAY_BTN_DISPLAY_WX:
							requestBuyProp(item, isConfirm, PAY_SIGN_WX_SDK,PAY_LIST_NO,-1);
							// Toast.makeText(mContext, "微信支付",
							// Toast.LENGTH_SHORT).show();
							break;
						case PayListDialog.PAY_BTN_DISPLAY_SIX:
							Toast.makeText(mContext, "支付暂未开通，敬请期待！",
									Toast.LENGTH_SHORT).show();
							break;
						}

					}
				},
				new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						if(from == BANKRUPTCY){
							CardZoneFragment frag=FiexedViewHelper.getInstance().cardZoneFragment;
							if(frag!=null){
								frag.requestBankruptcy();
							}
						}
					}
				});

	}
	
	@SuppressLint("HandlerLeak")
	private Handler mPayHandler = new Handler(Looper.getMainLooper()) {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLER_SIGNTYPE_SUCCUSS:// 获取支付方式成功
				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
				}

				Bundle b = msg.getData();
				final int signType = b.getInt("signType");
				final String signParam = b.getString("signParam");

				final GoodsItem item = (GoodsItem) msg.obj;
				final int from=msg.arg1;
				final int shopID = item.shopID;
				final int point = item.pointValue;
				
				// 1. 是否需要二次确认 ; 2.支付宝始终不需要二次确认 ；3.斯凯不需要二次确认
				if(from == BANKRUPTCY){
					startPlistdialog(from);
				}else if (from == FAST_BUY
						&& !(signType == PAY_SIGN_MOBILE_SDK || signType == PAY_SIGN_MOBILE_MM)) {

					UtilHelper.showBuyDialog(mContext, new OnClickListener() {
						@Override
						public void onClick(View v) {
							// 调用快速购买方法
							toBuy(signType, signParam, shopID,from);
						}
					}, new OnClickListener() {
						@Override
						public void onClick(View v) {
							startPlistdialog(from);
						}
					}, item, "", null, null, false);
				} else if (isConfirm && signType != PAY_SIGN_ALIPAY
						&& signType != PAY_SIGN_TELECOM_EGAME
						&& signType != PAY_SIGN_MOBILE_MM
						//&& signType != PAY_SIGN_UNICOM_UNIPAY
						//&& signType != PAY_SIGN_SKYMOBILE
						&& signType != PAY_SIGN_WX_SDK
						&& signType != PAY_SIGN_MOBILE_SDK
						&& !getSharedPreferences()
						&& from != SMS_PAY) {
					int mdoValue = 0;
					CharSequence payColorStr = null;
					String mdo = UtilHelper.parseStatusXml(signParam, "mdo");
					try {
						mdoValue = Integer.parseInt(mdo);
					} catch (NumberFormatException e) {
						mdoValue = 0;
					}

					String telno = UtilHelper
							.parseStatusXml(signParam, "telno");
					if (mdoValue == 1) {
						payColorStr = getMdoPayColorStr(item, telno);
					} else {
						payColorStr = getCmccPayColorStr(item, telno);
					}

					UtilHelper.showCustomDialogWithServer(mContext,
							payColorStr, new OnClickListener() {
								@Override
								public void onClick(View v) {
									// 这里调用购买协议发送
									toBuy(signType, signParam, shopID,from);

								}
							}, new OnClickListener() {
								@Override
								public void onClick(View v) {
									startPlistdialog(from);
								}
							}, true);
				} else {
					toBuy(signType, signParam, shopID,from);
				}
				break;
			case HANDLER_SIGNTYPE_FAIL:
				String error = msg.obj.toString();
				UtilHelper.showCustomDialogWithServer(mContext, error);

				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
				}
				break;
			case HANDLER_MARKET_BUY_SUCCESS_UPDATE:// 刷新背包和个人信息
				int goodsId = msg.arg1;
				BuyGoods buyedGoods = (BuyGoods) msg.obj;
				int userId = FiexedViewHelper.getInstance().getUserId();
				requestBackPackList(userId, goodsId, buyedGoods);
				break;

			case HANDLER_MARKET_BUY_BEAN_SUCCESS: // 购买商品返回失败

				BuyGoods failGoods = (BuyGoods) msg.obj;
				String goodsInfo = failGoods.goodsInfo;
				int result = msg.arg2;
				if (!Util.isEmptyStr(goodsInfo)) {
					SpannableStringBuilder str = new SpannableStringBuilder(
							goodsInfo);
					String hilights = failGoods.hilightWords;
					if (hilights != null) {
						if (hilights.contains("|")) {
							String[] hilight = hilights.split("\\|");
							for (int i = 0; i < hilight.length; i++) {
								int index = 0;
								while ((index = goodsInfo.indexOf(hilight[i],
										index)) >= 0) {
									str.setSpan(new ForegroundColorSpan(
											0xffffff00), index, index
											+ hilight[i].length(),
											Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
									index += hilight[i].length();
								}
							}
						} else {
							int index = 0;
							while ((index = goodsInfo.indexOf(hilights, index)) >= 0) {
								str.setSpan(
										new ForegroundColorSpan(0xffffff00),
										index, index + hilights.length(),
										Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
								index += hilights.length();
							}
						}
					}
					FiexedViewHelper instance = FiexedViewHelper.getInstance();
					boolean isSkip = instance.isSkipFragment();
					boolean isCardZone = instance.getCurFragment() == FiexedViewHelper.CARDZONE_VIEW;
					boolean isFragment = instance.isFragmentActivity();

					if (result == 48 && isFragment && isCardZone && !isSkip) {
						View.OnClickListener lstner = new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								FiexedViewHelper.getInstance().startGame = true;
							}
						};
						android.content.DialogInterface.OnDismissListener disL = new android.content.DialogInterface.OnDismissListener() {
							@Override
							public void onDismiss(DialogInterface dialog) {
								// TODO Auto-generated method stub
								FiexedViewHelper.getInstance()
										.requestUserBean();
							}
						};
						UtilHelper.showCustomDialogWithServer(mContext, str,
								lstner,
								mContext.getString(R.string.start_game), disL);

					} else if (result == 48) {
						android.content.DialogInterface.OnDismissListener disL = new android.content.DialogInterface.OnDismissListener() {
							@Override
							public void onDismiss(DialogInterface dialog) {
								// TODO Auto-generated method stub
								FiexedViewHelper.getInstance()
										.requestUserBean();
							}
						};
						UtilHelper
								.showCustomDialogWithServer(mContext, str,
										null,
										mContext.getString(R.string.i_know),
										disL);
					} else {
						UtilHelper.showCustomDialogWithServer(mContext, str);
					}
					handMessageToCardZoneForSmallMoneyPkg(failGoods.propId);

				}

				break;
			case HANDLER_PACK_QUERY_SUCCESS:// 查询背包数据列表成功
				final int shopID1 = msg.arg1;
				BuyGoods goods = (BuyGoods) msg.obj;
				int propCount = goods.propCount;
				String goodsInfos = goods.goodsInfo;
				String hilights = goods.hilightWords;
				SpannableStringBuilder str = new SpannableStringBuilder(
						goodsInfos);
				if (hilights != null) {
					if (hilights.contains("|")) {
						String[] hilight = hilights.split("\\|");
						for (int i = 0; i < hilight.length; i++) {
							int index = 0;
							while ((index = goodsInfos.indexOf(hilight[i],
									index)) >= 0) {
								str.setSpan(
										new ForegroundColorSpan(0xffffff00),
										index, index + hilight[i].length(),
										Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
								index += hilight[i].length();
							}
						}
					} else {
						int index = 0;
						while ((index = goodsInfos.indexOf(hilights, index)) >= 0) {
							str.setSpan(new ForegroundColorSpan(0xffffff00),
									index, index + hilights.length(),
									Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
							index += hilights.length();
						}
					}
				}
				HPropData[] propData = goods.propData;
				if (propCount > 0) {
					if (propData != null) {
						// 快速使用
						final BackPackItem backpackInfo = BackPackItemProvider
								.getInstance().getBackPackItem(shopID1);

						UtilHelper.showCustomDialogWithServer(mContext, str,
								new OnClickListener() {
									@Override
									public void onClick(View v) {
										int userId = FiexedViewHelper
												.getInstance().getUserId();
										if (backpackInfo != null) {
											short urlId = backpackInfo.urlId;
											if (urlId != 0) {
												String userToken = LoginInfoManager
														.getInstance()
														.getToken();
												UtilHelper.showWebView(
														userToken, urlId,
														userId, mContext);
											} else {
												final long CLISEC = 0;
												final byte CBTYPE = 0; // 无扩展数据
												int exData = 0; // 附加参数（如需要GameID、RoomID的道具，可传入相关数据，默认为0）
												if ((null != backpackInfo.Attribute)
														&& ((backpackInfo.Attribute[1] >> GAME_OBJ) & 1) == 1) {
													exData = AppConfig.gameId;
												}
												requestUseMarketGoods(userId,
														shopID1,
														backpackInfo.IndexID,
														exData, CLISEC, CBTYPE);
											}
										}
									}
								});
					}
				} else if (propCount != -1) {
					// 进入我的物品
					UtilHelper.showCustomDialogWithServer(mContext, str,
							new OnClickListener() {

								@Override
								public void onClick(View v) {
									Intent intent = new Intent(mContext
											.getApplicationContext(),
											BackPackActivity.class);
									mContext.startActivity(intent);
								}
							});

				}
				break;

			// 查询背包数据列表成功，但没数据
			case HANDLER_PACK_QUERY_SUCCESS_NODATA:
				Log.e(TAG, "我的物品没有数据！");
				break;

			case HANDLER_PACK_QUERY_FAIL:// 背包列表协议接收失败
				UtilHelper.showCustomDialogWithServer(mContext, mContext
						.getResources().getString(R.string.market_buy_success));
				break;
			case HANDLER_MARKET_USE_SUCCESS:
				String strInfo = (String) msg.obj;
				if (!Util.isEmptyStr(strInfo)) {
					UtilHelper.showCustomDialogWithServer(mContext, strInfo);
				}
				break;
			case HANDLER_BUY_HEAD_SUCCESS:
				HeadManager.getInstance().buyHeadSuccess(msg.arg1);
				break;
			case HANDLER_BUY_HEAD_FAIL:
				BuyGoods bgoods = (BuyGoods) msg.obj;
				HeadManager.getInstance().buyHeadFail(msg.arg1, msg.arg2,
						bgoods.goodsInfo);
				break;
			case HANDLER_MARKET_BUY_FAIL_AND_TRY:
				BuyGoods buyFailGoods = (BuyGoods) msg.obj;
				String err = buyFailGoods.goodsInfo;
				final int id = buyFailGoods.propId;
				OnClickListener listener = new OnClickListener() {
					@Override
					public void onClick(View v) {
						GoodsItem item = GoodsItemProvider.getInstance()
								.findGoodsItemById(id);
						requestBuyProp(item);

					}
				};
				UtilHelper.showCustomDialogWithServer(mContext, err, "其他支付",
						listener, "我知道了", null, null);
				break;
			case HANDLER_MARKET_BUY_FAIL_AND_SMS:
				BuyGoods buyFailGoods1 = (BuyGoods) msg.obj;
				String Param = buyFailGoods1.goodsInfo;
				int shopId=msg.arg1;
				getPaydata(shopId,Param);			
				break;
			case HANDLER_MARKET_BUY_FAIL_NOT_TRY:
				BuyGoods errGoods = (BuyGoods) msg.obj;
				String errStr = errGoods.goodsInfo;
				if (!Util.isEmptyStr(errStr)) {
					UtilHelper.showCustomDialog(mContext, errStr);
				}
				break;
			case HANDLER_MARKET_BUY_CMCC_SDK_RETRY:
				MobileSDKPayment.getInstance(mContext).CmccSdkRetryBilling();
				break;
			case HANDLER_PAYLIST_SUCCUSS:
				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
				}
				Bundle bun = msg.getData();
				GoodsItem _item = (GoodsItem) msg.obj;
				int from1=msg.arg1;
				final String signParam1 = bun.getString("signParam");
				
				Analytical(signParam1, _item,from1);
				break;
			case HANDLER_PAYLIST_FAIL:
				break;
			default:
				break;
			}
		}
	};

	private void getPaydata(int shopId, String paystr) {
		if (Util.isEmptyStr(paystr)) {
			return;
		}
		try {
			JSONTokener jsonParser = new JSONTokener(paystr);
			JSONObject person = (JSONObject) jsonParser.nextValue();
			String status = person.getString("status");
			String toolinfo = person.getString("toolinfo");
			String retry = person.getString("content");
			GoodsItem item = GoodsItemProvider.getInstance()
					.findGoodsItemById(shopId);
//			Toast.makeText(mContext, retry, Toast.LENGTH_SHORT).show();
			requestBuyPropSMS(item, retry, SMS_PAY);
		} catch (JSONException ex) {
			// 键为null或使用json不支持的数字格式(NaN, infinities)
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * 获取支付参数
	 */
	public void Analytical(String signParam,final GoodsItem _item,final int from){
		if (!Util.isEmptyStr(signParam)) {
			// 是否是大额支付
			String large = UtilHelper.parseStatusXml(signParam, "large");
			// 道具名称
			String name = UtilHelper.parseStatusXml(signParam, "name");
			// 价格
			String price = UtilHelper.parseStatusXml(signParam, "price");
			// 道具内容
			String giftdesc = UtilHelper.parseStatusXml(signParam, "giftdesc");
			// 道具说明
			String intro = UtilHelper.parseStatusXml(signParam, "intro");
			// 显示道具列表
			String plist = UtilHelper.parseStatusXml(signParam, "plist");
			
			if(plist == null || plist.length()<1){
//				Toast.makeText(mContext, "plist为空", Toast.LENGTH_SHORT).show();
				Log.e(TAG, "plist为空");
			}
			
			if(price != null && price.length()>0  && giftdesc.length()>0 && giftdesc!=null){
				CharSequence price_color=getPayListColorStr(price);
				CharSequence giftdesc_color=getPayListColorStr(giftdesc);
				PayListDialog.setPaylistDialog(name, price_color,giftdesc_color  ,intro, plist,large,_item);
			}else{
//				Toast.makeText(mContext, "支付列表参数异常！", Toast.LENGTH_SHORT).show();
				Log.e(TAG, "支付列表参数异常！");
			}
			
			
			if(large.equals("1")){
				startPlistdialog(from);
			}else if(large.equals("0")){
				if(IS_BUY_CANCLE){
					// 去请求支付，则把此标志还原
					IS_BUY_CANCLE = false;
					startPlistdialog(from);
				}else{
					requestBuyProp(_item, isConfirm,0,PAY_LIST_ALL,from);
				}
			}else {
//				Toast.makeText(mContext, "支付列表参数异常，请稍后再试！", Toast.LENGTH_SHORT).show();
				Log.e(TAG, "大小额参数异常！");
			}

		}else{
			Toast.makeText(mContext, mContext.getString(R.string.pay_error),Toast.LENGTH_SHORT).show();
		}
	}
	

	public static final int lightblue=0x00ffea;
	/**
	 * 获取多种支付颜色字体
	 * 
	 * @param item
	 * @return
	 */
	private CharSequence getPayListColorStr(String str) {
		if(!str.contains("{") || !str.contains("}")){
			return str;
		}
		int first = str.indexOf("{");
		int first_size = str.indexOf("}", first);
		str=str.replace("{", " ");
		str=str.replace("}", " ");

		int color = mContext.getResources().getColor(R.color.pay_text_color);
		SpannableStringBuilder builder = new SpannableStringBuilder(str);
		builder.setSpan(new ForegroundColorSpan(color), first, 
				first_size, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		return builder;
	}
	
	
	/**
	 * 发送获取多种支付列表请求
	 * 
	 * @param isConfirm
	 *            是否二次确认
	 * @param from
	 *            从哪里请求，0快捷购买，1商城
	 */
	private long currentTime = 0;
	public void requestBuyPropPlist(final GoodsItem item, boolean isConfirm, int from) {
		
		if ((System.currentTimeMillis() - currentTime) > 2500) {
			currentTime = System.currentTimeMillis();
		}else{
			return;
		}
		
		this.isConfirm = isConfirm;
		mProgressDialog = UtilHelper.showProgress(mContext, null,
				mContext.getString(R.string.pay_login), false, true);
		int signType = 0;
		final int mobileCardType = UtilHelper.getMobileCardType(mContext);
		String externalData = PayUtils.getPayExternal(mContext, mobileCardType,
				false,PAY_LIST_ALL,null);
		requestWhichPayList(2, signType, externalData, mPayHandler, item,from);
	}
	/**
	 * 发送购买请求
	 * 
	 * @param isConfirm
	 *            是否二次确认
	 * @param paysign
	 *            支付类型
	 * @param paytype
	 *            上传参数类型
	 *            
	 */
	public void requestBuyProp(final GoodsItem item, boolean isConfirm, int paysign,int paytype,final int from) {
		this.isConfirm = isConfirm;
		if(mProgressDialog == null || !mProgressDialog.isShowing()){
			mProgressDialog = UtilHelper.showProgress(mContext, null,
					mContext.getString(R.string.pay_login), false, true);
		}
		
		int signType = 0;
		if(paytype == PAY_LIST_NO){
			signType = paysign;
		}
		final int mobileCardType = UtilHelper.getMobileCardType(mContext);
		String externalData = PayUtils.getPayExternal(mContext, mobileCardType,
				false,paytype,null);
		requestWhichPay(2, signType, externalData, mPayHandler, item,from);
	}
	
	/**
	 * 支付宝发送购买请求
	 */
	public void requestBuyProp(final GoodsItem item) {
		this.isConfirm = false;
		mProgressDialog = UtilHelper.showProgress(mContext, null,
				mContext.getString(R.string.pay_login), false, true);
		int signType = PAY_SIGN_ALIPAY; // 支付宝支付
		final int mobileCardType = UtilHelper.getMobileCardType(mContext);
		String externalData = PayUtils.getPayExternal(mContext, mobileCardType,
				false,PAY_LIST_NO,null);
		requestWhichPay(2, signType, externalData, mPayHandler, item,-1);
	}

	/**
	 * 支付失败时短信购买
	 */
	public void requestBuyPropSMS(final GoodsItem item,String retry,int from) {
		this.isConfirm = false;
		if(mProgressDialog == null || !mProgressDialog.isShowing()){
			mProgressDialog = UtilHelper.showProgress(mContext, null,
					mContext.getString(R.string.pay_login), false, true);
		}
		int signType = PAY_SIGN_UNICOM_SMS; // 支付宝支付
		final int mobileCardType = UtilHelper.getMobileCardType(mContext);
		String externalData = PayUtils.getPayExternal(mContext, mobileCardType,
				false,PAY_LIST_ALL,retry);
		requestWhichPay(2, signType, externalData, mPayHandler, item,from);
	}

	/**
	 * 请求购买头像
	 * 
	 * @param head
	 */
	public void requestBuyHead(final HeadInfo head) {
		short currencyType = head.getCurrencyType();
		if (currencyType == 3 || currencyType == 4) { // 乐豆或者金币购买
			int userId1 = FiexedViewHelper.getInstance().getUserId();
			String token = FiexedViewHelper.getInstance().getUserToken();
			short ONE = 1;
			long CLISEC = 0;
			String channelId = AppConfig.channelId + "#"
					+ AppConfig.childChannelId;
			buyMarketGoods(userId1, MOBILEPLATID, currencyType, head.getId(),
					token, channelId, "", CLISEC, ONE, AppConfig.gameId);
		} else {
			GoodsItem item = new GoodsItem(head); // 头像转换成商品
			requestBuyPropPlist(item, AppConfig.isConfirmon, MARKET_BUY);
		}
	}

	/**
	 * 获取购买颜色字体
	 * 
	 * @param item
	 * @return
	 */
	private CharSequence getCmccPayColorStr(final GoodsItem item, String number) {
		StringBuffer sb = new StringBuffer();

		String cmccBuying = mContext.getString(R.string.sms_buying);
		String paySuccess = mContext.getString(R.string.buy_success);
		String priceInfo = mContext.getString(R.string.cmcc_sms_price_info);
		// String phoneNum=mContext.getString(R.string.sms_server_num);
		// wanghj 2014-2-20修改格式
		String goodsName = item.goodsName;
		String ledouDesc = item.goodsPresented;

		String price = item.getGoodsValue();

		sb.append(cmccBuying);
		sb.append(goodsName);
		sb.append(priceInfo);
		sb.append(price);
		if (!Util.isEmptyStr(ledouDesc)) { // 购买成功后可送XXXXXXX
			sb.append(paySuccess);
			sb.append(ledouDesc);
		}
		sb.append(mContext.getString(R.string.sms_free_if_cancel));
		// sb.append('\n');
		//
		//
		// sb.append('\n');
		// sb.append(phoneNum);
		// sb.append(number);
		//
		// sb.append(mContext.getString(R.string.mobile_pay_info));

		int first = cmccBuying.length();
		int first_size = goodsName.length();

		int second = first + first_size + priceInfo.length();
		int second_size = price.length();
		int color = 0xffffff00;
		SpannableStringBuilder builder = new SpannableStringBuilder(sb);
		builder.setSpan(new ForegroundColorSpan(color), first, first
				+ first_size, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		builder.setSpan(new ForegroundColorSpan(color), second, second
				+ second_size, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

		if (!Util.isEmptyStr(ledouDesc)) {
			int third = second + second_size + paySuccess.length();
			int third_size = ledouDesc.length();
			builder.setSpan(new ForegroundColorSpan(color), third, third
					+ third_size, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		}
		// int
		// second=first+first_size+paySuccess.length()+ledouDesc.length()+priceInfo.length()+1;
		// int second_size=price.length();
		// CharSequence colorStr =
		// setPayTextStyle(sb.toString(),first,first_size,second ,second_size);

		return builder;
	}

	/**
	 * 获取购买颜色字体
	 * 
	 * @param item
	 * @return
	 */
	private CharSequence getMdoPayColorStr(final GoodsItem item, String number) {
		StringBuffer sb = new StringBuffer();

		String mdoBuying = mContext.getString(R.string.sms_mdo_buying);
		String goodsInfo = mContext.getString(R.string.mdo_goods_info);
		String phoneNum = mContext.getString(R.string.sms_server_num);
		String priceInfo = mContext.getString(R.string.sms_price_info);
		String smsCount = mContext.getString(R.string.sms_count);
		String smsUnit = mContext.getString(R.string.sms_unit);
		String smsPayInfo = mContext.getString(R.string.sms_pay_info);

		String paySuccess = mContext.getString(R.string.buy_success);

		String goodsName = item.goodsName;
		String price = item.getGoodsValue();
		String ledouDesc = item.goodsPresented;

		sb.append(mdoBuying);
		sb.append(goodsInfo);
		sb.append(goodsName);
		sb.append('\n');
		sb.append(phoneNum);
		sb.append(number);
		sb.append('\n');

		sb.append(priceInfo);
		sb.append(price);
		sb.append(smsCount);
		sb.append(price);
		sb.append(smsUnit);
		sb.append(smsPayInfo);

		if (!Util.isEmptyStr(ledouDesc)) {
			sb.append(paySuccess);
			sb.append(ledouDesc);
		}
		sb.append("。");
		sb.append(mContext.getString(R.string.mobile_pay_info));

		int first = mdoBuying.length() + goodsInfo.length();
		int first_size = goodsName.length();

		int second = first + first_size + phoneNum.length() + number.length()
				+ priceInfo.length() + 2;
		int second_size = price.length();

		int third = second + second_size + smsCount.length();
		int third_size = price.length() + smsUnit.length();
		CharSequence colorStr = setPayTextStyle(sb.toString(), first,
				first_size, second, second_size, third, third_size);
		return colorStr;
	}

	/**
	 * 保存购买的商品信息
	 * 
	 * @param shopId
	 * @param startTime
	 */
	public static void setBuyingRecord(int shopId, long startTime) {
		if (buyMap == null) {
			buyMap = new SparseArray<Long>();
		}
		buyMap.put(shopId, startTime);
	}

	/**
	 * 获取购买的商品信息
	 * 
	 * @param shopId
	 * @return
	 */
	public static long getBuyingRecord(int shopId) {
		long res = 0;
		if (buyMap != null) {
			Long time = buyMap.get(shopId);
			if (time != null) {
				res = time.longValue();
			}

		}
		return res;
	}

	/**
	 * 删除购买的商品信息
	 * 
	 * @param shopId
	 */
	public static void removeBuyingRecord(int shopId) {
		if (buyMap != null) {
			buyMap.delete(shopId);
		}
	}

	/**
	 * 购买超时提示
	 */
	public static boolean isBuyingTimeOut(final int shopId) {
		long time = getBuyingRecord(shopId);
		long estimatedTime = System.currentTimeMillis() - time;
		final int statusBit = FiexedViewHelper.getInstance().getUserStatusBit();
		int limitTime = PayUtils.getBuyLimitTime(statusBit);
		if (estimatedTime < (long) limitTime * 1000) {
			// 弹出提示
			UtilHelper.showCustomDialog(mContext,
					mContext.getString(R.string.buyfail));
			return false;
		}
		return true;
	}

	/**
	 * 设置短信购买字体颜色
	 * 
	 * @param text
	 * @param first
	 * @param first_size
	 * @return
	 */
	public static CharSequence setPayTextStyle(CharSequence text, int first,
			int first_size) {
		int color = Color.YELLOW;
		SpannableStringBuilder builder = new SpannableStringBuilder(text);
		builder.setSpan(new ForegroundColorSpan(color), first, first
				+ first_size, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

		builder.setSpan(new ForegroundColorSpan(color), text.length() - 5,
				text.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		return builder;
	}

	/**
	 * 设置短信购买字体颜色
	 * 
	 * @param text
	 * @param first
	 * @param first_size
	 * @return
	 */
	public static CharSequence setPayTextStyle(CharSequence text, int first,
			int first_size, int second, int second_size) {
		int color = Color.YELLOW;
		SpannableStringBuilder builder = new SpannableStringBuilder(text);
		builder.setSpan(new ForegroundColorSpan(color), first, first
				+ first_size, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		builder.setSpan(new ForegroundColorSpan(color), second, second
				+ second_size, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

		builder.setSpan(new ForegroundColorSpan(color), text.length() - 5,
				text.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		return builder;
	}

	/**
	 * 设置短信购买字体颜色
	 * 
	 * @param text
	 * @param first
	 * @param first_size
	 * @return
	 */

	public static CharSequence setPayTextStyle(CharSequence text, int first,
			int first_size, int second, int second_size, int third,
			int third_size) {
		int color = Color.YELLOW;
		SpannableStringBuilder builder = new SpannableStringBuilder(text);
		builder.setSpan(new ForegroundColorSpan(color), first, first
				+ first_size, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		builder.setSpan(new ForegroundColorSpan(color), second, second
				+ second_size, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		builder.setSpan(new ForegroundColorSpan(color), third, third
				+ third_size, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);

		builder.setSpan(new ForegroundColorSpan(color), text.length() - 5,
				text.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
		return builder;
	}

	/**
	 * 标记用户为付费用户
	 */
	private static void markUserPaySuccess() {
		UserInfo user = HallDataManager.getInstance().getUserMe();
		if (user != null) {
			user.statusBit |= (1 << 24); // 第25位标示是否付费用户
		}
	}

	/**
	 * 发起请求获取多种支付列表
	 * 
	 * @author JiangYinZhi
	 */
	private void requestWhichPayList(final int dataType, final int signType,
			final String external, final Handler handler, final GoodsItem item, final int from) {
		final int point = item.pointValue;
		final int shopID = item.shopID;
		// 创建发送的数据包
		TDataOutputStream tdos = new TDataOutputStream(false);
		tdos.writeByte((byte) dataType); // 数据类型1-充值 2-购买
		tdos.writeByte((byte) signType); // 签名类型1-支付宝 2-其他
		tdos.writeInt(point);// 总金额 表示为分,如10元= 1000分
		tdos.writeInt(shopID);// 商品编号，充值ShopID=0
		tdos.writeInt(1); // 商品数量,充值商品数量=0
		tdos.writeUTFShort(external); // 扩展数据，0
		NetSocketPak pointBalance = new NetSocketPak(MDM_PROP,
				MSUB_CMD_PAYLIST_REQ, tdos);
		// 定义接受数据的协议
		short[][] parseProtocol = { { MDM_PROP, MSUB_CMD_PAYLIST_RESP } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			public boolean doReceive(NetSocketPak netSocketPak) {
				// 解析接受到的网络数据
				TDataInputStream tdis = netSocketPak.getDataInputStream();
				tdis.setFront(false);
				byte result = tdis.readByte(); // 充值结果 0-成功1-失败
				if (result == 0) {// 成功

					tdis.readByte();// 签名类型1-支付宝 2-易宝 3-酷派
					int signType = tdis.readByte()&0xff;
					tdis.readShort(); // 参数总长度，超过2k,
					String s = tdis.readUTFShort();
					String signParam = "";
					try {
						signParam = URLDecoder.decode(s, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					Message msg = handler.obtainMessage();
					Bundle b = new Bundle();
					b.putInt("signType", signType);
					b.putString("signParam", signParam);
					msg.setData(b);
					msg.obj = item;
					msg.arg1=from;
					msg.what = HANDLER_PAYLIST_SUCCUSS;
					handler.sendMessage(msg);

				} else {// 失败
					String error = tdis.readUTFShort(); // 消息文本内容
					if (error == null) { // 服务器要是没配好可能崩，所以要检测
						error = "";
					}
					Message msg = handler.obtainMessage();
					msg.obj = error;
					msg.what = HANDLER_SIGNTYPE_FAIL;
					// SIM卡类型：联通
					handler.sendMessage(msg);
				}
				// 数据处理完成，终止继续解析
				return true;
			}
		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		// 发送协议
		NetSocketManager.getInstance().sendData(pointBalance);
		// 清理协议对象
		pointBalance.free();

	}
	
	
	/**
	 * 发起请求获取使用的支付方式
	 * 
	 * @author JiangYinZhi
	 */
	private void requestWhichPay(final int dataType, final int signType,
			final String external, final Handler handler, final GoodsItem item,final int from) {
		final int point = item.pointValue;
		final int shopID = item.shopID;
		// 创建发送的数据包
		TDataOutputStream tdos = new TDataOutputStream(false);
		tdos.writeByte((byte) dataType); // 数据类型1-充值 2-购买
		tdos.writeByte((byte) signType); // 签名类型1-支付宝 2-其他
		tdos.writeInt(point);// 总金额 表示为分,如10元= 1000分
		tdos.writeInt(shopID);// 商品编号，充值ShopID=0
		tdos.writeInt(1); // 商品数量,充值商品数量=0
		tdos.writeUTFShort(external); // 扩展数据，0
		NetSocketPak pointBalance = new NetSocketPak(MDM_PROP,
				MSUB_CMD_SIGNATURE_REQ, tdos);
		// 定义接受数据的协议
		short[][] parseProtocol = { { MDM_PROP, MSUB_CMD_SIGNATURE_RESP } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			public boolean doReceive(NetSocketPak netSocketPak) {
				// 解析接受到的网络数据
				TDataInputStream tdis = netSocketPak.getDataInputStream();
				tdis.setFront(false);
				byte result = tdis.readByte(); // 充值结果 0-成功1-失败
				if (result == 0) {// 成功

					tdis.readByte();// 签名类型1-支付宝 2-易宝 3-酷派
					int signType = tdis.readByte()&0xff;
					tdis.readShort(); // 参数总长度，超过2k,
					String s = tdis.readUTFShort();
					String signParam = "";
					try {
						signParam = URLDecoder.decode(s, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					Message msg = handler.obtainMessage();
					Bundle b = new Bundle();
					b.putInt("signType", signType);
					b.putString("signParam", signParam);
					msg.setData(b);
					msg.obj = item;
					msg.arg1=from;
					msg.what = HANDLER_SIGNTYPE_SUCCUSS;
					handler.sendMessage(msg);

				} else {// 失败
					String error = tdis.readUTFShort(); // 消息文本内容
					if (error == null) { // 服务器要是没配好可能崩，所以要检测
						error = "";
					}
					Message msg = handler.obtainMessage();
					msg.obj = error;
					msg.what = HANDLER_SIGNTYPE_FAIL;
					// SIM卡类型：联通
					handler.sendMessage(msg);
				}
				// 数据处理完成，终止继续解析
				return true;
			}
		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		// 发送协议
		NetSocketManager.getInstance().sendData(pointBalance);
		// 清理协议对象
		pointBalance.free();

	}

	/**
	 * @Description: 购买商品协议接收
	 * @param shopID
	 * @param handler
	 */
	public void netReceive(final int shopId) {
		short[][] parseProtocol = { { MDM_PROP, MSUB_CMD_BUY_SHOP_RESP } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {

			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				TDataInputStream tdis = netSocketPak.getDataInputStream();
				if (tdis == null) {
					return true;
				}

				BuyGoods goods = new BuyGoods();
				goods.userId = tdis.readInt(); // 用户ID
				goods.propId = tdis.readInt(); // 道具ID,商品ID

				// '33' '扣费失败'
				// '34' 'WAP 购买失败，您可以通过CMNET或WIFI登录，进入商城购买！'
				// '35' '余额不足'
				byte result = tdis.readByte(); // 成功失败

				goods.goodsInfo = tdis.readUTFShort(); // 具体描述信息
				goods.cliSec = tdis.readLong(); // cliSec客户端标识
				int propCount = tdis.readByte(); // 道具数量
				goods.propCount = propCount;
				HPropData propData[] = null;
				if (propCount > 0) {
					propData = new HPropData[propCount]; // 道具数据数组
					for (int i = 0; i < propCount; i++) {
						propData[i] = new HPropData(tdis); // 道具数据
					}

				}
				goods.propData = propData;
				goods.hilightWords = tdis.readUTFShort();
				removeBuyingRecord(shopId);

				Message msg = mPayHandler.obtainMessage();
				msg.obj = goods;
				msg.arg1 = shopId;
				short headPayType = HeadManager.getInstance().getPayType(
						goods.propId); // 头像购买方式，如果购买不是头像返回-1

				if (result == 0 || result == 48) { // 0表示购买需要使用道具，48表示直接到帐道具如获取乐豆
					if (headPayType != 0) { // 不是金币或乐豆买头像
						markUserPaySuccess();
					}
				}
				if (headPayType == -1) { // 非头像购买，使用金钱购买
					if (result == 0) { // 购买完成 0
						msg.what = PayManager.HANDLER_MARKET_BUY_SUCCESS_UPDATE;
					} else if (result == 33) { // 扣费失败
						msg.what = PayManager.HANDLER_MARKET_BUY_FAIL_AND_TRY;
					} else if (result == 34) { // WAP购买失败，您可以通过CMNET或WIFI登录，进入商城购买
						msg.what = PayManager.HANDLER_MARKET_BUY_FAIL_AND_TRY;
					} else if (result == 35) { // 余额不足
						msg.what = PayManager.HANDLER_MARKET_BUY_FAIL_AND_TRY;
					}else if(result == 39){ //移动SDK重复购买
						msg.what=PayManager.HANDLER_MARKET_BUY_CMCC_SDK_RETRY;
					} else if (result == 48) { // 购买成功直接送豆
						msg.what = PayManager.HANDLER_MARKET_BUY_BEAN_SUCCESS;
						msg.arg2 = result;
					}else if(result == 50){
						msg.what = PayManager.HANDLER_MARKET_BUY_FAIL_AND_SMS;//短信购买失败处理
					} else {
						msg.what = PayManager.HANDLER_MARKET_BUY_FAIL_NOT_TRY;
					}
				} else { // 头像购买，使用游戏货币购买
					if (result == 0) {
						msg.what = PayManager.HANDLER_BUY_HEAD_SUCCESS;
					} else {
						msg.what = PayManager.HANDLER_BUY_HEAD_FAIL;
						msg.arg2 = result;
					}
				}

				mPayHandler.sendMessage(msg);
				/** 购买成功统计 */
				AnalyticsUtils.onClickEvent(mContext, "052");
				return true;
			}
		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
	}

	/**
	 * @Title: requestMarketGoods
	 * @Description: 解析请求购买商品协议
	 * @param userID
	 *            用户ID
	 * @param platID
	 *            平台ID
	 * @param currency
	 *            货币类型 （1：虚拟币 2：移动点数）
	 * @param shopID
	 *            商品ID
	 * @param token
	 *            用户token串
	 * @param channelID
	 *            渠道数据
	 * @param key
	 *            如：PlatID=3，则Key为移动用户伪码key长度（来自移动）
	 * @param cliSec
	 *            客户端标识
	 * @param shopCount
	 *            购买商品数量,默认为1
	 * @version: 2012-7-25 下午04:28:11
	 * @param handler
	 */
	public void buyMarketGoods(int userID, int platID, short currency,
			final int shopID, String token, String channelID, String key,
			long cliSec, short shopCount, int gameId) {
		int playid = FiexedViewHelper.getInstance().getGameType();
		// 创建发送的数据包
		TDataOutputStream tdos = new TDataOutputStream();
		tdos.setFront(false);
		tdos.writeInt(userID);
		tdos.writeInt(platID);
		tdos.writeShort(currency);
		tdos.writeInt(shopID);
		tdos.writeUTFShort(token);
		tdos.writeUTFShort(channelID);
		tdos.writeUTFShort(key);
		tdos.writeLong(cliSec);
		tdos.writeShort(shopCount);
		tdos.writeInt(gameId);
		
		switch (CardZoneDataListener.NODE_DATA_PROTOCOL_VER) {
		case CardZoneDataListener.VERSION_1:// 列表协议第一版，每个节点单独请求
		case CardZoneDataListener.VERSION_2:// 列表协议第二版，支持所有列表数据一次请求完成，本地缓存列表数据
			playid = 0;
			tdos.writeByte(playid); // 玩法选择
			break;
		case CardZoneDataListener.VERSION_3:// 列表协议第三版，支持斗地主3种玩法，列表数据zip压缩，本地缓存列表数据
			tdos.writeByte(playid); // 玩法选择
			break;
		default:
			break;
		}
		
		NetSocketPak requestGoods = new NetSocketPak(MDM_PROP,
				MSUB_CMD_BUY_SHOP_REQ, tdos);
		// 定义接受协议数据
		// 购买成功回调
		netReceive(shopID);
		// 发送协议
		NetSocketManager.getInstance().sendData(requestGoods);
		// 清理协议对象
		requestGoods.free();
		long startTime = System.currentTimeMillis();
		PayManager.setBuyingRecord(shopID, startTime);
	}

	/**
	 * @Title: requestBackPackList
	 * @Description: 获取背包列表
	 * @param userID
	 * @param PropCount
	 *            作为参数传递商品个数
	 * @param shopID
	 *            作为参数传递商品ID
	 * @version: 2012-7-26 上午11:08:09
	 */
	public void requestBackPackList(int userID, final int shopId,
			final BuyGoods buyGoods) {
		BackPackItemProvider.getInstance().init();
		// 创建发送的数据包
		TDataOutputStream tdos = new TDataOutputStream();
		tdos.writeInt(userID, false);
		NetSocketPak pointBalance = new NetSocketPak(MDM_PROP,
				MSUB_CMD_PACK_PROP_LIST_REQ_EX, tdos);
		// 定义接受数据的协议
		short[][] parseProtocol = { { MDM_PROP, MSUB_CMD_PACK_PROP_LIST_RESP } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {
			public boolean doReceive(NetSocketPak netSocketPak) {
				TDataInputStream tdis = netSocketPak.getDataInputStream();
				tdis.setFront(false);

				int total = tdis.readShort(); // 商品总个数
				int num = tdis.readShort(); // 当次商品个数
				BackPackItem[] backPackItems;
				if (num <= 0) {
					// 未查询到相关记录,请稍后再试...
					Message msg = mPayHandler.obtainMessage();
					msg.what = HANDLER_PACK_QUERY_SUCCESS_NODATA;
				} else {
					backPackItems = new BackPackItem[num];
					// 累计接受到数据到数组中
					for (int i = 0; i < num; i++) {
						backPackItems[i] = new BackPackItem(tdis);
					}
					BackPackItemProvider.getInstance().setBackPackItem(
							backPackItems);
					curBackpackNum += num; // 积累保存到全局变量，记录当前返回累计数目

					if (curBackpackNum == total) {
						Message msg = mPayHandler.obtainMessage();
						msg.what = HANDLER_PACK_QUERY_SUCCESS;
						msg.arg1 = shopId;
						msg.obj = buyGoods;
						mPayHandler.sendMessage(msg);
						BackPackItemProvider.getInstance().setFinish(true);
						curBackpackNum = 0;
					}

				}

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

	/***
	 * @Title: requestUseMarketGoods
	 * @Description: 立即使用改道具协议
	 * @param userID
	 *            用户ID
	 * @param propID
	 *            道具ID
	 * @param indexID
	 *            道具索引编号
	 * @param exData
	 *            附加参数（如需要GameID、RoomID的道具，可传入相关数据，默认为0）
	 * @param cliSec
	 *            客户端标识
	 * @param cbType
	 *            0-无扩展数据1 为充值卡 2 为话费券 3.实物道具
	 * @param extXml
	 *            扩展数据
	 * @version: 2012-7-26 上午09:54:59
	 */
	public void requestUseMarketGoods(int userID, int propID, long indexID,
			int exData, long cliSec, byte cbType) {

		// 创建发送的数据包
		TDataOutputStream tdos = new TDataOutputStream(false);
		tdos.writeInt(userID);
		tdos.writeInt(propID);
		tdos.writeLong(indexID);
		tdos.writeInt(exData);
		tdos.writeLong(cliSec);
		tdos.writeByte(cbType);

		NetSocketPak useGoods = new NetSocketPak(MDM_PROP,
				MSUB_CMD_USE_PROP_REQ, tdos);
		// 定义接受数据的协议
		short[][] parseProtocol = { { MDM_PROP, MSUB_CMD_USE_PROP_RESP } };
		// 创建协议解析器
		NetPrivateListener nPListener = new NetPrivateListener(parseProtocol) {

			@Override
			public boolean doReceive(NetSocketPak netSocketPak) {
				try {
					TDataInputStream tdis = netSocketPak.getDataInputStream();
					tdis.readInt();
					tdis.readInt();
					tdis.readInt();// result
					if (tdis.available() > 0) {
						/** long IndexID= */
						tdis.readLong();
					}
					if (tdis.available() > 0) {
						String msgStr = tdis.readUTFByte();
						Message msg = mPayHandler.obtainMessage();
						msg.obj = msgStr;
						msg.what = HANDLER_MARKET_USE_SUCCESS;
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

				return true;
			}
		};
		// 注册协议解析器到网络数据分发器中
		NetSocketManager.getInstance().addPrivateListener(nPListener);
		// 发送协议
		NetSocketManager.getInstance().sendData(useGoods);
		// 清理协议对象
		useGoods.free();

	}
	/***
	 * 获取支付类型列表 是否是第二次支付，第二次支付只是用支付宝支付
	 * 
	 * @return
	 */
	public static String getPlistString(boolean isPayAgain) {

		StringBuffer sb = new StringBuffer();

		if (!isPayAgain) {
			//sb.append(PAY_SIGN_MOBILE).append(",");// 移动短信支付
			//sb.append(PAY_SIGN_MOBILE_WAP).append(",");// cmwap支付
			//sb.append(PAY_SIGN_MOBILE_MM).append(",");// 移动MM支付
			//sb.append(PAY_SIGN_UNICOM_UNIPAY).append(",");// 联通支付
			//sb.append(PAY_SIGN_UNICOM_SMS).append(",");// 联通短信支付
			//sb.append(PAY_SIGN_TELECOM).append(",");// 电信支付
			//sb.append(PAY_SIGN_TELECOM_EGAME).append(",");//电信爱游戏
			//sb.append(PAY_SIGN_WX_SDK).append(",");// 微信支付
			//sb.append(PAY_SIGN_SKYMOBILE).append(",");// 斯凯SDK支付
			//if (AppConfig.isOpenPayByCmccSdk()) {
			sb.append(PAY_SIGN_MOBILE_SDK).append(",");// 移动SDK支付
			//}
		}

		sb.append(PAY_SIGN_ALIPAY);// 支付宝支付

		return sb.toString();
	}

	/***
	 * 获取多种支付时短代支付类型
	 * 
	 * @return
	 */
	public static String getSigntype() {
		StringBuffer sb = new StringBuffer();
		//sb.append(PAY_SIGN_MOBILE).append(",");// 移动短信支付
		//sb.append(PAY_SIGN_MOBILE_WAP).append(",");// cmwap支付
		//sb.append(PAY_SIGN_MOBILE_MM).append(",");// 移动MM支付
		//sb.append(PAY_SIGN_UNICOM_UNIPAY).append(",");// 联通支付
		//sb.append(PAY_SIGN_UNICOM_SMS).append(",");// 联通短信支付
		//sb.append(PAY_SIGN_TELECOM).append(",");// 电信支付
		//sb.append(PAY_SIGN_TELECOM_EGAME).append(",");//电信爱游戏
		//if (AppConfig.isOpenPayByCmccSdk()) {
		sb.append(PAY_SIGN_MOBILE_SDK).append(",");// 移动SDK支付
		//}
		return sb.toString();
	}
	/***
	 * 获取客户端默认支付类型
	 * 
	 * @return
	 */
	public static int getSigntypeString() {
		int signType = -1;
		// SIM卡类型:1-移动;2-联通;3-电信
		final int mobileCardType = UtilHelper.getMobileCardType(mContext);
		if (mobileCardType == UtilHelper.UNICOM_TYPE) {// 联通
			// signType=PAY_SIGN_UNICOM_UNIPAY;//联通SDK
			signType = PAY_SIGN_UNICOM_SMS; // 联通SMS
		} else if (mobileCardType == UtilHelper.TELECOM_TYPE) {// 电信
			signType = PAY_SIGN_TELECOM;
		} else if (mobileCardType == UtilHelper.MOVE_MOBILE_TYPE) {// 移动
			String mobileUserId = MobileHttpApiMgr.getInstance()
					.getOnlineGameUserId();
			if (Util.isCMWap(mContext) && !Util.isEmptyStr(mobileUserId)) {
				signType = PAY_SIGN_MOBILE_WAP;
			} else {
				// 非wap即发短信
				// signType=PAY_SIGN_MOBILE_MM;
				signType = PAY_SIGN_MOBILE;
			}
		} else {// 第三方
			signType = PAY_SIGN_ALIPAY;
		}
		return signType;
	}
	
	public void setSharedPreferences(boolean ridiotype){
		pay_ridio_type=ridiotype;
		Util.setBooleanSharedPreferences(mContext, "ridio_type", ridiotype);
	}
	
	private boolean getSharedPreferences(){
		return Util.getBooleanSharedPreferences(mContext, "ridio_type", pay_ridio_type);
	}
	
	public static void toBuy(int signType, String signParam, int shopID,int from) {
		
		
		String smsOrder = UtilHelper.parseStatusXml(signParam,
				"orderId");
	    AppConfig.talkingData(AppConfig.ACTION_MARKET_YES ,shopID,signType,smsOrder); 
		
		switch (signType) {
		case PAY_SIGN_MOBILE:// 移动短信支付
			MobilePayment.getInstance(mContext).Analytical(shopID, signParam,from);
			break;
		case PAY_SIGN_MOBILE_MM:// 移动MM支付
		    MMPayment.getInstance(mContext).Analytical(shopID,signParam);
		    break;
		case PAY_SIGN_TELECOM:// 电信支付
			TelecomPayment.getInstance(mContext).Analytical(shopID, signParam,from);
			break;
		case PAY_SIGN_ALIPAY:// 支付宝支付
			AlixPayment.getInstance(mContext).Analytical(shopID, signParam);
			break;
		case PAY_SIGN_MOBILE_WAP: //移动cmwap
			MobileCMwapPayment.getInstance(mContext).Analytical(shopID,
					signParam,from);
			break;
		/*case PAY_SIGN_UNICOM_UNIPAY:// 联通unipay支付
			UnipayPayment.getInstance(mContext).Analytical(shopID, signParam);
			break;*/
		case PAY_SIGN_UNICOM_SMS: // 联通短信支付
			UnicomSmsPayment.getInstance(mContext)
					.Analytical(shopID, signParam,from);
			break;
		case PAY_SIGN_MOBILE_SDK:  //移动SDK支付
			MobileSDKPayment.getInstance(mContext).Analytical(shopID,signParam);
			break;
		//case PAY_SIGN_SKYMOBILE:// 斯凯支付
		//SkyMobilePay.getInstance(mContext).pay(signParam,shopID);
		//	break;
		case PAY_SIGN_WX_SDK:  //  微信支付
			WXPayment.getInstance(mContext).Analytical(shopID, signParam);
			break;
		case PAY_SIGN_TELECOM_EGAME://电信爱游戏
			TelecomEgamePayment.getInstance(mContext).Analytical(shopID,
					signParam);
			break;
		default:
			Toast.makeText(mContext, mContext.getString(R.string.buyerror),
					Toast.LENGTH_LONG).show();
			break;
		}

		// 点击商城确认 购买统计事件--支付宝
		AnalyticsUtils.onClickEvent(mContext, "017");
	}

	private void handMessageToCardZoneForSmallMoneyPkg(int propId) {
		if (propId == AppConfig.smallMoneyPkgPropId) {
			GoodsItemProvider.getInstance().removeSmalMoneyPkg(mContext);
			CardZoneFragment fragment = FiexedViewHelper.getInstance()
					.getCardZoneFragment();
			if (fragment != null) {
				fragment.hideSmallMoneyPkgView();
			}

		}
	}
}
